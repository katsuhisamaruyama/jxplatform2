/*
 *     JRBProperties.java  Nov 4, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.gui;
import java.io.*;
import java.net.URL;
import java.util.Properties;

public class JRBProperties {
    private static JRBProperties singleton = new JRBProperties();
    public static final String PROPERTIES_NAME = "/JRB.properties";
    private static Properties properties = new Properties();
    
    private JRBProperties() {
    }

    public static JRBProperties getInstance() {
        return singleton;
    }

    public static Properties getProperties() {
        return properties;
    }

    public static void setProperty(String key, String value) {
        if (properties != null) {
            properties.setProperty(key, value);
        }
    }

    public static String getProperty(String key) {
        if (properties != null) {
            return properties.getProperty(key);
        }
        return null;
    }

    public static String getPropertyName() {
        if (System.getProperty("os.name").startsWith("Windows")) {
            return PROPERTIES_NAME + ".Windows";
        } else {
            return PROPERTIES_NAME + ".Unix";
        }
    }

    public static boolean load() {
        String propertyName = getPropertyName();
        try {
            InputStream is = JRB.class.getResourceAsStream(propertyName);
            if (is == null) {
                return false;
            }
            properties.load(is);
            is.close();
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public static boolean store() {
        String propertyName = getPropertyName();
        try {
            URL url = JRB.class.getResource(propertyName);
            OutputStream os = new FileOutputStream(url.getFile());
            properties.store(os,
              "Java Refactoring Browser properties for " + System.getProperty("os.name"));
            os.close();
        } catch (IOException e) {
            return false;
        }
        return true;
    }
}
