package com.beatmaker.core.midi;

public class MidiIoThread extends Thread {

    private MidiQueue queue;
    private final MidiInterfaceBase midiInterface;

    public MidiIoThread(MidiInterfaceBase midiInterface) {
        this.midiInterface = midiInterface;
    }

    public MidiQueue allocQueue(int size) {
        freeQueue();
        if (null == queue) {
            queue = new MidiQueue(size);
        }

        return queue;
    }

    public void freeQueue() {
        queue = null;
    }

    public MidiQueue getQueue() {
        return queue;
    }
}
