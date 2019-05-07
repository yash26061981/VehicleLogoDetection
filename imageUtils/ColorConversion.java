package com.avanseus.imageUtils;

import java.awt.image.*;

public class ColorConversion {
    private ImageUtils imageUtils = new ImageUtils();

    public BufferedImage rgb2gray(BufferedImage inImage) {
        BufferedImage outImage = null;
        try {
            double grayLevel;
            double tol = 0.5;
            double[] mask = {0.2989, 0.5870, 0.1140};
            outImage = new BufferedImage(inImage.getWidth(),inImage.getHeight(),BufferedImage.TYPE_BYTE_GRAY);
            WritableRaster writableRaster = outImage.getRaster();
            Raster raster = inImage.getRaster();

            for(int x = 0; x < writableRaster.getWidth(); x++) {
                for (int y = 0; y < writableRaster.getHeight(); y++) {
                    grayLevel = raster.getSample(x, y, 0) * mask[0] +
                                raster.getSample(x, y, 1) * mask[1] +
                                raster.getSample(x, y, 2) * mask[2] + tol;
                    writableRaster.setSample(x, y, 0, grayLevel);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return outImage;
    }

    public BufferedImage rgb2hsl(BufferedImage inImage) {
        BufferedImage outImage = null;
        try {
            double var_R, var_G, var_B;
            double H = 0, S, L;
            double var_Min, var_Max, del_Max;
            double del_R, del_G, del_B;
            outImage = new BufferedImage(inImage.getWidth(),inImage.getHeight(),BufferedImage.TYPE_3BYTE_BGR);
            WritableRaster writableRaster = outImage.getRaster();
            Raster raster = inImage.getRaster();

            for(int x = 0; x < writableRaster.getWidth(); x++) {
                for (int y = 0; y < writableRaster.getHeight(); y++) {
                    var_R = (raster.getSample(x, y, 0) / 255 );
                    var_G = (raster.getSample(x, y, 1) / 255 );
                    var_B = (raster.getSample(x, y, 2) / 255 );
                    double[] rgbArr = {var_R, var_G, var_B};
                    var_Min = imageUtils.findMinimum(rgbArr);
                    var_Max = imageUtils.findMaximum(rgbArr);
                    del_Max = var_Max - var_Min;

                    L = ( var_Max + var_Min ) / 2;

                    if (del_Max == 0){
                        H = 0; S = 0;
                    }
                    else {
                        if (L < 0.5)
                            S = del_Max /(var_Max + var_Min);
                        else
                            S = del_Max /(2 - var_Max - var_Min);

                        del_R = (((var_Max - var_R) / 6) + (del_Max / 2)) / del_Max;
                        del_G = (((var_Max - var_G) / 6) + (del_Max / 2)) / del_Max;
                        del_B = (((var_Max - var_B) / 6) + (del_Max / 2)) / del_Max;

                        if ( var_R == var_Max )
                            H = del_B - del_G;
                        else if(var_G == var_Max)
                            H = (1 / 3) + del_R - del_B;
                        else if(var_B == var_Max)
                            H = (2 / 3) + del_G - del_R;

                        if (H < 0)
                            H = H + 1;
                        else if(H > 1)
                            H = H - 1;
                    }
                    writableRaster.setSample(x, y, 0, H);
                    writableRaster.setSample(x, y, 1, S);
                    writableRaster.setSample(x, y, 2, L);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return outImage;
    }

    public BufferedImage hsl2rgb(BufferedImage inImage) {
        BufferedImage outImage = null;
        try {
            double R, G, B;
            double H, S, L;
            double var_2, var_1;
            outImage = new BufferedImage(inImage.getWidth(),inImage.getHeight(),BufferedImage.TYPE_3BYTE_BGR);
            WritableRaster writableRaster = outImage.getRaster();
            Raster raster = inImage.getRaster();

            for(int x = 0; x < writableRaster.getWidth(); x++) {
                for (int y = 0; y < writableRaster.getHeight(); y++) {
                    H = raster.getSample(x, y, 0);
                    S = raster.getSample(x, y, 1);
                    L = raster.getSample(x, y, 2);
                    if(S==0){
                        R = L*255;
                        G = L*255;
                        B = L*255;
                    }
                    else{
                        if(L < 0.5)
                            var_2 = L * (1 + S);
                        else
                            var_2 = (L + S) - (S * L);
                        var_1 = 2 * L - var_2;
                        R = 255 * hue_2_rgb(var_1, var_2, H + (1 / 3)) ;
                        G = 255 * hue_2_rgb(var_1, var_2, H);
                        B = 255 * hue_2_rgb(var_1, var_2, H - (1 / 3));
                    }
                    writableRaster.setSample(x, y, 0, R);
                    writableRaster.setSample(x, y, 1, G);
                    writableRaster.setSample(x, y, 2, B);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return outImage;
    }

    private double hue_2_rgb(double v1, double v2, double vH){
        if (vH < 0)
            vH = vH + 1;
        if (vH > 1)
            vH = vH - 1;
        if((6 * vH) < 1)
            return (v1 + (v2 - v1) * 6 * vH);
        else if((2 * vH) < 1)
            return v2;
        else if((3 * vH) < 2)
            return (v1 + (v2 - v1) * ((2 / 3) - vH) * 6);
        else
            return v1;
    }

    public BufferedImage rgb2hsv(BufferedImage inImage) {
        BufferedImage outImage = null;
        try {
            double var_R, var_G, var_B;
            double H = 0, S, V;
            double var_Min, var_Max, del_Max;
            double del_R, del_G, del_B;
            outImage = new BufferedImage(inImage.getWidth(),inImage.getHeight(),BufferedImage.TYPE_3BYTE_BGR);
            WritableRaster writableRaster = outImage.getRaster();
            Raster raster = inImage.getRaster();

            for(int x = 0; x < writableRaster.getWidth(); x++) {
                for (int y = 0; y < writableRaster.getHeight(); y++) {
                    var_R = (raster.getSample(x, y, 0) / 255 );
                    var_G = (raster.getSample(x, y, 1) / 255 );
                    var_B = (raster.getSample(x, y, 2) / 255 );
                    double[] rgbArr = {var_R, var_G, var_B};
                    var_Min = imageUtils.findMinimum(rgbArr);
                    var_Max = imageUtils.findMaximum(rgbArr);
                    del_Max = var_Max - var_Min;

                    V = var_Max;

                    if (del_Max == 0){
                        H = 0; S = 0;
                    }
                    else {
                        S = del_Max / var_Max;
                        del_R = (((var_Max - var_R) / 6) + (del_Max / 2)) / del_Max;
                        del_G = (((var_Max - var_G) / 6) + (del_Max / 2)) / del_Max;
                        del_B = (((var_Max - var_B) / 6) + (del_Max / 2)) / del_Max;

                        if ( var_R == var_Max )
                            H = del_B - del_G;
                        else if(var_G == var_Max)
                            H = (1 / 3) + del_R - del_B;
                        else if(var_B == var_Max)
                            H = (2 / 3) + del_G - del_R;

                        if (H < 0)
                            H = H + 1;
                        else if(H > 1)
                            H = H - 1;
                    }
                    writableRaster.setSample(x, y, 0, H);
                    writableRaster.setSample(x, y, 1, S);
                    writableRaster.setSample(x, y, 2, V);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return outImage;
    }

    public BufferedImage hsv2rgb(BufferedImage inImage) {
        BufferedImage outImage = null;
        try {
            double R, G, B;
            double H, S, V;
            double var_h, var_1, var_2, var_3, var_i;
            double var_r, var_g, var_b;
            outImage = new BufferedImage(inImage.getWidth(),inImage.getHeight(),BufferedImage.TYPE_3BYTE_BGR);
            WritableRaster writableRaster = outImage.getRaster();
            Raster raster = inImage.getRaster();

            for(int x = 0; x < writableRaster.getWidth(); x++) {
                for (int y = 0; y < writableRaster.getHeight(); y++) {
                    H = raster.getSample(x, y, 0);
                    S = raster.getSample(x, y, 1);
                    V = raster.getSample(x, y, 2);
                    if(S==0){
                        R = V*255;
                        G = V*255;
                        B = V*255;
                    }
                    else{
                        var_h = H * 6;
                        if (var_h == 6)
                            var_h = 0;

                        var_i = Math.floor(var_h);
                        var_1 = V * (1 - S);
                        var_2 = V * (1 - S * (var_h - var_i));
                        var_3 = V * (1 - S * (1 - (var_h - var_i)));

                        if(var_i == 0) {
                            var_r = V;
                            var_g = var_3;
                            var_b = var_1;
                        }
                        else if(var_i == 1) {
                            var_r = var_2; var_g = V; var_b = var_1;
                        }
                        else if(var_i == 2) {
                            var_r = var_1; var_g = V; var_b = var_3;
                        }
                        else if(var_i == 3) {
                            var_r = var_1; var_g = var_2; var_b = V;
                        }
                        else if(var_i == 4) {
                            var_r = var_3; var_g = var_1; var_b = V;
                        }
                        else {
                            var_r = V; var_g = var_1; var_b = var_2;
                        }
                        R = var_r * 255;
                        G = var_g * 255;
                        B = var_b * 255;
                    }
                    writableRaster.setSample(x, y, 0, R);
                    writableRaster.setSample(x, y, 1, G);
                    writableRaster.setSample(x, y, 2, B);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return outImage;
    }

    public BufferedImage rgb2ycbcr(BufferedImage inImage) {
        BufferedImage outImage = null;
        try {
            double Y, Cb, Cr;
            double R,G,B;
            outImage = new BufferedImage(inImage.getWidth(),inImage.getHeight(),BufferedImage.TYPE_3BYTE_BGR);
            WritableRaster writableRaster = outImage.getRaster();
            Raster raster = inImage.getRaster();

            for(int x = 0; x < writableRaster.getWidth(); x++) {
                for (int y = 0; y < writableRaster.getHeight(); y++) {
                    R = raster.getSample(x, y, 0);
                    G = raster.getSample(x, y, 1);
                    B = raster.getSample(x, y, 2);

                    Y = 16 + (0.257*R + 0.504*G + 0.098*B);
                    Cb = 128 + (-0.148*R - 0.291*G + 0.439*B);
                    Cr = 128 + (0.439*R - 0.368*G - 0.071*B);

                    Y = imageUtils.getLowerUpperBoundedValue(Y,16,235);
                    Cb = imageUtils.getLowerUpperBoundedValue(Cb,16,240);
                    Cr = imageUtils.getLowerUpperBoundedValue(Cr,16,240);

                    writableRaster.setSample(x, y, 0, Y);
                    writableRaster.setSample(x, y, 1, Cb);
                    writableRaster.setSample(x, y, 2, Cr);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return outImage;
    }

    public BufferedImage ycbcr2rgb(BufferedImage inImage) {
        BufferedImage outImage = null;
        try {
            double Y, Cb, Cr;
            double R,G,B;
            outImage = new BufferedImage(inImage.getWidth(),inImage.getHeight(),BufferedImage.TYPE_3BYTE_BGR);
            WritableRaster writableRaster = outImage.getRaster();
            Raster raster = inImage.getRaster();

            for(int x = 0; x < writableRaster.getWidth(); x++) {
                for (int y = 0; y < writableRaster.getHeight(); y++) {
                    Y = raster.getSample(x, y, 0);
                    Cb = raster.getSample(x, y, 1);
                    Cr = raster.getSample(x, y, 2);

                    R = (1.164*(Y-16) + 1.596*(Cr - 128));
                    G = (1.164*(Y-16) - 0.392*(Cb - 128) - 0.813*(Cr - 128));
                    B = (1.164*(Y-16) + 2.017*(Cb - 128));

                    R = imageUtils.getLowerUpperBoundedValue(R,0,255);
                    G = imageUtils.getLowerUpperBoundedValue(G,0,255);
                    B = imageUtils.getLowerUpperBoundedValue(B,0,255);

                    writableRaster.setSample(x, y, 0, R);
                    writableRaster.setSample(x, y, 1, G);
                    writableRaster.setSample(x, y, 2, B);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return outImage;
    }


}
