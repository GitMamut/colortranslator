package com.mintfrost.colortranslator3;

/**
 * Created by Przemek on 14.12.2017.
 */

public class ClosestColor {
    private int originalColor;
    private ColorDesc matchingColor;
    private double distance;

    public ClosestColor(int originalColor, ColorDesc matchingColor, double distance) {
        this.originalColor = originalColor;
        this.matchingColor = matchingColor;
        this.distance = distance;
    }

    public int getOriginalColor() {
        return originalColor;
    }

    public ColorDesc getMatchingColor() {
        return matchingColor;
    }

    public double getDistance() {
        return distance;
    }
}
