package com.avanseus.manager;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.fs.permission.FsAction;
import org.apache.hadoop.fs.permission.FsPermission;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HDFSManager {
    private Configuration configuration;
    private FileSystem fileSystem;

    public HDFSManager() {
        try {
            configuration = new Configuration();
            configuration.set("fs.hdfs.impl", org.apache.hadoop.hdfs.DistributedFileSystem.class.getName());
            configuration.set("fs.file.impl", org.apache.hadoop.fs.LocalFileSystem.class.getName());
            //configuration.set("fs.default.name", "hdfs://161.202.175.229:9000/user/hadoop/");
            configuration.set("fs.default.name", "hdfs://10.2.2.100:9000/user/hadoop/");
            fileSystem = FileSystem.get(configuration);
        } catch (IOException e) {
            System.out.println("Unable to get the file system from configurations.");
            e.printStackTrace();
        }
    }

    public HDFSManager(String masterIp) {
        try {
            configuration = new Configuration();
            configuration.set("fs.hdfs.impl", org.apache.hadoop.hdfs.DistributedFileSystem.class.getName());
            configuration.set("fs.file.impl", org.apache.hadoop.fs.LocalFileSystem.class.getName());
            configuration.set("fs.default.name", "hdfs://" + masterIp + ":9000/user/hadoop/");
            fileSystem = FileSystem.get(configuration);
        } catch (IOException e) {
            System.out.println("Unable to get the file system from configurations.");
            e.printStackTrace();
        }
    }


    public List<String> listFiles(String path, boolean recursiveSearch) {
        try {
            List<String> files = new ArrayList<>();
            Path hdfsPath = new Path(path);
            RemoteIterator<LocatedFileStatus> fileList = fileSystem.listFiles(hdfsPath, recursiveSearch);
            while (fileList.hasNext()) {
                FileStatus fileStatus = fileList.next();
                System.out.println(fileStatus.getPath().getName());
                files.add(fileStatus.getPath().getName());
            }
            return files;
        } catch (IOException e) {
            System.out.println("Unable to list file in the path: " + path);
            e.printStackTrace();
            return null;
        }
    }

    public void createDirectory(String path) {
        try {
            FsPermission fsPermission = new FsPermission(FsAction.ALL, FsAction.ALL, FsAction.ALL);
            Path hdfsPath = new Path(path);
            boolean status = fileSystem.mkdirs(hdfsPath, fsPermission);
            if (status) {
                System.out.println(path + " created!");
            } else {
                System.out.println("Unable to create: " + path);
            }
        } catch (IOException e) {
            System.out.println("Unable to create directory in the path");
            e.printStackTrace();
        }
    }

    public void copyFromLocal(String localFSPath, String hdfsDestPath) {
        try {
            Path sourcePath = new Path(localFSPath);
            Path hdfsPath = new Path(hdfsDestPath);
            System.out.println("Copying in progres....");
            fileSystem.copyFromLocalFile(sourcePath, hdfsPath);
            System.out.println("File copied successfully from: " + localFSPath + " to: " + hdfsDestPath);
        } catch (IOException e) {
            System.out.println("Unable to copy files from local to HDFS");
            e.printStackTrace();
        }
    }

    public void copyToLocal(String hdfsSourcePath, String localFSPath) {
        try {
            Path hdfsPath = new Path(hdfsSourcePath);
            Path localPath = new Path(localFSPath);
            System.out.println("Copying in progres....");
            fileSystem.copyToLocalFile(hdfsPath, localPath);
            System.out.println("File copied successfully from: " + hdfsSourcePath + " to: " + localFSPath);
        } catch (IOException e) {
            System.out.println("Unable to copy files from HDFS to local");
            e.printStackTrace();
        }
    }

    public void writeToHdfs(String fileName, String path) {
        try {
            System.out.println("Reading file at path: " + path);
            Path hdfsPath = new Path(fileName);
            FSDataOutputStream fsDataOutputStream = fileSystem.create(hdfsPath);
            File file = new File(path);
            FileInputStream inputStream = new FileInputStream(file);
            byte fileContent[] = new byte[(int) file.length()];
            inputStream.read(fileContent);
            System.out.println("Writing file on HDFS at path: " + fileName);
            fsDataOutputStream.write(fileContent);
            fsDataOutputStream.flush();
            fsDataOutputStream.close();
            System.out.println("HDFS streaming completed!.");
        } catch (IOException e) {
            System.out.println("Unable to stream file to HDFS.");
            e.printStackTrace();
        }
    }

    public void writeToHdfs(String fileName, byte[] fileContent) {
        try {
            System.out.println("Writing file on HDFS at path: " + fileName);
            Path hdfsPath = new Path(fileName);
            FSDataOutputStream fsDataOutputStream = fileSystem.create(hdfsPath);
            fsDataOutputStream.write(fileContent);
            fsDataOutputStream.flush();
            fsDataOutputStream.close();
            System.out.println("HDFS streaming completed!.");
        } catch (IOException e) {
            System.out.println("Unable to stream file to HDFS.");
            e.printStackTrace();
        }
    }

    public FSDataOutputStream getHdfsOutputSteam(String fileName) {
        try {
            System.out.println("Getting HDFS output stream....");
            Path hdfsPath = new Path(fileName);
            System.out.println("HDFS output stream returned!.");
            return fileSystem.create(hdfsPath);
        } catch (IOException e) {
            System.out.println("Unable to stream file to HDFS.");
            e.printStackTrace();
            return null;
        }
    }

    public void closeHdfsOutputSteam(FSDataOutputStream fsDataOutputStream) {
        try {
            System.out.println("Closing HDFS output stream...");
            fsDataOutputStream.close();
            System.out.println("HDFS output stream closed!.");
        } catch (IOException e) {
            System.out.println("Unable to close HDFS output stream.");
            e.printStackTrace();
        }
    }

    public void readFromHdfs(String fileName) {
        try {
            String line;
            Path path = new Path(fileName);
            FSDataInputStream fsDataInputStream = fileSystem.open(path);
            while ((line = fsDataInputStream.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            System.out.println("Unable to create file.");
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            fileSystem.close();
        } catch (IOException e) {
            System.out.println("File system could not be closed.");
            e.printStackTrace();
        }
        return;
    }

    public static void main(String[] args) {
        HDFSManager hdfsManager = new HDFSManager();
        if (args.length < 2) {
            System.out.println("Please pass arguments to the program.\n" +
                    "Operations possible:\n" +
                    "1. Directory creation (eg:java -cp AVR.jar com.avanseus.manager.HDFSManager create /user/hadoop/testfolder)\n" +
                    "2. List files and directories (eg:java -cp AVR.jar com.avanseus.manager.HDFSManager list /user/hadoop/testfolder)\n" +
                    "3. Copy files from local to HDFS (eg: java -cp AVR.jar com.avanseus.manager.HDFSManager push /local/filepath/localfile /hdfs/path/)\n" +
                    "4. Copy files from HDFS to local (eg.java -cp AVR.jar com.avanseus.manager.HDFSManager pull /hdfs/path/file /local/filepath/).");
        } else {
            System.out.println("Processing....");
            switch (args[0]) {
                case "create":
                    hdfsManager.createDirectory(args[1]);
                    break;
                case "list":
                    hdfsManager.listFiles(args[1], true);
                    break;
                case "push":
                    if (args.length < 3) {
                        System.out.println("Invalid number of arguments. (eg. java -cp AVR.jar com.avanseus.manager.HDFSManager push /local/filepath/localfile /hdfs/path/)");
                        break;
                    }
                    hdfsManager.copyFromLocal(args[1], args[2]);
                    break;
                case "pull":
                    if (args.length < 3) {
                        System.out.println("Invalid number of arguments. (eg.java -cp AVR.jar com.avanseus.manager.HDFSManager pull /hdfs/path/file /local/filepath/)");
                        break;
                    }
                    hdfsManager.copyToLocal(args[1], args[2]);
                    break;
                default:
                    System.out.println("Unknown operation.\n" +
                            "Operations possible:\n" +
                            "1. Directory creation (eg:java -cp AVR.jar com.avanseus.manager.HDFSManager create /user/hadoop/testfolder)\n" +
                            "2. List files and directories (eg:java -cp AVR.jar com.avanseus.manager.HDFSManager list /user/hadoop/testfolder)\n" +
                            "3. Copy files from local to HDFS (eg: java -cp AVR.jar com.avanseus.manager.HDFSManager push /local/filepath/localfile /hdfs/path/)\n" +
                            "4. Copy files from HDFS to local (eg.java -cp AVR.jar com.avanseus.manager.HDFSManager pull /hdfs/path/file /local/filepath/).");
            }
            hdfsManager.close();
            System.out.println("Operation completed!");
        }
    }
}