package com.wordonline.server.statistic.domain;

import lombok.Getter;

@Getter
public class UpdateTimeStatistic {

    private int minInterval = Integer.MAX_VALUE;
    private int maxInterval = Integer.MIN_VALUE;
    private float meanInterval;
    private int frameNum = 0;

    public void addInterval(int interval) {
        minInterval = Math.min(minInterval, interval);
        maxInterval = Math.max(maxInterval, interval);

        float totalInterval = meanInterval * frameNum + interval;
        frameNum++;
        meanInterval = totalInterval / frameNum;
    }
}
