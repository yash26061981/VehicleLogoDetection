package com.avanseus.descriptorVectors;

import com.avanseus.imageUtils.ImageUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by hemanth on 5/5/16.
 */
public class DescriptiveVectorCreator {
    private String parentDirPath = null;

    public DescriptiveVectorCreator(String parentDirPath) {
        this.parentDirPath = parentDirPath;
    }

    public void createDescriptiveVectorsByReadingCharacterDirectory() {
        try {
            File parentFolder = new File(parentDirPath);
            File[] listOfCharacterDir = parentFolder.listFiles();
            File vectorFile = new File(parentDirPath+File.separator+"vectorChars.text");
            FileWriter fileWriter = new FileWriter(vectorFile,true);
            BufferedWriter bufferWriter = new BufferedWriter(fileWriter);
            int count=0;
            for (File dir : listOfCharacterDir) {
                if (dir.isDirectory()) {
                    File[] listOfCharImages = dir.listFiles();
                    String dirName = dir.getName();
                        String encoding = VectorEncoding.valueOf(dirName).getImageEncoding();
                        System.out.println("Directory name:" + dir.getName() + " Number of images found:" + listOfCharImages.length);
                        System.out.println("Encoding found is:" + encoding);
                        for(File charImageFile:listOfCharImages) {
                            dirName = charImageFile.getName();
                            BufferedImage inputImage = ImageIO.read(charImageFile);
                            int[] descriptiveVector = createVectorByReadingBufferedImage(inputImage);
                            bufferWriter.write("list"+dirName+": ");
                            for (int i = 0; i < descriptiveVector.length; i++) {
                                double val = 0.11;
                                if(descriptiveVector[i] == 1) {
                                    val = 0.99;
                                }
                                bufferWriter.write(String.valueOf(val) + " ");
                            }
                            count++;
/*                            bufferWriter.write("> ");
                            bufferWriter.write(encoding);*/
                            bufferWriter.write("\n");
                        }
                }
            }
            bufferWriter.close();
            System.out.println("Count:"+count);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int[] createVectorByReadingBufferedImage(BufferedImage inputImage) {
        ImageUtils imageUtils = new ImageUtils();
        int[][] imageArray = imageUtils.getLogical2DImage(inputImage);
        int[] vector = imageUtils.flatten2DArray(imageArray);
        return vector;
    }

    public static void main(String[] args) {
        DescriptiveVectorCreator descriptiveVectorCreator = new DescriptiveVectorCreator("/home/hemanth/Downloads/thickTest");
        descriptiveVectorCreator.createDescriptiveVectorsByReadingCharacterDirectory();
    }
}
