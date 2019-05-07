package com.avanseus.avr.driver;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created by hemanth on 28/4/16.
 */
public class DescriptiveVectorWriter {
    public static void writeDescriptiveVectorToFile(HashMap<Integer,int[]> descriptiveVectors) {
        File file = new File(Driver.destDir+File.separator+"vector.text");
        System.out.println("File name:"+file.getName());
        try {
            if(!file.exists()){
                file.createNewFile();
            }
            FileWriter fileWriter = new FileWriter(file,true);
            BufferedWriter bufferWriter = new BufferedWriter(fileWriter);
            for(int index:descriptiveVectors.keySet()) {
                int[] descriptiveVector = descriptiveVectors.get(index);
                bufferWriter.write("list"+String.valueOf(index)+": ");
                for (int i = 0; i < descriptiveVector.length; i++) {
                    bufferWriter.write(String.valueOf(descriptiveVector[i]) + " ");
                }
                bufferWriter.write("\n");
            }
            bufferWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
