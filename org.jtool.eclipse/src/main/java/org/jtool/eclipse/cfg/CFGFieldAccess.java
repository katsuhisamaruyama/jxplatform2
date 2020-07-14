/*
 *  Copyright 2020
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg;

import org.eclipse.jdt.core.dom.ASTNode;

/**
 * A node that represents a method call node within a CFG.
 * 
 * @author Katsuhisa Maruyama
 */
public class CFGFieldAccess extends CFGStatement {
    
    private JFieldReference jfieldAccess;
    
    public CFGFieldAccess(ASTNode node, JFieldReference jfacc, CFGNode.Kind kind) {
        super(node, kind);
        jfieldAccess = jfacc;
    }
    
    public JFieldReference getFieldAccess() {
        return jfieldAccess;
    }
    
    public boolean hasReceiver() {
        return jfieldAccess.hasReceiver();
    }
    
    public CFGStatement getReceiver() {
        return jfieldAccess.getReceiver();
    }
    
    public String getName() {
        return jfieldAccess.getName();
    }
    
    public String getSignature() {
        return jfieldAccess.getSignature();
    }
    
    public String getQualifiedName() {
        return jfieldAccess.getQualifiedName();
    }
    
    public String getDeclaringClassName() {
        return jfieldAccess.getDeclaringClassName();
    }
    
    public String getReturnType() {
        return jfieldAccess.getType();
    }
    
    public boolean isPrimitiveType() {
        return jfieldAccess.isPrimitiveType();
    }
    
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(super.toString());
        buf.append(" FIELD ACCESS = " + jfieldAccess.getSignature());
        return buf.toString();
    }
}
