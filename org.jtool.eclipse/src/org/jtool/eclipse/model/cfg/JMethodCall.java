/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.model.cfg;

import static org.jtool.eclipse.model.java.JavaElement.QualifiedNameSeparator;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import java.util.List;
import java.util.ArrayList;

/**
 * An object representing a call to a method or a constructor.
 * @author Katsuhisa Maruyama
 */
public class JMethodCall extends JVariable {
    
    protected boolean isMethod;
    protected boolean isConstructor;
    protected boolean isLocalCall;
    
    protected List<Expression> arguments = new ArrayList<Expression>();
    protected List<String> argumentTypes = new ArrayList<String>();
    
    protected JMethodCall() {
    }
    
    public JMethodCall(ASTNode node, IMethodBinding mbinding, List<Expression> args) {
        super(node);
        
        IMethodBinding binding = mbinding.getMethodDeclaration();
        enclosingClassName = findEnclosingClassName(node);
        enclosingMethodName = findEnclosingMethodName(enclosingClassName, node);
        declaringClassName = getQualifiedClassName(binding.getDeclaringClass().getTypeDeclaration());
        declaringMethodName = "";
        
        name = binding.getName();
        signature = getSignature(binding);
        fqn = declaringClassName + QualifiedNameSeparator + signature;
        type = binding.getReturnType().getQualifiedName();
        isPrimitiveType = binding.getReturnType().isPrimitive();
        modifiers = binding.getModifiers();
        inProject = binding.getDeclaringClass().isFromSource();
        isMethod = isMethod(binding);
        isConstructor = isConstructor(binding);
        isLocalCall = enclosingClassName.equals(declaringClassName);
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
        }
    }
    
    public String getArgumentType(int ordinal) {
        if (ordinal >= 0 && ordinal < arguments.size()) {
            return argumentTypes.get(ordinal);
        } else {
            return "";
        }
    }
    
    public boolean callSelfDirectly() {
        String fqn2 = enclosingClassName + QualifiedNameSeparator + enclosingMethodName;
        return fqn.equals(fqn2);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof JMethodCall) {
            JMethodCall jcall = (JMethodCall)obj;
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
