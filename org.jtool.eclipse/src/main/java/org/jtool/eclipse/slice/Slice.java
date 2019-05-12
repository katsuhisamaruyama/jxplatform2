/*
 *  Copyright 2019
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.slice;

import org.jtool.eclipse.pdg.CommonPDG;
import org.jtool.eclipse.pdg.PDGNode;
import org.jtool.eclipse.pdg.PDGStatement;
import org.jtool.eclipse.pdg.Dependence;
import org.jtool.eclipse.pdg.DD;
import org.jtool.eclipse.pdg.CD;
import org.jtool.eclipse.cfg.CommonCFG;
import org.jtool.eclipse.cfg.CFGNode;
import org.jtool.eclipse.cfg.JReference;
import org.jtool.eclipse.cfg.StopConditionOnReachablePath;
import org.jtool.eclipse.cfg.CFGStatement;
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
    private Set<CFGNode> reachablePathToCriterion;
    
    private Set<PDGNode> nodesInSlice = new HashSet<PDGNode>();
    
    private Set<PDGNode> visitCallNodes = new HashSet<PDGNode>();
    
    public Slice(SliceCriterion criterion) {
        this.criterion = criterion;
        CommonCFG cfg = criterion.getPDG().getCFG();
        reachablePathToCriterion = cfg.backwardReachableNodes(criterion.getNode().getCFGNode(), true);
        
        for (JReference var : criterion.getnVariables()) {
            extract(criterion.getNode(), var);
        }
    }
    
    public CommonPDG getPDG() {
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
        
        if (node.getCFGNode().isActual()) {
            for (CD call : node.getIncomingCDEdges()) {
                for (CD edge : call.getSrcNode().getIncomingCDEdges()) {
                    if (edge.isTrue() || edge.isFalse() || edge.isFallThrough()) {
                        traverseBackward(edge.getSrcNode());
                    }
                }
            }
        } else {
            for (CD edge : node.getIncomingCDEdges()) {
                if (edge.isTrue() || edge.isFalse() || edge.isFallThrough()) {
                    traverseBackward(edge.getSrcNode());
                }
            }
        }
        
        for (PDGNode start : findStartNode(node, jv)) {
            for (CD edge : start.getIncomingCDEdges()) {
                traverseBackward(edge.getSrcNode());
            }
        }
    }
    
    private PDGNode getDominantNode(PDGNode node) {
        for (CD edge : node.getIncomingCDEdges()) {
            if (edge.isTrue() || edge.isFalse()) {
                return edge.getSrcNode();
            }
        }
        return null;
    }
    
    private PDGNode getMethodEntry(PDGNode node) {
        while (node != null && !node.getCFGNode().isMethodEntry()) {
            node = getDominantNode(node);
        }
        return node;
    }
    
    private Set<PDGNode> getMethodCall(PDGNode node) {
        Set<PDGNode> nodes = new HashSet<PDGNode>();
        for (Dependence edge : node.getIncomingDependeceEdges()) {
            if (edge.isCall()) {
                nodes.add(edge.getSrcNode());
            }
        }
        return nodes;
    }
    
    private PDGNode getOutgoingOD(PDGNode node, JReference jv) {
        for (DD edge : node.getOutgoingDDEdges()) {
            if (edge.isOutput() && jv.equals(edge.getVariable())) {
                return edge.getDstNode();
            }
        }
        return null;
    }
    
    private Set<PDGNode> getCallNodes(PDGNode node, JReference jv) {
        Set<PDGNode> nodes = new HashSet<PDGNode>();
        PDGNode methodEntry = getMethodEntry(node);
        if (methodEntry == null) {
            return nodes;
        }
        
        for (PDGNode callnode : getMethodCall(methodEntry)) {
            if (getOutgoingOD(callnode, jv) == null && reachablePathToCriterion.contains(callnode.getCFGNode())) {
                nodes.add(callnode);
            }
        }
        return nodes;
    }
    
    private void traverseBackward(PDGNode node) {
        if (nodesInSlice.contains(node)) {
            return;
        }
        nodesInSlice.add(node);
        
        for (Dependence edge : node.getIncomingDependeceEdges()) {
            PDGNode src = edge.getSrcNode();
            
            if (edge.isCD()) {
                traverseBackward(src);
                
            } else if (edge.isLIDD() || edge.isLCDD()) {
                traverseBackward(src);
                
            } else if (edge.isFieldAccess()) {
                if (!src.getCFGNode().isMethodCall()) {
                    PDGNode entry = getDominantNode(src);
                    if (entry.getCFGNode().isFieldEntry()) {
                        traverseBackward(src);
                    } else {
                        DD fedge = (DD)edge;
                        visitCallNodes.addAll(getCallNodes(src, fedge.getVariable()));
                        for (PDGNode callnode : getMethodCall(entry)) {
                            if (visitCallNodes.contains(callnode)) {
                                traverseBackward(src);
                            }
                        }
                    }
                }
                
            } else if (edge.isParameterIn()) {
                if (visitCallNodes.contains(getDominantNode(src))) {
                    traverseBackward(src);
                }
            } else if (edge.isParameterOut()) {
                visitCallNodes.add(getDominantNode(node));
                traverseBackward(src);
                
            } else if (edge.isSummary()) {
                PDGNode ainOn = getDominantNode(src);
                PDGNode aoutOn = getDominantNode(node);
                if (ainOn.equals(aoutOn)) {
                    traverseBackward(src);
                }
            }
        }
    }
    
    private Set<PDGNode> findStartNode(PDGNode node, JReference jv) {
        Set<PDGNode> pdgnodes = new HashSet<PDGNode>();
        if (node.isStatement()) {
            PDGStatement pdgnode = (PDGStatement)node;
            if (pdgnode.definesVariable(jv)) {
                pdgnodes.add(node);
                return pdgnodes;
            }
        }
        
        for (DD edge : node.getIncomingDDEdges()) {
            if (edge.getVariable().equals(jv)) {
                pdgnodes.add(edge.getSrcNode());
            }
        }
        if (pdgnodes.size() > 0) {
            return pdgnodes;
        }
        
        CommonCFG cfg = criterion.getPDG().getCFG();
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
    
    public void print() {
        System.out.println(toString());
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
            PDGNode pdgnode = (PDGNode)node;
            buf.append(node.toString() + "@" + pdgnode.getCFGNode().getASTNode().getStartPosition());
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
