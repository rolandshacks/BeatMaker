package com.beatmaker.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;

import java.util.Locale;

public class ButtonRenderer {

    public static final int STYLE_NORMAL = 0x0;
    public static final int STYLE_BLACK_THIN = 0x1;
    public static final int STYLE_BLACK = 0x2;
    public static final int STYLE_HIGHLIGHT = 0x4;
    public static final int STYLE_TEXT_TOP = 0x8;

    private Context context;

    public static Bitmap createButton(int width, int height, int color0, int color1, boolean gradient) {

        assert (width > 0 && height > 0);

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);

        Paint p = new Paint();
        p.setAntiAlias(true);
        if (false == gradient) {
            p.setColor(0xff000000 | color0);
        } else {
            p.setShader(new RadialGradient((width / 2), (height / 2), (width / 2), 0xff000000 | color0, 0xff000000 | color1, Shader.TileMode.CLAMP));
        }

        canvas.drawRoundRect(0, 0, width, height, 8.0f, 8.0f, p);

        return bitmap;
    }

    public static void createTextOverlay(Bitmap backgroundBitmap, String text, int textSize, int style) {
        if (null == backgroundBitmap) return;

        int width = backgroundBitmap.getWidth();
        int height = backgroundBitmap.getHeight();

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);

        String overlayText = text.toUpperCase(Locale.ROOT);

        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

        p.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        p.setTextSize(textSize);
        p.setStrokeWidth(1.0f);
        p.setColor(0xff000000);

        if ((style&STYLE_BLACK)!=0 || (style&STYLE_BLACK_THIN)!=0) {
            int paddingX = 4;
            int paddingY = paddingX;
            if ((style&STYLE_BLACK_THIN)!=0) {
                paddingY=height/3;
                canvas.drawRect(paddingX, paddingY, width-paddingX, height-paddingY, p);
            } else {
                RectF r = new RectF(paddingX, paddingY, width-paddingX, height-paddingY);
                canvas.drawRoundRect(r, 6.0f, 6.0f, p);
            }
            p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            p.setColor(0xffffffff);
        }

        Rect textBounds = new Rect();
        p.getTextBounds(overlayText, 0, overlayText.length(), textBounds);

        int x = width/2;
        int y = 0;

        if ((style&STYLE_TEXT_TOP)!=0) {
            y = 6 + textBounds.height();
        } else {
            y = height/2;
        }

        canvas.drawText(overlayText, x - textBounds.centerX(), y - textBounds.centerY(), p);

        Canvas backgroundCanvas = new Canvas(backgroundBitmap);
        backgroundCanvas.drawBitmap(bitmap, 0, 0, null);
    }

    public static void createLedOverlay(Bitmap backgroundBitmap, boolean active) {
        Bitmap overlay = createLedButton(backgroundBitmap.getWidth(), backgroundBitmap.getHeight(), active);
        Canvas canvas = new Canvas(backgroundBitmap);
        canvas.drawBitmap(overlay, 0, 0, null);
    }

    public static Bitmap createLedButton(int width, int height, boolean active) {

        assert (width > 0 && height > 0);

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);

        int sz = (width + height) / 2;

        int barMarginX = sz / 8;
        int barMarginY = height / 16;
        if (barMarginY < 6) barMarginY = 6;
        int barHeight = height / 16;
        if (barHeight < 8) barHeight = 8;

        RectF rectBar = new RectF(barMarginX, height - barMarginY - barHeight, width-barMarginX, height - barMarginY);

        int barPaddingX = 2;
        int barPaddingY = 2;

        RectF rectInset = new RectF(rectBar.left + barPaddingX, rectBar.top + barPaddingY, rectBar.right - barPaddingX, rectBar.bottom - barPaddingY);

        Paint p2 = new Paint();
        p2.setAntiAlias(true);
        p2.setColor(0xff000000);
        canvas.drawRoundRect(rectBar, 3.0f, 3.0f, p2);

        Paint p3 = new Paint();
        p3.setAntiAlias(true);

        int lightColor = active ? 0xffffffff : 0x20ffffff;

        p3.setColor(lightColor);
        p3.setShadowLayer(10, 0, 0, 0xffffffff);

        canvas.drawRoundRect(rectInset, 3.0f, 3.0f, p3);

        return bitmap;

    }

    public static Bitmap createButtonFrame(int width, int height) {
        assert (width > 0 && height > 0);

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);

        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setColor(0x60ffffff);
        p.setStrokeWidth(2.0f);
        p.setStyle(Paint.Style.STROKE);

        canvas.drawRoundRect(1, 1, width - 1, height - 1, 8.0f, 8.0f, p);

        return bitmap;
    }
}
