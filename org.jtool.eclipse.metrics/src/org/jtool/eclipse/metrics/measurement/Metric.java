/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.metrics.measurement;

import org.jtool.eclipse.metrics.ClassMetrics;
import org.jtool.eclipse.metrics.FieldMetrics;
import org.jtool.eclipse.metrics.MethodMetrics;
import org.jtool.eclipse.metrics.MetricsSort;
import org.jtool.eclipse.metrics.PackageMetrics;
import org.jtool.eclipse.metrics.ProjectMetrics;
import org.jtool.eclipse.metrics.UnsupportedMetricsException;

/**
 * A default implementation for metric measurements.
 * @author Katsuhisa Maruyama
 */
public class Metric implements IMetric, MetricsSort{
    
    private String name = "";
    private String description = "";
    
    public Metric() {
    }
    
    protected Metric(String name, String description) {
        this.name = name;
        this.description = description;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public String getDescription() {
        return description;
    }
    
    @Override
    public boolean isProjectMetric() {
        return false;
    }
    
    @Override
    public boolean isPackageMetric() {
        return false;
    }
    
    @Override
    public boolean isClassMetric() {
        return false;
    }
    
    @Override
    public boolean isMethodMetric() {
        return false;
    }
    
    @Override
    public boolean isFieldMetric() {
        return false;
    }
    
    @Override
    public double valueOf(ProjectMetrics mproject) throws UnsupportedMetricsException {
        throw new UnsupportedMetricsException(this.getDescription() + " for a project " + mproject.getName());
    }
    
    @Override
    public double valueOf(PackageMetrics mpackage) throws UnsupportedMetricsException {
        throw new UnsupportedMetricsException(this.getDescription() + " for package " + mpackage.getName());
    }
    
    @Override
    public double valueOf(ClassMetrics mclass) throws UnsupportedMetricsException {
        throw new UnsupportedMetricsException(this.getDescription() + " for class " + mclass.getQualifiedName());
    }
    
    @Override
    public double valueOf(MethodMetrics mmethod) throws UnsupportedMetricsException {
        throw new UnsupportedMetricsException(this.getDescription() + " for method " + mmethod.getQualifiedName());
    }
    
    @Override
    public double valueOf(FieldMetrics mfield) throws UnsupportedMetricsException {
        throw new UnsupportedMetricsException(this.getDescription() + " for field " + mfield.getQualifiedName());
    }
    
    @Override
    public double maxValueIn(ProjectMetrics mproject) throws UnsupportedMetricsException {
        throw new UnsupportedMetricsException(this.getDescription() + " for project " + mproject.getName());
    }
    
    @Override
    public double maxValueIn(PackageMetrics mpackage) throws UnsupportedMetricsException {
        throw new UnsupportedMetricsException(this.getDescription() + " for package " + mpackage.getName());
    }
    
    @Override
    public double maxValueIn(ClassMetrics mclass) throws UnsupportedMetricsException {
        throw new UnsupportedMetricsException(this.getDescription() + " for class " + mclass.getQualifiedName());
    }
    
    @Override
    public double maxValueIn(MethodMetrics mmethod) throws UnsupportedMetricsException {
        throw new UnsupportedMetricsException(this.getDescription() + " for method " + mmethod.getQualifiedName());
    }
    
    @Override
    public double maxValueIn(FieldMetrics mfield) throws UnsupportedMetricsException {
        throw new UnsupportedMetricsException(this.getDescription() + " for field " + mfield.getQualifiedName());
    }
}