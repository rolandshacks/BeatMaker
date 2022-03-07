package com.beatmaker.config;

public class Constants {

    /*
            MEASURE (1)
                QUARTERS (4)
                    STEPS (4)
                        TICK (8)
    */

    public static final int DEFAULT_BPM = 110;

    public static final int NUM_TRACKS = 8;
    public static final int NUM_MEASURES = 1;

    public static final int NUM_QUARTERS_PER_MEASURE = 4;

    public static final int STEPS_PER_QUARTER_NOTE = 4;
    public static final int NUM_STEPS = NUM_MEASURES * NUM_QUARTERS_PER_MEASURE * STEPS_PER_QUARTER_NOTE;

    public static final int TICKS_PER_STEP = 64; // DEFAULT: 8
    public static final int TICKS_PER_QUARTER_NOTE = STEPS_PER_QUARTER_NOTE * TICKS_PER_STEP;

    public static final int DEFAULT_MIDI_CHANNEL = 0x0;
    public static final int DEFAULT_NOTE_PITCH = 36; // C
    public static final int[] DEFAULT_TRACK_PITCH = { 36, 37, 38, 39, 40, 42, 46, 49 };
    public static final int DEFAULT_NOTE_VELOCITY = 72;

    public static final int DEFAULT_NOTE_DURATION = Constants.TICKS_PER_STEP; // TICKS

    public static final boolean MIDI_OUTPUT_ASYNC_MODE = false;
    public static final int MIDI_OUTPUT_MAX_QUEUE_SIZE = 512;

    public static final int MIDI_DEVICE_IO_TIMEOUT = 250;

}
