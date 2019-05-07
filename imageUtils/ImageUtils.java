package com.avanseus.imageUtils;


import com.avanseus.avr.driver.Driver;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ImageUtils {

    public double[][] get2DImage(BufferedImage inImage){
        double[][] in2DImage = new double[inImage.getWidth()][inImage.getHeight()];
        Raster readRaster = inImage.getRaster();
        for(int col = 0; col < inImage.getWidth(); col++){
            for(int row = 0; row < inImage.getHeight(); row++){
                in2DImage[col][row] =  readRaster.getSample(col,row,0);
            }
        }
        return in2DImage;
    }

    public int[][] get2DImageInteger(BufferedImage inImage){
        int[][] in2DImage = new int[inImage.getWidth()][inImage.getHeight()];
        Raster readRaster = inImage.getRaster();
        for(int col = 0; col < inImage.getWidth(); col++){
            for(int row = 0; row < inImage.getHeight(); row++){
                in2DImage[col][row] =  readRaster.getSample(col,row,0);
            }
        }
        return in2DImage;
    }

    public int[][] getLogical2DImage(BufferedImage inImage){
        int[][] in2DImage = new int[inImage.getWidth()][inImage.getHeight()];
        Raster readRaster = inImage.getRaster();
        for(int col = 0; col < inImage.getWidth(); col++){
            for(int row = 0; row < inImage.getHeight(); row++) {
                if(readRaster.getSample(col,row,0) <= 230) {
                    in2DImage[col][row] =  0;
                } else {
                    in2DImage[col][row] = 1;
                }

            }
        }
        return in2DImage;
    }

    public double[][] getLogical2DImageDouble(BufferedImage inImage){
        double[][] in2DImage = new double[inImage.getWidth()][inImage.getHeight()];
        Raster readRaster = inImage.getRaster();
        for(int col = 0; col < inImage.getWidth(); col++){
            for(int row = 0; row < inImage.getHeight(); row++) {
                if(readRaster.getSample(col,row,0) <= 200) {
                    in2DImage[col][row] =  0;
                } else {
                    in2DImage[col][row] = 1;
                }

            }
        }
        return in2DImage;
    }

    public BufferedImage getBufferedImageFrom2D(double[][] inImage){
        BufferedImage outImage = new BufferedImage(inImage.length, inImage[0].length, BufferedImage.TYPE_BYTE_GRAY);
        WritableRaster writableRaster = outImage.getRaster();
        for(int col = 0; col < inImage.length; col++){
            for(int row = 0; row < inImage[0].length; row++){
                writableRaster.setSample(col,row,0,inImage[col][row]);
            }
        }
        return outImage;
    }

    public BufferedImage getBufferedImageFrom2D(int[][] inImage){
        BufferedImage outImage = new BufferedImage(inImage.length, inImage[0].length, BufferedImage.TYPE_BYTE_GRAY);
        WritableRaster writableRaster = outImage.getRaster();
        for(int col = 0; col < inImage.length; col++){
            for(int row = 0; row < inImage[0].length; row++){
                writableRaster.setSample(col,row,0,inImage[col][row]);
            }
        }
        return outImage;
    }

    public double[][] getOnesImage(BufferedImage inImage){
        double[][] onesImage = new double[inImage.getWidth()][inImage.getHeight()];
        for(int col = 0; col < inImage.getWidth(); col++){
            for(int row = 0; row < inImage.getHeight(); row++){
                onesImage[col][row] =  1;
            }
        }
        return onesImage;
    }

    public int[][] getOnesImageInteger(BufferedImage inImage){
        int[][] onesImage = new int[inImage.getWidth()][inImage.getHeight()];
        for(int col = 0; col < inImage.getWidth(); col++){
            for(int row = 0; row < inImage.getHeight(); row++){
                onesImage[col][row] =  1;
            }
        }
        return onesImage;
    }

    public BufferedImage getSquaredImage(BufferedImage inImage){
        BufferedImage squaredImage = new BufferedImage(inImage.getWidth(),inImage.getHeight(),BufferedImage.TYPE_BYTE_GRAY);
        WritableRaster writableRaster = squaredImage.getRaster();
        Raster readRaster = inImage.getRaster();
        for(int col = 0; col < inImage.getWidth(); col++){
            for(int row = 0; row < inImage.getHeight(); row++){
                writableRaster.setSample(col,row,0,Math.pow(readRaster.getSample(col,row,0),2));
            }
        }
        return squaredImage;
    }

    public int findMinimumGrayValue(BufferedImage inImage){
        Raster grayRaster = inImage.getRaster();
        int minGrayVal = 256;
        for (int col =0; col < inImage.getWidth(); col++) {
            for (int row = 0; row < inImage.getHeight(); row++) {
                int pixelVal = grayRaster.getSample(col, row, 0);
                if (pixelVal < minGrayVal) {
                    minGrayVal = pixelVal;
                }
            }
        }
        return minGrayVal;
    }

    public int findMaximumGrayValue(BufferedImage inImage){
        Raster grayRaster = inImage.getRaster();
        int maxGrayVal = 0;
        for (int col =0; col < inImage.getWidth(); col++) {
            for (int row = 0; row < inImage.getHeight(); row++) {
                int pixelVal = grayRaster.getSample(col, row, 0);
                if (pixelVal > maxGrayVal) {
                    maxGrayVal = pixelVal;
                }
            }
        }
        return maxGrayVal;
    }

    public double[][] padImage(BufferedImage inImage, int[] pMask, String option){
        double[][] paddedImage = new double[inImage.getWidth() + 2*pMask[1]][inImage.getHeight() + 2*pMask[0]];
        Raster grayRaster = inImage.getRaster();
        boolean isZero = false,isSquared = false;
        switch (option){
            case "zeros":
                isZero = true;
                break;
            case "square":
                isSquared = true;
                break;
            default:
                isZero = false;
                isSquared = false;
                break;
        }
        for (int col =0; col < inImage.getWidth(); col++) {
            for (int row = 0; row < inImage.getHeight(); row++) {
                int cornerEdgeVal = grayRaster.getSample(col, row, 0);
                if ( isSquared )
                    cornerEdgeVal = (int) Math.pow(cornerEdgeVal, 2);
                paddedImage[col + pMask[0]][row + pMask[1]] = cornerEdgeVal;
            }
        }
        if (!isZero) {
            //get the top rows
            int[] limit = {-1};
            double[] startRow = getAllElementOfRowCol(paddedImage, false, pMask[0], limit);
            double stVal = startRow[pMask[1]];
            double edVal = startRow[startRow.length - 1 - pMask[1]];
            for (int ii = 0, jj = startRow.length - pMask[1]; ii < pMask[1] && jj < startRow.length; ii++, jj++) {
                startRow[ii] = stVal;
                startRow[jj] = edVal;
            }
            double[] endRow = getAllElementOfRowCol(paddedImage, false, (paddedImage.length - pMask[0] - 1), limit);
            stVal = endRow[pMask[1]];
            edVal = endRow[endRow.length - 1 - pMask[1]];
            for (int ii = 0, jj = endRow.length - pMask[1]; ii < pMask[1] && jj < endRow.length; ii++, jj++) {
                endRow[ii] = stVal;
                endRow[jj] = edVal;
            }
            for (int ii = 0, jj = paddedImage.length - pMask[0]; ii < pMask[0] && jj < paddedImage.length; ii++, jj++) {
                for (int kk = 0; kk < paddedImage[0].length; kk++) {
                    paddedImage[ii][kk] = startRow[kk];
                    paddedImage[jj][kk] = endRow[kk];
                }
            }
            double[] startCol = getAllElementOfRowCol(paddedImage, true, pMask[1], limit);
            double[] endCol = getAllElementOfRowCol(paddedImage, true, (paddedImage[0].length - pMask[1] - 1), limit);
            for (int ii = 0, jj = paddedImage[0].length - pMask[1]; ii < pMask[1] && jj < paddedImage[0].length; ii++, jj++) {
                for (int kk = 0; kk < paddedImage.length; kk++) {
                    paddedImage[kk][ii] = startCol[kk];
                    paddedImage[kk][jj] = endCol[kk];
                }
            }
        }
        return paddedImage;
    }


    public double[][] removePaddedPartFromImage(double[][] paddedImage, int[] mask){
        double[][] origImage = new double[paddedImage.length - 2*mask[0]][paddedImage[0].length - 2*mask[1]];
        for(int col = 1;col<paddedImage.length-1; col++){
            for(int row = 1; row < paddedImage[0].length-1; row++){
                origImage[col-1][row-1] = paddedImage[col][row];
            }
        }
        return origImage;
    }


    public double[] findUnique(double[] arrVal1){
        double[] arrVal = new double[arrVal1.length];
        arrVal= arrVal1.clone();
        Arrays.sort(arrVal);
        List<Double> uniqueValList = new ArrayList<>();

        uniqueValList.add(arrVal[0]);
        for (int indx =1; indx < arrVal.length; indx++){
            double diff = arrVal[indx] - arrVal[indx-1];
            if (diff != 0) {
                uniqueValList.add(arrVal[indx]);
            }
        }
        double[] uniqueVal = new double[uniqueValList.size()];
        for(int id = 0; id < uniqueValList.size(); id++){
            uniqueVal[id] = uniqueValList.get(id);
        }
        return uniqueVal;
    }

    public double[] getAllElementOfRowCol(double[][] inData, boolean isRow, int numbRowCol, int[] limit){
        double[] outData = null;
        if (limit[0] < 0){
            if (isRow){
                int colLen = inData.length;
                outData = new double[colLen];
                for(int col=0; col <colLen; col++){
                    outData[col] = inData[col][numbRowCol];
                }
            }
            else {
                int rowLen = inData[numbRowCol].length;
                outData = new double[rowLen];
                for(int row=0; row <rowLen; row++){
                    outData[row] = inData[numbRowCol][row];
                }
            }
        }
        else{
            if (isRow){
                int colLen = limit[1] - limit[0] + 1;
                outData = new double[colLen];
                for(int col=limit[0]; col <= limit[1]; col++){
                    outData[col] = inData[col][numbRowCol];
                }
            }
            else {
                int rowLen = limit[1] - limit[0] + 1;
                outData = new double[rowLen];
                for(int row=limit[0]; row <= limit[1]; row++){
                    outData[row] = inData[numbRowCol][row];
                }
            }
        }
        return outData;
    }

    public double[][] getEntireRowColElements(double[][] inData, boolean allRow, int[] limit){
        double[][] outData = null;
        if (allRow) {
            int colLen = limit[1] - limit[0] + 1;
            outData = new double[colLen][inData[0].length];
            for(int col = limit[0]; col <= limit[1]; col++) {
                for (int row = 0; row < inData[0].length; row++) {
                    outData[col-limit[0]][row] = inData[col][row];
                }
            }
        }
        else {
            int rowLen = limit[1] - limit[0] + 1;
            outData = new double[inData.length][rowLen];
            for(int col = 0; col < inData.length ; col++) {
                for (int row = limit[0]; row <= limit[1]; row++) {
                    outData[col][row-limit[0]] = inData[col][row];
                }
            }
        }
        return outData;
    }

    public boolean isMember(double val1, double[] arrVal){
        boolean flag = false;
        for(int indx = 0; indx < arrVal.length; indx++){
            if(arrVal[indx] == val1)
                flag = true;
        }
        return flag;
    }


    public double[] findUniqueIn2D(double[][] arrVal){
        int rowLen = arrVal[0].length;
        int colLen = arrVal.length;
        List<Double> totalUnique = new ArrayList<>();
        for(int indx1 = 0; indx1 <colLen; indx1++){
            int[] limit = {-1};
            boolean isRow = false;
            double[] rowValues = getAllElementOfRowCol(arrVal, isRow, indx1, limit);
            double[] uniqRowVal = findUnique(rowValues);
            for(int indx2 = 0; indx2 < uniqRowVal.length; indx2++){
                totalUnique.add(uniqRowVal[indx2]);
            }
        }
        double[] allRowUniq = new double[totalUnique.size()];
        for(int indx1 = 0; indx1 <totalUnique.size(); indx1++){
            allRowUniq[indx1] = totalUnique.get(indx1);
        }
        return findUnique(allRowUniq);
    }

    public double[] shiftArrayLeft(double[] arrVal){
        double[] newArrayVal = new double[arrVal.length-1];
        for(int indx = 0; indx < newArrayVal.length; indx++){
            newArrayVal[indx] = arrVal[indx+1];
        }
        return  newArrayVal;
    }

    private boolean checkOperatorCondition(double arrVal, String operation, double val){
        boolean result = false;
        switch (operation){
            case "==":
                if (arrVal == val ){
                    result = true;
                }
                break;
            case ">=":
                if (arrVal >= val ){
                    result = true;
                }
                break;
            case ">":
                if (arrVal > val ){
                    result = true;
                }
                break;
            case "<=":
                if (arrVal <= val ){
                    result = true;
                }
                break;
            case "<":
                if (arrVal < val ){
                    result = true;
                }
                break;
            case "!=":
                if (arrVal != val ){
                    result = true;
                }
                break;

            default:
                result = true;
                break;
        }
        return result;
    }
    public int[] findIndx(double[] arrVal, String operation, double val){
        List<Integer> totalQualified = new ArrayList<>();
        for(int indx = 0; indx < arrVal.length; indx++){
            if (checkOperatorCondition(arrVal[indx],operation, val)){
                totalQualified.add(indx);
            }
        }
        int[] validIndx = new int[totalQualified.size()];
        for(int indx1 = 0; indx1 <totalQualified.size(); indx1++){
            validIndx[indx1] = totalQualified.get(indx1);
        }
        return validIndx;
    }

    public int[][] findIndxIn2D(double[][] arrVal, String operation, double val){
        List<Integer> rows = new ArrayList<>();
        List<Integer> cols = new ArrayList<>();

        for(int col = 0; col < arrVal.length; col++){
            for(int row = 0; row < arrVal[0].length; row++){
                if (checkOperatorCondition(arrVal[col][row],operation, val)) {
                    rows.add(row); cols.add(col);
                }
            }
        }
        int[][] validIndx = new int[cols.size()][2];
        for(int indx1 = 0; indx1 <cols.size(); indx1++){
            validIndx[indx1][0] = cols.get(indx1);
            validIndx[indx1][1] = rows.get(indx1);
        }
        return validIndx;
    }

    public void copy2D(double[][] inImage, double[][] outImage){
        for(int col = 0; col < inImage.length; col++){
            for(int row = 0; row < inImage[0].length; row++){
                outImage[col][row] = inImage[col][row];
            }
        }
    }

    public double[] getArrayFromList(List<Integer> doneLabel){
        double[] arr = new double[doneLabel.size()];
        for(int indx = 0; indx < doneLabel.size(); indx++) {
            arr[indx] = doneLabel.get(indx);
        }
        return arr;
    }

    public double[][] multiplyElementWise2D(double[][] inImage1, double[][] inImage2){
        double[][] outImag = new double[inImage1.length][inImage1[0].length];
        for(int col=0; col < inImage1.length; col++){
            for(int row=0; row < inImage1[0].length; row++){
                outImag[col][row] = inImage1[col][row] * inImage2[col][row];
            }
        }
        return outImag;
    }

    public double[] multiplyElementWise1D(double[] inImage1, double[] inImage2){
        double[] outImag = new double[inImage1.length];
        for(int indx = 0; indx < inImage1.length; indx++) {
            outImag[indx] = inImage1[indx] * inImage2[indx];
        }
        return outImag;
    }

    public double[][] divideElementWise2D(double[][] inImage1, double[][] inImage2){
        double[][] outImag = new double[inImage1.length][inImage1[0].length];
        for(int col=0; col < inImage1.length; col++){
            for(int row=0; row < inImage1[0].length; row++){
                outImag[col][row] = inImage1[col][row] / inImage2[col][row];
            }
        }
        return outImag;
    }

    public double[] divideElementWise1D(double[] inImage1, double[] inImage2){
        double[] outImag = new double[inImage1.length];
        for(int indx = 0; indx < inImage1.length; indx++) {
            outImag[indx] = inImage1[indx] / inImage2[indx];
        }
        return outImag;
    }


    public double[] sum2DRowColWise(double[][] inImage, int sumDir){
        int cols = inImage.length;
        int rows = inImage[0].length;
        double[] sum;
        if (sumDir == 1) {
            sum = new double[rows];
            int[] limit = {-1};
            for(int indx = 0; indx < rows; indx++){
                sum[indx] = sumOfArray(getAllElementOfRowCol(inImage, true, indx, limit));
            }
        }else{
            sum = new double[cols];
            int[] limit = {-1};
            for(int indx = 0; indx < cols; indx++){
                sum[indx] = sumOfArray(getAllElementOfRowCol(inImage, false, indx, limit));
            }
        }
        return sum;
    }

    public double sumOfArray(double[] arrImage){
        double sum = 0;
        for(int indx =0; indx < arrImage.length; indx++){
            sum = sum+arrImage[indx];
        }
        return sum;
    }

    public double[] getDoubleCastArray(int[] Arr){
        double[] newArr = new double[Arr.length];
        for(int indx=0; indx < Arr.length; indx++)
            newArr[indx] = (double)Arr[indx];

        return newArr;
    }
    public double[][] getDoubleCastArray(int[][] Arr){
        double[][] newArr = new double[Arr.length][Arr[0].length];
        for(int indx=0; indx < Arr.length; indx++){
            for(int indx1=0; indx1 < Arr[0].length; indx1++) {
                newArr[indx][indx1] = (double) Arr[indx][indx1];
            }
        }
        return newArr;
    }

    public int[] getIntCastArray(double[] Arr){
        int[] newArr = new int[Arr.length];
        for(int indx=0; indx < Arr.length; indx++)
            newArr[indx] = (int)Arr[indx];

        return newArr;
    }
    public int[][] getIntCastArray(double[][] Arr){
        int[][] newArr = new int[Arr.length][Arr[0].length];
        for(int indx=0; indx < Arr.length; indx++){
            for(int indx1=0; indx1 < Arr[0].length; indx1++) {
                newArr[indx][indx1] = (int) Arr[indx][indx1];
            }
        }
        return newArr;
    }


    public void getVisibleImageFromBufferedImageOfLogicalOnes(BufferedImage bufferedImage, String destFileName) {
        BufferedImage visibleImage = new BufferedImage(bufferedImage.getWidth(),bufferedImage.getHeight(),BufferedImage.TYPE_BYTE_GRAY);;
        Raster raster = bufferedImage.getRaster();
        WritableRaster writableRaster = visibleImage.getRaster();
        for(int i=0;i<raster.getWidth();i++) {
            for(int j=0;j<raster.getHeight();j++) {
                int sampleValue = raster.getSample(i,j,0);
                if(sampleValue > 0)
                    sampleValue = 255;
                writableRaster.setSample(i,j,0,sampleValue);
            }
        }
        File outputDir = new File(Driver.destDir+File.separator+destFileName+".jpg");
        try {
            ImageIO.write(visibleImage, "jpg", outputDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveBufferedImage(BufferedImage bufferedImage, String destFileName) {
        File outputDir = new File(Driver.destDir+File.separator+destFileName+".jpg");
        try {
            ImageIO.write(bufferedImage, "jpg", outputDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getSumOfMatrixFromRaster(Raster raster) {
        int sum = 0;
        for(int i=0;i<raster.getWidth();i++) {
            for(int j=0;j<raster.getHeight();j++)  {
                //System.out.println("val:"+raster.getSample(i,j,0));
                sum+=raster.getSample(i,j,0);
            }
        }
        return sum;
    }

    public BufferedImage negateBitMap(BufferedImage inImage){
        BufferedImage outImage = new BufferedImage(inImage.getWidth(), inImage.getHeight(),BufferedImage.TYPE_BYTE_GRAY);
        WritableRaster writableRaster = outImage.getRaster();
        Raster readRaster = inImage.getRaster();

        for (int col =0; col < inImage.getWidth(); col++) {
            for (int row = 0; row < inImage.getHeight(); row++) {
                writableRaster.setSample(col, row, 0, (1 - readRaster.getSample(col, row, 0)));
            }
        }
        return outImage;
    }

    public BufferedImage getRowColInterchangedImage(BufferedImage inImage){
        BufferedImage outImage = new BufferedImage(inImage.getHeight(),inImage.getWidth(),BufferedImage.TYPE_BYTE_GRAY);
        WritableRaster writableRaster = outImage.getRaster();
        Raster readRaster = inImage.getRaster();

        for (int col =0; col < inImage.getWidth(); col++) {
            for (int row = 0; row < inImage.getHeight(); row++) {
                writableRaster.setSample(row, col, 0, readRaster.getSample(col, row, 0));
            }
        }
        return outImage;
    }
    public double[][] getRowColInterchangedImage(double[][] inImage){
        double[][] outImage = new double[inImage[0].length][inImage.length];

        for (int col =0; col < inImage.length; col++) {
            for (int row = 0; row < inImage[0].length; row++) {
                outImage[row][col] = inImage[col][row];
            }
        }
        return outImage;
    }

    public double findMinimum(double[] inArr){
        double min = 655356;
        for(int indx=0; indx < inArr.length; indx++){
            if (inArr[indx] < min)
                min = inArr[indx];
        }
        return min;
    }
    public double findMaximum(double[] inArr){
        double max = 0;
        for(int indx=0; indx < inArr.length; indx++){
            if (inArr[indx] > max)
                max = inArr[indx];
        }
        return max;
    }

    public int[] flatten2DArray(int[][] arr) {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr[i].length; j++) {
                list.add(arr[i][j]);
            }
        }

        int[] vector = new int[list.size()];
        for (int i = 0; i < vector.length; i++) {
            vector[i] = list.get(i);
        }
        return vector;
    }

    public double getLowerUpperBoundedValue(double value, double minVal, double maxVal){
        double []arr1 = new double[2];
        arr1[0] = value; arr1[1] = minVal;
        arr1[0] = findMaximum(arr1);
        arr1[1] = maxVal;
        return findMinimum(arr1);
    }

    public BufferedImage getCroppedBufferedImage(BufferedImage inImage, int xMin, int xMax, int yMin, int yMax){
        BufferedImage outImage = new BufferedImage((xMax-xMin),(yMax-yMin),BufferedImage.TYPE_BYTE_GRAY);
        WritableRaster writableRaster = outImage.getRaster();
        Raster readRaster = inImage.getRaster();

        for (int col =xMin; col < xMax; col++) {
            for (int row = yMin; row < yMax; row++) {
                writableRaster.setSample(col-xMin, row-yMin, 0, (readRaster.getSample(col, row, 0)));
            }
        }
        return outImage;
    }

    public static void main(String[] args) {
        double[][] mask = { {8, 1, 6, 9},
                            {3, 5, 7, 4},
                            {4, 9, 1, 5}};

        int[] pmask = {1,1};
        ImageUtils imageUtils = new ImageUtils();
        int[] limit = {-1};
        double[] f1row = imageUtils.getAllElementOfRowCol(mask,false,1,limit);
        double[][] padIm = imageUtils.padImage(imageUtils.getBufferedImageFrom2D(mask),pmask,"replicate");
        System.out.println("Done");

    }
}
