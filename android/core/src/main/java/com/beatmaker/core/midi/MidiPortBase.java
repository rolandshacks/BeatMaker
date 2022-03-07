package com.beatmaker.core.midi;

public class MidiPortBase {

    public static final int INPUT = 0;
    public static final int OUTPUT = 1;

    protected int direction;

    protected MidiPortBase() {
    }

    public int getMaxPacketSize() {
        return 512;
    }

    public int getDirection() {
        return direction;
    }

    public int send(MidiData midiData) {
        return -1;
    }

}
