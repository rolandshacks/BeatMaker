package com.beatmaker.app;

import android.content.Context;

import com.beatmaker.config.Settings;
import com.beatmaker.core.application.ApplicationBase;
import com.beatmaker.core.utils.SystemCalls;
import com.beatmaker.core.utils.SystemUtils;
import com.beatmaker.io.UsbMidi;

public class Application extends ApplicationBase {

    public static final int LONG_TOUCH_DELAY_MS = 800; // milliseconds
    public static final int LONG_TOUCH_MOVE_LIMIT = 50; // pixels

    public static final boolean OPEN_SETTINGS_AT_START = true;

    public static final boolean SHOW_FPS = false;

    public static final boolean DISABLE_RENDERING_ALL = false;
    public static final boolean DISABLE_RENDERING_TRACKS = false;
    public static final boolean DISABLE_RENDERING_ELEMENTS = false;

    public static final boolean OVERWRITE_VIEW_METRICS = false;
    public static final int OVERWRITE_VIEW_WIDTH = 2732;
    public static final int OVERWRITE_VIEW_HEIGHT = 2048;
    public static final int OVERWRITE_VIEW_DENSITY = 264;

    /*
    public static final int OVERWRITE_VIEW_WIDTH = 800;
    public static final int OVERWRITE_VIEW_HEIGHT = 600;
    public static final int OVERWRITE_VIEW_DENSITY = 96;

    public static final int OVERWRITE_VIEW_WIDTH = 1280;
    public static final int OVERWRITE_VIEW_HEIGHT = 800;
    public static final int OVERWRITE_VIEW_DENSITY = 160;

    public static final int OVERWRITE_VIEW_WIDTH = 2732;
    public static final int OVERWRITE_VIEW_HEIGHT = 2048;
    public static final int OVERWRITE_VIEW_DENSITY = 264;
    */

    private Context context;

    public Application(Context context) {
        super();
        this.context = context;
    }

    @Override
    public void create() {
        setSystemCallInterface();
        settings = new Settings();
        setPriorities();
        midi = new UsbMidi(context);
        super.create();
    }

    private void setSystemCallInterface() {
        SystemUtils.setSystemCallInterface(new SystemCalls() {
            @Override
            public void forceTerminateApplication() {
                android.os.Process.killProcess(android.os.Process.myPid());
                java.lang.System.exit(0);
            }

            @Override
            public void setPriority(int priority) {
                android.os.Process.setThreadPriority(priority);
            }

            @Override
            public int getPriority() {
                return android.os.Process.getThreadPriority(android.os.Process.myTid());
            }
        });
    }

    private void setPriorities() {
        Settings.Priorities p = Settings.instance().getPriorities();
        p.sequencerThreadPriority = android.os.Process.THREAD_PRIORITY_DEFAULT;
        p.midiInputThreadPriority = android.os.Process.THREAD_PRIORITY_DEFAULT;
        p.midiInputDispatchThreadPriority = android.os.Process.THREAD_PRIORITY_DEFAULT;
        p.midiOutputThreadPriority = android.os.Process.THREAD_PRIORITY_DEFAULT;
        p.backgroundThreadPriority = android.os.Process.THREAD_PRIORITY_BACKGROUND;
    }
}
