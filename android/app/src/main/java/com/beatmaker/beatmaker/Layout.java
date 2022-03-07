package com.beatmaker.beatmaker;

import android.graphics.Rect;

import com.beatmaker.app.Application;
import com.beatmaker.config.Constants;
import com.beatmaker.core.sequencer.SequencerMetrics;

class Layout {

    private final SequencerMetrics sequencerMetrics = SequencerMetrics.instance();

    public int viewWidth = 0;
    public int viewHeight = 0;

    public final float panelWidthDpi = 0.6f;
    public int panelWidth = 0;

    public final int borderLeft = 16;
    public final int borderRight = 16;
    public final int borderTop = 16;
    public final int borderBottom = 16;
    public final int borderPanel = 16;

    public float displayDensity = 1.0f;

    public int cols = 0;
    public int rows = 0;

    public final int spacingX1 = 2;
    public final int spacingX2 = 16;
    public final int spacingY = 8;

    public int groupWidth = 0;
    public int stepsPerQuarter = 0;

    public int elementWidth = 0;
    public int elementHeight = 0;

    public Rect panelRect = new Rect();
    public Rect elementsRect = new Rect();

    public void updateWidth(int screenWidth) {

        viewWidth = screenWidth;

        cols = sequencerMetrics.getNumSteps();

        int quarters = sequencerMetrics.getNumQuarters();
        stepsPerQuarter = Constants.STEPS_PER_QUARTER_NOTE;

        panelRect.left = borderLeft;
        panelRect.right = Math.max(panelRect.left, borderLeft + panelWidth);

        elementsRect.left = panelRect.right + borderPanel;
        int xmax = Math.max(elementsRect.left, screenWidth - borderRight);

        int spacingWidth = (quarters - 1) * spacingX2 + quarters * (stepsPerQuarter - 1) * spacingX1;

        elementWidth = ((xmax - elementsRect.left) - spacingWidth) / cols;
        if (elementWidth < 16) elementWidth = 16;

        groupWidth = (stepsPerQuarter - 1) * spacingX1 + stepsPerQuarter * elementWidth;

        int clientWidth = cols * elementWidth + spacingWidth;
        elementsRect.right = elementsRect.left + clientWidth;

    }

    public void updateHeight(int screenHeight, int maxElementHeight){

        viewHeight = screenHeight;

        rows = sequencerMetrics.getNumTracks();

        panelRect.top = borderTop;
        panelRect.bottom = Math.max(panelRect.top, screenHeight - borderBottom);

        elementsRect.top = panelRect.top;

        int ymax = Math.max(elementsRect.top, screenHeight - borderBottom);

        elementHeight = ((ymax-elementsRect.top) - ((rows - 1) * spacingY)) / rows;
        if (elementHeight < 16) elementHeight = 16;

        if (maxElementHeight > 0 && elementHeight > maxElementHeight) {
            elementHeight = maxElementHeight;
        }

        int clientHeight = rows * (elementHeight + spacingY) - (rows > 0 ? spacingY : 0);
        elementsRect.bottom = elementsRect.top + clientHeight;

    }

    public void update(int screenWidth, int screenHeight) {
        if (Application.OVERWRITE_VIEW_METRICS) {
            if (0 != Application.OVERWRITE_VIEW_WIDTH) screenWidth = Application.OVERWRITE_VIEW_WIDTH;
            if (0 != Application.OVERWRITE_VIEW_HEIGHT) screenHeight = Application.OVERWRITE_VIEW_HEIGHT;
        }

        updateWidth(screenWidth);
        updateHeight(screenHeight, elementWidth);
    }

    public void update(int screenWidth, int screenHeight, float density) {
        if (Application.OVERWRITE_VIEW_METRICS) {
            if (0 != Application.OVERWRITE_VIEW_DENSITY) density = (float) Application.OVERWRITE_VIEW_DENSITY;
        }

        if (density != 0.0f) {
            this.displayDensity = density;
            panelWidth = (int) (panelWidthDpi * displayDensity);
        }

        update(screenWidth, screenHeight);
    }

    public int getElementX(int col) {
        int quarter = col / stepsPerQuarter;
        int spacingWidth = quarter * (spacingX2 - spacingX1) + col * spacingX1;
        int x = elementsRect.left + col * elementWidth + spacingWidth;
        return x;
    }

    public int getElementY(int row) {
        int y = elementsRect.top + row * (elementHeight + spacingY);
        return y;
    }

    public Rect getElementRect(int col, int row) {
        int x0 = getElementX(col);
        int y0 = getElementY(row);
        int x1 = x0 + elementWidth;
        int y1 = y0 + elementHeight;
        return new Rect(x0, y0, x1, y1);
    }

    private int getRow(int y) {

        if (y < elementsRect.top || y >= elementsRect.bottom) return -1;

        y -= elementsRect.top;
        int row = y / (elementHeight + spacingY);
        int offset = y - (row * (elementHeight + spacingY));
        if (offset >= elementHeight) return -1; // between buttons

        return row;
    }

    public int getTrackNumber(int x, int y) {
        if (x < panelRect.left || x >= panelRect.right) return -1;
        return getRow(y);
    }

    public int getElementTrackNumber(int x, int y) {
        if (x < elementsRect.left || x >= elementsRect.right) return -1;
        return getRow(y);
    }

    public int getCol(int x, int y) {
        if (x < elementsRect.left || x >= elementsRect.right) return -1;
        if (y < elementsRect.top || y >= elementsRect.bottom) return -1;

        x -= (elementsRect.left);

        int quarter = x / (groupWidth + spacingX2);
        int groupOffset = x - (quarter * (groupWidth + spacingX2));
        if (groupOffset >= groupWidth) {
            return -1; // between measures
        }

        int element = groupOffset / (elementWidth + spacingX1);
        int offset = groupOffset - (element * (elementWidth + spacingX1));
        if (offset >= elementWidth) {
            return -1; // between buttons;
        }

        int col = (quarter * stepsPerQuarter) + element;

        return col;
    }
}
