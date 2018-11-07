/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.slice;

import org.jtool.eclipse.pdg.PDG;
import org.jtool.eclipse.pdg.PDGNode;
import org.jtool.eclipse.pdg.DD;
import org.jtool.eclipse.pdg.Dependence;
import org.jtool.eclipse.pdg.PDGStatement;
import org.jtool.eclipse.cfg.CFG;
import org.jtool.eclipse.cfg.CFGNode;
import org.jtool.eclipse.cfg.CFGMethodCall;
import org.jtool.eclipse.cfg.JReference;
import org.jtool.eclipse.cfg.StopConditionOnReachablePath;
import org.jtool.eclipse.cfg.CFGStatement;
import org.jtool.eclipse.cfg.JMethodReference;
import org.jtool.eclipse.graph.GraphNode;
import java.util.Set;
import java.util.HashSet;

/**
 * An object storing information about a program slice.
 * 
 * @author Katsuhisa Maruyama
 */
public class Slice {
    
    private CFG cfg;
    private SliceCriterion criterion;
    
    private Set<PDGNode> nodesInSlice = new HashSet<PDGNode>();
    
    public Slice(PDG pdg, SliceCriterion criterion) {
        this.cfg = pdg.getCFG();
        this.criterion = criterion;
        
        for (JReference var : criterion.getnVariables()) {
            extract(criterion.getNode(), var);
        }
    }
    
    public PDGNode getCriterionNode() {
        return criterion.getNode();
    }
    
    public Set<JReference> getCriterionVariables() {
        return criterion.getnVariables();
    }
    
    public Set<PDGNode> getNodes() {
        return nodesInSlice;
    }
    
    private void extract(PDGNode node, JReference jv) {
        if (jv.isFieldAccess()) {
            traverseOnCFG(node, jv);
        } else {
            traverseOnPDG(node, jv);
        }
    }
    
    private void traverseOnCFG(PDGNode node, JReference jv) {
        Set<CFGNode> cfgnodes = cfg.backwardReachableNodes(node.getCFGNode(), cfg.getStartNode(), true);
        for (CFGNode cfgnode : cfgnodes) {
            PDGNode pdgnode = cfgnode.getPDGNode();
            if (pdgnode instanceof PDGStatement) {
                PDGStatement candidate = (PDGStatement)pdgnode;
                if (candidate.definesVariable(jv) || candidate.usesVariable(jv)) {
                    nodesInSlice.add(candidate);
                }
            }
        }
    }
    
    private void traverseOnPDG(PDGNode anchor, JReference jv) {
        for (PDGNode node : findStartNode(anchor, jv)) {
            traverseBackward(node);
        }
    }
    
    private void traverseBackward(PDGNode node) {
        if (nodesInSlice.contains(node)) {
            return;
        }
        nodesInSlice.add(node);
        
        for (Dependence edge : node.getIncomingDependeceEdges()) {
            if (edge.isCD() || edge.isDD2()) {
                
                PDGNode src = edge.getSrcNode();
                traverseBackward(src);
                
                if (edge.isCall()) {
                    CFGMethodCall call = (CFGMethodCall)src.getCFGNode();
                    JReference var = getVariableReference(call.getPrimary());
                    if (var != null) {
                        extract(src, var);
                    }
                }
            }
        }
    }
    
    private JReference getVariableReference(JReference ref) {
        while (ref != null && ref.isMethodCall()) {
            JMethodReference inv = (JMethodReference)ref;
            ref = inv.getPrimary();
        }
        if (ref != null && ref.isVariableAccess()) {
            return ref;
        } else {
            return null;
        }
    }
    
    private Set<PDGNode> findStartNode(PDGNode node, JReference jv) {
        Set<PDGNode> pdgnodes = new HashSet<PDGNode>();
        for (DD edge : node.getIncomingDDEdges()) {
            if (edge.getVariable().equals(jv)) {
                pdgnodes.add(edge.getSrcNode());
            }
        }
        if (pdgnodes.size() > 0) {
            return pdgnodes;
        }
        
        cfg.backwardReachableNodes(node.getCFGNode(), true, new StopConditionOnReachablePath() {
            @Override
            public boolean isStop(CFGNode node) {
                if (node.hasDefVariable()) {
                    CFGStatement cfgnode = (CFGStatement)node;
                    if (cfgnode.defineVariable(jv)) {
                        pdgnodes.add(cfgnode.getPDGNode());
                        return true;
                    }
                }
                return false;
            }
        });
        return pdgnodes;
    }
    
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("----- Slice (from here) -----\n");
        buf.append("Node = " + getCriterionNode().getId() + "; Variable = " + getVariableNames(getCriterionVariables()));
        buf.append("\n");
        buf.append(getNodeInfo());
        buf.append("----- Slice (to here) -----\n");
        return buf.toString();
    }
    
    private String getNodeInfo() {
        StringBuilder buf = new StringBuilder();
        for (GraphNode node : GraphNode.sortGraphNode(nodesInSlice)) {
            buf.append(node.toString());
            buf.append("\n");
        }
        return buf.toString();
    }
    
    private String getVariableNames(Set<JReference> jvs) {
        StringBuilder buf = new StringBuilder();
        for (JReference jv : jvs) {
            buf.append(" " + jv.getName());
        }
        return buf.toString();
    }
}
