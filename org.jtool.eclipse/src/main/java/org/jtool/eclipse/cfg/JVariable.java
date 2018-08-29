/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg;

import static org.jtool.eclipse.javamodel.JavaElement.QualifiedNameSeparator;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.jtool.eclipse.javamodel.JavaMethod;

/**
 * An object representing an expression for an access to a method, a field, or a local variable.
 * @author Katsuhisa Maruyama
 */
public abstract class JVariable {
    
    protected ASTNode astNode;
    protected String enclosingClassName;
    protected String enclosingMethodName;
    protected String declaringClassName;
    protected String declaringMethodName;
    
    protected String name;
    protected String signature;
    protected String fqn;
    protected String type;
    protected boolean isPrimitiveType;
    protected int modifiers;
    protected boolean inProject;
    
    protected JVariable() {
    }
    
    public JVariable(ASTNode node) {
        astNode = node;
    }
    
    public ASTNode getASTNode() {
        return astNode;
    }
    
    public String getEnclosingClassName() {
        return enclosingClassName;
    }
    
    public String getEnclosingMethodName() {
        return enclosingMethodName;
    }
    
    public String getDeclaringClassName() {
        return declaringClassName;
    }
    
    public String getDeclaringMethodName() {
        return declaringMethodName;
    }
    
    public String getName() {
        return name;
    }
    
    public String getSignature() {
        return signature;
    }
    
    public String getQualifiedName() {
        return fqn;
    }
    
    public String getType() {
        return type;
    }
    
    public boolean isPrimitiveType() {
        return isPrimitiveType;
    }
    
    public boolean isVoidType() {
        return type.equals("void");
    }
    
    public int getModifiers() {
        return modifiers;
    }
    
    public boolean isInProject() {
        return inProject;
    }
    
    public boolean isFieldAccess() {
        return false;
    }
    
    public boolean isLocalAccess() {
        return false;
    }
    
    public boolean isMethodCall() {
        return false;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof JVariable)) {
            return false;
        }
        return equals((JVariable)obj);
    }
    
    public boolean equals(JVariable jvar) {
        return this == jvar || fqn.equals(jvar.fqn);
    }
    
    @Override
    public int hashCode() {
        return fqn.hashCode();
    }
    
    protected static boolean isField(IVariableBinding vbinding) {
        return vbinding != null && vbinding.isField() && !vbinding.isEnumConstant();
    }
    
    protected static boolean isEnumConstant(IVariableBinding vbinding) {
        return vbinding != null && vbinding.isEnumConstant();
    }
    
    protected static boolean isLocal(IVariableBinding vbinding) {
        return vbinding != null && !vbinding.isField();
    }
    
    protected static boolean isMethod(IMethodBinding mbinding) {
        return mbinding != null && !mbinding.isConstructor() && !mbinding.isDefaultConstructor();
    }
    
    protected static boolean isConstructor(IMethodBinding mbinding) {
        return mbinding != null && (mbinding.isConstructor() || mbinding.isDefaultConstructor());
    }
    
    protected static String findEnclosingClassName(ASTNode node) {
        return getQualifiedClassName(findEnclosingClass(node));
    }
    
    protected static String findEnclosingMethodName(ASTNode node) {
        return getQualifiedMethodName(findEnclosingMethod(node));
    }
    
    protected static String findEnclosingMethodName(String className, ASTNode node) {
        IMethodBinding mbinding = findEnclosingMethod(node);
        if (mbinding != null) {
            return getQualifiedMethodName(className, mbinding);
        }
        return "";
    }
    
    protected static String getQualifiedClassName(ITypeBinding tbinding) {
        if (tbinding == null) {
            return "";
        }
        
        String qname = tbinding.getQualifiedName();
        if (qname.length() != 0) {
            return qname;
        }
        qname = tbinding.getBinaryName();
        if (qname.length() != 0) {
            return qname;
        }
        
        ITypeBinding tb = tbinding.getDeclaringClass();
        while (tb != null && tb.getQualifiedName().length() == 0) {
            tb = tb.getDeclaringClass();
        }
        if (tb == null) {
            return "";
        }
        qname = tb.getQualifiedName();
        String key = tbinding.getKey();
        int index = key.indexOf('$');
        if (index != -1) {
            key = key.substring(index, key.length() - 1);
        } else {
            key = "$";
        }
        String fqn = qname + key;
        return fqn;
    }
    
    protected static String getQualifiedMethodName(IMethodBinding mbinding) {
        if (mbinding == null) {
            return "";
        }
        String className = getQualifiedClassName(mbinding.getDeclaringClass().getTypeDeclaration());
        return getQualifiedMethodName(className, mbinding);
    }
    
    protected static String getQualifiedMethodName(String className, IMethodBinding mbinding) {
        if (className.length() == 0) {
            return "";
        }
        String sig = getSignature(mbinding);
        return className + QualifiedNameSeparator + sig;
    }
    
    protected static String getSignature(IMethodBinding mbinding) {
        return JavaMethod.getSignature(mbinding);
    }
    
    public static ITypeBinding findEnclosingClass(ASTNode node) {
        TypeDeclaration tnode = (TypeDeclaration)getAncestor(node, ASTNode.TYPE_DECLARATION);
        if (tnode != null) {
            return tnode.resolveBinding();
        }
        EnumDeclaration enode = (EnumDeclaration)getAncestor(node, ASTNode.ENUM_DECLARATION);
        if (enode != null) {
            return enode.resolveBinding();
        }
        return null;
    }
    
    public static IMethodBinding findEnclosingMethod(ASTNode node) {
        MethodDeclaration mnode = (MethodDeclaration)getAncestor(node, ASTNode.METHOD_DECLARATION);
        if (mnode != null) {
            return mnode.resolveBinding();
        }
        return null;
    }
    
    protected static ASTNode getAncestor(ASTNode node, int sort) {
        if (node.getNodeType() == sort) {
            return node;
        }
        ASTNode parent = node.getParent();
        if (parent != null) {
            return getAncestor(parent, sort);
        }
        return null;
    }
}
