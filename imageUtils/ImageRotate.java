package com.avanseus.imageUtils;

import com.avanseus.Dimension.PCA.Matrix;

/**
 * Created by yash on 08-05-2016.
 */
public class ImageRotate {
    private ImageUtils imageUtils;
    private Matrix matrixOperations;

    public ImageRotate() {
        imageUtils = new ImageUtils();
        matrixOperations = new Matrix();
    }

    public int[][] RotateImageUsingNearestNeighbor(double deskewAngle,int[][] inImage) {

        int row = inImage.length;
        int col = inImage[0].length;

        double rads = (2 * Math.PI * deskewAngle) / 360;
        int rowf = (int) Math.ceil(row * Math.abs(Math.cos(rads)) + col * Math.abs(Math.sin(rads)));
        int colf = (int) Math.ceil(row * Math.abs(Math.sin(rads)) + col * Math.abs(Math.cos(rads)));
        int x0 = (int) Math.ceil(row / 2);
        int y0 = (int) Math.ceil(col / 2);

        int[][] temp = new int[rowf][colf];

        double midx = Math.ceil(temp.length / 2);
        double midy = Math.ceil(temp[0].length / 2);

        for (int i = 0; i < temp.length; i++) {
            for (int j = 0; j < temp[0].length; j++) {
                double x = (i - midx) * Math.cos(rads) + (j - midy) * Math.sin(rads);
                double y = -(i - midx) * Math.sin(rads) + (j - midy) * Math.cos(rads);
                x = Math.round(x) + x0;
                y = Math.round(y) + y0;

                if (x >=0 && y >= 0 && x < inImage.length && y < inImage[0].length) {
                    temp[i][j] = inImage[(int)x][(int)y];
                }//else { temp[i][j] = 255;}
            }
        }
        int rotated[][] = imageUtils.getIntCastArray(matrixOperations.transposeMatrix(imageUtils.getDoubleCastArray(temp)));
        return rotated;
    }
}
