/*
 *     SummaryJavaClass.java  Nov 14, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.parser.summary;
import jp.ac.ritsumei.cs.fse.jrt.model.JavaModifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.StringTokenizer;

public class SummaryJavaClass {
    private String name;
    private SummaryJavaFile jfile;
    private String superClassName = null;
    private String superClassNameList = null;
    private boolean isInterface = false;
    private ArrayList methods = new ArrayList();  // SummaryJavaMethod
    private ArrayList fields = new ArrayList();  // SummaryJavaField
    private List parents = new ArrayList();  // SummaryJavaClass
    private List ancestors = new ArrayList();  // SummaryJavaClass
    private List descendants = new ArrayList();  // SummaryJavaClass

    public SummaryJavaClass(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getLongName() {
        if (getPackageName().length() != 0) {
            return getPackageName() + "." + name;
        }
        return name;
    }

    public String toString() {
        return getName();
    }

    public void setJavaFile(SummaryJavaFile file) {
        jfile = file;
    }

    public SummaryJavaFile getJavaFile() {
        return jfile;
    }

    public String getPackageName() {
        return jfile.getPackageName();
    }

    public void setSuperClassName(String name) {
        superClassName = name;
    }

    public String getSuperClassName() {
        return superClassName;
    }

    public boolean hasSuperClass() {
        return superClassName != null;
    }

    public void setSuperClassNameList(String nameList) {
        superClassNameList = nameList;
    }

    public String getSuperClassNameList() {
        return superClassNameList;
    }

    public boolean hasSuperInterface() {
        return superClassNameList != null;
    }

    public void setInterface(boolean bool) {
        isInterface = bool;
    }

    public boolean isInterface() {
        return isInterface;
    }

    public boolean isInSamePackage(SummaryJavaClass jclass) {
        if (getPackageName().compareTo(jclass.getPackageName()) == 0) {
            return true;
        }
        return false;
    }

    public void addJavaMethod(SummaryJavaMethod jmethod) {
        methods.add(jmethod);
    }

    public SummaryJavaMethod getJavaMethod(String sig) {
        Iterator it = methods.iterator();
        while (it.hasNext()) {
            SummaryJavaMethod jmethod = (SummaryJavaMethod)it.next();
            if (jmethod.equalsSignature(sig)) {
                return jmethod;
            }
        }
        return null;
    }

    public ArrayList getJavaMethods() {
        return methods;
    }

    public ArrayList getJavaMethods(String name) {
        ArrayList matchedMethods = new ArrayList();  // SummaryJavaMethod
        Iterator it = methods.iterator();
        while (it.hasNext()) {
            SummaryJavaMethod jmethod = (SummaryJavaMethod)it.next();
            if (name.compareTo(jmethod.getName()) == 0) {
                matchedMethods.add(jmethod);
            }
        }
        return matchedMethods;
    }

    public void addJavaField(SummaryJavaField jfield) {
        fields.add(jfield);
    }

    public SummaryJavaField getJavaField(String name) {
        String fname = name;
        if (name.indexOf(".") == -1) {
            fname = getName() + "." + name;
        }

        Iterator it = fields.iterator();
        while (it.hasNext()) {
            SummaryJavaField jfield = (SummaryJavaField)it.next();
            if (name.compareTo(jfield.getName()) == 0) {
                return jfield;
            }
        }
        return null;
    }

    public void clearAncestors() {
        parents.clear();
        ancestors.clear();
        descendants.clear();
    }

    public List getParents() {
        return parents;
    }

    public List getAncestors() {
        return ancestors;
    }

    public boolean isAncestorOf(SummaryJavaClass sc) {
        if (descendants.contains(sc)) {
            return true;
        }
        return false;
    }

    public List getDescendants() {
        return descendants;
    }

    public boolean isDescendantOf(SummaryJavaClass sc) {
        if (ancestors.contains(sc)) {
            return true;
        }
        return false;
    }

    public void addAncestor(SummaryJavaClass jclass) {
        if (!ancestors.contains(jclass)) {
            ancestors.add(jclass);
        }
    }

    public void addDescendant(SummaryJavaClass jclass) {
        if (!descendants.contains(jclass)) {
            descendants.add(jclass);
        }
    }

    public void setParents() {
        StringBuffer superClassNames = new StringBuffer();
        if (hasSuperClass()) {
            superClassNames.append(getSuperClassName());
            superClassNames.append(",");
        }
        
        /*
        if (hasSuperInterface()) {
            superClassNames.append(getSuperClassNameList());
        }
        */

        StringTokenizer st = new StringTokenizer(superClassNames.toString(), ",");
        while (st.hasMoreTokens()) {
            String name = st.nextToken();
            String qname = jfile.getQualifiedName(name);
            
            if (qname != null) {
                SummaryJavaFile jf = Summary.getInstance().getJavaFile(qname + ".java");
                if (jf != null) {
                    SummaryJavaClass jclass = jf.getJavaClass(qname);
                    if (jclass != null) {
                        parents.add(jclass);
                    }
                }
            }
        }
    }

    public void setAncestors() {
        collectAncestors();
    }

    private List collectAncestors() {
        Iterator it = getParents().iterator();
        while (it.hasNext()) {
            SummaryJavaClass parent = (SummaryJavaClass)it.next();

            this.addAncestor(parent);
            parent.addDescendant(this);
            addAncestors(parent.collectAncestors());
        }
        return getAncestors();
    }

    private void addAncestors(List collection) {
        Iterator it = collection.iterator();
        while (it.hasNext()) {
            SummaryJavaClass jclass = (SummaryJavaClass)it.next();
            this.addAncestor(jclass);
            jclass.addDescendant(this);
        }            
    }

    public String getQualifiedName() {
        return jfile.getQualifiedName(getName());
    }

    public String getQualifiedName(String name) {
        return jfile.getQualifiedName(name);
    }

    public void print() {
        System.out.println("  CLASS = " + getName());
        System.out.println("  ANCESTORS   = " + getAncestors());
        System.out.println("  DESCENDANTS = " + getDescendants());
        printAllMethods();
        printAllFields();
    }

    public void printAllMethods() {
        Iterator it = methods.iterator();
        while (it.hasNext()) {
            SummaryJavaMethod jmethod = (SummaryJavaMethod)it.next();
            jmethod.print();
        }
    }

    public void printAllFields() {
        Iterator it = fields.iterator();
        while (it.hasNext()) {
            SummaryJavaField jfield = (SummaryJavaField)it.next();
            jfield.print();
        }
    }
}
