package com.avanseus.avr.detection;

import com.avanseus.avr.driver.Driver;
import com.avanseus.avr.model.AVRConstants;
import com.avanseus.avr.model.WindowData;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hemanth on 11/4/16.
 */
public class Localization {
    private BufferedImage histogramEqualizedImage;
    private BufferedImage sobelVerticalEdgeImage;
    private double edgeThreshold;

    public Localization(BufferedImage h, BufferedImage s){
        this.histogramEqualizedImage = h;
        this.sobelVerticalEdgeImage = s;
        /*if ((histogramEqualizedImage.getHeight() > 600)&&(histogramEqualizedImage.getWidth()>800)){
            edgeThreshold = AVRConstants.edgeThresholdHD;
        }else{
            edgeThreshold = AVRConstants.edgeThreshold;
        }*/
    }

    public List<WindowData> localizeLP(double threshold) {
        edgeThreshold = threshold;
        int[][] integralImage = computeIntegratedImage();
        int rows = sobelVerticalEdgeImage.getWidth();
        int cols = sobelVerticalEdgeImage.getHeight();

        List<Integer> winHRanges = new ArrayList<>();
        for(int i= AVRConstants.MIN_WINDOW_HEIGHT; i<= AVRConstants.MAX_WINDOW_HEIGHT; i+=((int)(AVRConstants.sizeStep*AVRConstants.MIN_WINDOW_HEIGHT)/100)) {
            winHRanges.add(i);
        }

        List<Integer> winWRanges = new ArrayList<>();
        for(Integer winHRange : winHRanges) {
            winWRanges.add(winHRange * AVRConstants.minAspectRatio);
        }

        List<WindowData> possibleCandidates = new ArrayList<>();
        for(int index=0;index<winHRanges.size();index++) {
            int winH = winHRanges.get(index);
            int winW = winWRanges.get(index);
            List<Integer> stepR = new ArrayList<>();
            for(int i=1;i <= cols-winH; i+=((AVRConstants.transitionStep*winH)/100)) {
                stepR.add(i);
            }

            List<Integer> stepC = new ArrayList<>();
            for(int i=1;i <= rows-winW; i+=((AVRConstants.transitionStep*winW)/100)) {
                stepC.add(i);
            }

            for(int r:stepR) {
                for(int c:stepC) {
                    WindowData windowData = new WindowData();
                    windowData.setMinX(r);
                    windowData.setMinY(c);
                    windowData.setMaxX(r + winH - 1);
                    windowData.setMaxY(c + winW - 1);
                    windowData.setEdgeDensity(getDensity(windowData, integralImage));
                    windowData.setAverageIntensity(getAverageIntensityOfWindow(windowData));
                    if(windowData.getEdgeDensity() >= edgeThreshold && windowData.getAverageIntensity() > AVRConstants.averageIntensity) {
                        windowData.setResult(true);
                        windowData.setHeight(windowData.getMaxX() - windowData.getMinX() + 1);
                        windowData.setWidth(windowData.getMaxY() - windowData.getMinY() + 1);
                        possibleCandidates.add(windowData);
                    }
                }
            }
        }
        return possibleCandidates;
    }

    public List<WindowData> localizeLogo() {
        int[][] integralImage = computeIntegratedImage();
        int rows = sobelVerticalEdgeImage.getWidth();
        int cols = sobelVerticalEdgeImage.getHeight();

        List<Integer> winHRanges = new ArrayList<>();
        for(int i= AVRConstants.MIN_WINDOW_HEIGHT_LOGO; i<= AVRConstants.MAX_WINDOW_HEIGHT_LOGO; i+=((int)(AVRConstants.sizeStepLogo*AVRConstants.MIN_WINDOW_HEIGHT_LOGO)/100)) {
            winHRanges.add(i);
        }

        List<Integer> winWRanges = new ArrayList<>();
        for(Integer winHRange : winHRanges) {
            winWRanges.add(winHRange * AVRConstants.minAspectRatioLogo);
        }

        List<WindowData> possibleCandidates = new ArrayList<>();
        for(int index=0;index<winHRanges.size();index++) {
            int winH = winHRanges.get(index);
            int winW = winWRanges.get(index);
            List<Integer> stepR = new ArrayList<>();
            for(int i=1;i <= cols-winH; i+=((AVRConstants.transitionStepLogo*winH)/100)) {
                stepR.add(i);
            }

            List<Integer> stepC = new ArrayList<>();
            for(int i=1;i <= rows-winW; i+=((AVRConstants.transitionStepLogo*winW)/100)) {
                stepC.add(i);
            }

            for(int r:stepR) {
                for(int c:stepC) {
                    WindowData windowData = new WindowData();
                    windowData.setMinX(r);
                    windowData.setMinY(c);
                    windowData.setMaxX(r + winH - 1);
                    windowData.setMaxY(c + winW - 1);
                    windowData.setEdgeDensity(getDensity(windowData, integralImage));
                    windowData.setAverageIntensity(getAverageIntensityOfWindow(windowData));
                    if(windowData.getEdgeDensity() >= AVRConstants.edgeThresholdLogo &&
                            windowData.getAverageIntensity() > AVRConstants.averageIntensityLogo) {
                        windowData.setResult(true);
                        windowData.setHeight(windowData.getMaxX() - windowData.getMinX() + 1);
                        windowData.setWidth(windowData.getMaxY() - windowData.getMinY() + 1);
                        possibleCandidates.add(windowData);
                    }
                }
            }
        }
        return possibleCandidates;
    }

    private double getAverageIntensityOfWindow(WindowData windowData) {
        Raster histogramEqualizedImageRaster = histogramEqualizedImage.getRaster();
        int xmin = windowData.getMinX();
        int xmax = windowData.getMaxX();
        int ymin = windowData.getMinY();
        int ymax = windowData.getMaxY();
        double intensity = 0.0f;
        double totalArea = (xmax - xmin + 1) * (ymax - ymin + 1);
        for(int i=xmin;i<=xmax;i++) {
            for(int j=ymin;j<=ymax;j++) {
                intensity = intensity + (double)(histogramEqualizedImageRaster.getSample(j-1,i-1,0)/totalArea);
            }
        }
        return intensity;
    }

    private double getDensity(WindowData windowData, int[][] integralImage) {
        int edge = integralImage[windowData.getMaxY()-1][windowData.getMaxX()-1] -
                    integralImage[windowData.getMinY()-1][windowData.getMaxX()-1] -
                integralImage[windowData.getMaxY()-1][windowData.getMinX()-1] +
                integralImage[windowData.getMinY()-1][windowData.getMinX()-1];
        int totalArea = (windowData.getMaxX() - windowData.getMinX() + 1) *
                (windowData.getMaxY() - windowData.getMinY() + 1);
        return (double)edge/totalArea;
    }

    public int[][] computeIntegratedImage() {
        Raster sobelVerticalRaster = sobelVerticalEdgeImage.getRaster();
        int rows = sobelVerticalRaster.getWidth();
        int cols = sobelVerticalRaster.getHeight();
        int integralImage[][] = new int[rows][cols];
        int rsum[][] = new int[rows][cols];

        for(int i=0; i<rows; i++) {
            for(int j=0; j<cols; j++) {
                int toAdd = 0;

                if(sobelVerticalRaster.getSample(i,j,0) == 1)
                    toAdd = 1;

                if(j == 0) {
                    rsum[i][j] = rsum[i][j] + toAdd;
                } else {
                    rsum[i][j] = rsum[i][j-1] + toAdd;
                }

                if(i == 0) {
                    integralImage[i][j] = integralImage[i][j] + rsum[i][j];
                } else {
                    integralImage[i][j] = integralImage[i-1][j] + rsum[i][j];
                }
            }
        }
        /*for(int i=0;i<rows;i++) {
            for (int j = 0; j < cols; j++) {
                //if(i==105 && j== 19) { //we can find this in 106 col n 20th row in matlab
                if(i==19 && j== 105) {   //we can find this in 20th col n 106th row in matlab
                    System.out.println("i:"+i+" j:"+j+" integralImage:"+integralImage[i][j]);
                }
            }
        }*/
        return integralImage;
    }

    public BufferedImage getSlidingWindowPatchImage(List<WindowData> slidingWindows) {
        BufferedImage bmpImage = new BufferedImage(histogramEqualizedImage.getWidth(),histogramEqualizedImage.getHeight(),BufferedImage.TYPE_BYTE_GRAY); //by default everything is zero
        WritableRaster bmpRaster = bmpImage.getRaster();

        for (WindowData windowData : slidingWindows) {
            int xmin = windowData.getMinX();
            int xmax = windowData.getMaxX();
            int ymin = windowData.getMinY();
            int ymax = windowData.getMaxY();
            for (int i = xmin; i <= xmax; i++) {
                for (int j = ymin; j <= ymax; j++) {
                    bmpRaster.setSample(j, i, 0, 1); //wherever we are getting sliding windows we are making it as 1. So that image contains only 0 n 1.
                    // If we make 255 then we can visually see this.
                }
            }
        }
        return bmpImage;
    }
}
