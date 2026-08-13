package com.wordonline.server.statistic.domain;

import lombok.Getter;

@Getter
public class UpdateTimeStatistic {

    private long minInterval = Long.MAX_VALUE;
    private long maxInterval = Long.MIN_VALUE;
    private float meanInterval;
    private int frameNum = 0;

    public void addInterval(long interval) {
        minInterval = Math.min(minInterval, interval);
        maxInterval = Math.max(maxInterval, interval);

        float totalInterval = meanInterval * frameNum + interval;
        frameNum++;
        meanInterval = totalInterval / frameNum;
    }
}
