/*
 * Copyright 2010 Roger Kapsi
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.ardverk.sunclock;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

class ImageUtils {

    private ImageUtils() {}

    /**
     * Takes the image brightness and turns it into an alpha-channel.
     */
    public static BufferedImage brightnessToAlpha(
            Image image, float alpha) {
        return brightnessToAlpha(image, (int)(255f * alpha));
    }
    
    /**
     * Takes the image brightness and turns it into an alpha-channel.
     */
    public static BufferedImage brightnessToAlpha(
            Image image, int alpha) {
        
        if (image == null) {
            throw new NullPointerException("image");
        }
        
        if (alpha < 0 || 255 < alpha) {
            throw new IllegalArgumentException("alpha=" + alpha);
        }
        
        BufferedImage src = toBufferedImage(image);
        
        int width = src.getWidth();
        int height = src.getHeight();
        
        int[] rgb = new int[width * height];
        src.getRGB(0, 0, width, height, rgb, 0, width);
        
        BufferedImage dst = new BufferedImage(
                width, height, BufferedImage.TYPE_INT_ARGB);
        
        int value, r, g, b, a;
        float[] hsb = new float[3];
        
        for (int i = 0; i < rgb.length; i++) {
            value = rgb[i];
            
            // Get the RGB components
            r = (value >> 16) & 0xFF;
            g = (value >>  8) & 0xFF;
            b = (value      ) & 0xFF;
            
            // Turn RGB into HSB
            Color.RGBtoHSB(r, g, b, hsb);
            
            // Use the Brightness to compute the alpha value
            a = (int)(hsb[2] * alpha);
            
            // Re-Create the pixel with the new alpha value
            rgb[i] = (value & 0x00FFFFFF) | (a << 24);
        }
        
        dst.setRGB(0, 0, width, height, rgb, 0, width);
        return dst;
    }

    /**
     * Scales an image to the given dimensions.
     */
    public static BufferedImage scale(Image image, int w, int h) {
        if (image == null) {
            throw new NullPointerException("image");
        }
        
        if (w < 0) {
            throw new IllegalArgumentException("width=" + w);
        }
        
        if (h < 0) {
            throw new IllegalArgumentException("height=" + w);
        }
        
        int width = image.getWidth(null);
        int height = image.getHeight(null);
        
        if (width == w && height == h) {
            return toBufferedImage(image);
        }
        
        double sx = (double)width/(double)w;
        double sy = (double)height/(double)h;
        
        AffineTransform tx = AffineTransform.getScaleInstance(sx, sy);
        
        BufferedImage dst = new BufferedImage(
                width, height, BufferedImage.TYPE_INT_ARGB);
        
        Graphics2D gfx = dst.createGraphics();
        gfx.setTransform(tx);
        gfx.drawImage(image, 0, 0, null);
        gfx.dispose();
        return dst;
    }
    
    /**
     * Takes an {@link Image} and adds an alpha-channel component to it.
     * In other words, a RGB image is turned into an ARGB image.
     */
    public static BufferedImage createWithAlpha(Image image) {
        if (image == null) {
            throw new NullPointerException("image");
        }
        
        BufferedImage src = toBufferedImage(image);
        
        // Check if it has already an alpha channel
        WritableRaster alpha = src.getAlphaRaster();
        if (alpha != null) {
            return src;
        }
        
        // If not create a new BufferedImage with an alpha channel
        int width = src.getWidth();
        int height = src.getHeight();
        
        BufferedImage dst = new BufferedImage(
                width, height, BufferedImage.TYPE_INT_ARGB);
                
        Graphics2D gfx = dst.createGraphics();
        gfx.drawImage(image, 0, 0, null);
        gfx.dispose();
        return dst;
    }
    
    /**
     * Takes an {@link Image} and turns it into a {@link BufferedImage}.
     */
    public static BufferedImage toBufferedImage(Image image) {
        if (image == null) {
            throw new NullPointerException("image");
        }
        
        if (image instanceof BufferedImage) {
            return (BufferedImage)image;
        }
        
        int width = image.getWidth(null);
        int height = image.getHeight(null);
        
        BufferedImage dst = new BufferedImage(
                width, height, BufferedImage.TYPE_INT_ARGB);
        
        Graphics2D gfx = dst.createGraphics();
        gfx.drawImage(image, 0, 0, null);
        gfx.dispose();
        return dst;
    }
    
    /**
     * Loads an image from the given {@link URL}.
     */
    public static BufferedImage load(URL url) throws IOException {
        BufferedImage image = ImageIO.read(url);
        return createWithAlpha(image);
    }
}
