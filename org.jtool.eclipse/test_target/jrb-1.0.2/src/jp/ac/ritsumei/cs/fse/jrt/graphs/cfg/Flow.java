/*
 *     Flow.java  Sep 19, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.graphs.cfg;
import jp.ac.ritsumei.cs.fse.jrt.graphs.util.*;

public class Flow extends GraphEdge {
    private boolean loopback = false;

    private Flow() {
    }

    public Flow(CFGNode src, CFGNode dst) {
        super(src, dst);
    }

    public boolean equals(GraphComponent c) {
        GraphEdge edge = (GraphEdge)c;
        if (this == edge) {
            return true;
        }
        if (src.equals(edge.getSrcNode()) && dst.equals(edge.getDstNode())
            && sort == edge.getSort()) {
            return true;
        }
        return false;
    }

    public void setTrue() {
        sort = trueControlFlow;
    }

    public boolean isTrue() {
        if (sort == trueControlFlow) {
            return true;
        }
        return false;
    }

    public void setFalse() {
        sort = falseControlFlow;
    }

    public boolean isFalse() {
        if (sort == falseControlFlow) {
            return true;
        }
        return false;
    }

    public void setLoopBack(boolean bool) {
        loopback = bool;
    }

    public boolean isLoopBack() {
        return loopback;
    }

    public void setFallThrough() {
        sort = fallThroughFlow;
    }

    public boolean isFallThrough() {
        if (sort == fallThroughFlow) {
            return true;
        }
        return false;
    }

    public void setParameter() {
        sort = parameterFlow;
    }

    public boolean isParameter() {
        if (sort == parameterFlow) {
            return true;
        }
        return false;
    }

    public void print() {
        String mesg = "";
        switch (sort) {
            case trueControlFlow: mesg = "T"; break;
            case falseControlFlow: mesg = "F"; break;
            case fallThroughFlow: mesg = "Fall"; break;
            case parameterFlow: mesg = "P"; break;
        }
        if (loopback) {
            mesg = mesg + "(L)";
        }
        System.out.println("Edge: " + src.getID() + " -> " + dst.getID() + ": " + mesg);
    }
}
