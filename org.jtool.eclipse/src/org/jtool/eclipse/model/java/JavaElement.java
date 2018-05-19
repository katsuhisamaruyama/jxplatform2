/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.model.java;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

/**
 * A root object for a Java program element.
 * @author Katsuhisa Maruyama
 */
public abstract class JavaElement {
    
    protected ASTNode astNode;
    protected JavaFile jfile;
    protected CodeRange codeRange;
    
    public static final String QualifiedNameSeparator = "#";
    
    protected JavaElement() {
    }
    
    public JavaFile getFile() {
        return jfile;
    }
    	
    public ASTNode getASTNode() {
        return astNode;
    }
    
    protected JavaElement(ASTNode node, JavaFile jfile) {
        this.astNode = node;
        this.jfile = jfile;
        if (node != null) {
            codeRange = new CodeRange(node);
        } else {
            codeRange = null;
        }
    }
    
    public void dispose() {
        astNode = null;
        jfile = null;
        codeRange = null;
    }
    
    public CodeRange getCodeRange() {
        return codeRange;
    }
    
    public String getSource() {
        if (codeRange != null && codeRange.getCodeLength() > 0) {
            StringBuilder buf = new StringBuilder(jfile.getCode());
            return buf.substring(codeRange.getStartPosition(), codeRange.getEndPosition() + 1);
        }
        return "";
    }
    
    public String getExtendedSource() {
        if (codeRange != null && codeRange.getCodeLength() > 0) {
            StringBuffer buf = new StringBuffer(jfile.getCode());
            return buf.substring(codeRange.getExtendedStartPosition(), codeRange.getExtendedEndPosition() + 1);
        }
        return "";
    }
    
    protected static String retrieveQualifiedName(ITypeBinding binding) {
        String name = binding.getQualifiedName();
        if (name.length() != 0) {
            return name;
        }
        
        ITypeBinding b = binding.getDeclaringClass();
        while (b != null && b.getQualifiedName().length() == 0) {
            b = b.getDeclaringClass();
        }
        if (b == null) {
            return ".UNKNOWN";
        }
        
        name = b.getQualifiedName();
        String key = binding.getKey();
        int index = key.indexOf('$');
        if (index != -1) {
            key = key.substring(index, key.length() - 1);
        } else {
            key = "$";
        }
        String fqn = name + key;
        return fqn;
    }
    
    public static JavaClass findDeclaringClass(JavaProject jproject, ITypeBinding tbinding) {
        if (tbinding != null) {
            tbinding = tbinding.getTypeDeclaration();
            String fqn = retrieveQualifiedName(tbinding);
            if (fqn != null && fqn.length() != 0) {
                if (tbinding.isFromSource()) {
                    return jproject.getClass(fqn);
                } else {
                    JavaClass jclass = jproject.getExternalClass(fqn);
                    if (jclass == null) {
                        jclass = new JavaClass(tbinding, false);
                        jproject.addExternalClass(jclass);
                    }
                    return jclass;
                }
            }
        }
        return null;
    }
    
    public static JavaMethod findDeclaringMethod(JavaProject jproject, IMethodBinding mbinding) {
        if (mbinding != null) { 
            mbinding = mbinding.getMethodDeclaration();
            JavaClass jclass = findDeclaringClass(jproject, mbinding.getDeclaringClass());
            if (jclass != null) {
                if (jclass.isInProject()) {
                    return jclass.getMethod(JavaMethod.getSignature(mbinding));
                    
                } else {
                    JavaMethod jmethod = jclass.getMethod(JavaMethod.getSignature(mbinding));
                    if (jmethod == null) {
                        jmethod = new JavaMethod(mbinding, jclass, false);
                    }
                    return jmethod;
                }
            }
        }
        return null;
    }
    
    public static JavaField findDeclaringField(JavaProject jproject, IVariableBinding vbinding) {
        if (vbinding != null && vbinding.isField()) {
            vbinding = vbinding.getVariableDeclaration();
            JavaClass jclass = findDeclaringClass(jproject, vbinding.getDeclaringClass());
            if (jclass != null) {
                if (jclass.isInProject()) {
                    return jclass.getField(vbinding.getName());
                    
                } else {
                    JavaField jfield = jclass.getField(vbinding.getName());
                    if (jfield == null) {
                        jfield = new JavaField(vbinding, jclass, false);
                    }
                    return jfield;
                }
            } else {
                jclass = getArrayClass(jproject);
                JavaField jfield = jclass.getField(vbinding.getName());
                if (jfield == null) {
                    jfield = new JavaField(vbinding, jclass, false);
                }
                return jfield;
            }
        }
        return null;
    }
    
    private static JavaClass getArrayClass(JavaProject jproject) {
        String fqn = JavaClass.ArrayClassFqn;
        JavaClass jclass = jproject.getExternalClass(fqn);
        if (jclass == null) {
            jclass = new JavaClass(fqn, false);
            jproject.addExternalClass(jclass);
        }
        return jclass;
    }
    
    public static ASTNode getAncestor(ASTNode node, int sort) {
        if (node.getNodeType() == sort) {
            return node;
        }
        ASTNode parent = node.getParent();
        if (parent != null) {
            return getAncestor(parent, sort);
        }
        return null;
    }
    
    public static JavaClass findEnclosingClass(JavaProject jproject, ASTNode node) {
        TypeDeclaration tnode = (TypeDeclaration)getAncestor(node, ASTNode.TYPE_DECLARATION);
        if (tnode != null) {
            return findDeclaringClass(jproject, tnode.resolveBinding());
        }
        EnumDeclaration enode = (EnumDeclaration)getAncestor(node, ASTNode.ENUM_DECLARATION);
        if (enode != null) {
            return findDeclaringClass(jproject, enode.resolveBinding());
        }
        return null;
    }
    
    public static JavaMethod findEnclosingMethod(JavaProject jproject, ASTNode node) {
        MethodDeclaration mnode = (MethodDeclaration)getAncestor(node, ASTNode.METHOD_DECLARATION);
        if (mnode != null) {
            return findDeclaringMethod(jproject, mnode.resolveBinding());
        }
        Initializer inode = (Initializer)getAncestor(node, ASTNode.INITIALIZER);
        if (inode != null) {
            JavaClass jc = findEnclosingClass(jproject, inode);
            if (jc != null) {
                return jc.getInitializer();
            }
        }
        return null;
    }
    
    protected static boolean isVoid(String type) {
        return type.equals("void");
    }
    
    protected static boolean isPrimitiveType(String type) {
        return type.equals("byte") || type.equals("short") || type.equals("int") || type.equals("long") ||
               type.equals("float") || type.equals("double") || type.equals("char") || type.equals("boolean");
    }
}
