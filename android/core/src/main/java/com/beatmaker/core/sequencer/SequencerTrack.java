package com.beatmaker.core.sequencer;

import com.beatmaker.config.Constants;
import com.beatmaker.core.midi.MidiBase;
import com.beatmaker.core.midi.MidiData;
import com.beatmaker.core.midi.MidiNote;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SequencerTrack extends SequencerElement  {

    private static final String TAG = "SequencerTrack";
    private String name;
    private SequencerStep[] steps;
    private Map<Integer, MidiNoteState> playingNotes = new HashMap<>();

    private class MidiNoteState extends MidiNote {

        private int duration;
        private boolean active;

        public MidiNoteState(MidiNote note, int duration) {
            super(note);
            this.duration = duration;
            this.active = true;
        }

        public MidiNoteState(int channel, int pitch, int velocity, int duration) {
            super(channel, pitch, velocity);
            this.duration = duration;
            this.active = true;
        }

        public void set(MidiNote note, int duration) {
            super.set(note);
            this.duration = duration;
            this.active = true;
        }

        public void set(int channel, int note, int velocity, int duration) {
            super.set(channel, note, velocity);
            this.duration = duration;
            this.active = true;
        }

        public void setActive(boolean active) {
            this.active = active;
        }

        public boolean isActive() {
            return active;
        }

        public int getDuration() {
            return duration;
        }

        public void setDuration(int duration) {
            this.duration = duration;
        }

        public void update() {
            if (duration > 0) duration--;
        }

        public boolean isExpired() {
            return (duration <= 0);
        }
    }

    public SequencerTrack() {
        super(null, 0);
        setConfig(new ElementConfig());
        alloc();
    }

    public SequencerTrack(int index, String name) {
        super(null, index);

        this.name = name;
        int pitch = Constants.DEFAULT_NOTE_PITCH;
        if (index < Constants.DEFAULT_TRACK_PITCH.length) {
            pitch = Constants.DEFAULT_TRACK_PITCH[index];
        }

        int channel = 0;

        ElementConfig config = new ElementConfig();
        config.setChannel(channel);
        config.setNote(channel, pitch, Constants.DEFAULT_NOTE_VELOCITY);
        setConfig(config);

        alloc();
    }

    private void alloc() {
        steps = new SequencerStep[Constants.NUM_STEPS];
        for (int i = 0; i< steps.length; i++) {
            steps[i] = new SequencerStep(this, i);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean hasName() {
        return (null != name && name.length() > 0);
    }

    @Override
    public synchronized void clear() {
        if (null == steps) return;
        for (SequencerStep e : steps) {
            e.clear();
        }
    }

    public int getNumElements() {
        if (null == steps) return 0;
        return steps.length;
    }

    private int idx(double pos) {
        return (int) (((long) pos) / Constants.TICKS_PER_STEP);
    }

    public synchronized void notesOn(SequencerElement element) {
        ElementConfig config = element.getConfig();
        if (null == config && element.hasParent()) config = element.getParent().getConfig();

        List<MidiNote> notes = config.getNotes();
        if (null != notes) {
            for (MidiNote note : notes) {

                int pitch = note.getPitch();

                // map next midi note to play to current midi channel
                note.setChannel(config.getChannel());

                MidiNoteState noteState = playingNotes.get(pitch);
                if (null != noteState) {
                    if (noteState.active) {
                        //Logger.d(TAG, "(update) note off:" +  noteState.getPitch());

                        MidiData m = MidiData.noteOff(noteState);
                        MidiBase.instance().enqueueMidiOutput(m);
                    }

                    // refresh midi note
                    noteState.set(note, Constants.DEFAULT_NOTE_DURATION);
                } else {
                    noteState = new MidiNoteState(note, Constants.DEFAULT_NOTE_DURATION);
                    playingNotes.put(pitch, noteState);
                }

                //Logger.d(TAG, "note on:" +  noteState.getPitch());
                MidiData m = MidiData.noteOn(noteState);
                MidiBase.instance().enqueueMidiOutput(m);
            }
        }
    }

    public synchronized void allNotesOff() {

        if (playingNotes.isEmpty()) return;

        for (MidiNoteState noteState : playingNotes.values()) {
            if (noteState.isActive()) {
                //Logger.d(TAG, "note off:" +  noteState.getPitch());
                MidiData m = MidiData.noteOff(noteState);
                MidiBase.instance().enqueueMidiOutput(m);
            }
        }

        playingNotes.clear();
    }

    private final List<Integer> deleteList = new ArrayList<>();

    public synchronized void updateNotes() {

        if (playingNotes.isEmpty()) return;

        deleteList.clear();

        for (MidiNoteState noteState : playingNotes.values()) {
            if (noteState.isActive()) {
                noteState.update();
                if (noteState.isExpired()) {
                    //Logger.d(TAG, "note off:" +  noteState.getPitch());
                    MidiData m = MidiData.noteOff(noteState);
                    MidiBase.instance().enqueueMidiOutput(m);
                    deleteList.add(noteState.getPitch());
                }
            }
        }

        for (int pitch : deleteList) {
            playingNotes.remove(pitch);
        }

    }

    public SequencerStep getElement(int step) {
        if (step < 0 || step >= steps.length) return null;
        return steps[step];
    }

    public SequencerStep getElementAtPos(double pos) {
        return getElement(idx(pos));
    }

    public synchronized void updateTick(double pos) {
        updateNotes();
    }

    public synchronized void update(double pos) {
        SequencerElement element = getElementAtPos(pos);
        if (null == element) return;

        if (element.isActive()) {
            notesOn(element);
        }
    }
}
