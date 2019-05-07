package com.avanseus.avr.detection;

import com.avanseus.imageUtils.EdgeDetection;
import com.avanseus.imageUtils.ImageUtils;
import com.avanseus.avr.driver.Driver;
import com.avanseus.imageUtils.MorphologicalOperations;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;

/**
 * Created by hemanth on 20/4/16.
 */
public class FineTuneCropping {

    private ImageUtils imageUtils;
    private EdgeDetection edgeDetection;

    public FineTuneCropping() {
        imageUtils = new ImageUtils();
        edgeDetection = new EdgeDetection();
    }

    public int[][] cropLPImage(int[][] inImage) {
        Double edgethreshold = null;
        int cutoffThreshold = 5;
        int[][] verticallyCroppedImage = getVerticalProfile(inImage,cutoffThreshold,edgethreshold);
        if( (verticallyCroppedImage) == null){
            return inImage;
        }
        else{
            cutoffThreshold = 10;
            int[][] horizontallyCroppedImage = getHorizontalProfile(verticallyCroppedImage,cutoffThreshold,edgethreshold);
            if((horizontallyCroppedImage) == null){
                return verticallyCroppedImage;
            }else {
                return horizontallyCroppedImage;
            }
        }
    }

    public int[][] cropLogoImage(int[][] inImage) {
        EdgeDetection edgeDetection = new EdgeDetection();
        BufferedImage edgeImage = edgeDetection.getBackgroundTextureSuppressedEdgeImage(imageUtils.getBufferedImageFrom2D(inImage), 0.01);
        imageUtils.getVisibleImageFromBufferedImageOfLogicalOnes(edgeImage, "LocalisedPatchesSuppressedEdges");
        MorphologicalOperations morphologicalOperations = new MorphologicalOperations();
        double[][] se = morphologicalOperations.getStructuringElement("diamond",3);
        BufferedImage closedImage = morphologicalOperations.applyMorphologicalClosing(edgeImage,se);
        imageUtils.getVisibleImageFromBufferedImageOfLogicalOnes(closedImage, "closedSuppressedEdges");
        BufferedImage openImage = morphologicalOperations.applyMorphologicalOpening(closedImage,se);
        imageUtils.getVisibleImageFromBufferedImageOfLogicalOnes(openImage, "openedSuppressedEdges");

        double[] verticalProfile = imageUtils.sum2DRowColWise(imageUtils.get2DImage(openImage),2);
        int[] id12 = imageUtils.findIndx(verticalProfile,">",0);
        if(id12.length == 0) {
            return inImage;
        }else {
            int[] limit = {id12[0] + 1, id12[id12.length - 1] - 1};
            Arrays.sort(limit);
            double[][] verticalCroppedProfile = imageUtils.getEntireRowColElements(imageUtils.getDoubleCastArray(inImage), true, limit);
            double[][] verticalEdgeCroppedProfile = imageUtils.getEntireRowColElements(imageUtils.get2DImage(openImage), true, limit);
            double[] horizontalProfile = imageUtils.sum2DRowColWise(verticalEdgeCroppedProfile,1);
            int[] id22 = imageUtils.findIndx(horizontalProfile,">",0);
            if(id22.length == 0){
                return  imageUtils.getIntCastArray(verticalCroppedProfile);
            }else {
                limit[0] = id22[0]; limit[1] =  id22[id22.length - 1];
                if ( id22[0] > 2 )
                    limit[0] = id22[0] - 2;
                if ( id22[id22.length - 1] < horizontalProfile.length - 2 )
                    limit[1] = id22[id22.length - 1] + 2;

                double[][] horizontalCroppedProfile = imageUtils.getEntireRowColElements(verticalCroppedProfile, false, limit);
                return imageUtils.getIntCastArray(horizontalCroppedProfile);
            }
        }
    }

    public int[][] getVerticalProfile(int[][] inImage, int cutoffThreshold, Double edgethreshold) {
        BufferedImage edgeBufferedImage = edgeDetection.getSobelEdgeImage(imageUtils.getBufferedImageFrom2D(inImage), "vertical",edgethreshold);
        int[][] edgeImage = imageUtils.get2DImageInteger(edgeBufferedImage);

        double[] verticalProfile = imageUtils.sum2DRowColWise(imageUtils.getDoubleCastArray(edgeImage),2);
        int[] id12 = imageUtils.findIndx(verticalProfile,">",cutoffThreshold);
        if(id12.length == 0) {
            //System.out.println("NO Vertical Cropping done");
            return null;
        }else {
            int[] limit = {id12[0] - 1, id12[id12.length - 1] + 1};
            if(limit[0] < 0)
                limit[0] = 0;
            if(limit[1] >= verticalProfile.length)
                limit[1] = verticalProfile.length - 1;

            Arrays.sort(limit);
            double[][] verticalCroppedProfile = imageUtils.getEntireRowColElements(imageUtils.getDoubleCastArray(inImage), true, limit);

            //System.out.println("Vertical Cropping done");
            //saveCroppedImage(verticallyCroppedImg,"verticallyCropped_"+imageSequenceNumber);
            return imageUtils.getIntCastArray(verticalCroppedProfile);
        }
    }

    private int[][] getHorizontalProfile(int[][] inImage, int cutoffThreshold, Double edgethreshold) {
        BufferedImage edgeBufferedImage = edgeDetection.getSobelEdgeImage(imageUtils.getBufferedImageFrom2D(inImage), "vertical",edgethreshold);
        int[][] edgeImage = imageUtils.get2DImageInteger(edgeBufferedImage);

        double[] horizontalProfile = imageUtils.sum2DRowColWise(imageUtils.getDoubleCastArray(edgeImage),1);
        int[] id22 = imageUtils.findIndx(horizontalProfile,">",cutoffThreshold);
        if(id22.length == 0){
            //System.out.println("NO Horizontal Cropping done");
            return  null;
        }else {
            int[] limit = {id22[0], id22[id22.length - 1]};
            if ( id22[0] > 2 )
                limit[0] = id22[0] - 2;
            if ( id22[id22.length - 1] < horizontalProfile.length - 2 )
                limit[1] = id22[id22.length - 1] + 2;


            double[][] horizontalCroppedProfile = imageUtils.getEntireRowColElements(imageUtils.getDoubleCastArray(inImage), false, limit);
            //System.out.println("Horizontal Cropping done");
            //saveCroppedImage(horizantallyCroppedImg,"HorizontallyCropped_"+imageSequenceNumber);
            return imageUtils.getIntCastArray(horizontalCroppedProfile);
        }
    }

    /*private List<Integer> getValidPeaksFromSummationArray(ArrayList<Integer> sumOfRowOrColm) {
        List<Integer> peaks = findPeak(sumOfRowOrColm);
        double meanOfPeaks = calculateMean(peaks);
        double stdDevOfPeaks = calculateStdDeviation(meanOfPeaks, peaks);
        int threshold = (int) (meanOfPeaks - stdDevOfPeaks -2);
        List<Integer> validPeakMap = new ArrayList<>();
        for(Integer val: peaks) {
            if(val >= threshold) {
                validPeakMap.add(val);
            }
        }
        return validPeakMap;
    }


    public double calculateStdDeviation(double meanOfPeaks, List<Integer> peaks) {
        List<Double> squaredArray = new ArrayList<>();
        for(Integer i: peaks) {
            double temp = i - meanOfPeaks;
            squaredArray.add(temp*temp);
        }

        double sumOfSquares = 0;
        for(double val: squaredArray) {
            sumOfSquares += val;
        }
        return Math.sqrt(sumOfSquares/(squaredArray.size()-1));
    }

    public double calculateMean(List<Integer> peaks) {
        double sum = 0;
        for(Integer i:peaks) {
            sum += i;
        }
        sum = sum/peaks.size();
        return sum;
    }

    public List<Integer> findPeak(ArrayList<Integer> rowSumArray) {
        List<Integer> peakValues = new ArrayList<>();
        for(int i=1;i<rowSumArray.size()-1;i++) {
            if((rowSumArray.get(i) > rowSumArray.get(i-1)) && (rowSumArray.get(i) > rowSumArray.get(i+1))) {
                peakValues.add(rowSumArray.get(i));
            }
        }
        return peakValues;
    }*/
}
