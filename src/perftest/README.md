# Session scheduler benchmark

Compares two execution models while keeping frame rate, mailbox, action volume, and simulated
frame work identical:

- `dedicated`: one platform thread per session, matching current `GameLoop` execution.
- `actor`: one shared dispatcher thread, with every session scheduled at 20 frames per second.

The benchmark reports delivered frames, frames starting more than one full frame late, lateness
percentiles, process central processing unit time, heap, Linux resident set size, and peak Java
thread count. It excludes Spring, network, database, serialization, and game rules. Results isolate
scheduler cost; they do not predict full production capacity.

Run from `game/` with Java 21. The Gradle task fixes the Java heap at 1 gigabyte and reports one
processor to the Java virtual machine. `taskset` enforces actual single-core scheduling on Linux:

```shell
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 taskset --cpu-list 0 ./gradlew --no-daemon \
  sessionSchedulerBenchmark --args='--mode both --sessions 500 --duration-seconds 30 --work-iterations 2000 --actions-per-frame 2'
```

For a strict 1 gigabyte process and operating-system memory limit, compile outside the container,
then run each mode in a fresh container. This also gives the Java virtual machine one processor and
leaves half the container memory available for thread stacks, Metaspace, native memory, and the
operating system:

```shell
./gradlew compilePerftestJava
docker run --rm --cpus=1 --memory=1g --memory-swap=1g --pids-limit=2048 \
  -v "$PWD/build/classes/java/perftest:/benchmark:ro" eclipse-temurin:21-jdk \
  java -Xms256m -Xmx512m -Xss512k -XX:ActiveProcessorCount=1 \
  -cp /benchmark com.wordonline.server.perftest.SessionSchedulerBenchmark \
  --mode dedicated --sessions 500 --duration-seconds 30 --work-iterations 2000 --actions-per-frame 2
```

Strict container results from 2026-08-21 are under `results/`. The 100-session run used a two-second
warmup and five-second measurement; the 500-session stress run used a five-second warmup and
ten-second measurement. Both used three fresh containers per mode.

Increase `--sessions` until `late_percent` exceeds the chosen service-level objective. Current
server alerting treats less than 15 frames per second as unhealthy, equivalent to at least 25
percent of expected frames missing their 50 millisecond deadline. Prefer a stricter threshold such
as p99 lateness below 50 milliseconds for capacity planning.

Default warmup is 5 seconds per mode. Override it with `--warmup-seconds`. Run at least three fresh
processes per mode and retain every comma-separated output row; do not average percentile columns.
