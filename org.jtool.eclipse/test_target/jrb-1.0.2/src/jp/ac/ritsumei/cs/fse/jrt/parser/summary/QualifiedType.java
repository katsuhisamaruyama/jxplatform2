/*
 *     QualifiedType.java  Nov 14, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.parser.summary;
import jp.ac.ritsumei.cs.fse.jrt.parser.JDKZipFile;
import jp.ac.ritsumei.cs.fse.jrt.model.*;
import java.util.*;
import java.io.*;

public class QualifiedType {
    private SummaryJavaFile jfile;
    private String classPath;
    private String packageName;
    private List importFiles = new ArrayList();  // String

    public QualifiedType(SummaryJavaFile jfile) {
        this.jfile = jfile;
        String fileName = jfile.getName();
        classPath = System.getProperty("java.class.path");
        if (jfile.getPackageName().length() != 0) {
            packageName = jfile.getPackageName().replace('.', File.separatorChar);
        } else {
            packageName = "";
        }

        importFiles.clear();
        addFilesInSamePackage();
        addFilesContainingImportClasses();
    }

    private void addFilesInSamePackage() {
        List files = Summary.getInstance().getFilesInSamePackage(jfile);
        Iterator it = files.iterator();
        while (it.hasNext()) {
            SummaryJavaFile jf = (SummaryJavaFile)it.next();
            addClassesInFile(jf);
        }
    }

    private void addClassesInFile(SummaryJavaFile jf) {
        Iterator it = jf.getJavaClasses().iterator();
        while (it.hasNext()) {
            SummaryJavaClass jclass = (SummaryJavaClass)it.next();
            importFiles.add(jf.getName() + "#" + jclass.getName());
        }
    }

    private void addFilesContainingImportClasses() {
        jfile.rearrangeImports();

        Iterator it = jfile.getImports().iterator();
        while (it.hasNext()) {
            String name = (String)it.next();
            String importName = name.replace('.', File.separatorChar);
            addFilesWithClassPath(importName);
        }
    }

    private void addFilesWithClassPath(String name) {
        importFiles.add(File.separator + name);

        if (classPath != null) {
            StringTokenizer st = new StringTokenizer(classPath, File.pathSeparator);
            while (st.hasMoreTokens()) {
                String path = st.nextToken();
                String cpath = getAbsolutePath(path);
                importFiles.add(cpath + File.separator + name);
            }
        }
    }

    private String getAbsolutePath(String fname) {
        File file = new File(fname);
        try {
            return file.getCanonicalPath();
        } catch (IOException e) {
            return fname;
        }
    }

    public void printAllImports() {
        Iterator it = importFiles.iterator();
        while (it.hasNext()) {
            System.out.println("Import: " + (String)it.next());
        }
    }

    public String getQualifiedNameList(String nameList) {
        StringBuffer buf = new StringBuffer();
        StringTokenizer st = new StringTokenizer(nameList, ",", true);
        while (st.hasMoreTokens()) {
            String name = st.nextToken();
            if (name.equals(",")) {
                buf.append(name);
            } else {
                buf.append(getQualifiedName(name));
            }
        }
        return buf.toString();
    }

    public String getQualifiedName(String name) {
        if (name == null || name.length() == 0 || name.equals("null")
          || name.equals("void") || name.charAt(0) == '!' || name.charAt(0) == '/') {
            return name;
        }

        String array = "";
        int sep = name.indexOf("[");
        if (sep != -1) {
            array = name.substring(sep);
            name = name.substring(0, sep);
        }

        String fname = name.replace('.', File.separatorChar);
        String qname = foundInImportFiles(fname);

        if (qname != null) {
            return qname + array;
        }
        return null;
    }

    private String foundInJDKFile(String name) {
        if (JDKZipFile.in("src" + name + ".java")) {
            return JDKZipFile.JDKPREFIX + name;
        }            
        return null;
    }

    private String checkQualifiedName(String name) {
        String qname = foundInJDKFile(name);
        if (qname != null) {
            return qname;
        }

        String fname;
        if (name.indexOf("#") != -1) {
            fname = name.substring(0, name.indexOf("#"));
        } else {
            fname = name + ".java";
        }

        SummaryJavaFile jf = Summary.getInstance().getJavaFile(fname);
        if (jf != null) {
            return name;
        }
        return null;
    }

    private String foundInImportFiles(String name) {
        String qname = null;
        Iterator it = importFiles.iterator();
        while (it.hasNext()) {
            String iname = (String)it.next();

            if (iname.endsWith("#" + name)) {
                qname = checkQualifiedName(iname);

            } else {
                if (iname.endsWith(File.separator + "*")) {
                    String iprefix = iname.substring(0, iname.lastIndexOf(File.separator));
                    if (name.indexOf(File.separator) != -1) {
                        String nprefix = File.separator + name.substring(0, name.lastIndexOf(File.separator));
                        if (iprefix.compareTo(nprefix) == 0) {
                            qname = checkQualifiedName(File.separator + name);
                        }

                    } else {
                        qname = checkQualifiedName(iprefix + File.separator + name);
                    }
                }
            }
            if (qname != null) {
                return qname;
            }
        }
        return null;
    }
}
