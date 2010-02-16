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

import static java.lang.Math.acos;
import static java.lang.Math.asin;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static java.lang.Math.toDegrees;
import static java.lang.Math.toRadians;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

class ClockUtils {

    private static final TimeZone UTC = TimeZone.getTimeZone("GMT");
    
    private static final double HORIZON_SEA_LEVEL = -50.0/60.0;
    
    private static final double HORIZON_TWILIGHT = -360.0/45.0;
    
    private static final int GRGORIAN_BEGIN_YEAR = 1582;
    
    private static final int GRGORIAN_BEGIN_MONTH = 10;
    
    private static final int GRGORIAN_BEGIN_DAY = 15;
    
    private ClockUtils() {}
    
    public static int[] createMask(int w, int h) {
        return createMask(System.currentTimeMillis(), w, h);
    }
    
    public static int[] createMask(long time, int w, int h) {
        
        int[] mask = new int[w*h];
        
        Calendar cal = GregorianCalendar.getInstance(UTC, Locale.US);
        cal.setTimeInMillis(time);
        
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int month = cal.get(Calendar.MONTH) + 1;
        int year = cal.get(Calendar.YEAR);
        
        double julianDay = julianDay(day, month, year);
        double t = julianCentury(julianDay);
        
        double[] declination = apparentRightAscensionAndDeclination(t);
        double alpha = declination[0];
        double delta = declination[1];
        
        double theta = meanGreenwichSideralTime(t);
        
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        int second = cal.get(Calendar.SECOND);
        double ftime = fractionalTime(hour, minute, second);
        
        int i, j, k;
        double latitude, longitude, cosH0, H0, m0, m1, m2;
        double h0[] = { HORIZON_SEA_LEVEL, HORIZON_TWILIGHT };
        
        for (i = 0; i < h; i++) {
            latitude = 90 - (double)i * 180.0 / (double)h;
            for (j = 0; j < w; j++) {
                
                int x = j;
                int y = h-i-1;
                
                for (k = 0; k < h0.length; k++) {
                    cosH0 = (sin(toRadians(h0[k]))-sin(toRadians(latitude)) *
                            sin(delta)) / (cos(toRadians(latitude))*cos(delta));
                    
                    if (cosH0 > 1.0) { // pixel is always below horizon so it's night
                        if (k == 0) {
                            bitBelow(mask, x, y, w);
                        } else {
                            bitAbove(mask, x, y, w);
                        }
                    } else if (cosH0 >= -1) {
                        H0 = toDegrees(acos(cosH0));
                        // the longitude is intervered since the shadow was going
                        // the wrong way on the map :))
                        
                        longitude = 180.0 - (double)j * 360.0 / (double)w;
                        m0 = (alpha + longitude - theta) / 360.0; // transit
                        while (m0 > 1.0) m0 -= 1.0;
                        while (m0 < 0.0) m0 += 1.0;
                        m1 = m0 - H0/360.0; // sunrise
                        
                        while (m1 > 1.0) m1 -= 1.0;
                        while (m1 < 0.0) m1 += 1.0;
                        m2 = m0 + H0/360.0; // sunset
                        
                        while (m2 > 1.0) m2 -= 1.0;
                        while (m2 < 0.0) m2 += 1.0;
                        
                        if (m1 < m2 && (ftime < m1 || ftime > m2)) {
                            if (k == 0) {
                                bitBelow(mask, x, y, w);
                            } else {
                                bitAbove(mask, x, y, w);
                            }
                            
                        } else if (m1 > m2 && ftime > m2 && ftime < m1) {
                            if (k == 0) {
                                bitBelow(mask, x, y, w);
                            } else {
                                bitAbove(mask, x, y, w);
                            }
                        }
                    }
                }
            }
        }
        
        return mask;
    }
    
    private static void bitBelow(int[] mask, int x, int y, int scansize) {
        mask[(y * scansize) + x] = 0x80;
    }
    
    private static void bitAbove(int[] mask, int x, int y, int scansize) {
        mask[(y * scansize) + x] = 0xFF;
    }
    
    private static double julianDay(int day, int month, int year) {
        int a = 0;
        int b = 0;
        
        boolean isGregorian = false;
        if (year > GRGORIAN_BEGIN_YEAR) {
            isGregorian = true;
        } else if (year == GRGORIAN_BEGIN_YEAR) {
            if (month > GRGORIAN_BEGIN_MONTH) {
                isGregorian = true;
            } else if (month == GRGORIAN_BEGIN_MONTH) {
                if (day >= GRGORIAN_BEGIN_DAY) {
                    isGregorian = true;
                }
            }
        }
        
        if (month < 3) {
            year--;
            month += 3;
        }
        
        if (isGregorian) {
            a = year / 100;
            b = 2 - a + (int)(a / 4);
        }
        
        return ((int)365.25 * (year + 4716)) 
                + (int)(30.6001 * (month + 1)) 
                + day + (double)b - 1524.5;
    }
    
    private static double julianCentury(double julianDay) {
        return ((julianDay - 2451545.0) / 36525.0);
    }
    
    private static double meanLongitude(double t) {
        double longitude = 280.46646 + 36000.76983 * t + pow((0.0003032 * t), 2.0);
        while (longitude > 360.0) {
            longitude -= 360.0;
        }
        
        while (longitude < 0.0) {
            longitude += 360.0;
        }
        
        return longitude;
    }
    
    private static double meanAnomaly(double t) {
        double anomaly = 357.52911 + 35999.05029 * t - pow((0.0001537 * t), 2.0);
        while (anomaly > 360.0) { 
            anomaly -= 360.0;
        }
        
        while (anomaly < 0.0) { 
            anomaly += 360.0;
        }
        
        return anomaly;
    }
    
    private static double centerEquation(double m, double t) {
        return (1.914602 - 0.004817 * t - 0.000014 * (t*t)) * Math.sin(m) +
                (0.019993 - 0.000101 * t) * Math.sin(2.0 * m) +
                0.000289 * Math.sin(3.0 * m);
    }
    
    private static double trueLongitude(double longitude, double c) {
        return (longitude + c);
    }
    
    private static double apparentLongitudeCorrection(double t) {
        return 125.04 - 1934.136 * t;
    }
    
    private static double apparentLongitude(double omicron, double omega, double t) {
        return omicron - 0.00569 - 0.00478 * Math.sin(Math.toRadians(omega));
    }
    
    private static double ecliptiqueObliquity(double t) {
        return (23.0 + (26.0 + ((21.448 - t*(46.8150 +
                t*(0.00059 - t*(0.001813))))/60.0))/60.0);
    }
    
    private static double apparentRightAscension(double epsilon, double omega, double lambda) {
        epsilon += toRadians(0.00256) * cos(omega); // correction low accuracy
        double alpha = toDegrees(atan2((cos(epsilon) * sin(lambda)), (cos(lambda))));
        while (alpha > 360.0) alpha -= 360.0;
        while (alpha < 0) alpha += 360.0;
        return alpha;

    }
    
    private static double apparentDeclination(double epsilon, double omega, double lambda) {
        epsilon += toRadians(0.00256) * cos(omega); // correction low accuracy
        return asin(sin(epsilon) * sin(lambda));
    }
    
    private static double[] apparentRightAscensionAndDeclination(double t) {
        double l0 = meanLongitude(t);
        double m = meanAnomaly(t);
        double c = centerEquation(toRadians(m), t);
        double omicron = trueLongitude(l0, c);
        double omega = apparentLongitudeCorrection(t);
        double lambda = toRadians(apparentLongitude(omicron, omega, t));
        double epsilon = toRadians(ecliptiqueObliquity(t));
        omega = toRadians(omega);
        
        double a = apparentRightAscension(epsilon, omega, lambda);
        double b = apparentDeclination(epsilon, omega, lambda);
        
        return new double[] { a, b };
    }
    
    private static double meanGreenwichSideralTime(double t) {
        double theta = 100.46061837 + 36000.770053608 * t + 0.000387933
                * (t * t) - (t * t * t) / 38710000;
        while (theta > 360.0) {
            theta -= 360.0;
        }
        
        while (theta < 0.0) {
            theta += 360.0;
        }
        
        return theta;
    }
    
    private static double fractionalTime(int hour, int minute, int second) {
        return (double)hour/24.0 
                + (double)minute / (24.0 * 60.0) 
                + (double)second / (24.0 * 60.0 * 60.0);
    }
}
