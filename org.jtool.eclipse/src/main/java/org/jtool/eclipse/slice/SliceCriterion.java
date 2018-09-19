/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.slice;

import org.jtool.eclipse.pdg.PDG;
import org.jtool.eclipse.pdg.PDGNode;
import org.jtool.eclipse.cfg.JReference;
import java.util.Set;

/**
 * An object that represents slicing criterion.
 * 
 * @author Katsuhisa Maruyama
 */
public class SliceCriterion {
    
    private PDG pdg;
    private PDGNode node;
    private Set<JReference> variables;
    
    public SliceCriterion(PDG pdg, PDGNode node, Set<JReference> vars) {
        this.pdg = pdg;
        this.node = node;
        this.variables = vars;
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
    
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("Node = " + node.getId());
        buf.append(";");
        buf.append("Variable = " + getVariableNames(variables));
        buf.append("\n");
        return buf.toString();
    }
    
    private String getVariableNames(Set<JReference> vars) {
        if (vars.size() == 0) {
            return "Unspecified";
        }
        StringBuilder buf = new StringBuilder();
        for (JReference var : vars) {
            buf.append(" " + var.getName());
        }
        return buf.toString();
    }
}
