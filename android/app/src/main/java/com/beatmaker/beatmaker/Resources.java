package com.beatmaker.beatmaker;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;

import com.beatmaker.config.Constants;
import com.beatmaker.ui.ButtonRenderer;
import com.beatmaker.ui.ColorUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

class Resources {

    private static Resources instance_;

    private Context context;

    private final ArrayList<Bitmap> buttons = new ArrayList<>();
    private int buttonWidth;
    private int buttonHeight;

    private final ArrayList<Bitmap> trackButtons = new ArrayList<>();
    private int trackButtonWidth;
    private int trackButtonHeight;

    public Typeface font;
    public Paint pntConsole;
    public Bitmap buttonFrame;
    public float fontSize;
    public Bitmap bmpBackground;

    public Bitmap bmpButtonLedActive;
    public Bitmap bmpButtonLedInactive;
    public Bitmap bmpButtonHighlighted;

    public float displayDensity = 1.0f;

    public static Resources instance() {
        return instance_;
    }

    public Resources(Context context) {
        assert (context != null);
        this.context = context;

        if (null == instance_) {
            instance_ = this;
        }

        initialize();
    }

    private void initialize() {

        font = Typeface.createFromAsset(context.getAssets(), "fonts/ubuntu_mono.ttf");
        fontSize = context.getResources().getDimensionPixelSize(R.dimen.consoleFontSize);

        {
            pntConsole = new Paint();
            pntConsole.setColor(Color.WHITE);
            pntConsole.setTypeface(font);
            pntConsole.setTextSize(fontSize);
        }

        bmpBackground = createBackgroundImage();

    }

    public Typeface getFont() {
        return font;
    }

    public float getFontSize() {
        return fontSize;
    }

    public void cacheButtons(int width, int height, float displayDensity) {

        if (0.0f != displayDensity) {
            this.displayDensity = displayDensity;
        }

        cacheElementButtons(width, height);
        cacheTrackButtons(width, height);

        if (null == bmpButtonLedActive || null == bmpButtonLedInactive || width != buttonWidth || height != buttonHeight) {
            bmpButtonLedActive = ButtonRenderer.createLedButton(width, height, true);
            bmpButtonLedInactive = ButtonRenderer.createLedButton(width, height, false);
        }

    }

    private void cacheElementButtons(int width, int height) {
        if (buttons.isEmpty() || width != buttonWidth || height != buttonHeight) {
            createButtons(width, height);
        }
    }

    private void cacheTrackButtons(int width, int height) {
        if (trackButtons.isEmpty() || width != trackButtonWidth || height != trackButtonHeight) {
            createTrackButtons(width, height);
        }
    }

    private Bitmap loadBitmap(String path) {

        AssetManager assets = context.getAssets();

        Bitmap bmp;
        try {
            InputStream bitmapStream = assets.open(path);
            bmp = BitmapFactory.decodeStream(bitmapStream);
        } catch (IOException e) {
            return null;
        }

        return bmp;
    }

    public void unload() {
        buttons.clear();
    }

    public void update(int elementWidth, int elementHeight) {
        cacheElementButtons(elementWidth, elementHeight);
    }

    public List<Bitmap> getButtons() {
        return buttons;
    }

    public List<Bitmap> getTrackButtons(int width, int height) {
        cacheTrackButtons(width, height);
        return trackButtons;
    }

    public List<Bitmap> getButtons(int width, int height) {
        cacheElementButtons(width, height);
        return buttons;
    }

    private Bitmap createBackgroundImage() {
        Bitmap bitmap = Bitmap.createBitmap(1000, 16, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);

        Paint p = new Paint();
        p.setAntiAlias(false);
        p.setStrokeWidth(1.0f);
        p.setStyle(Paint.Style.STROKE);

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        for (int x=0; x<w; x++) {
            float a = (float) x / (float) w;
            int rgb = ColorUtils.getRainbowColor(a, 1.0f, 1.0f);
            p.setColor(rgb);
            canvas.drawLine(x, 0, x, h, p);
        }

        return bitmap;
    }

    private int getTrackColor(int track, float brightness, float saturation) {
        float a = ((float) track) / (float) (Constants.NUM_TRACKS-1);
        int rgb = ColorUtils.getRainbowColor(a, brightness, saturation);
        return rgb;
    }

    private void createButtons(int width, int height) {

        buttonWidth = width;
        buttonHeight = height;
        buttons.clear();

        for (int i=0; i<Constants.NUM_TRACKS; i++) {

            float saturation = 0.6f;
            int baseColor = getTrackColor(i, 0.5f, saturation);
            int highlightColor1 = getTrackColor(i, 1.0f, saturation);
            int highlightColor2 = getTrackColor(i, 0.8f, saturation);

            Bitmap bmpNormal = ButtonRenderer.createButton(width, height, baseColor, baseColor, false);
            buttons.add(bmpNormal);

            Bitmap bmpHighlight = ButtonRenderer.createButton(width, height, highlightColor1, highlightColor2, true);
            buttons.add(bmpHighlight);
        }

        buttonFrame = ButtonRenderer.createButtonFrame(width, height);

        bmpButtonHighlighted = ButtonRenderer.createButton(width, height, 0xffb0a8b0, 0xff807880, true);
    }

    private void createTrackButtons(int width, int height) {

        trackButtonWidth = width;
        trackButtonHeight = height;
        trackButtons.clear();

        int trackButtonFontSize = height / 5;

        for (int i=0; i<Constants.NUM_TRACKS; i++) {
            Bitmap bmp = ButtonRenderer.createButton(width, height, 0xff707070, 0xff505050, true);
            ButtonRenderer.createTextOverlay(bmp, "track " + (i+1), trackButtonFontSize, ButtonRenderer.STYLE_NORMAL);
            trackButtons.add(bmp);

            Bitmap bmp2 = ButtonRenderer.createButton(width, height, 0xffa8a8a8, 0xff808080, true);
            ButtonRenderer.createTextOverlay(bmp2, "track " + (i+1), trackButtonFontSize, ButtonRenderer.STYLE_NORMAL);
            trackButtons.add(bmp2);
        }

    }

    // Background: 17181A  Medium: 1A1C1C   Lighter: 232424
    // Backgrounds (brighter): 8EA2B1 7D909C 6A7983 556167 40474C

    // Bright Text: 9DAABE  Dark Text: 1C2023
    // Pink 4E1A44, light: DB23B3 A61A87
    // Brown: 42300B, light: DB9E23 A6781A
    // Cyan: 0A3F40, light: 22D1D4 1AA0A3
    // Green: 19411D, light: 22D42B 1AA221
    // Purple: 392A55

    //public static final int COLOR_BACKGROUND_DARK = 0x17181A;
    //public static final int COLOR_TEXT = 0x879CAA;
    /*
    public static final int COLOR_PINK = 0x4E1A44;
    public static final int COLOR_BROWN = 0x42300B;
    public static final int COLOR_CYAN = 0x0A3F40;
    public static final int COLOR_GREEN = 0x19411D;
    public static final int COLOR_PURPLE = 0x392A55;
     */

    //public static final int COLOR_BUTTON_TEXT = 0xCCCCCC;
    //public static final int COLOR_BUTTON_BACKGROUND = 0x4A4B4C;
}
