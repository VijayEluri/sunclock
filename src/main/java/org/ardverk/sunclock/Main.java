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

import java.awt.Image;
import java.io.IOException;
import java.net.URL;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;
import javax.swing.JLabel;

public class Main {
    
    public static void main(String[] args) throws IOException {
        URL dayUrl = Main.class.getResource("world.jpg");
        URL nightUrl = Main.class.getResource("world_night.jpg");
        //URL cloudsUrl = new URL("http://xplanet.sourceforge.net/clouds/clouds_2048.jpg");
        
        Image dayImage = ImageUtils.load(dayUrl);
        Image nightImage = ImageUtils.load(nightUrl);
        //Image cloudsImage = ImageUtils.load(cloudsUrl);
        
        final World world = new World(dayImage, nightImage);
        //final Clouds clouds = new Clouds(cloudsImage, 0.4f);
        
        //final CompositeIcon composite 
        //    = new CompositeIcon(world, clouds);
        
        final JFrame frame = new JFrame();
        frame.getContentPane().add(new JLabel(world));
        //frame.getContentPane().add(new JLabel(composite));
        frame.setBounds(20, 30, 1000, 500);
        frame.setVisible(true);
        
        Runnable task = new Runnable() {
            
            private final Calendar calendar 
                = GregorianCalendar.getInstance(Locale.US);
            
            @Override
            public void run() {
                world.setTime(calendar.getTimeInMillis());
                frame.setTitle(toTimeString(calendar));
                calendar.add(Calendar.MONTH, 1);
                
                frame.repaint();
            }
        };
        
        ScheduledExecutorService executor 
            = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleWithFixedDelay(task, 0, 3, TimeUnit.SECONDS);
    }
    
    private static String toTimeString(Calendar cal) {
        String month = cal.getDisplayName(Calendar.MONTH, 
                Calendar.LONG, Locale.US);
        
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        int second = cal.get(Calendar.SECOND);
        
        StringBuilder buffer = new StringBuilder();
        
        buffer.append(month).append(" @ ");
        append(buffer, hour).append(":");
        append(buffer, minute).append(":");
        append(buffer, second);
        
        return buffer.toString();
    }
    
    private static StringBuilder append(StringBuilder buffer, int value) {
        if (value < 10) {
            buffer.append("0");
        }
        return buffer.append(value);
    }
    
    /*private static class CompositeIcon implements Icon {

        private final Icon[] icons;
        
        public CompositeIcon(Icon... icons) {
            if (icons == null) {
                throw new NullPointerException("icons");
            }
            
            this.icons = icons;
        }
        
        @Override
        public int getIconHeight() {
            int height = 0;
            for (Icon icon : icons) {
                height = Math.max(icon.getIconHeight(), height);
            }
            return height;
        }

        @Override
        public int getIconWidth() {
            int width = 0;
            for (Icon icon : icons) {
                width = Math.max(icon.getIconWidth(), width);
            }
            return width;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            for (Icon icon : icons) {
                icon.paintIcon(c, g, x, y);
            }
        }
    }*/
}
