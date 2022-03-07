package com.beatmaker.config;

public class Settings {

    public class Priorities {
        public int sequencerThreadPriority = 0;
        public int midiInputThreadPriority = 0;
        public int midiInputDispatchThreadPriority = 0;
        public int midiOutputThreadPriority = 0;
        public int backgroundThreadPriority = 0;
    }

    private String midiInterface = "Yamaha Corporation MODX";
    private Priorities priorities = new Priorities();

    private static Settings instance_;

    public static Settings instance() {
        return instance_;
    }

    public Settings() {
        if (null == instance_) instance_ = this;
    }

    public void setMidiInterface(String midiInterface) {
        this.midiInterface = midiInterface;
    }

    public String getMidiInterface() {
        return midiInterface;
    }

    public Priorities getPriorities() {
        return priorities;
    }

}
