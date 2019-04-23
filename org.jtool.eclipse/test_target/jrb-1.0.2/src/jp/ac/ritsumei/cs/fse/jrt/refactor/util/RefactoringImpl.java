/*
 *     RefactoringImpl.java  Dec 29, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.refactor.util;
import jp.ac.ritsumei.cs.fse.jrt.gui.Refactor;
import jp.ac.ritsumei.cs.fse.jrt.gui.ParsingMonitor;
import jp.ac.ritsumei.cs.fse.jrt.parser.JavaModelFactory;
import jp.ac.ritsumei.cs.fse.jrt.parser.summary.*;
import jp.ac.ritsumei.cs.fse.jrt.graphs.GraphFactory;
import jp.ac.ritsumei.cs.fse.jrt.graphs.cfg.*;
import jp.ac.ritsumei.cs.fse.jrt.graphs.util.*;
import jp.ac.ritsumei.cs.fse.jrt.model.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.*;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class RefactoringImpl {
    private Refactor refactor;
    private JFrame frame;
    private boolean isCanceled = false;

    public void setRefactor(Refactor refactor) {
        this.refactor = refactor;
    }

    public void setFrame(JFrame frame) {
        this.frame = frame;
    }


    /****  Files ****/

    public String getText(String name) {
        if (refactor != null) {
            return refactor.getDestinationText(name);
        }
        return readFile(name);
    }

    public JavaFile getJavaFile(String name) {
        JavaFile jf = getParsedFile(name);
        if (jf != null) {
            if (jf.getText() == null) {
                jf.setText(getText(name));
            }
            return jf;
        }
        return getJavaFile(name, getText(name));
    }

    public JavaFile getJavaFile(String name, String text) {
        JavaFile jfile = JavaModelFactory.getInstance().parseEachFile(name, text);
        jfile.setText(text);
        addParsedFile(jfile);
        return jfile;
    }

    public JavaFile getJavaFile(String name, JavaFile jf) {
        if (jf.getJavaClass(name) != null) {
            return jf;
        }

        String fname = getFileName(name, jf);
        if (fname != null) {
            return getJavaFile(fname);
        }
        return new JavaFile();
    }

    public JavaFile getJavaFile(SummaryJavaFile sfile) {
        return getJavaFile(sfile.getName());
    }

    private JavaFile getJavaFile(SummaryJavaClass sc) {
        return getJavaFile(sc.getJavaFile());
    }

    private JavaFile getJavaFile(SummaryJavaMethod sm) {
        return getJavaFile(sm.getJavaClass());
    }

    private JavaFile getJavaFile(SummaryJavaField sf) {
        return getJavaFile(sf.getJavaClass());
    }

    public JavaFile getClone(JavaFile jf) {
        removeParsedFile(jf);
        JavaFile jfile = JavaModelFactory.getInstance().getCloneOfJavaFile(jf);
        addParsedFile(jfile);
        return jfile;
    }

    public JavaFile getParsedFile(String name) {
        return JavaModelFactory.getInstance().getParsedFile(name);
    }
    
    public void addParsedFile(JavaFile jf) {
        JavaModelFactory.getInstance().addParsedFile(jf);
    }

    public void removeParsedFile(JavaFile jf) {
        JavaModelFactory.getInstance().removeParsedFile(jf);
    }

    public void removeParsedFile(String name) {
        removeParsedFile(getParsedFile(name));
    }

    private String getFileName(String name, JavaFile jf) {
        if (name.indexOf("#") == -1) {
            if (name.indexOf(File.separator) == -1) {
                return findSameNameFileInPackage(name, jf);
            }
        } else {
            name = name.substring(0, name.indexOf("#"));
        }
        return getFileName(name);
    }

    private String getFileName(String name) {
        if (!name.endsWith(".java")) {
            name = name + ".java";
        }            
        File file = new File(name);
        if (file.exists()) {
            return name;
        }
        return null;
    }

    public String findSameNameFileInPackage(String name, JavaFile jf) {
        return jf.getQualifiedNameInPackage(name);
    }        

    public boolean existsSameNameFileInPackage(String name, JavaFile jf) {
        if (findSameNameFileInPackage(name, jf) != null) {
            return true;
        }
        return false;
    }

    public boolean isUsedInFile(String type, JavaFile jf) {
        Iterator it = jf.getJavaClasses().iterator();
        while (it.hasNext()) {
            JavaClass jc = (JavaClass)it.next();
            if (isUsedInClass(type, jc)) {
                return true;
            }
        }
        return false;
    }

    public boolean isUsedInClass(String type, JavaClass jc) {
        Iterator it = jc.getUsedTypes().iterator();
        while (it.hasNext()) {
            String name = (String)it.next();
            if (type.indexOf(File.separator) == -1) {
                name = name.substring(name.lastIndexOf(File.separator) + 1);
                if (name.indexOf("#") != -1) {
                    name = name.substring(name.indexOf("#") + 1);
                }
            }
            if (type.compareTo(name) == 0) {
                return true;
            }
        }
        return false;
    }

    public List collectFilesUsingClass(JavaClass jc) {
        List candidates = new ArrayList();
        Iterator it = parseFiles().iterator();
        while (it.hasNext()) {
            JavaFile jfile = (JavaFile)it.next();
            if (isUsedInFile(jc.getQualifiedName(), jfile)) {
                candidates.add(jfile);
            }
        }
        return candidates;
    }
    
    public List collectFilesCallingMethod(JavaMethod jm) {
        SummaryJavaClass sclass = getJavaClass(jm.getJavaClass());

        List candidates = new ArrayList();
        Iterator it = parseFiles().iterator();
        while (it.hasNext()) {
            JavaFile jfile = (JavaFile)it.next();

            Iterator itc = jfile.getJavaClasses().iterator();
            while (itc.hasNext()) {
                JavaClass jc = (JavaClass)itc.next();
                if (jc != null && isCalledInClass(jm, jc)) {
                    if (!candidates.contains(jc.getJavaFile())) {
                        candidates.add(jc.getJavaFile());
                    }
                }
            }
        }
        return candidates;
    }

    public List collectFilesContainingSubclassesCallingMethod(JavaMethod jm) {
        List classes = collectSubclassesCallingMethod(jm, jm.getJavaClass());

        List candidates = new ArrayList();
        Iterator it = classes.iterator();
        while (it.hasNext()) {
            JavaClass jc = (JavaClass)it.next();
            if (!candidates.contains(jc.getJavaFile())) {
                candidates.add(jc.getJavaFile());
            }
        }
        return candidates;
    }

    public List collectFilesUsingField(JavaVariable jv) {
        SummaryJavaClass sclass = getJavaClass(jv.getJavaClass());

        List candidates = new ArrayList();
        Iterator it = parseFiles().iterator();
        while (it.hasNext()) {
            JavaFile jfile = (JavaFile)it.next();

            Iterator itc = jfile.getJavaClasses().iterator();
            while (itc.hasNext()) {
                JavaClass jc = (JavaClass)itc.next();

                if (jc != null && isDirectlyUsedInClass(jv, jc)) {
                    if (!candidates.contains(jc.getJavaFile())) {
                        candidates.add(jc.getJavaFile());
                    }
                }
            }
        }
        return candidates;
    }
    
    public List collectFilesContainingSubclassesUsingField(JavaVariable jv) {
        List classes = collectSubclassesUsingField(jv, jv.getJavaClass());

        List candidates = new ArrayList();
        Iterator it = classes.iterator();
        while (it.hasNext()) {
            JavaClass jc = (JavaClass)it.next();
            if (!candidates.contains(jc.getJavaFile())) {
                candidates.add(jc.getJavaFile());
            }
        }
        return candidates;
    }


    /****  Classes ****/

    public String getClassName(String name) {
        if (name.indexOf(File.separator) != -1) {
            name = name.substring(name.lastIndexOf(File.separator) + 1);
        }
        if (name.indexOf("#") != -1) {
            return name.substring(name.indexOf("#") + 1);
        }
        return name;
    }

    public JavaClass getJavaClass(SummaryJavaClass sc) {
        return getJavaFile(sc).getJavaClass(sc.getName());
    }

    public SummaryJavaClass getJavaClass(JavaClass jc) {
        return jc.getSummaryJavaClass();
    }

    public void makeCFG(JavaClass jc) {
        GraphFactory.getInstance().makeCFG(jc);
    }

    public void makePDG(JavaFile jc) {
        GraphFactory.getInstance().makePDG(jc);
    }

    public boolean existsSameNameClassInPackage(String name, JavaFile jf) {
        return existsSameNameFileInPackage(name, jf);
    }

    public boolean existsSameNameClassInFile(String name, JavaFile jf) {
        if (jf.getJavaClass(name) != null) {
            return true;
        }
        return false;
    }

    public boolean existsSameClassInFile(JavaClass jc, JavaFile jf) {
        return existsSameNameClassInFile(jc.getName(), jf);
    }

    public List collectSubclasses(JavaClass jclass) {
        List classes = new ArrayList();
        SummaryJavaClass sclass = getJavaClass(jclass);

        Iterator it = sclass.getDescendants().iterator();
        while (it.hasNext()) {
            SummaryJavaClass sc = (SummaryJavaClass)it.next();
            JavaClass jc = getJavaClass(sc);
            if (jc != null && !classes.contains(jc)) {
                classes.add(jc);
            }
        }
        return classes;
    }

    public List collectChildren(JavaClass jclass) {
        List classes = new ArrayList();
        SummaryJavaClass sclass = getJavaClass(jclass);
        
        Iterator it = sclass.getDescendants().iterator();
        while (it.hasNext()) {
            SummaryJavaClass sc = (SummaryJavaClass)it.next();
            JavaClass jc = getJavaClass(sc);
            if (jc != null && jc.isChildOf(jclass)) {
                if (!classes.contains(jc)) {
                    classes.add(jc);
                }
            }
        }
        return classes;
    }


    /****  Methods ****/

    public JavaMethod getJavaMethod(SummaryJavaMethod sm) {
        JavaClass jc = getJavaClass(sm.getJavaClass());
        return jc.getJavaMethod(sm.getSignature());
    }

    public JavaMethod getGetter(String name, JavaVariable jv) {
        if (name != null) {
            JavaMethod jm = new JavaMethod();
            jm.setName(name);
            return jm;
        }
        return null;
    }

    public JavaMethod getSetter(String name, JavaVariable jv) {
        if (name != null) {
            JavaStatement jst = new JavaStatement();
            jst.addDefVariable(jv);
            JavaMethod jm = new JavaMethod();
            jm.setName(name);
            jm.addParameter(jst);
            return jm;
        }
        return null;
    }

    public void makeCFG(JavaMethod jm) {
        GraphFactory.getInstance().makeCFG(jm.getJavaClass());
    }

    public void makePDG(JavaMethod jm) {
        GraphFactory.getInstance().makePDG(jm.getJavaClass());
    }

    public boolean existsSameNameMethodInClass(String name, JavaClass jc) {
        Iterator it = jc.getJavaMethods().iterator();
        while (it.hasNext()) {
            JavaMethod jm = (JavaMethod)it.next();
            if (name.compareTo(jm.getName()) == 0) {
                return true;
            }
        }
        return false;
    }

    public boolean existsSameParameterMethodInClass(JavaMethod jm, JavaClass jc) {
        Iterator it = jc.getJavaMethods().iterator();
        while (it.hasNext()) {
            JavaMethod m = (JavaMethod)it.next();
            if (jm.equalsParameterTypes(m)) {
                return true;
            }
        }
        return false;
    }

    public boolean existsSameMethodInClass(JavaMethod jm, JavaClass jc) {
        JavaMethod m = jc.getJavaMethod(jm.getSignature());
        if (m != null) {
            return true;
        }
        return false;
    }

    public boolean existsSameMethodInClass(JavaMethod jm, SummaryJavaClass sc) {
        SummaryJavaMethod m = sc.getJavaMethod(jm.getSignature());
        if (m != null) {
            return true;
        }
        return false;
    }

    public boolean existsSameMethodBetweenClasses(JavaClass src, JavaClass dst) {
        Iterator it = src.getJavaMethods().iterator();
        while (it.hasNext()) {
            JavaMethod jm = (JavaMethod)it.next();
            if (existsSameMethodInClass(jm, dst)) {
                return true;
            }
        }
        return false;
    }

    public List collectSameMethodInSuperclasses(JavaMethod jm) {
        return collectSameMethodInClasses(jm, getJavaClass(jm.getJavaClass()).getAncestors());
    }

    public List collectSameMethodInSubclasses(JavaMethod jm) {
        return collectSameMethodInClasses(jm, getJavaClass(jm.getJavaClass()).getDescendants());
    }

    public List collectSameMethodInClasses(JavaMethod jm, List list) {
        List clist = new ArrayList();
        Iterator it = list.iterator();
        while (it.hasNext()) {
            SummaryJavaClass sc = (SummaryJavaClass)it.next();
            if (existsSameMethodInClass(jm, sc)) {
                clist.add(sc);
            }
        }
        return clist;
    }

    private boolean callsMethodsInClass(JavaMethod jm, JavaClass jc) {
        return !collectCalledMethodInClass(jm, jc).isEmpty();
    }

    public List collectCalledMethodsInClassOrAncestors(JavaMethod jm) {
        List mlist = new ArrayList();
        mlist.addAll(collectCalledMethodInClass(jm, jm.getJavaClass()));

        SummaryJavaClass sc = getJavaClass(jm.getJavaClass());
        Iterator it = sc.getAncestors().iterator();
        while (it.hasNext()) {
            SummaryJavaClass c = (SummaryJavaClass)it.next();
            mlist.addAll(collectCalledMethodInClass(jm, c));
        }
        return mlist;
    }

    public List collectCalledMethodInClass(JavaMethod jm, JavaClass jc) {
        return collectCalledMethodInClass(jm, getJavaClass(jc));
    }

    public List collectCalledMethodInClass(JavaMethod jm, SummaryJavaClass sc) {
        List mlist = new ArrayList();
        Iterator it = jm.getCalledMethods().iterator();
        while (it.hasNext()) {
            SummaryJavaMethod sm = (SummaryJavaMethod)it.next();
            if (sc.equals(sm.getJavaClass()) && !jm.equalsSignature(sm.getSignature())) {
                mlist.add(getJavaMethod(sm));
            }
        }
        return mlist;
    }

    public boolean callsMethodsInAncestors(JavaMethod jm) {
        SummaryJavaClass sc = getJavaClass(jm.getJavaClass());
        Iterator it = jm.getCalledMethods().iterator();
        while (it.hasNext()) {
            SummaryJavaMethod sm = (SummaryJavaMethod)it.next();
            if (sc.getAncestors().contains(sm.getJavaClass())) {
                return true;
            }
        }
        return false;
    }

    public boolean callsMethodsInClassOrAncestors(JavaMethod jm) {
        if (callsMethodsInClass(jm, jm.getJavaClass()) || callsMethodsInAncestors(jm)) {
            return true;
        }
        return false;
    }

    public boolean callsPrivateMethods(JavaMethod jm) {
        Iterator it = collectCalledMethodInClass(jm, jm.getJavaClass()).iterator();
        while (it.hasNext()) {
            JavaMethod m = (JavaMethod)it.next();
            if (m.isPrivate()) {
                return true;
            }
        }
        return false;
    }

    public boolean isCalledInClass(JavaMethod jm, JavaClass jc) {
        Iterator it = collectCalledMethodsInClass(jc).iterator();
        while (it.hasNext()) {
            JavaMethod m = (JavaMethod)it.next();
            if (jm.equalsSignature(m.getSignature())) {
                return true;
            }
        }
        return false;
    }

    public List collectCalledMethodsInClass(JavaClass jc) {
        List mlist = new ArrayList();
        Iterator it = jc.getJavaMethods().iterator();
        while (it.hasNext()) {
            JavaMethod jm = (JavaMethod)it.next(); 
            mlist.addAll(collectCalledMethodsInMethod(jm));
        }
        return mlist;
    }

    public List collectCalledMethodsInMethod(JavaMethod jm) {
        List mlist = new ArrayList();
        Iterator it = jm.getCalledMethods().iterator();
        while (it.hasNext()) {
            SummaryJavaMethod sm = (SummaryJavaMethod)it.next();
            JavaClass jc = getJavaClass(sm.getJavaClass());
            if (jc != null) {
                JavaMethod m = jc.getJavaMethod(sm.getSignature());
                if (m != null && !jm.equalsSignature(m.getSignature())) {
                    mlist.add(m);
                }
            }
        }
        return mlist;
    }

    public List collectSubclassesCallingMethod(JavaMethod jm, JavaClass jclass) {
        List classes = new ArrayList();
        SummaryJavaClass sclass = getJavaClass(jclass);

        Iterator it = sclass.getDescendants().iterator();
        while (it.hasNext()) {
            SummaryJavaClass sc = (SummaryJavaClass)it.next();
            JavaClass jc = getJavaClass(sc);
            if (jc != null && isCalledInClass(jm, jc)) {
                if (!classes.contains(jc)) {
                    classes.add(jc);
                }
            }
        }
        return classes;
    }

    public List collectChildrenCallingMethod(JavaMethod jm) {
        List classes = new ArrayList();
        SummaryJavaClass sclass = getJavaClass(jm.getJavaClass());
        
        Iterator it = sclass.getDescendants().iterator();
        while (it.hasNext()) {
            SummaryJavaClass sc = (SummaryJavaClass)it.next();
            JavaClass jc = getJavaClass(sc);
            if (jc != null && jc.isChildOf(jm.getJavaClass())) {
                if (isCalledInClass(jm, jc)
                  || !collectSubclassesCallingMethod(jm, jc).isEmpty()) {
                    if (!classes.contains(jc)) {
                        classes.add(jc);
                    }
                }
            }
        }
        return classes;
    }

    public JavaVariableList collectVariablesReferringToClass(JavaMethod jm, JavaClass jc) {
        JavaVariableList vlist = new JavaVariableList();
        Iterator it = jm.getJavaVariables().iterator();
        while (it.hasNext()) {
            JavaVariable jv = (JavaVariable)it.next();
            if (refersToClass(jv, jc)) {
                if (!vlist.contains(jv)) {
                    vlist.add(jv);
                }
            }
        }
        return vlist;
    }

    public boolean refersToPrivateField(JavaMethod jm) {
        if (!collectVariablesReferringToPrivateFields(jm).isEmpty()) {
            return true;
        }
        return false;
    }

    public JavaVariableList collectVariablesReferringToPrivateFields(JavaMethod jm) {
        JavaVariableList vlist = new JavaVariableList();
        Iterator it = jm.getJavaVariables().iterator();
        while (it.hasNext()) {
            JavaVariable jv = (JavaVariable)it.next();
            if (refersToPrivateField(jv)) {
                if (!vlist.contains(jv)) {
                    vlist.add(jv);
                }
            }
        }
        return vlist;
    }

    public boolean usesFieldsInClass(JavaMethod jm, JavaClass jc) {
        Iterator it = jm.getJavaVariables().iterator();
        while (it.hasNext()) {
            JavaVariable jv = (JavaVariable)it.next();
            if (existsItsDeclarationInClass(jv, jc)) {
                return true;
            }
        }
        return false;
    }


    /****  Fields ****/

    private JavaStatement getJavaFieldStatement(JavaVariable jv) {
        Iterator it = jv.getJavaClass().getJavaFields().iterator();
        while (it.hasNext()) {
            JavaStatement jst = (JavaStatement)it.next();
            if (jv.equals(jst.getDeclaration())) {
                return jst;
            }
        }
        return null;
    }

    public void makeCFG(JavaVariable jv) {
        GraphFactory.getInstance().makeCFG(jv.getJavaClass());
    }

    public void makePDG(JavaVariable jv) {
        GraphFactory.getInstance().makePDG(jv.getJavaClass());
    }

    public boolean existsSameNameFieldInClass(String name, JavaClass jc) {
        if (name.indexOf(".") != -1) {
            name = name.substring(name.lastIndexOf(".") + 1);
        }
        JavaStatement jst = jc.getJavaField(name);
        if (jst != null) {
            return true;
        }
        return false;
    }

    public boolean existsSameFieldInClass(JavaVariable jv, JavaClass jc) {
        return existsSameNameFieldInClass(jv.getName(), jc);
    }

    public boolean existsSameFieldInClass(JavaVariable jv, SummaryJavaClass sc) {
        SummaryJavaField sv = sc.getJavaField(jv.getName());
        if (sv != null) {
            return true;
        }
        return false;
    }

    public boolean existsSameFieldBetweenClasses(JavaClass src, JavaClass dst) {
        Iterator it = src.getJavaFields().iterator();
        while (it.hasNext()) {
            JavaStatement jst = (JavaStatement)it.next();
            JavaVariable jv = jst.getDeclaration();
            if (existsSameFieldInClass(jv, dst)) {
                return true;
            }
        }
        return false;
    }

    public boolean existsSameFieldInSuperclasses(JavaVariable jv) {
        return existsSameFieldInClasses(jv, getJavaClass(jv.getJavaClass()).getAncestors());
    }

    public boolean existsSameFieldInSubclasses(JavaVariable jv) {
        return existsSameFieldInClasses(jv, getJavaClass(jv.getJavaClass()).getDescendants());
    }

    public boolean existsSameFieldInClasses(JavaVariable jv, List list) {
        Iterator it = list.iterator();
        while (it.hasNext()) {
            SummaryJavaClass sc = (SummaryJavaClass)it.next();
            if (existsSameFieldInClass(jv, getJavaClass(sc))) {
                return true;
            }
        }
        return false;
    }

    public boolean usesOtherFieldsInClassAtDeclaration(JavaVariable jv) {
        JavaStatement jst = getJavaFieldStatement(jv);
        Iterator it = jst.getUseVariables().iterator();
        while (it.hasNext()) {
            JavaVariable v = (JavaVariable)it.next();
            if (existsItsDeclarationInClass(v)) {
                return true;
            }
        }
        return false;
    }

    public boolean existsItsDeclarationInClass(JavaVariable jv) {
        return existsItsDeclarationInClass(jv, jv.getJavaClass());
    }

    private boolean existsItsDeclarationInClass(JavaVariable jv, JavaClass jc) {
        SummaryJavaClass sc = getJavaClass(jc);
        SummaryJavaField sfield = jv.getDeclField();
        if (sfield != null && sc.equals(sfield.getJavaClass())) {
            return true;
        }
        return false;
    }

    public boolean usesOtherFieldsInAncestorsAtDeclaration(JavaVariable jv) {
        JavaStatement jst = getJavaFieldStatement(jv);
        Iterator it = jst.getUseVariables().iterator();
        while (it.hasNext()) {
            JavaVariable v = (JavaVariable)it.next();
            if (existsItsDeclarationInAncestors(v)) {
                return true;
            }
        }
        return false;
    }

    public boolean existsItsDeclarationInAncestors(JavaVariable jv) {
        SummaryJavaField sfield = jv.getDeclField();
        SummaryJavaClass sc = getJavaClass(jv.getJavaClass());
        if (sfield != null && sc.getAncestors().contains(sfield.getJavaClass())) {
            return true;
        }
        return false;
    }

    public JavaVariableList collectUsedFieldsInClassOrAncestors(JavaMethod jm) {
        JavaVariableList vlist = new JavaVariableList();
        Iterator it = jm.getJavaVariables().iterator();
        while (it.hasNext()) {
            JavaVariable jv = (JavaVariable)it.next();
            if (existsItsDeclarationInClass(jv) || existsItsDeclarationInAncestors(jv)) {
                vlist.add(jv);
            }
        }
        return vlist;
    }

    public JavaVariableList collectUsedFieldsInClassOrAncestors(JavaVariable jvar) {
        JavaVariableList vlist = new JavaVariableList();
        JavaStatement jst = getJavaFieldStatement(jvar);
        Iterator it = jst.getUseVariables().iterator();
        while (it.hasNext()) {
            JavaVariable jv = (JavaVariable)it.next();
            if (existsItsDeclarationInClass(jv) || existsItsDeclarationInAncestors(jv)) {
                vlist.add(jv);
            }
        }
        return vlist;
    }

    public boolean isDirectlyUsedInClass(JavaVariable jv, JavaClass jc) {
        return isDirectlyUsedInClass(jv, jc, null, null);
    }

    public boolean isDirectlyUsedInClass(JavaVariable jv, JavaClass jc, JavaMethod gm, JavaMethod sm) {
        SummaryJavaClass sc = getJavaClass(jv.getJavaClass());
        String name = jv.getName();
        SummaryJavaField sfield = sc.getJavaField(jv.getName());
        
        Iterator it = jc.getJavaFields().iterator();
        while (it.hasNext()) {
            JavaStatement jst = (JavaStatement)it.next();
            if (isDirectlyUsedInField(sfield, jst)) {
                return true;
            }
        }

        it = jc.getJavaMethods().iterator();
        while (it.hasNext()) {
            JavaMethod jm = (JavaMethod)it.next();
            if (gm != null && sm != null) {
                if (!jm.equalsSignature(gm) && !jm.equalsSignature(sm)) {
                    if (isDirectlyUsedInMethod(sfield, jm)) {
                        return true;
                    }
                }
            } else {
                if (isDirectlyUsedInMethod(sfield, jm)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isDirectlyUsedInField(SummaryJavaField sfield, JavaStatement jst) {
        Iterator it = jst.getUseVariables().iterator();
        while (it.hasNext()) {
            JavaVariable v = (JavaVariable)it.next();
            SummaryJavaField sf = v.getDeclField();
            if (sf != null && sf.equals(sfield)) {
                return true;
            }
        }
        return false;
    }

    private boolean isDirectlyUsedInMethod(SummaryJavaField sfield, JavaMethod jm) {
        Iterator it = jm.getJavaVariables().iterator();
        while (it.hasNext()) {
            JavaVariable v = (JavaVariable)it.next();

            SummaryJavaField sf = v.getDeclField();
            if (sf != null && sf.equals(sfield)) {
                return true;
            }
        }
        return false;
    }

    public List collectSubclassesUsingField(JavaVariable jv, JavaClass jclass) {
        List classes = new ArrayList();
        SummaryJavaClass sclass = getJavaClass(jclass);

        Iterator it = sclass.getDescendants().iterator();
        while (it.hasNext()) {
            SummaryJavaClass sc = (SummaryJavaClass)it.next();
            JavaClass jc = getJavaClass(sc);
            if (jc != null && isDirectlyUsedInClass(jv, jc)) {
                if (!classes.contains(jc)) {
                    classes.add(jc);
                }
            }
        }
        return classes;
    }

    public List collectChildrenUsingField(JavaVariable jv) {
        List classes = new ArrayList();
        SummaryJavaClass sclass = getJavaClass(jv.getJavaClass());
        
        Iterator it = sclass.getDescendants().iterator();
        while (it.hasNext()) {
            SummaryJavaClass sc = (SummaryJavaClass)it.next();
            JavaClass jc = getJavaClass(sc);
            if (jc != null && jc.isChildOf(jv.getJavaClass())) {
                if (isDirectlyUsedInClass(jv, jc)
                  || !collectSubclassesUsingField(jv, jc).isEmpty()) {
                    if (!classes.contains(jc)) {
                        classes.add(jc);
                    }
                }
            }
        }
        return classes;
    }

    public boolean isUsedInClass(JavaVariable jv, JavaClass jc) {
        Iterator it = jc.getJavaMethods().iterator();        
        while (it.hasNext()) {
            JavaMethod jm = (JavaMethod)it.next();
            if (isUsedInClass(jv, jm)) {
                return true;
            }
        }
        return false;
    }

    public boolean isUsedInClass(JavaVariable jv, JavaMethod jm) {
        SummaryJavaClass sclass = jv.getJavaClass().getSummaryJavaClass();
        SummaryJavaField sfield = sclass.getJavaField(jv.getName());
        if (sfield == null) {
            return false;
        }

        Iterator it = jm.getJavaVariables().iterator();
        while (it.hasNext()) {
            JavaVariable v = (JavaVariable)it.next();
            SummaryJavaField sf = v.getDeclField();
            if (sf != null && sfield.equals(sf)) {
                return true;
            }
        }
        return false;
    }


    /**** Local variables ****/

    public boolean existsSameNameVariableInMethod(String name, JavaMethod jm) {
        Iterator it = jm.getJavaVariables().iterator();
        while (it.hasNext()) {
            JavaVariable jv = (JavaVariable)it.next();
            if (name.compareTo(jv.getName()) == 0) {
                return true;
            }
        }
        return false;
    }

    public boolean refersToClass(JavaVariable jv, JavaClass jc) {
        if (jv == null || jv.getType() == null || jc == null || jv.getQualifiedType() == null) {
            return false;
        }
        String qname = jc.getQualifiedName();
        if (qname != null && qname.compareTo(jv.getQualifiedType()) == 0) {
            return true;
        }
        return false;
    }

    public boolean refersToPrivateField(JavaVariable jv) {
        JavaStatement jst = jv.getJavaClass().getJavaField(jv);
        if (jst != null) {
            JavaVariable jfield = jst.getDeclaration();
            if (jfield.isPrivate()) {
                return true;
            }
        }
        return false;
    }

    public boolean isAllSame(JavaVariableList vlist) {
        if (vlist.isEmpty()) {
            return false;
        }
        String name = vlist.getFirst().getName();
        Iterator it = vlist.iterator();
        while (it.hasNext()) {
            JavaVariable jv = (JavaVariable)it.next();
            if (name.compareTo(jv.getName()) != 0) {
                return false;
            }
        }
        return true;
    }

    public boolean isAllDefinedInSimpleAssignments(JavaVariable jv, JavaClass jc) {
        Iterator it = jc.getJavaMethods().iterator();
        while (it.hasNext()) {
            JavaMethod jm = (JavaMethod)it.next();
            if (!isAllDefinedInSimpleAssignment(jv, jm)) {
                return false;
            }
        }
        return true;
    }

    public boolean isAllDefinedInSimpleAssignment(JavaVariable jv, JavaMethod jm) {
        makeCFG(jm);

        Iterator it = jm.getCFG().getNodes().iterator(); 
        while (it.hasNext()) {
            CFGNode node = (CFGNode)it.next();
            if (node.hasDefVariable()) {
                CFGStatementNode st = (CFGStatementNode)node;
                if (st.getDefVariables().contains(jv)) {

                    JavaStatement jst = (JavaStatement)st.getJavaComponent();
                    if (!jst.isStatementExpression()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public boolean isLocallyUsedInMethod(JavaVariable jv) {
        JavaMethod jm = jv.getJavaMethod();

        List vlist = new ArrayList();
        Iterator it = jm.getJavaVariables().iterator();
        while (it.hasNext()) {
            JavaVariable v = (JavaVariable)it.next();
            SummaryJavaField sfield = v.getDeclField();
            if (sfield == null && jv.equals(v)) {
                vlist.add(v);
            }
        }
        return (vlist.size() > 1);
    }

    /*
    public JavaVariableList collectFieldsVariablesReferringToFields(JavaMethod jm, JavaClass jc) {
        SummaryJavaClass sc = getJavaClass(jc);
        JavaVariableList vlist = new JavaVariableList();

        Iterator it = jm.getJavaVariables().iterator();
        while (it.hasNext()) {
            JavaVariable jv = (JavaVariable)it.next();
            SummaryJavaField sfield = jv.getDeclField();
            if (sfield != null && sc.equals(sfield.getJavaClass())) {
                vlist.add(jv);
            }
        }
        return vlist;
    }
    */

    private String readFile(String name) {
        StringBuffer content;
        try {
            File file = new File(name);
            BufferedReader reader = new BufferedReader(new FileReader(file));
            content = new StringBuffer();
            while (reader.ready()) {
                String oneLineString = reader.readLine();
                if (oneLineString == null) {
                    break;
                }
                content.append(oneLineString + "\n");
            }
            reader.close();

            return new String(content);

        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }
        return null;
    }

    /*
    private List parseFiles() {
        List files = Summary.getInstance().getSummaryJavaFiles();

        List flist = new ArrayList();  // JavaFile
        Iterator it = files.iterator();
        while (it.hasNext()) {
            SummaryJavaFile sfile = (SummaryJavaFile)it.next();
            JavaFile jfile = getJavaFile(sfile.getName());
            flist.add(jfile);
        }
        return flist;
    }
    */
    
    private List parsedFiles = new ArrayList();  // JavaFile
    private List parseFiles() {
        final List files = Summary.getInstance().getSummaryJavaFiles();
        final ParsingMonitor monitor = new ParsingMonitor(frame,
          "Parsing Java files:", 0, files.size());
        monitor.setProgress(1);

        Thread parser = new Thread() {
            private SummaryJavaFile sfile;
            private int num = 1;
            
            public void run() {
                isCanceled = false;
                Iterator it = files.iterator();
                while (it.hasNext()) {
                    sfile = (SummaryJavaFile)it.next();

                    JavaFile jfile = getJavaFile(sfile.getName());
                    parsedFiles.add(jfile);

                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            monitor.setNote(sfile.getName());
                            monitor.setProgress(num);
                        }
                    });
                    num++;
                    
                    if (monitor.isCanceled()) {
                        isCanceled = true;
                        break;
                    }
                }
                monitor.close();
            }
        };

        parsedFiles.clear();
        parser.start();
        monitor.setVisible(true);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) { }

        if (isCanceled) {
            parsedFiles.clear();
        }
        return parsedFiles;
    }
}
