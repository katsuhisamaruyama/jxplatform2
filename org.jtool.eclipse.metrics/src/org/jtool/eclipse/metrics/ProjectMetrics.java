/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.metrics;

import org.jtool.eclipse.javamodel.JavaProject;
import org.jtool.eclipse.util.TimeInfo;
import java.util.List;
import java.util.ArrayList;
import java.time.ZonedDateTime;

/**
 * Stores metric information on a project.
 * @author Katsuhisa Maruyama
 */
public class ProjectMetrics extends Metrics implements MetricsSort {
    
    public static final String Id = "ProjectMetrics";
    
    protected String path;
    protected ZonedDateTime time;
    
    protected List<PackageMetrics> packages = new ArrayList<PackageMetrics>();
    
    public ProjectMetrics(JavaProject jproject, ZonedDateTime time) {
        super(jproject.getName());
        
        path = jproject.getPath();
        this.time = time;
    }
    
    public void collect(JavaProject jproject) {
        PackageMetrics.sort(packages);
        collectMetrics(jproject);
        collectMetricsMax();
    }
    
    public ProjectMetrics(String name, String path, ZonedDateTime time) {
        super(name);
        this.path = path;
        this.time = time;
    }
    
    public String getName() {
        return fqn;
    }
    
    public String getPath() {
        return path;
    }
    
    public ZonedDateTime getTime() {
        return time;
    }
    
    public long getTimeAsLong() {
        return TimeInfo.getTimeAsLong(time);
    }
    
    public String getTimeAsString() {
        return TimeInfo.getTimeAsISOString(time);
    }
    
    public String getFormatedDate() {
        return TimeInfo.getFormatedDate(time);
    }
    
    public void addPackage(PackageMetrics mpackage) {
        if (!packages.contains(mpackage)) {
            packages.add(mpackage);
        }
    }
    
    public List<PackageMetrics> getPackages() {
        return packages;
    }
    
    public void sortPackages() {
        PackageMetrics.sort(packages);
    }
    
    public List<ClassMetrics> getClasses() {
        List<ClassMetrics> classes = new ArrayList<ClassMetrics>();
        for (PackageMetrics mpackage : getPackages()) {
            classes.addAll(mpackage.getClasses());
        }
        ClassMetrics.sort(classes);
        return classes;
    }
    
    private void collectMetrics(JavaProject jproject) {
        putMetricValue(LINES_OF_CODE, sum(LINES_OF_CODE));
        putMetricValue(NUMBER_OF_STATEMENTS, sum(NUMBER_OF_STATEMENTS));
        
        putMetricValue(NUMBER_OF_FILES, new Double(jproject.getFiles().size()));
        putMetricValue(NUMBER_OF_PACKAGES, new Double(jproject.getPackages().size()));
        
        putMetricValue(NUMBER_OF_CLASSES, new Double(jproject.getClasses().size()));
        putMetricValue(NUMBER_OF_METHODS, sum(NUMBER_OF_METHODS));
        putMetricValue(NUMBER_OF_FIELDS, sum(NUMBER_OF_FIELDS));
        putMetricValue(NUMBER_OF_METHODS_AND_FIELDS, sum(NUMBER_OF_METHODS_AND_FIELDS));
        putMetricValue(NUMBER_OF_PUBLIC_METHODS, sum(NUMBER_OF_PUBLIC_METHODS));
        putMetricValue(NUMBER_OF_PUBLIC_FIELDS, sum(NUMBER_OF_PUBLIC_FIELDS));
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
        
        for (PackageMetrics mpackage: packages) {
            for (ClassMetrics mclass: mpackage.getClasses()) {
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
    
    private Double sum(String sort) {
        double value = 0;
        for (PackageMetrics mpackage : packages) {
            value = value + mpackage.getMetricValue(sort);
        }
        return new Double(value);
    }
    
    public void collectMetricsAfterXMLImport() {
        PackageMetrics.sort(packages);
        collectMetricsMax();
    }
}
