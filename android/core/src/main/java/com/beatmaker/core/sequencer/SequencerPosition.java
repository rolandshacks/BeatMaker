package com.beatmaker.core.sequencer;

import com.beatmaker.config.Constants;

public class SequencerPosition {

    private volatile double pos = 0.0;

    public SequencerPosition() {
    }

    public SequencerPosition(double position) {
        this.pos = position;
    }

    public void reset() {
        set(0.0);
    }

    public void set(double pos) {
        this.pos = pos;
    }

    public double get() {
        return pos;
    }

    public long getTick() { return (long) pos; }

    public long getStep() {
        return getTick() / Constants.TICKS_PER_STEP;
    }

    public long getQuarter() {
        return getTick() / Constants.TICKS_PER_QUARTER_NOTE;
    }

    public long getTickInQuarter() {
        return getTick() % Constants.TICKS_PER_QUARTER_NOTE;
    }

    public long getQuarterInMeasure() {
        return getQuarter()%Constants.NUM_QUARTERS_PER_MEASURE;
    }

    public long getStepInQuarter() {
        return (getTick() % Constants.TICKS_PER_QUARTER_NOTE) / Constants.TICKS_PER_STEP;
    }

    public long getTickInStep() {
        return getTick() % Constants.TICKS_PER_STEP;
    }

    public static long ticksToTime(long ticks) {
        return ticks * SequencerMetrics.instance().getTickTime();
    }

    public static long timeToTicks(long time) {
        long tickTime = SequencerMetrics.instance().getTickTime();
        return tickTime > 0 ? time / tickTime : 0;
    }

}
