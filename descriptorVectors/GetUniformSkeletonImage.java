package com.avanseus.descriptorVectors;

import com.avanseus.avr.model.AVRConstants;
import com.avanseus.imageUtils.ImageResize;
import com.avanseus.imageUtils.ImageUtils;
import com.avanseus.imageUtils.MorphologicalOperations;
import com.avanseus.imageUtils.MorphologicalOperations;
import com.avanseus.imageUtils.ThinningService;

import java.awt.image.BufferedImage;
import java.util.HashMap;

/**
 * Created by yash on 26-04-2016.
 */
public class GetUniformSkeletonImage {

    HashMap<Integer,int[][]> segmentedImage;
    ImageUtils imageUtils;
    int[] finalSize = {0,0};

    public GetUniformSkeletonImage(HashMap<Integer,int[][]> s){
        this.segmentedImage = s;
        this.imageUtils = new ImageUtils();
        finalSize[0] = AVRConstants.uniformSizeChar[0] - 2;
        finalSize[1] = AVRConstants.uniformSizeChar[1] - 2;
    }

    public int[][] getUniformSizeCharLabels(int[][] inImage){
        ImageResize imageResize = new ImageResize();
        int[] oldSize = {inImage[0].length, inImage.length};
        double[] scaling = imageResize.GetScalingRatioFromSize(oldSize, finalSize);
        double[][] resizedImage = imageResize.ResizeImageUsingNearestNeighbor(imageUtils.getDoubleCastArray(inImage),scaling);
        return imageUtils.getIntCastArray(resizedImage);
    }

    public HashMap<Integer,int[][]> getThinnedCharacter(){
        HashMap<Integer,int[][]> thinnedImage = new HashMap<>();
        MorphologicalOperations morphologicalOperations = new MorphologicalOperations();
        //ThinningService thinningService = new ThinningService();

        for(int indx =0; indx < segmentedImage.size(); indx++){
            int[][] inImg = segmentedImage.get(indx);
            int[][] resizedImg = getUniformSizeCharLabels(inImg);
            int[] padMask = {1,1};
            double[][] paddedResizedImg = imageUtils.padImage(imageUtils.getBufferedImageFrom2D(resizedImg),padMask,"zeros");
            imageUtils.getVisibleImageFromBufferedImageOfLogicalOnes(
                    imageUtils.getBufferedImageFrom2D(paddedResizedImg), "A-resized_"+indx);
            BufferedImage thinImg = morphologicalOperations.applyMorphologicalThinning(imageUtils.getBufferedImageFrom2D(paddedResizedImg));
            //BufferedImage thinImg = thinningService.doZhangSuenThinning(imageUtils.getBufferedImageFrom2D(paddedResizedImg));
            thinnedImage.put(indx,imageUtils.get2DImageInteger(thinImg));
        }
        return thinnedImage;
    }


}
