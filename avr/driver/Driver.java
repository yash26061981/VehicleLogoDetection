package com.avanseus.avr.driver;

import com.avanseus.Dimension.PCA.Matrix;
import com.avanseus.avr.detection.Localization;
import com.avanseus.avr.detection.LogoDetection;
import com.avanseus.descriptorVectors.GetUniformSkeletonImage;
import com.avanseus.imageUtils.*;
import com.avanseus.segmentation.Segmentation;
import com.avanseus.avr.detection.FineTuneClustering;
import com.avanseus.avr.fileOperations.FileUtils;
import com.avanseus.avr.model.CombinedImage;
import com.avanseus.avr.model.WindowData;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

/**
 * Created by hemanth on 18/4/16.
 */
public class Driver {
    private Properties properties = new Properties();
    private String classPath = null;
    public static String srcImage = null;
    public static String destDir = null;

    public Driver(boolean user, String imagePath,String destinationDir) {
        try {
            if(user) {
                classPath = "/home/hemanth/analytics/analytics/AVR/AVR-core/src/main/resources/";
            } else {
                classPath = "D:/GitRepo/workspace/AVR/AVR-core/src/main/resources/";
            }
            InputStream input = new FileInputStream(classPath+"avr.properties");
            properties.load(input);
            srcImage = properties.getProperty("SRC_IMAGE");
            destDir = properties.getProperty("DEST_DIR");
            //srcImage = imagePath;
            //destDir = destinationDir;
            FileUtils.createDirectory(destDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run(BufferedImage bufferedImage) {
        ImageUtils imageUtils = new ImageUtils();
        try {
            ColorConversion colorConversion = new ColorConversion();
            BufferedImage grayImage = colorConversion.rgb2gray(bufferedImage); //gray
            ImageUtils.saveBufferedImage(grayImage, "GrayImage");
            System.out.println("GrayScale image creation done.");

            EqualiseHistogram equaliseHistogram = new EqualiseHistogram();
            BufferedImage histEqualizedImage = equaliseHistogram.performHistEqOperation(grayImage);
            ImageUtils.saveBufferedImage(histEqualizedImage, "EqualisedImage");
            System.out.println("histEqualised image created.");

            EdgeDetection edgeDetection = new EdgeDetection();
            BufferedImage edgeImage = edgeDetection.getSobelEdgeImage(histEqualizedImage, "vertical", null);
            imageUtils.getVisibleImageFromBufferedImageOfLogicalOnes(edgeImage, "EdgeImage");
            System.out.println("Edge image creation done.");


            //Localization localization = new Localization(histEqualizedImage,edgeImage);
            int countIndx = 0; double edgeThresh = 0.15; int maxIter = 4;
            do {
                countIndx++;
                Localization localization = new Localization(histEqualizedImage,edgeImage);
                List<WindowData> slidingWindows = localization.localizeLP(edgeThresh);
                edgeThresh = edgeThresh + 0.05;
                System.out.println("Total Patches: "+slidingWindows.size());

                BufferedImage patchBitmapImage = localization.getSlidingWindowPatchImage(slidingWindows);

                BufferedImage patchedImage = imageUtils.getBufferedImageFrom2D(imageUtils.multiplyElementWise2D(
                        imageUtils.get2DImage(patchBitmapImage), imageUtils.get2DImage(histEqualizedImage)));
                ImageUtils.saveBufferedImage(patchedImage, "LocalisedPatches");
                System.out.println("Patch image created.");

                ConnectedComponentAnalysis connectedComponentAnalysis = new ConnectedComponentAnalysis();
                double[][] connectedLabelImage = connectedComponentAnalysis.findConnectedComponent(patchBitmapImage);

                FineTuneClustering fineTuneClustering = new FineTuneClustering(histEqualizedImage);
                List<CombinedImage> validImages = fineTuneClustering.discardUnwantedPatches(connectedLabelImage);
                HashMap<Integer, int[][]> charLabels = new HashMap<>();
                int j = 1;
                HashMap<Integer,int[]> descriptiveVectors = new HashMap<>();
                for(CombinedImage validImage:validImages) {
                    Segmentation segmentation = new Segmentation();
                    AdaptiveThresholding adaptiveThresholding = new AdaptiveThresholding();

                    BufferedImage patch = imageUtils.getBufferedImageFrom2D(validImage.getCroppedImage());
                    BufferedImage thresholdBmp = adaptiveThresholding.applyNiBlackThresholding(patch);
                    String name = "LPC_Thresholded_" + j;
                    imageUtils.getVisibleImageFromBufferedImageOfLogicalOnes(thresholdBmp, name);

                    charLabels = segmentation.SegmentUsingConnectedComponentAnalysis(thresholdBmp);
                    if((charLabels) == null)
                        continue;

                    for(int indx=1;indx<=charLabels.size();indx++) {
                        name = "SegmentedChars_" + j +"_"+ indx;
                        imageUtils.getVisibleImageFromBufferedImageOfLogicalOnes(
                                imageUtils.getBufferedImageFrom2D(charLabels.get(indx-1)), name);
                    }
                    HashMap<Integer, int[][]> thinSegmentedChars = new HashMap<>();
                    GetUniformSkeletonImage getUniformSkeletonImage = new GetUniformSkeletonImage(charLabels);
                    thinSegmentedChars = getUniformSkeletonImage.getThinnedCharacter();
                    for(int indx=1;indx<=charLabels.size();indx++) {
                        name = "ThinnedSegmentedChars_" + j +"_"+ indx;
                        imageUtils.getVisibleImageFromBufferedImageOfLogicalOnes(
                                imageUtils.getBufferedImageFrom2D(thinSegmentedChars.get(indx-1)), name);
                        descriptiveVectors.put(indx, imageUtils.flatten2DArray(thinSegmentedChars.get(indx-1)));
                    }
                    System.out.println("Segmented. Total Characters: "+ charLabels.size());
                    LogoDetection logoDetection = new LogoDetection(validImage,histEqualizedImage,edgeImage);
                    List<CombinedImage> validLogoImages = logoDetection.getPossibleLogoRegion();

                    j++;
                    //DescriptiveVectorWriter.writeDescriptiveVectorToFile(descriptiveVectors);
                }
                if((charLabels) == null)
                    continue;
                if ((charLabels.size() > 7)&& (charLabels.size() < 12))
                    break;
            }while(countIndx <= maxIter);

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public static void main(String[] args) {
        try {
            /*
            File folder = new File("/home/hemanth/CAR_NUMBER_PLATES/Basement-LB_Improved");
            File[] listOfFiles = folder.listFiles();
            for (File file : listOfFiles) {
                if (file.isFile()) {
                    String src = file.getAbsolutePath();
                    String dest = src.
                            substring(0, src.lastIndexOf("."));
                    Driver driver = new Driver(true,src,dest);
                    File inputImageFile = new File(srcImage); //org
                    BufferedImage inputImage = ImageIO.read(inputImageFile);
                    */
            Driver driver = new Driver(false,null,null);
            File inputImageFile = new File(srcImage);
            BufferedImage inputImage = ImageIO.read(inputImageFile);
            driver.run(inputImage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
