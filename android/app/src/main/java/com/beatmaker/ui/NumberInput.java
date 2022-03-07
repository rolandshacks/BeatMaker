package com.beatmaker.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import androidx.core.content.ContextCompat;

import com.beatmaker.beatmaker.R;
import com.beatmaker.beatmaker.databinding.NumberInputBinding;

import java.util.ArrayList;
import java.util.List;

public class NumberInput extends LinearLayout {

    private List<OnValueChangeListener> listeners = new ArrayList<>();
    private List<ValueNameProvider> nameProviders = new ArrayList<>();

    private NumberInputBinding ui;
    private int value;
    private int minValue;
    private int maxValue;

    public interface OnValueChangeListener {
        /**
         * Called upon a change of the current value.
         *
         * @param widget The NumberInput associated with this listener.
         * @param oldVal The previous value.
         * @param newVal The new value.
         */
        void onValueChange(NumberInput widget, int oldVal, int newVal);
    }

    public interface ValueNameProvider {
        String provideName(NumberInput widget, int val);
    }

    public NumberInput(Context context) {
        super(context);
        init(context, null, 0);
    }

    public NumberInput(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public NumberInput(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {

        LayoutInflater layoutInflater =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        ui = NumberInputBinding.inflate(layoutInflater, this, true);

        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.NumberInput, defStyle, 0);

        int defaultValue = 0;
        int defaultMinValue = 0;
        int defaultMaxValue = 100;

        if (isInEditMode()){
            defaultValue = 1234;
            defaultMaxValue = 9999;
        }

        minValue = a.getInteger(R.styleable.NumberInput_minValue, defaultMinValue);
        maxValue = a.getInteger(R.styleable.NumberInput_maxValue, defaultMaxValue);
        if (maxValue < minValue) maxValue = minValue;

        value = a.getInteger(R.styleable.NumberInput_value, defaultValue);
        if (value < minValue) value = minValue;
        if (value > maxValue) value = maxValue;

        int textColor;
        int buttonTextColor;
        int buttonBackgroundColor;

        int default_textColor = ContextCompat.getColor(context, R.color.number_input_text);
        int default_backgroundColor = ContextCompat.getColor(context, R.color.number_input_background);

        textColor = a.getColor(R.styleable.NumberInput_android_textColor, default_textColor);
        buttonTextColor = a.getColor(R.styleable.NumberInput_buttonTextColor, default_textColor);

        Drawable buttonBackground = a.getDrawable(R.styleable.NumberInput_buttonBackground);
        buttonBackgroundColor = a.getColor(R.styleable.NumberInput_buttonBackgroundColor, default_backgroundColor);

        ui.textView.setTextColor(textColor);
        ui.textView.setText(getValueString());

        ui.buttonMinus.setTextColor(buttonTextColor);
        if (null != buttonBackground) {
            ui.buttonMinus.setBackground(buttonBackground);
        } else {
            ui.buttonMinus.setBackgroundColor(buttonBackgroundColor);
        }

        ui.buttonMinus.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                decValue();
            }
        });

        ui.buttonPlus.setTextColor(buttonTextColor);
        if (null != buttonBackground) {
            ui.buttonPlus.setBackground(buttonBackground);
        } else {
            ui.buttonPlus.setBackgroundColor(buttonBackgroundColor);
        }

        ui.buttonPlus.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                incValue();
            }
        });

        a.recycle();
    }

    private void decValue() {
        if (value > minValue) {
            setValue(value - 1);
        }
    }

    private void incValue() {
        if (value < maxValue) {
            setValue(value + 1);
        }
    }

    private String getValueString() {
        return queryValueNameProviders(String.valueOf(value));
    }

    public void setValue(int value) {
        setValue(value, true);
    }

    public void setValue(int value, boolean notification) {
        int v = getValidatedValue(value);

        int oldValue = this.value;
        this.value = v;

        if (null != ui) {
            ui.textView.setText(getValueString());
        }

        if (notification) {
            if (v != oldValue) {
                triggerValueChange(oldValue, v);
            }
        }
    }

    public int getValue() {
        return value;
    }

    public void setMinValue(int minValue) {
        this.minValue = minValue;
        if (value < minValue) {
            setValue(minValue);
        }
    }

    public int getMinValue() {
        return minValue;
    }

    public void setMaxValue() {
        this.maxValue = maxValue;
        if (value > maxValue) {
            setValue(maxValue);
        }
    }

    public int getMaxValue() {
        return value;
    }

    private int getValidatedValue(int v) {
        if (v < minValue) v = minValue;
        if (v > maxValue) v = maxValue;
        return v;
    }

    public void setOnValueChangedListener(OnValueChangeListener valueChangeListener) {
        listeners.add(valueChangeListener);
    }

    private void triggerValueChange(int oldVal, int newVal) {
        for (OnValueChangeListener listener : listeners) {
            listener.onValueChange(this, oldVal, newVal);
        }
    }

    public void setValueNameProvider(ValueNameProvider valueNameProvider) {
        nameProviders.add(valueNameProvider);
    }

    public String queryValueNameProviders() {
        return queryValueNameProviders(null);
    }

    public String queryValueNameProviders(String defaultValue) {
        for (ValueNameProvider nameProvider : nameProviders) {
            String result = nameProvider.provideName(this, value);
            if (null != result) return result;
        }

        return defaultValue;
    }

}
