package com.avanseus.avr.fileOperations;

import java.io.File;

/**
 * Created by hemanth on 6/4/16.
 */
public class FileUtils {

    public static boolean createDirectory(String dirName) {
        File directory = new File(dirName);
        if (!directory.exists()) {
            return directory.mkdir();
        }
        return true;
    }

    public static String[] listAllFilesInADirectory(File dirName) {
        if(dirName.isDirectory() && dirName.listFiles().length !=0) {
            return dirName.list();
        }
        return null;
    }
}
