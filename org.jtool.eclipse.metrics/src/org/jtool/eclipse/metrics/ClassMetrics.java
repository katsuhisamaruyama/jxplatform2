/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.metrics;

import org.jtool.eclipse.javamodel.JavaClass;
import org.jtool.eclipse.javamodel.JavaField;
import org.jtool.eclipse.javamodel.JavaMethod;

import java.util.List;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Stores metric information on a class.
 * @author Katsuhisa Maruyama
 */
public class ClassMetrics extends CommonMetrics implements MetricsSort {
    
    public static final String Id = "ClassMetrics";
    
    protected JavaClass.Kind kind;
    protected PackageMetrics packageMetrics;
    protected String superClassName;
    protected List<String> superInterfaceNames = new ArrayList<String>();
    protected String path;
    
    protected List<MethodMetrics> methods = new ArrayList<MethodMetrics>();
    protected List<FieldMetrics> fields = new ArrayList<FieldMetrics>();
    protected List<String> afferentClassNames = new ArrayList<String>();
    protected List<String> efferentClassNames = new ArrayList<String>();
    
    public ClassMetrics(JavaClass jclass, PackageMetrics mpackage) {
        super(jclass.getQualifiedName(), jclass.getName(), jclass.getQualifiedName(), jclass.getModifiers());
        
        kind = jclass.getKind();
        packageMetrics = mpackage;
        superClassName = jclass.getSuperClassName();
        superInterfaceNames.addAll(jclass.getSuperInterfaceNames());
        path = jclass.getFile().getRelativePath();
        
        int start = jclass.getCodeRange().getStartPosition();
        int end = jclass.getCodeRange().getEndPosition();
        int upper = jclass.getCodeRange().getUpperLineNumber();
        int bottom = jclass.getCodeRange().getBottomLineNumber();
        setCodeProperties(start, end, upper, bottom);
        
        for (JavaMethod jmethod : jclass.getMethods()) {
            methods.add(new MethodMetrics(jmethod, this));
        }
        for (JavaField jfield: jclass.getFields()) {
            fields.add(new FieldMetrics(jfield, this));
        }
        
        for (JavaClass jc : jclass.getAfferentClasses()) {
            addAfferentClass(jc.getQualifiedName());
        }
        for (JavaClass jc : jclass.getEfferentClasses()) {
            addEfferentClass(jc.getQualifiedName());
        }
        
        MethodMetrics.sort(methods);
        FieldMetrics.sort(fields);
        sortNames(afferentClassNames);
        sortNames(efferentClassNames);
        collectMetrics(jclass);
        collectMetricsMax();
    }
    
    public ClassMetrics(String fqn, String name, int modifiers, String kindStr, String path, PackageMetrics mpackage) {
        super(fqn, name, fqn, modifiers);
        kind = JavaClass.Kind.valueOf(kindStr);
        packageMetrics = mpackage;
        this.path = path;
    }
    
    public JavaClass.Kind getKind() {
        return kind;
    }
    
    public String getPath() {
        return path;
    }
    
    public String getFullPath() {
        return packageMetrics.getProject().getPath() + File.separatorChar + path;
    }
    
    public boolean isClass() {
        return kind == JavaClass.Kind.J_CLASS;
    }
    
    public boolean isInterface() {
        return kind == JavaClass.Kind.J_INTERFACE;
    }
    
    public boolean isEnum() {
        return kind == JavaClass.Kind.J_ENUM;
    }
    
    public boolean isLambda() {
        return kind == JavaClass.Kind.J_LAMBDA;
    }
    
    public PackageMetrics getPackage() {
        return packageMetrics;
    }
    
    public String getPackageName() {
        return packageMetrics.getName();
    }
    
    public void setSuperClass(String name) {
        superClassName = name;
    }
    
    public String getSuperClassName() {
        return superClassName;
    }
    
    public void addSuperInterface(String name) {
        superInterfaceNames.add(name);
    }
    
    public List<String> getSuperInterfaceNames() {
        return superInterfaceNames;
    }
    
    public void addMethod(MethodMetrics mmethod) {
        if (!methods.contains(mmethod)) {
            methods.add(mmethod);
        }
    }
    
    public List<MethodMetrics> getMethods() {
        return methods;
    }
    
    public void sortMethods() {
        MethodMetrics.sort(methods);
    }
    
    public void addField(FieldMetrics mfield) {
        if (!fields.contains(mfield)) {
            fields.add(mfield);
        }
    }
    
    public List<FieldMetrics> getFields() {
        return fields;
    }
    
    public void sortFields() {
        FieldMetrics.sort(fields);
    }
    
    public void addAfferentClass(String name) {
        if (!afferentClassNames.contains(name)) {
            afferentClassNames.add(name);
        }
    }
    
    public List<String> getAfferentClasses() {
        return afferentClassNames;
    }
    
    public void addEfferentClass(String name) {
        if (!efferentClassNames.contains(name)) {
            efferentClassNames.add(name);
        }
    }
    
    public List<String> getEfferentClasses() {
        return efferentClassNames;
    }
    
    public String getSourceCode() {
        return super.getSourceCode(getFullPath());
    }
    
    private void collectMetrics(JavaClass jclass) {
        putMetricValue(LINES_OF_CODE, new Double(bottom - upper + 1));
        putMetricValue(NUMBER_OF_STATEMENTS, sum(MetricsSort.NUMBER_OF_STATEMENTS));
        
        putMetricValue(NUMBER_OF_METHODS, new Double(jclass.getMethods().size()));
        putMetricValue(NUMBER_OF_FIELDS, new Double(jclass.getFields().size()));
        putMetricValue(NUMBER_OF_METHODS_AND_FIELDS, new Double(jclass.getMethods().size() + jclass.getFields().size()));
        putMetricValue(NUMBER_OF_PUBLIC_METHODS, new Double(getNumPublicMethods(jclass)));
        putMetricValue(NUMBER_OF_PUBLIC_FIELDS, new Double(getNumPublicFields(jclass)));
        
        putMetricValue(NUMBER_OF_AFFERENT_CLASSES, new Double(jclass.getAfferentClassesInProject().size()));
        putMetricValue(NUMBER_OF_EFFERENT_CLASSES, new Double(jclass.getEfferentClassesInProject().size()));
        putMetricValue(NUMBER_OF_AFFERENT_METHODS, sum(NUMBER_OF_AFFERENT_METHODS));
        putMetricValue(NUMBER_OF_EFFERENT_METHODS, sum(NUMBER_OF_EFFERENT_METHODS));
        putMetricValue(NUMBER_OF_AFFERENT_FIELDS, sum(NUMBER_OF_AFFERENT_FIELDS));
        
        putMetricValue(COUPLING_BETWEEN_OBJECTS, getCBO(jclass));
        putMetricValue(DEPTH_OF_INHERITANCE_TREE, new Double(jclass.getAllSuperClasses().size()));
        putMetricValue(NUMBER_OF_CHILDREN, new Double(jclass.getChildren().size()));
        putMetricValue(RESPONSE_FOR_CLASS, getRFC(jclass));
        putMetricValue(WEIGHTED_METHODS_PER_CLASS, getWMC(jclass));
        putMetricValue(LACK_OF_COHESION_OF_METHODS, getLCOM(jclass));
    }
    
    protected void collectMetricsMax() {
        double maxLOC = 0;
        double maxNOST = 0;
        
        double maxNOAM = 0;
        double maxNOEM = 0;
        double maxNOAF = 0;
        
        double maxNOPT = 0;
        double maxCC = 0;
        double maxNOVL = 0;
        double maxMNON = 0;
        
        for (MethodMetrics mmethod : methods) {
            maxLOC = Math.max(maxLOC, mmethod.getMetricValue(LINES_OF_CODE));
            maxNOST = Math.max(maxNOST, mmethod.getMetricValue(NUMBER_OF_STATEMENTS));
            
            maxNOAM = Math.max(maxNOST, mmethod.getMetricValue(NUMBER_OF_AFFERENT_METHODS));
            maxNOEM = Math.max(maxNOST, mmethod.getMetricValue(NUMBER_OF_EFFERENT_METHODS));
            maxNOAF = Math.max(maxNOST, mmethod.getMetricValue(NUMBER_OF_AFFERENT_FIELDS));
            
            maxNOPT = Math.max(maxNOPT, mmethod.getMetricValue(NUMBER_OF_PARAMETERS));
            maxCC = Math.max(maxCC, mmethod.getMetricValue(CYCLOMATIC_COMPLEXITY));
            maxNOVL = Math.max(maxNOVL, mmethod.getMetricValue(NUMBER_OF_VARIABLES));
            maxMNON = Math.max(maxMNON, mmethod.getMetricValue(MAX_NUMBER_OF_NESTING));
        }
        
        putMetricValue(MAX_LINE_OF_CODE, maxLOC);
        putMetricValue(MAX_NUMBER_OF_STATEMENTS, maxNOST);
        
        putMetricValue(MAX_NUMBER_OF_AFFERENT_METHODS, maxNOAM);
        putMetricValue(MAX_NUMBER_OF_EFFERENT_METHODS, maxNOEM);
        putMetricValue(MAX_NUMBER_OF_AFFERENT_FIELDS, maxNOAF);
        
        putMetricValue(MAX_NUMBER_OF_PARAMETERS, maxNOPT);
        putMetricValue(MAX_CYCLOMATIC_COMPLEXITY, maxCC);
        putMetricValue(MAX_NUMBER_OF_VARIABLES, maxNOVL);
        putMetricValue(MAX_MAX_NUMBER_OF_NESTING, maxMNON);
    }
    
    private double getNumPublicMethods(JavaClass jclass) {
        int num = 0;
        for (JavaMethod jm : jclass.getMethods()) {
            if (jm.isPublic()) {
                num++;
            }
        }
        return (double)num;
    }
    
    private double getNumPublicFields(JavaClass jclass) {
        int num = 0;
        for (JavaField jf : jclass.getFields()) {
            if (jf.isPublic()) {
                num++;
            }
        }
        return (double)num;
    }
    
    private double getRFC(JavaClass jclass) {
        List<JavaMethod> calledMethods = new ArrayList<JavaMethod>();
        for (JavaMethod jm : jclass.getMethods()) {
            for (JavaMethod m : jm.getCalledMethodsInProject()) {
                calledMethods.add(m);
            }
        }
        return (double)(jclass.getMethods().size() + calledMethods.size());
    }
    
    private double getCBO(JavaClass jclass) {
        List<JavaClass> classes = new ArrayList<JavaClass>();
        collectCoupledClasses(jclass, classes);
        return (double)classes.size();
    }
    
    private void collectCoupledClasses(JavaClass jclass, List<JavaClass> classes) {
        for (JavaClass jc : jclass.getAfferentClassesInProject()) {
            if (!classes.contains(jc)) {
                classes.add(jc);
                collectCoupledClasses(jc, classes);
            }
        }
    }
    
    private double getLCOM(JavaClass jclass) {
        int accessedMethods = 0;
        int cohesiveMethods = 0;
        
        ArrayList<JavaMethod> jmethods = new ArrayList<JavaMethod>(jclass.getMethods());
        for (int i = 0; i < jmethods.size(); i++) {
            for (int j = i + 1; j < jmethods.size(); j++) {
                JavaMethod jm1 = jmethods.get(i);
                JavaMethod jm2 = jmethods.get(j);
                
                for (JavaField jf1 : jm1.getAccessedFieldsInProject()) {
                    for (JavaField jf2 : jm2.getAccessedFieldsInProject()) {
                        if (jf1.equals(jf2)) {
                            cohesiveMethods++;
                        } else {
                            accessedMethods++;
                        }
                    }
                }
            }
        }
        
        if (accessedMethods > cohesiveMethods) {
            return (double)(accessedMethods - cohesiveMethods);
        }
        return (double)0;
    }
    
    private double getWMC(JavaClass jclass) {
        double wmc  = 0;
        for (MethodMetrics mmethod : methods) {
            wmc = wmc + mmethod.getMetricValue(MetricsSort.CYCLOMATIC_COMPLEXITY);
        }
        return wmc;
    }
    
    private Double sum(String sort) {
        double value = 0;
        for (MethodMetrics mmethod : methods) {
            value = value + mmethod.getMetricValue(sort);
        }
        for (FieldMetrics mfield : fields) {
            value = value + mfield.getMetricValue(sort);
        }
        return new Double(value);
    }
    
    public static void sort(List<ClassMetrics> classes) {
        Collections.sort(classes, new Comparator<ClassMetrics>() {
            public int compare(ClassMetrics mclass1, ClassMetrics mclass2) {
                return mclass1.getQualifiedName().compareTo(mclass2.getQualifiedName());
            }
        });
    }
    
    public void collectMetricsAfterXMLImport() {
        MethodMetrics.sort(methods);
        FieldMetrics.sort(fields);
        sortNames(afferentClassNames);
        sortNames(efferentClassNames);
        collectMetricsMax();
    }
}
