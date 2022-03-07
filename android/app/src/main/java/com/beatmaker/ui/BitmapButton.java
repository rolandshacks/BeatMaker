package com.beatmaker.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Checkable;

import androidx.annotation.Nullable;

import com.beatmaker.beatmaker.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BitmapButton extends View implements Checkable {

    public enum ButtonType {COLOR, BLACK, LED};

    private int clientWidth;
    private int clientHeight;

    private String text;
    private String textOn;
    private String textOff;
    private int color;
    private ButtonType buttonType = ButtonType.COLOR;

    private boolean cached = false;

    private boolean checkable = false;
    private boolean checked = false;
    private boolean pressed = false;

    private Bitmap bmpUnchecked;
    private Bitmap bmpChecked;

    private List<OnCheckedChangeListener> listeners = new ArrayList<>();

    public static interface OnCheckedChangeListener {
        void onCheckedChanged(BitmapButton view, boolean isChecked);
    }

    public BitmapButton(Context context) {
        super(context);
        init(null, 0);
    }

    public BitmapButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public BitmapButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    public BitmapButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs, 0);
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener onCheckedChangeListener) {
        listeners.add(onCheckedChangeListener);
    }

    public void removeOnCheckedChangeListener(OnCheckedChangeListener onCheckedChangeListener) {
        listeners.remove(onCheckedChangeListener);
    }

    public void removeAllOnCheckedChangeListeners() {
        listeners.clear();
    }

    private int getDimension(TypedArray attr, int id, int defaultValue) {

        int t = attr.getType(id);

        if (t == TypedValue.TYPE_DIMENSION) {
            return attr.getDimensionPixelSize(id, defaultValue);
        }

        if (t == TypedValue.TYPE_INT_DEC) {
            int value = attr.getInteger(id, defaultValue);
            if (value < 0) value = defaultValue;
        }

        return defaultValue;
    }

    private void init(AttributeSet attrs, int defStyle) {

        final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.BitmapView, defStyle, 0);

        clientWidth = getDimension(a, R.styleable.BitmapView_android_layout_width, 192);
        clientHeight = getDimension(a, R.styleable.BitmapView_android_layout_height, 128);

        checkable = a.getBoolean(R.styleable.BitmapView_checkable, false);
        checked = a.getBoolean(R.styleable.BitmapView_android_checked, false);
        color = a.getColor(R.styleable.BitmapView_android_color, 0xffc0c0c0);
        text = a.getString(R.styleable.BitmapView_android_text);
        textOff = a.getString(R.styleable.BitmapView_android_textOff);
        textOn = a.getString(R.styleable.BitmapView_android_textOn);
        buttonType = ButtonType.values()[a.getInt(R.styleable.BitmapView_type, 0)];

        a.recycle();

        if (isInEditMode()) {
            if (null == text) text = "Hello";
        }

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {}
        });

        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                int action = event.getAction();
                int x = (int) event.getX();
                int y = (int) event.getY();
                boolean inside = (x >= 0 && y >= 0 && x < getWidth() && y < getHeight());

                if (action == MotionEvent.ACTION_DOWN) {
                    pressed = true;
                    invalidate();
                } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                    pressed = false;
                    if (checkable) {
                        if (inside) toggle();
                    } else {
                        if (inside) performClick();
                        invalidate();
                    }
                } else {
                    if (pressed != inside) {
                        pressed = inside;
                        invalidate();
                    }
                }

                return true;
            }
        });

        clearCache();
    }

    private void clearCache() {
        cached = false;
    }

    private void render() {
        if (cached) return;

        String t = text;
        String t2 = text;

        if (checkable) {
            if (null != textOff) t = textOff;
            if (null != textOn) t2 = textOn;
        }

        String overlayText = (null != t) ? t.toUpperCase(Locale.ROOT) : null;
        String overlayText2 = (null != t2) ? t2.toUpperCase(Locale.ROOT) : null;

        int overlayStyle = ButtonRenderer.STYLE_NORMAL;
        if (buttonType == ButtonType.BLACK) overlayStyle = ButtonRenderer.STYLE_BLACK;
        if (buttonType == ButtonType.LED) {
            overlayStyle |= ButtonRenderer.STYLE_TEXT_TOP;
        }

        int overlayTextSize = (buttonType == ButtonType.LED) ? (clientHeight - clientHeight /8) / 3 : clientHeight / 3;

        int color0 = ColorUtils.adjustColor(color, 0.7f, 1.0f);
        int color1 = ColorUtils.adjustColor(color, 0.5f, 1.0f);
        bmpUnchecked = ButtonRenderer.createButton(clientWidth, clientHeight, color0, color1, true);

        if (buttonType == ButtonType.LED) {
            ButtonRenderer.createLedOverlay(bmpUnchecked, false);
        }

        if (null != overlayText) {
            ButtonRenderer.createTextOverlay(bmpUnchecked, overlayText, overlayTextSize, overlayStyle);
        }

        int color2 = color0;
        int color3 = color1;

        if (buttonType != ButtonType.LED) {
            color2 = ColorUtils.adjustColor(color, 1.0f, 1.0f);
            color3 = ColorUtils.adjustColor(color, 0.8f, 1.0f);
        }

        bmpChecked = ButtonRenderer.createButton(clientWidth, clientHeight, color2, color3, true);

        if (buttonType == ButtonType.LED) {
            ButtonRenderer.createLedOverlay(bmpChecked, true);
        }

        if (null != overlayText) {
            ButtonRenderer.createTextOverlay(bmpChecked, overlayText2, overlayTextSize, overlayStyle);
        }

        cached = true;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        clientWidth = w;
        clientHeight = h;

        clientWidth -= getPaddingLeft() + getPaddingRight();
        if (clientWidth < 0) clientWidth = 0;
        clientHeight -= getPaddingTop() + getPaddingBottom();
        if (clientHeight < 0) clientHeight = 0;

        clearCache();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int width = clientWidth + getPaddingLeft() + getPaddingRight();
        int height = clientHeight + getPaddingTop() + getPaddingBottom();

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        render();

        canvas.save();

        Bitmap bmp = bmpUnchecked;

        if (checked || pressed) {
            bmp = bmpChecked;
        }

        int x = getPaddingLeft() + (clientWidth - bmp.getWidth()) / 2;
        int y = getPaddingTop() + (clientHeight - bmp.getHeight()) / 2;

        canvas.drawBitmap(bmp, x, y, null);

        canvas.restore();
    }

    public String getText() {
        return text;
    }

    public String getTextOn() {
        return textOn;
    }

    public String getTextOff() {
        return textOff;
    }

    public void setText(String text) {
        this.text = text;
        clearCache();
        invalidate();
    }

    public void setTextOn(String text) {
        this.textOn = text;
        clearCache();
        invalidate();
    }

    public void setTextOff(String text) {
        this.textOff = text;
        clearCache();
        invalidate();
    }

    public void toggle() {
        if (!checkable) return;
        checked = (!checked);
        invalidate();
        notifyListeners();
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked (boolean checked) {
        if (checked != this.checked) {
            this.checked = checked;
            invalidate();
            notifyListeners();
        }
    }

    public void notifyListeners() {
        boolean checked = this.checked;
        for (OnCheckedChangeListener listener : listeners) {
            listener.onCheckedChanged(this, checked);
        }
    }

}
