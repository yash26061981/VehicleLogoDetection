package com.avanseus.imageUtils;

import com.avanseus.Dimension.PCA.Matrix;
import com.avanseus.segmentation.MergeConnectingLabels;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by hemanth on 14/4/16.
 */
public class ConnectedComponentAnalysis {

    private ImageUtils imageUtils;
    Matrix matrix;
    private double[][] mask = {
            {1, 1, 1},
            {1, 1, 0},
            {0, 0, 0}
    };
    public ConnectedComponentAnalysis() {
        imageUtils = new ImageUtils();
        matrix = new Matrix();
    }

    /**
     *
     * @param bmpImage
     * @return
     */
    public double[][] findConnectedComponent(BufferedImage bmpImage) {
        int pMask[] = {1, 1};
        double[][] oldPaddedBmp = imageUtils.padImage(bmpImage, pMask, "zeros");
        double[][] paddedBmp = matrix.transposeMatrix(oldPaddedBmp);
        double[][] labelImage = new double[paddedBmp.length][paddedBmp[0].length];
        HashMap<Integer,int[]> equivalent = new HashMap<>();
        int counter = 1;
        int label = 1;
        int rows = paddedBmp.length;
        int cols = paddedBmp[0].length;
        for(int i=1;i<=rows-2;i++) {
            for(int j=1;j<=cols-2;j++) {

                double[][] cropImage = new double[3][3];
                for(int x=i-1, rr =0;x<=i+1 && rr < 3 ; x++,rr++) {
                    for(int y=j-1, cc =0;y<=j+1 && cc < 3;y++, cc++) {
                        cropImage[rr][cc] = paddedBmp[x][y];
                    }
                }

                double[][] maskedImage = applyMaskForTheGivenMatrix(cropImage);

                if(maskedImage[1][1] == 1) {
                    double sum = 0;
                    for(int k=0;k<maskedImage.length;k++) {
                        sum += maskedImage[0][k];
                    }
                    sum += maskedImage[1][0];
                    if(sum == 0) {
                      labelImage[i][j] = label; label++;
                    } else {
                        double[] cropIndexedLabelImage = new double[9];
                        int indx = 0;
                        for(int x=i-1;x<=i+1;x++) {
                            for(int y=j-1;y<=j+1;y++) {
                                cropIndexedLabelImage[indx++] = labelImage[x][y];
                            }
                        }
                        double[] uniqueCropLabelIndex = imageUtils.findUnique(cropIndexedLabelImage);
                        if(uniqueCropLabelIndex[0] == 0) {
                            uniqueCropLabelIndex = Arrays.copyOfRange(uniqueCropLabelIndex,1,uniqueCropLabelIndex.length);
                        }
                        if(uniqueCropLabelIndex.length == 1)
                            labelImage[i][j] = uniqueCropLabelIndex[0];
                        else {
                            int[] pair = new int[2];
                            pair[0] = (int) uniqueCropLabelIndex[0];
                            pair[1] = (int) uniqueCropLabelIndex[1];
                            equivalent.put(counter,pair);
                            counter++;
                            labelImage[i][j] = uniqueCropLabelIndex[0];
                        }
                    }
                }
            }
        }
        if(equivalent!=null && equivalent.size()!=0) {
            return (getConnectedLabels(matrix.transposeMatrix(labelImage),equivalent));
        } else {
            return matrix.transposeMatrix(labelImage);
        }
    }


    public double[][] getConnectedLabels(double[][] labelImage,HashMap<Integer,int[]> equivalentPairs) {
        MergeConnectingLabels mergeConnectingLabels = new MergeConnectingLabels(labelImage,equivalentPairs);
        double[][] newLabelImage = mergeConnectingLabels.findMergeConnectedLabels();
        return newLabelImage;
    }

    private double[][] applyMaskForTheGivenMatrix(double[][] temp) {
        double[][] maskedImage = new double[3][3];
        for(int i=0;i<temp.length;i++) {
            for(int j=0;j<temp[0].length;j++) {
                maskedImage[i][j] = mask[i][j] * temp[i][j];
            }
        }
        return maskedImage;
    }


}
