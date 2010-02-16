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
import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.swing.Icon;

class Clouds implements Icon {

    private final BufferedImage clouds;
    
    private volatile Image image;
    
    public Clouds(Image clouds, float alpha) {
        if (clouds == null) {
            throw new NullPointerException("clouds");
        }
        
        this.clouds = ImageUtils.brightnessToAlpha(clouds, alpha);
    }
    
    @Override
    public int getIconWidth() {
        return clouds.getWidth();
    }
    
    @Override
    public int getIconHeight() {
        return clouds.getHeight();
    }
    
    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        int width = c.getWidth();
        int height = c.getHeight();
        
        Image image = this.image;
        
        if (image == null || image.getWidth(null) != width 
                || image.getHeight(null) != height) {
            image = clouds.getScaledInstance(
                    width, height, BufferedImage.SCALE_DEFAULT);
            this.image = image;
        }
        
        g.drawImage(image, 0, 0, null);
    }
}
