/*
 *     JavaFilePool.java  Nov 12, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.parser;
import jp.ac.ritsumei.cs.fse.jrt.model.*;
import java.util.*;
import java.io.*;

public class JavaFilePool {
    private static JavaFilePool filePool = new JavaFilePool();
    private static final String fileName = "javafilepool.dat";
    private ArrayList files = new ArrayList();  // JavaFile

    private JavaFilePool() {
    }

    public static JavaFilePool getInstance() {
        return filePool;
    }

    public JavaFile checkJavaFile(String qname) {
        Iterator it = files.iterator();
        while (it.hasNext()) {
            JavaFile jfile = (JavaFile)it.next();

            if (jfile.isValid() && qname.compareTo(jfile.getName()) == 0) {
                if (jfile.hasChanged()) {
                    removeParsedFile(jfile);
                    return null;
                }
                return jfile;
            }
        }
        return null;
    }

    public boolean exists(String qname) {
        if (checkJavaFile(qname) != null) {
            return true;
        }
        return false;
    }
    
    public boolean exists(JavaFile jfile) {
        if (jfile != null && jfile.isValid() && files.contains(jfile)) {
            if (jfile.hasChanged()) {
                return true;
            }
            removeParsedFile(jfile);
        }
        return false;
    }

    public boolean isParsed(String qname) {
        JavaFile jfile = checkJavaFile(qname);
        if (jfile != null && jfile.isParsed()) {
            return true;
        }
        return false;
    }

    public void addParsedFile(JavaFile jfile) {
        files.add(jfile);
    }

    public void removeAllParsedFiles() {
        files.clear();
    }

    public void removeParsedFile(JavaFile jfile) {
        files.remove(jfile);
    }

    public void removeParsedFile(String qname) {
        JavaFile jfile = checkJavaFile(qname);
        if (jfile != null) {
            files.remove(jfile);
        }
    }

    public void removeNonParsedFile() {
        ArrayList parsedFiles = new ArrayList(files);
        Iterator it = parsedFiles.iterator();
        while (it.hasNext()) {
            JavaFile jfile = (JavaFile)it.next();
            if (!jfile.isParsed()) {
                files.remove(jfile);
            }
        }
    }

    public void save() {
        try {
            FileOutputStream fos = new FileOutputStream(fileName);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeInt(files.size());
            for (int i = 0; i < files.size(); i++) {
                JavaFile jfile = (JavaFile)files.get(i);
                oos.writeObject(jfile);
            }
            oos.flush();
            oos.close();
        } catch (IOException e) {
            System.err.println("Cannot save JavaFile information");
        }
    }

    public void restore() {
        try {
            FileInputStream fis = new FileInputStream(fileName);
            ObjectInputStream ois = new ObjectInputStream(fis);
            int size = (int)ois.readInt();
            for (int i = 0; i < size; i++) {
                JavaFile jfile = (JavaFile)ois.readObject();
                files.add(jfile);
            }
            ois.close();
        } catch (IOException e) {
            System.err.println("Cannot restore JavaFile information");
        } catch (ClassNotFoundException e) {
            System.err.println("Cannot restore JavaFile information");
        }
    }

    public void print() {
        Iterator it = files.iterator();
        while (it.hasNext()) {
            JavaFile jfile = (JavaFile)it.next();
            System.out.println(jfile.getName());
        }
    }
}
