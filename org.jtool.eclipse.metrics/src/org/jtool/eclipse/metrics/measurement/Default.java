/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.metrics.measurement;

import org.jtool.eclipse.metrics.ProjectMetrics;
import org.jtool.eclipse.metrics.PackageMetrics;
import org.jtool.eclipse.metrics.ClassMetrics;
import org.jtool.eclipse.metrics.MethodMetrics;
import org.jtool.eclipse.metrics.FieldMetrics;
import org.jtool.eclipse.metrics.UnsupportedMetricsException;

/**
 * Measures nothing and returns always default values.
 * @author Katsuhisa Maruyama
 */
public class Default extends Metric {
    
    public static final String Name = "(default)";
    private static final String Description = "Default metric";
    
    public Default() {
        super(Name, Description);
    }
    
    @Override
    public boolean isProjectMetric() {
        return true;
    }
    
    @Override
    public boolean isPackageMetric() {
        return true;
    }
    
    @Override
    public boolean isClassMetric() {
        return true;
    }
    
    @Override
    public boolean isMethodMetric() {
        return true;
    }
    
    @Override
    public boolean isFieldMetric() {
        return true;
    }
    
    @Override
    public double valueOf(ProjectMetrics mproject) throws UnsupportedMetricsException {
        return -1.0;
    }
    
    @Override
    public double valueOf(PackageMetrics mpackage) throws UnsupportedMetricsException {
        return -1.0;
    }
    
    @Override
    public double valueOf(ClassMetrics mclass) throws UnsupportedMetricsException {
        return -1.0;
    }
    
    @Override
    public double valueOf(MethodMetrics mmethod) throws UnsupportedMetricsException {
        return -1.0;
    }
    
    @Override
    public double valueOf(FieldMetrics mfield) throws UnsupportedMetricsException {
        return -1.0;
    }
    
    @Override
    public double maxValueIn(ProjectMetrics mproject) throws UnsupportedMetricsException {
        return -1.0;
    }
    
    @Override
    public double maxValueIn(PackageMetrics mpackage) throws UnsupportedMetricsException {
        return -1.0;
    }
    
    @Override
    public double maxValueIn(ClassMetrics mclass) throws UnsupportedMetricsException {
        return -1.0;
    }
    
    @Override
    public double maxValueIn(MethodMetrics mmethod) throws UnsupportedMetricsException {
        return -1;
    }
    
    @Override
    public double maxValueIn(FieldMetrics mfield) throws UnsupportedMetricsException {
        return -1.0;
    }
}