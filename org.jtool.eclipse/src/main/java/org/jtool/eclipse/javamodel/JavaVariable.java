/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.javamodel;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Modifier;

/**
 * An object representing a field, an enum-constant, or a local variable.
 * 
 * @author Katsuhisa Maruyama
 */
public class JavaVariable extends JavaElement {
    
    public enum Kind {
        J_FIELD, J_ENUM_CONSTANT, J_LOCAL, J_PARAMETER, UNKNOWN;
    }
    
    protected String name;
    protected String fqn;
    protected String type;
    protected boolean isPrimitive;
    protected int modifiers;
    protected Kind kind;
    
    protected JavaClass declaringClass =  null;
    protected JavaMethod declaringMethod = null;
    
    protected JavaVariable() {
    }
    
    protected JavaVariable(ASTNode node, JavaFile jfile) {
        super(node, jfile);
    }
    
    protected JavaVariable.Kind getKind(IVariableBinding binding) {
        if (binding.isEnumConstant()) {
            return JavaVariable.Kind.J_ENUM_CONSTANT;
        } else if (binding.isField()) {
            return JavaVariable.Kind.J_FIELD;
        } else if (binding.isParameter()) {
            return JavaVariable.Kind.J_PARAMETER;
        } else {
            return kind = JavaVariable.Kind.J_LOCAL;
        }
    }
    
    @Override
    public void dispose() {
        super.dispose();
        
        name = null;
        fqn = null;
        type = null;
        kind = null;
        declaringClass =  null;
        declaringMethod = null;
    }
    
    public JavaClass getDeclaringClass() {
        return declaringClass;
    }
    
    public JavaMethod getDeclaringMethod() {
        return declaringMethod;
    }
    
    public String getName() {
        return name;
    }
    
    public String getQualifiedName() {
        return fqn;
    }
    
    public String getType() {
        return type;
    }
    
    public boolean isPrimitiveType() {
        return isPrimitive;
    }
    
    public int getModifiers() {
        return modifiers;
    }
    
    public Kind getKind() {
        return kind;
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
    
    public boolean isField() {
        return kind == JavaVariable.Kind.J_FIELD;
    }
    
    public boolean isEnumConstant() {
        return kind == JavaVariable.Kind.J_ENUM_CONSTANT;
    }
    
    public boolean isLocal() {
        return kind == JavaVariable.Kind.J_LOCAL;
    }
    
    public boolean isParameter() {
        return kind == JavaVariable.Kind.J_PARAMETER;
    }
}
