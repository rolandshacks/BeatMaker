package com.beatmaker.core.sequencer;

import com.beatmaker.config.Constants;

public class SequencerMetrics {

    private final int numTracks = Constants.NUM_TRACKS;
    private final int numSteps = Constants.NUM_STEPS;
    private final int numMeasures = Constants.NUM_MEASURES;
    private final int numQuarters = Constants.NUM_MEASURES * Constants.NUM_QUARTERS_PER_MEASURE;
    private final int numTicks = numQuarters * Constants.TICKS_PER_QUARTER_NOTE;

    private int bpm = 0;
    private long quarterTime;
    private long tickTime;
    private long tickTimeNano;
    private double tickTimeSec;
    private long stepTime;
    private long songTime;

    private static SequencerMetrics instance_;
    public static SequencerMetrics instance() {
        if (null == instance_) {
            instance_ = new SequencerMetrics();
        }
        return instance_;
    }

    public SequencerMetrics() {
        setBpm(Constants.DEFAULT_BPM);
    }

    private void reset() {
        this.quarterTime = 0;
        this.tickTime = 0;
        this.stepTime = 0;
        this.songTime = 0;
        this.tickTimeNano = 0;
        this.tickTimeSec = 0.0;
    }

    private synchronized void update() {
        if (bpm <= 0.0) {
            reset();
            return;
        }

        quarterTime = (60000000 / bpm);
        tickTime = quarterTime / Constants.TICKS_PER_QUARTER_NOTE;
        stepTime = (quarterTime * Constants.TICKS_PER_STEP) / Constants.TICKS_PER_QUARTER_NOTE;
        songTime = stepTime * numSteps;

        tickTimeNano = 60000000000L / ((long) bpm * Constants.TICKS_PER_QUARTER_NOTE);
        double quarterTimeSec = 60.0 / bpm;
        tickTimeSec = quarterTimeSec * (double) Constants.TICKS_PER_QUARTER_NOTE;
    }

    public int getBpm() {
        return bpm;
    }

    public synchronized void setBpm(int bpm) {
        this.bpm = bpm;
        update();
    }

    public long getTickTime() {
        return tickTime;
    }

    public long getTickTimeNano() {
        return tickTimeNano;
    }

    public double getTickTimeSec() {
        return tickTimeSec;
    }

    /*
    public long getQuarterTime() {
        return quarterTime;
    }

    public long getSongTime() {
        return songTime;
    }
    */

    public static double getBpmFromMicros(long micros) {
        if (micros < 1) return 0.0;
        return (double) (60000000L / micros);
    }

    public int getNumTracks() {
        return numTracks;
    }

    public int getNumTicks() {
        return numTicks;
    }

    public int getNumSteps() {
        return numSteps;
    }

    public int getNumQuarters() {
        return numQuarters;
    }

    public int getNumMeasures() {
        return numMeasures;
    }

}
