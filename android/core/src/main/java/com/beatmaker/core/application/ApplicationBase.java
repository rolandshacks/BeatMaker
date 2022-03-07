package com.beatmaker.core.application;

import com.beatmaker.config.Debug;
import com.beatmaker.config.Settings;
import com.beatmaker.core.midi.MidiBase;
import com.beatmaker.core.sequencer.Sequencer;

import java.util.HashSet;
import java.util.Set;

public class ApplicationBase {

    private static ApplicationBase instance_;
    protected Settings settings;

    protected MidiBase midi;
    protected Sequencer sequencer;
    private Set<ApplicationListener> listeners = new HashSet<>();

    public static ApplicationBase instance() {
        return instance_;
    }

    public ApplicationBase() {
        assert(null == instance_);
        instance_ = this;
    }

    public void addListener(ApplicationListener applicationListener) {
        listeners.add(applicationListener);
    }

    public void removeListener(ApplicationListener applicationListener) {
        listeners.remove(applicationListener);
    }

    public void allListeners() {
        listeners.clear();
    }

    public void create() {

        if (null == settings) {
            settings = new Settings();
        }

        if (null == midi) {
            midi = new MidiBase();
        }

        midi.create();

        sequencer = new Sequencer();
        sequencer.create();
    }

    public void startup() {

        midi.start();

        if (Debug.SEQUENCER_AUTOSTART) {
            sequencer.start();
        }
    }

    public void shutdown() {

        for (ApplicationListener listener : listeners) {
            listener.onApplicationShutdown();
        }

        listeners.clear();

        if (null != sequencer) {
            sequencer.destroy();
            sequencer = null;
        }

        if (null != midi) {
            midi.destroy();
            midi = null;
        }
    }

    public void destroy() {
        shutdown();

        if (this == instance_) {
            instance_ = null;
        }
    }

    public void pause() {
        if (null != midi) {
            midi.pause();
        }

        for (ApplicationListener listener : listeners) {
            listener.onApplicationPause();
        }
    }

    public void resume() {
        for (ApplicationListener listener : listeners) {
            listener.onApplicationResume();
        }

        if (null != midi) {
            midi.resume();
        }
    }
}
