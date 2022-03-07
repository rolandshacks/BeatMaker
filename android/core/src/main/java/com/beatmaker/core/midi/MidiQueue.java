package com.beatmaker.core.midi;

import com.beatmaker.config.Debug;
import com.beatmaker.core.utils.Logger;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@SuppressWarnings("NonAtomicOperationOnVolatileField")
public class MidiQueue {

    private static final String TAG = "MidiQueue";
    public static final int DEFAULT_SIZE = 4096; // queue elements

    private final Object sync = new Object();
    private final Lock lock = new ReentrantLock();

    private int size;
    private volatile int count;
    private int add_ofs;
    private int pull_ofs;

    private MidiData[] data;

    class MidiQueueIterator implements Iterator<MidiData> {

        private final MidiQueue q;

        protected MidiQueueIterator(MidiQueue q) {
            this.q = q;
        }

        @Override
        public boolean hasNext() {
            return !q.isEmpty();
        }

        @Override
        public MidiData next() {
            return q.lockedPoll();
        }
    }

    public MidiQueue(int size) {
        this.size = size;
        data = new MidiData[size];
    }

    public MidiQueueIterator iterator() {
        return new MidiQueueIterator(this);
    }

    public void add(MidiData midiData) {

        if (null == midiData) return;

        lock.lock();

        {
            if (count >= size) {
                lock.unlock();
                return;
            }

            if (null == data[add_ofs]) {
                data[add_ofs] = new MidiData();
            }

            data[add_ofs].set(midiData);
            add_ofs = (add_ofs + 1) % size;
            count++;
        }

        lock.unlock();

        synchronized(sync) {
            sync.notify();
        }
    }

    public void addArray(MidiData[] midiData) {
        if (null == midiData || midiData.length<1) {
            return;
        }

        lock.lock();

        {
            for (MidiData m : midiData) {

                if (count >= size) {
                    break;
                }

                if (null == data[add_ofs]) {
                    data[add_ofs] = new MidiData();
                }

                data[add_ofs].set(m);
                add_ofs = (add_ofs + 1) % size;
                count++;
            }
        }

        lock.unlock();

        synchronized(sync) {
            sync.notify();
        }
    }

    public void addList(List<MidiData> midiData) {
        if (null == midiData || midiData.isEmpty()) {
            return;
        }

        long t = System.nanoTime()/1000;
        long maxMidiEnqueueLatency = 0;

        lock.lock();

        {
            for (MidiData m : midiData) {

                if (count >= size) {
                    break;
                }

                if (null == data[add_ofs]) {
                    data[add_ofs] = new MidiData();
                }

                data[add_ofs].set(m);
                add_ofs = (add_ofs + 1) % size;
                count++;

                long midiTime = m.getTimestamp();
                long midiEnqueueLatency = (0 != midiTime) ? t - midiTime : 0;
                if (midiEnqueueLatency > maxMidiEnqueueLatency) {
                    maxMidiEnqueueLatency = midiEnqueueLatency;
                }
            }
        }

        lock.unlock();

        if (Debug.SHOW_LATENCIES) {
            Logger.d(TAG, "max enqueue latency: " + maxMidiEnqueueLatency);
        }

        synchronized(sync) {
            sync.notify();
        }
    }

    public MidiData poll() {

        if (count < 1) {
            return null;
        }

        MidiData m;

        lock.lock();

        {
            m = data[pull_ofs];
            if (null != m) {
                pull_ofs = (pull_ofs + 1) % size;
                count--;
            }
        }

        lock.unlock();

        return m;
    }

    public MidiData lockedPoll() {

        if (count < 1) {
            return null;
        }

        MidiData m = data[pull_ofs];
        if (null != m) {
            pull_ofs = (pull_ofs + 1) % size;
            count--;
        }

        return m;
    }

    public void lockBuffer() {
        lock.lock();
    }

    public void unlockBuffer() {
        lock.unlock();
    }

    public MidiData peek() {

        lock.lock();

        MidiData m = (count > 0) ? data[pull_ofs] : null;

        lock.unlock();

        return m;
    }

    public boolean isEmpty() {
        return (count < 1);
    }

    public int length() {
        return count;
    }

    public boolean waitForData(long timeout) {

        if (count > 0) {
            return true;
        }

        if (0 == timeout) {
            return false;
        }

        synchronized (sync) {
            try {
                sync.wait(timeout);
            } catch (InterruptedException e) {
                return false;
            }
        }

        return (count > 0);
    }

    public void clear() {

        lock.lock();

        {
            count = 0;
            add_ofs = 0;
            pull_ofs = 0;
        }

        lock.unlock();
    }
}
