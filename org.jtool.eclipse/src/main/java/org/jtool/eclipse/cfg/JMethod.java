/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg;

import java.util.Set;

/**
 * An abstract class that provides concise information on a method.
 * 
 * @author Katsuhisa Maruyama
 */
public abstract class JMethod {
    
    protected JClass declaringClass;
    
    protected JMethod[] accessedMethods = null;
    protected JField[] accessedFields = null;
    protected JMethod[] overrindingMethods = null;
    protected JMethod[] overriddenMethods = null;
    
    protected enum SideEffectStatus {
        YES, NO, MAYBE, UNKNOWM;
    }
    protected SideEffectStatus sideEffects = SideEffectStatus.UNKNOWM;
    protected static final int SideEffectCheckCount = 5;
    
    protected JMethod() {
    }
    
    public JClass getDeclaringClass() {
        return declaringClass;
    }
    
    public abstract String getName();
    
    public abstract String getQualifiedName();
    
    public abstract String getSignature();
    
    public abstract String getReturnType();
    
    public abstract boolean isPrimitiveReturnType();
    
    public abstract boolean isVoid();
    
    public abstract boolean isMethod();
    
    public abstract boolean isConstructor();
    
    public abstract boolean isInitializer();
    
    public abstract boolean isPublic();
    
    public abstract boolean isProtected();
    
    public abstract boolean isPrivate();
    
    public abstract boolean isDefault();
    
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
    
    public boolean hasSideEffects(Set<JMethod> visitedMethods) {
        return hasSideEffects(SideEffectCheckCount, visitedMethods);
    }
    
    protected boolean hasSideEffects(int count, Set<JMethod> visitedMethods) {
        if (count == 0) {
            sideEffects = SideEffectStatus.MAYBE;
        }
        if (sideEffects == SideEffectStatus.UNKNOWM) {
            checkSideEffectsOnFields(visitedMethods);
            checkSideEffectsOnMethods(count, visitedMethods);
        }
        return sideEffects == SideEffectStatus.YES || sideEffects == SideEffectStatus.MAYBE;
    }
    
    protected void checkSideEffectsOnFields(Set<JMethod> visitedMethods) {
    }
    
    protected void checkSideEffectsOnMethods(int count, Set<JMethod> visitedMethods) {
        for (JMethod method : getOverridingMethods()) {
            if (method != null && !visitedMethods.contains(method) && method.hasSideEffects(count, visitedMethods)) {
                sideEffects = SideEffectStatus.YES;
                return;
            }
        }
        
        for (JMethod method : getAccessedMethods()) {
            if (method != null && !visitedMethods.contains(method) && method.hasSideEffects(count - 1, visitedMethods)) {
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
