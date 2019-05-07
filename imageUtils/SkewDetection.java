package com.avanseus.imageUtils;
import com.avanseus.Dimension.PCA.Matrix;

/**
 * Created by hemanth on 21/4/16.
 */
public class SkewDetection {
    private HoughTransform houghTransform;
    private ImageRotate imageRotate;
    private Matrix matrixOperations;
    private ImageUtils imageUtils;
    public SkewDetection() {
        houghTransform = new HoughTransform();
        imageRotate = new ImageRotate();
        matrixOperations = new Matrix();
        imageUtils = new ImageUtils();
    }

    public int[][] performSkewDetection(int[][] inputImage) {

        double deskewAngle;
        int[][] inImage = imageUtils.getIntCastArray(matrixOperations.transposeMatrix
                (imageUtils.getDoubleCastArray(inputImage)));
        double skew = houghTransform.getObjectAngle(inImage);
        if (skew < 0) {
            deskewAngle = skew + ((2 * Math.abs(skew)) - 1);
        } else if (skew > 0) {
            deskewAngle = skew - ((2 * Math.abs(skew)) - 1);
        } else {
            deskewAngle = 0;
        }
        System.out.println("Image Angle is: " + skew + ", Deskewed by Angle: "+ deskewAngle);

        return imageRotate.RotateImageUsingNearestNeighbor(deskewAngle,inImage);
    }


}
