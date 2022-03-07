package com.beatmaker.core.midi;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MidiInterfaceBase {

    private static final String TAG = "MidiInterfaceBase";

    protected Set<MidiInterfaceListener> listeners = new HashSet<>();

    protected String id;
    protected String alias;

    protected ArrayList<MidiPortBase> inputs;
    protected ArrayList<MidiPortBase> outputs;

    protected boolean connected;

    protected MidiInterfaceBase() {
        id = null;
        alias = null;
        inputs = null;
        outputs = null;
    }

    public void addListener(MidiInterfaceListener midiInterfaceListener) {
        listeners.add(midiInterfaceListener);
    }

    public void removeListener(MidiInterfaceListener midiInterfaceListener) {
        listeners.remove(midiInterfaceListener);
    }

    public void removeAllListeners() {
        listeners.clear();
    }

    public boolean hasInputs() {
        return (getNumInputs() > 0);
    }

    public int getNumInputs() {
        return (null != inputs) ? inputs.size() : 0;
    }

    public List<MidiPortBase> getInputs() {
        return inputs;
    }

    public boolean hasOutputs() {
        return (getNumOutputs() > 0);
    }

    public int getNumOutputs() {
        return (null != outputs) ? outputs.size() : 0;
    }

    public List<MidiPortBase> getOutputs() {
        return outputs;
    }

    public boolean open() {
        return false;
    }

    public void close() {

        connected = false;

        if (null != inputs) {
            inputs.clear();
            inputs = null;
        }

        if (null != outputs) {
            outputs.clear();
            outputs = null;
        }

    }

    public boolean isConnected() {
        return connected;
    }

    public String getId() {
        return id;
    }

    public void setAlias(int counter) {

        String aliasBase = id;

        if (0 == counter) {
            this.alias = aliasBase;
        } else {
            this.alias = aliasBase + " #" + counter;
        }
    }

    public String getAlias() {
        return alias;
    }

    public MidiPortBase getDefaultInput() {
        if (!hasInputs()) {
            return null;
        }

        return inputs.get(0);
    }

    public MidiPortBase getDefaultOutput() {
        if (!hasOutputs()) {
            return null;
        }

        return outputs.get(0);
    }

    public void detectPorts() {
        inputs = null;
        outputs = null;
    }

    public void addPort(MidiPortBase midiPort) {

        int direction = midiPort.getDirection();

        if (direction == MidiPortBase.INPUT) {
            if (null == inputs) {
                inputs = new ArrayList<>();
            }
            inputs.add(midiPort);
        } else if (direction == MidiPortBase.OUTPUT) {
            if (null == outputs) {
                outputs = new ArrayList<>();
            }
            outputs.add(midiPort);
        }
    }

    public int send(MidiPortBase port, MidiData midiData) {
        return port.send(midiData);
    }

    public int send(MidiPortBase port, byte[] buffer, int length, int timeout) {
        return -1; // not implemented
    }

    public int receive(MidiPortBase port, byte[] buffer, int length, int timeout) {
        return -1; // not implemented
    }

}
