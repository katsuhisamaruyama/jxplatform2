/*
 *     JavaModelFactory.java  Nov 14, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.parser;
import jp.ac.ritsumei.cs.fse.jrt.parser.summary.*;
import jp.ac.ritsumei.cs.fse.jrt.model.*;
import jp.ac.ritsumei.cs.fse.jrt.util.SimpleEventSource;
import jp.ac.ritsumei.cs.fse.jrt.util.WarningEvent;
import jp.ac.ritsumei.cs.fse.jrt.util.LogEvent;
import java.util.*;
import java.io.*;

public class JavaModelFactory extends SimpleEventSource {
    private static JavaModelFactory factory = new JavaModelFactory();
    private SummaryJavaFile sfile;
    private static JavaParser parser = null;
    private ArrayList relatedFiles = new ArrayList();  // String

    private JavaModelFactory() {
    }

    public static JavaModelFactory getInstance() {
        return factory;
    }
    
    public void setJDKFile(String name) {
        JDKZipFile.setJDKZipFile(name);
    }

    public String getJDKFile() {
        return JDKZipFile.getJDKFile();
    }

    public JavaFile getJavaFile(String fname) {
        if (fname != null) {
            JavaFilePool.getInstance().removeNonParsedFile();
            relatedFiles.clear();
            relatedFiles.add(getAbsolutePath(fname));
            JavaFile jfile = parse();
            return jfile;
        }
        return new JavaFile();
    }

    public JavaFile getEachJavaFile(String fname) {
        if (fname != null) {
            String qname = fname;
            if (fname.indexOf(File.separator) == -1) {
                qname = getAbsolutePath(fname);
            }

            JavaFile jfile = JavaFilePool.getInstance().checkJavaFile(qname);
            if (jfile == null) {
                jfile = parseEachFile(qname);
                JavaFilePool.getInstance().addParsedFile(jfile);
            }
            return jfile;
        }
        return new JavaFile();
    }

    public JavaFile getCloneOfJavaFile(JavaFile jfile) {
        return parseEachFile(jfile.getName(), jfile.getText());
    }

    public JavaFile getParsedFile(String qname) {
        return JavaFilePool.getInstance().checkJavaFile(qname);
    }

    public boolean exists(String qname) {
        return JavaFilePool.getInstance().exists(qname);
    }        

    public boolean exists(JavaFile jfile) {
        return JavaFilePool.getInstance().exists(jfile);
    }

    public void addParsedFile(JavaFile jfile) {
        JavaFilePool.getInstance().addParsedFile(jfile);
    }

    public void removeAllParsedFiles() {
        JavaFilePool.getInstance().removeAllParsedFiles();
    }

    public void removeParsedFile(JavaFile jfile) {
        JavaFilePool.getInstance().removeParsedFile(jfile);
    }

    public void removeParsedSummaryFile(String fname) {
        Summary.getInstance().removeParsedFile(fname);
    }

    public void removeAllParsedSummaryFiles() {
        Summary.getInstance().removeAllParsedSummaryFiles();
    }

    public boolean isParsed(String fname) {
        return JavaFilePool.getInstance().isParsed(getAbsolutePath(fname));
    }

    private String getAbsolutePath(String fname) {
        File file = new File(fname);
        try {
            return file.getCanonicalPath();
        } catch (IOException e) {
            return fname;
        }            
    }

    public void addRelatedFiles(String nameList) {
        if (nameList == null) {
            return;
        }
        StringTokenizer st = new StringTokenizer(nameList, ",");
        while (st.hasMoreTokens()) {
            String qname = st.nextToken();

            if (sfile != null) {
                String name = qname.substring(qname.lastIndexOf(File.separator) + 1);
                if (sfile.getJavaClass(name) == null && !isJDKFile(qname)) {
                    String fname = getAbsolutePath(qname + ".java");
                    JavaFile jfile = JavaFilePool.getInstance().checkJavaFile(fname);
                    if ((jfile == null && !relatedFiles.contains(fname))
                      || (jfile != null && jfile.hasChanged())) {
                        relatedFiles.add(fname);
                    }
                }
            }
        }
    }

    public JavaFile parse() {
        JavaFile targetFile = null;
        while (relatedFiles.size() != 0) {
            String fname = (String)relatedFiles.get(0);
            JavaFile jfile = parseEachFile(fname);
            JavaFilePool.getInstance().addParsedFile(jfile);
            relatedFiles.remove(0);
            if (targetFile == null) {
                targetFile = jfile;
            }
        }
        return targetFile;
    }

    public JavaFile parseEachFile(String fname) {
        if (isJDKFile(fname)) {
            return parseJDKFile(fname);
        }

        SummaryJavaFile sf = Summary.getInstance().getJavaFile(fname);
        try {
            JavaFile jfile = parseEachFile(fname, sf);
            fireLogEvent(new LogEvent(this, "Parsing: " + fname + "."));
            return jfile;

        } catch (ParseException e) {
            return new JavaFile();
        }
    }

    public JavaFile parseEachFile(String name, String text) {
        File tmpFile = null;
        JavaFile jfile = null;

        try {
            File file = new File(name);
            tmpFile = File.createTempFile("TemporaryTargetFile", ".java", file.getParentFile());

            FileWriter writer = new FileWriter(tmpFile);
            writer.write(text);
            writer.flush();
            writer.close();

            SummaryJavaFile sf = Summary.getInstance().getJavaFile(name);
            jfile = parseEachFile(tmpFile.getAbsolutePath(), sf);
            jfile.setName(name);
            fireLogEvent(new LogEvent(this, "Parsing: " + name + "."));

        } catch (IOException e) {
            fireWarningEvent(new WarningEvent(this, "Failed to create temporary file."));

        } catch (ParseException e) {
            // System.err.println("Encounted Parse Error");

        } finally {
            if (tmpFile != null) {
                tmpFile.delete();
            }
        }

        if (jfile == null) {
            jfile = new JavaFile();
        }
        return jfile;
    }

    private synchronized JavaFile parseEachFile(String fname, SummaryJavaFile sf)
      throws ParseException{
        JavaFile jfile = new JavaFile();
        InputStream is = null;

        if (sf != null) {
            sfile = sf;
            sfile.setJavaFile(jfile);
        }
        jfile.setSummaryJavaFile(sfile);

        try {
            is = new FileInputStream(fname);
            if (parser == null) {
                parser = new JavaParser(is);
            } else {
                parser.ReInit(is);
            }

            jfile.setName(fname);
            parser.run(this, jfile);
            jfile.setLastModified();

        } catch (FileNotFoundException e) {
            fireWarningEvent(new WarningEvent(this, "Failed to open: " + fname + "."));

        } catch (IOException e) {

        } catch (ParseException e) {
            fireWarningEvent(new WarningEvent(this, "Parse error: " + e.getMessage()));
            removeParsedFile(jfile);
            throw e;

        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) { }
            }
        }
        return jfile;
    }

    public SummaryJavaFile getSummaryJavaFile(String name) {
        return Summary.getInstance().getJavaFile(name);
    }

    public SummaryJavaFile parseEachSummaryFile(String name, String text) {
        return Summary.getInstance().parseEachFile(name, text);
    }

    public SummaryJavaFile parseEachSummaryFile(String name) {
        return Summary.getInstance().parseEachFile(name);
    }

    public boolean existsInParsedSummaryFiles(String name) {
        return Summary.getInstance().exists(name);
    }

    public void collectInformation() {
        Summary.getInstance().collectInformation();
    }

    private JavaFile parseJDKFile(String fname) {
        return new JavaFile();
    }

    private boolean isJDKFile(String name) {
        if (name != null && name.startsWith(JDKZipFile.JDKPREFIX)) {
            return true;
        }
        return false;
    }

    public String getQualifiedName(String name) {
        return Summary.getInstance().getQualifiedName(sfile, name);
    }

    public String getQualifiedNameList(String nameList) {
        return Summary.getInstance().getQualifiedNameList(sfile, nameList);
    }

    public SummaryJavaClass getSummaryJavaClassInSelf(String name) {
        if (sfile != null) {
            return sfile.getJavaClass(name);
        }
        return null;
    }

    private SummaryJavaClass getSummaryJavaClass(String name) {
        SummaryJavaClass jclass = getSummaryJavaClassInSelf(name);
        if (jclass != null) {
            return jclass;
        }

        if (sfile != null) {
            String qname = getQualifiedName(name);

            SummaryJavaFile jf = null;
            if (isJDKFile(qname)) {
                jf = Summary.getInstance().getJavaFileInJDK(qname);
            } else if (qname != null) {
                jf = Summary.getInstance().getJavaFile(qname + ".java");
            }
            if (jf != null) {
                return jf.getJavaClass(qname);
            }
        }
        return null;
    }

    public SummaryJavaField getFieldType(JavaClass jclass, String name) {
        SummaryJavaClass sclass = jclass.getSummaryJavaClass();
        SummaryJavaField sf = FieldType.getFieldType(sclass, sclass, name);
        return FieldType.getFieldType(sclass, sclass, name);
    }

    public SummaryJavaField getFieldType(JavaClass jclass, String cname, String name) {
        SummaryJavaClass src = jclass.getSummaryJavaClass();
        SummaryJavaClass sclass = getSummaryJavaClass(cname);
        return FieldType.getFieldType(src, sclass, name);
    }

    public SummaryJavaField getFieldTypeAt(JavaClass jclass, String name) {
        SummaryJavaClass sclass = jclass.getSummaryJavaClass();
        return FieldType.getFieldType(sclass, name);
    }

    public SummaryJavaField getFieldTypeAt(JavaClass jclass, String cname, String name) {
        SummaryJavaClass src = jclass.getSummaryJavaClass();
        SummaryJavaClass sclass = getSummaryJavaClass(cname);
        return FieldType.getFieldTypeAt(src, sclass, name);
    }

    public String wideningConversions(JavaVariableList vlist) {
        return FieldType.wideningConversions(vlist);
    }

    public SummaryJavaMethod getMethodType(JavaClass jclass,
      String name, ArrayList params) {
        SummaryJavaClass sclass = jclass.getSummaryJavaClass();
        return MethodType.getMethodType(sclass, sclass, name, params);
    }

    public SummaryJavaMethod getMethodType(JavaClass jclass, String cname,
      String name, ArrayList params) {
        SummaryJavaClass src = jclass.getSummaryJavaClass();
        SummaryJavaClass sclass = getSummaryJavaClass(cname);
        return MethodType.getMethodType(src, sclass, name, params);
    }

    public SummaryJavaMethod getMethodTypeAt(JavaClass jclass, String cname,
      String name, ArrayList params) {
        SummaryJavaClass src = jclass.getSummaryJavaClass();
        SummaryJavaClass sclass = getSummaryJavaClass(cname);
        return MethodType.getMethodTypeAt(src, sclass, name, params);
    }

    public void printAllFiles() {
        JavaFilePool.getInstance().print();
    }
}
