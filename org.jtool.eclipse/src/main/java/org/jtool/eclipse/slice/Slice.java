/*
 *  Copyright 2019-2020
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.slice;

import org.jtool.eclipse.pdg.PDG;
import org.jtool.eclipse.pdg.CommonPDG;
import org.jtool.eclipse.pdg.PDGNode;
import org.jtool.eclipse.pdg.PDGStatement;
import org.jtool.eclipse.pdg.SDG;
import org.jtool.eclipse.pdg.Dependence;
import org.jtool.eclipse.pdg.DD;
import org.jtool.eclipse.pdg.CD;
import org.jtool.eclipse.pdg.ClDG;
import org.jtool.eclipse.cfg.CommonCFG;
import org.jtool.eclipse.cfg.CFGNode;
import org.jtool.eclipse.cfg.CFGStatement;
import org.jtool.eclipse.cfg.CFGMethodCall;
import org.jtool.eclipse.cfg.JReference;
import org.jtool.eclipse.cfg.StopConditionOnReachablePath;
import org.jtool.eclipse.graph.GraphNode;
import java.util.Set;
import java.util.HashSet;

/**
 * An object storing information about a program slice.
 * 
 * @author Katsuhisa Maruyama
 */
public class Slice {
    
    protected SliceCriterion criterion;
    
    private PDG pdgForTargetMethod;
    private Set<CFGNode> allNodesInTargetMethod;
    private Set<CFGNode> reachableNodesToCriterion;
    
    private Set<PDGNode> callNodes = new HashSet<PDGNode>();
    
    private Set<PDGNode> nodesInSlice = new HashSet<PDGNode>();
    
    public Slice(SliceCriterion criterion) {
        this.criterion = criterion;
        
        pdgForTargetMethod = getPDGForMethod();
        allNodesInTargetMethod = pdgForTargetMethod.getCFG().getNodes();
        reachableNodesToCriterion = criterion.getPDG().getCFG().backwardReachableNodes(criterion.getNode().getCFGNode(), true);
        
        criterion.getnVariables().forEach(var -> extract(criterion.getNode(), var));
    }
    
    private PDG getPDGForMethod() {
        CommonPDG pdg = criterion.getPDG();
        if (pdg.isPDG()) {
            return (PDG)pdg;
        } else if (pdg.isClDG()) {
            for (PDG g : ((ClDG)pdg).getPDGs()) {
                if (g.contains(criterion.getNode())) {
                    return g;
                }
            }
        } else {
            for (PDG g : ((SDG)pdg).getPDGs()) {
                if (g.contains(criterion.getNode())) {
                    return g;
                }
            }
        }
        return null;
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
            node.getIncomingCDEdges().stream()
                                     .flatMap(call -> call.getSrcNode().getIncomingCDEdges().stream())
                                     .filter(edge -> edge.isTrue() || edge.isFalse() || edge.isFallThrough())
                                     .forEach(edge -> traverseBackward(edge.getSrcNode()));
        } else {
            node.getIncomingCDEdges().stream()
                                     .filter(edge -> edge.isTrue() || edge.isFalse() || edge.isFallThrough())
                                     .forEach(edge -> traverseBackward(edge.getSrcNode()));
        }
        
        findStartNode(node, jv).stream()
                               .map(start -> (CD)start.getIncomingCDEdges())
                               .forEach(edge -> traverseBackward(edge.getSrcNode()));
        
        eliminateReceiverNodes(nodesInSlice);
    }
    
    private void eliminateReceiverNodes(Set<PDGNode> nodes) {
        Set<PDGNode> receiverNodes = new HashSet<PDGNode>();
        for (PDGNode node : nodes) {
            if (node.getCFGNode().isMethodCallReceiver()) {
                receiverNodes.add(node);
            }
        }
        
        for (PDGNode node : receiverNodes) {
            PDGNode callNode = this.getDominantNode(node);
            if (!nodes.contains(callNode)) {
                nodes.remove(node);
            }
        }
    }
    
    private void traverseBackward(PDGNode node) {
        if (nodesInSlice.contains(node)) {
            return;
        }
        
        nodesInSlice.add(node);
        
        if (node.getCFGNode().isCatch()) {
            for (Dependence edge : node.getIncomingDependeceEdges()) {
                PDGNode src = edge.getSrcNode();
                if (src.getCFGNode().isMethodCall()) {
                    callNodes.add(src);
                }
            }
        }
        
        if (node.getCFGNode().isActualOut()) {
            PDGNode callNode = getDominantNode(node);
            callNodes.add(callNode);
        }
        
        for (Dependence edge : node.getIncomingDependeceEdges()) {
            PDGNode src = edge.getSrcNode();
            if (edge.isFieldAccess() && src.getCFGNode().isMethodCall()) {
                DD dd = (DD)edge;
                CFGMethodCall callNode = (CFGMethodCall)src.getCFGNode();
                if (!callNode.getMethodCall().getQualifiedName().equals(pdgForTargetMethod.getQualifiedName()) &&
                    canTraverseMethodCall(src, dd.getVariable())) {
                    callNodes.add(src);
                }
            }
        }
        
        for (Dependence edge : node.getIncomingDependeceEdges()) {
            PDGNode src = edge.getSrcNode();
            if (edge.isCD()) {
                if (src.getCFGNode().isMethodCall()) {
                    if (callNodes.contains(src)) {
                        traverseBackward(src);
                    }
                } else {
                    traverseBackward(src);
                }
                    
            } else if (edge.isLIDD() || edge.isLCDD()) {
                if (src.getCFGNode().isMethodCall() && node.getCFGNode().isMethodCallReceiver()) {
                    PDGNode callNode = getDominantNode(src);
                    if (callNodes.contains(callNode)) {
                        traverseBackward(src);
                    }
                } else {
                    traverseBackward(src);
                }
                
            } else if (edge.isFieldAccess()) {
                DD dd = (DD)edge;
                if (src.getCFGNode().isMethodCall()) {
                    if (callNodes.contains(src)) {
                        traverseBackward(src);
                    }
                } else if (src.getCFGNode().isMethodCallReceiver()) {
                    if (callNodes.contains(src)) {
                        traverseBackward(src);
                    }
                } else {
                    PDGNode domNode = getDominantNode(src);
                    if (domNode.getCFGNode().isFieldEntry()) {
                        traverseBackward(src);
                    } else {
                        callNodes.addAll(getTraversableMethodCalls(src, dd.getVariable()));
                        for (PDGNode callnode : getMethodCalls(domNode)) {
                            if (callNodes.contains(callnode)) {
                                traverseBackward(src);
                            }
                        }
                    }
                }
                
            } else if (edge.isParameterIn()) {
                PDGNode callnode = getDominantNode(src);
                if (callNodes.contains(callnode)) {
                    traverseBackward(src);
                }
                
            } else if (edge.isParameterOut()) {
                traverseBackward(src);
                
            } else if (edge.isSummary()) {
                PDGNode ainOn = getDominantNode(src);
                PDGNode aoutOn = getDominantNode(node);
                if (ainOn.equals(aoutOn)) {
                    traverseBackward(src);
                }
                
            } else if (edge.isCall()) {
                if (callNodes.contains(src)) {
                    for (Dependence e : src.getOutgoingDependeceEdges()) {
                        if (e.isExceptionCatch()) {
                            traverseBackward(e.getDstNode());
                        }
                    }
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
    
    private PDGNode getDominantNode(PDGNode node) {
        for (CD edge : node.getIncomingCDEdges()) {
            if (edge.isTrue() || edge.isFalse()) {
                return edge.getSrcNode();
            }
        }
        return null;
    }
    
    private PDGNode getMethodEntry(PDGNode node) {
        while (node != null &&
                !node.getCFGNode().isMethodEntry() &&
                !node.getCFGNode().isConstructorEntry() &&
                !node.getCFGNode().isInitializerEntry()) {
            node = getDominantNode(node);
        }
        return node;
    }
    
    private Set<PDGNode> getMethodCalls(PDGNode node) {
        Set<PDGNode> nodes = new HashSet<PDGNode>();
        for (Dependence edge : node.getIncomingDependeceEdges()) {
            if (edge.isCall()) {
                nodes.add(edge.getSrcNode());
            }
        }
        return nodes;
    }
    
    private Set<PDGNode> getTraversableMethodCalls(PDGNode node, JReference jv) {
        Set<PDGNode> nodes = new HashSet<PDGNode>();
        PDGNode methodEntry = getMethodEntry(node);
        if (methodEntry == null) {
            return nodes;
        }
        
        for (PDGNode callnode : getMethodCalls(methodEntry)) {
            if (canTraverseMethodCall(callnode, jv)) {
                nodes.add(callnode);
            }
        }
        return nodes;
    }
    
    private boolean canTraverseMethodCall(PDGNode callNode, JReference jv) {
        if (!reachable(callNode)) {
            return false;
        }
        
        for (DD edge : callNode.getOutgoingDDEdges()) {
            if (edge.isOutput() && jv.getQualifiedName().equals(edge.getVariable().getQualifiedName()) && reachable(edge.getDstNode())) {
                return false;
            }
        }
        return true;
    }
    
    private boolean reachable(PDGNode node) {
        return !allNodesInTargetMethod.contains(node.getCFGNode()) || reachableNodesToCriterion.contains(node.getCFGNode());
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
        GraphNode.sortGraphNode(nodesInSlice).forEach(node -> {
            PDGNode pdgnode = (PDGNode)node;
            buf.append(node.toString() + "@" + pdgnode.getCFGNode().getASTNode().getStartPosition());
            buf.append("\n");
        });
        return buf.toString();
    }
    
    private String getVariableNames(Set<JReference> jvs) {
        StringBuilder buf = new StringBuilder();
        jvs.forEach(jv -> buf.append(" " + jv.getName() + "@" + jv.getASTNode().getStartPosition()));
        return buf.toString();
    }
}
