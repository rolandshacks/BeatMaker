package com.beatmaker.core.sequencer;

import com.beatmaker.config.Constants;
import com.beatmaker.config.Debug;
import com.beatmaker.config.Settings;
import com.beatmaker.core.midi.MidiBase;
import com.beatmaker.core.midi.MidiInputListener;
import com.beatmaker.core.midi.MidiNote;
import com.beatmaker.core.utils.Logger;
import com.beatmaker.core.utils.SystemUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.LockSupport;

public class Sequencer {

    private static final String TAG = "Sequencer";

    private static Sequencer instance_;

    private final SequencerMetrics globals = SequencerMetrics.instance();
    private SequencerControl state;
    private final Object lock = new Object();
    private Thread worker;
    private ArrayList<SequencerTrack> tracks;
    private Set<SequencerListener> listeners = new HashSet<>();
    private MidiBase midi;
    private boolean captureActive;
    private Map<Integer, MidiNote> captureData;
    private int captureChannel;

    private final Stats stats = new Stats();

    private class Stats {

        private static final long THRESHOLD = 10;
        private static final long UPDATE_INTERVAL_MS = 5000;

        private long lastUpdateTime = 0;

        private long tickJitterSum = 0;
        private long tickJitterCount = 0;

        private long stepJitterSum = 0;
        private long stepJitterCount = 0;

        private long avgTickJitter = 0;
        private long avgStepJitter = 0;

        private void reset() {
            tickJitterSum = 0;
            tickJitterCount = 0;
            stepJitterSum = 0;
            stepJitterCount = 0;
            avgTickJitter = 0;
            avgStepJitter = 0;
        }

        private void update() {
            long now = System.nanoTime();
            if (0 == lastUpdateTime) {
                lastUpdateTime = now;
                return;
            }

            long elapsed = now - lastUpdateTime;
            if (elapsed >= UPDATE_INTERVAL_MS * 1000000) {
                Logger.d(TAG, "avg. tick jitter: " + (avgTickJitter/1000) + "µs, avg. step jitter: " + (avgStepJitter/1000) + "µs");
                lastUpdateTime= now;
            }
        }

        public void addTickJitter(long t) {
            tickJitterSum += Math.abs(t);
            if (tickJitterCount < THRESHOLD) {
                tickJitterCount++;
            } else {
                tickJitterSum -= avgTickJitter;
            }
            avgTickJitter = tickJitterSum/tickJitterCount;
            update();
        }

        public void addStepJitter(long t) {
            stepJitterSum += Math.abs(t);
            if (stepJitterCount < THRESHOLD) {
                stepJitterCount++;
            } else {
                stepJitterSum -= avgStepJitter;
            }
            avgStepJitter = stepJitterSum/stepJitterCount;
            update();
        }

    }

    public static Sequencer instance() {
        return instance_;
    }

    public Sequencer() {
        assert(null == instance_); // ensure singleton
        instance_ = this;
    }

    public void addListener(SequencerListener sequencerListener) {
        listeners.add(sequencerListener);
    }

    public void removeListener(SequencerListener sequencerListener) {
        listeners.remove(sequencerListener);
    }

    public void removeAllListeners() {
        listeners.clear();
    }

    public void create() {
        state = new SequencerControl(this);

        this.midi = MidiBase.instance();

        if (null != midi) {
            midi.addInputListener(new MidiInputListener() {
                @Override
                public void onNoteOff(int channel, int pitch) {
                    if (isCaptureActive()) {
                        captureInput(new MidiNote(channel, pitch, 0));
                    }
                }

                @Override
                public void onNoteOn(int channel, int pitch, int velocity) {
                    captureInput(new MidiNote(channel, pitch, velocity));
                }
            });
        }

        alloc();

        worker = new Thread() {
            @Override
            public void run() {
                sequencerLoop();
            }
        };
        worker.setName("sequencer");
        worker.start();
    }

    public void destroy() {
        shutdown();

        if (this == instance_) {
            instance_ = null;
        }
    }

    public void shutdown() {

        stop();

        synchronized (lock) {
            lock.notify();
        }

        if (null != worker) {
            worker.interrupt();
            try {
                worker.join(500);
            } catch (InterruptedException ignored) {}
            worker = null;
        }

        free();
    }

    public void start() {
        state.start();
        notifyStateChange();
    }

    public void stop() {
        state.stop();
        notifyStateChange();
    }

    public void pause() {
        state.pause();
        notifyStateChange();
    }

    public void resume() {
        state.resume();
        notifyStateChange();
    }

    public void rewind() {
        state.rewind();
        if (!state.isPlaying()) {
            for (SequencerListener listener : listeners) {
                listener.onSequencerPositionUpdate(state.getPosition(), true);
            }
        }
    }

    public int getMode() {
        return state.getMode();
    }

    public SequencerTrack addTrack(SequencerTrack track) {
        if (null == tracks) {
            tracks = new ArrayList<>();
        }
        tracks.add(track);
        return track;
    }

    private void alloc() {
        free();

        int numTracks = globals.getNumTracks();
        int numSteps = globals.getNumSteps();

        for (int i = 0; i < numTracks; i++) {
            SequencerTrack track = addTrack(new SequencerTrack(i, "Track" + i));
            for (int j = 0; j < numSteps; j++) {
                long tick = (long) j * Constants.TICKS_PER_STEP;
                double pos = (double) tick;

                if (Debug.SEQUENCER_GENERATE_TEST_ELEMENTS) {
                    SequencerStep trackElement = track.getElementAtPos(pos);

                    if (0 == i && j < numSteps/2) {
                        trackElement = track.getElementAtPos(pos);
                        ElementConfig config = new ElementConfig();

                        config.setNotes(new MidiNote[]{
                                new MidiNote(0, 48, 72),
                                new MidiNote(0, 52, 72),
                                new MidiNote(0, 55, 72)
                        });

                        trackElement.setConfig(config);

                    }

                    if ((j % 2) == 0) {
                        trackElement.setActive();
                    }
                }

            }

        }

    }

    private void free() {
        tracks = null;
    }

    public boolean isEmpty() {
        return (null != tracks);
    }

    public boolean isCaptureActive() {
        return captureActive;
    }

    public synchronized void startCapture() {
        if (captureActive) {
            stopCapture();
        }

        captureData = new HashMap<>();
        captureChannel = 0;
        captureActive = true;
    }

    private synchronized void captureInput(MidiNote note) {
        if (!captureActive) {
            return;
        }

        if (captureData.isEmpty()) {
            captureChannel = note.getChannel();
        }

        if (note.getChannel() != captureChannel) {
            return;
        }

        if (note.getVelocity() > 0) {
            captureData.put(note.getPitch(), note);
        } else {
            //captureData.remove(pitch);
            stopCapture();
        }

    }

    public synchronized void stopCapture() {
        captureActive = false;

        List<MidiNote> capturedNotes = new ArrayList<>();

        if (null != captureData) {
            for (MidiNote note : captureData.values()) {
                capturedNotes.add(note);
            }
            captureData = null;
        }

        captureChannel = 0;

        if (!capturedNotes.isEmpty()) {
            captureFinished(capturedNotes);
        }

    }

    private void captureFinished(List<MidiNote> capturedNotes) {

        Logger.d(TAG, "Capture finished: ");
        for (MidiNote note : capturedNotes) {
            Logger.d(TAG, "note : " + note.getPitch());
        }

        for (SequencerListener listener : listeners) {
            listener.onSequencerCaptureFinished(capturedNotes);
        }
    }

    public SequencerTrack getTrack(int track) {
        if (null == tracks || track < 0 || track >= tracks.size()) {
            return null;
        }

        return tracks.get(track);
    }

    private void notifyStateChange() {
        synchronized (lock) {
            lock.notify();
        }
    }

    private boolean waitStateChange(long timeoutNanos) {
        long tmWait = timeoutNanos / 1000000;
        if (tmWait < 1) {
            return !Thread.interrupted();
        }
        synchronized (lock) {
            try {
                lock.wait(tmWait);
            } catch (InterruptedException e) {
                return false;
            }
        }
        return true;
    }

    private boolean waitNanos(long waitTime, Object sync) {

        long correctionTime = 0;

        long t = System.nanoTime();
        long tEnd = t + waitTime - correctionTime;

        if (t >= tEnd) {
            return !Thread.interrupted();
        }

        while (t < tEnd) {
            long remainingWait = tEnd - t;
            if (null != sync) {
                synchronized (lock) {
                    try {
                        long tmWait = remainingWait / 1000000;
                        if (tmWait > 0)
                        {
                            lock.wait(tmWait);
                        }
                    } catch (InterruptedException e) {
                        return false;
                    }
                }
            } else {
                LockSupport.parkNanos(remainingWait);
                if (Thread.interrupted()) {
                    return false;
                }
            }
            t = System.nanoTime();
        }

        return true;

    }

    public void allTracksOff() {
        if (null != midi) {
            for (SequencerTrack track : tracks) {
                track.allNotesOff();
            }
            midi.flushMidiOutput();
        }
    }

    public SequencerControl getState() {
        return state;
    }

    public boolean hasData(int track, int step) {
        if (null == tracks) {
            return false;
        }

        SequencerTrack t = tracks.get(track);
        if (null == t) {
            return false;
        }

        SequencerStep trackElement = t.getElement(step);
        if (null == trackElement) return false;

        return trackElement.isActive();
    }

    long getTimeNanos() {
        return System.nanoTime();
    }

    private void sequencerLoop() {

        SystemUtils.setPriority(Settings.instance().getPriorities().sequencerThreadPriority);

        stats.reset();

        long currentStep = -1;
        long lastStepTime = 0;
        long lastTickTime = 0;
        long nextTickTime = 0;

        allTracksOff();

        while (!Thread.interrupted()) {

            if (state.isPlaying()) {

                long tickTimeNano = globals.getTickTimeNano();

                long tmNow = getTimeNanos();
                if (tmNow < nextTickTime) {
                    long waitTime = nextTickTime - tmNow;
                    if (!waitNanos(waitTime, null)) {
                        break;
                    }
                } else {
                    if (0 == nextTickTime ) {
                        nextTickTime = tmNow;
                    }
                }

                nextTickTime += tickTimeNano;
                if (nextTickTime < tmNow) {
                    nextTickTime = tmNow;
                }

                if (!state.isPlaying()) {
                    continue;
                }

                long tmStart = getTimeNanos();

                if (state.isRewindDone()) {
                    currentStep = 0;
                    lastStepTime = 0;
                    lastTickTime = 0;
                    nextTickTime = 0;

                    for (SequencerListener listener : listeners) {
                        listener.onSequencerPositionUpdate(state.getPosition(), true);
                    }

                    continue;
                }

                state.increment();
                SequencerPosition position = state.getPosition();
                sequenceTick(position);

                if (Debug.SHOW_SCHEDULING_STATS || Debug.SHOW_SCHEDULING_JITTER_WARNINGS) {
                    long tmElapsed = (lastTickTime > 0) ? tmStart - lastTickTime : 0;
                    lastTickTime = tmStart;

                    if (tmElapsed > 0) {
                        long tickTime = globals.getTickTimeNano();
                        long diffTime = tmElapsed - tickTime;

                        if (Debug.SHOW_SCHEDULING_STATS) {
                            stats.addTickJitter(diffTime);
                        }

                        if (Math.abs(diffTime) > Debug.SHOW_SCHEDULING_JITTER_TICK_LIMIT) {
                            Logger.d(TAG, "tick jitter: " + tmElapsed + " / " + tickTime + " / " + diffTime + " (" + diffTime / 1000 + "µs)");
                        }
                    }
                }

                long step = position.getStep();
                boolean stepChange = false;

                if (step > currentStep || (step != currentStep && state.isOverrun())) {

                    if (Debug.SHOW_SCHEDULING_STATS || Debug.SHOW_SCHEDULING_JITTER_WARNINGS) {
                        long tmElapsed = (lastStepTime > 0) ? tmStart - lastStepTime : 0;
                        lastStepTime = tmStart;

                        if (tmElapsed > 0) {
                            long stepTime = globals.getTickTimeNano() * Constants.TICKS_PER_STEP;
                            long diffTime = tmElapsed - stepTime;

                            if (Debug.SHOW_SCHEDULING_STATS) {
                                stats.addStepJitter(diffTime);
                            }

                            if (Math.abs(diffTime) > Debug.SHOW_SCHEDULING_JITTER_STEP_LIMIT) {
                                Logger.d(TAG, "step jitter: " + tmElapsed + " / " + stepTime + " / " + diffTime + " (" + diffTime / 1000 + "µs)");
                            }
                        }
                    }

                    sequenceStep(step);
                    currentStep = step;
                    stepChange = true;
                }

                if (null != midi) {
                    midi.flushMidiOutput();
                }

                for (SequencerListener listener : listeners) {
                    listener.onSequencerPositionUpdate(position, stepChange);
                }

            } else {
                allTracksOff();

                //Logger.d(TAG, "sequencer paused");
                if (!waitStateChange(250000000)) {
                    break;
                }
                nextTickTime = 0; // immediately set active
                currentStep = -1;
            }
        }

        allTracksOff();
    }

    private void sequenceTick(SequencerPosition position) {
        if (null == tracks) {
            return;
        }

        double pos = position.get();

        for (SequencerTrack track : tracks) {
            track.updateTick(pos);
        }

    }

    private void sequenceStep(long step) {
        if (null == tracks) {
            return;
        }

        Logger.d(TAG, "sequenceStep " + step);

        double pos = (double) (step * Constants.TICKS_PER_STEP);

        for (SequencerTrack track : tracks) {
            track.update(pos);
        }
    }

}
