/*
 *  Copyright 2018-2020
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg;

import org.eclipse.jdt.core.dom.ASTNode;
import java.util.List;
import java.util.ArrayList;

/**
 * A statement node of a CFG, which stores defined and used variables.
 * 
 * @author Katsuhisa Maruyama
 */
public class CFGStatement extends CFGNode {
    
    private List<JReference> defs = new ArrayList<>();
    private List<JReference> uses = new ArrayList<>();
    
    public CFGStatement(ASTNode node, Kind kind) {
        super(node, kind);
    }
    
    public boolean addDefVariable(JReference jvar) {
        return (jvar!= null && !defineVariable(jvar)) ? defs.add(jvar) : false;
    }
    
    public boolean addUseVariable(JReference jvar) {
        return (jvar != null && !useVariable(jvar)) ? uses.add(jvar) : false;
    }
    
    public void addDefVariables(List<JReference> jvars) {
        jvars.forEach(jvar -> addDefVariable(jvar));
    }
    
    public void addUseVariables(List<JReference> jvars) {
        jvars.forEach(jvar -> addUseVariable(jvar));
    }
    
    public boolean removeDefVariable(JReference jvar) {
        return defs.remove(jvar);
    }
    
    public boolean removeUseVariable(JReference jvar) {
        return uses.remove(jvar);
    }
    
    public void clearDefVariables() {
        defs.clear();
    }
    
    public void clearUseVariables() {
        uses.clear();
    }
    
    public void setDefVariables(List<JReference> jvars) {
        defs = jvars;
    }
    
    public void setUseVariables(List<JReference> jvars) {
        uses = jvars;
    }
    
    public void setDefVariable(JReference jvar) {
        clearDefVariables();
        addDefVariable(jvar);
    }
    
    public void setUseVariable(JReference jvar) {
        clearUseVariables();
        addUseVariable(jvar);
    }
    
    public List<JReference> getDefVariables() {
        return defs;
    }
    
    public List<JReference> getUseVariables() {
        return uses;
    }
    
    public boolean defineVariable(JReference jvar) {
        return defs.stream().anyMatch(jv -> jv.equals(jvar));
    }
    
    public boolean useVariable(JReference jvar) {
        return uses.stream().anyMatch(jv -> jv.equals(jvar));
    }
    
    @Override
    public boolean hasDefVariable() {
        return defs.size() != 0;
    }
    
    @Override
    public boolean hasUseVariable() {
        return uses.size() != 0;
    }
    
    public JReference getDefVariable(String name) {
        return defs.stream().filter(jv -> jv.getName().equals(name)).findFirst().orElse(null);
    }
    
    public JReference getUseVariable(String name) {
        return uses.stream().filter(jv -> jv.getName().equals(name)).findFirst().orElse(null);
    }
    
    public JReference getFirst() {
        return getDefVariables().get(0);
    }
    
    protected void setClone(CFGStatement cloneNode) {
        cloneNode.addDefVariables(this.getDefVariables());
        cloneNode.addUseVariables(this.getUseVariables());
    }
    
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(super.toString());
        buf.append(" D = { " + toString(defs) + " }");
        buf.append(" U = { " + toString(uses) + " }");
        return buf.toString();
    }
    
    protected String toString(List<JReference> jvars) {
        StringBuffer buf = new StringBuffer();
        jvars.forEach(jvar -> {
            buf.append(jvar.getReferenceName());
            buf.append(", ");
        });
        return buf.length() != 0 ? buf.substring(0, buf.length() - 2) : "";
    }
}
