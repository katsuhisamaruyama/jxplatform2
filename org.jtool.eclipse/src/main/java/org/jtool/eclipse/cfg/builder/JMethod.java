/*
 *  Copyright 2018-2020
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg.builder;

import org.eclipse.jdt.core.dom.Modifier;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
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
    
    protected Set<DefOrUseField> defFields = null;
    protected Set<DefOrUseField> useFields = null;
    
    private static int MaxNumberOfVisitedMethods = 1000000;
    
    protected JMethod(String fqn, String className, String signature,
                      int modifiers, String returnType, boolean isPrimitive, CFGStore cfgStore) {
        super(fqn, cfgStore);
        this.className = className;
        this.signature = signature;
        this.modifiers = modifiers;
        this.returnType = returnType;
        this.isPrimitive = isPrimitive;
    }
    
    protected JMethod(CFGStore cfgStore, Map<String, String> cacheData) {
        super(cacheData.get(FqnAttr), cfgStore);
        try {
            this.className = cacheData.get(ClassNameAttr);
            this.signature = cacheData.get(SignatureAttr);
            this.modifiers = Integer.parseInt(cacheData.get(ModifierAttr));
            this.returnType = cacheData.get(TypeAttr);
            this.isPrimitive = Boolean.parseBoolean(cacheData.get(isPrimitiveAttr));
        } catch (NumberFormatException e) {
            System.err.println("Please remove the file \".bytecode.info\" whose format is obsolete.");
            System.exit(1);
        }
    }
    
    @Override
    protected void cache() {
        cacheData = new HashMap<>();
        cacheData.put(FqnAttr, fqn);
        cacheData.put(ClassNameAttr, className);
        cacheData.put(SignatureAttr, signature);
        cacheData.put(ModifierAttr, String.valueOf(modifiers));
        cacheData.put(TypeAttr, returnType);
        cacheData.put(DefAttr, convert(defFields));
        cacheData.put(UseAttr, convert(useFields));
    }
    
    protected String getClassName() {
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
    
    protected String convert(Set<DefOrUseField> fields) {
        if (fields == null) {
            return "";
        }
        StringBuilder buf = new StringBuilder();
        for (DefOrUseField field : fields) {
            buf.append(field.toString() + ";");
        }
        return buf.toString();
    }
    
    protected boolean defuseDecided() {
        return defFields != null;
    }
    
    protected void addDefField(DefOrUseField field) {
        defFields.add(field);
    }
    
    protected void addUseField(DefOrUseField field) {
        useFields.add(field);
    }
    
    protected void addDefFields(Set<DefOrUseField> fields) {
        defFields.addAll(fields);
    }
    
    protected void addUseFields(Set<DefOrUseField> fields) {
        useFields.addAll(fields);
    }
    
    protected Set<DefOrUseField> getDefFields() {
        return defFields;
    }
    
    protected Set<DefOrUseField> getUseFields() {
        return useFields;
    }
    
    protected void findDefUseFields(Set<JMethod> visited, boolean recursivelyCollect) {
        if (defFields == null) {
            defFields = new HashSet<>();
            useFields = new HashSet<>();
        }
        
        if (visited.contains(this)) {
            return;
        }
        
        if (visited.size() > MaxNumberOfVisitedMethods) {
            addDefField(DefOrUseField.UNKNOWN);
            addUseField(DefOrUseField.UNKNOWN);
            return;
        }
        
        findDefUseFieldsInThisMethod(visited, recursivelyCollect);
        findDefUseFieldsInAccessedMethods(visited, recursivelyCollect);
    }
    
    protected void findDefUseFieldsInThisMethod(Set<JMethod> visited, boolean recursivelyCollect) {
    }
    
    protected void findDefUseFieldsInAccessedMethods(Set<JMethod> visited, boolean recursivelyCollect) {
    }
    
    protected boolean equals(JMethod method) {
        return method != null && (this == method || getQualifiedName().equals(method.getQualifiedName()));
    }
    
    @Override
    public boolean equals(Object obj) {
        return (obj instanceof JMethod) ? equals((JMethod)obj) : false;
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
