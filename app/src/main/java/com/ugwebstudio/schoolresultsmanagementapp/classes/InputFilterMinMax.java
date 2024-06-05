package com.ugwebstudio.schoolresultsmanagementapp.classes;

import android.text.InputFilter;
import android.text.Spanned;

public class InputFilterMinMax implements InputFilter {
    private double min, max;

    public InputFilterMinMax(double min, double max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        StringBuilder inputStringBuilder = new StringBuilder(dest);
        inputStringBuilder.replace(dstart, dend, source.toString());
        try {
            double input = Double.parseDouble(inputStringBuilder.toString());
            if (isInRange(min, max, input)) {
                return null;
            }
        } catch (NumberFormatException nfe) {
            // If input is not a valid number, reject it
        }
        return "";
    }

    private boolean isInRange(double a, double b, double c) {
        return b > a ? c >= a && c <= b : c >= b && c <= a;
    }
}
