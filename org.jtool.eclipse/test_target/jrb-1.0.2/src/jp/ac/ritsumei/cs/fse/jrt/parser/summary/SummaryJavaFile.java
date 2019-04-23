/*
 *     SummaryJavaFile.java  Nov 20, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.parser.summary;
import jp.ac.ritsumei.cs.fse.jrt.model.JavaFile;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.File;

public class SummaryJavaFile {
    private String name;
    private String packageName = "";
    private ArrayList imports = new ArrayList();  // String
    private ArrayList classes = new ArrayList();  // SummaryJavaClass
    private QualifiedType qtype = null;
    private JavaFile jfile;

    public SummaryJavaFile() {
        name = null;
    }
     
    public SummaryJavaFile(String name) {
        this.name = name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return name;
    }

    public String getShortFileName() {
        String fileName = name.substring(0, name.lastIndexOf('.'));
        fileName = fileName.substring(fileName.lastIndexOf(File.separator) + 1);
        return fileName;
    }

    public String getDirName() {
        return name.substring(0, name.lastIndexOf(File.separator));
    }

    public void setPackageName(String name) {
        packageName = name;
    }

    public String getPackageName() {
        return packageName;
    }

    public void addImportFile(String name) {
        imports.add(name);
    }

    public ArrayList getImports() {
        return imports;
    }

    public void rearrangeImports() {
        ArrayList collection = new ArrayList(imports);
        Iterator it = collection.iterator();
        while (it.hasNext()) {
            String name = (String)it.next();
            if (name.indexOf("*") != -1) {
                imports.remove(name);
                imports.add(name);
            }
        }
    }

    public void addJavaClass(SummaryJavaClass jclass) {
        classes.add(jclass);
    }

    public ArrayList getJavaClasses() {
        return classes;
    }

    public SummaryJavaClass getJavaClass(String name) {
        if (name == null) {
            return null;
        }

        if (name.indexOf("#") != -1) {
            name = name.substring(name.indexOf("#") + 1);
        }

        if (name.indexOf(File.separator) != -1) {
            name = name.substring(name.lastIndexOf(File.separator) + 1);
        }

        Iterator it = classes.iterator();
        while (it.hasNext()) {
            SummaryJavaClass jclass = (SummaryJavaClass)it.next();
            if (name.compareTo(jclass.getName()) == 0) {
                return jclass;
            }
        }
        return null;
    }

    public String getQualifiedName(String name) {
        if (qtype == null) {
            qtype = new QualifiedType(this);
        }
        return qtype.getQualifiedName(name);
    }

    public String getQualifiedNameList(String nameList) {
        if (qtype != null) {
            return qtype.getQualifiedNameList(nameList);
        }
        return null;
    }

    public void setParents() {
        Iterator it = classes.iterator();
        while (it.hasNext()) {
            SummaryJavaClass jclass = (SummaryJavaClass)it.next();
            jclass.setParents();
        }
    }

    public void setAncestors() {
        Iterator it = classes.iterator();
        while (it.hasNext()) {
            SummaryJavaClass jclass = (SummaryJavaClass)it.next();
            jclass.setAncestors();
        }
    }

    public void clearAncestors() {
        Iterator it = classes.iterator();
        while (it.hasNext()) {
            SummaryJavaClass jclass = (SummaryJavaClass)it.next();
            jclass.clearAncestors();
        }
    }

    public void setJavaFile(JavaFile jfile) {
        this.jfile = jfile;
    }

    public JavaFile getJavaFile() {
        return jfile;
    }
    
    public void print() {
        System.out.println("FILE = " + name);
        Iterator it = classes.iterator();
        while (it.hasNext()) {
            SummaryJavaClass jclass = (SummaryJavaClass)it.next();
            jclass.print();
        }
    }

    public void printAllImports() {
        qtype.printAllImports();
    }
}
