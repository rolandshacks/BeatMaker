package com.beatmaker.config;

public class Debug {

    public static final boolean MOCKUP_MIDI = false;
    public static final boolean SEQUENCER_GENERATE_TEST_ELEMENTS = false;
    public static final boolean SEQUENCER_AUTOSTART = false;

    public static final boolean SHOW_LATENCIES = true;
    public static final boolean SHOW_SCHEDULING_STATS = true;
    public static final boolean SHOW_SCHEDULING_JITTER_WARNINGS = false;
    public static final long SHOW_SCHEDULING_JITTER_TICK_LIMIT = 20000000; // 20ms
    public static final long SHOW_SCHEDULING_JITTER_STEP_LIMIT = 20000000; // 20ms

}
