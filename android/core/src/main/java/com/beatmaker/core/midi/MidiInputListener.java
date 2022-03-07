package com.beatmaker.core.midi;

public class MidiInputListener {

    public void onNoteOff(int midiChannel, int pitch) {
    }

    public void onNoteOn(int midiChannel, int pitch, int velocity) {
    }

    public void onReset() {
    }

    public void onPolyKeyPress(int midiChannel, int key, int pressure) {
    }

    public void onControlChange(int midiChannel, int controller, int value) {
    }

    public void onProgramChange(int midiChannel, int program) {
    }

    public void onChannelPressure(int midiChannel, int pressure) {
    }

    public void onPitchBendChange(int midiChannel, int value) {
    }

    public void onTuneRequest() {
    }

    public void onClock() {
    }

    public void onStart() {
    }

    public void onContinue() {
    }

    public void onStop() {
    }

    public void onActiveSensing() {
    }

    public void onChannelModeMessage(int midiChannel, int message, int value) {
    }

    public boolean onData(MidiData midiData) {
        // return false to let the dispatcher continue
        return false;
    }
}
