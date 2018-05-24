/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.metrics;

import org.jtool.eclipse.javamodel.JavaPackage;
import org.jtool.eclipse.javamodel.JavaClass;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Stores metric information on a package.
 * @author Katsuhisa Maruyama
 */
public class PackageMetrics extends Metrics implements MetricsSort {
    
    public static final String Id = "PackageMetrics";
    
    private ProjectMetrics projectMetrics;
    
    protected List<ClassMetrics> classes = new ArrayList<ClassMetrics>();
    protected List<String> afferentPackageNames = new ArrayList<String>();
    protected List<String> efferentPackageNames = new ArrayList<String>();
    
    public PackageMetrics(JavaPackage jpackage, ProjectMetrics mproject) {
        super(jpackage.getName());
        
        projectMetrics = mproject;
        for (JavaClass jclass : jpackage.getClasses()) {
            ClassMetrics mclass = new ClassMetrics(jclass, this);
            classes.add(mclass);
        }
        
        for (JavaPackage jp : jpackage.getAfferentJavaPackages()) {
            addAfferentPackage(jp.getName());
        }
        for (JavaPackage jp : jpackage.getEfferentJavaPackages()) {
            addEfferentPackage(jp.getName());
        }
        
        ClassMetrics.sort(classes);
        sortNames(afferentPackageNames);
        sortNames(efferentPackageNames);
        collectMetrics(jpackage);
        collectMetricsMax();
    }
    
    public String getName() {
        return fqn;
    }
    
    public PackageMetrics(String name, ProjectMetrics mproject) {
        super(name);
        projectMetrics = mproject;
    }
    
    public ProjectMetrics getProject() {
        return projectMetrics;
    }
    
    protected void addClass(ClassMetrics mclass) {
        classes.add(mclass);
    }
    
    public List<ClassMetrics> getClasses() {
        return classes;
    }
    
    public void sortClasses() {
        ClassMetrics.sort(classes);
    }
    
    protected void addAfferentPackage(String name) {
        if (!afferentPackageNames.contains(name)) {
            afferentPackageNames.add(name);
        }
    }
    
    public List<String> getAfferentPackages() {
        return afferentPackageNames;
    }
    
    protected void addEfferentPackage(String name) {
        if (!efferentPackageNames.contains(name)) {
            efferentPackageNames.add(name);
        }
    }
    
    public List<String> getEfferentPackages() {
        return efferentPackageNames;
    }
    
    protected void collectMetrics(JavaPackage jpackage) {
        putMetricValue(LINES_OF_CODE, sum(LINES_OF_CODE));
        putMetricValue(NUMBER_OF_STATEMENTS, sum(NUMBER_OF_STATEMENTS));
        
        putMetricValue(NUMBER_OF_CLASSES, new Double(jpackage.getClasses().size()));
        putMetricValue(NUMBER_OF_METHODS, sum(NUMBER_OF_METHODS));
        putMetricValue(NUMBER_OF_FIELDS, sum(NUMBER_OF_FIELDS));
        putMetricValue(NUMBER_OF_METHODS_AND_FIELDS, sum(NUMBER_OF_METHODS_AND_FIELDS));
        putMetricValue(NUMBER_OF_PUBLIC_METHODS, sum(NUMBER_OF_PUBLIC_METHODS));
        putMetricValue(NUMBER_OF_PUBLIC_FIELDS, sum(NUMBER_OF_PUBLIC_FIELDS));
        
        putMetricValue(NUMBER_OF_AFFERENT_PACKAGES, new Double(jpackage.getAfferentJavaPackages().size()));
        putMetricValue(NUMBER_OF_EFFERENT_PACKAGES, new Double(jpackage.getEfferentJavaPackages().size()));
    }
    
    protected void collectMetricsMax() {
        double maxLOC = 0;
        double maxNOST = 0;
        
        double maxNOCL = 0;
        double maxNOMD = 0;
        double maxNOFD = 0;
        double maxNOMF = 0;
        double maxNOPM = 0;
        double maxNOPF = 0;
        
        double maxNOAC = 0;
        double maxNOEC = 0;
        
        double maxCBO = 0;
        double maxDIT = 0;
        double maxNOC = 0;
        double maxRFC = 0;
        double maxWMC = 0;
        double maxLCOM = 0;
        
        for (ClassMetrics mclass: classes) {
            maxLOC = Math.max(maxLOC, mclass.getMetricValue(LINES_OF_CODE));
            maxNOST = Math.max(maxNOST, mclass.getMetricValue(NUMBER_OF_STATEMENTS));
            
            maxNOCL = Math.max(maxNOCL, mclass.getMetricValue(NUMBER_OF_CLASSES));
            maxNOMD = Math.max(maxNOMD, mclass.getMetricValue(NUMBER_OF_METHODS));
            maxNOFD = Math.max(maxNOFD, mclass.getMetricValue(NUMBER_OF_FIELDS));
            maxNOMF = Math.max(maxNOMF, mclass.getMetricValue(NUMBER_OF_METHODS_AND_FIELDS));
            maxNOPM = Math.max(maxNOPM, mclass.getMetricValue(NUMBER_OF_PUBLIC_METHODS));
            maxNOPF = Math.max(maxNOPF, mclass.getMetricValue(NUMBER_OF_PUBLIC_FIELDS));
            
            maxNOAC = Math.max(maxNOAC, mclass.getMetricValue(NUMBER_OF_AFFERENT_CLASSES));
            maxNOEC = Math.max(maxNOEC, mclass.getMetricValue(NUMBER_OF_EFFERENT_CLASSES));
            
            maxCBO = Math.max(maxCBO, mclass.getMetricValue(COUPLING_BETWEEN_OBJECTS));
            maxDIT = Math.max(maxDIT, mclass.getMetricValue(DEPTH_OF_INHERITANCE_TREE));
            maxNOC = Math.max(maxNOC, mclass.getMetricValue(NUMBER_OF_CHILDREN));
            maxRFC = Math.max(maxRFC, mclass.getMetricValue(RESPONSE_FOR_CLASS));
            maxWMC = Math.max(maxWMC, mclass.getMetricValue(WEIGHTED_METHODS_PER_CLASS));
            maxLCOM = Math.max(maxLCOM, mclass.getMetricValue(LACK_OF_COHESION_OF_METHODS));
        }
        
        putMetricValue(MAX_LINE_OF_CODE, maxLOC);
        putMetricValue(MAX_NUMBER_OF_STATEMENTS, maxNOST);
        
        putMetricValue(MAX_NUMBER_OF_CLASSES, maxNOCL);
        putMetricValue(MAX_NUMBER_OF_METHODS, maxNOMD);
        putMetricValue(MAX_NUMBER_OF_FIELDS, maxNOFD);
        putMetricValue(MAX_NUMBER_OF_METHODS_AND_FIELDS, maxNOMF);
        putMetricValue(MAX_NUMBER_OF_PUBLIC_METHODS, maxNOPM);
        putMetricValue(MAX_NUMBER_OF_PUBLIC_FIELDS, maxNOPF);
        
        putMetricValue(MAX_NUMBER_OF_AFFERENT_CLASSES, maxNOAC);
        putMetricValue(MAX_NUMBER_OF_EFFERENT_CLASSES, maxNOEC);
        
        putMetricValue(MAX_COUPLING_BETWEEN_OBJECTS, maxCBO);
        putMetricValue(MAX_DEPTH_OF_INHERITANCE_TREE, maxDIT);
        putMetricValue(MAX_NUMBER_OF_CHILDREN, maxNOC);
        putMetricValue(MAX_RESPONSE_FOR_CLASS, maxRFC);
        putMetricValue(MAX_WEIGHTED_METHODS_PER_CLASS, maxWMC);
        putMetricValue(MAX_LACK_OF_COHESION_OF_METHODS, maxLCOM);
    }
    
    protected Double sum(String sort) {
        double value = 0;
        for (ClassMetrics mclass : classes) {
            value = value + mclass.getMetricValue(sort);
        }
        return new Double(value);
    }
    
    public static void sort(List<PackageMetrics> packages) {
        Collections.sort(packages, new Comparator<PackageMetrics>() {
            public int compare(PackageMetrics mpackage1, PackageMetrics mpackage2) {
                return mpackage1.getName().compareTo(mpackage2.getName());
            }
        });
    }
    
    public void collectMetricsAfterXMLImport() {
        ClassMetrics.sort(classes);
        sortNames(afferentPackageNames);
        sortNames(efferentPackageNames);
        collectMetricsMax();
        for (ClassMetrics mclass : classes) {
            mclass.collectMetricsAfterXMLImport();
        }
    }
}
