/*
 *  Copyright 2018-2020
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg;

import org.jtool.eclipse.graph.GraphEdge;
import org.jtool.eclipse.graph.GraphElement;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;

/**
 * An edge of CFGs, which represents a control flow between CFG nodes.
 * 
 * @author Katsuhisa Maruyama
 */
public class ControlFlow extends GraphEdge {
    
    private CFGNode loopback = null;
    protected Kind kind = Kind.undefined;
    
    public enum Kind {
        trueControlFlow,                 // Control flow outgoing to a true-branch
        falseControlFlow,                // Control flow outgoing to a false-branch
        fallThroughFlow,                 // Control flow representing a fall-through
        methodCall,                      // Flow representing the call to a method
        parameterFlow,                   // Flow representing the relationship between a class/method and its parameter
        exceptionCatchFlow,              // Flow representing the relationship between an exception occurrence and its catch
        undefined,
    }
    
    public ControlFlow(CFGNode src, CFGNode dst) {
        super(src, dst);
    }
    
    public ControlFlow.Kind getKind() {
        return kind;
    }
    
    public void setTrue() {
        kind = Kind.trueControlFlow;
    }
    
    public void setKind(Kind kind) {
        this.kind = kind;
    }
    
    public boolean isTrue() {
        return kind == Kind.trueControlFlow;
    }
    
    public void setFalse() {
        kind = Kind.falseControlFlow;
    }
    
    public boolean isFalse() {
        return kind == Kind.falseControlFlow;
    }
    
    public void setFallThrough() {
        kind = Kind.fallThroughFlow;
    }
    
    public boolean isFallThrough() {
        return kind == Kind.fallThroughFlow;
    }
    
    public void setMethodCall() {
        kind = Kind.methodCall;
    }
    
    public boolean isMethodCall() {
        return kind == Kind.methodCall;
    }
    
    public void setParameter() {
        kind = Kind.parameterFlow;
    }
    
    public boolean isParameter() {
        return kind == Kind.parameterFlow;
    }
    
    public void setExceptionCatch() {
        kind = Kind.exceptionCatchFlow;
    }
    
    public boolean isExceptionCatch() {
        return kind == Kind.exceptionCatchFlow;
    }
    
    @Override
    public CFGNode getSrcNode() {
        return (CFGNode)src;
    }
    
    @Override
    public CFGNode getDstNode() {
        return (CFGNode)dst;
    }
    
    public void setLoopBack(CFGNode node) {
        loopback = node;
    }
    
    public CFGNode getLoopBack() {
        return loopback;
    }
    
    public boolean isLoopBack() {
        return loopback != null;
    }
    
    @Override
    public boolean equals(GraphElement elem) {
        return (elem instanceof ControlFlow) ? equals((ControlFlow)elem) : false;
    }
    
    public boolean equals(ControlFlow flow) {
        return flow != null && (super.equals((GraphEdge)flow) && kind == flow.kind);
    }
    
    @Override
    public int hashCode() {
        return Long.valueOf(src.getId() + dst.getId()).hashCode();
    }
    
    @Override
    public ControlFlow clone() {
        ControlFlow cloneEdge = new ControlFlow(getSrcNode(), getDstNode());
        super.setClone(cloneEdge);
        setClone(cloneEdge);
        return cloneEdge;
    }
    
    protected void setClone(ControlFlow cloneEdge) {
        cloneEdge.setKind(kind);
        cloneEdge.setLoopBack(loopback);
    }
    
    public void print() {
        System.out.println(toString());
    }
    
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(src.getId() + " -> " + dst.getId());
        if (getKind() != null) {
            buf.append(" " + getKind().toString());
        }
        if (loopback != null) {
            buf.append(" (L = " + getLoopBack().getId() + ")");
        }
        return buf.toString();
    }
    
    public static List<ControlFlow> sortControlFlow(Collection<? extends ControlFlow> co) {
        List<ControlFlow> edges = new ArrayList<>(co);
        Collections.sort(edges, new Comparator<>() {
            
            @Override
            public int compare(ControlFlow edge1, ControlFlow edge2) {
                if (edge2.src.getId() == edge1.src.getId()) {
                    return edge2.dst.getId() == edge1.dst.getId() ?
                           edge2.kind.toString().compareTo(edge1.kind.toString()) :
                           edge1.dst.getId() > edge2.dst.getId() ? 1 : -1;
                } else if (edge1.src.getId() > edge2.src.getId()) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });
        return edges;
    }
}
