package com.beatmaker.core.sequencer;

public class SequencerElement {
    private int index;

    private SequencerElement parent;

    private volatile ElementConfig config;
    private volatile boolean active;

    public SequencerElement(SequencerElement parent, int index) {
        this.parent = parent;
        this.index = index;
        this.config = null;
    }

    public void setParent(SequencerElement parent) {
        this.parent = parent;
    }

    public SequencerElement getParent() {
        return parent;
    }

    public boolean hasParent() {
        return (null != parent);
    }

    public int getIndex() {
        return index;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setActive() {
        setActive(true);
    }

    public void setInactive() {
        setActive(false);
    }

    public boolean isActive() {
        return active;
    }

    public void clear() {
        active = false;
        config = null;
    }

    public boolean hasConfig() {
        return (null != config);
    }

    public ElementConfig getConfig() {
        return config;
    }

    public void clearConfig() {
        config = null;
    }

    public void setConfig(ElementConfig config) {
        this.config = config;
    }


    public boolean isTrack() {
        return (this instanceof SequencerTrack);
    }

    public boolean isStep() {
        return (this instanceof SequencerStep);
    }

}
