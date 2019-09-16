/*
 *  Copyright 2018-2019
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg;

import static org.jtool.eclipse.javamodel.JavaElement.QualifiedNameSeparator;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Modifier;
import java.util.List;
import java.util.ArrayList;

/**
 * An class that represents reference to a called method or a called constructor.
 * 
 * @author Katsuhisa Maruyama
 */
public class JMethodReference extends JReference {
    
    private boolean isMethod;
    private boolean isConstructor;
    private boolean isLocalCall;
    
    private List<ITypeBinding> exceptionTypes = new ArrayList<ITypeBinding>();
    
    private List<Expression> arguments = new ArrayList<Expression>();
    private List<String> argumentTypes = new ArrayList<String>();
    private List<Boolean> argumentPrimitiveTypes = new ArrayList<Boolean>();
    
    private CFGStatement receiver = null;
    
    public JMethodReference(ASTNode node, String receiverName, IMethodBinding mbinding, List<Expression> args) {
        super(node);
        
        IMethodBinding binding = mbinding.getMethodDeclaration();
        enclosingClassName = findEnclosingClassName(node);
        enclosingMethodName = findEnclosingMethodName(enclosingClassName, node);
        declaringClassName = getQualifiedClassName(binding.getDeclaringClass().getTypeDeclaration());
        declaringMethodName = "";
        
        name = binding.getName();
        signature = getSignature(binding);
        fqn = declaringClassName + QualifiedNameSeparator + signature;
        if (receiverName != null) {
            referenceName = receiverName + QualifiedNameSeparator + signature;
        } else {
            referenceName = signature;
        }
        type = binding.getReturnType().getQualifiedName();
        isPrimitiveType = binding.getReturnType().isPrimitive();
        modifiers = binding.getModifiers();
        inProject = binding.getDeclaringClass().isFromSource();
        isMethod = isMethod(binding);
        isConstructor = isConstructor(binding);
        isLocalCall = enclosingClassName.equals(declaringClassName);
        
        for (ITypeBinding tbinding : mbinding.getExceptionTypes()) {
            exceptionTypes.add(tbinding);
        }
        
        arguments.addAll(args);
        setArgumentTypes(binding);
    }
    
    @Override
    public boolean isMethodCall() {
        return true;
    }
    
    public boolean isMethod() {
        return isMethod;
    }
    
    public boolean isConstructor() {
        return isConstructor;
    }
    
    public boolean isLocalCall() {
        return isLocalCall;
    }
    
    public boolean isFinal() {
        return Modifier.isFinal(modifiers);
    }
    
    public boolean isAbstract() {
        return Modifier.isAbstract(modifiers);
    }
    
    public boolean isStatic() {
        return Modifier.isStatic(modifiers);
    }
    
    public boolean isSynchronized() {
        return Modifier.isSynchronized(modifiers);
    }
    
    public boolean isNative() {
        return Modifier.isNative(modifiers);
    }
    
    public boolean isStrictfp() {
        return Modifier.isStrictfp(modifiers);
    }
    
    public List<ITypeBinding> getExceptionTypes() {
        return exceptionTypes;
    }
    
    public List<Expression> getArguments() {
        return arguments;
    }
    
    public int getArgumentSize() {
        return arguments.size();
    }
    
    public Expression getArgument(int ordinal) {
        if (ordinal >= 0 && ordinal < arguments.size()) {
            return arguments.get(ordinal);
        } else {
            return null;
        }
    }
    
    private void setArgumentTypes(IMethodBinding mbinding) {
        ITypeBinding[] types = mbinding.getParameterTypes();
        for (int i = 0; i < types.length; i++) {
            argumentTypes.add(types[i].getTypeDeclaration().getQualifiedName());
            argumentPrimitiveTypes.add(types[i].getTypeDeclaration().isPrimitive());
        }
    }
    
    public String getArgumentType(int ordinal) {
        if (ordinal >= 0 && ordinal < argumentTypes.size()) {
            return argumentTypes.get(ordinal);
        } else {
            return "";
        }
    }
    
    public boolean getArgumentPrimitiveType(int ordinal) {
        if (ordinal >= 0 && ordinal < argumentPrimitiveTypes.size()) {
            return argumentPrimitiveTypes.get(ordinal);
        } else {
            return false;
        }
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
    
    public boolean callSelfDirectly() {
        String fqn2 = enclosingClassName + QualifiedNameSeparator + enclosingMethodName;
        return fqn.equals(fqn2);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof JMethodReference) {
            JMethodReference jcall = (JMethodReference)obj;
            return equals(jcall);
        }
        return false;
    }
    
    @Override
    public String toString() {
        if (isLocalCall()) {
            return signature + "@" + type;
        } else {
            return fqn + "@" + type;
        }
    }
}
