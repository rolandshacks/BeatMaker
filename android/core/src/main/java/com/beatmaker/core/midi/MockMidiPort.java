package com.beatmaker.core.midi;

public class MockMidiPort extends MidiPortBase {
    public MockMidiPort(int direction) {
        super();
        this.direction = direction;
    }

    @Override
    public int getMaxPacketSize() {
        return 512;
    }

}
