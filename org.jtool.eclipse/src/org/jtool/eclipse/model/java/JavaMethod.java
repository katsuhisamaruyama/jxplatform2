/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.model.java;

import org.jtool.eclipse.model.java.builder.FieldAccessCollector;
import org.jtool.eclipse.model.java.builder.LambdaCollector;
import org.jtool.eclipse.model.java.builder.LocalDeclarationCollector;
import org.jtool.eclipse.model.java.builder.MethodCallCollector;
import org.jtool.eclipse.model.java.builder.StatementCollector;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

/**
 * An object representing a method, a constructor, or an initializer.
 * @author Katsuhisa Maruyama
 */
public class JavaMethod extends JavaElement {
    
    protected IMethodBinding binding;
    
    public static final String InitializerName = "$Init()";
    
    public enum Kind {
        J_METHOD, J_CONSTRUCTOR, J_INITIALIZER, J_LAMBDA, UNKNOWN;
    }
    
    protected String name;
    protected String signature;
    protected String fqn;
    protected String returnType;
    protected int modifiers;
    protected Kind kind;
    protected boolean inProject;
    
    protected JavaClass declaringClass = null;
    
    protected List<JavaLocal> parameters = new ArrayList<JavaLocal>();
    protected List<JavaLocal> localDecls = new ArrayList<JavaLocal>();
    protected JavaLocal returnVariable = null;
    
    protected JavaMethod() {
    }
    
    protected JavaMethod(JavaFile jfile) {
        super(null, jfile);
    }
    
    @SuppressWarnings("unchecked")
    public JavaMethod(MethodDeclaration node, JavaClass jclass) {
        super(node, jclass.getFile());
        
        IMethodBinding mbinding = node.resolveBinding();
        if (mbinding != null) {
            binding = mbinding.getMethodDeclaration();
            name = binding.getName();
            signature = getSignature(binding);
            fqn = jclass.getQualifiedName() + QualifiedNameSeparator + signature;
            returnType = binding.getReturnType().getQualifiedName();
            modifiers = binding.getModifiers();
            kind = getKind(binding);
            inProject = true;
            
            declaringClass = jclass;
            
            collectParameters(node.parameters());
            collectLocalVariables(node.getBody());
            collectLambdas(node.getBody());
            if (!isVoid(returnType)) {
                returnVariable = new JavaLocal(this, "$" + name);
            } else {
                returnVariable = null;
            }
            
        } else {
            name = ".UNKNOWN";
            signature = ".UNKNOWN";
            fqn = ".UNKNOWN";
            kind = JavaMethod.Kind.UNKNOWN;
        }
        
        jclass.addMethod(this);
    }
    
    public JavaMethod(Initializer node, JavaClass jclass) {
        super(node, jclass.getFile());
        
        declaringClass = jclass;
        name = InitializerName;
        signature = name;
        fqn = declaringClass.getQualifiedName() + QualifiedNameSeparator + signature;
        returnType = "void";
        modifiers = 0;
        kind = JavaMethod.Kind.J_INITIALIZER;
        inProject = true;
        
        collectLocalVariables(node.getBody());
        collectLambdas(node.getBody());
        
        jclass.addMethod(this);
    }
    
    @SuppressWarnings("unchecked")
    public JavaMethod(LambdaExpression node, IMethodBinding mbinding, JavaClass jclass) {
        super(node, jclass.getFile());
        
        if (mbinding != null) {
            binding = mbinding.getMethodDeclaration();
            name = binding.getName();
            signature = getSignature(binding);
            fqn = jclass.getQualifiedName() + QualifiedNameSeparator + signature;
            returnType = binding.getReturnType().getQualifiedName();
            modifiers = binding.getModifiers();
            kind = JavaMethod.Kind.J_LAMBDA;
            inProject = true;
            
            declaringClass = jclass;
            
            collectParameters(node.parameters());
            collectLocalVariables(node.getBody());
            collectLambdas(node.getBody());
            if (!isVoid(returnType)) {
                returnVariable = new JavaLocal(this, "$" + name);
            } else {
                returnVariable = null;
            }
        }
        
        jclass.addMethod(this);
    }
    
    protected JavaMethod(IMethodBinding mbinding, JavaClass jclass, boolean inProject) {
        if (mbinding != null) {
            binding = mbinding.getMethodDeclaration();
            name = binding.getName();
            signature = getSignature(binding);
            fqn = jclass.getQualifiedName() + QualifiedNameSeparator + signature;
            returnType = binding.getReturnType().getQualifiedName();
            modifiers = binding.getModifiers();
            kind = getKind(binding);
            this.inProject = inProject;
            
            declaringClass = jclass;
        }
        
        jclass.addMethod(this);
    }
    
    @Override
    public void dispose() {
        name = null;
        signature = null;
        fqn = null;
        returnType = null;
        kind = null;
        declaringClass = null;
        parameters.clear();
        parameters = null;
        localDecls.clear();
        localDecls = null;
        returnVariable = null;
        binding = null;
        super.dispose();
    }
    
    private Kind getKind(IMethodBinding mbinding) {
        if (mbinding.isConstructor() || mbinding.isDefaultConstructor()) {
            return JavaMethod.Kind.J_CONSTRUCTOR;
        } else {
            return JavaMethod.Kind.J_METHOD;
        }
    }
    
    public static String getSignature(IMethodBinding mbinding) {
        return mbinding.getName() + "(" + getParameterString(mbinding) +")";
    }
    
    protected static String getParameterString(IMethodBinding mbinding) {
        StringBuilder buf = new StringBuilder();
        ITypeBinding[] bindings = mbinding.getParameterTypes();
        for (int i = 0; i < bindings.length; i++) {
            buf.append(" ");
            buf.append(bindings[i].getQualifiedName());
        }
        return buf.toString() + " ";
    }
    
    protected void collectParameters(List<VariableDeclaration> params) {
        for (VariableDeclaration decl : params) {
            JavaLocal param = new JavaLocal(decl, this);
            parameters.add(param);
        }
    }
    
    protected void collectLocalVariables(ASTNode node) {
        if (node == null) {
            return;
        }
        LocalDeclarationCollector visitor = new LocalDeclarationCollector(this);
        node.accept(visitor);
        localDecls.addAll(visitor.getLocalDeclarations());
    }
    
    protected void collectLambdas(ASTNode node) {
        if (node == null) {
            return;
        }
        LambdaCollector visitor = new LambdaCollector(this);
        node.accept(visitor);
    }
    
    public boolean isMethod() {
        return kind == Kind.J_METHOD;
    }
    
    public boolean isConstructor() {
        return kind == Kind.J_CONSTRUCTOR;
    }
    
    public boolean isInitializer() {
        return kind == Kind.J_INITIALIZER;
    }
    
    public boolean isLambda() {
        return kind == Kind.J_LAMBDA;
    }
    
    public JavaClass getDeclaringClass() {
        return declaringClass;
    }
    
    public String getName() {
       return name;
    }
    
    public String getQualifiedName() {
        return fqn;
    }
    
    public String getSignature() {
        return signature;
    }
    
    public String getReturnType() {
        return returnType;
    }
    
    public boolean isPrimitiveReturnType() {
        return isPrimitiveType(returnType);
    }
    
    public boolean isVoid() {
        return JavaElement.isVoid(returnType);
    }
    
    public int getModifiers() {
        return modifiers;
    }
    
    public Kind getKind() {
        return kind;
    }
    
    public List<JavaLocal> getParameters() {
        return parameters;
    }
    
    public int getParameterSize() {
        return parameters.size();
    }
    
    public JavaLocal getParameter(int index) {
        if (index >= 0 && index < parameters.size()) {
            return parameters.get(index);
        }
        return null;
    }
    
    public JavaLocal getParameter(String name) {
        int index = getParameterOrdinal(name);
        if (index != -1) {
            return getParameter(index);
        }
        return null;
    }
    
    public int getParameterOrdinal(String name) {
        for (int ordinal = 0; ordinal < parameters.size(); ordinal++) {
            JavaLocal param = getParameter(ordinal);
            if (param.getName().equals(name)) {
                return ordinal;
            }
        }
        return -1;
    }
    
    public JavaLocal getReturnVariable() {
        return returnVariable;
    }
    
    public JavaLocal getLocal(String name, int id) {
        for (JavaLocal jlocal : localDecls) {
            if (name.equals(jlocal.getName()) && id == jlocal.getVariableId()) {
                return jlocal;
            }
        }
        return null;
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
    
    public boolean hasSameSigantureAndModifier(JavaMethod jm) {
        return getSignature().compareTo(jm.getSignature()) == 0 && getModifiers() == jm.getModifiers();
    }
    
    public boolean isInProject() {
        return inProject;
    }
    
    public IMethodBinding getMethodBinding() {
        return binding;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof JavaMethod) {
            return equals((JavaMethod)obj);
        }
        return false;
    }
    
    public boolean equals(JavaMethod jmethod) {
        if (jmethod == null) {
            return false;
        }
        return this == jmethod || fqn.equals(jmethod.fqn);
    }
    
    @Override
    public int hashCode() {
        return fqn.hashCode();
    }
    
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("\n");
        buf.append("METHOD: ");
        buf.append(getSignature());
        buf.append("@");
        buf.append(getReturnType());
        buf.append(getParameterInfo());
        return buf.toString();
    }
    
    public String getParameterInfo() {
        StringBuilder buf = new StringBuilder();
        for (JavaLocal param : parameters) {
            buf.append("\n");
            buf.append(" PARAMETER : ");
            buf.append(param.getName());
            buf.append("@");
            buf.append(param.getType());
        }
        return buf.toString();
    }
    
    public String getCalledMethodInfo() {
        StringBuilder buf = new StringBuilder();
        for (JavaMethod jm : getCalledMethods()) {
            buf.append("\n");
            buf.append(" THIS METHOD CALLS : ");
            buf.append(jm.getSignature());
        }
        return buf.toString();
    }
    
    protected boolean bindingOk = true;
    protected boolean bindingFin = false;
    
    protected Set<JavaClass> exceptions = new HashSet<JavaClass>();
    protected Set<JavaMethod> calledMethods = new HashSet<JavaMethod>();
    protected Set<JavaMethod> callingMethods = new HashSet<JavaMethod>();
    protected Set<JavaField> accessedFields = new HashSet<JavaField>();
    protected Set<JavaField> accessingFields = new HashSet<JavaField>();
    protected Set<JavaMethod> overriddenMethods = null;
    protected Set<JavaMethod> overridingMethods = null;
    
    public boolean isBindingOk() {
        return bindingOk;
    }
    
    private void bindingFin() {
        if (!bindingFin) {
            System.err.println("This API can be used after resolving binding information of method " + fqn + ".");
        }
    }
    
    protected boolean collectBindingInfo() {
        if (binding != null && inProject) {
            bindingOk = bindingOk && findExceptions();
        }
        if (inProject) {
            bindingOk = bindingOk && findCalledMethods();
            bindingOk = bindingOk && findAccessedFields();
        } else {
            bindingOk = false;
        }
        if (!bindingOk) {
            if (declaringClass != null) {
                jfile.getProject().addUnresolvedBindingError(getName() + " of " + declaringClass.getQualifiedName());
            } else {
                jfile.getProject().addUnresolvedBindingError(getName());
            }
        }
        bindingFin = true;
        return bindingOk;
    }
    
    private boolean findExceptions() {
        for (ITypeBinding tb : binding.getExceptionTypes()) {
            JavaClass jc = findDeclaringClass(jfile.getProject(), tb);
            if (jc != null) {
                exceptions.add(jc);
            } else {
                jfile.getProject().addUnresolvedBindingError(tb.getQualifiedName());
                return false;
            }
        }
        return true;
    }
    
    private boolean findCalledMethods() {
        MethodCallCollector visitor = new MethodCallCollector(declaringClass);
        astNode.accept(visitor);
        if (visitor.isBindingOk()) {
            calledMethods.addAll(visitor.getCalledMethods());
            for (JavaMethod jm : calledMethods) {
                jm.addCallingMethod(this);
            }
            return true;
        }
        return false;
    }
    
    void addCallingMethod(JavaMethod jmethod) {
        callingMethods.add(jmethod);
    }
    
    private boolean findAccessedFields() {
        FieldAccessCollector visitor = new FieldAccessCollector(declaringClass);
        astNode.accept(visitor);
        if (visitor.isBindingOk()) {
            accessedFields.addAll(visitor.getAccessedFields());
            for (JavaField jf : accessedFields) {
                jf.addAccessingMethod(this);
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
    
    public Set<JavaClass> getExceptions() {
        bindingFin();
        return exceptions;
    }
    
    public Set<JavaMethod> getCalledMethods() {
        bindingFin();
        return calledMethods;
    }
    
    public Set<JavaMethod> getCalledMethodsInProject() {
        bindingFin();
        Set<JavaMethod> methods = new HashSet<JavaMethod>();
        for (JavaMethod jm : calledMethods) {
            if (jm.isInProject()) {
                methods.add(jm);
            }
        }
        return methods;
    }
    
    public Set<JavaMethod> getCallingMethods() {
        bindingFin();
        return callingMethods;
    }
    
    public Set<JavaMethod> getCallingMethodsInProject() {
        bindingFin();
        Set<JavaMethod> methods = new HashSet<JavaMethod>();
        for (JavaMethod jm : callingMethods) {
            if (jm.isInProject()) {
                methods.add(jm);
            }
        }
        return methods;
    }
    
    public Set<JavaField> getAccessedFields() {
        bindingFin();
        return accessedFields;
    }
    
    public Set<JavaField> getAccessedFieldsInProject() {
        bindingFin();
        Set<JavaField> fields = new HashSet<JavaField>();
        for (JavaField jf : accessedFields) {
            if (jf.isInProject()) {
                fields.add(jf);
            }
        }
        return fields;
    }
    
    public Set<JavaField> getAccessingFields() {
        bindingFin();
        return accessingFields;
    }
    
    public Set<JavaField> getAccessingFieldsInProject() {
        bindingFin();
        Set<JavaField> fields = new HashSet<JavaField>();
        for (JavaField jf : accessingFields) {
            if (jf.isInProject()) {
                fields.add(jf);
            }
        }
        return fields;
    }
    
    public Set<JavaMethod> getOverriddenMethods() {
        bindingFin();
        if (overriddenMethods == null) {
            findOverriddenMethods();
        }
        return overriddenMethods;
    }
    
    public Set<JavaMethod> getOverridingMethods() {
        bindingFin();
        if (overridingMethods == null) {
            findOverriddenMethods();
        }
        return overridingMethods;
    }
    
    private void findOverriddenMethods() {
        overriddenMethods = new HashSet<JavaMethod>();
        overridingMethods = new HashSet<JavaMethod>();
        for (JavaClass jc : declaringClass.getAncestors()) {
            for (JavaMethod jm : jc.getMethods()) {
                if (hasSameSigantureAndModifier(jm)) {
                    overriddenMethods.add(jm);
                    jm.addOverridingMethod(this);
                }
            }
        }
    }
    
    void addOverridingMethod(JavaMethod jm) {
        overridingMethods.add(jm);
    }
    
    private StatementCollector statementCollector = null;
    
    public int getNumberOfStatements() {
        if (statementCollector == null) {
            statementCollector = new StatementCollector();
            astNode.accept(statementCollector);
        }
        return statementCollector.getNumberOfStatements();
    }
    
    public int getMaximumNuberOfNesting() {
        if (statementCollector == null) {
            statementCollector = new StatementCollector();
            astNode.accept(statementCollector);
        }
        return statementCollector.getMaximumNuberOfNesting();
    }
    
    public int getCyclomaticNumber() {
        if (statementCollector == null) {
            statementCollector = new StatementCollector();
            astNode.accept(statementCollector);
        }
        return statementCollector.getCyclomaticNumber();
    }
}
