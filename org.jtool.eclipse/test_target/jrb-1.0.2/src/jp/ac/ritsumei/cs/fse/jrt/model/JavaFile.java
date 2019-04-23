/*
 *     JavaFile.java  Sep 6, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.model;
import jp.ac.ritsumei.cs.fse.jrt.parser.JavaModelFactory;
import jp.ac.ritsumei.cs.fse.jrt.parser.SimpleNode;
import jp.ac.ritsumei.cs.fse.jrt.parser.summary.Summary;
import jp.ac.ritsumei.cs.fse.jrt.parser.summary.SummaryJavaFile;
import jp.ac.ritsumei.cs.fse.jrt.parser.summary.SummaryJavaClass;
import jp.ac.ritsumei.cs.fse.jrt.parser.summary.SummaryJavaMethod;
import jp.ac.ritsumei.cs.fse.jrt.parser.summary.SummaryJavaField;
import jp.ac.ritsumei.cs.fse.jrt.parser.JavaParserVisitor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.io.File;
import java.io.IOException;

public class JavaFile extends JavaComponent {
    private String name = null;
    private String text = "";
    private String packageName = "";
    private ArrayList imports = new ArrayList();  // String
    private ArrayList classes = new ArrayList();  // JavaClass
    private long lastModifiedTime = -1;
    private SummaryJavaFile sfile;

    public JavaFile() {
        super();
    }

    public JavaFile(SimpleNode node) {
        super(node);
    }

    public JavaFile(String name) {
        super();
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

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public boolean isJavaFile() {
        return true;
    }

    public boolean isValid() {
        if (lastModifiedTime > 0) {
            return true;
        }
        return false;
    }

    public Object accept(JavaParserVisitor visitor) {
        return accept(visitor, this);
    }

    public void setPackageName(String name) {
        packageName = name;
    }

    public String getPackageName() {
        return packageName;
    }

    public void addImportFiles(String name) {
        imports.add(name);
    }

    public ArrayList getImports() {
        return imports;
    }

    public void addJavaClass(JavaClass jclass) {
        classes.add(jclass);
    }

    public JavaClass getJavaClass(String name) {
        Iterator it = classes.iterator();
        while (it.hasNext()) {
            JavaClass jclass = (JavaClass)it.next();
            if (name.compareTo(jclass.getName()) == 0) {
                return jclass;
            }
        }
        return null;
    }

    public JavaClass getJavaClass(int num) {
        return (JavaClass)classes.get(num);
    }

    public ArrayList getJavaClasses() {
        return classes;
    }

    public void setLastModified() {
        File file = new File(name);
        if (file != null) {
            lastModifiedTime = file.lastModified();
        }
    }

    public long getLastModified() {
        return lastModifiedTime;
    }

    public boolean isParsed() {
        if (lastModifiedTime != 0) {
            return true;
        }
        return false;
    }

    public boolean hasChanged() {
        if (isValid()) {
            File file = new File(name);
            if (file.lastModified() > getLastModified()) {
                return true;
            }
        }
        return false;
    }

    public void setSummaryJavaFile(SummaryJavaFile sfile) {
        this.sfile = sfile;
    }

    public SummaryJavaFile getSummaryJavaFile() {
        return sfile;
    }

    public String getQualifiedNameInPackage(String name) {
        String qname = sfile.getQualifiedName(name);
        SummaryJavaFile sf = Summary.getInstance().getJavaFile(qname + ".java");
        if (sf != null) {
            return sf.getName();
        }
        return null;
    }
}
