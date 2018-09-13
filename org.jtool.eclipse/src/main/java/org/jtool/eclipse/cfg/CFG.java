/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg;

import org.jtool.eclipse.cfg.builder.BasicBlockBuilder;
import org.jtool.eclipse.graph.Graph;
import org.jtool.eclipse.graph.GraphElement;
import org.jtool.eclipse.pdg.Dependence;

import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * An object storing information about a control flow graph (CFG).
 * 
 * @author Katsuhisa Maruyama
 */
public class CFG extends Graph<CFGNode, ControlFlow> {
    
    protected CFGEntry start;
    protected CFGNode end;
    
    private List<BasicBlock> basicBlocks = new ArrayList<BasicBlock>();
    
    public CFG() {
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
    
    public void append(CFG cfg) {
        for (CFGNode node : cfg.getNodes()) {
            add(node);
        }
        for (ControlFlow edge : cfg.getEdges()) {
            add(edge);
        }
    }
    
    public void add(BasicBlock block) {
        basicBlocks.add(block);
    }
    
    public List<BasicBlock> getBasicBlocks() {
        return basicBlocks;
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
        if (src != null && dst != null) {
            for (ControlFlow edge : getEdges()) {
                if (src.equals(edge.getSrcNode()) && dst.equals(edge.getDstNode())) {
                    return edge;
                }
            }
        }
        return null;
    }
    
    public CFGNode getNode(long id) {
        for (CFGNode node : getNodes()) {
            if (id == node.getId()) {
                return node;
            }
        }
        return null;
    }
    
    public ControlFlow getTrueFlowFrom(CFGNode node) {
        for (ControlFlow edge : getEdges()) {
            if (edge.getSrcNode().equals(node) && edge.isTrue()) {
                return edge;
            }
        }
        return null;
    }
    
    public ControlFlow getFalseFlowFrom(CFGNode node) {
        for (ControlFlow edge : getEdges()) {
            if (edge.getSrcNode().equals(node) && edge.isFalse()) {
                return edge;
            }
        }
        return null;
    }
    
    public CFGNode getTrueSuccessor(CFGNode node) {
        ControlFlow flow = getTrueFlowFrom(node);
        if (flow != null) {
            return flow.getDstNode();
        }
        return null;
    }
    
    public CFGNode getFalseSuccessor(CFGNode node) {
        ControlFlow flow = getFalseFlowFrom(node);
        if (flow != null) {
            return flow.getDstNode();
        }
        return null;
    }
    
    public boolean hasTryStatement(){
        for (CFGNode node : getNodes()) {
            if (node.isTry()) {
                return true;
            }
        }
        return false;
    }
    
    public Set<CFGNode> getCallNodes() {
        Set<CFGNode> set = new HashSet<CFGNode>();
        for (CFGNode node : getNodes()) {
            if (node.isMethodCall()) {
                set.add(node);
            }
        }
        return set;
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
        return forwardReachableNodes(from, loopbackOk, new StopConditionOnReachablePath() {
            @Override
            public boolean isStop(CFGNode node) {
                return false;
            }
        });
    }
    
    public Set<CFGNode> backwardReachableNodes(CFGNode from, boolean loopbackOk) {
        return backwardReachableNodes(from, loopbackOk, new StopConditionOnReachablePath() {
            @Override
            public boolean isStop(CFGNode node) {
                return false;
            }
        });
    }
    
    public Set<CFGNode> forwardReachableNodes(CFGNode from, final CFGNode to, boolean loopbackOk) {
        Set<CFGNode> track = new HashSet<CFGNode>();
        if (from.equals(to)) {
            track.add(from);
            return track;
        }
        
        Set<CFGNode> ftrack = forwardReachableNodes(from, loopbackOk, new StopConditionOnReachablePath() {
            @Override
            public boolean isStop(CFGNode node) {
                return node.equals(to);
            }
        });
        track.addAll(ftrack);
        track.add(to);
        return track;
    }
    
    public Set<CFGNode> backwardReachableNodes(CFGNode from, final CFGNode to, boolean loopbackOk) {
        Set<CFGNode> track = new HashSet<CFGNode>();
        if (from.equals(to)) {
            track.add(from);
            return track;
        }
        
        Set<CFGNode> btrack = backwardReachableNodes(from, loopbackOk, new StopConditionOnReachablePath() {
            @Override
            public boolean isStop(CFGNode node) {
                return node.equals(to);
            }
        });
        track.addAll(btrack);
        track.add(to);
        return track;
    }
    
    public Set<CFGNode> reachableNodes(CFGNode from, final CFGNode to, boolean loopbackOk) {
        Set<CFGNode> ftrack = forwardReachableNodes(from, to, loopbackOk);
        Set<CFGNode> btrack = backwardReachableNodes(to, from, loopbackOk);
        Set<CFGNode> track = GraphElement.intersection(ftrack, btrack);
        return track;
    }
    
    public Set<CFGNode> postDominator(CFGNode anchor) {
        Set<CFGNode> postDominator = new HashSet<CFGNode>();
        Set<CFGNode> track = new HashSet<CFGNode>();
        for (CFGNode node : getNodes()) {
            if (!anchor.equals(node)) {
                track.clear();
                track = forwardReachableNodes(anchor, node, true);
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
        if (obj instanceof CFG) {
            return equals((CFG)obj);
        }
        return false;
    }
    
    public boolean equals(CFG cfg) {
        if (cfg == null) {
            return false;
        }
        return this == cfg || getQualifiedName().equals(cfg.getQualifiedName());
    }
    
    @Override
    public int hashCode() {
        return getQualifiedName().hashCode();
    }
    
    @Override
    public CFG clone() {
        CFG cloneCFG = new CFG();
        HashMap<Long, Long> idmap = new HashMap<Long, Long>();
        
        for (CFGNode node : getNodes()) {
            CFGNode cloneNode = node.clone();
            cloneCFG.add(cloneNode);
            idmap.put(node.getId(), cloneNode.getId());
            
            if (node.isEntry()) {
                cloneCFG.setStartNode((CFGEntry)cloneNode);
            } else if (node.isExit()) {
                cloneCFG.setEndNode((CFGExit)cloneNode);
            }
        }
        
        for (ControlFlow edge : getEdges()) {
            CFGNode src = edge.getSrcNode();
            CFGNode dst = edge.getDstNode();
            long srcId = idmap.get(src.getId());
            long dstId = idmap.get(dst.getId());
            CFGNode cloneSrc = cloneCFG.getNode(srcId);
            CFGNode cloneDst = cloneCFG.getNode(dstId);
            
            ControlFlow cloneEdge = edge.clone();
            cloneEdge.setSrcNode(cloneSrc);
            cloneEdge.setDstNode(cloneDst);
            cloneCFG.add(edge);
        }
        BasicBlockBuilder.create(this);
        return cloneCFG;
    }
    
    public void print() {
        System.out.println(toString());
    }
    
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("----- CFG of " + getName() + "-----\n");
        buf.append(getNodeInfo()); 
        buf.append(getEdgeInfo());
        buf.append("-----------------------------------\n");
        return buf.toString();
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
