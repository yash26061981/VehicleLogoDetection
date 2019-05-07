package com.avanseus.avr.detection;

import com.avanseus.avr.model.CombinedImage;
import com.avanseus.avr.model.WindowData;
import com.avanseus.imageUtils.AdaptiveThresholding;
import com.avanseus.imageUtils.ConnectedComponentAnalysis;
import com.avanseus.imageUtils.EdgeDetection;
import com.avanseus.imageUtils.ImageUtils;
import com.avanseus.segmentation.Segmentation;

import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Created by yash on 31-05-2016.
 */
public class LogoDetection {

    private CombinedImage LPmageDetails;
    private BufferedImage histeqImage;
    private BufferedImage edgeImage;
    private ImageUtils imageUtils;
    private double cropRatio;

    public LogoDetection(CombinedImage cropRegion, BufferedImage hqImage, BufferedImage edImage) {
        imageUtils = new ImageUtils();
        LPmageDetails =cropRegion;
        histeqImage = hqImage;
        edgeImage = edImage;
        cropRatio = 0.5;
    }

    public BufferedImage cropHisteqImage(){
        int xMax = LPmageDetails.getXmax();
        int xMin = LPmageDetails.getXmin();
        int width = xMax - xMin;
        int height = (int)Math.floor((double)(width)*cropRatio);
        int yMin, yMax;
        yMax = LPmageDetails.getYmin();
        if (height >= yMax){
            yMin = 1;
        }else{
            yMin = yMax - height;
        }
        return imageUtils.getCroppedBufferedImage(histeqImage,xMin,xMax,yMin,yMax);
    }
    public BufferedImage cropEdgeImage(){
        int xMax = LPmageDetails.getXmax();
        int xMin = LPmageDetails.getXmin();
        int width = xMax - xMin;
        int height = (int)Math.floor((double)(width)*cropRatio);
        int yMin, yMax;
        yMax = LPmageDetails.getYmin();
        if (height >= yMax){
            yMin = 1;
        }else{
            yMin = yMax - height;
        }
        return imageUtils.getCroppedBufferedImage(edgeImage,xMin,xMax,yMin,yMax);
    }

    public List<CombinedImage> getPossibleLogoRegion(){
        List<CombinedImage> validLogoImages;
        BufferedImage croppedPossibleLogoRegion = cropHisteqImage();
        EdgeDetection edgeDetection = new EdgeDetection();
        BufferedImage croppedPossibleLogoEdge;
        /*if(false) {
            croppedPossibleLogoEdge = edgeDetection.getSobelEdgeImage(croppedPossibleLogoRegion, "vertical", 0.02);
        }
        else {
            croppedPossibleLogoEdge = cropEdgeImage();
        }*/
        croppedPossibleLogoEdge = edgeDetection.getBackgroundTextureSuppressedEdgeImage(croppedPossibleLogoRegion, null);

        ImageUtils.saveBufferedImage(croppedPossibleLogoRegion, "logoGrayImage");
        imageUtils.getVisibleImageFromBufferedImageOfLogicalOnes(croppedPossibleLogoEdge, "logoEdgeImage");

        Localization logoLocalization = new Localization(croppedPossibleLogoRegion,croppedPossibleLogoEdge);
        List<WindowData> slidingWindowLogo = logoLocalization.localizeLogo();
        System.out.println("Total Patches: "+slidingWindowLogo.size());

        BufferedImage patchLogoBitmapImage = logoLocalization.getSlidingWindowPatchImage(slidingWindowLogo);

        BufferedImage patchedLogoImage = imageUtils.getBufferedImageFrom2D(imageUtils.multiplyElementWise2D(
                imageUtils.get2DImage(patchLogoBitmapImage), imageUtils.get2DImage(croppedPossibleLogoRegion)));
        ImageUtils.saveBufferedImage(patchedLogoImage, "LocalisedLogoPatches");
        ConnectedComponentAnalysis connectedComponentAnalysisLogo = new ConnectedComponentAnalysis();
        double[][] connectedLogoLabelImage = connectedComponentAnalysisLogo.findConnectedComponent(patchLogoBitmapImage);

        FineTuneClustering fineTuneClusteringLogo = new FineTuneClustering(croppedPossibleLogoRegion);
        validLogoImages = fineTuneClusteringLogo.discardUnwantedLogoPatches(connectedLogoLabelImage);
        int j= 1;
        for(CombinedImage validImage:validLogoImages) {
            AdaptiveThresholding adaptiveThresholding = new AdaptiveThresholding();

            BufferedImage patch = imageUtils.getBufferedImageFrom2D(validImage.getCroppedImage());
            BufferedImage thresholdBmp = adaptiveThresholding.applyOtsuThresholding(patch);
            String name = "LOGO_Thresholded_" + j;
            imageUtils.getVisibleImageFromBufferedImageOfLogicalOnes(thresholdBmp, name);
            j++;
        }

        return validLogoImages;
    }
}
