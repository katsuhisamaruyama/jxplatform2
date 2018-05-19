/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.model.java;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.jtool.eclipse.model.java.builder.FieldInitializerCollector;
import org.jtool.eclipse.model.java.builder.MethodCallCollector;
import java.util.Set;
import java.util.HashSet;

/**
 * An object representing a field or an enum-constant.
 * @author Katsuhisa Maruyama
 */
public class JavaField extends JavaVariable {
    
    protected IVariableBinding binding;
    
    protected boolean inProject;
    
    protected JavaField() {
    }
    
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
        if (obj instanceof JavaField) {
            return equals((JavaField)obj);
        }
        return false;
    }
    
    public boolean equals(JavaField jfield) {
        if (jfield == null) {
            return false;
        }
        return this == jfield || fqn.equals(jfield.fqn);
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
    
    protected boolean bindingOk = true;
    protected boolean bindingFin = false;
    
    protected Set<JavaMethod> calledMethods = new HashSet<JavaMethod>();
    protected Set<JavaMethod> accessingMethods = new HashSet<JavaMethod>();
    protected Set<JavaField> accessedFields = new HashSet<JavaField>();
    protected Set<JavaField> accessingFields = new HashSet<JavaField>();
    
    public boolean isBindingOk() {
        return bindingOk;
    }
    
    private void bindingFin() {
        if (!bindingFin) {
            System.err.println("This API can be used after resolving binding information of field " + fqn + ".");
        }
    }
    
    protected boolean collectBindingInfo() {
        if (binding != null && inProject) {
            bindingOk = bindingOk && findCalledMethods();
            bindingOk = bindingOk && findAccessedFields();
        } else {
            bindingOk = false;
        }
        if (!bindingOk) {
            if (declaringClass != null) {
                jfile.getProject().addUnresolvedBindingError(getQualifiedName() + " of " + declaringClass.getQualifiedName());
            } else {
                jfile.getProject().addUnresolvedBindingError(getQualifiedName());
            }
        }
        bindingFin = true;
        return bindingOk;
    }
    
    private boolean findCalledMethods() {
        MethodCallCollector visitor = new MethodCallCollector(declaringClass);
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
        FieldInitializerCollector visitor = new FieldInitializerCollector(jfile);
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
    
    /* ================================================================================
     * The following APIs can be used after resolving binding information.
     * ================================================================================ */
    
    public Set<JavaField> getAccessedFields() {
        bindingFin();
        return accessedFields;
    }
    
    public Set<JavaField> getAccessedFieldsInProject() {
        bindingFin();
        Set<JavaField> jfields = new HashSet<JavaField>();
        for (JavaField jf : accessedFields) {
            if (jf.isInProject()) {
                jfields.add(jf);
            }
        }
        return jfields;
    }
    
    public Set<JavaField> getAccessingFields() {
        bindingFin();
        return accessingFields;
    }
    
    public Set<JavaField> getAccessingFieldsInProject() {
        bindingFin();
        Set<JavaField> jfields = new HashSet<JavaField>();
        for (JavaField jf : accessingFields) {
            if (jf.isInProject()) {
                jfields.add(jf);
            }
        }
        return jfields;
    }
    
    public Set<JavaMethod> getCalledMethods() {
        bindingFin();
        return calledMethods;
    }
    
    public Set<JavaMethod> getCalledMethodsInProject() {
        bindingFin();
        Set<JavaMethod> jmethods = new HashSet<JavaMethod>();
        for (JavaMethod jm : calledMethods) {
            if (jm.isInProject()) {
                jmethods.add(jm);
            }
        }
        return jmethods;
    }
    
    public Set<JavaMethod> getAccessingMethods() {
        bindingFin();
        return accessingMethods;
    }
    
    public Set<JavaMethod> getAccessingMethodsInProject() {
        bindingFin();
        Set<JavaMethod> jmethods = new HashSet<JavaMethod>();
        for (JavaMethod jm : accessingMethods) {
            if (jm.isInProject()) {
                jmethods.add(jm);
            }
        }
        return jmethods;
    }
}
