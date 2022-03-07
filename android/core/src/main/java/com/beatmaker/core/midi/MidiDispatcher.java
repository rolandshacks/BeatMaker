package com.beatmaker.core.midi;

import static com.beatmaker.core.utils.StringUtils.toHex;

import com.beatmaker.core.utils.Logger;

import java.util.HashSet;
import java.util.Set;

public class MidiDispatcher {

    private static final String TAG = "MidiDispatcher";

    private Set<MidiInputListener> listeners = new HashSet<>();
    private MidiClock midiClock;
    private Thread dispatchThread;

    public MidiDispatcher(MidiClock midiClock) {
        this.midiClock = midiClock;
    }

    public void addInputListener(MidiInputListener midiInputListener) {
        listeners.add(midiInputListener);
    }

    public void removeInputListener(MidiInputListener midiInputListener) {
        listeners.remove(midiInputListener);
    }

    public void removeAllInputListeners() {
        listeners.clear();
    }

    public void dispatch(MidiData midiData) {

        for (MidiInputListener listener : listeners) {
            if (listener.onData(midiData)) {
                return;
            }
        }

        int cable = midiData.getCable();
        int codeIndexNumber = midiData.getCodeIndexNumber();
        int midi0 = midiData.getMidi0();
        int midi1 = midiData.getMidi1();
        int midi2 = midiData.getMidi2();

        if (cable != 0x0) {
            // Yamaha MODX/Montage: cable 1 could be used for the "Mackie Control Protocol"
            Logger.d(TAG, "cable: " + cable);
            return;
        }

        switch (codeIndexNumber) {
            case 0x0: // miscellaneous function codes. reserved for future extensions
            {
                break;
            }
            case 0x1: // cable events. reserved for future expansion
            {
                break;
            }
            case 0x2: // 2-byte system common message (2 bytes)
            {
                Logger.d(TAG, "system common message");
                break;
            }
            case 0x3: // 3-byte system common message (3 bytes)
            {
                Logger.d(TAG, "system common message");
                break;
            }
            case 0x4: // sysex starts or continues (3 bytes)
            {
                Logger.d(TAG, "sysex start or continue");
                break;
            }
            case 0x5: // 1-byte system common message or sysex ends with next byte (1 byte)
            {
                Logger.d(TAG, "sysex end");
                break;
            }
            case 0x6: // sysex ends with next 2 bytes (2 bytes)
            {
                Logger.d(TAG, "sysex end");
                break;
            }
            case 0x7: // sysex ends with next 3 bytes (3 bytes)
            {
                Logger.d(TAG, "sysex end");
                break;
            }
            case 0x8: // note off (3 bytes)
            {
                int midiChannel = midi0 & 0xf;
                int pitch = midi1;
                int velocity = midi2;
                Logger.d(TAG, "note off: " + midiChannel + ", " + pitch);
                for (MidiInputListener listener : listeners) { listener.onNoteOff(midiChannel, pitch); }
                break;
            }
            case 0x9: // note on (3 bytes)
            {
                int midiChannel = midi0 & 0xf;
                int pitch = midi1;
                int velocity = midi2;

                if (velocity > 0) {
                    Logger.d(TAG, "note on: " + midiChannel + ", " + pitch + ", " + velocity);
                    for (MidiInputListener listener : listeners) { listener.onNoteOn(midiChannel, pitch, velocity); }
                } else {
                    Logger.d(TAG, "note off: " + midiChannel + ", " + pitch);
                    for (MidiInputListener listener : listeners) { listener.onNoteOff(midiChannel, pitch); }
                }
                break;
            }
            case 0xa: // poly key press (3 bytes)
            {
                int midiChannel = midi0 & 0xf;
                int key = midi1;
                int pressure = midi2;
                Logger.d(TAG, "poly key press: " + midiChannel + ", " + key + ", " + pressure);
                for (MidiInputListener listener : listeners) {  listener.onPolyKeyPress(midiChannel, key, pressure); }
                break;
            }
            case 0xb: // control change (3 bytes)
            {
                int midiChannel = midi0 & 0xf;
                int controller = midi1;
                int value = midi2;

                if (controller >= 120) {
                    for (MidiInputListener listener : listeners) { listener.onChannelModeMessage(midiChannel, controller, value); }
                    Logger.d(TAG, "channel mode message: " + midiChannel + ", " + controller + " = " + value);
                } else {
                    for (MidiInputListener listener : listeners) { listener.onControlChange(midiChannel, controller, value); }
                    if (controller != 64) { // filter sustain pedal
                        Logger.d(TAG, "control change: " + midiChannel + ", " + controller + " = " + value);
                    }
                }

                break;
            }
            case 0xc: // program change (2 bytes)
            {
                int midiChannel = midi0 & 0xf;
                int program = midi1;
                Logger.d(TAG, "program change: " + midiChannel + ", " + program);
                for (MidiInputListener listener : listeners) { listener.onProgramChange(midiChannel, program); }
                break;
            }
            case 0xd: // channel pressure (2 bytes)
            {
                int midiChannel = midi0 & 0xf;
                int pressure = midi1;
                Logger.d(TAG, "channel pressure change: " + midiChannel + ", " + pressure);
                for (MidiInputListener listener : listeners) { listener.onChannelPressure(midiChannel, pressure); }
                break;
            }
            case 0xe: // pitch bend change (3 bytes)
            {
                int midiChannel = midi0 & 0xf;
                int value = (midi2 * 128) + midi1 - 8192;
                Logger.d(TAG, "pitch bend change: " + midiChannel + ", " + value);
                for (MidiInputListener listener : listeners) { listener.onPitchBendChange(midiChannel, value); }
                break;
            }
            case 0xf: // single byte (1 byte)
            {
                int msg = midi0;

                switch (msg) {
                    case 0xf6:
                        Logger.d(TAG, "tune request");
                        for (MidiInputListener listener : listeners) { listener.onTuneRequest(); }
                        break;
                    case 0xf8:
                        //Logger.d(TAG, "realtime clock");
                        if (null != midiClock) {
                            midiClock.update();
                        }
                        for (MidiInputListener listener : listeners) { listener.onClock(); }
                        break;
                    case 0xfa:
                        Logger.d(TAG, "start");
                        for (MidiInputListener listener : listeners) { listener.onStart(); }
                        break;
                    case 0xfb:
                        Logger.d(TAG, "continue");
                        for (MidiInputListener listener : listeners) { listener.onContinue(); }
                        break;
                    case 0xfc:
                        Logger.d(TAG, "stop");
                        for (MidiInputListener listener : listeners) { listener.onStop(); }
                        break;
                    case 0xfe:
                        //Logger.d(TAG, "active sensing");
                        for (MidiInputListener listener : listeners) { listener.onActiveSensing(); }
                        break;
                    case 0xff:
                        Logger.d(TAG, "reset");
                        for (MidiInputListener listener : listeners) { listener.onReset(); }
                        break;
                    default:
                        Logger.d(TAG, "single byte: " + toHex(midi0));
                        break;
                }

                break;
            }
            default: {
                break;
            }
        }
    }
}
