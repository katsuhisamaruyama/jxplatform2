/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg;

import org.jtool.eclipse.javamodel.JavaMethod;
import org.eclipse.jdt.core.dom.Modifier;
import java.util.Set;

/**
 * An abstract class that provides concise information on a method.
 * 
 * @author Katsuhisa Maruyama
 */
public abstract class JMethod {
    
    protected JClass declaringClass;
    
    protected String name;
    protected String fqn;
    protected String signature;
    protected String returnType;
    protected boolean isPrimitiveType;
    protected JavaMethod.Kind kind;
    protected int modifiers;
    
    protected JMethod[] accessedMethods = null;
    protected JField[] accessedFields = null;
    protected JMethod[] overrindingMethods = null;
    protected JMethod[] overriddenMethods = null;
    
    public enum SideEffectStatus {
        YES, NO, MAYBE, UNKNOWM;
    }
    protected SideEffectStatus sideEffects = SideEffectStatus.UNKNOWM;
    protected static final int SideEffectCheckCount = 5;
    
    protected JMethod(JClass clazz) {
        declaringClass = clazz;
    }
    
    public void setAttribute(String name, String fqn, String signature, String returnType, boolean isPrimitiveType,
                                String kind, int modifiers) {
        this.name = name;
        this.fqn = fqn;
        this.signature = signature;
        this.returnType = returnType;
        this.isPrimitiveType = isPrimitiveType;
        this.kind = JavaMethod.Kind.valueOf(kind);
        this.modifiers = modifiers;
    }
    
    public JClass getDeclaringClass() {
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
        return isPrimitiveType;
    }
    
    public boolean isVoid() {
        return returnType.equals("void");
    }
    
    public JavaMethod.Kind getKind() {
        return kind;
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
    
    public int getModifiers() {
        return modifiers;
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
    
    public abstract boolean isInProject();
    
    public JMethod[] getAccessedMethods() {
        if (accessedMethods == null) {
            accessedMethods = findAccessedMethods();
        }
        return accessedMethods;
    }
    
    public JField[] getAccessedFields() {
        if (accessedFields == null) {
            accessedFields = findAccessedFields();
        }
        return accessedFields;
    }
    
    
    public JMethod[] getOverridingMethods() {
        if (overrindingMethods == null) {
            overrindingMethods = findOverridingMethods();
        }
        return overrindingMethods;
    }
    
    public JMethod[] getOverriddenMethods() {
        if (overriddenMethods == null) {
            overriddenMethods = findOverriddenMethods();
        }
        return overriddenMethods;
    }
    
    protected abstract JMethod[] findAccessedMethods();
    
    protected abstract JField[] findAccessedFields();
    
    protected abstract JMethod[] findOverridingMethods();
    
    protected abstract JMethod[] findOverriddenMethods();
    
    public SideEffectStatus sideEffects() {
        return sideEffects;
    }
    
    public boolean sideEffectsYes() {
        return sideEffects == SideEffectStatus.YES || sideEffects == SideEffectStatus.MAYBE;
    }
    
    public boolean sideEffectsNo() {
        return sideEffects == SideEffectStatus.NO;
    }
    
    public boolean hasSideEffects(Set<JMethod> visitedMethods) {
        return checkSideEffects(SideEffectCheckCount, visitedMethods);
    }
    
    protected boolean checkSideEffects(int count, Set<JMethod> visitedMethods) {
        if (count == 0) {
            sideEffects = SideEffectStatus.MAYBE;
        }
        if (sideEffects == SideEffectStatus.UNKNOWM) {
            checkSideEffectsOnFields(visitedMethods);
            checkSideEffectsOnMethods(count, visitedMethods);
        }
        return sideEffectsYes();
    }
    
    protected void checkSideEffectsOnFields(Set<JMethod> visitedMethods) {
    }
    
    protected void checkSideEffectsOnMethods(int count, Set<JMethod> visitedMethods) {
        for (JMethod method : getOverridingMethods()) {
            if (method != null && !visitedMethods.contains(method) && method.checkSideEffects(count, visitedMethods)) {
                sideEffects = SideEffectStatus.YES;
                return;
            }
        }
        
        for (JMethod method : getAccessedMethods()) {
            if (method != null && !visitedMethods.contains(method) && method.checkSideEffects(count - 1, visitedMethods)) {
                sideEffects = SideEffectStatus.YES;
                return;
            }
        }
    }
    
    public boolean equals(JMethod method) {
        if (method == null) {
            return false;
        }
        return this == method || getQualifiedName().equals(method.getQualifiedName());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof JMethod) {
            return equals((JMethod)obj);
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return getQualifiedName().hashCode();
    }
    
    @Override
    public String toString() {
        return getQualifiedName();
    }
}
