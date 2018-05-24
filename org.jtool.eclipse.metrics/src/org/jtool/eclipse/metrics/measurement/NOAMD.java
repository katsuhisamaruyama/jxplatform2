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
public class NOAMD extends Metric {
    
    public static final String Name = "NOAMD";
    private static final String Description = "Number of afferent methods";
    
    public NOAMD() {
        super(Name, Description);
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
    public double valueOf(ClassMetrics mclass) throws UnsupportedMetricsException {
        return mclass.getMetricValueWithException(NUMBER_OF_AFFERENT_METHODS);
    }
    
    @Override
    public double valueOf(MethodMetrics mmethod) throws UnsupportedMetricsException {
        return mmethod.getMetricValueWithException(NUMBER_OF_AFFERENT_METHODS);
    }
    
    @Override
    public double maxValueIn(ClassMetrics mclass) throws UnsupportedMetricsException {
        return mclass.getMetricValueWithException(MAX_NUMBER_OF_AFFERENT_METHODS);
    }
}
