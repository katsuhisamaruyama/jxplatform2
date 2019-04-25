/*
 *  Copyright 2019
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.slice;

import org.jtool.eclipse.pdg.PDG;
import org.jtool.eclipse.pdg.PDGNode;
import org.jtool.eclipse.pdg.Dependence;
import org.jtool.eclipse.pdg.DD;
import org.jtool.eclipse.pdg.CD;
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
    
    private SliceCriterion criterion;
    
    private Set<PDGNode> nodesInSlice = new HashSet<PDGNode>();
    
    public Slice(SliceCriterion criterion) {
        this.criterion = criterion;
        
        for (JReference var : criterion.getnVariables()) {
            extract(criterion.getNode(), var);
        }
    }
    
    public PDG getPDG() {
        return criterion.getPDG();
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
        for (PDGNode start : findStartNode(node, jv)) {
            traverseBackward(start);
        }
        
        for (Dependence edge : node.getIncomingCDEdges()) {
            traverseBackward(edge.getSrcNode());
        }
    }
    
    private boolean breakDDOnFieldAccess(PDGNode node, PDGNode anchor) {
        if (anchor.getCFGNode().isFieldEntry()) {
            for (DD edge : node.getOutgoingDDEdges()) {
                if (edge.isOutput()) {
                    PDGNode dst = edge.getDstNode();
                    for (DD edge2 : dst.getOutgoingDDEdges()) {
                        if (edge2.isDD2()) {
                            PDGNode anchor2 = edge2.getDstNode();
                            if (anchor2.equals(anchor)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
    
    private PDGNode getCDSrcNode(PDGNode node) {
        for (CD edge : node.getIncomingCDEdges()) {
            return edge.getSrcNode();
        }
        return null;
    }
    
    private PDGNode getCDDstNode(PDGNode node) {
        for (CD edge : node.getOutgoingCDEdges()) {
            return edge.getDstNode();
        }
        return null;
    }
    
    private boolean breakDDOnParameterIn(PDGNode node, PDGNode anchor) {
        if (anchor.getCFGNode().isFormalIn()) {
            PDGNode src = getCDSrcNode(node);
            if (src != null)  {
                for (DD edge : src.getOutgoingDDEdges()) {
                    if (edge.isOutput()) {
                        PDGNode dst = edge.getDstNode();
                        PDGNode node2 = getCDDstNode(dst);
                        if (node2 != null) {
                            for (DD edge2 : node2.getOutgoingDDEdges()) {
                                if (edge2.isDD2()) {
                                    PDGNode anchor2 = edge2.getDstNode();
                                    if (anchor2.equals(anchor)) {
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
    
    private void traverseBackward(PDGNode node) {
        if (nodesInSlice.contains(node)) {
            return;
        }
        
        nodesInSlice.add(node);
        
        for (Dependence edge : node.getIncomingDependeceEdges()) {
            if (edge.isCD()) {
                PDGNode src = edge.getSrcNode();
                traverseBackward(src);
            } else if (edge.isDD2()) {
                PDGNode src = edge.getSrcNode();
                DD dd = (DD)edge;
                if (dd.isFieldAccess()) {
                    if (!breakDDOnFieldAccess(src, node)) {
                        traverseBackward(src);
                    }
                } else if (dd.isParameterIn()) {
                    if (!breakDDOnParameterIn(src, node)) {
                        traverseBackward(src);
                    }
                } else {
                    traverseBackward(src);
                }
            } else if (edge.isCall()) {
                PDGNode src = edge.getSrcNode();
                CFGMethodCall call = (CFGMethodCall)src.getCFGNode();
                JReference var = getVariableReference(call.getPrimary());
                if (var != null) {
                    extract(src, var);
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
        
        CFG cfg = criterion.getPDG().getCFG();
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
            buf.append(" " + jv.getName() + "@" + jv.getASTNode().getStartPosition());
        }
        return buf.toString();
    }
}
