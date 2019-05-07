package com.avanseus.avr.detection;

import com.avanseus.avr.model.AVRConstants;
import com.avanseus.imageUtils.EdgeDetection;
import com.avanseus.imageUtils.ImageUtils;
import com.avanseus.avr.model.CombinedImage;
import com.avanseus.imageUtils.SkewDetection;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.util.*;

/**
 * Created by hemanth on 19/4/16.
 */
public class FineTuneClustering {
    private ImageUtils imageUtils;
    private BufferedImage histEqualizedImage;
    private Raster histEqualizedRaster;

    public FineTuneClustering(BufferedImage h) {
        imageUtils = new ImageUtils();
        this.histEqualizedImage = h;
        histEqualizedRaster = histEqualizedImage.getRaster();
    }

    public List<CombinedImage> discardUnwantedPatches(double[][] labelImage) {
        SkewDetection skewDetection = new SkewDetection();
        double[] totalLabels = imageUtils.findUniqueIn2D(labelImage);
        if(totalLabels.length>0 && totalLabels[0] == 0) {
            totalLabels = Arrays.copyOfRange(totalLabels, 1, totalLabels.length);
        }
        List<CombinedImage> combinedImages = new ArrayList<>();

        for(int i=0;i<totalLabels.length;i++) {
            List<Integer> rowIndexes = new ArrayList<>();
            List<Integer> colIndexes = new ArrayList<>();
            for(int j=0;j<labelImage.length;j++) {
                for(int k=0;k<labelImage[0].length;k++) {
                    if(labelImage[j][k] == (int)totalLabels[i]) {
                        rowIndexes.add(j);
                        colIndexes.add(k);
                    }
                }
            }

            Collections.sort(rowIndexes);
            int xmin = rowIndexes.get(0)-3;
            int xmax = rowIndexes.get(rowIndexes.size()-1)+3;
            if (xmax >= histEqualizedRaster.getWidth()){
                xmax = histEqualizedRaster.getWidth()-1;
            }
            if (xmin <0)xmin = 0;

            Collections.sort(colIndexes);
            int ymin = colIndexes.get(0)-3;
            int ymax = colIndexes.get(colIndexes.size()-1)+3;
            if (ymax >= histEqualizedRaster.getHeight()){
                ymax = histEqualizedRaster.getHeight()-1;
            }
            if (ymin<0) ymin = 0;
            int[][] charImage = new int[xmax-xmin+1][ymax-ymin+1];
            int newRow = 0, newCol = 0;
            for(int ii=xmin;ii<=xmax;ii++) {
                for(int jj=ymin;jj<=ymax;jj++) {
                    charImage[newRow][newCol] = histEqualizedRaster.getSample(ii,jj,0);
                    newCol++;
                }
                newRow++;
                newCol = 0;
            }
            if(charImage.length*charImage[0].length > AVRConstants.MIN_PIXEL_AREA && charImage.length > charImage[0].length) {
                CombinedImage combinedImage = new CombinedImage();
                combinedImage.setImage(charImage);
                combinedImage.setXmin(xmin);
                combinedImage.setXmax(xmax);
                combinedImage.setYmin(ymin);
                combinedImage.setYmax(ymax);
                //combinedImage.setCroppedImage(colorImage);
                combinedImages.add(combinedImage);
            }
        }
        int i=1;
        FineTuneCropping fineTuneCropping = new FineTuneCropping();
        for(CombinedImage combinedImage : combinedImages) {
            String name = "LPC_Patches_" + i; i++;
            imageUtils.saveBufferedImage(imageUtils.getBufferedImageFrom2D(imageUtils.getDoubleCastArray(combinedImage.getImage())),name);
        }

        int k=1;
        List<CombinedImage> validCombinedImages = getValidPatchesOfBeingNumberPlate(combinedImages);
        List<CombinedImage> validImageList = new ArrayList<>();
        for(CombinedImage validImage:validCombinedImages) {
            int[][] skewedImage = skewDetection.performSkewDetection(validImage.getImage());
            int[][] croppedImage = fineTuneCropping.cropLPImage(skewedImage);
            double croppedImageAspectRatio = (croppedImage.length / croppedImage[0].length);
            System.out.println("Cropped Image Aspect Ratio: "+croppedImageAspectRatio);
            if (croppedImageAspectRatio >= AVRConstants.MIN_LP_ASPECT_THRESHOLD &&
                    croppedImageAspectRatio <= AVRConstants.MAX_LP_ASPECT_THRESHOLD) {
                CombinedImage combinedImage = new CombinedImage();
                validImage.setCroppedImage(croppedImage);
                combinedImage.setImage(validImage.getImage());
                combinedImage.setCroppedImage(croppedImage);
                combinedImage.setXmin(validImage.getXmin());
                combinedImage.setXmax(validImage.getXmax());
                combinedImage.setYmin(validImage.getYmin());
                combinedImage.setYmax(validImage.getYmax());

                String name = "LPC_Rotated_" + k;
                imageUtils.saveBufferedImage(imageUtils.getBufferedImageFrom2D(imageUtils.getDoubleCastArray(skewedImage)),name);
                name = "LPC_Rotated_Cropped_" + k; k++;
                imageUtils.saveBufferedImage(imageUtils.getBufferedImageFrom2D(imageUtils.getDoubleCastArray(croppedImage)),name);
                validImageList.add(combinedImage);
            }

        }
        return validImageList;

    }

    public List<CombinedImage> discardUnwantedLogoPatches(double[][] labelImage) {
        double[] totalLabels = imageUtils.findUniqueIn2D(labelImage);
        if(totalLabels.length>0 && totalLabels[0] == 0) {
            totalLabels = Arrays.copyOfRange(totalLabels, 1, totalLabels.length);
        }
        List<CombinedImage> combinedImages = new ArrayList<>();

        for(int i=0;i<totalLabels.length;i++) {
            List<Integer> rowIndexes = new ArrayList<>();
            List<Integer> colIndexes = new ArrayList<>();
            for(int j=0;j<labelImage.length;j++) {
                for(int k=0;k<labelImage[0].length;k++) {
                    if(labelImage[j][k] == (int)totalLabels[i]) {
                        rowIndexes.add(j);
                        colIndexes.add(k);
                    }
                }
            }

            Collections.sort(rowIndexes);
            int xmin = rowIndexes.get(0)-3;
            int xmax = rowIndexes.get(rowIndexes.size()-1)+3;
            if (xmax >= histEqualizedRaster.getWidth()){
                xmax = histEqualizedRaster.getWidth()-1;
            }
            if (xmin <0)xmin = 0;

            Collections.sort(colIndexes);
            int ymin = colIndexes.get(0)-3;
            int ymax = colIndexes.get(colIndexes.size()-1)+3;
            if (ymax >= histEqualizedRaster.getHeight()){
                ymax = histEqualizedRaster.getHeight()-1;
            }
            if (ymin<0) ymin = 0;
            int[][] charImage = new int[xmax-xmin+1][ymax-ymin+1];
            int newRow = 0, newCol = 0;
            for(int ii=xmin;ii<=xmax;ii++) {
                for(int jj=ymin;jj<=ymax;jj++) {
                    charImage[newRow][newCol] = histEqualizedRaster.getSample(ii,jj,0);
                    newCol++;
                }
                newRow++;
                newCol = 0;
            }

            CombinedImage combinedImage = new CombinedImage();
            combinedImage.setImage(charImage);
            combinedImage.setXmin(xmin);
            combinedImage.setXmax(xmax);
            combinedImage.setYmin(ymin);
            combinedImage.setYmax(ymax);
            combinedImage.setxCentroid((int)((xmax + xmin)/2.0));
            combinedImage.setyCentroid((int) ((ymax + ymin) / 2.0));
            combinedImages.add(combinedImage);

        }
        int imagexCentroid = labelImage.length/2;
        int imageyCentroid = labelImage[0].length/2;
        int i=1;
        FineTuneCropping fineTuneCropping = new FineTuneCropping();
        /*for(CombinedImage combinedImage : combinedImages) {
            String name = "Logo_Patches_" + i; i++;
            imageUtils.saveBufferedImage(imageUtils.getBufferedImageFrom2D(imageUtils.getDoubleCastArray(combinedImage.getImage())),name);
        }*/

        int k=1;
        //List<CombinedImage> validCombinedImages = getValidPatchesOfBeingNumberPlate(combinedImages);
        List<CombinedImage> validImageList = new ArrayList<>();
        for(CombinedImage validImage:combinedImages) {
            int xCentroid = validImage.getxCentroid();
            int yCentroid = validImage.getyCentroid();

            double position = ((double)xCentroid/imagexCentroid);
            if (position > 0.8 && position < 1.2) {
                CombinedImage combinedImage = new CombinedImage();
                int[][] croppedImage = fineTuneCropping.cropLogoImage(validImage.getImage());
                validImage.setCroppedImage(croppedImage);
                combinedImage.setImage(validImage.getImage());
                combinedImage.setCroppedImage(croppedImage);
                combinedImage.setXmin(validImage.getXmin());
                combinedImage.setXmax(validImage.getXmax());
                combinedImage.setYmin(validImage.getYmin());
                combinedImage.setYmax(validImage.getYmax());

                String name = "LOGO_Cropped_" + k; k++;
                imageUtils.saveBufferedImage(imageUtils.getBufferedImageFrom2D(imageUtils.getDoubleCastArray(croppedImage)),name);
                validImageList.add(combinedImage);
            }

        }
        return validImageList;

    }

    private List<CombinedImage> getValidPatchesOfBeingNumberPlate(List<CombinedImage> combinedImages) {
        List<CombinedImage> probablePatchesOfNumberPlate = new ArrayList<>();
        EdgeDetection edgeDetection = new EdgeDetection();
        for(CombinedImage tempImage: combinedImages) {
            BufferedImage bufferedImage = imageUtils.getBufferedImageFrom2D(tempImage.getImage());
            BufferedImage edgeImage = edgeDetection.getSobelEdgeImage(bufferedImage, "vertical",null);
            Raster rasterOfEdgeImage= edgeImage.getRaster();
            int sum = imageUtils.getSumOfMatrixFromRaster(rasterOfEdgeImage);
            double edgeDensity = (double) sum/(rasterOfEdgeImage.getHeight()*rasterOfEdgeImage.getWidth());
            double aspectThreshold = (double)rasterOfEdgeImage.getWidth()/rasterOfEdgeImage.getHeight();
            if(((edgeDensity > AVRConstants.MIN_EDGE_DENSITY) &&
                    ( aspectThreshold > AVRConstants.MIN_ASPECT_THRESHOLD))) {
                probablePatchesOfNumberPlate.add(tempImage);
            }
            System.out.println("Edge Density: "+ edgeDensity+ ", Aspect Ratio: "+ aspectThreshold);
        }
        return probablePatchesOfNumberPlate;
    }

}
