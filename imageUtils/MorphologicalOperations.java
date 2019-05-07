package com.avanseus.imageUtils;

import com.avanseus.avr.driver.Driver;
import com.avanseus.imageUtils.ImageUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.nio.Buffer;

/**
 * Created by yash on 12-04-2016.
 */
public class MorphologicalOperations {
    private ImageUtils imageUtils = new ImageUtils();

    public BufferedImage applyMorphologicalThinning(BufferedImage inImage) {
        BufferedImage thinImage = null;
        try {
            double[][] bwThinned = imageUtils.get2DImage(inImage);
            double[][] bwDeleted = imageUtils.getOnesImage(inImage);

            boolean flag = true;

            while (flag){
                flag = false;
                for(int col = 1; col < inImage.getWidth()-1; col++){
                    for(int row = 1; row < inImage.getHeight()-1; row++){
                        double[] pMask = get1DMask(bwThinned, col, row);
                        if (true == checkCondition1(pMask)){
                            int neighbor = 0;
                            for(int x = 1; x < 9; x++ ){
                                if(pMask[x] == 0 && pMask[x+1] == 1)
                                    neighbor++;
                            }
                            if (neighbor == 1){
                                bwDeleted[col][row] = 0;
                                flag = true;
                            }
                        }

                    }
                }
                bwThinned = imageUtils.multiplyElementWise2D(bwThinned, bwDeleted);

                for(int col = 1; col < inImage.getWidth()-1; col++){
                    for(int row = 1; row < inImage.getHeight()-1; row++){
                        double[] pMask = get1DMask(bwThinned, col, row);
                        if (true == checkCondition2(pMask)){
                            int neighbor = 0;
                            for(int x = 1; x < 9; x++ ){
                                if(pMask[x] == 0 && pMask[x+1] == 1)
                                    neighbor++;
                            }
                            if (neighbor == 1){
                                bwDeleted[col][row] = 0;
                                flag = true;
                            }
                        }

                    }
                }
                bwThinned = imageUtils.multiplyElementWise2D(bwThinned,bwDeleted);
            }
            thinImage = new BufferedImage(inImage.getWidth(),inImage.getHeight(),BufferedImage.TYPE_BYTE_GRAY);
            WritableRaster wRaster = thinImage.getRaster();
            for(int x = 0; x < wRaster.getWidth(); x++) {
                for (int y = 0; y < wRaster.getHeight(); y++) {
                    wRaster.setSample(x, y, 0, bwThinned[x][y]);
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return thinImage;
    }

    private boolean checkCondition1(double[] pMask){
        double sum = 0;
        boolean flag = false;
        for(int indx = 1; indx < 9; indx++){
            sum = sum + pMask[indx];
        }
        if((pMask[0] == 1) && (sum <= 6) && (sum >= 2) && ((pMask[1]*pMask[3]*pMask[5]) == 0) &&
                ((pMask[3]*pMask[5]*pMask[7]) == 0)){
            flag = true;
        }
        return flag;
    }

    private boolean checkCondition2(double[] pMask){
        double sum = 0;
        boolean flag = false;
        for(int indx = 1; indx < 9; indx++){
            sum = sum + pMask[indx];
        }
        if((pMask[0] == 1) && (sum <= 6) && (sum >= 2) && ((pMask[1]*pMask[3]*pMask[7]) == 0) &&
                ((pMask[1]*pMask[5]*pMask[7]) == 0)){
            flag = true;
        }
        return flag;
    }

    private double[] get1DMask(double[][] bwThinned, int col, int row){
        double[] pMask = new double[10];
        pMask[0] = bwThinned[col][row];
        pMask[1] = bwThinned[col][row-1];
        pMask[2] = bwThinned[col+1][row-1];
        pMask[3] = bwThinned[col+1][row];
        pMask[4] = bwThinned[col+1][row+1];
        pMask[5] = bwThinned[col][row+1];
        pMask[6] = bwThinned[col-1][row+1];
        pMask[7] = bwThinned[col-1][row];
        pMask[8] = bwThinned[col-1][row-1];
        pMask[9] = bwThinned[col][row-1];

        return  pMask;
    }

    public double[][] getStructuringElement(String shape, int size){
        BufferedImage inImage = new BufferedImage(size,size,BufferedImage.TYPE_BYTE_GRAY);
        double[][] onesImage = null;
        switch (shape) {
            case "disk":
                onesImage = imageUtils.getOnesImage(inImage);
                int r1,c1;
                // top left
                r1 = 0;c1 = 0;
                onesImage[r1][c1] = 0;
                r1 = 0; c1 = 1;
                onesImage[r1][c1] = 0;
                r1 = 1; c1 = 0;
                onesImage[r1][c1] = 0;
                // top right
                r1 = 0;c1 = onesImage[0].length;
                onesImage[r1][c1] = 0;
                r1 = 0; c1 = onesImage[0].length - 1;
                onesImage[r1][c1] = 0;
                r1 = 1; c1 = onesImage[0].length;
                onesImage[r1][c1] = 0;
                // bottom left
                r1 = onesImage.length ;c1 = 0;
                onesImage[r1][c1] = 0;
                r1 = onesImage.length ;c1 = 1;
                onesImage[r1][c1] = 0;
                r1 = onesImage.length-1 ;c1 = 0;
                onesImage[r1][c1] = 0;
                // bottom right
                r1 = onesImage.length ;c1 = onesImage[0].length;
                onesImage[r1][c1] = 0;
                r1 = onesImage.length ;c1 = onesImage[0].length - 1;
                onesImage[r1][c1] = 0;
                r1 = onesImage.length - 1 ;c1 = onesImage[0].length;
                onesImage[r1][c1] = 0;
                break;

            case "diamond":
                onesImage = imageUtils.getOnesImage(inImage);
                int mid = size/2;
                int dsize = 0;
                for(int x=0; x<mid; x++){
                    for(int y = 0; y< mid-dsize; y++) {
                        onesImage[x][y ] = 0;
                        onesImage[onesImage.length -1- x][y ] = 0;
                    }
                    dsize++;
                }
                dsize = 0;
                for(int x=0; x<mid; x++){
                    for(int y = onesImage[0].length-1; y> mid+dsize; y--) {
                        onesImage[x][y ] = 0;
                        onesImage[onesImage.length -1- x][y ] = 0;
                    }
                    dsize++;
                }
                break;
            default:
                onesImage = imageUtils.getOnesImage(inImage);
                break;
        }
        return onesImage;
    }

    public BufferedImage applyMorphologicalDilation(BufferedImage inImage, double[][] se) {
        int mR = se.length;
        int mC = se[0].length;
        int [] pMask = new int[2];
        pMask[0] = mR/2; pMask[1] = mC/2;
        double[][] paddedBmp = imageUtils.padImage(inImage,pMask,"replicate");
        double[][] dilatedImage = new double[inImage.getWidth()][inImage.getHeight()];
        boolean isGrayScaleImage = false;
        if(imageUtils.findMaximumGrayValue(inImage) > 2)
            isGrayScaleImage = true;

        for(int row = 0; row < inImage.getWidth(); row++){
            for(int col = 0; col < inImage.getHeight(); col++){
                BufferedImage croppedImage = imageUtils.getCroppedBufferedImage(
                        imageUtils.getBufferedImageFrom2D(paddedBmp),row,(row+mR),col,(col+mC));
                BufferedImage maskedImage = imageUtils.getBufferedImageFrom2D(
                        imageUtils.multiplyElementWise2D(imageUtils.get2DImage(croppedImage), se));

                if (isGrayScaleImage){
                    dilatedImage[row][col] = imageUtils.findMaximumGrayValue(maskedImage);
                }else {
                    if ( imageUtils.getSumOfMatrixFromRaster(maskedImage.getRaster()) > 0 )
                        dilatedImage[row][col] = 1;
                }
            }
        }
        return imageUtils.getBufferedImageFrom2D(dilatedImage);
    }

    public BufferedImage applyMorphologicalErosion(BufferedImage inImage, double[][] se) {
        int mR = se.length;
        int mC = se[0].length;
        int [] pMask = new int[2];
        pMask[0] = mR/2; pMask[1] = mC/2;
        double[][] paddedBmp = imageUtils.padImage(inImage,pMask,"replicate");
        double[][] erodedImage = new double[inImage.getWidth()][inImage.getHeight()];
        boolean isGrayScaleImage = false;
        if(imageUtils.findMaximumGrayValue(inImage) > 2)
            isGrayScaleImage = true;

        int[][] maskNonZero = imageUtils.findIndxIn2D(se,"==",0);

        for(int row = 0; row < inImage.getWidth(); row++){
            for(int col = 0; col < inImage.getHeight(); col++){
                BufferedImage croppedImage = imageUtils.getCroppedBufferedImage(
                        imageUtils.getBufferedImageFrom2D(paddedBmp),row,(row+mR),col,(col+mC));
                BufferedImage maskedImage = imageUtils.getBufferedImageFrom2D(
                        imageUtils.multiplyElementWise2D(imageUtils.get2DImage(croppedImage), se));

                if (isGrayScaleImage){
                    WritableRaster writableRaster = maskedImage.getRaster();
                    for(int x = 0; x < maskNonZero.length; x++){
                        writableRaster.setSample(x,1,0,255);
                    }
                    erodedImage[row][col] = imageUtils.findMinimumGrayValue(maskedImage);
                }else {
                    int nonZeroLength = (se.length * se[0].length) - maskNonZero.length;
                    int sumOfRaster = imageUtils.getSumOfMatrixFromRaster(maskedImage.getRaster());
                    //System.out.printf("Sum:\n"+sumOfRaster);
                    if ( sumOfRaster == nonZeroLength )
                        erodedImage[row][col] = 1;
                }
            }
        }
        return imageUtils.getBufferedImageFrom2D(erodedImage);
    }

    public BufferedImage applyMorphologicalClosing(BufferedImage inImage, double[][] se) {
        BufferedImage dilatedImage = applyMorphologicalDilation(inImage,se);
        imageUtils.getVisibleImageFromBufferedImageOfLogicalOnes(dilatedImage,"dilatedImage");
        BufferedImage erodedImage = applyMorphologicalErosion(dilatedImage, se);
        imageUtils.getVisibleImageFromBufferedImageOfLogicalOnes(erodedImage,"erodedImage");
        return erodedImage;
    }

    public BufferedImage applyMorphologicalOpening(BufferedImage inImage, double[][] se) {
        BufferedImage erodeddImage = applyMorphologicalErosion(inImage, se);
        return (applyMorphologicalDilation(erodeddImage, se));
    }
}
