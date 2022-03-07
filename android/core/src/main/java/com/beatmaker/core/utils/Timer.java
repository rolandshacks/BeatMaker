package com.beatmaker.core.utils;

public class Timer implements Runnable {

    private static final String TAG = "Timer";
    private static final int PRIORITY_UNDEFINED = 0xffff;

    private long interval; // timer interval in microseconds
    private int priority = PRIORITY_UNDEFINED;
    private Thread thread;
    private boolean running;
    private long delta ;
    private boolean passive;
    private boolean triggered;
    private long minLoopDelay;
    private boolean manualStats;
    private String name;

    private final Object lock = new Object();

    public class Stats {

        private  long tmLastUpdate = 0;

        private long executionCount = 0;
        private long executionTimeCount = 0;

        private double avgUpdatesPerSecond = 0.0;
        private double avgExecutionTime = 0.0;

        public void reset() {
            tmLastUpdate = 0;
            executionCount = 0;
            executionTimeCount = 0;
            avgUpdatesPerSecond = 0.0;
            avgExecutionTime = 0.0;
        }

        public void inc(long tm, long executionTime) {
            executionCount++;
            executionTimeCount += executionTime;

            long elapsed = tm - tmLastUpdate;

            if (elapsed > 1000000) {
                if (tmLastUpdate > 0) {
                    avgUpdatesPerSecond = (double) (executionCount * 1000000) / (double) elapsed;
                    avgExecutionTime = (executionCount > 0) ? (double) executionTimeCount / (double) executionCount : 0.0;
                    executionCount = 0;
                    executionTimeCount = 0;
                }
                tmLastUpdate = tm;
            }
        }

        public double getAvgUpdatesPerSecond() {
            return avgUpdatesPerSecond;
        }

        public double getAvgExecutionTime() {
            return avgExecutionTime;
        }
    }

    private final Stats stats = new Stats();

    public Timer() {
        initialize(0, PRIORITY_UNDEFINED);
    }

    public Timer(long interval) {
        initialize(interval, PRIORITY_UNDEFINED);
    }

    public Timer(long interval, int priority) {
        initialize(interval, priority);
    }

    private void initialize(long interval, int priority) {
        this.interval = interval;
        this.priority = priority;
    }

    public boolean isRunning() {
        return running;
    }

    public void setInterval(long interval) {
        this.interval = interval;
    }

    public long getInterval() {
        return interval;
    }

    public void setName(String name) {
        this.name = name;
        if (null != thread) {
            thread.setName(name);
        }
    }

    public String getName() {
        return name;
    }

    public synchronized void start() {

        Logger.d(TAG, "start");

        running = true;

        if (null != thread) return;

        thread = new Thread("Timer Thread") {
            public void run() {
                Logger.d(TAG, "timer thread enter");
                timerLoop();
                Logger.d(TAG, "timer thread exit");
            }
        };

        if (null != name) {
            thread.setName(name);
        }

        thread.start();
    }

    public synchronized void stop() {

        Logger.d(TAG, "stop");

        running = false;
        triggered = false;

        Thread t = thread;
        if (null != t) {
            t.interrupt();
            try {
                t.join(250);
            } catch (InterruptedException ignored) {
                ;
            }
        }

        thread = null;
    }

    public void setPassiveMode(boolean passive) {
        this.passive = passive;
    }

    public void trigger() {
        synchronized (lock) {
            triggered = true;
            lock.notify();
        }
    }

    private long getTimeMicro() {
        return System.nanoTime()/1000;
    }

    private boolean timerLoopActive() {
        Thread t = thread;
        return running && t.isAlive() && !t.isInterrupted();
    }

    private void setMinLoopDelay(long delay) {
        this.minLoopDelay = delay;
    }

    private void timerLoop() {

        Thread t = thread;

        if (priority != PRIORITY_UNDEFINED) {
            SystemUtils.setPriority(priority);
        } else {
            priority = SystemUtils.getPriority();
        }

        long tmNext = getTimeMicro() + interval;
        long tmLast = 0;

        while (timerLoopActive()) {

            long tmNow = getTimeMicro();

            if (tmNow < tmNext) {
                long tmWait = tmNext - tmNow;
                synchronized (lock) {
                    try {
                        lock.wait(tmWait / 1000);
                    } catch (InterruptedException ignored) {}
                }

                if (!timerLoopActive()) {
                    break;
                }
            } else if (minLoopDelay > 0) {
                try {
                    //noinspection BusyWait
                    Thread.sleep(minLoopDelay / 1000);
                } catch (InterruptedException ignored) {
                    break;
                }
            }

            if (!passive || triggered) {

                long tmElapsed = tmNow - tmLast;
                delta = tmLast > 0 ? tmElapsed : 0;
                long tmExecStart = tmNow;

                run();

                long tmExecEnd = getTimeMicro();
                long tmExec = tmExecEnd - tmExecStart;

                if (!manualStats) {
                    stats.inc(tmNow, tmExec);
                }

                delta = 0;
                tmLast = tmNow;

                if (!triggered) {
                    tmNext += interval;
                    if (tmNext < tmNow) {
                        tmNext = tmNow;
                    }
                } else {
                    tmNext = tmNow + interval;
                }

                triggered = false;
            }
        }

        running = false;
    }

    public double getDeltaSeconds() {
        return (double) delta / 1000000.0;
    }

    public long getDeltaMicros() {
        return delta;
    }

    public void run() {
        throw new RuntimeException("Stub!");
    }

    public Stats getStats() {
        return stats;
    }

    public void setManualStats(boolean enable) {
        manualStats = enable;
    }

    public void updateStats(long executionTime) {
        long tm = getTimeMicro();
        stats.inc(tm, executionTime);
    }

}
