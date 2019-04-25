/*
 *  Copyright 2019
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.slice;

import org.jtool.eclipse.pdg.PDG;
import org.jtool.eclipse.pdg.PDGNode;
import org.jtool.eclipse.pdg.PDGStatement;
import org.jtool.eclipse.cfg.JReference;
import org.eclipse.jdt.core.dom.ASTNode;
import java.util.Set;
import java.util.HashSet;

/**
 * An object that represents a slicing criterion.
 * 
 * @author Katsuhisa Maruyama
 */
public class SliceCriterion {
    
    private PDG pdg;
    private PDGNode node;
    private Set<JReference> variables = new HashSet<JReference>();
    
    public SliceCriterion(PDG pdg, PDGNode node, JReference var) {
        this.pdg = pdg;
        this.node = node;
        variables.add(var);
    }
    
    public SliceCriterion(PDG pdg, PDGNode node, Set<JReference> vars) {
        this.pdg = pdg;
        this.node = node;
        for (JReference var : vars) {
            if (var.isVariableAccess()) {
                variables.add(var);
            }
        }
    }
    
    public PDG getPDG() {
        return pdg;
    }
    
    public PDGNode getNode() {
        return node;
    }
    
    public Set<JReference> getnVariables() {
        return variables;
    }
    
    public static SliceCriterion find(PDG pdg, String code, int lineNumber, int offset) {
        String[] lines = code.split(System.getProperty("line.separator"));
        int index = 0;
        for (int line = 0; line < lineNumber - 1; line++) {
            index = index + lines[line].length() + 1;
        }
        index = index + offset;
        return find(pdg, index);
    }
    
    public static SliceCriterion find(PDG pdg, ASTNode node) {
        return find(pdg, node.getStartPosition());
    }
    
    public static SliceCriterion find(PDG pdg, int offset) {
        for (PDGNode node : pdg.getNodes()) {
            if (node.isStatement()) {
                PDGStatement stnode = (PDGStatement)node;
                for (JReference def : stnode.getDefVariables()) {
                    if (def.isVisible() && offset == def.getASTNode().getStartPosition()) {
                        return new SliceCriterion(pdg, stnode, def);
                    }
                }
                for (JReference use : stnode.getUseVariables()) {
                    if (use.isVisible() && offset == use.getASTNode().getStartPosition()) {
                        return new SliceCriterion(pdg, stnode, use);
                    }
                }
            }
        }
        return null;
    }
    
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("Node = " + node.getId());
        buf.append(";");
        buf.append(" Variable =" + getVariableNames(variables));
        buf.append("\n");
        return buf.toString();
    }
    
    private String getVariableNames(Set<JReference> vars) {
        if (vars.size() == 0) {
            return "Unspecified";
        }
        StringBuilder buf = new StringBuilder();
        for (JReference var : vars) {
            buf.append(" " + var.getName() + "@" + var.getASTNode().getStartPosition());
        }
        return buf.toString();
    }
}