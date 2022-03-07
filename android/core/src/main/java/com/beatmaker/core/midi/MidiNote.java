package com.beatmaker.core.midi;

public class MidiNote {

    private static final String[] NOTE_NAMES = { "A-", "A#", "B-", "C-", "C#", "D-", "D#", "E-", "F-", "F#", "G-", "G#"};

    private int channel;
    private int pitch;
    private int velocity;

    public MidiNote() {
    }

    public MidiNote(MidiNote note) {
        set(note);
    }

    public MidiNote(int pitch, int velocity) {
        set(pitch, velocity);
    }

    public MidiNote(int channel, int pitch, int velocity) {
        set(channel, pitch, velocity);
    }

    public void set(int pitch, int velocity) {
        set(0, pitch, velocity);
    }

    public void set(MidiNote note) {
        this.channel = note.channel;
        this.pitch = note.pitch;
        this.velocity = note.velocity;
    }

    public void set(int channel, int pitch, int velocity) {
        this.channel = channel;
        this.pitch = pitch;
        this.velocity = velocity;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

    public int getChannel() {
        return channel;
    }

    public void setPitch(int pitch) {
        this.pitch = pitch;
    }

    public int getPitch() {
        return pitch;
    }

    public void setVelocity(int velocity) {
        this.velocity = velocity;
    }

    public int getVelocity() {
        return velocity;
    }

    public static String getNoteName(int midiPitch) {
        int ofs = midiPitch - 21;
        if (ofs < 0) return "??";

        int note = ofs % 12;
        int oct = ofs / 12;

        String s = NOTE_NAMES[note] + oct;

        return s;
    }

    public String getNoteName() {
        return getNoteName(pitch);
    }
}
