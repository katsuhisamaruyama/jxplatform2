/*
 *  Copyright 2019
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg;

import org.jtool.eclipse.graph.Graph;
import java.util.Set;

/**
 * An object storing information about a call graph.
 * 
 * @author Katsuhisa Maruyama
 */
public class CallGraph extends Graph<CFGNode, ControlFlow> {
    
    protected String name;
    
    public CallGraph(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    @Override
    public void add(ControlFlow flow) {
        if (!contains(flow.getSrcNode())) {
            super.add(flow.getSrcNode());
        }
        if (!contains(flow.getDstNode())) {
            super.add(flow.getDstNode());
        }
        if (!contains(flow)) {
            super.add(flow);
        }
    }
    
    public void append(CallGraph callGraph) {
        if (callGraph != null) {
            for (ControlFlow edge : callGraph.getEdges()) {
                add(edge);
            }
        }
    }
    
    public Set<CFGNode> getCalleeNoder(CFGNode src) {
        return src.getSuccessors();
    }
    
    public Set<CFGNode> getCallerNoder(CFGNode dst) {
        return dst.getPredecessors();
    }
    
    public void print() {
        System.out.println(toString());
    }
    
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("----- CallGraph of " + getName() + "-----\n");
        buf.append(getEdgeInfo());
        buf.append("-----------------------------------\n");
        return buf.toString();
    }
    
    @Override
    protected String getEdgeInfo() {
        StringBuilder buf = new StringBuilder();
        int index = 1;
        for (ControlFlow edge : ControlFlow.sortControlFlow(getEdges())) {
            buf.append(String.valueOf(index));
            buf.append(": ");
            buf.append(edge.toString());
            buf.append("\n");
            index++;
        }
        return buf.toString();
    }
}
