/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.metrics;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.Comparator;

/**
 * Stores metric values.
 * @author Katsuhisa Maruyama
 */
public class Metrics {
    
    protected String fqn;
    protected Map<String, Double> metricValues = new HashMap<String, Double>();
    
    protected Metrics(String fqn) {
        this.fqn = fqn;
    }
    
    protected void setCodeProperties(int start, int end, int upper, int bottom) {
    }
    
    public String getQualifiedName() {
        return fqn;
    }
    
    public double getMetricValueWithException(String sort) throws UnsupportedMetricsException {
        Double value = metricValues.get(sort);
        if (value != null) {
            return value.doubleValue();
        }
        throw new UnsupportedMetricsException("Cannot obtain the metric value of " + sort);
    }
    
    public double getMetricValue(String sort) {
        Double value = metricValues.get(sort);
        if (value != null) {
            return value.doubleValue();
        }
        return -1.0;
    }
    
    public void putMetricValue(String sort, double value) {
        metricValues.put(sort, new Double(value));
    }
    
    public Map<String, Double> getMetricValues() {
        return metricValues;
    }
    
    public void sortNames(List<String> names) {
        Collections.sort(names, new Comparator<String>() {
            public int compare(String name1, String name2) {
                return name1.compareTo(name2);
            }
        });
    }
}
