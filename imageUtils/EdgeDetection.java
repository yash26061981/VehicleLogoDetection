package com.avanseus.imageUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;

public class EdgeDetection {
    private ImageUtils imageUtils;

    public EdgeDetection() {
        imageUtils = new ImageUtils();
    }

    public BufferedImage getSobelEdgeImage(BufferedImage grayImage, String direction, Double threshold) {
        BufferedImage edgeImage = null;
        try {
            Raster grayRaster = grayImage.getRaster();

            int xDir = 0, yDir = 0;
            switch (direction) {
                case "vertical":
                    xDir = 1;
                    break;
                case "horizontal":
                    yDir = 1;
                    break;
                default:
                    xDir = 1;
                    yDir = 1;
                    break;
            }
            double[][] inImage = preProcessImage(grayImage);
            double[][] edgeGradientPadded = findEdgeGradientImage(inImage, xDir, yDir);
            int[] padMask = {1,1};
            double[][] edgeGradient = imageUtils.removePaddedPartFromImage(edgeGradientPadded,padMask);
            double cutOffThreshold;
            if (null == threshold){
                cutOffThreshold = computeThreshold(edgeGradient);
            }else {
                cutOffThreshold = threshold;
            }

            edgeImage = new BufferedImage(grayImage.getWidth(),grayImage.getHeight(),BufferedImage.TYPE_BYTE_GRAY);
            WritableRaster raster = edgeImage.getRaster();

            for(int x = 0; x < grayRaster.getWidth(); x++) {
                for (int y = 0; y < grayRaster.getHeight(); y++) {
                    if (edgeGradient[x][y] >= cutOffThreshold) {
                        raster.setSample(x, y, 0, 1);
                    }
                    else {
                        raster.setSample(x, y, 0, 0);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return edgeImage;
    }

    private double[][] preProcessImage(BufferedImage grayImage){
        int[] pMask = {1,1};
        double[][] paddedImage = imageUtils.padImage(grayImage, pMask, "replicate");
        for(int col = 0; col < paddedImage.length; col++){
            for(int row = 0; row < paddedImage[0].length; row++){
                paddedImage[col][row] = paddedImage[col][row]/255.0;
            }
        }
        return paddedImage;
    }

    private double[][] findEdgeGradientImage(double[][] inImage, int scaleX, int scaleY){
        double[][] maskX = {{1, 0, -1}, {2, 0, -2},{1, 0, -1}};
        double[][] maskY = {{1, 2, 1}, {0, 0, 0}, {-1, -2, -1}};
        double gradX, gradY;
        double[][] pixelGrad = new double[inImage.length][inImage[0].length];

        for (int x = 0; x < inImage.length-2 ; x++)
            for (int y = 0; y < inImage[0].length-2; y++) {
                gradX = 0.0;
                gradY = 0.0;
                gradX = gradX +
                        inImage[x][y] * maskX[0][0]/8 + inImage[x+1][y] * maskX[0][1]/8 + inImage[x+2][y] * maskX[0][2]/8 +
                        inImage[x][y+1] * maskX[1][0]/8 + inImage[x+1][y+1] * maskX[1][1]/8 + inImage[x+2][y+1] * maskX[1][2]/8+
                        inImage[x][y+2] * maskX[2][0]/8 + inImage[x+1][y+2] * maskX[2][1]/8 + inImage[x+2][y+2] * maskX[2][2]/8;

                gradY = gradY +
                        inImage[x][y] * maskY[0][0]/8 + inImage[x+1][y] * maskY[0][1]/8 + inImage[x+2][y] * maskY[0][2]/8 +
                        inImage[x][y+1] * maskY[1][0]/8 + inImage[x+1][y+1] * maskY[1][1]/8 + inImage[x+2][y+1] * maskY[1][2]/8+
                        inImage[x][y+2] * maskY[2][0]/8 + inImage[x+1][y+2] * maskY[2][1]/8 + inImage[x+2][y+2] * maskY[2][2]/8;

                pixelGrad[x+1][y+1] = ((scaleX) * gradX * gradX + scaleY * gradY * gradY);
            }
        return pixelGrad;
    }

    private double computeThreshold(double[][] edgeGradient){
        double edgeGradSum = 0;
        for (int x = 0; x < edgeGradient.length ; x++)
            for (int y = 0; y < edgeGradient[0].length; y++) {
                edgeGradSum += (edgeGradient[x][y])/(edgeGradient.length *edgeGradient[0].length);
            }
        return (4*edgeGradSum);
    }

    public BufferedImage getBackgroundTextureSuppressedEdgeImage(BufferedImage croppedImage, Double threshold){
        EdgeDetection edgeDetection = new EdgeDetection();
        BufferedImage horizontalEdgeImage = edgeDetection.getSobelEdgeImage(croppedImage,"horizontal",threshold);
        BufferedImage verticalEdgeImage = edgeDetection.getSobelEdgeImage(croppedImage,"vertical",threshold);
        double sumOfHorizontalEdges = imageUtils.getSumOfMatrixFromRaster(horizontalEdgeImage.getRaster());
        double sumOfVerticalEdges = imageUtils.getSumOfMatrixFromRaster(verticalEdgeImage.getRaster());
        if (sumOfHorizontalEdges > sumOfVerticalEdges)
            return verticalEdgeImage;
        else
            return horizontalEdgeImage;

    }
}
