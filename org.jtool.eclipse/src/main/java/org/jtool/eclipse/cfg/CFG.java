/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg;

import org.jtool.eclipse.cfg.builder.BasicBlockBuilder;
import org.jtool.eclipse.graph.Graph;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * An object storing information about a control flow graph (CFG).
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
    
    public Set<CFGNode> getForwardReachableNodes(CFGNode from, CFGNode to) {
        Set<CFGNode> track = new HashSet<CFGNode>();
        walkForward(from, to, true, track);
        return track;
    }
    
    public Set<CFGNode> getForwardReachableNodesWithoutLoopback(CFGNode from, CFGNode to) {
        Set<CFGNode> track = new HashSet<CFGNode>();
        walkForward(from, to, false, track);
        return track;
    }
    
    public Set<CFGNode> getBackwardReachableNodes(CFGNode from, CFGNode to) {
        Set<CFGNode> track = new HashSet<CFGNode>();
        walkBackward(from, to, true, track);
        return track;
    }
    
    public Set<CFGNode> getBackwardReachableNodesWithoutLoopback(CFGNode from, CFGNode to) {
        Set<CFGNode> track = new HashSet<CFGNode>();
        walkBackward(from, to, false, track);
        return track;
    }
    
    private void walkForward(CFGNode from, CFGNode to, boolean loopbackOk, Set<CFGNode> track) {
        if (from == null) {
            return;
        }
        if (from.equals(to) && !track.isEmpty()) {
            track.add(from);
            return;
        }
        track.add(from);
        
        for (ControlFlow flow : from.getOutgoingFlows()) {
            if (loopbackOk || !flow.isLoopBack()) {
                CFGNode succ = flow.getDstNode();
                if (!track.contains(succ)) {
                    walkForward(succ, to, loopbackOk, track);
                }
            }
        }
    }
    
    private void walkBackward(CFGNode to, CFGNode from, boolean loopbackOk, Set<CFGNode> track) {
        if (to == null) {
            return;
        }
        if (to.equals(from) && !track.isEmpty()) {
            track.add(to);
            return;
        }
        track.add(to);
        
        for (ControlFlow flow : to.getIncomingFlows()) {
            if (loopbackOk || !flow.isLoopBack()) {
                CFGNode pred = flow.getSrcNode();
                if (!track.contains(pred)) {
                    walkBackward(pred, from, loopbackOk, track);
                }
            }
        }
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
