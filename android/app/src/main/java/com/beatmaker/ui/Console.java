package com.beatmaker.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class Console {

    private static final int MAX_CONSOLE_LINES = 128;

    private final ReentrantLock lock = new ReentrantLock();
    private ArrayList<String> consoleLines;
    private long consoleStartTime = 0;

    public Console() {

    }

    public void println(String tag, String text) {
        long t = System.currentTimeMillis();
        if (0 == consoleStartTime) {
            consoleStartTime = t;
        }

        //(t - consoleStartTime) + " " + text
    }

    public void println(String text) {
        println("Console", text);
    }

    private void add(String s) {

        lock.lock();

        if (consoleLines.size() >= MAX_CONSOLE_LINES) {
            consoleLines.remove(0);
        }
        consoleLines.add(s);

        lock.unlock();

    }

    public List<String> lockBuffer() {
        lock.lock();
        return consoleLines;
    }

    public void unlockBuffer() {
        lock.unlock();
    }


}
