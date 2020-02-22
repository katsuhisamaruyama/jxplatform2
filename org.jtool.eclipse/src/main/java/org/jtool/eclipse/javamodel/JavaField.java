/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.javamodel;

import org.jtool.eclipse.javamodel.builder.FieldInitializerCollector;
import org.jtool.eclipse.javamodel.builder.MethodCallCollector;
import org.jtool.eclipse.util.Logger;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * An object representing a field.
 * 
 * @author Katsuhisa Maruyama
 */
public class JavaField extends JavaVariable {
    
    protected IVariableBinding binding;
    
    protected boolean inProject;
    
    public JavaField(VariableDeclaration node, JavaClass jclass) {
        this(node, node.resolveBinding(), jclass);
    }
    
    public JavaField(VariableDeclarationFragment node, JavaClass jclass) {
        this(node, node.resolveBinding(), jclass);
    }
    
    public JavaField(EnumConstantDeclaration node, JavaClass jclass) {
        this(node, node.resolveVariable(), jclass);
    }
    
    protected JavaField(ASTNode node, IVariableBinding vbinding, JavaClass jclass) {
        super(node, jclass.getFile());
        
        if (vbinding != null) {
            binding = vbinding.getVariableDeclaration();
            name = vbinding.getName();
            fqn = jclass.getQualifiedName() + QualifiedNameSeparator + name;
            type = retrieveQualifiedName(binding.getType());
            isPrimitive = vbinding.getType().isPrimitive();
            modifiers = vbinding.getModifiers();
            kind = getKind(binding);
            inProject = true;
            
            declaringClass = jclass;
            declaringMethod = null;
        } else {
            name = ".UNKNOWN";
            fqn = ".UNKNOWN";
            kind = JavaVariable.Kind.UNKNOWN;
        }
        
        jclass.addField(this);
    }
    
    public JavaField(IVariableBinding vbinding, JavaClass jclass, boolean inProject) {
        super(null, null);
        
        if (vbinding != null) {
            binding = vbinding.getVariableDeclaration();
            name = vbinding.getName();
            fqn = jclass.getQualifiedName() + QualifiedNameSeparator + name;
            type = retrieveQualifiedName(binding.getType());
            isPrimitive = vbinding.getType().isPrimitive();
            modifiers = vbinding.getModifiers();
            kind = getKind(binding);
            this.inProject = inProject;
            
            declaringClass = jclass;
            declaringMethod = null;
        }
        
        jclass.addField(this);
    }
    
    @Override
    public void dispose() {
        binding = null;
        super.dispose();
    }
    
    public boolean isInProject() {
        return inProject;
    }
    
    public IVariableBinding getVariableBinding() {
        return binding;
    }
    
    @Override
    public boolean equals(Object obj) {
        return (obj instanceof JavaField) ? equals((JavaField)obj) : false;
    }
    
    public boolean equals(JavaField jfield) {
        return jfield != null && (this == jfield || fqn.equals(jfield.fqn));
    }
    
    @Override
    public int hashCode() {
        return fqn.hashCode();
    }
    
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("\n");
        buf.append("FIELD: ");
        buf.append(getName());
        buf.append("@");
        buf.append(getType());
        return buf.toString();
    }
    
    protected boolean resolved = false;
    protected Set<JavaMethod> calledMethods = new HashSet<JavaMethod>();
    protected Set<JavaMethod> accessingMethods = new HashSet<JavaMethod>();
    protected Set<JavaField> accessedFields = new HashSet<JavaField>();
    protected Set<JavaField> accessingFields = new HashSet<JavaField>();
    
    protected void collectInfo() {
        if (!inProject || resolved) {
            return;
        }
        
        boolean resolveOk = true;
        if (binding != null) {
            resolveOk = resolveOk && findCalledMethods();
            resolveOk = resolveOk && findAccessedFields();
        } else {
            resolveOk = false;
        }
        
        if (!resolveOk) {
            if (declaringClass != null) {
                Logger.getInstance().printUnresolvedError("Field " + getQualifiedName() + " of " + declaringClass.getQualifiedName() + " in " + jfile.getPath());
            } else {
                Logger.getInstance().printUnresolvedError("Field in " + jfile.getPath());
            }
        }
        resolved = true;
    }
    
    private boolean findCalledMethods() {
        MethodCallCollector visitor = new MethodCallCollector(getJavaProject());
        astNode.accept(visitor);
        if (visitor.isBindingOk()) {
            calledMethods.addAll(visitor.getCalledMethods());
            for (JavaMethod jm : calledMethods) {
                jm.addAccessingField(this);
            }
            return true;
        }
        return false;
    }
    
    void addAccessingMethod(JavaMethod jmethod) {
        accessingMethods.add(jmethod);
    }
    
    private boolean findAccessedFields() {
        FieldInitializerCollector visitor = new FieldInitializerCollector(getJavaProject());
        astNode.accept(visitor);
        if (visitor.isBindingOk()) {
            accessedFields.addAll(visitor.getAccessedFields());
            for (JavaField jf : accessedFields) {
                jf.addAccessingField(this);
            }
            return true;
        }
        return false;
    }
    
    void addAccessingField(JavaField jfield) {
        accessingFields.add(jfield);
    }
    
    public Set<JavaField> getAccessedFields() {
        collectInfo();
        return accessedFields;
    }
    
    public Set<JavaField> getAccessingFields() {
        collectInfo();
        return accessingFields;
    }
    
    public Set<JavaField> getAccessedFieldsInProject() {
        collectInfo();
        return accessedFields.stream().filter(jf -> jf.isInProject()).collect(Collectors.toCollection(HashSet::new));
    }
    
    public Set<JavaField> getAccessingFieldsInProject() {
        collectInfo();
        return accessingFields.stream().filter(jf -> jf.isInProject()).collect(Collectors.toCollection(HashSet::new));
    }
    
    public Set<JavaMethod> getCalledMethods() {
        collectInfo();
        return calledMethods;
    }
    
    public Set<JavaMethod> getAccessingMethods() {
        collectInfo();
        return accessingMethods;
    }
    
    public Set<JavaMethod> getCalledMethodsInProject() {
        collectInfo();
        return calledMethods.stream().filter(jm -> jm.isInProject()).collect(Collectors.toCollection(HashSet::new));
    }
    
    public Set<JavaMethod> getAccessingMethodsInProject() {
        collectInfo();
        return accessingMethods.stream().filter(jm -> jm.isInProject()).collect(Collectors.toCollection(HashSet::new));
    }
}
