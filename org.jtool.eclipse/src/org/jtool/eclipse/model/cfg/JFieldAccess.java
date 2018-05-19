/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.model.cfg;

import static org.jtool.eclipse.model.java.JavaElement.QualifiedNameSeparator;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.jtool.eclipse.model.java.JavaClass;
import org.eclipse.jdt.core.dom.ITypeBinding;

/**
 * An object representing an expression for an access to a field, an enum-constant, or a local variable.
 * @author Katsuhisa Maruyama
 */
public class JFieldAccess extends JVariable {
    
    protected boolean isField;
    protected boolean isEnumConstant;
    
    protected JFieldAccess() {
    }
    
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
