package com.beatmaker.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.beatmaker.core.utils.Logger;
import com.beatmaker.core.utils.Timer;

public class AsyncSurfaceView extends SurfaceView {

    private static final String TAG = "AsyncSurfaceView";
    private static final int DEFAULT_FRAMES_PER_SECOND = 60;

    private int renderFramesPerSecond = DEFAULT_FRAMES_PER_SECOND;

    private boolean initialized = false;
    private boolean renderingActive = false;
    private SurfaceHolder surfaceHolder;

    private Timer renderTimer;

    public AsyncSurfaceView(Context context) {
        super(context);
        create();
    }

    public AsyncSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        create();
    }

    public AsyncSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        create();
    }

    private void create() {

        initialized = false;

        renderTimer = new Timer() {
            @Override
            public void run() {
                redrawSurface();
            }
        };

        loadResources();
        initialize();

        surfaceHolder = getHolder();
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {

            @SuppressLint("WrongCall")
            public void surfaceCreated(SurfaceHolder holder) {
                onSurfaceCreated();
            }

            public void surfaceDestroyed(SurfaceHolder holder) {
                onSurfaceDestroyed();
            }

            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                formatChanged(format, width, height);
            }
        });

    }

    @SuppressLint("WrongCall")
    private void onSurfaceCreated() {
        Logger.d(TAG, "surface created");
        setWillNotDraw(false);
        initialized = true;
        redrawSurface();
        start();
    }

    private void onSurfaceDestroyed() {
        Logger.d(TAG, "surface destroyed");
        stop();
    }

    private boolean isSurfaceValid() {
        SurfaceHolder s = surfaceHolder;
        if (null == s) return false;
        return s.getSurface().isValid();
    }

    private boolean redrawSurface()
    {
        if (!isSurfaceValid()) {
            return false;
        }

        Canvas canvas = surfaceHolder.lockCanvas(null);
        if (null == canvas) {
            Logger.e(TAG, "canvas is null");
            return false;
        }

        try {
            canvas.save();
            redraw(canvas);
        } catch (Exception e) {
            Logger.e(TAG, e.getMessage());
        } finally {
            canvas.restore();
            surfaceHolder.unlockCanvasAndPost(canvas);
        }

        return true;
    }

    protected void invalidateSurface() {
        renderTimer.trigger();
    }

    public synchronized void start() {

        Logger.d(TAG, "start rendering");

        if (!initialized) return;

        renderingActive = true;
        renderTimer.setInterval(1000000 / renderFramesPerSecond);
        renderTimer.start();
    }

    public synchronized void stop() {

        Logger.d(TAG, "stop rendering");

        renderTimer.stop();

        if (renderingActive) {
            renderingActive = false;
        }

    }

    public void setDirty() {
        renderTimer.trigger();
    }

    public void enableDirtyCheck(boolean enable) {
        renderTimer.setPassiveMode(enable);
    }

    protected void initialize() { ; }

    protected void loadResources() {
        ;
    }

    protected void unloadResources() {
        ;
    }

    protected void formatChanged(int format, int width, int height) {
        Logger.d(TAG, "surface changed: " + format + " / " + width + " / " + height);
    }

    protected void redraw(Canvas canvas) {
        ;
    }

    protected double getTimeDelta() {
        return renderTimer.getDeltaSeconds();
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

}
