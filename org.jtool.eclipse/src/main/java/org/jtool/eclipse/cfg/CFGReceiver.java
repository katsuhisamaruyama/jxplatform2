/*
 *  Copyright 2020
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

/**
 * A node for a parameter of a method declaration.
 * 
 * @author Katsuhisa Maruyama
 */
public class CFGReceiver extends CFGStatement {
    
    private boolean isMethodRef = false;
    private boolean isFieldRef = false;
    private int modifiers;
    
    public CFGReceiver(ASTNode node, CFGNode.Kind kind) {
        super(node, kind);
        if (node instanceof MethodInvocation) {
            IMethodBinding mbinding = ((MethodInvocation)node).resolveMethodBinding();
            if (mbinding != null) {
                isMethodRef = true;
                modifiers = mbinding.getModifiers();
            }
        } else if (node instanceof FieldAccess) {
            IVariableBinding vbinding = ((FieldAccess)node).resolveFieldBinding();
            if (vbinding != null) {
                isFieldRef = true;
                modifiers = vbinding.getModifiers();
            }
        } else if (node instanceof Name) {
            IVariableBinding vbinding = getVariableBinding((Name)node);
            if (vbinding != null && vbinding.isField()) {
                isFieldRef = true;
                modifiers = vbinding.getModifiers();
            }
        }
    }
    
    private IVariableBinding getVariableBinding(Name node) {
        IBinding binding = node.resolveBinding();
        if (binding != null && binding.getKind() == IBinding.VARIABLE) {
            return (IVariableBinding)binding;
        }
        return null;
    }
    
    public boolean isMethodRef() {
        return isMethodRef;
    }
    
    public boolean isFieldRef() {
        return isFieldRef;
    }
    
    public String getType() {
        return "";
    }
    
    public boolean isPublic() {
        return Modifier.isPublic(modifiers);
    }
    
    public boolean isProtected() {
        return Modifier.isProtected(modifiers);
    }
    
    public boolean isPrivate() {
        return Modifier.isPrivate(modifiers);
    }
    
    public boolean isDefault() {
        return !isPublic() && !isProtected() && !isPrivate();
    }
    
    public boolean isStatic() {
        return Modifier.isStatic(modifiers);
    }
}
