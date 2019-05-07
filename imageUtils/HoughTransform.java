package com.avanseus.imageUtils;

import com.avanseus.avr.model.AVRConstants;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;

/**
 * Created by yash on 08-05-2016.
 */
public class HoughTransform {
    private ImageUtils imageUtils;
    public HoughTransform() {
        imageUtils = new ImageUtils();
    }


    public double getObjectAngle(int[][] inImage) {
        EdgeDetection edgeDetection = new EdgeDetection();

        BufferedImage bufferedImage = imageUtils.getBufferedImageFrom2D(inImage);
        BufferedImage edgeImage = edgeDetection.getSobelEdgeImage(bufferedImage, "horizontal",null);
        Raster edgeImageRaster = edgeImage.getRaster();
        int rows = inImage.length; //220
        int cols = inImage[0].length; //29

        double diagonal = Math.sqrt(Math.pow(rows - 1, 2) + Math.pow(cols - 1, 2));
        int nrho = (int) (2 * (Math.ceil(diagonal / AVRConstants.rhoResolution)) + 1);

        int stAngle = -90;
        int edAngle = 89;
        int totalAngleLen = edAngle - stAngle + 1;
        double[] theta = new double[(int) Math.ceil(totalAngleLen / AVRConstants.thetaResolution)];

        theta[0] = stAngle;
        for (int indx = 1; indx < theta.length; indx++) {
            theta[indx] = theta[indx - 1] + AVRConstants.thetaResolution;
        }

        double[][] houghTable = new double[nrho][theta.length];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if ( edgeImageRaster.getSample(i, j, 0) == 1 ) {
                    //System.out.println("val:"+edgeImageRaster.getSample(j,i,0));
                    for (int ang = 0; ang < theta.length; ang++) {
                        int rho = (int) ((diagonal + i * Math.cos((theta[ang] * Math.PI / 180)) +
                                            j * Math.sin((theta[ang] * Math.PI / 180))) + 0.5 + 1);
                        if (rho > nrho-1){rho = nrho-1;}
                        houghTable[rho][ang] = houghTable[rho][ang] + 1;
                    }
                }
            }
        }
        double max = 0;
        int idx = 0, idy = 0;
        for (int i = 0; i < houghTable.length; i++) {
            for (int j = 0; j < houghTable[0].length; j++) {
                if ( houghTable[i][j] > max ) {
                    max = houghTable[i][j];
                    idx = i;
                    idy = j;
                }
            }
        }

        return (theta[idy]);
    }
}
