package com.avanseus.segmentation;

import com.avanseus.avr.model.AVRConstants;
import com.avanseus.imageUtils.ImageUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by yash on 18-04-2016.
 */
public class SplitConnectingCharacters {

    private HashMap<Integer, int[][]> characterLabels;
    ImageUtils imageUtils;
    private int minPixelConnectivity;
    private int maxPixelConnectivity;
    private double singleJoinedCharMinAreaRatio;
    private double singleJoinedCharMaxAreaRatio;
    private double doubleJoinedFirstCharMinAreaRatio;
    private double doubleJoinedFirstCharMaxAreaRatio;
    private double doubleJoinedSecondCharMinAreaRatio;
    private double doubleJoinedSecondCharMaxAreaRatio;

    public void SplitConnectingCharacters(HashMap<Integer,int[][]> labels){
        this.characterLabels = labels;
        imageUtils = new ImageUtils();
        minPixelConnectivity = AVRConstants.minPixelConnectivity;
        maxPixelConnectivity = AVRConstants.maxPixelConnectivity;
        singleJoinedCharMinAreaRatio = AVRConstants.singleJoinedCharMinAreaRatio;
        singleJoinedCharMaxAreaRatio = AVRConstants.singleJoinedCharMaxAreaRatio;
        doubleJoinedFirstCharMinAreaRatio = AVRConstants.doubleJoinedFirstCharMinAreaRatio;
        doubleJoinedFirstCharMaxAreaRatio = AVRConstants.doubleJoinedFirstCharMaxAreaRatio;
        doubleJoinedSecondCharMinAreaRatio = AVRConstants.doubleJoinedSecondCharMinAreaRatio;
        doubleJoinedSecondCharMaxAreaRatio = AVRConstants.doubleJoinedSecondCharMaxAreaRatio;
    }

    public HashMap<Integer, int[][]> splitMergedCharacters(){
        List<Integer> labelsCol = new ArrayList<>();
        List<Integer> labelsRow = new ArrayList<>();

        HashMap<Integer, int[][]> newLabelImage = null;
        for(int indx = 0; indx < characterLabels.size(); indx++){
            labelsCol.add(characterLabels.get(indx).length);
            labelsRow.add(characterLabels.get(indx)[0].length);
        }
        double[] colPair = imageUtils.getArrayFromList(labelsCol);
        double[] rowPair = imageUtils.getArrayFromList(labelsRow);

        int[] mergedIndx = imageUtils.findIndx(imageUtils.divideElementWise1D(colPair, rowPair), ">", AVRConstants.NOT_MERGED_VALID_CHAR_AR_LIMIT);

        if(mergedIndx.length > 0){
            newLabelImage = splitCharacters(mergedIndx);
        }
        else {
            newLabelImage = characterLabels;
        }
        return newLabelImage;
    }

    private HashMap<Integer, int[][]> splitCharacters(int[] mergedIndx){

        HashMap<Integer, int[][]> newLabel = null;

        for(int indx = 0; indx < mergedIndx.length; indx++){
            int[][] mergedChar = characterLabels.get(mergedIndx[indx]);
            double[] sumMerged = imageUtils.sum2DRowColWise(imageUtils.getDoubleCastArray(mergedChar),2);
            int[] find1 = imageUtils.findIndx(sumMerged,"<=", maxPixelConnectivity);
            int[] find2 = imageUtils.findIndx(sumMerged,">=", minPixelConnectivity);
            int[] oldId = getIntersectedId(find1, find2);
            int[] nonZeroNonCornerIds = deleteCornerIds(oldId, mergedChar.length);

            if((nonZeroNonCornerIds) == null){
                newLabel = characterLabels;
            }
            else if(nonZeroNonCornerIds.length == 1){
                newLabel = splitSingleJoinedCharacters(mergedChar, mergedIndx[indx], nonZeroNonCornerIds);
            }else if(nonZeroNonCornerIds.length == 2){
                newLabel = splitDoubleJoinedCharacters(mergedChar,mergedIndx[indx], nonZeroNonCornerIds);
            }
            else {
                newLabel = characterLabels;
            }
            int newSize = newLabel.size() - characterLabels.size();
            if (newSize > 0) {
                for (int indx1 = 0; indx1 < mergedIndx.length; indx1++) {
                    mergedIndx[indx1] = mergedIndx[indx1] + newSize;
                }
            }
            characterLabels = newLabel;
        }
        return newLabel;
    }

    private HashMap<Integer, int[][]> splitSingleJoinedCharacters(
            int[][] mergedChar, int mergedIndx, int[] nonZeroNonCornerIds){
        double f1Cut = ((double)nonZeroNonCornerIds[0]) / mergedChar.length;
        boolean isCut = false;

        HashMap<Integer, int[][]> newLabelImage = new HashMap<>();

        if(f1Cut <= singleJoinedCharMaxAreaRatio && f1Cut >= singleJoinedCharMinAreaRatio){
            for(int row = 0; row < mergedChar[0].length; row++){
                mergedChar[nonZeroNonCornerIds[0]][row] = 0;
            }
            isCut = true;
        }
        if(isCut){
            if(mergedIndx == 0){
                addSplittedCharsInLabelImage(newLabelImage,mergedChar,nonZeroNonCornerIds,0);
                for(int indx =1; indx < characterLabels.size(); indx++){
                    newLabelImage.put(indx+1,characterLabels.get(indx));
                }
            }
            else if (mergedIndx == characterLabels.size()-1){
                for(int indx =0; indx < characterLabels.size()-1; indx++){
                    newLabelImage.put(indx,characterLabels.get(indx));
                }
                addSplittedCharsInLabelImage(newLabelImage,mergedChar,nonZeroNonCornerIds,(characterLabels.size()-1));
            }
            else{
                for(int indx =0; indx < mergedIndx; indx++){
                    newLabelImage.put(indx,characterLabels.get(indx));
                }
                addSplittedCharsInLabelImage(newLabelImage,mergedChar,nonZeroNonCornerIds,(mergedIndx));
                for(int indx = mergedIndx+1; indx < characterLabels.size(); indx++){
                    newLabelImage.put(indx+1,characterLabels.get(indx));
                }
            }
        }else {
            newLabelImage = characterLabels;
        }
        return newLabelImage;
    }


    private HashMap<Integer, int[][]> splitDoubleJoinedCharacters(
            int[][] mergedChar, int mergedIndx, int[] nonZeroNonCornerIds){
        double f1Cut = ((double)nonZeroNonCornerIds[0]) / mergedChar.length;
        double f2Cut = ((double)nonZeroNonCornerIds[1]) / mergedChar.length;
        boolean isF1Cut = false;
        boolean isF2Cut = false;

        double[][] entireRowColElement;
        HashMap<Integer, int[][]> newLabelImage = new HashMap<>();

        if(f1Cut <= doubleJoinedFirstCharMaxAreaRatio && f1Cut >= doubleJoinedFirstCharMinAreaRatio){
            for(int row = 0; row < mergedChar[0].length; row++){
                mergedChar[nonZeroNonCornerIds[0]][row] = 0;
            }
            isF1Cut = true;
        }
        if(f2Cut <= doubleJoinedSecondCharMaxAreaRatio && f2Cut >= doubleJoinedSecondCharMinAreaRatio){
            for(int row = 0; row < mergedChar[0].length; row++){
                mergedChar[nonZeroNonCornerIds[1]][row] = 0;
            }
            isF2Cut = true;
        }
        int toAdd = 1;
        int[] newId;
        if(isF1Cut && isF2Cut) {
            toAdd = 2;
            newId = nonZeroNonCornerIds;
        } else if (isF1Cut){
            newId = new int[1];
            newId[0] = nonZeroNonCornerIds[0];
        }
        else{
            newId = new int[1];
            newId[0] = nonZeroNonCornerIds[1];
        }

        if(isF1Cut || isF2Cut){
            if(mergedIndx == 0){
                addSplittedCharsInLabelImage(newLabelImage,mergedChar,newId,0);
                for(int indx =1; indx < characterLabels.size(); indx++){
                    newLabelImage.put(indx+toAdd,characterLabels.get(indx));
                }

            }
            else if (mergedIndx == characterLabels.size()-1){
                for(int indx =0; indx < characterLabels.size()-1; indx++){
                    newLabelImage.put(indx,characterLabels.get(indx));
                }
                addSplittedCharsInLabelImage(newLabelImage,mergedChar,newId,(characterLabels.size()-1));
            }
            else{
                for(int indx =0; indx < mergedIndx; indx++){
                    newLabelImage.put(indx,characterLabels.get(indx));
                }
                addSplittedCharsInLabelImage(newLabelImage,mergedChar,newId,(mergedIndx));
                for(int indx = mergedIndx+1; indx < characterLabels.size(); indx++){
                    newLabelImage.put(indx+toAdd,characterLabels.get(indx));
                }
            }
        }else {
            newLabelImage = characterLabels;
        }
        return newLabelImage;
    }

    private void addSplittedCharsInLabelImage(HashMap<Integer, int[][]> LabelImage,
                                              int[][] mergedChar, int[] nonZeroNonCornerIds, int Indx){
        double[][] entireRowColElement;
        int[] limit = new int[2];
        if(nonZeroNonCornerIds.length == 1){
            limit[0] = 0; limit[1] = nonZeroNonCornerIds[0];
            entireRowColElement = imageUtils.getEntireRowColElements(imageUtils.getDoubleCastArray(mergedChar), true,limit);
            LabelImage.put(Indx,imageUtils.getIntCastArray(entireRowColElement));

            limit[0] = nonZeroNonCornerIds[0]; limit[1] = mergedChar.length-1;
            entireRowColElement = imageUtils.getEntireRowColElements(imageUtils.getDoubleCastArray(mergedChar), true,limit);
            LabelImage.put(Indx+1,imageUtils.getIntCastArray(entireRowColElement));
        }
        else if(nonZeroNonCornerIds.length == 2){
            limit[0] = 0; limit[1] = nonZeroNonCornerIds[0];
            entireRowColElement = imageUtils.getEntireRowColElements(imageUtils.getDoubleCastArray(mergedChar), true,limit);
            LabelImage.put(Indx,imageUtils.getIntCastArray(entireRowColElement));
            limit[0] = nonZeroNonCornerIds[0]; limit[1] = nonZeroNonCornerIds[1];
            entireRowColElement = imageUtils.getEntireRowColElements(imageUtils.getDoubleCastArray(mergedChar), true,limit);
            LabelImage.put(Indx+1,imageUtils.getIntCastArray(entireRowColElement));
            limit[0] = nonZeroNonCornerIds[1]; limit[1] = mergedChar.length-1;
            entireRowColElement = imageUtils.getEntireRowColElements(imageUtils.getDoubleCastArray(mergedChar), true,limit);
            LabelImage.put(Indx+2,imageUtils.getIntCastArray(entireRowColElement));
        }
        else{
            // do Nothing
        }
    }

    private int[] getIntersectedId(int[] find1, int[] find2){
        int f1length = find1.length;
        int f2length = find2.length;
        List<Integer> uniqId = new ArrayList<>();
        if(f1length < f2length){
            double[] doublefind2 = imageUtils.getDoubleCastArray(find2);
            for(int indx = 0; indx < f1length; indx++){
                if(imageUtils.isMember((double)find1[indx], doublefind2)){
                    uniqId.add((int) find1[indx]);
                }
            }
        }else{
            double[] doublefind1 = imageUtils.getDoubleCastArray(find1);
            for(int indx = 0; indx < f2length; indx++){
                if(imageUtils.isMember((double)find2[indx], doublefind1)){
                    uniqId.add((int) find2[indx]);
                }
            }
        }
        return imageUtils.getIntCastArray(imageUtils.getArrayFromList(uniqId));
    }

    private int[] deleteCornerIds(int[] oldId, int col){
        int[] newId = null;
        for(int indx = 0; indx < oldId.length; indx++){
            if(oldId[indx] <= 2 || oldId[indx] >= (col-3)){
                oldId[indx] = 0;
            }
        }

        int[] nonZeroIds = imageUtils.findIndx(imageUtils.getDoubleCastArray(oldId), ">", 0);
        if(nonZeroIds.length > 0){
            newId = new int[nonZeroIds.length];
            for(int indx = 0; indx < nonZeroIds.length; indx++){
                newId[indx] = oldId[nonZeroIds[indx]];
            }
        }
        if(((newId) != null)&&(newId.length == 2) && (newId[1]-newId[0] <= 2)){
            newId = imageUtils.getIntCastArray(imageUtils.shiftArrayLeft(imageUtils.getDoubleCastArray(newId)));
        }
        return newId;
    }

}
