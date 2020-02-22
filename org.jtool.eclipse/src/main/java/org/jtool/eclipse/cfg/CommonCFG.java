/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg;

import org.jtool.eclipse.graph.Graph;
import org.jtool.eclipse.graph.GraphElement;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * An object storing information about a control flow graph (CFG).
 * 
 * @author Katsuhisa Maruyama
 */
public class CommonCFG extends Graph<CFGNode, ControlFlow> {
    
    protected CFGEntry start;
    protected CFGNode end;
    
    public CommonCFG() {
    }
    
    public void setStartNode(CFGEntry node) {
        start = node;
        start.setCFG(this);
    }
    
    public CFGEntry getStartNode() {
        return start;
    }
    
    public void setEndNode(CFGNode node) {
        end = node;
    }
    
    public CFGNode getEndNode() {
        return end;
    }
    
    public long getId() {
        return start.getId();
    }
    
    public String getName() {
        return start.getName();
    }
    
    public String getQualifiedName() {
        return start.getQualifiedName();
    }
    
    public boolean isMethod() {
        return start instanceof CFGMethodEntry;
    }
    
    public boolean isField() {
        return start instanceof CFGFieldEntry;
    }
    
    public boolean isClass() {
        return start instanceof CFGClassEntry;
    }
    
    public boolean isBranch(CFGNode node) {
        return node.isBranch();
    }
    
    public boolean isLoop(CFGNode node) {
        return node.isLoop();
    }
    
    public boolean isJoinNode(CFGNode node) {
        return node.isJoin();
    }
    
    public ControlFlow getFlow(CFGNode src, CFGNode dst) {
        if (src == null || dst == null) {
            return null;
        }
        return getEdges().stream()
                .filter(edge -> src.equals(edge.getSrcNode()) && dst.equals(edge.getDstNode())).findFirst().orElse(null);
    }
    
    public CFGNode getNode(long id) {
        return getNodes().stream().filter(node -> id == node.getId()).findFirst().orElse(null);
    }
    
    public ControlFlow getTrueFlowFrom(CFGNode node) {
        return getEdges().stream().filter(edge -> edge.getSrcNode().equals(node) && edge.isTrue()).findFirst().orElse(null);
    }
    
    public ControlFlow getFalseFlowFrom(CFGNode node) {
        return getEdges().stream().filter(edge -> edge.getSrcNode().equals(node) && edge.isFalse()).findFirst().orElse(null);
    }
    
    public CFGNode getTrueSuccessor(CFGNode node) {
        ControlFlow flow = getFalseFlowFrom(node);
        return (flow != null) ? flow.getDstNode() : null;
    }
    
    public CFGNode getFalseSuccessor(CFGNode node) {
        ControlFlow flow = getFalseFlowFrom(node);
        return (flow != null) ? flow.getDstNode() : null;
    }
    
    public boolean hasTryStatement(){
        return getNodes().stream().anyMatch(node -> node.isTry());
    }
    
    public Set<CFGNode> getCallNodes() {
        return getNodes().stream().filter(node -> node.isMethodCall()).collect(Collectors.toCollection(HashSet::new));
    }
    
    public Set<CFGNode> forwardReachableNodes(CFGNode from, boolean loopbackOk, StopConditionOnReachablePath condition) {
        Set<CFGNode> track = new HashSet<CFGNode>();
        if (from != null) {
            walkForward(from, condition, loopbackOk, track);
        }
        return track;
    }
    
    public Set<CFGNode> backwardReachableNodes(CFGNode from, boolean loopbackOk, StopConditionOnReachablePath condition) {
        Set<CFGNode> track = new HashSet<CFGNode>();
        if (from != null) {
            walkBackward(from, condition, loopbackOk, track);
        }
        return track;
    }
    
    public Set<CFGNode> forwardReachableNodes(CFGNode from, boolean loopbackOk) {
        return forwardReachableNodes(from, loopbackOk, node -> { return false; });
    }
    
    public Set<CFGNode> backwardReachableNodes(CFGNode from, boolean loopbackOk) {
        return backwardReachableNodes(from, loopbackOk, node -> { return false; });
    }
    
    public Set<CFGNode> forwardReachableNodes(CFGNode from, CFGNode to, boolean loopbackOk) {
        Set<CFGNode> track = new HashSet<CFGNode>();
        if (from.equals(to)) {
            track.add(from);
            return track;
        }
        
        Set<CFGNode> ftrack = forwardReachableNodes(from, loopbackOk, node -> node.equals(to));
        track.addAll(ftrack);
        track.add(to);
        return track;
    }
    
    public Set<CFGNode> backwardReachableNodes(CFGNode from, CFGNode to, boolean loopbackOk) {
        Set<CFGNode> track = new HashSet<CFGNode>();
        if (from.equals(to)) {
            track.add(from);
            return track;
        }
        
        Set<CFGNode> btrack = backwardReachableNodes(from, loopbackOk, node -> node.equals(to));
        track.addAll(btrack);
        track.add(to);
        return track;
    }
    
    public Set<CFGNode> reachableNodes(CFGNode from, CFGNode to, boolean loopbackOk) {
        Set<CFGNode> ftrack = forwardReachableNodes(from, to, loopbackOk);
        Set<CFGNode> btrack = backwardReachableNodes(to, from, loopbackOk);
        Set<CFGNode> track = GraphElement.intersection(ftrack, btrack);
        return track;
    }
    
    public Set<CFGNode> postDominator(CFGNode anchor) {
        Set<CFGNode> postDominator = new HashSet<CFGNode>();
        for (CFGNode node : getNodes()) {
            if (!anchor.equals(node)) {
                Set<CFGNode> track = forwardReachableNodes(anchor, node, true);
                if (track.contains(node) && !track.contains(getEndNode())) {
                    postDominator.add(node);
                }
            }
        }
        return postDominator;
    }
    
    public Set<CFGNode> constrainedReachableNodes(CFGNode from, CFGNode to) {
        Set<CFGNode> btrackf = backwardReachableNodes(to, from, true);
        Set<CFGNode> ftrackf = forwardReachableNodes(from, getEndNode(), true);
        Set<CFGNode> fCRP = GraphElement.intersection(btrackf, ftrackf);
        
        Set<CFGNode> ftrackb = forwardReachableNodes(from, to, true);
        Set<CFGNode> btrackb = backwardReachableNodes(to, getStartNode(), true);
        Set<CFGNode> bCRP = GraphElement.intersection(ftrackb, btrackb);
        
        Set<CFGNode> CRP = GraphElement.union(fCRP, bCRP);
        return CRP;
    }
    
    private void walkForward(CFGNode node, StopConditionOnReachablePath condition, boolean loopbackOk, Set<CFGNode> track) {
        if (condition.isStop(node)) {
            return;
        }
        track.add(node);
        
        for (ControlFlow flow : node.getOutgoingFlows()) {
            if (loopbackOk || !flow.isLoopBack()) {
                CFGNode succ = flow.getDstNode();
                if (!track.contains(succ)) {
                    walkForward(succ, condition, loopbackOk, track);
                }
            }
        }
    }
    
    private void walkBackward(CFGNode node, StopConditionOnReachablePath condition, boolean loopbackOk, Set<CFGNode> track) {
        if (condition.isStop(node)) {
            return;
        }
        track.add(node);
        
        for (ControlFlow flow : node.getIncomingFlows()) {
            if (loopbackOk || !flow.isLoopBack()) {
                CFGNode pred = flow.getSrcNode();
                if (!track.contains(pred)) {
                    walkBackward(pred, condition, loopbackOk, track);
                }
            }
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        return (obj instanceof CommonCFG) ? equals((CommonCFG)obj) : false;
    }
    
    public boolean equals(CommonCFG cfg) {
        return cfg != null && (this == cfg || getQualifiedName().equals(cfg.getQualifiedName()));
    }
    
    @Override
    public int hashCode() {
        return getQualifiedName().hashCode();
    }
    
    public void print() {
        System.out.println(toString());
    }
    
    @Override
    protected String getNodeInfo() {
        StringBuilder buf = new StringBuilder();
        for (CFGNode node : CFGNode.sortCFGNode(getNodes())) {
            buf.append(node.toString());
            buf.append("\n");
        }
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
