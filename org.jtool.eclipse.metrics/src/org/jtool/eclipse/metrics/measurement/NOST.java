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
public class NOST extends Metric {
    
    public static final String Name = "NOST";
    private static final String Description = "Number of statements";
    
    public NOST() {
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
        return mproject.getMetricValueWithException(NUMBER_OF_STATEMENTS);
    }
    
    @Override
    public double valueOf(PackageMetrics mpackage) throws UnsupportedMetricsException {
        return mpackage.getMetricValueWithException(NUMBER_OF_STATEMENTS);
    }
    
    @Override
    public double valueOf(ClassMetrics mclass) throws UnsupportedMetricsException {
        return mclass.getMetricValueWithException(NUMBER_OF_STATEMENTS);
    }
    
    @Override
    public double valueOf(MethodMetrics mmethod) throws UnsupportedMetricsException {
        return mmethod.getMetricValueWithException(NUMBER_OF_STATEMENTS);
    }
    
    @Override
    public double valueOf(FieldMetrics mfield) throws UnsupportedMetricsException {
        return mfield.getMetricValueWithException(NUMBER_OF_STATEMENTS);
    }
    
    @Override
    public double maxValueIn(ProjectMetrics mproject) throws UnsupportedMetricsException {
        return mproject.getMetricValueWithException(MAX_NUMBER_OF_STATEMENTS);
    }
    
    @Override
    public double maxValueIn(PackageMetrics mpackage) throws UnsupportedMetricsException {
        return mpackage.getMetricValueWithException(MAX_NUMBER_OF_STATEMENTS);
    }
    
    @Override
    public double maxValueIn(ClassMetrics mclass) throws UnsupportedMetricsException {
        return mclass.getMetricValueWithException(MAX_NUMBER_OF_STATEMENTS);
    }
}
