package com.beatmaker.io;

import com.beatmaker.core.midi.MidiData;
import com.beatmaker.core.midi.MidiInterfaceBase;
import com.beatmaker.core.midi.MidiInterfaceListener;
import com.beatmaker.core.utils.Logger;

import javax.sound.midi.*;
import java.util.List;

public class JavaMidiInterface extends MidiInterfaceBase {

    private static final String TAG = "MidiInterface";

    private MidiDevice inputDevice;
    private MidiDevice outputDevice;

    public static String getDeviceId(MidiDevice device) {
        if (null == device) return null;

        String name = device.getDeviceInfo().getName();

        if (name.equals("MODX-1")) {
            return "Yamaha Corporation MODX";
        }

        return name;
    }

    public JavaMidiInterface() {
        super();
    }

    public JavaMidiInterface(MidiDevice inputDevice, MidiDevice outputDevice) {
        super();

        if (null != inputDevice) this.id = getDeviceId(inputDevice);
        if (null != outputDevice) this.id = getDeviceId(outputDevice);
        this.inputDevice = inputDevice;
        this.outputDevice = outputDevice;
    }

    public boolean hasInputDevice() {
        return (null != inputDevice);
    }

    public MidiDevice getInputDevice() {
        return inputDevice;
    }

    public void setInputDevice(MidiDevice device) {
        this.inputDevice = device;
    }

    public boolean hasOutputDevice() {
        return (null != outputDevice);
    }

    public MidiDevice getOutputDevice() {
        return outputDevice;
    }

    public void setOutputDevice(MidiDevice device) {
        this.outputDevice = device;
    }

    @Override
    public boolean open() {

        if (null != inputDevice) {
            if (!inputDevice.isOpen()) {
                try {
                    inputDevice.open();
                } catch (MidiUnavailableException e) {;}
            }
        }

        if (null != outputDevice) {
            if (!outputDevice.isOpen()) {
                try {
                    outputDevice.open();
                } catch (MidiUnavailableException e) {;}
            }
        }

        detectPorts();

        this.connected = true;

        return true;
    }

    @Override
    public void close() {

        super.close();

        if (null != inputDevice) {
            List<Transmitter> transmitters = inputDevice.getTransmitters();
            if (null != transmitters) {
                for (Transmitter transmitter : transmitters) {
                    transmitter.close();
                }
            }
            inputDevice = null;
        }

        if (null != outputDevice) {
            List<Receiver> receivers = outputDevice.getReceivers();
            if (null != receivers) {
                for (Receiver receiver : receivers) {
                    receiver.close();
                }
            }
            outputDevice = null;
        }

    }

    @Override
    public void detectPorts() {

        inputs = null;
        outputs = null;

        if (null != inputDevice) {
            try {

                Transmitter transmitter = inputDevice.getTransmitter();
                if (null != transmitter) {
                    addPort(new JavaMidiPort(transmitter));
                }

                Receiver midiHandler = new Receiver() {

                    @Override
                    public void send(MidiMessage message, long timeStamp) {

                        try {
                            onReceivedMessage(message);
                        } catch (Exception e) {
                            Logger.d(TAG, "Exception: " + e.toString());
                        }

                    }

                    @Override
                    public void close() {

                    }
                };
                transmitter.setReceiver(midiHandler);

                //transmitter.setReceiver(this);

            } catch (MidiUnavailableException e) {
                Logger.d(TAG, "failed:"  + e.toString());
            }
        }

        if (null != outputDevice) {
            try {
                Receiver receiver = outputDevice.getReceiver();
                if (null != receiver) {
                    addPort(new JavaMidiPort(receiver));
                }
            } catch (MidiUnavailableException e) {}
        }

    }

    private int onReceivedMessage(MidiMessage midiMessage) {

        if (null == midiMessage) return -1;

        //Log.d(TAG, "receive message");

        byte[] rawData = midiMessage.getMessage();

        int len = rawData.length;

        if (null == rawData || len < 1) return -1;

        long timestamp = System.nanoTime();

        //Log.d(TAG, StringUtils.hexformat(rawData, 0, len));

        int midi0 = rawData[0] & 0xff;
        int midi1 = (len >= 2) ? rawData[1] & 0xff : 0x0;
        int midi2 = (len >= 3) ? rawData[2] & 0xff : 0x0;

        int codeIndexNumber = (midi0 >> 4) & 0xff;

        MidiData midiData = new MidiData(timestamp, 0, codeIndexNumber, midi0, midi1, midi2);
        for (MidiInterfaceListener listener : listeners) {
            listener.onData(midiData);
        }

        return len;
    }

}
