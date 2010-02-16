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

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

import javax.swing.Icon;

class World implements Icon {

    private final BufferedImage day;
    
    private final BufferedImage night;
    
    private volatile CompositeRef compositeRef;
    
    private volatile long time;

    public World(Image day, Image night) {
        this(day, night, System.currentTimeMillis());
    }
    
    public World(Image day, Image night, long time) {
        this.day = ImageUtils.createWithAlpha(day);
        this.night = ImageUtils.createWithAlpha(night);
        this.time = time;
        
        if (this.day.getWidth() != this.night.getWidth() 
                || this.day.getHeight() != this.night.getHeight()) {
            throw new IllegalArgumentException();
        }
    }
    
    @Override
    public int getIconHeight() {
        return day.getHeight();
    }

    @Override
    public int getIconWidth() {
        return day.getWidth();
    }
    
    public long getTime() {
        return time;
    }
    
    public void setTime(long time) {
        this.time = time;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        int width = c.getWidth();
        int height = c.getHeight();
        
        CompositeRef compositeRef = this.compositeRef;
        
        if (compositeRef == null || compositeRef.time != time) {
            BufferedImage composite = createCompositeImage(time);
            compositeRef = new CompositeRef(composite, time);
            
            this.compositeRef = compositeRef;
        }
        
        Image image = compositeRef.getScaledInstance(width, height);
        g.drawImage(image, 0, 0, null);
    }
    
    private BufferedImage createCompositeImage(long time) {
        int imageWidth = day.getWidth();
        int imageHeight = day.getHeight();
        
        int[] mask = ClockUtils.createMask(time, imageWidth, imageHeight);
        
        WritableRaster alphaMask = night.getAlphaRaster();
        alphaMask.setPixels(0, 0, imageWidth, imageHeight, mask);
        
        BufferedImage composite = new BufferedImage(
                imageWidth, imageHeight, 
                BufferedImage.TYPE_INT_ARGB);
        
        Graphics2D gfx = composite.createGraphics();
        gfx.drawImage(day, 0, 0, null);
        gfx.drawImage(night, 0, 0, null);
        
        gfx.dispose();
        
        return composite;
    }
    
    private static class CompositeRef {
        
        private final BufferedImage composite;
        
        private final long time;
        
        private Image image = null;
        
        public CompositeRef(BufferedImage composite, long time) {
            this.composite = composite;
            this.time = time;
        }
        
        public Image getScaledInstance(int width, int height) {
            if (image == null || image.getWidth(null) != width
                    || image.getHeight(null) != height) {
                image = composite.getScaledInstance(
                    width, height, BufferedImage.SCALE_DEFAULT);
            }
            
            return image;
        }
    }
}
