/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.javamodel;

import org.jtool.eclipse.javamodel.builder.FieldAccessCollector;
import org.jtool.eclipse.javamodel.builder.LambdaCollector;
import org.jtool.eclipse.javamodel.builder.LocalDeclarationCollector;
import org.jtool.eclipse.javamodel.builder.MethodCallCollector;
import org.jtool.eclipse.javamodel.builder.ProjectStore;
import org.jtool.eclipse.javamodel.builder.StatementCollector;
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
        super(null, null);
        
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
    
    public List<JavaLocal> getLocalVariables() {
        return localDecls;
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
    
    protected boolean resolved = false;
    protected Set<JavaClass> exceptions = new HashSet<JavaClass>();
    protected Set<JavaMethod> calledMethods = new HashSet<JavaMethod>();
    protected Set<JavaMethod> callingMethods = new HashSet<JavaMethod>();
    protected Set<JavaField> accessedFields = new HashSet<JavaField>();
    protected Set<JavaField> accessingFields = new HashSet<JavaField>();
    protected Set<JavaMethod> overriddenMethods = null;
    protected Set<JavaMethod> overridingMethods = null;
    protected StatementCollector statementCollector = null;
    
    protected void collectInfo() {
        boolean resolveOk = true;
        if (binding != null) {
            resolveOk = resolveOk && findExceptions();
        }
        if (inProject) {
            resolveOk = resolveOk && findCalledMethods();
            resolveOk = resolveOk && findAccessedFields();
        } else {
            resolveOk = false;
        }
        if (!resolveOk) {
            if (declaringClass != null) {
                ProjectStore.getInstance().printUnresolvedError(getName() + " of " + declaringClass.getQualifiedName());
            } else {
                ProjectStore.getInstance().printUnresolvedError(getName());
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
            
            JavaClass jc = findDeclaringClass(tbinding);
            if (jc != null) {
                exceptions.add(jc);
            } else {
                resolveOk = false;
                ProjectStore.getInstance().printUnresolvedError(tbinding.getQualifiedName());
            }
        }
        return resolveOk;
    }
    
    private boolean findCalledMethods() {
        MethodCallCollector visitor = new MethodCallCollector();
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
        FieldAccessCollector visitor = new FieldAccessCollector();
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
        Set<JavaMethod> methods = new HashSet<JavaMethod>();
        for (JavaMethod jm : calledMethods) {
            if (jm.isInProject()) {
                methods.add(jm);
            }
        }
        return methods;
    }
    
    public Set<JavaMethod> getCallingMethods() {
        collectInfo();
        return callingMethods;
    }
    
    public Set<JavaMethod> getCallingMethodsInProject() {
        collectInfo();
        Set<JavaMethod> methods = new HashSet<JavaMethod>();
        for (JavaMethod jm : callingMethods) {
            if (jm.isInProject()) {
                methods.add(jm);
            }
        }
        return methods;
    }
    
    public Set<JavaField> getAccessedFields() {
        collectInfo();
        return accessedFields;
    }
    
    public Set<JavaField> getAccessedFieldsInProject() {
        collectInfo();
        Set<JavaField> fields = new HashSet<JavaField>();
        for (JavaField jf : accessedFields) {
            if (jf.isInProject()) {
                fields.add(jf);
            }
        }
        return fields;
    }
    
    public Set<JavaField> getAccessingFields() {
        collectInfo();
        return accessingFields;
    }
    
    public Set<JavaField> getAccessingFieldsInProject() {
        collectInfo();
        Set<JavaField> fields = new HashSet<JavaField>();
        for (JavaField jf : accessingFields) {
            if (jf.isInProject()) {
                fields.add(jf);
            }
        }
        return fields;
    }
    
    public Set<JavaMethod> getOverriddenMethods() {
        collectInfo();
        if (overriddenMethods == null) {
            findOverriddenMethods();
        }
        return overriddenMethods;
    }
    
    public Set<JavaMethod> getOverridingMethods() {
        collectInfo();
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
