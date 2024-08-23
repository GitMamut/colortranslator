package com.mintfrost.colortranslator2;

import android.graphics.Color;
import android.util.Log;

import java.util.Arrays;

class ColorDesc {
    private static final String LOG_TAG = ColorDesc.class.getSimpleName();

    private int[] rgb;
    private String hex;

    private String simpleDescription;

    private String richDescription;
    public ColorDesc(String hex, String simpleDescription, String richDescription) {
        this.hex = hex;
        int parsedColor = Color.parseColor(hex);
        rgb = new int[]{Color.red(parsedColor), Color.green(parsedColor), Color.blue(parsedColor)};
        this.simpleDescription = simpleDescription;
        this.richDescription = richDescription;
    }

    public int[] getRgb() {
        return rgb;
    }

    public String getHex() {
        return hex;
    }

    public String getSimpleDescription() {
        return simpleDescription;
    }

    public String getRichDescription() {
        return richDescription;
    }

    @Override
    public String toString() {
        return "ColorDesc{" +
                "rgb=" + Arrays.toString(rgb) +
                ", simpleDescription='" + simpleDescription + '\'' +
                ", richDescription='" + richDescription + '\'' +
                '}';
    }

    double getDistance(int capturedColor) {
        double sqrt = Math.sqrt(Math.pow(Color.red(capturedColor) - getRgb()[0], 2)
                + Math.pow(Color.green(capturedColor) - getRgb()[1], 2)
                + Math.pow(Color.blue(capturedColor) - getRgb()[2], 2));
        Log.d(LOG_TAG, "DIST: " + sqrt + "\tComparing " + this + " " + Color.red(capturedColor) + ", " + Color.green(capturedColor) + ", " + Color.blue(capturedColor) + ", ");
        return sqrt;
    }
}
