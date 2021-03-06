/*
 *  Copyright 2018-2020
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.javamodel;

import org.jtool.eclipse.javamodel.builder.FieldAccessCollector;
import org.jtool.eclipse.javamodel.builder.LambdaCollector;
import org.jtool.eclipse.javamodel.builder.LocalDeclarationCollector;
import org.jtool.eclipse.javamodel.builder.MethodCallCollector;
import org.jtool.eclipse.javamodel.builder.StatementCollector;
import org.jtool.eclipse.util.Logger;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * An object representing a method, a constructor, or an initializer.
 * 
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
    
    protected List<JavaLocalVar> parameters = new ArrayList<>();
    protected List<JavaLocalVar> localDecls = new ArrayList<>();
    protected JavaLocalVar returnVariable = null;
    
    protected Map<String, Type> exceptionTypes = new HashMap<>();
    
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
            if (mbinding.isConstructor()) {
                returnType = binding.getName();
            } else {
                returnType = binding.getReturnType().getQualifiedName();
            }
            modifiers = binding.getModifiers();
            kind = getKind(binding);
            inProject = true;
            
            declaringClass = jclass;
            
            collectParameters(node.parameters());
            collectLocalVariables(node.getBody());
            collectLambdas(node.getBody());
            if (!isVoid(returnType)) {
                returnVariable = new JavaLocalVar(this, "$" + name);
            } else {
                returnVariable = null;
            }
            for (Type exceptionType : (List<Type>)node.thrownExceptionTypes()) {
                exceptionTypes.put(exceptionType.resolveBinding().getTypeDeclaration().getQualifiedName(), exceptionType);
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
                returnVariable = new JavaLocalVar(this, "$" + name);
            } else {
                returnVariable = null;
            }
        }
        
        jclass.addMethod(this);
    }
    
    protected JavaMethod(IMethodBinding mbinding, JavaClass jclass, boolean inProject) {
        super(null, null);
        
        if (mbinding != null) {
            binding = mbinding.getMethodDeclaration();
            name = binding.getName();
            signature = getSignature(binding);
            fqn = jclass.getQualifiedName() + QualifiedNameSeparator + signature;
            if (mbinding.isConstructor()) {
                returnType = binding.getName();
            } else {
                returnType = binding.getReturnType().getQualifiedName();
            }
            modifiers = binding.getModifiers();
            kind = getKind(binding);
            this.inProject = inProject;
            declaringClass = jclass;
            
            collectParameters(mbinding.getParameterTypes());
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
            if (bindings[i].isTypeVariable()) {
                buf.append("java.lang.Object");
            } else {
                buf.append(bindings[i].getQualifiedName());
            }
        }
        return buf.toString() + " ";
    }
    
    protected void collectParameters(List<VariableDeclaration> params) {
        for (VariableDeclaration decl : params) {
            if (decl.resolveBinding() != null) {
                JavaLocalVar param = new JavaLocalVar(decl, this);
                parameters.add(param);
            }
        }
    }
    
    protected void collectParameters(ITypeBinding[] types) {
        for (ITypeBinding tbinding : types) {
            JavaLocalVar param = new JavaLocalVar(tbinding, this);
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
        return kind == JavaMethod.Kind.J_METHOD;
    }
    
    public boolean isConstructor() {
        return kind == JavaMethod.Kind.J_CONSTRUCTOR;
    }
    
    public boolean isInitializer() {
        return kind == JavaMethod.Kind.J_INITIALIZER;
    }
    
    public boolean isLambda() {
        return kind == JavaMethod.Kind.J_LAMBDA;
    }
    
    public JavaClass getDeclaringClass() {
        return declaringClass;
    }
    
    public String getName() {
       return name;
    }
    
    @Override
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
        return returnType != null ? isPrimitiveType(returnType) : false;
    }
    
    public boolean isVoid() {
        return returnType != null ? JavaElement.isVoid(returnType) : false;
    }
    
    public List<JavaLocalVar> getLocalVariables() {
        return localDecls;
    }
    
    public int getModifiers() {
        return modifiers;
    }
    
    public Kind getKind() {
        return kind;
    }
    
    public List<JavaLocalVar> getParameters() {
        return parameters;
    }
    
    public int getParameterSize() {
        return parameters.size();
    }
    
    public JavaLocalVar getParameter(int index) {
        return (index >= 0 && index < parameters.size()) ? parameters.get(index) : null;
    }
    
    public JavaLocalVar getParameter(String name) {
        int index = getParameterOrdinal(name);
        return (index != -1) ? getParameter(index) : null;
    }
    
    public int getParameterOrdinal(String name) {
        for (int ordinal = 0; ordinal < parameters.size(); ordinal++) {
            JavaLocalVar param = getParameter(ordinal);
            if (param.getName().equals(name)) {
                return ordinal;
            }
        }
        return -1;
    }
    
    public JavaLocalVar getReturnVariable() {
        return returnVariable;
    }
    
    public JavaLocalVar getLocal(String name, int id) {
        return localDecls.stream()
                .filter(jl -> name.equals(jl.getName()) && id == jl.getVariableId()).findFirst().orElse(null);
    }
    
    public Map<String, Type> getExceptionTypeNodes() {
        return exceptionTypes;
    }
    
    public ASTNode getExceptionTypeNode(String type) {
        return exceptionTypes.get(type);
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
        return !isPrivate() && getSignature().equals(jm.getSignature());
    }
    
    public boolean isInProject() {
        return inProject;
    }
    
    public IMethodBinding getMethodBinding() {
        return binding;
    }
    
    @Override
    public boolean equals(Object obj) {
        return (obj instanceof JavaMethod) ? equals((JavaMethod)obj) : false;
    }
    
    public boolean equals(JavaMethod jmethod) {
        return jmethod != null && (this == jmethod || fqn.equals(jmethod.fqn));
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
        for (JavaLocalVar param : parameters) {
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
    
    protected boolean resolved = false;
    protected Set<JavaClass> exceptions = new HashSet<>();
    protected Set<JavaMethod> calledMethods = new HashSet<>();
    protected Set<JavaMethod> callingMethods = new HashSet<>();
    protected Set<JavaField> accessedFields = new HashSet<>();
    protected Set<JavaField> accessingFields = new HashSet<>();
    protected Set<JavaMethod> overriddenMethods = null;
    protected Set<JavaMethod> overridingMethods = null;
    protected StatementCollector statementCollector = null;
    
    protected void collectInfo() {
        if (!inProject || resolved) {
            return;
        }
        
        boolean resolveOk = true;
        if (!isInitializer()) {
            if (binding != null) {
                resolveOk = resolveOk && findExceptions();
                resolveOk = resolveOk && findCalledMethods();
                resolveOk = resolveOk && findAccessedFields();
            } else {
                resolveOk = false;
            }
        } else {
            resolveOk = resolveOk && findCalledMethods();
            resolveOk = resolveOk && findAccessedFields();
        }
        
        if (!resolveOk) {
            if (declaringClass != null) {
                Logger.getInstance().printUnresolvedError("Method " + getName() + " of " + declaringClass.getQualifiedName() + " in " + jfile.getPath());
            } else {
                Logger.getInstance().printUnresolvedError("Method in " + jfile.getPath());
            }
        }
        resolved = true;
    }
    
    private boolean findExceptions() {
        boolean resolveOk = true;
        for (ITypeBinding tbinding : binding.getExceptionTypes()) {
            if (tbinding.isTypeVariable()) {
                break;
            }
            
            JavaClass jc = findDeclaringClass(getJavaProject(), tbinding);
            if (jc != null) {
                exceptions.add(jc);
            } else {
                resolveOk = false;
                Logger.getInstance().printUnresolvedError("Exception type in " + jfile.getPath());
            }
        }
        return resolveOk;
    }
    
    private boolean findCalledMethods() {
        MethodCallCollector visitor = new MethodCallCollector(getJavaProject());
        astNode.accept(visitor);
        if (visitor.isBindingOk()) {
            calledMethods.addAll(visitor.getCalledMethods());
            for (JavaMethod jmethod : calledMethods) {
                jmethod.addCallingMethod(this);
            }
            return true;
        }
        return false;
    }
    
    void addCallingMethod(JavaMethod jmethod) {
        callingMethods.add(jmethod);
    }
    
    private boolean findAccessedFields() {
        FieldAccessCollector visitor = new FieldAccessCollector(getJavaProject());
        astNode.accept(visitor);
        if (visitor.isBindingOk()) {
            accessedFields.addAll(visitor.getAccessedFields());
            for (JavaField jfield : accessedFields) {
                jfield.addAccessingMethod(this);
            }
            return true;
        }
        return false;
    }
    
    void addAccessingField(JavaField jfield) {
        accessingFields.add(jfield);
    }
    
    public Set<JavaClass> getExceptions() {
        collectInfo();
        return exceptions;
    }
    
    public Set<JavaMethod> getCalledMethods() {
        collectInfo();
        return calledMethods;
    }
    
    public Set<JavaMethod> getCalledMethodsInProject() {
        collectInfo();
        return calledMethods
                .stream()
                .filter(jm -> jm.isInProject())
                .collect(Collectors.toSet());
    }
    
    public Set<JavaMethod> getAllCalledMethods() {
        Set<JavaMethod> methods = new HashSet<>();
        collectAllCalledMethods(this, methods);
        methods.remove(this);
        return methods;
    }
    
    private void collectAllCalledMethods(JavaMethod jmethod, Set<JavaMethod> methods) {
        if (methods.contains(jmethod)) {
            return;
        }
        methods.add(jmethod);
        for (JavaMethod jm : jmethod.getCalledMethods()) {
            collectAllCalledMethods(jm, methods);
        }
    }
    
    public Set<JavaMethod> getCallingMethods() {
        collectInfo();
        return callingMethods;
    }
    
    public Set<JavaMethod> getCallingMethodsInProject() {
        collectInfo();
        return callingMethods
                .stream()
                .filter(jm -> jm.isInProject())
                .collect(Collectors.toSet());
    }
    
    public Set<JavaField> getAccessedFields() {
        collectInfo();
        return accessedFields;
    }
    
    public Set<JavaField> getAccessedFieldsInProject() {
        collectInfo();
        return accessedFields
                .stream()
                .filter(jf -> jf.isInProject())
                .collect(Collectors.toSet());
    }
    
    public Set<JavaField> getAccessingFields() {
        collectInfo();
        return accessingFields;
    }
    
    public Set<JavaField> getAccessingFieldsInProject() {
        collectInfo();
        return accessingFields
                .stream()
                .filter(jf -> jf.isInProject())
                .collect(Collectors.toSet());
    }
    
    public Set<JavaMethod> getOverriddenMethods() {
        collectInfo();
        if (overriddenMethods == null) {
            findOverriddenMethods();
        }
        if (overriddenMethods == null) {
            overriddenMethods = new HashSet<>();
        }
        return overriddenMethods;
    }
    
    public Set<JavaMethod> getOverridingMethods() {
        collectInfo();
        if (overridingMethods == null) {
            findOverriddenMethods();
        }
        if (overridingMethods == null) {
            overridingMethods = new HashSet<>();
        }
        return overridingMethods;
    }
    
    private void findOverriddenMethods() {
        for (JavaClass jc : declaringClass.getAncestors()) {
            for (JavaMethod jm : jc.getMethods()) {
                if (hasSameSigantureAndModifier(jm)) {
                    addOverriddenMethod(jm);
                    jm.addOverridingMethod(this);
                }
            }
        }
    }
    
    void addOverriddenMethod(JavaMethod jm) {
        if (overriddenMethods == null) {
            overriddenMethods = new HashSet<>();
        }
        overriddenMethods.add(jm);
    }
    
    void addOverridingMethod(JavaMethod jm) {
        if (overridingMethods == null) {
            overridingMethods = new HashSet<>();
        }
        overridingMethods.add(jm);
    }
    
    public int getNumberOfStatements() {
        if (statementCollector == null) {
            statementCollector = new StatementCollector();
            astNode.accept(statementCollector);
        }
        return statementCollector.getNumberOfStatements();
    }
    
    public int getMaximumNumberOfNesting() {
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
