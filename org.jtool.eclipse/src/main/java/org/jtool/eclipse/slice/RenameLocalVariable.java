/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.slice;

import org.jtool.eclipse.javamodel.JavaMethod;
import org.jtool.eclipse.cfg.JLocalReference;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.SimpleName;

public class RenameLocalVariable extends ASTVisitor {
    
    public JavaMethod jmethod;
    public int variableId;
    public String newName;
    
    public RenameLocalVariable(JavaMethod jmethod, JLocalReference var, String newName) {
        this.jmethod = jmethod;
        this.variableId = var.getVariableId();
        this.newName = newName;
    }
    
    public void perform() {
        jmethod.getASTNode().accept(this);
    }
    
    public boolean visit(SimpleName node) {
        IBinding binding = node.resolveBinding();
        if (binding != null && binding.getKind() == IBinding.VARIABLE) {
            IVariableBinding vbinding = (IVariableBinding)binding;
            if (vbinding.getVariableDeclaration().getVariableId() == variableId) {
                node.setIdentifier(newName);
            }
        }
        return false;
    }
}
