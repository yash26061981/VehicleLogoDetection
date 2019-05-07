package com.avanseus.avr.model;

/**
 * Created by hemanth on 19/4/16.
 */
public class CombinedImage {
    private int[][] image;
    private int[][] croppedImage;
    private int xmin,xmax,ymin,ymax;
    private int xCentroid, yCentroid;

    public int[][] getImage() {
        return image;
    }

    public void setImage(int[][] image) {
        this.image = image;
    }

    public int[][] getCroppedImage() {
        return croppedImage;
    }

    public void setCroppedImage(int[][] croppedImage) {
        this.croppedImage = croppedImage;
    }

    public int getXmin() {
        return xmin;
    }

    public void setXmin(int xmin) {
        this.xmin = xmin;
    }

    public int getXmax() {
        return xmax;
    }

    public void setXmax(int xmax) {
        this.xmax = xmax;
    }

    public int getYmin() {
        return ymin;
    }

    public void setYmin(int ymin) {
        this.ymin = ymin;
    }

    public int getYmax() {
        return ymax;
    }

    public void setYmax(int ymax) {
        this.ymax = ymax;
    }
    public void setxCentroid(int centroid){this.xCentroid = centroid;}
    public int getxCentroid(){return xCentroid;}
    public void setyCentroid(int centroid){this.yCentroid = centroid;}
    public int getyCentroid(){return yCentroid;}
}
