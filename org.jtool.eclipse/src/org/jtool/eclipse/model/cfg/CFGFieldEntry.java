/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.model.cfg;

import org.eclipse.jdt.core.dom.ASTNode;
import java.util.List;

/**
 * The entry node of a CFG for a field declaration, or an enum-constant.
 * @author Katsuhisa Maruyama
 */
public class CFGFieldEntry extends CFGEntry {
    
    private String type;
    
    protected CFGFieldEntry() {
    }
    
    public CFGFieldEntry(ASTNode node, CFGNode.Kind kind, String name, String fqn) {
        super(node, kind, name, fqn);
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getType() {
        return type;
    }
    
    public List<JVariable> getUsedFields() {
        CFGNode[] nodes = toArray(getDstNodes());
        CFGStatement decl = (CFGStatement)nodes[0];
        return decl.getUseVariables();
    }
    
    @Override
    public String toString() {
        return super.toString() + " FIELD [ " + getName() + "]";
    }
}
