/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Modifier;

/**
 * An object representing an expression for an access to a field, an enum-constant, or a local variable.
 * 
 * @author Katsuhisa Maruyama
 */
public class JApparentAccess extends JAccess {
    
    public JApparentAccess(ASTNode node, String name, IVariableBinding vbinding) {
        super(node);
        
        setProperties(node);
        this.name = name;
        IVariableBinding binding = vbinding.getVariableDeclaration();
        type = binding.getType().getQualifiedName();
        isPrimitiveType = binding.getType().isPrimitive();
        modifiers = binding.getModifiers();
    }
    
    public JApparentAccess(ASTNode node, String name, ITypeBinding tbinding) {
        super(node);
        
        this.name = name;
        isPrimitiveType = false;
        ITypeBinding binding = tbinding.getTypeDeclaration();
        type = binding.getQualifiedName();
        modifiers = binding.getModifiers();
        
        setProperties(node);
    }
    
    public JApparentAccess(ASTNode node, String name, String type, boolean primitive) {
        super(node);
        
        this.name = name;
        this.type = type;
        isPrimitiveType = primitive;
        modifiers = Modifier.NONE;
        setProperties(node);
    }
    
    public JApparentAccess(ASTNode node, String name, boolean primitive) {
        super(node);
        
        ITypeBinding binding = findEnclosingClass(node).getTypeDeclaration();
        this.name = name;
        this.type = binding.getQualifiedName();
        isPrimitiveType = primitive;
        modifiers = binding.getModifiers();
        
        setProperties(node);
    }
    
    private void setProperties(ASTNode node) {
        enclosingClassName = findEnclosingClassName(node);
        enclosingMethodName = findEnclosingMethodName(enclosingClassName, node);
        declaringClassName = enclosingClassName;
        declaringMethodName = enclosingMethodName;
        
        signature = name;
        fqn = declaringMethodName + "!" + name;
        inProject = true;
    }
    
    @Override
    public boolean isLocalAccess() {
        return true;
    }
    
    @Override
    public String toString() {
        return name + "@" + type;
    }
}
