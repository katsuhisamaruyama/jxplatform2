/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg;

import org.jtool.eclipse.javamodel.JavaField;
import java.util.List;

/**
 * The entry node of a CFG for a field declaration, or an enum-constant.
 * 
 * @author Katsuhisa Maruyama
 */
public class CFGFieldEntry extends CFGEntry {
    
    private JavaField jfield;
    
    public CFGFieldEntry(JavaField jfield, CFGNode.Kind kind) {
        super(jfield.getASTNode(), kind, jfield.getName(), jfield.getQualifiedName(), jfield.getQualifiedName());
        this.jfield = jfield;
    }
    
    public JavaField getJavaField() {
        return jfield;
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
