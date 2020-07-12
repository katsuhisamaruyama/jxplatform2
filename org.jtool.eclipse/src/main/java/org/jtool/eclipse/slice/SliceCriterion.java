/*
 *  Copyright 2019-2020
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.slice;

import org.jtool.eclipse.pdg.CommonPDG;
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
    
    private CommonPDG pdg;
    private PDGNode node;
    private Set<JReference> variables = new HashSet<>();
    
    public SliceCriterion(CommonPDG pdg, PDGNode node, JReference var) {
        this.pdg = pdg;
        this.node = node;
        variables.add(var);
    }
    
    public SliceCriterion(CommonPDG pdg, PDGNode node, Set<JReference> vars) {
        this.pdg = pdg;
        this.node = node;
        vars.stream().filter(var -> var.isVariableAccess()).forEach(var -> variables.add(var));
    }
    
    public CommonPDG getPDG() {
        return pdg;
    }
    
    public PDGNode getNode() {
        return node;
    }
    
    public Set<JReference> getnVariables() {
        return variables;
    }
    
    public static SliceCriterion find(CommonPDG pdg, String code, int lineNumber, int columnNumber) {
        String[] lines = code.split(System.getProperty("line.separator"));
        int position = 0;
        for (int line = 0; line < lineNumber - 1; line++) {
            position = position + lines[line].length() + 1;
        }
        position = position + columnNumber;
        return find(pdg, position);
    }
    
    public static SliceCriterion find(CommonPDG pdg, ASTNode node) {
        return find(pdg, node.getStartPosition());
    }
    
    public static SliceCriterion find(CommonPDG pdg, int position) {
        for (PDGNode node : pdg.getNodes()) {
            if (node.isStatement() && !node.getCFGNode().isActualOut()) {
                PDGStatement stnode = (PDGStatement)node;
                for (JReference def : stnode.getDefVariables()) {
                    if (def.isVisible() && position == def.getStartPosition()) {
                        return new SliceCriterion(pdg, stnode, def);
                    }
                }
                for (JReference use : stnode.getUseVariables()) {
                    if (use.isVisible() && position == use.getStartPosition()) {
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
        vars.forEach(var -> buf.append(" " + var.getName() + "@" + var.getASTNode().getStartPosition()));
        return buf.toString();
    }
}
