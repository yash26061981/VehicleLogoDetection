package com.avanseus.avr.model;

/**
 * Created by hemanth on 12/4/16.
 */
public class WindowData {
    private int minX;
    private int minY;
    private int maxX;
    private int maxY;
    boolean result = false;
    private double edgeDensity;
    private int width;
    private int height;
    private double averageIntensity;


    public int getMinX() {
        return minX;
    }

    public void setMinX(int minX) {
        this.minX = minX;
    }

    public int getMinY() {
        return minY;
    }

    public void setMinY(int minY) {
        this.minY = minY;
    }

    public int getMaxX() {
        return maxX;
    }

    public void setMaxX(int maxX) {
        this.maxX = maxX;
    }

    public int getMaxY() {
        return maxY;
    }

    public void setMaxY(int maxY) {
        this.maxY = maxY;
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public double getEdgeDensity() {
        return edgeDensity;
    }

    public void setEdgeDensity(double edgeDensity) {
        this.edgeDensity = edgeDensity;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public double getAverageIntensity() {
        return averageIntensity;
    }

    public void setAverageIntensity(double averageIntensity) {
        this.averageIntensity = averageIntensity;
    }

}
