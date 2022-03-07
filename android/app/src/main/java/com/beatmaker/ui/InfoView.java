package com.beatmaker.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import com.beatmaker.beatmaker.R;

public class InfoView extends View {

    private static final String TAG = "InfoView";

    private String text;
    private boolean hasBackgroundColor;
    private int backgroundColor;
    private Paint paint;
    private Paint.FontMetricsInt metrics;
    private int fontOffsetY;
    private Point textSize;
    private boolean textChanged;

    public InfoView(Context context) {
        super(context);
    }

    public InfoView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs);
    }

    public InfoView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs);
    }

    public InfoView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialize(context, attrs);
    }

    private void initialize(Context context, @Nullable AttributeSet attrs) {

        TypedArray style = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.InfoView,
                0, 0);

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        text = style.getString(R.styleable.InfoView_android_text);
        textChanged = true;

        hasBackgroundColor = style.hasValue(R.styleable.InfoView_android_backgroundTint);
        if (hasBackgroundColor) {
            backgroundColor = 0xff000000 | style.getColor(R.styleable.InfoView_android_backgroundTint, 0x0);
        }

        Typeface f = null;
        if(!isInEditMode()) {
            int fontResourceId = style.getResourceId(R.styleable.InfoView_android_fontFamily, 0);
            if (0 != fontResourceId) {
                f = ResourcesCompat.getFont(context, fontResourceId);
                if (null != f) {
                    paint.setTypeface(f);
                }
            }
        } else {
            if (null == text) text = "Hello, world!";
        }

        float fontSize = style.getDimension(R.styleable.InfoView_android_textSize, 18.0f);
        paint.setTextSize(fontSize);

        if (style.hasValue(R.styleable.InfoView_android_textColor)) {
            paint.setColor(style.getColor(R.styleable.InfoView_android_textColor, 0xff000000));
        }

        paint.setTextAlign(Paint.Align.CENTER);
    }
    public void setText(String text) {
        this.text = text;
        this.textChanged = true;
        if(Looper.myLooper() == Looper.getMainLooper()) {
            invalidate();
        } else {
            postInvalidate();
        }
    }

    public String getText() {
        return text;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    private Paint.FontMetricsInt getTextMetrics() {

        if (null == metrics) {
            if (null != paint) {
                metrics = paint.getFontMetricsInt();
                fontOffsetY = metrics.top;
            }
        }

        return metrics;
    }

    private Point getTextSize() {

        Paint.FontMetricsInt metrics = getTextMetrics();

        if (null == textSize) {
            textSize = new Point();
        }

        if (textChanged) {
            textChanged = false;
            if (null != paint && null != metrics) {
                textSize.set(
                        ((null != text) ? (int) paint.measureText(text) : 0),
                        metrics.bottom - metrics.top
                );
            } else {
                textSize.set(256, 128);
            }
        }

        return textSize;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        Point sz = getTextSize();

        int w = sz.x;
        w += getPaddingLeft() + getPaddingRight();

        int h = sz.y;
        h += getPaddingTop() + getPaddingBottom();

        setMeasuredDimension(w, h);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Point sz = getTextSize();

        canvas.save();

        int w = getWidth();
        int h = getHeight();

        int clientWidth = Math.max(0, w - (getPaddingLeft() + getPaddingRight()));
        int clientHeight = Math.max(0, h - (getPaddingTop() + getPaddingBottom()));

        int x = clientWidth / 2;
        x += getPaddingLeft();

        int y = (clientHeight - sz.y) / 2 - fontOffsetY;
        y += getPaddingTop();

        if (hasBackgroundColor) {
            canvas.drawColor(backgroundColor);
        }

        if (null != text) {
            canvas.drawText(text, x, y, paint);
        }

        canvas.restore();

    }

}
