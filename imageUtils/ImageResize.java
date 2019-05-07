package com.avanseus.imageUtils;

import com.avanseus.avr.driver.Driver;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

/**
 * Created by yash on 26-04-2016.
 */
public class ImageResize {

    public double[][] ResizeImageUsingNearestNeighbor(double[][] inImage, double[] scale){
        int oldRows = inImage[0].length;
        int oldCols = inImage.length;
        int newRows = (int)Math.floor(oldRows * scale[0]);
        int newCols = (int)Math.floor(oldCols * scale[1]);

        int[] rowIndx = new int[newRows];
        int[] colIndx = new int[newCols];
        for(int indx = 0; indx < newRows; indx++){
            rowIndx[indx] = (int)Math.min(((indx + 0.5)/scale[0]) + 0.5, (oldRows-1));
        }
        for(int indx = 0; indx < newCols; indx++){
            colIndx[indx] = (int)Math.min(((indx + 0.5)/scale[1]) + 0.5, (oldCols-1));
        }
        double[][] outImage = new double[newCols][newRows];
        for(int c= 0; c < newCols; c++){
            for(int r=0; r < newRows; r++){
                outImage[c][r] = inImage[colIndx[c]][rowIndx[r]];
            }
        }
        return outImage;
    }

    public double[] GetScalingRatioFromSize(int[] oldSize, int[] newSize){
        double[] scale = new double[2];
        //int[] oldSize = {thImage.getHeight(),thImage.getWidth()};
        //int[] newSize = {30,20};
        // first row, second col
        scale[0] = newSize[0]/((double)oldSize[0]);
        scale[1] = newSize[1]/((double)oldSize[1]);
        return scale;
    }

    static public void main(String args[]) throws Exception
    {
        ImageResize imageResize = new ImageResize();
        ImageUtils imageUtils = new ImageUtils();

        File parentFolder = new File("/home/hemanth/Downloads/thickTest");
        File[] listOfSubDir = parentFolder.listFiles();
        for(File dir : listOfSubDir) {
            if(dir.isDirectory()) {
                //File folder = new File("/home/hemanth/Downloads/thickTest/A");
                File[] listOfFiles = dir.listFiles();
                for (File file : listOfFiles) {
                    System.out.println("A");
                    BufferedImage thImage = ImageIO.read(file);
                    int[] oldSize = {thImage.getHeight(),thImage.getWidth()};
                    int[] newSize = {22,18};
                    double[] scaling = imageResize.GetScalingRatioFromSize(oldSize,newSize);
                    double[][] outImage = imageResize.ResizeImageUsingNearestNeighbor(imageUtils.get2DImage(thImage), scaling);
                    int[] padMask = {1,1};
                    double[][] paddedResizedImg = imageUtils.padImage(imageUtils.getBufferedImageFrom2D(outImage), padMask, "zeros");
                    BufferedImage bufferedImage = imageUtils.getBufferedImageFrom2D(paddedResizedImg);
                    BufferedImage visibleImage = new BufferedImage(bufferedImage.getWidth(),bufferedImage.getHeight(),BufferedImage.TYPE_BYTE_GRAY);;
                    Raster raster = bufferedImage.getRaster();
                    WritableRaster writableRaster = visibleImage.getRaster();
                    for(int i=0;i<raster.getWidth();i++) {
                        for(int j=0;j<raster.getHeight();j++) {
                            int sampleValue = raster.getSample(i,j,0);
                            writableRaster.setSample(i,j,0,sampleValue);
                        }
                    }
                    String destPath = file.getAbsolutePath();
                    File outputDir = new File(destPath);
                    try {
                        ImageIO.write(visibleImage, "jpg", outputDir);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

}
