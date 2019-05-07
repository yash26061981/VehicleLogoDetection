package com.avanseus.manager;

import org.apache.hadoop.fs.FSDataOutputStream;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by hemanth on 31/3/16.
 */
public class HDFSTestStub {
    public static void main(String[] args) {
        HDFSManager hdfsManager = new HDFSManager("10.2.2.100");

        try {
            FSDataOutputStream fsDataOutputStream = hdfsManager.getHdfsOutputSteam("/user/HemanthTest/sample.txt");
            for(int i=0;i<10;i++) {
                fsDataOutputStream.writeBytes("Testing..");
            }
            fsDataOutputStream.flush();
            fsDataOutputStream.close();;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        hdfsManager.copyToLocal("/user/HemanthTest/sample.txt","/home/hemanth/testVideo/");
        hdfsManager.close();

        /* Second API.
        try {
            File file = new File("/home/hemanth/testVideo/abc.mp4");
            FileInputStream inputStream = null;
            inputStream = new FileInputStream(file);
            byte fileContent[] = new byte[(int) file.length()];
            inputStream.read(fileContent);
            //Write the contents to HDFS
            hdfsManager.writeToHdfs("/user/HemanthTest/sample1.mp4", fileContent);
            hdfsManager.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }
}
