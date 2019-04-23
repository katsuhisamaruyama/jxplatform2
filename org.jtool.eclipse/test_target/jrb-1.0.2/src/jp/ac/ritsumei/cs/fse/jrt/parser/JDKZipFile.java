/*
 *     JDKZipFile.java  Nov 17, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.parser;
import jp.ac.ritsumei.cs.fse.jrt.model.JavaFile;
import java.util.jar.*;
import java.util.zip.*;
import java.util.*;
import java.io.*;

public class JDKZipFile {
    public static final String JDKPREFIX = "/JDKFILE:";
    private static ZipFile zipFile;
    private static ArrayList jdkClassNames = new ArrayList();  // String

    public static void setJDKZipFile(String name) {
        try {
            if (name.endsWith(".jar")) {
                zipFile = new JarFile(name);
            } else if (name.endsWith("zip")) {
                zipFile = new ZipFile(name);
            }
        } catch (IOException e) {
            // System.err.println("Failed to open: " + name + ".");
            return;
        }

        Enumeration e = zipFile.entries();
        while (e.hasMoreElements()) {
            ZipEntry entry = (ZipEntry)e.nextElement();
            String zname = entry.getName();
            jdkClassNames.add(zname.replace('/', File.separatorChar));
        }
    }

    public static String getJDKFile() {
        return zipFile.getName();
    }

    public static boolean in(String name) {
        Iterator it = jdkClassNames.iterator();
        while (it.hasNext()) {
            String zname = (String)it.next();
            if (name.compareTo(zname) == 0) {
                return true;
            }
        }
        return false;
    }

    public static List getEntires() {
        List collection = new ArrayList();

        Enumeration e = zipFile.entries();
        while (e.hasMoreElements()) {
            ZipEntry entry = (ZipEntry)e.nextElement();
            if (entry.getName().endsWith(".java")) {
                collection.add(entry.getName());
            }
        }
        return collection;
    }

    public static InputStream getInputStream(String name) {
        try {
            return zipFile.getInputStream(zipFile.getEntry(name));
        } catch (IOException e) {
            return null;
        }
    }
}
