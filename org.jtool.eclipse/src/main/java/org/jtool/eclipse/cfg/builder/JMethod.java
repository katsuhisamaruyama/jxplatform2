/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg.builder;

import org.eclipse.jdt.core.dom.Modifier;
import java.util.Set;
import java.util.HashSet;
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
    
    protected Set<String> defFields = null;
    protected Set<String> useFields = null;
    
    private static int MaxNumberOfVisitedMethods = 1000000;
    protected static final String UNKNOWN_FIELD_NAME = "*";
    
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
        cacheData.put(DefAttr, convert(defFields));
        cacheData.put(UseAttr, convert(useFields));
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
    
    protected JMethod[] findAccessedMethods() {
        return emptyMethodArray;
    }
    
    protected JField[] findAccessedFields() {
        return emptyFieldArray;
    }
    
    protected JMethod[] findOverridingMethods() {
        return emptyMethodArray;
    }
    
    protected JMethod[] findOverriddenMethods() {
        return emptyMethodArray;
    }
    
    protected String convert(Set<String> names) {
        if (names == null) {
            return "";
        }
        StringBuilder buf = new StringBuilder();
        for (String name : names) {
            buf.append(name + ";");
        }
        return buf.toString();
    }
    
    protected boolean defuseDecided() {
        return defFields != null;
    }
    
    protected void addDefField(String var) {
        defFields.add(var);
    }
    
    protected void addUseFields(Set<String> vars) {
        useFields.addAll(vars);
    }
    
    protected void addDefFields(Set<String> vars) {
        defFields.addAll(vars);
    }
    
    protected void addUseField(String var) {
        useFields.add(var);
    }
    
    protected Set<String> getDefFields() {
        return defFields;
    }
    
    protected Set<String> getUseFields() {
        return useFields;
    }
    
    protected void findDefUseFields(Set<JMethod> visited, boolean recursivelyCollect) {
        if (defFields == null) {
            defFields = new HashSet<String>();
            useFields = new HashSet<String>();
        }
        
        if (visited.size() > MaxNumberOfVisitedMethods) {
            addDefField(UNKNOWN_FIELD_NAME);
            addUseField(UNKNOWN_FIELD_NAME);
            return;
        }
        
        findDefUseFieldsInThisMethod(visited, recursivelyCollect);
        findDefUseFieldsInAccessedMethods(visited, recursivelyCollect);
    }
    
    protected void findDefUseFieldsInThisMethod(Set<JMethod> visited, boolean recursivelyCollect) {
    }
    
    protected void findDefUseFieldsInAccessedMethods(Set<JMethod> visited, boolean recursivelyCollect) {
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof JMethod) {
            return equals((JMethod)obj);
        }
        return false;
    }
    
    protected boolean equals(JMethod method) {
        if (method == null) {
            return false;
        }
        return this == method || getQualifiedName().equals(method.getQualifiedName());
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
