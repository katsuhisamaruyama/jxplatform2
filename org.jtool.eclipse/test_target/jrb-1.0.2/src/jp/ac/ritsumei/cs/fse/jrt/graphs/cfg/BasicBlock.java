/*
 *     BasicBlock.java  Oct 16, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.graphs.cfg;
import jp.ac.ritsumei.cs.fse.jrt.graphs.util.*;
import java.util.Iterator;

public class BasicBlock {
    private static int blockNum = 0;
    private int id;
    private CFGNode leader;
    protected GraphComponentSet nodes = new GraphComponentSet();  // CFGNode

    private BasicBlock() {
    }

    public BasicBlock(CFGNode node) {
        blockNum++;
        id = blockNum;
        leader = node;
    }

    public BasicBlock(BasicBlock block) {
        this(block.getLeader());
        Iterator it = block.getNodes().iterator();
        while (it.hasNext()) {
            CFGNode node = (CFGNode)it.next();
            nodes.add(node);
        }
    }

    public int getID() {
        return id;
    }

    public CFGNode getLeader() {
        return leader;
    }

    public void add(CFGNode node) {
        nodes.add(node);
        node.setBasicBlock(this);
    }

    public GraphComponentSet getNodes() {
        return nodes;
    }

    public boolean contains(CFGNode node) {
        return nodes.contains(node);
    }

    public boolean isEmpty() {
        return nodes.isEmpty();
    }

    public boolean equals(BasicBlock block) {
        if (this == block) {
            return true;
        }
        if (leader.equals(block.getLeader())
          && nodes.equals(block.getNodes())) {
            return true;
        }
        return false;
    }

    public GraphComponentSet union(BasicBlock block) {
        return nodes.union(block.getNodes());
    }

    public GraphComponentSet intersection(BasicBlock block) {
        return nodes.intersection(block.getNodes());
    }

    public GraphComponentSet difference(BasicBlock block) {
        return nodes.difference(block.getNodes());
    }

    public void print() {
        System.out.print("BB(" + getID() + ") = {");
        printNodes();
        System.out.println(" }");
    }

    public void printNodes() {
        Iterator it = nodes.iterator();
        while (it.hasNext()) {
            CFGNode node = (CFGNode)it.next();
            System.out.print(" " + node.getID());
        }
    }
}
