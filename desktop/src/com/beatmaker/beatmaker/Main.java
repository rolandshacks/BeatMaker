package com.beatmaker.beatmaker;

import com.beatmaker.app.DesktopApplication;

public class Main {

    private static final String TAG = "Main";

    public static void main(String[] args) {

        DesktopApplication app = new DesktopApplication();

        app.create();
        app.startup();

        while (true) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {
                break;
            }
        }

        app.shutdown();
    }
}
