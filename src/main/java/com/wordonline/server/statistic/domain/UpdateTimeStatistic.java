package com.wordonline.server.statistic.domain;

import lombok.Getter;

@Getter
public class UpdateTimeStatistic {

    private int minInterval;
    private int maxInterval;
    private float meanInterval;
    private int frameNum;

    public void addInterval(int interval) {
        minInterval = Math.min(minInterval, interval);
        maxInterval = Math.max(maxInterval, interval);

        float totalInterval = meanInterval * frameNum + interval;
        frameNum++;
        meanInterval = totalInterval / frameNum;
    }
}
