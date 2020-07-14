/*
 *  Copyright 2018-2020
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg;

import org.jtool.eclipse.javamodel.JavaClass;
import static org.jtool.eclipse.javamodel.JavaElement.QualifiedNameSeparator;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
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
    private boolean isLocal;
    private boolean isSuper;
    
    private CFGStatement receiver = null;
    protected ASTNode nameNode;
    protected Expression receiverNode;
    
    public JFieldReference(ASTNode node, ASTNode nameNode, String receiverName, IVariableBinding vbinding) {
        super(node);
        
        this.nameNode = nameNode;
        
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
        if (receiverName.indexOf(".") == -1) {
            if (Modifier.isStatic(vbinding.getModifiers())) {
                referenceName = declaringClassName + "." + receiverName;
            } else {
                referenceName = "this." + receiverName;
            }
        } else {
            referenceName = receiverName;
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
        isLocal = enclosingClassName.equals(declaringClassName);
        isSuper = node instanceof SuperFieldAccess;
    }
    
    public JFieldReference(ASTNode node, String className, String name,
            String receiverName, String type, boolean primitive, int modifiers, boolean inProject) {
        super(node);
        
        this.nameNode = node;
        
        enclosingClassName = findEnclosingClassName(node);
        enclosingMethodName = findEnclosingMethodName(enclosingClassName, node);
        declaringClassName = className;
        declaringMethodName = "";
        
        this.name = name;
        signature = declaringClassName + QualifiedNameSeparator + name;
        fqn = signature;
        referenceName = receiverName;
        this.type = type;
        isPrimitiveType = primitive;
        this.modifiers = modifiers;
        this.inProject = inProject;
        isField = true;
        isEnumConstant = false;
        isLocal = enclosingClassName.equals(declaringClassName);
        isSuper = node instanceof SuperFieldAccess;
    }
    
    public ASTNode getNameNode() {
        return nameNode;
    }
    
    public boolean hasReceiver() {
        return receiver != null;
    }
    
    public void setReceiver(CFGStatement receiver) {
        this.receiver = receiver;
    }
    
    public CFGStatement getReceiver() {
        return receiver;
    }
    
    public void setReceiverNode(Expression exp) {
        receiverNode = exp;
    }
    
    public Expression getReceiverNode() {
        return receiverNode;
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
    
    public boolean isLocal() {
        return isLocal;
    }
    
    public boolean isSuper() {
        return isSuper;
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
        return (index == -1) ? astNode.getStartPosition() : index + astNode.getStartPosition() - 1;
    }
    
    @Override
    public String toString() {
        return fqn + "@" + type;
    }
}
