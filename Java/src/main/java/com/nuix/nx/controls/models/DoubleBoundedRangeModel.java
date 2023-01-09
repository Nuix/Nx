package com.nuix.nx.controls.models;

import javax.swing.*;

public class DoubleBoundedRangeModel extends DefaultBoundedRangeModel {
    private double min, max, extent, value;
    private int digitsToMaintain;

    public DoubleBoundedRangeModel(double value, double extent, double min, double max, int digitsToMaintain) {
        this.value = value;
        this.extent = extent;
        this.min = min;
        this.max = max;
        this.digitsToMaintain = digitsToMaintain;
    }

    @Override
    public int getExtent() {
        return convertToInt(extent, digitsToMaintain);
    }

    @Override
    public int getMaximum() {
        return convertToInt(max, digitsToMaintain);
    }

    @Override
    public int getMinimum() {
        return convertToInt(min, digitsToMaintain);
    }

    @Override
    public int getValue() {
        return convertToInt(value, digitsToMaintain);
    }

/*
    @Override
    public void setExtent(int extent) {
        this.extent = convertToDouble(extent, digitsToMaintain);
    }

    @Override
    public void setMaximum(int max) {
        this.max = convertToDouble(max, digitsToMaintain);
    }

    @Override
    public void setMinimum(int min) {
        this.min = convertToDouble(min, digitsToMaintain);
    }

    @Override
    public void setValue(int value) {
        this.value = convertToDouble(value, digitsToMaintain);
    }
*/

    @Override
    public void setRangeProperties(int value, int extent, int min, int max, boolean adjusting) {
        this.value = convertToDouble(value, digitsToMaintain);
        this.extent = convertToDouble(extent, digitsToMaintain);
        this.min = convertToDouble(min, digitsToMaintain);
        this.max = convertToDouble(max, digitsToMaintain);
    }

    public double getExtentAsDouble() {
        return extent;
    }

    public double getMaximumAsDouble() {
        return max;
    }

    public double getMinimumAsDouble() {
        return min;
    }

    public double getValueAsDouble() {
        return value;
    }

    private static int convertToInt(double toConvert, int digitsToMaintain) {
        double multiplier = Math.pow(10.0, digitsToMaintain);
        double results = toConvert * multiplier;
        return (int)Math.round(results);
    }

    private static double convertToDouble(int toConvert, int digitsToMaintain) {
        double divider = Math.pow(10.0, digitsToMaintain);
        return ((double)toConvert)/divider;
    }
}
