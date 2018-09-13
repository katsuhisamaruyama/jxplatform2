/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.slice;

import org.jtool.eclipse.cfg.CFG;
import org.jtool.eclipse.cfg.CFGNode;
import org.jtool.eclipse.cfg.CFGStore;
import org.jtool.eclipse.cfg.JReference;
import org.jtool.eclipse.pdg.DD;
import org.jtool.eclipse.pdg.Dependence;
import org.jtool.eclipse.pdg.PDG;
import org.jtool.eclipse.pdg.PDGNode;
import org.jtool.eclipse.pdg.PDGStatement;
import org.eclipse.jdt.core.dom.ASTNode;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;

/**
 * An object storing information about a program slice.
 * 
 * @author Katsuhisa Maruyama
 */
public class Slice extends PDG {
    
    private PDG pdg;
    private PDGStatement criterionNode;
    private List<JReference> criterionVariables;
    
    public Slice(PDG pdg, PDGStatement node, JReference jv) {
        this.pdg = pdg;
        criterionNode = node;
        criterionVariables = new ArrayList<JReference>();
        criterionVariables.add(jv);
        
        extract(node, jv);
    }
    
    public Slice(PDG pdg, PDGStatement node, List<JReference> jvs) {
        this.pdg = pdg;
        criterionNode = node;
        criterionVariables = jvs;
        
        for (JReference jv : jvs) {
            extract(node, jv);
        }
    }
    
    private void extract(PDGStatement node, JReference jv) {
        if (jv.isFieldAccess()) {
            traverseOnCFG(node, jv);
        } else {
            traverseOnPDG(node, jv);
        }
    }
    
    public ASTNode getSliceOnAST() {
        CFG cfg = pdg.getCFG();
        StatementExtractor extractor = new StatementExtractor(this);
        ASTNode methodDeclNode = cfg.getStartNode().getASTNode();
        methodDeclNode.accept(extractor);
        return methodDeclNode;
    }
    
    public PDGStatement getCriterionNode() {
        return criterionNode;
    }
    
    public List<JReference> getCriterionVariables() {
        return criterionVariables;
    }
    
    private void traverseOnCFG(PDGStatement node, JReference jv) {
        String fqn = jv.getEnclosingMethodName();
        CFG cfg = CFGStore.getInstance().getCFG(fqn);
        Set<CFGNode> cfgNodes = cfg.backwardReachableNodes(node.getCFGNode(), cfg.getStartNode(), true);
        
        for (CFGNode cfgNode : cfgNodes) {
            PDGNode pdgNode = cfgNode.getPDGNode();
            if (pdgNode instanceof PDGStatement) {
                PDGStatement candidate = (PDGStatement)pdgNode;
                if (candidate.definesVariable(jv) || candidate.usesVariable(jv)) {
                    add(candidate);
                }
            }
        }
    }
    
    private void traverseOnPDG(PDGStatement node, JReference jv) {
        if (node.definesVariable(jv)) {
            traverseBackward(node);
        } else if (node.usesVariable(jv)) {
            add(node);
            for (PDGStatement defnode : findDefNode(node, jv)) {
                traverseBackward(defnode);
            }
        }
    }
    
    private Set<PDGStatement> findDefNode(PDGStatement anchor, JReference jvar) {
        Set<PDGStatement> defs = new HashSet<PDGStatement>();
        
        for (DD edge : anchor.getIncomingDDEdges()) {
            if (jvar.equals(edge.getVariable())) {
                PDGStatement node = (PDGStatement)edge.getSrcNode();
                defs.add(node);
            }
        }
        return defs;
    }
    
    private void traverseBackward(PDGStatement anchor) {
        add(anchor);
        for (Dependence edge : anchor.getIncomingDependeceEdges()) {
            add(edge);
            PDGStatement node = (PDGStatement)edge.getSrcNode();
            if (!getNodes().contains(node)) {
                traverseBackward(node);
            }
        }
    }
    
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("----- Slice (from here) -----\n");
        buf.append("Node = " + criterionNode.getId() + "; Variable = " + getVariableNames(criterionVariables));
        buf.append("\n");
        buf.append(getNodeInfo());
        buf.append(getEdgeInfo());
        buf.append("----- Slice (to here) -----\n");
        return buf.toString();
    }
    
    private String getVariableNames(List<JReference> jvs) {
        StringBuilder buf = new StringBuilder();
        for (JReference jv : jvs) {
            buf.append(" " + jv.getName());
        }
        return buf.toString();
    }
}
