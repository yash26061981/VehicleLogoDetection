package com.avanseus.segmentation;


import com.avanseus.Dimension.PCA.Matrix;
import com.avanseus.imageUtils.ImageUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yash on 14-04-2016.
 */
public class ReAssignLabelsLeftToRight {
    ImageUtils imageUtils;
    boolean isSquarePlate;

    public ReAssignLabelsLeftToRight(boolean isSquarePlateFrame){
        isSquarePlate = isSquarePlateFrame;
        imageUtils = new ImageUtils();
    }

    public double[][] reAssignLabels(double[][] labelImage){

        double[] totalLabels = imageUtils.findUniqueIn2D(labelImage);
        if(totalLabels[0] == 0)
            totalLabels = imageUtils.shiftArrayLeft(totalLabels);

        int[] midLine = null;
        if (isSquarePlate){
            midLine = new int[2];
            midLine[0] = (int) Math.floor(labelImage[0].length /4);
            midLine[1] = (int) Math.floor(labelImage[0].length * 3 /4);
        }else{
            midLine = new int[1];
            midLine[0] = (int) Math.floor(labelImage[0].length /2);
        }
        //int midLine = (int) Math.floor(labelImage[0].length /2);
        double[][] newLabelImage = new double[labelImage.length][labelImage[0].length];
        int midIndex = 0;
        List<Integer> doneLabel = new ArrayList<>();
        imageUtils.copy2D(labelImage, newLabelImage);
        boolean f1Time = true;
        int labelId = 1;
        do {
            int[] limit = {-1};
            boolean isRow = true;
            double[] midScanLine = imageUtils.getAllElementOfRowCol(labelImage, isRow, midLine[midIndex++], limit);

            int[] labelIndx = imageUtils.findIndx(midScanLine, ">", 0.0);
            for(int id = 0; id < labelIndx.length; id++) {
                if ( f1Time ) {
                    f1Time = false;
                    doneLabel.add((int) midScanLine[labelIndx[id]]);
                    int[][] midScanColRow = imageUtils.findIndxIn2D(labelImage, "==", midScanLine[labelIndx[id]]);
                    if ( !imageUtils.isMember(labelId, totalLabels) ) {
                        for (int indx = 0; indx < midScanColRow.length; indx++) {
                            newLabelImage[midScanColRow[indx][0]][midScanColRow[indx][1]] = labelId;
                        }
                    } else {
                        int[][] labelColRow = imageUtils.findIndxIn2D(labelImage, "==", labelId);
                        for (int indx = 0; indx < midScanColRow.length; indx++) {
                            newLabelImage[midScanColRow[indx][0]][midScanColRow[indx][1]] = labelId;
                        }
                        for (int indx = 0; indx < labelColRow.length; indx++) {
                            newLabelImage[labelColRow[indx][0]][labelColRow[indx][1]] = midScanLine[labelIndx[id]];
                        }
                    }
                    labelId++;
                } else {
                    if ( !imageUtils.isMember(midScanLine[labelIndx[id]], imageUtils.getArrayFromList(doneLabel)) ) {
                        doneLabel.add((int) midScanLine[labelIndx[id]]);
                        int[][] midScanColRow = imageUtils.findIndxIn2D(labelImage, "==", midScanLine[labelIndx[id]]);
                        if ( (!imageUtils.isMember(labelId, totalLabels)) ||
                                (imageUtils.isMember(labelId, imageUtils.getArrayFromList(doneLabel))) ) {
                            for (int indx = 0; indx < midScanColRow.length; indx++) {
                                newLabelImage[midScanColRow[indx][0]][midScanColRow[indx][1]] = labelId;
                            }
                        } else {
                            int[][] labelColRow = imageUtils.findIndxIn2D(labelImage, "==", labelId);
                            for (int indx = 0; indx < midScanColRow.length; indx++) {
                                newLabelImage[midScanColRow[indx][0]][midScanColRow[indx][1]] = labelId;
                            }
                            for (int indx = 0; indx < labelColRow.length; indx++) {
                                newLabelImage[labelColRow[indx][0]][labelColRow[indx][1]] = midScanLine[labelIndx[id]];
                            }
                        }
                        labelId++;
                    }
                }
            }
        }
        while (midIndex < midLine.length);
        return newLabelImage;
    }
}
