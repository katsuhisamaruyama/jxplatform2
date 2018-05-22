/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IVariableBinding;

/**
 * An object representing an expression for an access to a field, an enum-constant, or a local variable.
 * @author Katsuhisa Maruyama
 */
public class JLocalAccess extends JVariable {
    
    private boolean isParameter;
    
    public JLocalAccess(ASTNode node, IVariableBinding vbinding) {
        super(node);
        
        IVariableBinding binding = vbinding.getVariableDeclaration();
        enclosingClassName = findEnclosingClassName(node);
        enclosingMethodName = findEnclosingMethodName(enclosingClassName, node);
        declaringClassName = enclosingClassName;
        declaringMethodName = enclosingMethodName;
        
        name = binding.getName();
        signature = name;
        fqn = declaringMethodName + "!" + name + "$" + String.valueOf(binding.getVariableId());
        type = binding.getType().getQualifiedName();
        isPrimitiveType = binding.getType().isPrimitive();
        modifiers = binding.getModifiers();
        inProject = true;
        isParameter = binding.isParameter();
    }
    
    @Override
    public boolean isLocalAccess() {
        return true;
    }
    
    public boolean isParameter() {
        return isParameter;
    }
    
    @Override
    public String toString() {
        return name + "@" + type;
    }
}
