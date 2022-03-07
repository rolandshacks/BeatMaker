package com.beatmaker.core.sequencer;

public class SequencerControl {

    private static final String TAG = "SequencerState";

    private final SequencerMetrics globals = SequencerMetrics.instance();
    private final Sequencer sequencer;
    private final Object lock = new Object();

    public static final int MODE_STOPPED = 0;
    public static final int MODE_PLAYING = 1;
    public static final int MODE_PAUSED = 2;

    private volatile int mode;
    private volatile boolean rewindRequested;
    private volatile boolean rewindDone;

    SequencerPosition position = new SequencerPosition();

    private volatile boolean overrun = false;

    public SequencerControl(Sequencer sequencer) {
        this.sequencer = sequencer;
        mode = MODE_STOPPED;
        reset();
    }

    public boolean isValid() {
        return globals.getBpm() > 0 && globals.getTickTime() > 0;
    }

    public boolean isPlaying() {
        return mode == SequencerControl.MODE_PLAYING;
    }

    public void reset() {
        position.reset();
    }

    public void start() {
        position.reset();
        resume();
    }

    public void stop() {
        mode = MODE_STOPPED;
    }

    public void pause() {
        mode = MODE_PAUSED;
    }

    public void resume() {
        mode = MODE_PLAYING;
    }

    public int getMode() {
        return mode;
    }

    public SequencerPosition getPosition() {
        return position;
    }

    public boolean isOverrun() {
        return overrun;
    }
    public boolean isRewindDone() { return rewindDone; }

    public synchronized void rewind() {
        if (!isPlaying()) {
            reset();
        } else {
            rewindRequested = true;
        }
    }

    synchronized void increment() {
        update(1);
    }

    private synchronized void update(long elapsedTicks) {

        if (!isValid()) return;

        if (rewindRequested) {
            rewindRequested = false;
            rewindDone = true;
            overrun = true;
            position.reset();
            return;
        } else {
            rewindDone = false;
        }

        long tick = position.getTick();
        long nextTick = tick + elapsedTicks;
        long numTicks = globals.getNumTicks();

        if (nextTick >= numTicks) {
            overrun = true;
            nextTick = nextTick % numTicks;
        } else {
            overrun = false;
        }

        position.set((double) nextTick);
    }

}
