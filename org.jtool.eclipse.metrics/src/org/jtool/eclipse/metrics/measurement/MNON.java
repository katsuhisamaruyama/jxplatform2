/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.metrics.measurement;

import org.jtool.eclipse.metrics.ClassMetrics;
import org.jtool.eclipse.metrics.MethodMetrics;
import org.jtool.eclipse.metrics.UnsupportedMetricsException;

/**
 * @author Katsuhisa Maruyama
 */
public class MNON extends Metric {
    
    public static final String Name = "MNON";
    private static final String Description = "Maximum number of nesting";
    
    public MNON() {
        super(Name, Description);
    }
    
    @Override
    public boolean isMethodMetric() {
        return true;
    }
    
    @Override
    public double valueOf(MethodMetrics mmethod) throws UnsupportedMetricsException {
        return mmethod.getMetricValueWithException(MAX_NUMBER_OF_NESTING);
    }
    
    @Override
    public double maxValueIn(ClassMetrics mclass) throws UnsupportedMetricsException {
        return mclass.getMetricValueWithException(MAX_MAX_NUMBER_OF_NESTING);
    }
}
