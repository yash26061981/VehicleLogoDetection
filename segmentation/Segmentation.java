package com.avanseus.segmentation;

import com.avanseus.avr.detection.FineTuneCropping;
import com.avanseus.avr.driver.Driver;
import com.avanseus.avr.model.AVRConstants;
import com.avanseus.imageUtils.ConnectedComponentAnalysis;
import com.avanseus.imageUtils.ImageUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created by yash on 22-04-2016.
 */
public class Segmentation {
    ImageUtils imageUtils = new ImageUtils();

    public HashMap<Integer,int[][]> SegmentUsingConnectedComponentAnalysis(BufferedImage thresholdBmp){

        ConnectedComponentAnalysis cca = new ConnectedComponentAnalysis();
        BufferedImage negateThresholdBmp = imageUtils.negateBitMap(thresholdBmp);

        double[][] labelImage = cca.findConnectedComponent(negateThresholdBmp);
        imageUtils.getVisibleImageFromBufferedImageOfLogicalOnes(imageUtils.getBufferedImageFrom2D(labelImage), "labelled.jpg");
        //debugLabelTest(labelImage,1);

        boolean squarePlateFlag = false;
        int xRowLabelSize = labelImage[0].length;
        int yColLabelSize = labelImage.length;
        double ratio = (double)(yColLabelSize)/(double)(xRowLabelSize);
        if (ratio < 3){
            squarePlateFlag = true;
        }
        labelImage = RejectInValidLabels(labelImage, squarePlateFlag);
        imageUtils.getVisibleImageFromBufferedImageOfLogicalOnes(imageUtils.getBufferedImageFrom2D(labelImage), "labeldeleted.jpg");
        //debugLabelTest(labelImage,1);

        ReAssignLabelsLeftToRight reAssignLabelsLeftToRight = new ReAssignLabelsLeftToRight(squarePlateFlag);
        labelImage = reAssignLabelsLeftToRight.reAssignLabels(labelImage);

        HashMap<Integer,int[][]> charLabels = GetSegmentedCharacters(labelImage);
        SplitConnectingCharacters splitConnectingCharacters = new SplitConnectingCharacters();
        splitConnectingCharacters.SplitConnectingCharacters(charLabels);
        HashMap<Integer,int[][]> splittedCharLabels = splitConnectingCharacters.splitMergedCharacters();

        if(ValidateSegmentedLabels(splittedCharLabels)){
            return RejectNotMergedButInvalidChars(splittedCharLabels);
        }else {
            return null;
        }
        //return splittedCharLabels;

        //return charLabels;
    }

    private void debugLabelTest(double[][] labelImage, int labelId){
        int[][] indxColRow = imageUtils.findIndxIn2D(labelImage,"==",labelId);
        int[] limit = {-1};
        double[] labelCols = imageUtils.getAllElementOfRowCol(imageUtils.getDoubleCastArray(indxColRow),true,0,limit);
        double[] labelRows = imageUtils.getAllElementOfRowCol(imageUtils.getDoubleCastArray(indxColRow),true,1,limit);
        int xSize = (int)(imageUtils.findMaximum(labelRows) - imageUtils.findMinimum(labelRows) + 1 + 2);
        int ySize = (int)(imageUtils.findMaximum(labelCols) - imageUtils.findMinimum(labelCols) + 1 + 2);
        int ymin = (int)imageUtils.findMinimum(labelCols);
        int xmin = (int)imageUtils.findMinimum(labelRows);
        int[][] charImage = new int[ySize][xSize];
        for (int indx1 = 0; indx1 < labelCols.length; indx1++){
            charImage[(int)labelCols[indx1]-ymin+2][(int)labelRows[indx1]-xmin+2] =
                    (labelImage[(int)labelCols[indx1]][(int)labelRows[indx1]] > 0 ? 1: 0);
        }
        String name = "label_" + labelId + ".jpg";
        imageUtils.getVisibleImageFromBufferedImageOfLogicalOnes(imageUtils.getBufferedImageFrom2D(charImage), name);
    }

    private HashMap<Integer,int[][]> RejectNotMergedButInvalidChars(HashMap<Integer,int[][]> splittedChars){
        int totalSize = splittedChars.size();
        HashMap<Integer,int[][]> finalLabels = new HashMap<>();
        int validIndx=0;
        for (int indx=0; indx<totalSize;indx++){
            int[][] labelImage = splittedChars.get(indx);
            int xRowLabelSize = labelImage[0].length;
            int yColLabelSize = labelImage.length;
            double ratio = (double)yColLabelSize/xRowLabelSize;
            if (ratio <= AVRConstants.NOT_MERGED_INVALID_CHAR_AR_LIMIT){
                finalLabels.put(validIndx,labelImage);
                validIndx++;
            }
        }
        if(finalLabels.size() == 0)
            return null;
        else if((finalLabels.size() > AVRConstants.MAX_NUMBER_SEGMENTED_LABELS) ||
                (finalLabels.size() < AVRConstants.MIN_NUMBER_SEGMENTED_LABELS)){
            return null;
        } else {
            return finalLabels;
        }
    }

    private boolean ValidateSegmentedLabels(HashMap<Integer,int[][]> splittedLabels){
        int totalSize = splittedLabels.size();
        int validLabels =0, inValidLabels=0;
        for (int indx=0; indx<totalSize;indx++){
            int[][] labelImage = splittedLabels.get(indx);
            int xRowLabelSize = labelImage[0].length;
            int yColLabelSize = labelImage.length;
            double ratio = (double)yColLabelSize/xRowLabelSize;
            if (ratio > 1)
                inValidLabels++;
            else
                validLabels++;
        }
        double inValidRatio = (double)inValidLabels/totalSize;
        if((totalSize > AVRConstants.MAX_NUMBER_SEGMENTED_LABELS) ||
                (totalSize < AVRConstants.MIN_NUMBER_SEGMENTED_LABELS) ||
                (inValidRatio >= AVRConstants.MAX_INVALID_SEGMENTED_LABEL_LIMIT)){
            return false;
        } else {
            return true;
        }
    }

    private HashMap<Integer,int[][]> GetSegmentedCharacters(double[][] labelImage){
        double[] totalLabels = imageUtils.findUniqueIn2D(labelImage);
        if (totalLabels[0] == 0)
            totalLabels = imageUtils.shiftArrayLeft(totalLabels);
        int setOffset = 2;
        HashMap<Integer,int[][]> charLabels = new HashMap<>();

        for(int indx  = 0; indx < totalLabels.length; indx++){
            int[][] indxColRow = imageUtils.findIndxIn2D(labelImage,"==",totalLabels[indx]);
            int[] limit = {-1};
            double[] labelCols = imageUtils.getAllElementOfRowCol(imageUtils.getDoubleCastArray(indxColRow),true,0,limit);
            double[] labelRows = imageUtils.getAllElementOfRowCol(imageUtils.getDoubleCastArray(indxColRow),true,1,limit);
            int xSize = (int)(imageUtils.findMaximum(labelRows) - imageUtils.findMinimum(labelRows) + 1 + setOffset);
            int ySize = (int)(imageUtils.findMaximum(labelCols) - imageUtils.findMinimum(labelCols) + 1 + setOffset);
            int ymin = (int)imageUtils.findMinimum(labelCols);
            int xmin = (int)imageUtils.findMinimum(labelRows);
            int[][] charImage = new int[ySize][xSize];
            for (int indx1 = 0; indx1 < labelCols.length; indx1++){
                charImage[(int)labelCols[indx1]-ymin+setOffset][(int)labelRows[indx1]-xmin+setOffset] =
                        (labelImage[(int)labelCols[indx1]][(int)labelRows[indx1]] > 0 ? 1: 0);
            }
            //imageUtils.getVisibleImageFromBufferedImageOfLogicalOnes(imageUtils.getBufferedImageFrom2D(charImage), "label1.jpg");
            charLabels.put(indx, charImage);
        }
        return charLabels;
    }


    private double[][] RejectInValidLabels(double[][] labelImage, boolean squarePlateFlag){
        double[] totalLabels = imageUtils.findUniqueIn2D(labelImage);
        if (totalLabels[0] == 0)
            totalLabels = imageUtils.shiftArrayLeft(totalLabels);
        int xRowLabelSize = labelImage[0].length;
        int yColLabelSize = labelImage.length;
        if (squarePlateFlag){
            xRowLabelSize = xRowLabelSize/2;
        }
        for(int indx  = 0; indx < totalLabels.length; indx++){
            int[][] indxColRow = imageUtils.findIndxIn2D(labelImage,"==",totalLabels[indx]);
            int[] limit = {-1};
            double[] labelCols = imageUtils.getAllElementOfRowCol(imageUtils.getDoubleCastArray(indxColRow),true,0,limit);
            double[] labelRows = imageUtils.getAllElementOfRowCol(imageUtils.getDoubleCastArray(indxColRow),true,1,limit);
            double xSize = imageUtils.findMaximum(labelRows) - imageUtils.findMinimum(labelRows) + 1;
            double ySize = imageUtils.findMaximum(labelCols) - imageUtils.findMinimum(labelCols) + 1;

            if (labelCols.length < AVRConstants.MIN_LABEL_AREA || (xSize/xRowLabelSize) < AVRConstants.MIN_LABEL_XRATIO || (ySize/yColLabelSize) < AVRConstants.MIN_LABEL_YRATIO){
                for (int indx1 = 0; indx1 < labelCols.length; indx1++){
                    labelImage[(int)labelCols[indx1]][(int)labelRows[indx1]] = 0;
                }
            }
        }
        return labelImage;
    }
    public static void main(String[] args) {
        try {
            Segmentation segmentation = new Segmentation();
            ImageUtils imageUtils = new ImageUtils();
            Driver driver = new Driver(false,null,null);
            Driver.destDir = "D:\\TataPowerSED\\MatlabCode\\ANPRDATA\\ExtractedFrames\\segmentedImage\\34";
            File inputImageFile = new File("D:\\TataPowerSED\\MatlabCode\\ANPRDATA\\ExtractedFrames\\segmentedImage\\34.jpg");
            BufferedImage inputImage = ImageIO.read(inputImageFile);
            double [][] inImage = imageUtils.getLogical2DImageDouble(inputImage);
            FineTuneCropping fineTuneCropping = new FineTuneCropping();
            int[][] croppedImage = fineTuneCropping.cropLPImage(imageUtils.getIntCastArray(inImage));
            HashMap<Integer,int[][]> charLabels = segmentation.SegmentUsingConnectedComponentAnalysis(imageUtils.getBufferedImageFrom2D(croppedImage));
            if((charLabels) == null){
                System.out.printf("NO Segmented Characters");
            }
            else {
                for (int indx = 1; indx <= charLabels.size(); indx++) {
                    String name = "SegmentedChars_" + indx + "_" + indx;
                    imageUtils.getVisibleImageFromBufferedImageOfLogicalOnes(
                            imageUtils.getBufferedImageFrom2D(charLabels.get(indx - 1)), name);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
