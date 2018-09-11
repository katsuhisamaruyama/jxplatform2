/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IVariableBinding;

/**
 * An class that represents a reference to an accessed local variable.
 * 
 * @author Katsuhisa Maruyama
 */
public class JLocalReference extends JReference {
    
    private boolean isParameter;
    
    public JLocalReference(ASTNode node, IVariableBinding vbinding) {
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
