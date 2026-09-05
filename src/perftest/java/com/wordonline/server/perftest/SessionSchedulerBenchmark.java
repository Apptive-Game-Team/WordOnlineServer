package com.wordonline.server.perftest;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;

/**
 * In-process comparison of the server's current thread-per-session loop and a shared actor loop.
 * This intentionally has no Spring or database dependencies, keeping scheduler cost visible.
 */
public final class SessionSchedulerBenchmark {

    private static final long FRAME_NANOS = Duration.ofMillis(50).toNanos();
    private static volatile long blackHole;

    private SessionSchedulerBenchmark() {
    }

    public static void main(String[] arguments) throws Exception {
        Settings settings = Settings.parse(arguments);
        System.out.println("mode,sessions,duration_seconds,frames,frames_per_second,late_percent," +
                "p50_lateness_milliseconds,p95_lateness_milliseconds,p99_lateness_milliseconds," +
                "max_lateness_milliseconds,actions,process_cpu_seconds,heap_used_megabytes," +
                "resident_set_megabytes,peak_threads");

        for (Mode mode : settings.modes()) {
            if (settings.warmupSeconds > 0) {
                run(mode, settings.withDuration(settings.warmupSeconds), false);
            }
            run(mode, settings, true);
        }
    }

    private static void run(Mode mode, Settings settings, boolean printResult) throws Exception {
        BenchmarkState state = new BenchmarkState(settings);
        Runner runner = mode == Mode.DEDICATED
                ? new DedicatedThreadRunner(state)
                : new SharedActorRunner(state);

        long processCpuStart = processCpuNanos();
        long start = System.nanoTime();
        runner.start();
        runner.awaitCompletion();
        long elapsed = System.nanoTime() - start;
        long processCpuElapsed = processCpuNanos() - processCpuStart;

        if (!printResult) {
            return;
        }

        long[] lateness = state.lateness.snapshot();
        Arrays.sort(lateness);
        double seconds = elapsed / 1_000_000_000.0;
        System.out.printf(Locale.ROOT,
                "%s,%d,%.3f,%d,%.1f,%.3f,%.3f,%.3f,%.3f,%.3f,%d,%.3f,%.1f,%.1f,%d%n",
                mode.cliName, settings.sessions, seconds, state.frames.get(),
                state.frames.get() / seconds, percent(state.lateFrames.get(), state.frames.get()),
                percentileMilliseconds(lateness, 0.50), percentileMilliseconds(lateness, 0.95),
                percentileMilliseconds(lateness, 0.99), percentileMilliseconds(lateness, 1.0),
                state.actions.get(), processCpuElapsed / 1_000_000_000.0,
                usedHeapMegabytes(), residentSetMegabytes(), state.peakThreads.get());
    }

    private static final class DedicatedThreadRunner implements Runner {
        private final BenchmarkState state;
        private final List<Thread> threads = new ArrayList<>();

        private DedicatedThreadRunner(BenchmarkState state) {
            this.state = state;
        }

        @Override
        public void start() throws InterruptedException {
            CountDownLatch ready = new CountDownLatch(state.settings.sessions);
            CountDownLatch begin = new CountDownLatch(1);
            for (int session = 0; session < state.settings.sessions; session++) {
                Session simulatedSession = new Session(session, state.settings);
                Thread thread = Thread.ofPlatform().name("benchmark-session-" + session).unstarted(() -> {
                    ready.countDown();
                    await(begin);
                    long deadline = System.nanoTime();
                    long stop = deadline + state.settings.durationNanos;
                    while (deadline < stop) {
                        waitUntil(deadline);
                        state.tick(simulatedSession, deadline);
                        deadline += FRAME_NANOS;
                    }
                });
                threads.add(thread);
                thread.start();
            }
            ready.await();
            state.sampleThreads();
            begin.countDown();
        }

        @Override
        public void awaitCompletion() throws InterruptedException {
            for (Thread thread : threads) {
                thread.join();
                state.sampleThreads();
            }
        }
    }

    private static final class SharedActorRunner implements Runner {
        private final BenchmarkState state;
        private Thread dispatcher;

        private SharedActorRunner(BenchmarkState state) {
            this.state = state;
        }

        @Override
        public void start() {
            List<Session> sessions = new ArrayList<>(state.settings.sessions);
            for (int session = 0; session < state.settings.sessions; session++) {
                sessions.add(new Session(session, state.settings));
            }
            dispatcher = Thread.ofPlatform().name("benchmark-actor-dispatcher").start(() -> {
                long start = System.nanoTime();
                long stop = start + state.settings.durationNanos;
                PriorityQueue<ScheduledSession> schedule = new PriorityQueue<>(
                        Comparator.comparingLong(ScheduledSession::deadline));
                for (Session session : sessions) {
                    schedule.add(new ScheduledSession(session, start));
                }
                state.sampleThreads();
                while (!schedule.isEmpty()) {
                    ScheduledSession scheduled = schedule.remove();
                    if (scheduled.deadline >= stop) {
                        continue;
                    }
                    waitUntil(scheduled.deadline);
                    state.tick(scheduled.session, scheduled.deadline);
                    schedule.add(new ScheduledSession(scheduled.session, scheduled.deadline + FRAME_NANOS));
                }
            });
        }

        @Override
        public void awaitCompletion() throws InterruptedException {
            dispatcher.join();
        }

        private record ScheduledSession(Session session, long deadline) {
        }
    }

    private static final class BenchmarkState {
        private final Settings settings;
        private final AtomicLong frames = new AtomicLong();
        private final AtomicLong lateFrames = new AtomicLong();
        private final AtomicLong actions = new AtomicLong();
        private final AtomicLong peakThreads = new AtomicLong();
        private final LongSamples lateness;

        private BenchmarkState(Settings settings) {
            this.settings = settings;
            this.lateness = new LongSamples(Math.multiplyExact(settings.sessions, settings.durationSeconds * 20));
        }

        private void tick(Session session, long deadline) {
            long started = System.nanoTime();
            long late = Math.max(0, started - deadline);
            lateness.add(late);
            frames.incrementAndGet();
            if (late > FRAME_NANOS) {
                lateFrames.incrementAndGet();
            }
            actions.addAndGet(session.drainActions());
            burnCpu(settings.workIterations);
            session.enqueueActions(settings.actionsPerFrame);
            sampleThreads();
        }

        private void sampleThreads() {
            peakThreads.accumulateAndGet(ManagementFactory.getThreadMXBean().getThreadCount(), Math::max);
        }
    }

    private static final class Session {
        private final Queue<Runnable> mailbox = new ConcurrentLinkedQueue<>();
        private final Runnable action;

        private Session(int identifier, Settings settings) {
            action = () -> blackHole = mix(blackHole + identifier + settings.workIterations);
            enqueueActions(settings.actionsPerFrame);
        }

        private void enqueueActions(int count) {
            for (int actionIndex = 0; actionIndex < count; actionIndex++) {
                mailbox.add(action);
            }
        }

        private int drainActions() {
            int drained = 0;
            Runnable action;
            while ((action = mailbox.poll()) != null) {
                action.run();
                drained++;
            }
            return drained;
        }
    }

    private static final class LongSamples {
        private final long[] values;
        private int size;

        private LongSamples(int capacity) {
            values = new long[capacity];
        }

        private synchronized void add(long value) {
            values[size++] = value;
        }

        private synchronized long[] snapshot() {
            return Arrays.copyOf(values, size);
        }
    }

    private record Settings(List<Mode> modes, int sessions, int durationSeconds, int warmupSeconds,
                            int workIterations, int actionsPerFrame, long durationNanos) {

        private static Settings parse(String[] arguments) {
            String modeArgument = value(arguments, "--mode", "both");
            List<Mode> modes = switch (modeArgument) {
                case "dedicated" -> List.of(Mode.DEDICATED);
                case "actor" -> List.of(Mode.ACTOR);
                case "both" -> List.of(Mode.DEDICATED, Mode.ACTOR);
                default -> throw new IllegalArgumentException("unknown mode: " + modeArgument);
            };
            int sessions = positiveInt(arguments, "--sessions", 100);
            int durationSeconds = positiveInt(arguments, "--duration-seconds", 10);
            int warmupSeconds = nonNegativeInt(arguments, "--warmup-seconds", 5);
            int workIterations = positiveInt(arguments, "--work-iterations", 2_000);
            int actionsPerFrame = positiveInt(arguments, "--actions-per-frame", 2);
            return new Settings(modes, sessions, durationSeconds, warmupSeconds,
                    workIterations, actionsPerFrame,
                    Duration.ofSeconds(durationSeconds).toNanos());
        }

        private Settings withDuration(int seconds) {
            return new Settings(modes, sessions, seconds, 0, workIterations, actionsPerFrame,
                    Duration.ofSeconds(seconds).toNanos());
        }

        private static int positiveInt(String[] arguments, String name, int fallback) {
            int parsed = Integer.parseInt(value(arguments, name, Integer.toString(fallback)));
            if (parsed <= 0) {
                throw new IllegalArgumentException(name + " must be positive");
            }
            return parsed;
        }

        private static int nonNegativeInt(String[] arguments, String name, int fallback) {
            int parsed = Integer.parseInt(value(arguments, name, Integer.toString(fallback)));
            if (parsed < 0) {
                throw new IllegalArgumentException(name + " must not be negative");
            }
            return parsed;
        }

        private static String value(String[] arguments, String name, String fallback) {
            for (int index = 0; index < arguments.length; index++) {
                if (arguments[index].equals(name) && index + 1 < arguments.length) {
                    return arguments[index + 1];
                }
            }
            return fallback;
        }
    }

    private enum Mode {
        DEDICATED("dedicated"), ACTOR("actor");

        private final String cliName;

        Mode(String cliName) {
            this.cliName = cliName;
        }
    }

    private interface Runner {
        void start() throws InterruptedException;

        void awaitCompletion() throws InterruptedException;
    }

    private static void waitUntil(long deadline) {
        while (true) {
            long remaining = deadline - System.nanoTime();
            if (remaining <= 0) {
                return;
            }
            LockSupport.parkNanos(remaining);
        }
    }

    private static void await(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(exception);
        }
    }

    private static void burnCpu(int iterations) {
        long value = blackHole;
        for (int iteration = 0; iteration < iterations; iteration++) {
            value = mix(value + iteration);
        }
        blackHole = value;
    }

    private static long mix(long value) {
        value ^= value >>> 33;
        value *= 0xff51afd7ed558ccdl;
        value ^= value >>> 33;
        return value;
    }

    private static long processCpuNanos() {
        java.lang.management.OperatingSystemMXBean bean = ManagementFactory.getOperatingSystemMXBean();
        if (bean instanceof com.sun.management.OperatingSystemMXBean operatingSystem) {
            return operatingSystem.getProcessCpuTime();
        }
        return 0;
    }

    private static double usedHeapMegabytes() {
        MemoryMXBean memory = ManagementFactory.getMemoryMXBean();
        return memory.getHeapMemoryUsage().getUsed() / 1024.0 / 1024.0;
    }

    private static double residentSetMegabytes() {
        try {
            for (String line : Files.readAllLines(Path.of("/proc/self/status"))) {
                if (line.startsWith("VmRSS:")) {
                    String kibibytes = line.substring("VmRSS:".length()).trim().split("\\s+")[0];
                    return Long.parseLong(kibibytes) / 1024.0;
                }
            }
        } catch (IOException | NumberFormatException ignored) {
            // Portable fallback: resident set measurement is Linux-specific.
        }
        return -1;
    }

    private static double percent(long numerator, long denominator) {
        return denominator == 0 ? 0 : numerator * 100.0 / denominator;
    }

    private static double percentileMilliseconds(long[] sorted, double percentile) {
        if (sorted.length == 0) {
            return 0;
        }
        int index = Math.min(sorted.length - 1, (int) Math.ceil(percentile * sorted.length) - 1);
        return sorted[index] / 1_000_000.0;
    }
}
