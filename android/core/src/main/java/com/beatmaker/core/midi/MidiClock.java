package com.beatmaker.core.midi;

import com.beatmaker.config.Constants;
import com.beatmaker.core.sequencer.SequencerMetrics;

public class MidiClock {

    private static final String TAG = "MidiClock";

    private static final long UPDATE_BUFFER_TIME = 1000000L; // seconds
    private static final long MIDI_REALTIME_CLOCK_UPDATES_PER_MEASURE = 96;

    private long midiClockStart = 0;
    private long midiClockCount = 0;
    private long midiClockLastUpdate = 0;
    private int bpm = 0;

    public void reset() {
        midiClockStart = 0;
        midiClockCount = 0;
    }

    public int getBpm() {
        return bpm;
    }

    public void update() {
        // midi sends 96 updates per measure

        double quartersPerMeasure = Constants.NUM_QUARTERS_PER_MEASURE;
        double midiRtClockUpdatesPerMeasure = (double) MIDI_REALTIME_CLOCK_UPDATES_PER_MEASURE;

        long now = System.nanoTime()/1000;

        long updateDelay = now - midiClockLastUpdate;
        midiClockLastUpdate = now;
        if (updateDelay > 1000000) {
            // last was too long ago, reset
            midiClockStart = now;
            midiClockCount = 0;
            return;
        }

        midiClockCount++;

        long elapsed = now - midiClockStart;
        if (elapsed < UPDATE_BUFFER_TIME) {
            return;
        }

        // calculate avg. bpm
        double elapsedSeconds = (double) elapsed / 1000000.0;
        double updatesPerSecond = (double) midiClockCount / elapsedSeconds;
        double measuredBpm = (updatesPerSecond * 60.0 * quartersPerMeasure / midiRtClockUpdatesPerMeasure);

        //Logger.d(TAG, "measured bpm: " + measuredBpm);

        // reset counters
        midiClockStart = now;
        midiClockCount = 0;

        // update bpm value
        int newBpm = (int) (measuredBpm + 0.5);
        if (newBpm != bpm) {
            bpm = newBpm;
            onBpmChanged(bpm);
        }

    }

    protected void onBpmChanged(int bpm) {
        //Logger.d(TAG, "bpm changed: " + bpm);
        SequencerMetrics.instance().setBpm(bpm);
    }

}
