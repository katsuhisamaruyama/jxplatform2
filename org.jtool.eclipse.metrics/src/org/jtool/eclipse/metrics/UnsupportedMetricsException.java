/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.metrics;

/**
 * An object encapsulating a exception with respect to a metric measurement.
 * @author Katsuhisa Maruyama
 */
public class UnsupportedMetricsException extends Exception {
    
    private static final long serialVersionUID = -1035705745652052966L;
    
    public UnsupportedMetricsException() {
        super();
    }
    
    public UnsupportedMetricsException(String mesg) {
        super("Unsupported: " + mesg);
    }
    
    @Override
    public String getMessage() {
        return super.getMessage();
    }
}