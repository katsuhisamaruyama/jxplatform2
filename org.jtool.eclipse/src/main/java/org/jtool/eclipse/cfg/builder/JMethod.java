/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg.builder;

import org.eclipse.jdt.core.dom.Modifier;
import java.util.Set;
import java.util.HashMap;

/**
 * An abstract class that provides concise information on a method.
 * All methods of this class are not intended to be directly called by clients.
 * 
 * @author Katsuhisa Maruyama
 */
abstract class JMethod extends JElement {
    
    protected String className;
    protected String signature;
    protected int modifiers;
    protected String returnType;
    protected boolean isPrimitive;
    
    protected JClass declaringClass;
    
    protected JMethod[] accessedMethods = null;
    protected JField[] accessedFields = null;
    
    protected JMethod[] overrindingMethods = null;
    protected JMethod[] overriddenMethods = null;
    
    protected enum SideEffectStatus {
        YES, NO, MAY, UNK;
    }
    protected SideEffectStatus sideEffects = SideEffectStatus.UNK;
    protected static final int SideEffectCheckCount = 5;
    
    protected JMethod(String fqn, CFGStore cfgStore, String className, String signature,
                      int modifiers, String returnType, boolean isPrimitive) {
        super(fqn, cfgStore);
        this.className = className;
        this.signature = signature;
        this.returnType = returnType;
        this.isPrimitive = isPrimitive;
    }
    
    protected void cache() {
        cacheData = new HashMap<String, String>();
        cacheData.put(FqnAttr, fqn);
        cacheData.put(ClassNameAttr, className);
        cacheData.put(SignatureAttr, signature);
        cacheData.put(SideEffectsAttr, sideEffects.toString());
    }
    
    protected String getClasName() {
        return className;
    }
    
    protected String getSignature() {
        return signature;
    }
    
    protected String getReturnType() {
        return returnType;
    }
    
    protected boolean isPrimitiveReturnType() {
        return isPrimitive;
    }
    
    protected boolean isVoid() {
        return returnType.equals("void");
    }
    
    protected JClass getDeclaringClass() {
        return declaringClass;
    }
    
    protected boolean isPublic() {
        return Modifier.isPublic(modifiers);
    }
    
    protected boolean isProtected() {
        return Modifier.isProtected(modifiers);
    }
    
    protected boolean isPrivate() {
        return Modifier.isPrivate(modifiers);
    }
    
    protected boolean isDefault() {
        return !isPublic() && !isProtected() && !isPrivate();
    }
    
    protected JMethod[] getAccessedMethods() {
        if (accessedMethods == null) {
            accessedMethods = findAccessedMethods();
        }
        return accessedMethods;
    }
    
    protected JField[] getAccessedFields() {
        if (accessedFields == null) {
            accessedFields = findAccessedFields();
        }
        return accessedFields;
    }
    
    protected JMethod[] getOverridingMethods() {
        if (overrindingMethods == null) {
            overrindingMethods = findOverridingMethods();
        }
        return overrindingMethods;
    }
    
    protected JMethod[] getOverriddenMethods() {
        if (overriddenMethods == null) {
            overriddenMethods = findOverriddenMethods();
        }
        return overriddenMethods;
    }
    
    protected SideEffectStatus sideEffects() {
        return sideEffects;
    }
    
    protected boolean sideEffectsYes() {
        return sideEffects == SideEffectStatus.YES || sideEffects == SideEffectStatus.MAY;
    }
    
    protected boolean sideEffectsNo() {
        return sideEffects == SideEffectStatus.NO;
    }
    
    protected boolean hasSideEffects(Set<JMethod> visitedMethods) {
        return checkSideEffects(SideEffectCheckCount, visitedMethods);
    }
    
    protected boolean checkSideEffects(int count, Set<JMethod> visitedMethods) {
        if (count == 0) {
            sideEffects = SideEffectStatus.MAY;
        }
        if (sideEffects == SideEffectStatus.UNK) {
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
    
    protected boolean equals(JMethod method) {
        if (method == null) {
            return false;
        }
        return this == method || getQualifiedName().equals(method.getQualifiedName());
    }
    
    protected abstract JMethod[] findAccessedMethods();
    
    protected abstract JField[] findAccessedFields();
    
    protected abstract JMethod[] findOverridingMethods();
    
    protected abstract JMethod[] findOverriddenMethods();
    
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
