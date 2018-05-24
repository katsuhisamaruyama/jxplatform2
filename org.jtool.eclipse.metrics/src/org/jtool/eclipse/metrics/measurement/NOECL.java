/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.metrics.measurement;

import org.jtool.eclipse.metrics.ProjectMetrics;
import org.jtool.eclipse.metrics.PackageMetrics;
import org.jtool.eclipse.metrics.ClassMetrics;
import org.jtool.eclipse.metrics.UnsupportedMetricsException;

/**
 * @author Katsuhisa Maruyama
 */
public class NOECL extends Metric {
    
    public static final String Name = "NOECL";
    private static final String Description = "Number of efferent classes";
    
    public NOECL() {
        super(Name, Description);
    }
    
    @Override
    public boolean isClassMetric() {
        return true;
    }
    
    @Override
    public double valueOf(ClassMetrics mclass) throws UnsupportedMetricsException {
        return mclass.getMetricValueWithException(NUMBER_OF_EFFERENT_CLASSES);
    }
    
    @Override
    public double maxValueIn(ProjectMetrics mproject) throws UnsupportedMetricsException {
        return mproject.getMetricValueWithException(MAX_NUMBER_OF_EFFERENT_CLASSES);
    }
    
    @Override
    public double maxValueIn(PackageMetrics mpackage) throws UnsupportedMetricsException {
        return mpackage.getMetricValueWithException(MAX_NUMBER_OF_EFFERENT_CLASSES);
    }
}
