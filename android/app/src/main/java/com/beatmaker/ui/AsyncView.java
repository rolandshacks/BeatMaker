package com.beatmaker.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.beatmaker.core.utils.Logger;
import com.beatmaker.core.utils.Timer;

public class AsyncView extends View {

    private static final String TAG = "DebugView";
    private static final int DEFAULT_FRAMES_PER_SECOND = 60;

    private Timer renderTimer;
    private int renderFramesPerSecond = DEFAULT_FRAMES_PER_SECOND;
    private boolean renderDirtyCheck = false;

    private boolean initialized = false;
    private volatile boolean startRequested = false;

    public AsyncView(Context context) {
        super(context);
    }

    public AsyncView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public AsyncView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AsyncView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private void create() {

        if (initialized) {
            return;
        }

        if (0 == renderFramesPerSecond) {
            renderFramesPerSecond = DEFAULT_FRAMES_PER_SECOND;
        }

        renderTimer = new Timer(1000000 / renderFramesPerSecond) {
            public void run() {
                postInvalidate();
            }
        };
        renderTimer.setName("AsyncView Timer");
        renderTimer.setPassiveMode(renderDirtyCheck);
        renderTimer.setManualStats(true);

        loadResources();
        initialize();

        initialized = true;

        if (startRequested) {
            startRequested = false;
            start();
        }
    }

    protected void initialize() {
    }

    protected void loadResources() {
    }

    protected void unloadResources() {
    }

    public synchronized void start() {

        Logger.d(TAG, "start rendering");

        if (!initialized) {
            startRequested = true;
            return;
        }

        if (null != renderTimer) {
            renderTimer.setInterval(1000000 / renderFramesPerSecond);
            renderTimer.start();
        }
    }

    public synchronized void stop() {

        Logger.d(TAG, "stop rendering");

        if (null != renderTimer) {
            renderTimer.stop();
        }
    }

    public void enableDirtyCheck(boolean enable) {
        renderDirtyCheck = enable;
        if (null != renderTimer) {
            renderTimer.setPassiveMode(enable);
        }
    }

    public void setDirty() {
        if (null != renderTimer) {
            renderTimer.trigger();
        }
    }

    protected void setFramesPerSecond(int fps) {
        if (fps > 0) {
            renderFramesPerSecond = fps;
        } else {
            renderFramesPerSecond = DEFAULT_FRAMES_PER_SECOND;
        }
    }

    public Timer.Stats getStats() {
        if (null == renderTimer) return null;
        return renderTimer.getStats();
    }

    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        canvas.save();

        if (false == initialized) {
            create();
        }

        long tmStart = System.nanoTime();
        redraw(canvas);
        long tmEnd = System.nanoTime();

        canvas.restore();

        renderTimer.updateStats((tmEnd-tmStart)/1000);

    }

    protected void redraw(Canvas canvas) { ; }
}
