/*
 *     Summary.java  Nov 20, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.parser.summary;
import jp.ac.ritsumei.cs.fse.jrt.parser.JDKZipFile;
import jp.ac.ritsumei.cs.fse.jrt.util.SimpleEventSource;
import jp.ac.ritsumei.cs.fse.jrt.util.WarningEvent;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.*;

public class Summary extends SimpleEventSource {
    private static Summary summary = new Summary();
    private ArrayList files = new ArrayList();  // SummaryJavaFile
    private ArrayList JDKFiles = new ArrayList();  // SummaryJavaFile
    private static SummaryJavaParser parser = null;
    private boolean inProgress = false;

    private Summary() {
        super();
    }

    public static Summary getInstance() {
        return summary;
    }

    public void setJDKFile(String name) {
        JDKZipFile.setJDKZipFile(name);
    }

    public ArrayList getSummaryJavaFiles() {
        return files;
    }

    public void clear() {
        files.clear();
    }

    public void parse(String dir) {
        parseAllFiles(dir);
        setParents();
        setAncestors();
    }

    public void reparse(String dir) {
        clear();
        parse(dir);
    }

    public void addParsedFile(SummaryJavaFile jfile) {
        files.add(jfile);
    }

    public void removeParsedFile(SummaryJavaFile jfile) {
        if (jfile != null) {
            files.remove(jfile);
        }
    }

    public void removeParsedFile(String name) {
        removeParsedFile(getJavaFile(name));
    }

    public void removeAllParsedSummaryFiles() {
        files.clear();
    }

    public void parseEach(String name) {
        try {
            File file = new File(name);
            parseEachFile(file.getCanonicalPath());
        } catch (IOException e) {
            // System.err.println("Encounted Parse Error");
        }
    }

    public boolean getInProgress() {
        return inProgress;
    }

    private void setInProgress(boolean bool) {
        inProgress = bool;
    }
    
    public synchronized void collectInformation() {
        setInProgress(true);
        clearAncestors();
        setParents();
        setAncestors();
        setInProgress(false);
    }

    private void parseAllFiles(String dir) {
        File directory = new File(dir);
        String[] names = directory.list();
        for (int i = 0; i < names.length; i++) {
            File file = new File(dir, names[i]);
            if (file.isDirectory()) {
                parseAllFiles(file.getPath());
            }
            if (file.isFile() && names[i].endsWith(".java")) {
                try {
                    parseEachFile(file.getCanonicalPath());
                } catch (IOException e) {
                    // System.err.println("Encounted Parse Error");
                }
            }
        }
    }

    public SummaryJavaFile parseEachFileWithError(String name, String text) {
        try {
            return parseText(name, text);

        } catch (ParseException e) {
            fireWarningEvent(new WarningEvent(this, "Parse error: " + e.getMessage()));
        }
        return null;
    }

    public SummaryJavaFile parseEachFile(String name, String text) {
        try {
            return parseText(name, text);

        } catch (ParseException e) {
            // System.err.println("Encounted Parse Error");
        }
        return null;
    }

    private SummaryJavaFile parseText(String name, String text) throws ParseException {
        File tmpFile = null;
        SummaryJavaFile sfile = null;

        try {
            File file = new File(name);
            tmpFile = File.createTempFile("TemporaryTargetFile", ".java", file.getParentFile());

            FileWriter writer = new FileWriter(tmpFile);
            writer.write(text);
            writer.flush();
            writer.close();

            sfile = parseText(tmpFile.getAbsolutePath());
            if (sfile != null) {
                sfile.setName(name);
            }

        } catch (IOException e) {
            fireWarningEvent(new WarningEvent(this, "Failed to create temporary file."));

        } catch (ParseException e) {
            throw e;

        } finally {
            if (tmpFile != null) {
                tmpFile.delete();
            }
        }
        return sfile;
    }

    public SummaryJavaFile parseEachFile(String name) {
        try {
            return parseText(name);

        } catch (ParseException e) {
            // System.err.println("Encounted Parse Error");
        }
        return null;
    }

    private synchronized SummaryJavaFile parseText(String name) throws ParseException {
        setInProgress(true);
        SummaryJavaFile jfile = null;
        InputStream is = null;

        try {
            is = new FileInputStream(name);
            if (parser == null) {
                parser = new SummaryJavaParser(is);
            } else {
                parser.ReInit(is);
            }
            jfile = parser.run(name);

            removeParsedFile(name);
            addParsedFile(jfile);

        } catch (FileNotFoundException e) {
            fireWarningEvent(new WarningEvent(this, "Failed to open: " + name + "."));

        } catch (IOException e) {

        } catch (ParseException e) {
            removeParsedFile(name);
            throw e;

        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) { }
            }
            setInProgress(false);
        }
        return jfile;
    }

    private void clearAncestors() {
        Iterator it = files.iterator();
        while (it.hasNext()) {
            SummaryJavaFile jfile = (SummaryJavaFile)it.next();
            jfile.clearAncestors();
        }
    }

    private void setParents() {
        Iterator it = files.iterator();
        while (it.hasNext()) {
            SummaryJavaFile jfile = (SummaryJavaFile)it.next();
            jfile.setParents();
        }
    }

    private void setAncestors() {
        Iterator it = files.iterator();
        while (it.hasNext()) {
            SummaryJavaFile jfile = (SummaryJavaFile)it.next();
            jfile.setAncestors();
        }
    }

    public boolean exists(String name) {
        if (getJavaFile(name) != null) {
            return true;
        }
        return false;
    } 
       
    public SummaryJavaFile getJavaFile(String name) {
        if (name.indexOf("#") != -1) {
            name = name.substring(0, name.indexOf("#"));
        }

        File file = new File(name);
        try {
            String cname = file.getCanonicalPath();
            Iterator it = files.iterator();
            while (it.hasNext()) {
                SummaryJavaFile jfile = (SummaryJavaFile)it.next();
                if (cname.compareTo(jfile.getName()) == 0) {
                    return jfile;
                }
            }

        } catch (IOException e) {
            // System.err.println("Encounted Parse Error");
        } 
        return null;
    }

    public SummaryJavaFile getJavaFileInJDK(String name) {
        String fname = "src" + name.substring(name.indexOf(":") + 1) + ".java";
        Iterator it = JDKFiles.iterator();
        while (it.hasNext()) {
            SummaryJavaFile jfile = (SummaryJavaFile)it.next();
            if (fname.compareTo(jfile.getName()) == 0) {
                return jfile;
            }
        }
        return null;
    }

    public String getQualifiedName(SummaryJavaFile jfile, String name) {
        if (jfile != null) {
            return jfile.getQualifiedName(name);
        }
        return null;
    }

    public String getQualifiedNameList(SummaryJavaFile jfile, String nameList) {
        if (jfile != null) {
            return jfile.getQualifiedNameList(nameList);
        }
        return null;
    }

    public List getFilesInSamePackage(SummaryJavaFile jfile) {
        String pname = jfile.getPackageName();

        List collection = new ArrayList();
        if (pname == null || pname.length() == 0) {
            File dir = new File(jfile.getDirName());
            String[] names = dir.list();
            for (int i = 0; i < names.length; i++) {
                if (names[i].endsWith(".java")) {
                    String fname = jfile.getDirName() + File.separator + names[i];
                    SummaryJavaFile jf = getJavaFile(fname);
                    if (jf != null) {
                        collection.add(jf);
                    }
                }
            }

        } else {
            Iterator it = files.iterator();
            while (it.hasNext()) {
                SummaryJavaFile jf = (SummaryJavaFile)it.next();
                if (jf.getPackageName() != null && pname.compareTo(jf.getPackageName()) == 0) {
                    collection.add(jf);
                }
            }
        }
        return collection;
    }

    public void parseJDKFiles() {
        Iterator it = JDKZipFile.getEntires().iterator();
        while (it.hasNext()) {
            String name = (String)it.next();
            parseJDKFile(name);
        }
    }

    public void parseJDKFile(String name) {
        InputStream is = JDKZipFile.getInputStream(name);

        try {
            if (parser == null) {
                parser = new SummaryJavaParser(is);
            } else {
                parser.ReInit(is);
            }

            SummaryJavaFile jfile = parser.run(name);
            is.close();
            JDKFiles.add(jfile);

        } catch (IOException e) {
        } catch (ParseException e) {
            // System.err.println("Encounted Parse Error");
        }
    }

    public void print() {
        Iterator it = files.iterator();
        while (it.hasNext()) {
            SummaryJavaFile jfile = (SummaryJavaFile)it.next();
            jfile.print();
        }
    }
}
