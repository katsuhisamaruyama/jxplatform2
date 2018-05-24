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
 * @author Katsuhisa Maruyama
 */
public class LOC extends Metric {
    
    public static final String Name = "LOC";
    private static final String Description = "Lines of code";
    
    public LOC() {
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
        return mproject.getMetricValueWithException(LINES_OF_CODE);
    }
    
    @Override
    public double valueOf(PackageMetrics mpackage) throws UnsupportedMetricsException {
        return mpackage.getMetricValueWithException(LINES_OF_CODE);
    }
    
    @Override
    public double valueOf(ClassMetrics mclass) throws UnsupportedMetricsException {
        return mclass.getMetricValueWithException(LINES_OF_CODE);
    }
    
    @Override
    public double valueOf(MethodMetrics mmethod) throws UnsupportedMetricsException {
        return mmethod.getMetricValueWithException(LINES_OF_CODE);
    }
    
    @Override
    public double valueOf(FieldMetrics mfield) throws UnsupportedMetricsException {
        return mfield.getMetricValueWithException(LINES_OF_CODE);
    }
    
    @Override
    public double maxValueIn(ProjectMetrics mproject) throws UnsupportedMetricsException {
        return mproject.getMetricValueWithException(MAX_LINE_OF_CODE);
    }
    
    @Override
    public double maxValueIn(PackageMetrics mpackage) throws UnsupportedMetricsException {
        return mpackage.getMetricValueWithException(MAX_LINE_OF_CODE);
    }
    
    @Override
    public double maxValueIn(ClassMetrics mclass) throws UnsupportedMetricsException {
        return mclass.getMetricValueWithException(MAX_LINE_OF_CODE);
    }
}
