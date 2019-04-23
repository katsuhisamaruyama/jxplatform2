/*
 *     CFGMergeNode.java  Nov 29, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.graphs.cfg;
import jp.ac.ritsumei.cs.fse.jrt.graphs.util.*;
import jp.ac.ritsumei.cs.fse.jrt.model.*;

public class CFGMergeNode extends CFGNode {
    private CFGNode branch;

    public CFGMergeNode(CFGNode node) {
        super(mergeSt);
        branch = node;
    }

    public void setBranchNode(CFGNode node) {
        branch = node;
    }

    public CFGNode getBranchNode() {
        return branch;
    }

    public void print() {
        print("merge");
    }
}
