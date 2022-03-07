package com.beatmaker.core.sequencer;

public class SequencerStep extends SequencerElement {

    public SequencerStep(SequencerTrack track, int stepIndex) {
        super(track, stepIndex);
    }

    public SequencerTrack getTrack() {
        SequencerElement parent = getParent();
        if (null == parent) return null;
        return (SequencerTrack) parent;
    }

    public int getTrackIndex() {
        SequencerTrack track = getTrack();
        if (null == track) return 0;
        return track.getIndex();
    }

}
