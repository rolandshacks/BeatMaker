package com.beatmaker.beatmaker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

import com.beatmaker.app.Application;
import com.beatmaker.config.Constants;
import com.beatmaker.core.sequencer.Sequencer;
import com.beatmaker.core.sequencer.SequencerControl;
import com.beatmaker.core.sequencer.SequencerElement;
import com.beatmaker.core.sequencer.SequencerPosition;
import com.beatmaker.core.sequencer.SequencerStep;
import com.beatmaker.core.sequencer.SequencerTrack;
import com.beatmaker.core.utils.Logger;
import com.beatmaker.core.utils.Timer;
import com.beatmaker.ui.AsyncView;
import com.beatmaker.ui.Console;

import java.util.ArrayList;
import java.util.List;

public class BeatView extends AsyncView  {
    private static final String TAG = "BeatView";
    private static final int FPS = 5;
    private static final boolean ENABLE_DIRTY_CHECK = true;

    private static final int EDIT_NONE = 0;
    private static final int EDIT_SET = 1;
    private static final int EDIT_CLEAR = 2;

    private Layout layout;

    private long lastStatsUpdateTime;
    private int editMode = EDIT_NONE;

    private boolean touchActive;
    private Point touchReferencePoint;
    private boolean touchMove;
    private long touchStartTime;
    private Runnable touchCallback;
    private int backgroundColor;

    private SequencerTrack selectedTrack;
    private SequencerStep selectedElement;

    private List<ActionListener> actionListeners = new ArrayList<>();

    public interface ActionListener {
        void onFocus();
        void onSelect(SequencerElement element);
    }

    public BeatView(Context context) {
        super(context);
        initializeView(context, null);
    }

    public BeatView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeView(context, attrs);
    }

    public BeatView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initializeView(context, attrs);
    }

    private void initializeView(Context context, AttributeSet attrs) {
        if (null != attrs) {
            TypedArray style = context.getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.BeatView,
                    0, 0);

            backgroundColor = style.getColor(R.styleable.BeatView_android_backgroundTint, 0xff000000);
        } else {
            backgroundColor = 0xff000000;
        }
    }

    public void setActionListener(ActionListener actionListener) {
        actionListeners.add(actionListener);
    }

    private void startTouch(MotionEvent event) {
        touchStartTime = System.currentTimeMillis();
        touchActive = true;
        touchMove = false;
        touchReferencePoint = new Point((int) event.getX(), (int) event.getY());
    }

    private void stopTouch() {
        touchActive = false;
        touchMove = false;
        touchReferencePoint = null;
        editMode = EDIT_NONE;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void initialize() {

        setFramesPerSecond(FPS);
        enableDirtyCheck(ENABLE_DIRTY_CHECK);

        setHapticFeedbackEnabled(false); // avoid vibration for long-click

        touchCallback = new Runnable() {

            @Override
            public void run() {
                if (!touchMove && null != touchReferencePoint) {
                    // long press
                    select(touchReferencePoint.x, touchReferencePoint.y, true);
                }
                stopTouch();
            }
        };

        setOnTouchListener((v, event) -> {

            int action = event.getAction();
            int x = (int) event.getX();
            int y = (int) event.getY();

            if (action == MotionEvent.ACTION_DOWN) {
                changeSelection(x, y);
                for (ActionListener actionListener : actionListeners) {
                    actionListener.onFocus();
                }
                startTouch(event);
                v.postDelayed(touchCallback, Application.LONG_TOUCH_DELAY_MS);
            } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                v.removeCallbacks(touchCallback);
                if (touchActive) {
                    if (!touchMove) {
                        select(x, y, false);
                        select(EDIT_NONE, x, y);
                    }
                    performClick();
                }
                stopTouch();
            } else if (action == MotionEvent.ACTION_MOVE) {
                changeSelection(x, y);

                if (null != touchReferencePoint && !touchMove) {
                    if (Math.abs(x - touchReferencePoint.x) > Application.LONG_TOUCH_MOVE_LIMIT || Math.abs(y - touchReferencePoint.y) > Application.LONG_TOUCH_MOVE_LIMIT) {
                        v.removeCallbacks(touchCallback);
                        select(editMode, touchReferencePoint.x, touchReferencePoint.y);
                        touchMove = true;
                    }
                }

                if (touchMove) {
                    select(editMode, x, y);
                }
            }

            return false;
        });

        setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });
    }

    private void updateStats() {
        long tm = System.currentTimeMillis();
        if (tm - lastStatsUpdateTime > 5000) {
            lastStatsUpdateTime = tm;
            Timer.Stats stats = this.getStats();
            if (Application.SHOW_FPS) {
                Logger.d(TAG, "avg. fps: " + (long) (stats.getAvgUpdatesPerSecond() + 0.5) + ", avg. render time: " + (long) stats.getAvgExecutionTime() + " µs");
            }
        }
    }

    private void drawConsole(Canvas canvas, Console console) {

        Paint pnt = Resources.instance().pntConsole;
        Paint.FontMetricsInt metrics = pnt.getFontMetricsInt();

        int height = getHeight();
        int lineHeight = metrics.bottom - metrics.top;

        List<String> buffer = console.lockBuffer();

        try {

            int y = height - lineHeight;
            int pos = buffer.size() - 1;
            while (pos >= 0 && y > -lineHeight) {
                String s = buffer.get(pos);
                canvas.drawText(s, 8, y, pnt);
                y -= lineHeight;
                pos--;
            }

        } finally {
            console.unlockBuffer();
        }

    }

    @Override
    protected void loadResources() {
        DisplayMetrics metrics = getResources().getDisplayMetrics();

        layout = new Layout();
        layout.update(getWidth(), getHeight(), metrics.densityDpi);
        Resources.instance().cacheButtons(layout.elementWidth, layout.elementHeight, metrics.densityDpi);
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        if (null != layout) {
            layout.update(width, height);
        }
    }

    @Override
    protected void unloadResources() {
    }

    @Override
    protected void redraw(Canvas canvas) {

        if (Application.DISABLE_RENDERING_ALL) {

            if (null != Resources.instance()) {
                Bitmap bmpBackground = Resources.instance().bmpBackground;
                if (null != bmpBackground) {
                    Rect r = new Rect(0, 0, canvas.getWidth() / 2, canvas.getHeight());
                    canvas.drawBitmap(bmpBackground, null, r, null);
                }
            }

            return;
        }

        SequencerPosition position = null;

        boolean editMode = isInEditMode();
        if (!editMode) {
            Sequencer sequencer = Sequencer.instance();
            if (null != sequencer) {
                SequencerControl state = sequencer.getState();
                position = state.getPosition();
            }
        }

        drawElements(canvas, position);

        if (!editMode && Application.SHOW_FPS) {
            updateStats();
        }
    }

    private void drawElements(Canvas canvas, SequencerPosition position) {

        if (Application.DISABLE_RENDERING_ALL) {
            return;
        }

        canvas.drawColor(backgroundColor);

        //canvas.drawRect(0, 0, layout.panelRect.right + layout.borderPanel, layout.viewHeight, pntBackground);

        for (int row=0; row<layout.rows; row++) {
            if (!Application.DISABLE_RENDERING_TRACKS) {
                drawTrackHeader(canvas, row);
            }
        }

        for (int row=0; row<layout.rows; row++) {
            if (!Application.DISABLE_RENDERING_ELEMENTS) {
                drawTrack(canvas, row, position);
            }
        }
    }

    private void drawTrackHeader(Canvas canvas, int trackNumber) {

        List<Bitmap> trackButtons = Resources.instance().getTrackButtons(layout.panelWidth, layout.elementHeight);
        int bmpIndex = Math.min(trackButtons.size()-1, trackNumber*2);
        if (null != selectedTrack && selectedTrack.getIndex() == trackNumber) {
            bmpIndex++;
        }

        Bitmap bmp = trackButtons.get(bmpIndex);
        int y = layout.getElementY(trackNumber);
        canvas.drawBitmap(bmp, layout.borderLeft, y, null);
    }

    private void drawTrack(Canvas canvas, int trackNumber, SequencerPosition position) {
        for (int col = 0; col < layout.cols; col++) {
            drawElement(canvas, trackNumber, col, position);
        }
    }

    private void drawElement(Canvas canvas, int row, int col, SequencerPosition position) {

        List<Bitmap> buttons = Resources.instance().getButtons(layout.elementWidth, layout.elementHeight);

        SequencerStep element = getElement(row, col);
        if (null == element) {
            return;
        }

        int x = layout.getElementX(col);
        int y = layout.getElementY(row);

        Bitmap bmp;

        if (!element.hasConfig()) {
            int bmpIndex = Math.min(buttons.size()-2, row*2);
            if (element.isActive()) {
                bmpIndex++;
            }
            bmp = buttons.get(bmpIndex);
        } else {
            bmp = Resources.instance().bmpButtonHighlighted;
        }

        canvas.drawBitmap(bmp, x, y, null);

        int currentStep = (null != position) ? (int) position.getStep() : -1;

        if (col == currentStep || (null != element && element == selectedElement)) {
            canvas.drawBitmap(Resources.instance().buttonFrame, x, y, null);
        }

        /*
        if (element.hasConfig() && null != UiResources.instance().bmpButtonLedActive) {
            canvas.drawBitmap(UiResources.instance().bmpButtonLedActive, x, y, null);
        } else if (null != UiResources.instance().bmpButtonLedInactive) {
            canvas.drawBitmap(UiResources.instance().bmpButtonLedInactive, x, y, null);
        }

         */

    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    private SequencerStep getElement(int trackId, int step) {

        if (null == layout) return null;
        if (trackId < 0 || step < 0) return null;

        Sequencer sequencer = Sequencer.instance();
        if (null == sequencer) return null;

        SequencerTrack track = sequencer.getTrack(trackId);
        if (null == track) return null;

        SequencerStep trackElement = track.getElement(step);

        return trackElement;
    }

    private SequencerTrack getTrackAt(int x, int y) {

        Sequencer sequencer = Sequencer.instance();
        if (null == sequencer) return null;

        int trackId = layout.getTrackNumber(x, y);
        if (trackId < 0) return null;

        SequencerTrack track = sequencer.getTrack(trackId);

        return track;
    }

    private SequencerStep getElementAt(int x, int y) {

        if (null == layout) return null;

        Sequencer sequencer = Sequencer.instance();
        if (null == sequencer) return null;

        int trackId = layout.getElementTrackNumber(x, y);
        if (trackId < 0) return null;

        SequencerTrack track = sequencer.getTrack(trackId);
        if (null == track) return null;

        int step = layout.getCol(x, y);
        if (step < 0) return null;
        SequencerStep trackElement = track.getElement(step);

        return trackElement;
    }

    public boolean select(int mode, int x, int y) {

        SequencerStep element = getElementAt(x, y);
        if (null == element) {
            return false;
        }

        long tick = element.getIndex() * Constants.TICKS_PER_STEP;
        double pos = (double) tick;

        if (mode == EDIT_NONE) {
            if (!element.isActive()) {
                element.setActive();
                editMode = EDIT_SET;
            } else {
                element.setInactive();
                editMode = EDIT_CLEAR;
            }
        } else {
            if (!element.isActive()) {
                if (editMode == EDIT_SET) {
                    element.setActive();
                }
            } else {
                if (editMode == EDIT_CLEAR) {
                    element.setInactive();
                }
            }
        }

        setDirty();

        return true;
    }

    public void changeSelection(int x, int y) {
        selectedTrack = getTrackAt(x, y);
        selectedElement = getElementAt(x, y);
    }

    public boolean select(int x, int y, boolean longPress) {

        changeSelection(x, y);

        SequencerTrack track = selectedTrack;
        SequencerStep element = selectedElement;

        SequencerElement selected = null;

        if (null != element && longPress) {
            selected = element;
        } else if (null != track) {
            selected = track;
        }

        for (ActionListener actionListener : actionListeners) {
            actionListener.onSelect(selected);
        }

        if (null == selected) {
            return false;
        }

        setDirty();
        return true;
    }

}
