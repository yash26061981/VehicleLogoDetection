package com.avanseus.segmentation;

import com.avanseus.imageUtils.ImageUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by yash on 14-04-2016.
 */
public class MergeConnectingLabels {
    public double[][] labelImage;
    HashMap<Integer,int[]> equivPair;
    ImageUtils imageUtils;

    public MergeConnectingLabels(double[][] lmImage, HashMap<Integer,int[]> ep){
        this.labelImage = lmImage;
        this.equivPair = ep;
        this.imageUtils = new ImageUtils();
    }

    public double[][] findMergeConnectedLabels(){

        List<Integer> equiv1 = new ArrayList<>();
        List<Integer> equiv2 = new ArrayList<>();
        double[][] newLabelImage = new double[labelImage.length][labelImage[0].length];

        imageUtils.copy2D(labelImage, newLabelImage);

        for(Integer indx:equivPair.keySet()){
            int[] pair = new int[2];
            pair = equivPair.get(indx);
            equiv1.add(pair[0]); equiv2.add(pair[1]);
        }

        double[] pair1Label = imageUtils.getArrayFromList(equiv1);
        double[] pair2Label = imageUtils.getArrayFromList(equiv2);

        double[] uLabel1 = imageUtils.findUnique(pair1Label);

        //List<Integer> equiv21 = new ArrayList<>();
        HashMap<Integer, List<Integer>> labelSets = new HashMap<>();
        int setId = 0;
        for(int indx = 0; indx < uLabel1.length; indx++){
            int[] uind1 = imageUtils.findIndx(pair1Label,"==", uLabel1[indx]);
            List<Integer> equiv21 = new ArrayList<>();
            for(int indx1 = 0; indx1 < uind1.length; indx1++){
                equiv21.add((int) pair2Label[uind1[indx1]]);
            }

            double[] uind2 = imageUtils.findUnique(imageUtils.getArrayFromList(equiv21));
            if(indx == 0){
                createNewSet(setId,labelSets,(int)uLabel1[indx],uind2);
                setId++;
            }
            else {
                int key = checkIfValueExist(uLabel1[indx], labelSets);
                if(key != -1)
                {
                    addInExistingSet(key, labelSets,(int)uLabel1[indx],uind2);
                }
                else {
                    createNewSet(setId,labelSets,(int)uLabel1[indx],uind2);
                    setId++;
                }
            }
        }
        mergeConnectedLabels(labelSets,newLabelImage);
        return newLabelImage;
    }

    private int checkIfValueExist(double value, HashMap<Integer, List<Integer>> setS){
        for (Integer key:setS.keySet()){
            if(imageUtils.isMember(value,imageUtils.getArrayFromList(setS.get(key)))){
                return key;
            }
        }
        return -1;
    }

    private void mergeConnectedLabels(HashMap<Integer, List<Integer>> setS, double[][] newLabelImage){

        for(int indx = 0; indx < setS.size(); indx++){
            double[] uid = imageUtils.findUnique(imageUtils.getArrayFromList(setS.get(indx)));
            int f1Label = (int)(uid[0]);
            for(int indx1 = 1; indx1 < uid.length; indx1++){
                int[][] idColRow = imageUtils.findIndxIn2D(labelImage, "==", uid[indx1]);
                for(int indx2=0; indx2 < idColRow.length; indx2++){
                    newLabelImage[idColRow[indx2][0]][idColRow[indx2][1]] = f1Label;
                }
            }
        }
    }

    private void createNewSet(int Id, HashMap<Integer, List<Integer>> setS, double uLabel1, double[] uLabel2Arr){
        List<Integer> pair1 = new ArrayList<>();
        pair1.add((int)uLabel1);
        for(int indx1 = 0; indx1 < uLabel2Arr.length; indx1++)
            pair1.add((int)uLabel2Arr[indx1]);
        setS.put(Id,pair1);
    }

    private void addInExistingSet(int Id, HashMap<Integer, List<Integer>> setS, double uLabel1, double[] uLabel2Arr){
        List<Integer> pair1 = setS.get(Id);
        pair1.add((int)uLabel1);
        for(int indx1 = 0; indx1 < uLabel2Arr.length; indx1++)
            pair1.add((int)uLabel2Arr[indx1]);

        setS.put((Id), pair1);
    }

    static public void main(String args[]) throws Exception
    {
        ImageUtils imageUtils = new ImageUtils();
        double[][] a ={{8,1,6,1, 4},{3,5,7,2,8},{1,12,4,4,0}};
        int[] limit = {-1};
        double[] a1 =imageUtils.findUniqueIn2D(a);
    }
}
