/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.model.cfg;

import org.eclipse.jdt.core.dom.ASTNode;
import java.util.List;
import java.util.ArrayList;

/**
 * A statement node of a CFG.
 * @author Katsuhisa Maruyama
 */
public class CFGStatement extends CFGNode {
    
    protected List<JVariable> defs = new ArrayList<JVariable>();
    protected List<JVariable> uses = new ArrayList<JVariable>();
    
    protected CFGStatement() {
    }
    
    public CFGStatement(ASTNode node, Kind kind) {
        super(node, kind);
    }
    
    public boolean addDefVariable(JVariable jvar) {
        if (jvar!= null && !defineVariable(jvar)) {
            return defs.add(jvar);
        }
        return false;
    }
    
    public boolean addUseVariable(JVariable jvar) {
        if (jvar != null && !useVariable(jvar)) {
            return uses.add(jvar);
        }
        return false;
    }
    
    public boolean addDefVariables(List<JVariable> jvars) {
        if (jvars == null) {
            return false;
        }
        for (JVariable jvar : jvars) {
            addDefVariable(jvar);
        }
        return true;
    }
    
    public boolean addUseVariables(List<JVariable> jvars) {
        if (jvars == null) {
            return false;
        }
        for (JVariable jvar : jvars) {
            addUseVariable(jvar);
        }
        return true;
    }
    
    public boolean removeDefVariable(JVariable jvar) {
        if (jvar != null) {
            return defs.remove(jvar);
        }
        return false;
    }
    
    public boolean removeUseVariable(JVariable jvar) {
        if (jvar != null) {
            return uses.remove(jvar);
        }
        return false;
    }
    
    public void clearDefVariables() {
        defs.clear();
    }
    
    public void clearUseVariables() {
        uses.clear();
    }
    
    public void setDefVariables(List<JVariable> jvars) {
        defs = jvars;
    }
    
    public void setUseVariables(List<JVariable> jvars) {
        uses = jvars;
    }
    
    public void setDefVariable(JVariable jvar) {
        clearDefVariables();
        addDefVariable(jvar);
    }
    
    public void setUseVariable(JVariable jvar) {
        clearUseVariables();
        addUseVariable(jvar);
    }
    
    public List<JVariable> getDefVariables() {
        return defs;
    }
    
    public List<JVariable> getUseVariables() {
        return uses;
    }
    
    public boolean defineVariable(JVariable jvar) {
        for (JVariable jv : defs) {
            if (jv.equals(jvar)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean useVariable(JVariable jvar) {
        for (JVariable jv : uses) {
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
    
    public JVariable getDefVariable(String name) {
        for (JVariable jv : defs) {
            if (jv.getName().equals(name)) {
                return jv;
            }
        }
        return null;
    }
    
    public JVariable getUseVariable(String name) {
        for (JVariable jv : uses) {
            if (jv.getName().equals(name)) {
                return jv;
            }
        }
        return null;
    }
    
    public JVariable getFirst() {
        return getDefVariables().get(0);
    }
    
    @Override
    public CFGStatement clone() {
        CFGStatement cloneNode = new CFGStatement(astNode, kind);
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
    
    protected String toString(List<JVariable> jvars) {
        StringBuffer buf = new StringBuffer();
        for (JVariable jvar : jvars) {
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
