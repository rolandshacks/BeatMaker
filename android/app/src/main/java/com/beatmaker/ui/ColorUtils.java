package com.beatmaker.ui;

public class ColorUtils {
    public static int multiplyChannel(int v, float factor) {
        v = (int) ((float) v * factor + 0.5);
        if (v < 0) v = 0;
        if (v > 255) v = 255;
        return v;
    }

    public static int multiply(int color, float factor) {

        int A = (color >> 24) & 0xff; // or color >>> 24
        int R = (color >> 16) & 0xff;
        int G = (color >>  8) & 0xff;
        int B = (color      ) & 0xff;

        A = multiplyChannel(A, factor);
        R = multiplyChannel(R, factor);
        G = multiplyChannel(G, factor);
        B = multiplyChannel(B, factor);

        color = (A & 0xff) << 24 | (R & 0xff) << 16 | (G & 0xff) << 8 | (B & 0xff);

        return color;
    }

    public static int getRainbowColor(float offset, float brightness, float saturation) {

        // returns a rainbow
        // (red-yellow-green-cyan-blue)

        if (offset < 0.0f) offset = 0.0f;
        if (offset >= 1.0f) offset = 1.0f;

        float red, green, blue;

        if (offset <= 0.25f) {
            red = 1.0f;
            green = offset * 4.0f;
            blue = 0.0f;
        } else if (offset <= 0.5f) {
            red = 1.0f - (offset-0.25f) * 4.0f;
            green = 1.0f;
            blue = 0.0f;
        } else if (offset <= 0.75f) {
            red = 0.0f;
            green = 1.0f;
            blue = (offset-0.5f) * 4.0f;
        } else {
            red = 0.0f;
            green = 1.0f - (offset-0.75f) * 4.0f;
            blue = 1.0f;
        }

        return toColor(red, green, blue, brightness, saturation);
    }

    public static int getRainbowColorEx(float offset, float brightness, float saturation) {

        // returns a cyclic rainbow
        // (red-yellow-green-cyan-blue-pink-red)

        if (offset < 0.0f) offset = 0.0f;
        if (offset >= 1.0f) offset = 1.0f;

        float red, green, blue;

        float n = 6.0f;
        float seg = 1.0f/6.0f;

        if (offset <= seg*1.0f) {
            red = 1.0f;
            green = offset * n;
            blue = 0.0f;
        } else if (offset <= seg*2.0f) {
            red = 1.0f - (offset-seg) * n;
            green = 1.0f;
            blue = 0.0f;
        } else if (offset <= seg*3.0f) {
            red = 0.0f;
            green = 1.0f;
            blue = (offset-seg*2.0f) * n;
        } else if (offset <= seg*4.0f) {
            red = 0.0f;
            green = 1.0f - (offset-seg*3.0f) * n;
            blue = 1.0f;
        } else if (offset <= seg*5.0f) {
            red = (offset-seg*4.0f) * n;
            green = 0.0f;
            blue = 1.0f;
        } else {
            red = 1.0f;
            green = 0.0f;
            blue = 1-0f-(offset*seg*5.0f)*n;
        }

        return toColor(red, green, blue, brightness, saturation);
    }

    public static int toColor(float red, float green, float blue, float brightness, float saturation) {

        red *= brightness;
        green *= brightness;
        blue *= brightness;

        float avg = (red + green + blue) / 3.0f;

        /*
        float min = Math.min(Math.min(red, green), blue);
        float max = Math.max(Math.min(red, green), blue);
        float delta = max - min;
        */

        float inv_saturation = 1.0f - saturation;

        red = avg * inv_saturation + red * saturation;
        green = avg * inv_saturation + green * saturation;
        blue = avg * inv_saturation + blue * saturation;

        int r = (int) (red * 255.999);
        if (r < 0) r = 0;
        if (r > 255) r = 255;

        int g = (int) (green * 255.999);
        if (g < 0) g = 0;
        if (g > 255) g = 255;

        int b = (int) (blue * 255.999);
        if (b < 0) b = 0;
        if (b > 255) b = 255;

        int rgb = ((r&0xff) << 16) | ((g&0xff) << 8) | (b&0xff);

        return rgb;
    }

    public static int adjustColor(int color, float brightness, float saturation) {
        float R = (float)((color >> 16) & 0xff)/255.0f;
        float G = (float)((color >>  8) & 0xff)/255.0f;
        float B = (float)((color) & 0xff)/255.0f;
        return toColor(R, G, B, brightness, saturation);
    }

}
