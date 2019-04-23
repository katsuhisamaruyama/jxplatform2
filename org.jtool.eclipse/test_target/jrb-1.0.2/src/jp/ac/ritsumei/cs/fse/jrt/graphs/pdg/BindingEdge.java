/*
 *     BindingEdge.java  Sep 10, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.graphs.pdg;
import jp.ac.ritsumei.cs.fse.jrt.graphs.util.*;
import jp.ac.ritsumei.cs.fse.jrt.model.JavaVariable;

public class BindingEdge extends GraphEdge {
    private JavaVariable var;
    private static final int classMemberBinding = 1;
    private static final int methedCallBinding  = 2;
    private static final int parameterBinding   = 3;  // inter-method
    private static final int argumentBinding    = 4;  // intra-method (summary edge)

    public BindingEdge() {
        super();
    }

    public BindingEdge(GraphNode src, GraphNode dst) {
        super(src, dst);
    }

    public BindingEdge(GraphNode src, GraphNode dst, JavaVariable v) {
        super(src, dst);
        var = v;
    }

    public void setVariable(JavaVariable v) {
        var = v;
    }

    public JavaVariable getVariable() {
        return var;
    }

    public void setClassMember() {
        sort = classMemberBinding;
    }

    public boolean isClassMember() {
        if (sort == classMemberBinding) {
            return true;
        }
        return false;
    }

    public void setMethedCall() {
        sort = methedCallBinding;
    }

    public boolean isMethedCall() {
        if (sort == methedCallBinding) {
            return true;
        }
        return false;
    }
    public void setParameter() {
        sort = parameterBinding;
    }

    public boolean isParameter() {
        if (sort == parameterBinding) {
            return true;
        }
        return false;
    }

    public void setArgument() {
        sort = argumentBinding;
    }

    public boolean isArgument() {
        if (sort == argumentBinding) {
            return true;
        }
        return false;
    }
}
