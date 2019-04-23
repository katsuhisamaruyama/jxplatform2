/*
 *     GraphEdgeSort.java  Nov 30, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.graphs.util;

public interface GraphEdgeSort {
    public static final int Flows            = 1;
    public static final int trueControlFlow  = 11;
    public static final int falseControlFlow = 12;
    public static final int fallThroughFlow  = 13;
    public static final int parameterFlow    = 14;

    public static final int CDs                    = 2;
    public static final int trueControlDependence  = 21;
    public static final int falseControlDependence = 22;
    public static final int fallControlDependence  = 23;

    public static final int DDs                = 3;
    public static final int defuseDependence   = 31;
    public static final int deforderDependence = 32;
    public static final int outputDependence   = 33;
    public static final int antiDependence     = 34;
}
