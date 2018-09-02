/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg;

import org.eclipse.jdt.core.dom.ASTNode;
import java.util.List;
import java.util.ArrayList;

/**
 * A statement node of a CFG.
 * 
 * @author Katsuhisa Maruyama
 */
public class CFGStatement extends CFGNode {
    
    private List<JAccess> defs = new ArrayList<JAccess>();
    private List<JAccess> uses = new ArrayList<JAccess>();
    
    protected CFGStatement() {
    }
    
    public CFGStatement(ASTNode node, Kind kind) {
        super(node, kind);
    }
    
    public boolean addDefVariable(JAccess jvar) {
        if (jvar!= null && !defineVariable(jvar)) {
            return defs.add(jvar);
        }
        return false;
    }
    
    public boolean addUseVariable(JAccess jvar) {
        if (jvar != null && !useVariable(jvar)) {
            return uses.add(jvar);
        }
        return false;
    }
    
    public void addDefVariables(List<JAccess> jvars) {
        for (JAccess jvar : jvars) {
            addDefVariable(jvar);
        }
    }
    
    public void addUseVariables(List<JAccess> jvars) {
        for (JAccess jvar : jvars) {
            addUseVariable(jvar);
        }
    }
    
    public boolean removeDefVariable(JAccess jvar) {
        return defs.remove(jvar);
    }
    
    public boolean removeUseVariable(JAccess jvar) {
        return uses.remove(jvar);
    }
    
    public void clearDefVariables() {
        defs.clear();
    }
    
    public void clearUseVariables() {
        uses.clear();
    }
    
    public void setDefVariables(List<JAccess> jvars) {
        defs = jvars;
    }
    
    public void setUseVariables(List<JAccess> jvars) {
        uses = jvars;
    }
    
    public void setDefVariable(JAccess jvar) {
        clearDefVariables();
        addDefVariable(jvar);
    }
    
    public void setUseVariable(JAccess jvar) {
        clearUseVariables();
        addUseVariable(jvar);
    }
    
    public List<JAccess> getDefVariables() {
        return defs;
    }
    
    public List<JAccess> getUseVariables() {
        return uses;
    }
    
    public boolean defineVariable(JAccess jvar) {
        for (JAccess jv : defs) {
            if (jv.equals(jvar)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean useVariable(JAccess jvar) {
        for (JAccess jv : uses) {
            if (jv.equals(jvar)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public boolean hasDefVariable() {
        return defs.size() != 0;
    }
    
    @Override
    public boolean hasUseVariable() {
        return uses.size() != 0;
    }
    
    public JAccess getDefVariable(String name) {
        for (JAccess jv : defs) {
            if (jv.getName().equals(name)) {
                return jv;
            }
        }
        return null;
    }
    
    public JAccess getUseVariable(String name) {
        for (JAccess jv : uses) {
            if (jv.getName().equals(name)) {
                return jv;
            }
        }
        return null;
    }
    
    public JAccess getFirst() {
        return getDefVariables().get(0);
    }
    
    @Override
    public CFGStatement clone() {
        CFGStatement cloneNode = new CFGStatement(getASTNode(), getKind());
        super.setClone(cloneNode);
        setClone(cloneNode);
        return cloneNode;
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
    
    protected String toString(List<JAccess> jvars) {
        StringBuffer buf = new StringBuffer();
        for (JAccess jvar : jvars) {
            if (jvar.isFieldAccess()) {
                buf.append(jvar.getQualifiedName());
            } else {
                buf.append(jvar.getName());
            }
            buf.append(", ");
        }
        if (buf.length() != 0) {
            return buf.substring(0, buf.length() - 2);
        } else {
            return "";
        }
    }
}
