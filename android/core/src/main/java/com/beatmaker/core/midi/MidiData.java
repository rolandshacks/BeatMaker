package com.beatmaker.core.midi;

public class MidiData {
    private long timestamp;
    private int cable;
    private int codeIndexNumber;
    private int midi0;
    private int midi1;
    private int midi2;

    public MidiData() {
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public MidiData(long timestamp, int cable, int codeIndexNumber, int midi0, int midi1, int midi2) {
        this.timestamp = timestamp;
        this.cable = cable;
        this.codeIndexNumber = codeIndexNumber;
        this.midi0 = midi0;
        this.midi1 = midi1;
        this.midi2 = midi2;
    }

    public void set(MidiData m) {
        copy(m);
    }

    private void copy(MidiData m) {
        this.timestamp = m.timestamp;
        this.cable = m.cable;
        this.codeIndexNumber = m.codeIndexNumber;
        this.midi0 = m.midi0;
        this.midi1 = m.midi1;
        this.midi2 = m.midi2;
    }

    public int serialize(byte[] buffer, int ofs, int length) {

        if (length - ofs < 4) {
            return 0;
        }

        int i = ofs;

        buffer[i++] = (byte) (((cable & 0x0f) << 4) | ((codeIndexNumber & 0x0f)));
        buffer[i++] = (byte) midi0;
        buffer[i++] = (byte) midi1;
        buffer[i++] = (byte) midi2;

        return (i-ofs);
    }

    public int deserialize(byte[] buffer, int ofs, int length) {

        if (length - ofs < 4) {
            return 0;
        }

        int i = ofs;

        cable = (buffer[i]&0xf0)>>4;
        codeIndexNumber =  buffer[0]&0x0f; i++;
        midi0 = 0xff & buffer[i]; i++;
        midi1 = 0xff & buffer[i]; i++;
        midi2 = 0xff & buffer[i]; i++;

        return (i-ofs);
    }

    public int getCable() {
        return cable;
    }

    public int getCodeIndexNumber() {
        return codeIndexNumber;
    }

    public int getMidi0() {
        return midi0;
    }

    public int getMidi1() {
        return midi1;
    }

    public int getMidi2() {
        return midi2;
    }

    public static MidiData noteOn(int channel, int pitch, int velocity) {
        return new MidiData(0, 0, 0x9, 0x90 | (channel&0xf), pitch, velocity);
    }

    public static MidiData noteOn(MidiNote note) {
        return noteOn(note.getChannel(), note.getPitch(), note.getVelocity());
    }

    public static MidiData noteOff(int channel, int pitch) {
        return new MidiData(0, 0, 0x8, 0x80 | (channel&0xf), pitch, 0);
    }

    public static MidiData noteOff(MidiNote note) {
        return noteOff(note.getChannel(), note.getPitch());
    }
}
