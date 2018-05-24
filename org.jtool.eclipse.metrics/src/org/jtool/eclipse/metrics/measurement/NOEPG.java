/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.metrics.measurement;

import org.jtool.eclipse.metrics.ProjectMetrics;
import org.jtool.eclipse.metrics.PackageMetrics;
import org.jtool.eclipse.metrics.UnsupportedMetricsException;

/**
 * @author Katsuhisa Maruyama
 */
public class NOEPG extends Metric {
    
    public static final String Name = "NOFL";
    private static final String Description = "Number of efferent packages";
    
    public NOEPG() {
        super(Name, Description);
    }
    
    @Override
    public boolean isPackageMetric() {
        return true;
    }
    
    @Override
    public double valueOf(PackageMetrics mpackage) throws UnsupportedMetricsException {
        return mpackage.getMetricValueWithException(NUMBER_OF_EFFERENT_PACKAGES);
    }
    
    @Override
    public double maxValueIn(ProjectMetrics mproject) throws UnsupportedMetricsException {
        return mproject.getMetricValueWithException(MAX_NUMBER_OF_EFFERENT_PACKAGES);
    }
}
