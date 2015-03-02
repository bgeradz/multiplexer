package com.bgeradz.multiplexer;

public class SpeedMeasurer {
    private int[] times;
    private int[] counts;

    private int timesSum;
    private int countsSum;

    private int head;

    private long lastReportTime;

    public SpeedMeasurer(int capacity) {
        times = new int[capacity];
        counts = new int[capacity];
        lastReportTime = System.currentTimeMillis();
    }

    public void report(int count) {
        long now = System.currentTimeMillis();
        int elapsed = (int) (now - lastReportTime);
        lastReportTime = now;

        timesSum -= times[head];
        countsSum -= counts[head];
        times[head] = elapsed;
        counts[head] = count;

        head++;
        if (head >= times.length) {
            head = 0;
        }
    }

    // counts per second
    public double getAverageSpeed() {
        if (timesSum == 0) {
            return 0.0;
        } else {
            return (double) countsSum / (double) timesSum * 1000.0;
        }
    }
}
