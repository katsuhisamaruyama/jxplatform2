/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.metrics.measurement;

import org.jtool.eclipse.metrics.ClassMetrics;
import org.jtool.eclipse.metrics.FieldMetrics;
import org.jtool.eclipse.metrics.MethodMetrics;
import org.jtool.eclipse.metrics.PackageMetrics;
import org.jtool.eclipse.metrics.ProjectMetrics;
import org.jtool.eclipse.metrics.UnsupportedMetricsException;

/**
 * An interface for metric measurements.
 * @author Katsuhisa Maruyama
 */
public interface IMetric {
    
    public String getName();
    public String getDescription();
    
    public boolean isProjectMetric();
    public boolean isPackageMetric();
    public boolean isClassMetric();
    public boolean isMethodMetric();
    public boolean isFieldMetric();
    
    public double valueOf(ProjectMetrics mproject) throws UnsupportedMetricsException;
    public double valueOf(PackageMetrics mpackage) throws UnsupportedMetricsException;
    public double valueOf(ClassMetrics mclass) throws UnsupportedMetricsException;
    public double valueOf(MethodMetrics mmethod) throws UnsupportedMetricsException;
    public double valueOf(FieldMetrics mfield) throws UnsupportedMetricsException;
    
    public double maxValueIn(ProjectMetrics mproject) throws UnsupportedMetricsException;
    public double maxValueIn(PackageMetrics mpackage) throws UnsupportedMetricsException;
    public double maxValueIn(ClassMetrics mclass) throws UnsupportedMetricsException;
    public double maxValueIn(MethodMetrics mmethod) throws UnsupportedMetricsException;
    public double maxValueIn(FieldMetrics mfield) throws UnsupportedMetricsException;
}