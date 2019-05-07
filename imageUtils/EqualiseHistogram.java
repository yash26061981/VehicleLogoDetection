package com.avanseus.imageUtils;

import com.avanseus.avr.driver.Driver;
import com.avanseus.avr.model.AVRConstants;

import javax.imageio.ImageIO;
import java.awt.image.*;
import java.io.File;

public class EqualiseHistogram {
    public BufferedImage equalisedImage;
    private int numberOfBins;
    private int levels;

    public EqualiseHistogram() {
        numberOfBins = 256;
        levels = 64;
    }

    public BufferedImage performHistEqOperation(BufferedImage grayImage){
        try{
            Raster grayRaster = grayImage.getRaster();
            double[] histG = computeHistogram(grayRaster);
            double[] cumHist = computeCumulativeHistogram(histG);
            double[] histEqMapping = computeTransformationMapping(grayImage,histG,cumHist);
            int equalisedPixel;
            double tol = 0.5;
            equalisedImage = new BufferedImage(grayImage.getWidth(),grayImage.getHeight(),BufferedImage.TYPE_BYTE_GRAY);
            WritableRaster raster = equalisedImage.getRaster();
            for(int x = 0; x < raster.getWidth(); x++)
                for (int y = 0; y < raster.getHeight(); y++) {
                    equalisedPixel = (int)(histEqMapping[grayRaster.getSample(x, y, 0)] + tol);
                    raster.setSample(x, y, 0, equalisedPixel);
                }

        }catch (Exception e) {
            e.printStackTrace();
        }
        return equalisedImage;
    }

    public double[]  computeHistogram(Raster grayImageRaster){
        double[] histG = new double[numberOfBins];
        for (int x = 0; x < grayImageRaster.getWidth() ; x++)
            for (int y = 0; y < grayImageRaster.getHeight(); y++) histG[grayImageRaster.getSample(x, y, 0)]++;
        return histG;
    }

    public double[] computeCumulativeHistogram(double[] histG){
        double[] cumHist = new double[numberOfBins];
        cumHist[0] = histG[0];
        for (int x = 1; x < numberOfBins; x++){
            cumHist[x] = cumHist[x-1]+histG[x];
        }
        return cumHist;
    }

    private double[] computeTransformationMapping(BufferedImage grayImage,double[] histG,double[] cumHist){
        double sqrtEps = 1.4901e-08;
        double[] cumdHgram = new double[levels];
        double[][] err = new double[levels][numberOfBins];
        double[] mapping = new double[numberOfBins];
        cumdHgram[0] = ((grayImage.getHeight()*grayImage.getWidth())/levels);
        for(int x =1; x<levels; x++)
            cumdHgram[x] = cumdHgram[x-1] + ((grayImage.getHeight()*grayImage.getWidth())/levels);

        histG[0] = 0; histG[numberOfBins-1] = 0;
        for (int x = 0; x < levels; x++){
            for (int y = 0; y < numberOfBins; y++){
                err[x][y] = (cumdHgram[x] - cumHist[y]) + (histG[y]/2.0);
                if (err[x][y] < (-1 * grayImage.getWidth() * grayImage.getHeight() * sqrtEps)){
                    err[x][y] = grayImage.getWidth() * grayImage.getHeight();
                }
            }
        }
        for (int y = 0; y < numberOfBins; y++){
            double min = grayImage.getWidth() * grayImage.getHeight() + 1;
            int minId = 0;
            for (int x = 0; x < levels; x++){
                if (err[x][y] < min){
                    min = err[x][y];
                    minId = x;
                }
            }
            mapping[y] = 255.0*((double)(minId))/(levels - 1);
        }
        return mapping;
    }

}
