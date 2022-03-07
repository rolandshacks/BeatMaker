package com.beatmaker.core.midi;

import com.beatmaker.config.Constants;
import com.beatmaker.config.Debug;
import com.beatmaker.config.Settings;
import com.beatmaker.core.utils.Logger;
import com.beatmaker.core.utils.SystemUtils;
import com.beatmaker.core.utils.Timer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class MidiBase implements MidiApiProvider {

    private static final String TAG = "MidiBase";

    private static MidiBase instance_;

    protected MidiInterfaceBase currentInterface;
    protected MidiDispatcher dispatcher;
    protected MidiClock midiClock;

    protected Timer updateTimer;
    protected MidiIoThread midiInputThread;
    protected MidiQueue midiInputQueue;
    protected Thread midiInputDispatcherThread;
    protected MidiIoThread midiOutputThread;
    protected final ArrayList<MidiData> midiOutputQueue = new ArrayList<>();

    protected boolean paused = false;

    private HashMap<String, MidiInterfaceBase> midiInterfaces = new HashMap<>();

    public static MidiBase instance() {
        return instance_;
    }

    public MidiBase() {
        assert(null == instance_); // ensure singleton
        instance_ = this;
    }

    public void addInputListener(MidiInputListener midiInputListener) {
        dispatcher.addInputListener(midiInputListener);
    }

    public void removeInputListener(MidiInputListener midiInputListener) {
        dispatcher.removeInputListener(midiInputListener);
    }

    public void removeAllInputListeners() {
        dispatcher.removeAllInputListeners();
    }

    public synchronized void create() {

        this.midiClock = new MidiClock();
        this.dispatcher = new MidiDispatcher(midiClock);

        Logger.d(TAG, "created midi controller");

        if (!Debug.MOCKUP_MIDI) {
            updateTimer = new Timer(1000000, Settings.instance().getPriorities().backgroundThreadPriority) {
                @Override
                public void run() {
                    update();
                }
            };
            updateTimer.setName("midi device update timer");
        }
    }

    public synchronized void destroy() {
        stop();

        if (this == instance_) {
            instance_ = null;
        }
    }

    public synchronized void start() {
        Logger.d(TAG, "start midi controller");

        paused = false;

        update();

        if (!Debug.MOCKUP_MIDI) {
            if (null != updateTimer) {
                updateTimer.start();
            }
        }
    }

    public synchronized void stop() {
        Logger.d(TAG, "stop midi controller");

        if (null != updateTimer) {
            updateTimer.stop();
        }

        paused = false;

        if (null != currentInterface) {
            currentInterface.close();
            currentInterface = null;
        }

        if (null != midiInterfaces) {
            midiInterfaces.clear();
        }

    }

    protected HashMap<String, MidiInterfaceBase> queryMidiInterfaces() {
        HashMap<String, MidiInterfaceBase> interfaces = new HashMap<>();
        return interfaces;
    }

    private synchronized void update() {

        if (paused) {
            return;
        }

        //Logger.d(TAG, "update device list");

        if (Debug.MOCKUP_MIDI) {

            if (midiInterfaces.isEmpty()) {
                MidiInterfaceBase intf = new MockMidiInterface("test.interface.0", "Test Interface");
                midiInterfaces.put(intf.getId(), intf);
                intf.addPort(new MockMidiPort(MidiPortBase.INPUT));
                intf.addPort(new MockMidiPort(MidiPortBase.OUTPUT));
                Settings.instance().setMidiInterface(intf.getAlias());
                onInterfaceReady(intf, true);
            }

            return;
        }

        HashMap<String, MidiInterfaceBase> currentMidiInterfaces = queryMidiInterfaces();

        ArrayList<MidiInterfaceBase> removeList = new ArrayList<>();
        ArrayList<MidiInterfaceBase> addList = new ArrayList<>();

        synchronized (midiInterfaces) {
            for (MidiInterfaceBase midiInterface : currentMidiInterfaces.values()) {
                if (!midiInterfaces.containsKey(midiInterface.getId())) {
                    midiInterface.detectPorts();
                    addList.add(midiInterface);
                }
            }
            for (MidiInterfaceBase midiInterface : midiInterfaces.values()) {
                if (!currentMidiInterfaces.containsKey(midiInterface.getId())) {
                    removeList.add(midiInterface);
                }
            }
        }

        for (MidiInterfaceBase midiInterface : removeList) {
            onRemoveInterface(midiInterface);
        }

        if (removeList.isEmpty() && addList.isEmpty()) {
            return;
        }

        synchronized (midiInterfaces) {

            for (MidiInterfaceBase midiInterface : removeList) {
                midiInterface.close();
                midiInterfaces.remove(midiInterface.getId());
            }

            for (MidiInterfaceBase midiInterface : addList) {
                midiInterfaces.put(midiInterface.getId(), midiInterface);
            }

            HashMap<String, MidiInterfaceBase> aliases = new HashMap<>();

            for (MidiInterfaceBase midiInterface : midiInterfaces.values()) {
                int counter = 0;
                midiInterface.setAlias(counter);
                while (aliases.containsKey(midiInterface.getAlias())) {
                    counter++;
                    midiInterface.setAlias(counter);
                }
                aliases.put(midiInterface.getAlias(), midiInterface);
            }
        }

        for (MidiInterfaceBase midiInterface : addList) {
            onAddInterface(midiInterface);
        }
    }

    protected void onInterfaceReady(MidiInterfaceBase midiInterface, boolean createInputThread) {

        if (midiInterface.isConnected()) {
            return;
        }

        midiInterface.open();

        currentInterface = midiInterface;

        Logger.d(TAG, "opened midi interface");

        final MidiInterfaceBase intf = midiInterface;

        if (createInputThread) {
            midiInputThread = new MidiIoThread(midiInterface) {
                public void run() {
                    midiInputLoop(intf);
                }
            };
            midiInputThread.setName("midi input thread");
            midiInputThread.allocQueue(MidiQueue.DEFAULT_SIZE);
            midiInputThread.start();
        } else {
            // queue must be pre-allocated
            assert(null != midiInputQueue);
        }

        midiInputDispatcherThread = new Thread() {
            public void run() {
                if (null != midiInputThread) {
                    midiInputDispatchLoop(midiInputThread.getQueue());
                } else {
                    midiInputDispatchLoop(midiInputQueue);
                }
            }
        };
        midiInputDispatcherThread.setName("midi input dispatcher thread");
        midiInputDispatcherThread.start();

        if (Constants.MIDI_OUTPUT_ASYNC_MODE) {
            midiOutputThread = new MidiIoThread(midiInterface) {
                public void run() {
                    midiOutputLoop(intf);
                }
            };
            midiOutputThread.setName("midi output thread");
            midiOutputThread.allocQueue(MidiQueue.DEFAULT_SIZE);
            midiOutputThread.start();
        }
    }

    protected void onAddInterface(MidiInterfaceBase midiInterface) {
        Logger.d(TAG, "added midi interface: " + midiInterface.getAlias());

        String intfName = Settings.instance().getMidiInterface();

        if (null == currentInterface && (midiInterface.getAlias().equals(intfName) || intfName.isEmpty())) {
            connect(midiInterface);
            Settings.instance().setMidiInterface(midiInterface.getAlias());
        }
    }

    protected void onRemoveInterface(MidiInterfaceBase midiInterface) {
        Logger.d(TAG, "removed midi interface: " + midiInterface.getAlias());

        if (midiInterface == currentInterface) {
            disconnect(midiInterface);
            currentInterface = null;
        }
    }

    protected void connect(MidiInterfaceBase midiInterface) {
    }

    protected void disconnect(MidiInterfaceBase midiInterface) {
        Logger.d(TAG, "disconnect midi interface: " + midiInterface.getAlias());

        if (null != midiInputThread) {
            midiInputThread.interrupt();
        }

        midiInputDispatcherThread.interrupt();

        if (null != midiOutputThread) {
            midiOutputThread.interrupt();
        }

        if (null != midiInputThread) {
            try {
                midiInputThread.join(500);
            } catch (InterruptedException ignored) {}
            midiInputThread = null;
        }

        try {
            midiInputDispatcherThread.join(500);
        } catch (InterruptedException ignored) {
        }
        midiInputDispatcherThread = null;

        if (null != midiOutputThread) {
            try {
                midiOutputThread.join(500);
            } catch (InterruptedException ignored) {
            }
            midiOutputThread = null;
        }

        clearMidiOutput();

        midiInterface.close();
    }

    public void pause() {
        paused = true;
    }

    public void resume() {
        paused = false;
    }

    protected void midiInputLoop(MidiInterfaceBase midiInterface) {

        if (null == midiInterface) {
            return;
        }

        MidiPortBase port = midiInterface.getDefaultInput();
        if (null == port) {
            return;
        }

        int maxPacketSize = port.getMaxPacketSize();
        byte[] packetBuffer = new byte[maxPacketSize];

        MidiQueue queue = midiInputThread.getQueue();

        midiClock.reset();

        SystemUtils.setPriority(Settings.instance().getPriorities().midiInputThreadPriority);

        while (!Thread.interrupted()) {

            if (false == midiInterface.isConnected()) {
                break;
            }

            int sz = midiInterface.receive(port, packetBuffer, maxPacketSize, Constants.MIDI_DEVICE_IO_TIMEOUT);

            if (paused) {
                continue;
            }

            long midiReceiveTime = getTimeMicros();

            if (sz > 0) {

                int ofs = 0;

                while (ofs < sz) {
                    MidiData midiData = new MidiData();
                    int deserializedBytes = midiData.deserialize(packetBuffer, ofs, sz);
                    if (deserializedBytes < 1) {
                        break;
                    }
                    ofs += deserializedBytes;
                    midiData.setTimestamp(midiReceiveTime);
                    queue.add(midiData);
                }

            } else if (sz == 0) {
                Logger.d(TAG, "no data");
            } else {
                Logger.d(TAG, "error - I/O operation failed");
            }
        }
    }

    protected void midiInputDispatchLoop(MidiQueue queue) {

        SystemUtils.setPriority(Settings.instance().getPriorities().midiInputDispatchThreadPriority);

        while (!Thread.interrupted()) {

            if (queue.waitForData(100)) {
                //Logger.d(TAG, "new midi data!!!");
                while (!queue.isEmpty()) {
                    MidiData midiData = queue.poll();
                    if (null != midiData) {
                        dispatcher.dispatch(midiData);
                    }
                }
            }
        }

    }

    public void playTestTone(boolean pressed) {

        MidiData m;

        int channel = 0x0;

        if (pressed) {
            // note on
            m = new MidiData(0, 0, 0x9, 0x90 | (channel&0xf), 0x40, 0x40);
        } else {
            // note off
            m = new MidiData(0, 0, 0x9, 0x90 | (channel&0xf), 0x40, 0x00);
        }

        send(m);

        {
            // second note

            MidiData m2;

            int channel2 = 0x1;

            if (pressed) {
                // note on
                m2 = new MidiData(0, 0, 0x9, 0x90 | (channel2&0xf), 0x45, 0x40);
            } else {
                // note off
                m2 = new MidiData(0, 0, 0x9, 0x90 | (channel2&0xf), 0x45, 0x00);
            }

            send(m2);

        }
    }

    public int send(MidiData midiData) {

        if (Constants.MIDI_OUTPUT_ASYNC_MODE) {

            if (null == midiOutputThread) {
                return -1;
            }

            MidiQueue queue = midiOutputThread.getQueue();
            if (null == queue) {
                return -1;
            }

            queue.add(midiData);

            return 0;
        } else {

            MidiInterfaceBase intf = currentInterface;
            if (null == intf) {
                return -1;
            }

            MidiPortBase port = intf.getDefaultOutput();
            if (null == port) {
                return -1;
            }

            return send(intf, port, midiData);
        }
    }

    public int send(List<MidiData> midiData) {

        if (Constants.MIDI_OUTPUT_ASYNC_MODE) {

            if (null == midiOutputThread) {
                return -1;
            }

            MidiQueue queue = midiOutputThread.getQueue();
            if (null == queue) {
                return -1;
            }

            queue.addList(midiData);

            return 0;

        } else {

            MidiInterfaceBase intf = currentInterface;
            if (null == intf) {
                return -1;
            }

            MidiPortBase port = intf.getDefaultOutput();
            if (null == port) {
                return -1;
            }

            return send(intf, port, midiData.iterator());

        }
    }

    protected int send(MidiInterfaceBase midiInterface, MidiPortBase midiPort, MidiData midiData) {
        if (null == midiInterface || null == midiPort || null == midiData) {
            return -1;
        }

        int packetSize = Math.min(4, midiPort.getMaxPacketSize());
        byte[] packetBuffer = new byte[packetSize];

        int sz = midiData.serialize(packetBuffer, 0, packetSize);
        int bytes = midiInterface.send(midiPort, packetBuffer, 0, Constants.MIDI_DEVICE_IO_TIMEOUT);

        if (bytes != sz) return -1;

        return bytes;
    }

    protected int send(MidiInterfaceBase midiInterface, MidiPortBase midiPort, Iterator<MidiData> it) {
        if (null == midiInterface || null == midiPort || null == it) {
            return -1;
        }

        int maxPacketSize = midiPort.getMaxPacketSize();
        byte[] packetBuffer = new byte[maxPacketSize];

        int ofs = 0;
        int transferCount = 0;

        long maxLatency = 0;
        long earliestTime = 0;

        while (it.hasNext()) {
            MidiData m = it.next();
            boolean lastElement = !it.hasNext();

            long timestamp = m.getTimestamp();
            if (earliestTime == 0 || (timestamp < earliestTime && timestamp != 0)) {
                earliestTime = timestamp;
            }

            int serializedSize = m.serialize(packetBuffer, ofs, maxPacketSize);
            if (serializedSize < 1) {
                break;
            }
            ofs += serializedSize;

            if (ofs >= maxPacketSize || lastElement) {
                int sz = midiInterface.send(midiPort, packetBuffer, ofs, Constants.MIDI_DEVICE_IO_TIMEOUT);
                if (sz != ofs) {
                    Logger.e(TAG, "output I/O error: sent " + sz + " bytes instead of " + ofs);
                    return -1;
                }
                transferCount += ofs;
                ofs = 0;

                // update latency measurement
                long now = getTimeMicros();
                long latency = (0 != earliestTime) ? now - earliestTime : 0;
                if (latency > maxLatency) maxLatency = latency;
                earliestTime = 0;
            }
        }

        if (Debug.SHOW_LATENCIES) {
            Logger.d(TAG, "max. output latency: " + maxLatency);
        }

        return transferCount;
    }

    protected void midiOutputLoop(MidiInterfaceBase midiInterface) {

        assert(null != midiOutputThread);

        if (null == midiInterface) {
            return;
        }

        MidiPortBase port = midiInterface.getDefaultOutput();
        if (null == port) {
            return;
        }

        MidiQueue queue = midiOutputThread.getQueue();

        SystemUtils.setPriority(Settings.instance().getPriorities().midiOutputThreadPriority);

        {
            // send all notes off before starting
            send(midiInterface, port, new MidiData(0, 0, 0xb, 0xb0, 0x7b, 0x0));
        }

        while (!Thread.interrupted()) {

            if (false == midiInterface.isConnected()) {
                break;
            }

            if (paused) {
                try {
                    //noinspection BusyWait
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }

            if (queue.waitForData(250)) {
                queue.lockBuffer();
                try {
                    send(midiInterface, port, queue.iterator());
                } finally {
                    queue.unlockBuffer();
                }
            }
        }

        {
            // send all notes off before closing
            Logger.d(TAG, "send all notes off before closing midi output");
            send(midiInterface, port, new MidiData(0, 0, 0xb, 0xb0, 0x7b, 0x0));
        }
    }

    public void enqueueMidiOutput(MidiData midiData) {
        if (midiOutputQueue.size() >= Constants.MIDI_OUTPUT_MAX_QUEUE_SIZE) return;
        if (0 == midiData.getTimestamp()) {
            midiData.setTimestamp(getTimeMicros());
        }
        midiOutputQueue.add(midiData);
    }

    public void flushMidiOutput() {
        if (midiOutputQueue.isEmpty()) {
            return;
        }

        send(midiOutputQueue);

        midiOutputQueue.clear();
    }

    public void clearMidiOutput() {
        midiOutputQueue.clear();
    }

    protected long getTimeMicros() {
        return System.nanoTime()/1000;
    }

}
