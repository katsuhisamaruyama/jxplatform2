/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.javamodel;

import org.jtool.eclipse.javamodel.builder.TypeCollector;
import org.jtool.eclipse.util.Logger;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Modifier;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import java.util.Comparator;

/**
 * An object representing a class, an interface, an enum, or a lambda expression.
 * 
 * @author Katsuhisa Maruyama
 */
public class JavaClass extends JavaElement {
    
    protected ITypeBinding binding;
    
    public enum Kind {
        J_CLASS, J_INTERFACE, J_ENUM, J_LAMBDA, UNKNOWN;
    }
    
    public static final String ArrayClassFqn = ".JavaArray";
    
    protected String name;
    protected String fqn;
    protected int modifiers;
    protected Kind kind;
    protected boolean inProject;
    
    protected String superClassName = "";
    protected Set<String> superInterfaceNames = new HashSet<String>();
    
    protected JavaClass declaringClass = null;
    protected JavaMethod declaringMethod = null;
    
    protected List<JavaField> fields = new ArrayList<JavaField>();
    protected List<JavaMethod> methods = new ArrayList<JavaMethod>();
    protected List<JavaClass> innerClasses = new ArrayList<JavaClass>();
    
    protected JavaClass() {
    }
    
    public JavaClass(TypeDeclaration node, JavaFile jfile) {
        this(node, node.resolveBinding(), jfile);
    }
    
    public JavaClass(AnonymousClassDeclaration node, JavaFile jfile) {
        this(node, node.resolveBinding(), jfile);
    }
    
    public JavaClass(EnumDeclaration node, JavaFile jfile) {
        this(node, node.resolveBinding(), jfile);
    }
    
    protected JavaClass(ASTNode node, ITypeBinding tbinding, JavaFile jfile) {
        super(node, jfile);
        
        if (tbinding != null) {
            binding = tbinding.getTypeDeclaration();
            if (binding.getName().length() != 0) {
                name = binding.getName();
            } else {
                name = ".AnonymousClass";
            }
            fqn = retrieveQualifiedName(binding);
            modifiers = binding.getModifiers();
            kind = getKind(binding);
            inProject = true;
            
            if (!binding.isTopLevel()) {
                declaringClass = findDeclaringClass(binding.getDeclaringClass());
            }
            declaringMethod = findDeclaringMethod(binding.getDeclaringMethod());
            
            if (binding.getSuperclass() != null) {
                superClassName = binding.getSuperclass().getTypeDeclaration().getQualifiedName();
            }
            for (ITypeBinding tb : binding.getInterfaces()) {
                if (tb != null && tb.isInterface()) {
                    superInterfaceNames.add(tb.getTypeDeclaration().getQualifiedName());
                }
            }
        } else {
            name = ".UNKNOWN";
            fqn = ".UNKNOWN";
            kind = JavaClass.Kind.UNKNOWN;
        }
        
        jfile.getProject().addClass(this);
        JavaPackage jpackage = jfile.getPackage();
        if (jpackage == null) {
            jpackage = JavaPackage.createDefault(jfile);
            jfile.setPackage(jpackage);
        }
        jpackage.addClass(this);
        jfile.addClass(this);
    }
    
    public JavaClass(LambdaExpression node, String name, ITypeBinding tbinding, JavaMethod jmethod) {
        super(node, jmethod.getFile());
        
        if (tbinding != null) {
            binding = tbinding.getTypeDeclaration();
            this.name = name;
            fqn = name;
            modifiers = Modifier.PUBLIC;
            kind = JavaClass.Kind.J_LAMBDA;
            inProject = true;
            
            declaringClass = jmethod.getDeclaringClass();
            declaringMethod = jmethod;
            
            superClassName = null;
            superInterfaceNames.add(binding.getQualifiedName());
        }
        
        jfile.getProject().addClass(this);
        JavaPackage jpackage = jfile.getPackage();
        if (jpackage == null) {
            jpackage = JavaPackage.createDefault(jfile);
            jfile.setPackage(jpackage);
        }
        jpackage.addClass(this);
    }
    
    protected JavaClass(ITypeBinding tbinding, boolean inProject) {
        super(null, null);
        
        if (tbinding != null) {
            binding = tbinding.getTypeDeclaration();
        }
        
        name = binding.getName();
        fqn = retrieveQualifiedName(binding);
        modifiers = binding.getModifiers();
        kind = getKind(binding);
        this.inProject = inProject;
    }
    
    protected JavaClass(String fqn, boolean inProject) {
        super(null, null);
        
        this.name = fqn;
        this.fqn = fqn;
        kind = JavaClass.Kind.J_CLASS;
        modifiers = Modifier.PUBLIC;
        this.inProject = inProject;
    }
    
    private JavaClass.Kind getKind(ITypeBinding binding) {
        if (binding.isClass()) {
            return JavaClass.Kind.J_CLASS;
        } else if (binding.isInterface()) {
            return JavaClass.Kind.J_INTERFACE;
        } else if (binding.isEnum()) {
            return JavaClass.Kind.J_ENUM;
        }
        return JavaClass.Kind.UNKNOWN;
    }
    
    @Override
    public void dispose() {
        name = null;
        fqn = null;
        kind = null;
        superClassName = null;
        if (superInterfaceNames != null) {
            superInterfaceNames.clear();
        }
        superInterfaceNames = null;
        declaringClass = null;
        declaringMethod = null;
        binding = null;
        
        if (fields != null) {
            for (JavaField jfield : fields) {
                jfield.dispose();
            }
            fields.clear();
        }
        fields = null;
        if (methods != null) {
            for (JavaMethod jmethod : methods) {
                jmethod.dispose();
            }
            methods.clear();
        }
        methods = null;
        if (innerClasses != null) {
            for (JavaClass jclass : innerClasses) {
                if (jclass != null) {
                    jclass.dispose();
                }
            }
            innerClasses.clear();
            innerClasses = null;
        }
        super.dispose();
    }
    
    public String getName() {
        return name;
    }
    
    public String getQualifiedName() {
        return fqn;
    }
    
    public int getModifiers() {
        return modifiers;
    }
    
    public Kind getKind() {
        return kind;
    }
    
    public JavaPackage getPackage() {
        return jfile.getPackage();
    }
    
    public boolean isClass() {
        return kind == JavaClass.Kind.J_CLASS;
    }
    
    public boolean isInterface() {
        return kind == JavaClass.Kind.J_INTERFACE;
    }
    
    public boolean isEnum() {
        return kind == JavaClass.Kind.J_ENUM;
    }
    
    public boolean isLambda() {
        return kind == JavaClass.Kind.J_LAMBDA;
    }
    
    public String getKindLabel() {
        if (isClass()) {
            return "CLASS";
        } else if (isInterface()) {
            return "INTERFACE";
        } else if (isEnum()) {
            return "ENUM";
        } else if (isLambda()) {
            return "LAMBDA";
        } else {
            return kind.toString();
        }
    }
    
    public String getSuperClassName() {
        return superClassName;
    }
    
    public Set<String> getSuperInterfaceNames() {
        return superInterfaceNames;
    }
    
    public JavaClass getDeclaringClass() {
        return declaringClass;
    }
    
    public JavaMethod getDeclaringMethod() {
        return declaringMethod;
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
    
    public boolean isStrictfp() {
        return Modifier.isStrictfp(modifiers);
    }
    
    public boolean isInProject() {
        return inProject;
    }
    
    public ITypeBinding getTypeBinding() {
        return binding;
    }
    
    public void addField(JavaField jfield) {
        if (!fields.contains(jfield)) {
            fields.add(jfield);
        }
    }
    
    public JavaField getField(String name) {
        for (JavaField jf : fields) {
            if (jf.getName().equals(name)) {
                return jf;
            }
        }
        return null;
    }
    
    public List<JavaField> getFields() {
        return fields;
    }
    
    public List<JavaField> getFieldsInDictionaryOrder() {
        return sortFields(fields);
    }
    
    public void addMethod(JavaMethod jmethod) {
        if (!methods.contains(jmethod)) {
            methods.add(jmethod);
        }
    }
    
    public JavaMethod getMethod(String sig) {
        for (JavaMethod jm : methods) {
            if (jm.getSignature().equals(sig)) {
                return jm;
            }
        }
        return null;
    }
    
    public JavaMethod getInitializer() {
        return getMethod(JavaMethod.InitializerName);
    }
    
    public List<JavaMethod> getMethods() {
        return methods;
    }
    
    public List<JavaMethod> getMethodsInDictionaryOrder() {
        return sortMethods(methods);
    }
    
    public void addInnerClass(JavaClass jclass) {
        if (!innerClasses.contains(jclass)) {
            innerClasses.add(jclass);
        }
    }
    
    public List<JavaClass> getInnerClasses() {
        return innerClasses;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof JavaClass) {
            return equals((JavaClass)obj);
        }
        return false;
    }
    
    public boolean equals(JavaClass jclass) {
        if (jclass == null) {
            return false;
        }
        return this == jclass || fqn.equals(jclass.fqn);
    }
    
    @Override
    public int hashCode() {
        return fqn.hashCode();
    }
    
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("\n");
        buf.append(getKindLabel() + ": ");
        buf.append(getQualifiedName());
        buf.append("\n");
        if (getSuperClassName().length() > 0) {
            buf.append(" EXTENDS: ");
            buf.append(getSuperClassName());
        }
        if (getSuperInterfaceNames().size() != 0) {
            buf.append("\n");
            buf.append(" IMPLEMENTS:");
            for (String name : getSuperInterfaceNames()) {
                buf.append(" " + name);
            }
        }
        buf.append(getFieldInfo());
        buf.append(getMethodInfo());
        buf.append(getInnerClassInfo());
        return buf.toString();
    }
    
    protected String getFieldInfo() {
        StringBuilder buf = new StringBuilder();
        for (JavaField jf : getFields()) {
            buf.append(jf.toString());
        }
        return buf.toString();
    }
    
    protected String getMethodInfo() {
        StringBuilder buf = new StringBuilder();
        for (JavaMethod jm : getMethods()) {
            buf.append(jm.toString());
        }
        
        return buf.toString();
    }
    
    public String getInnerClassInfo() {
        StringBuilder buf = new StringBuilder();
        for (JavaClass jt : getInnerClasses()) {
            buf.append(jt.toString());
        }
        return buf.toString();
    }
    
    private List<JavaField> sortFields(List<JavaField> list) {
        List<JavaField> fs = new ArrayList<JavaField>(list);
        Collections.sort(fs, new Comparator<JavaField>() {
            public int compare(JavaField jf1, JavaField jf2) {
                return jf1.getName().compareTo(jf2.getName());
            }
        });
        return fs;
    }
    
    private List<JavaMethod> sortMethods(List<JavaMethod> list) {
        List<JavaMethod> ms = new ArrayList<JavaMethod>(list);
        Collections.sort(ms, new Comparator<JavaMethod>() {
            public int compare(JavaMethod jm1, JavaMethod jm2) {
                return jm1.getName().compareTo(jm2.getName());
            }
        });
        return ms;
    }
    
    protected boolean resolved = false;
    protected JavaClass superClass = null;
    protected Set<JavaClass> superInterfaces = new HashSet<JavaClass>();
    protected Set<JavaClass> usedClasses = new HashSet<JavaClass>();
    protected Set<JavaClass> afferentClasses = new HashSet<JavaClass>();
    protected Set<JavaClass> efferentClasses = new HashSet<JavaClass>();
    
    protected void collectInfo() {
        if (resolved) {
            return;
        }
        
        boolean resolveOk = true;
        if (binding != null) {
            resolveOk = resolveOk & findSuperClass();
            resolveOk = resolveOk & findSuperInterfaces();
            if (inProject) {
                resolveOk = resolveOk & findUsedClass();
                for (JavaMethod jmethod : methods) {
                    jmethod.collectInfo();
                }
                for (JavaField jfield : fields) {
                    jfield.collectInfo();
                }
                findEfferentClasses();
            }
        } else {
            resolveOk = false;
        }
        if (!resolveOk) {
            Logger.getInstance().printUnresolvedError(getQualifiedName());
        }
        resolved = true;
    }
    
    private boolean findSuperClass() {
        if (!isClass()) {
            return true;
        }
        
        if (binding.getSuperclass() != null) {
            superClass = findDeclaringClass(binding.getSuperclass());
            if (superClass != null) {
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }
    
    private boolean findSuperInterfaces() {
        if (isLambda()) {
            JavaClass jclass = findDeclaringClass(binding);
            if (jclass != null) {
                superInterfaces.add(jclass);
                return true;
            } else {
                return false;
            }
        }
        
        boolean resolveOk = true;
        for (ITypeBinding tbinding : binding.getInterfaces()) {
            JavaClass jclass = findDeclaringClass(tbinding);
            if (jclass != null) {
                superInterfaces.add(jclass);
            } else {
                resolveOk = false;
            }
        }
        return resolveOk;
    }
    
    private boolean findUsedClass() {
        TypeCollector visitor = new TypeCollector(this);
        astNode.accept(visitor);
        return visitor.isBindingOk();
    }
    
    public void addUsedClass(JavaClass jclass) {
        if (jclass != null && !jclass.equals(this) && !usedClasses.contains(jclass)) {
            usedClasses.add(jclass);
        }
    }
    
    private void findEfferentClasses() {
        if (efferentClasses == null) {
            efferentClasses = new HashSet<JavaClass>();
        }
        if (afferentClasses == null) {
            afferentClasses = new HashSet<JavaClass>();
        }
        for (JavaClass jclass : usedClasses) {
            if (!jclass.equals(this)) {
                efferentClasses.add(jclass);
                jclass.addAfferentClass(this);
            }
        }
        
        for (JavaMethod jmethod : getMethods()) {
            for (JavaMethod jm : jmethod.getCalledMethods()) {
                JavaClass jclass = jm.getDeclaringClass();
                if (!jclass.equals(this)) {
                    efferentClasses.add(jclass);
                    jclass.addAfferentClass(this);
                }
            }
            for (JavaField jf : jmethod.getAccessedFields()) {
                JavaClass jclass = jf.getDeclaringClass();
                if (!jclass.equals(this)) {
                    efferentClasses.add(jclass);
                    jclass.addAfferentClass(this);
                }
            }
        }
        
        for (JavaField jfield : getFields()) {
            for (JavaMethod jm : jfield.getCalledMethods()) {
                JavaClass jclass = jm.getDeclaringClass();
                if (!jclass.equals(this)) {
                    efferentClasses.add(jclass);
                    jclass.addAfferentClass(this);
                }
            }
            for (JavaField jf : jfield.getAccessedFields()) {
                JavaClass jclass = jf.getDeclaringClass();
                if (!jclass.equals(this)) {
                    efferentClasses.add(jclass);
                    jclass.addAfferentClass(this);
                }
            }
        }
    }
    
    private void addAfferentClass(JavaClass jclass) {
        if (jclass != null && !afferentClasses.contains(jclass)) {
            afferentClasses.add(jclass);
        }
    }
    
    public JavaClass getSuperClass() {
        collectInfo();
        return superClass;
    }
    
    public Set<JavaClass> getSuperInterfaces() {
        collectInfo();
        return superInterfaces;
    }
    
    public List<JavaClass> getChildren() {
        collectInfo();
        List<JavaClass> jclasses = new ArrayList<JavaClass>(); 
        for (JavaClass jclass : jfile.getProject().getClasses()) {
            if (jclass.isChildOf(this)) {
                jclasses.add(jclass);
            }
        }
        return jclasses;
    }
    
    public boolean isChildOf(JavaClass jclass) {
        collectInfo();
        if (superClass != null && superClass.getQualifiedName().equals(jclass.getQualifiedName())) {
            return true;
        }
        for (JavaClass jc : superInterfaces) {
            if (jc.getQualifiedName().equals(jc.getQualifiedName())) {
                return true;
            }
        }
        return false;
    }
    
    public List<JavaClass> getAllSuperClasses() {
        collectInfo();
        List<JavaClass> types = new ArrayList<JavaClass>();
        JavaClass parent = this.getSuperClass();
        while (parent != null) {
            types.add(parent);
            parent = parent.getSuperClass();
        }
        return types;
    }
    
    public List<JavaClass> getAllSuperInterfaces() {
        collectInfo();
        List<JavaClass> jclasses = new ArrayList<JavaClass>();
        getAllSuperInterfaces(this, jclasses);
        return jclasses;
    }
    
    private void getAllSuperInterfaces(JavaClass jclass, List<JavaClass> jclasses) {
        for (JavaClass parent : jclass.getSuperInterfaces()) {
            jclasses.add(parent);
            getAllSuperInterfaces(parent, jclasses);
        }
    }
    
    private void getAllChildren(JavaClass jclass, List<JavaClass> jclasses) {
        for (JavaClass child : jclass.getChildren()) {
            if (child != null) {
                jclasses.add(child);
                getAllChildren(child, jclasses);
            }
        }
    }
    
    public List<JavaClass> getAncestors() {
        collectInfo();
        List<JavaClass> jclasses = new ArrayList<JavaClass>();
        jclasses.addAll(getAllSuperClasses());
        jclasses.addAll(getAllSuperInterfaces());
        return jclasses;
    }
    
    public List<JavaClass> getDescendants() {
        collectInfo();
        List<JavaClass> jclasses = new ArrayList<JavaClass>();
        getAllChildren(this, jclasses);
        return jclasses;
    }
    
    public Set<JavaClass> getAfferentClasses() {
        return afferentClasses;
    }
    
    public Set<JavaClass> getAfferentClassesInProject() {
        Set<JavaClass> jclasses = new HashSet<JavaClass>();
        for (JavaClass jclass : afferentClasses) {
            if (jclass.isInProject()) {
                jclasses.add(jclass);
            }
        }
        return jclasses;
    }
    
    public Set<JavaClass> getEfferentClasses() {
        return efferentClasses;
    }
    
    public Set<JavaClass> getEfferentClassesInProject() {
        Set<JavaClass> jclasses = new HashSet<JavaClass>();
        for (JavaClass jclass : efferentClasses) {
            if (jclass.isInProject()) {
                jclasses.add(jclass);
            }
        }
        return jclasses;
    }
}
