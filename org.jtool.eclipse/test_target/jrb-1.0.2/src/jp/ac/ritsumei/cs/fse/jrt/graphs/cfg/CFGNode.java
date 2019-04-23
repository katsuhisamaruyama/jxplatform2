/*
 *     CFGNode.java  Sep 21, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.graphs.cfg;
import jp.ac.ritsumei.cs.fse.jrt.graphs.pdg.PDGNode;
import jp.ac.ritsumei.cs.fse.jrt.graphs.util.*;
import jp.ac.ritsumei.cs.fse.jrt.model.*;
import java.util.Iterator;

public class CFGNode extends GraphNode {
    protected JavaComponent javaComp;
    protected PDGNode pdgNode;
    private BasicBlock basicBlock = null;

    protected CFGNode() {
        super();
    }

    public CFGNode(int sort) {
        super(sort);
        javaComp = null;
    }

    public CFGNode(int sort, JavaComponent comp) {
        super(sort);
        javaComp = comp;
        comp.setCFGNode(this);
    }

    public JavaComponent getJavaComponent() {
        return javaComp;
    }

    public void setPDGNode(PDGNode node) {
        pdgNode = node;
    }

    public PDGNode getPDGNode() {
        return pdgNode;
    }

    public void setBasicBlock(BasicBlock block) {
        basicBlock = block;
    }

    public BasicBlock getBasicBlock() {
        return basicBlock;
    }

    public GraphComponentSet getPredecessors() {
        return getSrcNodes();
    }

    public GraphComponentSet getSuccessors() {
        return getDstNodes();
    }

    public int getPredecessorsNumber() {
        return getSrcNodes().size();
    }

    public int getSuccessorsNumber() {
        return getDstNodes().size();
    }

    public boolean isBranch() { 
        if (outgoingEdges.size() > 1) {
            return true;
        }
        return false;
    }

    public boolean isJoin() {
        if (incomingEdges.size() > 1) {
            return true;
        }
        return false;
    }

    public boolean isNextToBranch() {
        Iterator it = srcNodes.iterator();
        while (it.hasNext()) {
            CFGNode node = (CFGNode)it.next();
            if (node.isBranch()) {
                return true;
            }
        }
        return false;
    }

    public boolean isLeader() {
        if (basicBlock == null) {
            return false;
        }
        return this.equals(basicBlock.getLeader());
    }

    public boolean isLoop() {
        if (sort == whileSt || sort == doSt || sort == forSt) {
            return true;
        }
        return false;
    }

    public boolean isNormalStatement() {
        if (isAssignmentSt() || isBranchSt() || isCallSt() || isSwitchSt()) {
            return true;
        }
        return false;
    }

    public boolean isAssignmentSt() {
        if (sort / 10 == assignments) {
            return true;
        }
        return false;
    }

    public boolean isBranchSt() {
        if (sort / 10 == branches) {
            return true;
        }
        return false;
    }

    public boolean isEntrySt() {
        if (sort == methodEntry) {
            return true;
        }
        return false;
    }

    public boolean isCallSt() {
        if (sort / 10 == callings) {
            return true;
        }
        return false;
    }

    public boolean isNullSt() {
        if (sort / 10 == nulls) {
            return true;
        }
        return false;
    }

    public boolean isSwitchSt() {
        if (sort == switchSt) {
            return true;
        }
        return false;
    }

    public boolean isReturnSt() {
        if (sort == returnSt) {
            return true;
        }
        return false;
    }

    public boolean isParameterSt() {
        if (sort / 10 == parameters) {
            return true;
        }
        return false;
    }

    public boolean isFormalSt() {
        if (sort == formalIn || sort == formalOut) {
            return true;
        }
        return false;
    }

    public boolean isSwitchLabel() {
        if (sort == switchLabel) {
            return true;
        }
        return false;
    }

    public boolean isMergeSt() {
        if (sort == mergeSt) {
            return true;
        }
        return false;
    }

    public boolean hasDefVariable() {
        if (isAssignmentSt() || isBranchSt() || isCallSt() || isParameterSt()) {
            return true;
        }
        return false;
    }

    public boolean hasUseVariable() {
        if (isAssignmentSt() || isBranchSt() || isCallSt() || isParameterSt()) {
            return true;
        }
        return false;
    }

    public void print(String mesg) {
        System.out.print(id + ": ");
        System.out.println(mesg);
    }
}
