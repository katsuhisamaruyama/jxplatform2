/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg;

import org.eclipse.jdt.core.dom.ASTNode;
import org.jtool.eclipse.cfg.builder.JInfoStore;

import java.util.List;

/**
 * The entry node of a CFG for a field declaration, or an enum-constant.
 * 
 * @author Katsuhisa Maruyama
 */
public class CFGFieldEntry extends CFGEntry {
    
    private String type;
    
    private JField jfield;
    
    protected CFGFieldEntry() {
    }
    
    public CFGFieldEntry(ASTNode node, CFGNode.Kind kind, String name, String fqn, String className) {
        super(node, kind, name, name, fqn);
        jfield = JInfoStore.getInstance().getJField(className, name);
    }
    
    public JField getJField() {
        return jfield;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getType() {
        return type;
    }
    
    public JReference getDefField() {
        CFGNode[] nodes = toArray(getDstNodes());
        CFGStatement decl = (CFGStatement)nodes[0];
        return decl.getFirst();
    }
    
    public List<JReference> getUsedFields() {
        CFGNode[] nodes = toArray(getDstNodes());
        CFGStatement decl = (CFGStatement)nodes[0];
        return decl.getUseVariables();
    }
    
    @Override
    public String toString() {
        return super.toString() + " FIELD [ " + getName() + " ]";
    }
}
