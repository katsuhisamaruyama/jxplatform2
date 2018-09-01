/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.util;

import java.util.Map;
import java.util.HashMap;

/**
 * Collects command-line options.
 * 
 * @author Katsuhisa Maruyama
 */
public class Options {
    
    private Map<String, String> options = new HashMap<String, String>();
    
    public Options(String[] args) {
        for (int count = 0; count < args.length; ) {
            if (args[count].charAt(0) == '-') {
                String key = args[count].trim().substring(1);
                count++;
                if (count < args.length) {
                    if (args[count].charAt(0) != '-') {
                        String value = args[count];
                        options.put(key, value.trim());
                        count++;
                    } else {
                        options.put(key, "yes");
                    }
                }
            }
        }
    }
    
    public String get(String key, String defaultValue) {
        key = key.trim().substring(1);
        String value = options.get(key);
        if (value != null) {
            return value;
        } else {
            return defaultValue;
        }
    }
    
    public int get(String key, int defaultValue) {
        String value = options.get(key);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        } else {
            return defaultValue;
        }
    }
    
   
    public long get(String key, long defaultValue) {
        String value = options.get(key);
        if (value != null) {
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        } else {
            return defaultValue;
        }
    }
    
    public float get(String key, float defaultValue) {
        String value = options.get(key);
        if (value != null) {
            try {
                return Float.parseFloat(value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        } else {
            return defaultValue;
        }
    }
    
    public double get(String key, double defaultValue) {
        String value = options.get(key);
        if (value != null) {
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        } else {
            return defaultValue;
        }
    }
}
