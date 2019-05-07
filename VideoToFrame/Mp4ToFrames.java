package com.avanseus.VideoToFrame;

import com.avanseus.avr.driver.Driver;
import com.avanseus.avr.fileOperations.FileUtils;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FrameGrabber;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Created by hemanth on 5/4/16.
 */
public class Mp4ToFrames {

    private FFmpegFrameGrabber frameGrabber;
    private String fileName = "/home/hemanth/testVideo/scenario2Vids/09-12-015/MH01AR7928.mp4";
    private String destinationFolder = "/home/hemanth/testVideo/MH01AR7928/";

    public Mp4ToFrames(String videoFileName,String destinationFolder) {
        if(videoFileName!=null && destinationFolder!=null) {
            this.fileName = videoFileName;
            this.destinationFolder = destinationFolder;
        }
        frameGrabber = new FFmpegFrameGrabber(fileName);
        FileUtils.createDirectory(destinationFolder);
    }

    public void convertVideoFilesToFrames() {
        try {
            frameGrabber.start();
                for (int i = 0 ; i < frameGrabber.getLengthInFrames() ; i++) {
                    BufferedImage inputBuffer = frameGrabber.grab().getBufferedImage();
                    if(i==345) {
                        ImageIO.write(inputBuffer, "png", new File(destinationFolder + "video-frame-" + i + ".png"));
                        BufferedImage bufferedImage = inputBuffer;
                        Driver driver = new Driver(true,"","");
                        driver.run(bufferedImage);
                    }
                }
            frameGrabber.stop();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        /*try {
            Loader.load(org.bytedeco.javacpp.avutil.class);
            Loader.load(org.bytedeco.javacpp.avcodec.class);
            Loader.load(org.bytedeco.javacpp.avformat.class);
            Loader.load(org.bytedeco.javacpp.avdevice.class);
            Loader.load(org.bytedeco.javacpp.swscale.class);
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        Mp4ToFrames mp4ToFrames = new Mp4ToFrames("/home/hemanth/testVideo/scenario2Vids/09-12-015/MH43AT0938.mp4" ,
                "/home/hemanth/testVideo/scenario2Vids/09-12-015/MH43AT0938/");
        mp4ToFrames.convertVideoFilesToFrames();
    }
}
//mvn package exec:java -Dplatform.dependencies -Dexec.mainClass=com.avanseus.VideoToFrame.Mp4ToFrames

//mvn package exec:java -Dplatform.dependencies -Dexec.mainClass=Demo