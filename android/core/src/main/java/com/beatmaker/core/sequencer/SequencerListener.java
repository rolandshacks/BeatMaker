package com.beatmaker.core.sequencer;

import com.beatmaker.core.midi.MidiNote;

import java.util.List;

public interface SequencerListener {
    void onSequencerPositionUpdate(SequencerPosition position, boolean stepChange);
    void onSequencerCaptureFinished(List<MidiNote> capturedNotes);
}
