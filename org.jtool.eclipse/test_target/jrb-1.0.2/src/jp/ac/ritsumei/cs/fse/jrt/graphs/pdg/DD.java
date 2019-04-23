/*
 *     DD.java  Oct 8, 2001
 *
 *     Katsuhiko Yoshikawa (kappy@fse.cs.ritsumei.ac.jp)
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.graphs.pdg;
import jp.ac.ritsumei.cs.fse.jrt.graphs.util.*;
import jp.ac.ritsumei.cs.fse.jrt.model.JavaVariable;

public class DD extends Dependence {
    private JavaVariable var;
    private PDGNode loopCarriedNode = null;

    private DD() {
        super();
    }

    public DD(PDGNode src, PDGNode dst) {
        super(src, dst);
    }

    public DD(PDGNode src, PDGNode dst, JavaVariable v) {
        super(src, dst);
        var = v;
    }

    public void setVariable(JavaVariable v) {
        var = v;
    }

    public JavaVariable getVariable() {
        return var;
    }

    public void setLoopCarriedNode(PDGNode n) {
        loopCarriedNode = n;
    }

    public PDGNode getLoopCarriedNode() {
        return loopCarriedNode;
    }

    public boolean isLoopCarried() {
        if (loopCarriedNode != null) {
            return true; 
        }
        return false;  // loop independent
    }

    public boolean equals(GraphComponent c) {
        GraphEdge edge = (GraphEdge)c;
        if (this == edge) {
            return true;
        }
        if (src.equals(edge.getSrcNode()) && dst.equals(edge.getDstNode())
            && sort == edge.getSort() && var.equals(getVariable())) {
            return true;
        }
        return false;
    }

    public void setDefUse() {
        sort = defuseDependence;
    }

    public boolean isDefUse() {
        if (sort == defuseDependence) {
            return true;
        }
        return false;
    }

    public void setDefOrder() {
        sort = deforderDependence;
    }

    public boolean isDefOrder() {
        if (sort == deforderDependence) {
            return true;
        }
        return false;
    }

    public void setOutput() {
        sort = outputDependence;
    }

    public boolean isOutput() {
        if (sort == outputDependence) {
            return true;
        }
        return false;
    }

    public void setAnti() {
        sort = antiDependence;
    }

    public boolean isAnti() {
        if (sort == antiDependence) {
            return true;
        }
        return false;
    }

    public void print() {
        String mesg = "";
        switch (sort) {
            case defuseDependence: mesg = "DD"; break;
            case deforderDependence: mesg = "DO"; break;
            case outputDependence: mesg = "OD"; break;
            case antiDependence: mesg = "AD"; break;
        }
        System.out.print("Edge: " + src.getID() + " -> " + dst.getID() + ": "
          + mesg + " [ " + var.getNameWithID() + " ]");

        if (isLoopCarried()) {
            System.out.println(" (LoopCarried: " + getLoopCarriedNode().getID() + ")");
        } else {
            System.out.println();
        }
    }
}
