/*
 *     JavaClass.java  Nov 7, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.model;
import jp.ac.ritsumei.cs.fse.jrt.graphs.util.Graph;
import jp.ac.ritsumei.cs.fse.jrt.parser.SimpleNode;
import jp.ac.ritsumei.cs.fse.jrt.parser.summary.SummaryJavaClass;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.io.File;

public class JavaClass extends JavaComponent {
    private String name;
    private String qualifiedName = null;
    private boolean isInterface = false;
    private JavaModifier modifier = null;
    private String superClassName = null;
    private String superClassNameList = null;
    private ArrayList methods = new ArrayList();  // JavaMethod
    private ArrayList fields = new ArrayList();  // JavaStatement
    private ArrayList usedTypes = new ArrayList();  // String;
    private JavaFile jfile;
    private SummaryJavaClass sclass;
    private Graph ccfg = null;  // CCFG: Class Control Flow Graph
    private Graph cldg = null;  // ClDG: Class Dependence Graph

    public JavaClass() {
        super();
    }

    public JavaClass(SimpleNode node) {
        super(node);
    }

    public JavaClass(String name) {
        this.name = name;
    }

    public boolean isJavaClass() {
        return true;
    }

    public String toString() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getQualifiedName() {
        if (qualifiedName != null) {
            return qualifiedName;
        }
        if (sclass != null) {
            qualifiedName = sclass.getJavaFile().getQualifiedName(getName());
            return qualifiedName;
        }
        return name;
    }

    public void setInterface(boolean bool) {
        isInterface = bool;
    }

    public boolean isInterface() {
        return isInterface;
    }

    public void setModifier(JavaModifier modifier) {
        this.modifier = modifier;
    }

    public JavaModifier getModifier() {
        return modifier;
    }

    public void setSuperClassName(String name) {
        superClassName = name;
    }

    public String getSuperClassName() {
        return superClassName;
    }

    public String getShortSuperClassName() {
        if (superClassName == null) {
            return null;
        }
        if (superClassName.indexOf("#") != -1) {
            return superClassName.substring(superClassName.indexOf("#") + 1);
        }
        if (superClassName.indexOf(File.separator) != -1) {
            return superClassName.substring(superClassName.lastIndexOf(File.separator) + 1);
        }
        return superClassName;
    }

    public void setSuperClassNameList(String nameList) {
        superClassNameList = nameList;
    }

    public String getSuperClassNameList() {
        return superClassNameList;
    }

    public void addJavaMethod(JavaMethod jmethod) {
        methods.add(jmethod);
    }

    public ArrayList getJavaMethods() {
        return methods;
    }

    public JavaMethod getJavaMethod(String sig) {
        Iterator it = methods.iterator();
        while (it.hasNext()) {
            JavaMethod jmethod = (JavaMethod)it.next();
            if (jmethod.equalsSignature(sig)) {
                return jmethod;
            }
        }
        return null;
    }

    public void setJavaFile(JavaFile jfile) {
        this.jfile = jfile;
    }

    public JavaFile getJavaFile() {
        return jfile;
    }

    public void addJavaField(JavaStatement jst) {
        fields.add(jst);
    }

    public ArrayList getJavaFields() {
        return fields;
    }

    public JavaStatement getJavaField(String name) {
        String fname = name;
        if (name.indexOf(".") == -1) {
            fname = getName() + "." + name;
        }

        Iterator it = fields.iterator();
        while (it.hasNext()) {
            JavaStatement jst = (JavaStatement)it.next();
            JavaVariable jv = jst.getDeclaration();
            if (fname.compareTo(jv.getName()) == 0) {
                return jst;
            }
        }
        return null;
    }

    public JavaStatement getJavaField(JavaVariable jvar) {
        Iterator it = fields.iterator();
        while (it.hasNext()) {
            JavaStatement jst = (JavaStatement)it.next();
            JavaVariable jv = jst.getDeclaration();
            if (jv.equals(jvar)) {
                return jst;
            }
        }
        return null;
    }

    public void addUsedType(String type) {
        if (type == null) {
            return;
        }

        StringTokenizer st = new StringTokenizer(type, ",");
        while (st.hasMoreTokens()) {
            usedTypes.add(st.nextToken());
        }
    }

    public ArrayList getUsedTypes() {
        return usedTypes;
    }

    public void setSummaryJavaClass(SummaryJavaClass sclass) {
        this.sclass = sclass;
    }

    public SummaryJavaClass getSummaryJavaClass() {
        return sclass;
    }

    public boolean isChildOf(JavaClass jc) {
        if (getSuperClassName() == null) {
            return false;
        }

        if (getSuperClassName().compareTo(jc.getQualifiedName()) == 0) {
            return true;
        }
        return false;
    }

    public void setCCFG(Graph g) {
        ccfg = g;
    }

    public Graph getCCFG() {
        return ccfg;
    }

    public void setClDG(Graph g) {
        cldg = g;
    }

    public Graph getClDG() {
        return cldg;
    }

    public boolean isStatic() {
        return modifier.has("static");
    }

    public boolean isAbstract() {
        if (isInterface) {
            return true;
        }
        return modifier.has("abstract");
    }

    public boolean isFinal() {
        return modifier.has("final");
    }

    public boolean isPublic() {
        return modifier.has("public");
    }

    public boolean isProtected() {
        return modifier.has("protected");
    }

    public boolean isPrivate() {
        return modifier.has("private");
    }

    public boolean isDefault() {
        return (!isPublic() && !isProtected() && !isPrivate());
    }

    public boolean isStrictfp() {
        return modifier.has("strictfp");
    }

    public void printAllMethods() {
        Iterator it = methods.iterator();
        while (it.hasNext()) {
            JavaMethod jmethod = (JavaMethod)it.next();
            System.out.println("METHOD = " + jmethod.getSignature());
        }
    }

    public void printAllFields() {
        Iterator it = fields.iterator();
        while (it.hasNext()) {
            JavaVariable jvar = (JavaVariable)it.next();
            System.out.println("VARIABLE = " + jvar.getQualifiedType() + "#" + jvar.getNameWithID());
        }
    }
}
