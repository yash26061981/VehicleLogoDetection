package com.avanseus.imageUtils;

import com.avanseus.avr.model.AVRConstants;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.*;

/**
 * Created by yash on 11-04-2016.
 */
public class AdaptiveThresholding {
    public BufferedImage thresholdedImage;
    private int[] mask = AVRConstants.niBlackMask;
    ImageUtils imageUtils = new ImageUtils();
    double otsuWeight = 0;
    double otsuVariance = 0;

    public BufferedImage applyNiBlackThresholding(BufferedImage inImage){

        try{
            double[][] meanImage = averageFiltering(inImage, mask, "replicate");
            double[][] meanSquaredImage = averageFiltering(inImage, mask, "square");

            double[][] deviation = computeDeviation(meanImage, meanSquaredImage, inImage);

            int minGrayValue = imageUtils.findMinimumGrayValue(inImage);

            double[][] scoreValue = findScoreValue(meanImage,minGrayValue,deviation,inImage);

            thresholdedImage = new BufferedImage(inImage.getWidth(),inImage.getHeight(),BufferedImage.TYPE_BYTE_GRAY);
            WritableRaster wRaster = thresholdedImage.getRaster();
            Raster rRaster = inImage.getRaster();

            for(int x = 0; x < wRaster.getWidth(); x++) {
                for (int y = 0; y < wRaster.getHeight(); y++) {
                    int pixelVal = rRaster.getSample(x, y, 0);
                    if (pixelVal >= scoreValue[x][y]) {
                        wRaster.setSample(x, y, 0, 1.0f);
                    }
                    else {
                        wRaster.setSample(x, y, 0, 0.0f);
                    }
                }
            }

        }catch (Exception e) {
            e.printStackTrace();
        }
        return thresholdedImage;
    }

    private double[][] findScoreValue(double[][] meanImage, int minGrayValue, double[][] deviation, BufferedImage inImage){
        double[][] scoreFactor = new double[inImage.getWidth()][inImage.getHeight()];
        double constK = AVRConstants.constK;
        for (int col =0; col < inImage.getWidth(); col++) {
            for (int row = 0; row < inImage.getHeight(); row++) {
                scoreFactor[col][row] = (1 - constK)* meanImage[col][row] + constK  * minGrayValue +
                        ((constK *deviation[col][row])/AVRConstants.dynamicRange) * (meanImage[col][row] - minGrayValue);
            }
        }
        return scoreFactor;
    }

    private double[][] computeDeviation(double[][] meanImage, double[][] meanSquaredImage, BufferedImage inImage){
        double[][] deviateImage = new double[inImage.getWidth()][inImage.getHeight()];
        for (int col =0; col < inImage.getWidth(); col++) {
            for (int row = 0; row < inImage.getHeight(); row++) {
                deviateImage[col][row] = Math.sqrt(meanSquaredImage[col][row] - Math.pow(meanImage[col][row],2));
            }
        }
        return deviateImage;
    }

    private double[][] averageFiltering(BufferedImage inImage, int[] mask, String method){
        int m = mask[0];
        int n = mask[1];

        if(m%2 != 1)
            m = m-1;

        if(n%2 != 1)
            n = n-1;

        int[] newMask = {(m+1)/2,(n+1)/2};

        double[][] paddedImage = imageUtils.padImage(inImage, newMask, method);

        int newWidth = inImage.getWidth() + 2*newMask[1];
        int newHeight = inImage.getHeight() + 2*newMask[0];
        double[][] cumulativeImage = new double[newWidth][newHeight];

        cumulativeImage[0][0] = paddedImage[0][0];

        for (int col =0; col < newWidth; col++) {
            cumulativeImage[col][0] = paddedImage[col][0];
            for (int row = 1; row < newHeight; row++) {
                cumulativeImage[col][row] = cumulativeImage[col][row - 1] + paddedImage[col][row];
            }
        }
        for (int row =0; row < newHeight; row++) {
            cumulativeImage[0][row] = cumulativeImage[0][row];
            for (int col = 1; col < newWidth; col++) {
                cumulativeImage[col][row] = cumulativeImage[col-1][row] + cumulativeImage[col][row];
            }
        }
        double[][] meanImage = new double[inImage.getWidth()][inImage.getHeight()];
        for(int col = 0; col < inImage.getWidth(); col++){
            for (int row = 0; row < inImage.getHeight(); row++){
                meanImage[col][row] = (cumulativeImage[col][row] + cumulativeImage[col+n][row+m]
                        - cumulativeImage[col][row+m] - cumulativeImage[col+n][row])/(m*n);
            }
        }
        return meanImage;

    }

    public BufferedImage applyOtsuThresholding(BufferedImage inImage){
        try{
            Raster raster = inImage.getRaster();
            EqualiseHistogram equaliseHistogram = new EqualiseHistogram();
            double[] histG = equaliseHistogram.computeHistogram(raster);
            int[] index = new int[256];
            double[] result = new double[256];
            for (int indx = 0; indx < 256; indx++){
                index[indx] = indx;
            }
            for (int indx = 0; indx < 256; indx++){
                calculateWeightVarianceOtsu(0,indx,histG,index);
                double weightBG = otsuWeight;
                double varianceBG = otsuVariance;
                calculateWeightVarianceOtsu(indx+1,255,histG,index);
                result[indx] = (weightBG * varianceBG) + (otsuWeight * otsuVariance);
            }
            int[] minVal = imageUtils.findIndx(result, "==", imageUtils.findMinimum(result));
            int threshold = (((minVal[0] - 1) * 255)/256);
            thresholdedImage = new BufferedImage(inImage.getWidth(),inImage.getHeight(),BufferedImage.TYPE_BYTE_GRAY);
            WritableRaster wRaster = thresholdedImage.getRaster();
            Raster rRaster = inImage.getRaster();

            for(int x = 0; x < wRaster.getWidth(); x++) {
                for (int y = 0; y < wRaster.getHeight(); y++) {
                    int pixelVal = rRaster.getSample(x, y, 0);
                    if (pixelVal >  threshold) {
                        wRaster.setSample(x, y, 0, 1.0f);
                    }
                    else {
                        wRaster.setSample(x, y, 0, 0.0f);
                    }
                }
            }

        }catch (Exception e) {
            e.printStackTrace();
        }
        return thresholdedImage;
    }

    private void calculateWeightVarianceOtsu(int stInd, int edInd, double[] histg, int[] index){
        double sumH = 0, value = 0;
        double mean = 0;
        for(int indx = stInd; indx <= edInd; indx++){
            sumH = sumH + histg[indx];
            value = value + (histg[indx] * index[indx]);
        }
        double sumofHist = imageUtils.sumOfArray(histg);
        otsuWeight = sumH/sumofHist;
        if(sumH == 0)
            otsuVariance = 0;
        else {
            mean = value / sumH;
            double numer = 0;
            for (int indx = stInd; indx <= edInd; indx++) {
                numer = numer + (Math.pow((index[indx] - mean), 2) * histg[indx]);
            }
            otsuVariance = numer / sumH;
        }
    }
}
