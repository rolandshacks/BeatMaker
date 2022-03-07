package com.beatmaker.io;

import com.beatmaker.core.midi.MidiData;
import com.beatmaker.core.midi.MidiPortBase;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Transmitter;

public class JavaMidiPort extends MidiPortBase {

    private Receiver receiver;
    private Transmitter transmitter;

    public JavaMidiPort() {
        super();
    }

    public JavaMidiPort(Receiver receiver) {
        super();
        this.receiver = receiver;
        direction = OUTPUT;
    }

    public JavaMidiPort(Transmitter transmitter) {
        super();
        this.transmitter = transmitter;
        direction = INPUT;
    }

    @Override
    public int send(MidiData midiData) {
        if (null == receiver || JavaMidiPort.OUTPUT != direction) {
            return -1;
        }

        int command = (midiData.getMidi0() & 0xF0);
        int channel = (midiData.getMidi0() & 0x0F);

        ShortMessage msg = null;
        try {
            msg = new ShortMessage(command, channel, midiData.getMidi1(), midiData.getMidi2());
        } catch (InvalidMidiDataException e) {
            return -1;
        }

        receiver.send(msg, -1);

        return 4;
    }
}
