/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg;

import org.jtool.eclipse.javamodel.JavaClass;
import static org.jtool.eclipse.javamodel.JavaElement.QualifiedNameSeparator;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.ITypeBinding;

/**
 * An class that represents a reference to an accessed field.
 * 
 * @author Katsuhisa Maruyama
 */
public class JFieldReference extends JReference {
    
    private boolean isField;
    private boolean isEnumConstant;
    
    public JFieldReference(ASTNode node, String rname, IVariableBinding vbinding) {
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
        if (rname.indexOf(QualifiedNameSeparator) == -1) {
            referenceName = declaringClassName + QualifiedNameSeparator + rname;
        } else {
            referenceName = rname;
        }
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
    
    public JFieldReference(ASTNode node, String className, String name, String rname, String type, boolean primitive, boolean inProject) {
        super(node);
        
        enclosingClassName = findEnclosingClassName(node);
        enclosingMethodName = findEnclosingMethodName(enclosingClassName, node);
        declaringClassName = className;
        declaringMethodName = "";
        
        this.name = name;
        signature = declaringClassName + QualifiedNameSeparator + name;
        fqn = signature;
        if (rname.indexOf(QualifiedNameSeparator) == -1) {
            referenceName = declaringClassName + QualifiedNameSeparator + rname;
        } else {
            referenceName = rname;
        }
        this.type = type;
        isPrimitiveType = primitive;
        modifiers = 0;
        this.inProject = inProject;
        isField = true;
        isEnumConstant = false;
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
    
    public boolean isFinal() {
        return Modifier.isFinal(modifiers);
    }
    
    public boolean isStatic() {
        return Modifier.isStatic(modifiers);
    }
    
    public boolean isVolatile() {
        return Modifier.isVolatile(modifiers);
    }
    
    public boolean isTransient() {
        return Modifier.isTransient(modifiers);
    }
    
    @Override
    public int getStartPosition() {
        int index = referenceName.lastIndexOf('.');
        if (index == -1) {
            return astNode.getStartPosition();
        } else {
            return index + astNode.getStartPosition() - 1;
        }
    }
    
    @Override
    public String toString() {
        return fqn + "@" + type;
    }
}
