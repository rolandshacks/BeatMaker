package com.beatmaker.io;

import com.beatmaker.config.Debug;
import com.beatmaker.core.midi.*;
import com.beatmaker.core.utils.Logger;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import java.util.HashMap;
import java.util.Iterator;

public class JavaMidi extends MidiBase implements MidiApiProvider {

    private static final String TAG = "Midi";

    public JavaMidi() {
        super();
    }

    @Override
    protected HashMap<String, MidiInterfaceBase> queryMidiInterfaces() {
        HashMap<String, MidiInterfaceBase> interfaces = new HashMap<>();

        MidiDevice.Info[] deviceInfos = MidiSystem.getMidiDeviceInfo();
        if (null == deviceInfos || deviceInfos.length < 1) {
            Logger.d(TAG, "no MIDI devices found");
        }

        for (MidiDevice.Info deviceInfo : deviceInfos) {

            MidiDevice device = null;

            try {
                device = MidiSystem.getMidiDevice(deviceInfo);
            } catch (MidiUnavailableException e) {
                continue;
            }

            if (null != device) {

                int maxReceivers = device.getMaxReceivers();
                int maxTransmitters = device.getMaxTransmitters();
                if (maxReceivers == -1 && maxTransmitters == -1) continue;

                boolean isInput =  (maxReceivers >= 0);
                boolean isOutput = (maxTransmitters >= 0);

                String deviceId = JavaMidiInterface.getDeviceId(device);

                MidiInterfaceBase midiInterface = interfaces.get(deviceId);

                if (null == midiInterface) {
                    midiInterface = new JavaMidiInterface(isInput ? device : null, isOutput ? device : null);
                    interfaces.put(midiInterface.getId(), midiInterface);
                } else {
                    JavaMidiInterface intf = (JavaMidiInterface) midiInterface;

                    if (isInput && null == intf.getInputDevice()) {
                        intf.setInputDevice(device);
                    }
                    if (isOutput && null == intf.getOutputDevice()) {
                        intf.setOutputDevice(device);
                    }
                }
            }
        }

        return interfaces;
    }

    @Override
    protected void connect(MidiInterfaceBase midiInterface) {
        Logger.d(TAG, "connect midi interface: " + midiInterface.getAlias());

        if (midiInterface.isConnected()) {
            return;
        }

        if (null == midiInputQueue) {
            midiInputQueue = new MidiQueue(MidiQueue.DEFAULT_SIZE);
        } else {
            midiInputQueue.clear();
        }

        midiInterface.addListener(new MidiInterfaceListener() {
            @Override
            public void onData(MidiData midiData) {
                enqueueMidiInput(midiData);
            }
        });

        onInterfaceReady(midiInterface, false);
    }

    @Override
    protected int send(MidiInterfaceBase midiInterface, MidiPortBase midiPort, MidiData midiData) {
        if (null == midiInterface || null == midiPort || null == midiData) {
            return -1;
        }

        return midiPort.send(midiData);
    }

    @Override
    protected int send(MidiInterfaceBase midiInterface, MidiPortBase midiPort, Iterator<MidiData> it) {
        if (null == midiInterface || null == midiPort || null == it) {
            return -1;
        }

        int maxPacketSize = midiPort.getMaxPacketSize();
        byte[] packetBuffer = new byte[maxPacketSize];

        int transferCount = 0;

        long maxLatency = 0;
        long earliestTime = 0;

        while (it.hasNext()) {
            MidiData m = it.next();

            long timestamp = m.getTimestamp();
            if (earliestTime == 0 || (timestamp < earliestTime && timestamp != 0)) {
                earliestTime = timestamp;
            }

            int res = midiInterface.send(midiPort, m);
            if (res <= 0) {
                Logger.e(TAG, "output I/O error");
                return res;
            }

            transferCount++;

            // update latency measurement
            long now = getTimeMicros();
            long latency = (0 != earliestTime) ? now - earliestTime : 0;
            if (latency > maxLatency) maxLatency = latency;
            earliestTime = 0;
        }

        if (Debug.SHOW_LATENCIES) {
            Logger.d(TAG, "max. output latency: " + maxLatency);
        }

        return transferCount;
    }

    private void enqueueMidiInput(MidiData midiData) {
        midiInputQueue.add(midiData);
    }


}
