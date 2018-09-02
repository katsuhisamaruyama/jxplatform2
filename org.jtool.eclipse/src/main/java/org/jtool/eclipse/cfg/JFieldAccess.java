/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg;

import static org.jtool.eclipse.javamodel.JavaElement.QualifiedNameSeparator;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.jtool.eclipse.javamodel.JavaClass;
import org.eclipse.jdt.core.dom.ITypeBinding;

/**
 * An object representing an expression for an access to a field, an enum-constant, or a local variable.
 * 
 * @author Katsuhisa Maruyama
 */
public class JFieldAccess extends JAccess {
    
    private boolean isField;
    private boolean isEnumConstant;
    
    public JFieldAccess(ASTNode node, IVariableBinding vbinding) {
        super(node);
        
        IVariableBinding binding = vbinding.getVariableDeclaration();
        enclosingClassName = findEnclosingClassName(node);
        enclosingMethodName = findEnclosingMethodName(enclosingClassName, node);
        ITypeBinding tbinding = binding.getDeclaringClass();
        if (tbinding != null) {
            declaringClassName = getQualifiedClassName(binding.getDeclaringClass().getTypeDeclaration());
        } else {
            declaringClassName = JavaClass.ArrayClassFqn;
        }
        declaringMethodName = "";
        
        name = binding.getName();
        signature = declaringClassName + QualifiedNameSeparator + name;
        fqn = signature;
        type = binding.getType().getQualifiedName();
        isPrimitiveType = binding.getType().isPrimitive();
        modifiers = binding.getModifiers();
        if (tbinding != null) {
            inProject = tbinding.isFromSource();
        } else {
            inProject = false;
        }
        isField = isField(binding);
        isEnumConstant = isEnumConstant(binding);
    }
    
    @Override
    public boolean isFieldAccess() {
        return true;
    }
    
    public boolean isField() {
        return isField;
    }
    
    public boolean isEnumConstant() {
        return isEnumConstant;
    }
    
    @Override
    public String toString() {
        return fqn + "@" + type;
    }
}
