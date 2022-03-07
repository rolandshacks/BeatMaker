package com.beatmaker.core.midi;

public class MockMidiInterface extends MidiInterfaceBase {

    public MockMidiInterface(String id, String alias) {
        super();
        this.id = id;
        this.alias = alias;
    }

    @Override
    public int receive(MidiPortBase port, byte[] buffer, int length, int timeout) {
        if (MidiPortBase.INPUT != port.getDirection()) {
            return -1;
        }

        try {
            Thread.sleep(25);
        } catch (InterruptedException ignored) {}

        // always "receive" 4 bytes
        buffer[0] = (byte) (0xff & 0x0f);
        buffer[1] = (byte) (0xff & 0xf8);
        buffer[2] = (byte) (0xff & 0x00);
        buffer[3] = (byte) (0xff & 0x00);

        return 4;
    }

    @Override
    public int send(MidiPortBase port, byte[] buffer, int length, int timeout) {
        if (MidiPortBase.OUTPUT != port.getDirection()) {
            return -1;
        }

        return length;
    }

    @Override
    public int send(MidiPortBase port, MidiData midiData) {
        if (MidiPortBase.OUTPUT != port.getDirection()) {
            return -1;
        }

        return 4;
    }
}
