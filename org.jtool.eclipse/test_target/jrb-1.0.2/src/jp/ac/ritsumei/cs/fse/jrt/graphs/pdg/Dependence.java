/*
 *     Dependence.java  Oct 9, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.graphs.pdg;
import jp.ac.ritsumei.cs.fse.jrt.graphs.util.*;

public class Dependence extends GraphEdge {
    protected Dependence() {
        super();
    }

    public Dependence(PDGNode src, PDGNode dst) {
        super(src, dst);
    }

    public boolean isCD() {
        if (sort / 10 == CDs) {
            return true;
        }
        return false;
    }

    public boolean isDD() {
        if (sort / 10 == DDs) {
            return true;
        }
        return false;
    }

    public boolean isDU() {
        if (sort == defuseDependence) {
            return true;
        }
        return false;
    }

    public void print() {
        System.out.println("Edge: " + src.getID() + " -> " + dst.getID());
    }
}
