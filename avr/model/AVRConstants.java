package com.avanseus.avr.model;

/**
 * Created by hemanth on 27/4/16.
 */
public class AVRConstants {

    //LP Localization constants
    public static final int MIN_WINDOW_HEIGHT = 20;
    public static final int MAX_WINDOW_HEIGHT = 100;
    public static final int minAspectRatio = 5;
    public static final int maxAspectRatio = 8;
    public static final double edgeThreshold = 0.15; // 0.15 for nonHD Image
    public static final double edgeThresholdHD = 0.25; // 0.15 for nonHD Image
    public static final int transitionStep = 15;
    public static final int sizeStep = 40;
    public static final int averageIntensity = 100;

    //LP Localization constants
    public static final int MIN_WINDOW_HEIGHT_LOGO = 20;
    public static final int MAX_WINDOW_HEIGHT_LOGO = 20;
    public static final int minAspectRatioLogo = 1;
    public static final int maxAspectRatioLogo = 1;
    public static final double edgeThresholdLogo = 0.15;
    public static final int transitionStepLogo = 15;
    public static final int sizeStepLogo = 20;
    public static final int averageIntensityLogo = 100;

    //Adaptive thresholding
    public static final int dynamicRange = 128;
    public static final double constK = 0.2;
    public static final int[] niBlackMask = {15,15};

    // Skew Detection Constants
    public static final double rhoResolution = 1;
    public static final double thetaResolution = 0.5;

    //FineTuneClustering constants
    public static final double MIN_EDGE_DENSITY = 0.065;
    public static final double MIN_ASPECT_THRESHOLD = 3;
    public static final double MIN_LP_ASPECT_THRESHOLD = 4;
    public static final double MAX_LP_ASPECT_THRESHOLD = 15;
    public static final int MIN_PIXEL_AREA = 500;

    //Segmentation constants
    public static final double MIN_LABEL_AREA = 10;
    public static final double MIN_LABEL_XRATIO = 0.4;
    public static final double MIN_LABEL_YRATIO = 0.02;
    public static final int MAX_NUMBER_SEGMENTED_LABELS = 18;
    public static final int MIN_NUMBER_SEGMENTED_LABELS = 6;
    public static final double MAX_INVALID_SEGMENTED_LABEL_LIMIT = 0.4;
    public static final double NOT_MERGED_INVALID_CHAR_AR_LIMIT = 1.2;
    public static final double NOT_MERGED_VALID_CHAR_AR_LIMIT = 1.1;

    // Splitting Connecting Characters Constants
    public static final int minPixelConnectivity = 1;
    public static final int maxPixelConnectivity = 3;
    public static final double singleJoinedCharMinAreaRatio = 0.4;
    public static final double singleJoinedCharMaxAreaRatio = 0.6;
    public static final double doubleJoinedFirstCharMinAreaRatio = 0.25;
    public static final double doubleJoinedFirstCharMaxAreaRatio = 0.4;
    public static final double doubleJoinedSecondCharMinAreaRatio = 0.65;
    public static final double doubleJoinedSecondCharMaxAreaRatio = 0.75;

    // Final Input NN Characters Size
    public static final int[] uniformSizeChar = {30,20}; // Row,Col Format
}
