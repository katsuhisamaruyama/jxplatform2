/*
 *     CD.java  Oct 6, 2001
 *
 *     Shota Ueno (mi@fse.cs.ritsumei.ac.jp)
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.graphs.pdg;
import jp.ac.ritsumei.cs.fse.jrt.graphs.util.*;

public class CD extends Dependence {
    private CD() {
        super();
    }

    public CD(PDGNode src, PDGNode dst) {
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
        sort = trueControlDependence;
    }

    public boolean isTrue() {
        if (sort == trueControlDependence) {
            return true;
        }
        return false;
    }

    public void setFalse() {
        sort = falseControlDependence;
    }

    public boolean isFalse() {
        if (sort == falseControlDependence) {
            return true;
        }
        return false;
    }

    public void setFall() {
        sort = fallControlDependence;
    }

    public boolean isFall() {
        if (sort == fallControlDependence) {
            return true;
        }
        return false;
    }

    public void print() {
        String mesg = "";
        switch (sort) {
            case trueControlDependence: mesg = "T"; break;
            case falseControlDependence: mesg = "F"; break;
            case fallControlDependence: mesg = "Fall"; break;
        }
        System.out.println("Edge: " + src.getID() + " -> " + dst.getID() + ": " + mesg);
    }
}
