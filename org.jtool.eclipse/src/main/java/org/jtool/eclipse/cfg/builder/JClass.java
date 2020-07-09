/*
 *  Copyright 2018-2020
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg.builder;

import org.eclipse.jdt.core.dom.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;

/**
 * An abstract class that provides concise information on a class.
 * All methods of this class are not intended to be directly called by clients.
 * 
 * @author Katsuhisa Maruyama
 */
abstract class JClass extends JElement {
    
    protected String name;
    protected int modifier;
    protected boolean isInterface;
    
    protected JClass[] ancestors = null;
    protected JClass[] descendants = null;
    
    protected JField[] fields;
    protected JMethod[] methods;
    
    protected JClass(String fqn, String name, int modifier, boolean isInterface, CFGStore cfgStore) {
        super(fqn, cfgStore);
        this.name = name;
        this.modifier = modifier;
        this.isInterface = isInterface;
    }
    
    protected JClass(CFGStore cfgStore, Map<String, String> cacheData) {
        super(cacheData.get(FqnAttr), cfgStore);
        this.name = cacheData.get(NameAttr);
        this.modifier = Integer.parseInt(cacheData.get(ModifierAttr));
        this.isInterface = Boolean.parseBoolean(cacheData.get(InterfaceAttr));
    }
    
    @Override
    protected void cache() {
        cacheData = new HashMap<String, String>();
        cacheData.put(FqnAttr, fqn);
        cacheData.put(NameAttr, name);
        cacheData.put(ModifierAttr, String.valueOf(modifier));
        cacheData.put(InterfaceAttr, String.valueOf(isInterface));
    }
    
    protected String getName() {
        return name;
    }
    
    protected boolean isPublic() {
        return Modifier.isPublic(modifier);
    }
    
    protected boolean isProtected() {
        return Modifier.isProtected(modifier);
    }
    
    protected boolean isPrivate() {
        return Modifier.isPrivate(modifier);
    }
    
    protected boolean isDefault() {
        return !isPublic() && !isProtected() && !isPrivate();
    }
    
    protected boolean isInterface() {
        return isInterface;
    }
    
    protected JField[] getFields() {
        return fields;
    }
    
    protected JField getField(String name) {
        return Arrays.stream(fields).filter(field -> field.getSignature().equals(name)).findFirst().orElse(null);
    }
    
    protected JMethod[] getMethods() {
        return methods;
    }
    
    protected JMethod getMethod(String sig) {
        return Arrays.stream(methods).filter(method -> method.getSignature().equals(sig)).findFirst().orElse(null);
    }
    
    protected JClass[] getAncestors() {
        if (ancestors == null) {
            ancestors = findAncestors();
        }
        return ancestors;
    }
    
    protected JClass[] getDescendants() {
        if (descendants == null) {
            descendants = findDescendants();
        }
        return descendants;
    }
    
    protected boolean isTopLevelClass() {
        return true;
    }
    
    protected JClass[] findAncestors() {
        return emptyClassArray;
    }
    
    protected JClass[] findDescendants() {
        return emptyClassArray;
    }
    
    public boolean equals(JClass clazz) {
        return clazz != null && (this == clazz || getQualifiedName().equals(clazz.getQualifiedName()));
    }
    
    @Override
    public boolean equals(Object obj) {
        return (obj instanceof JClass) ?equals((JClass)obj) : false;
    }
    
    @Override
    public int hashCode() {
        return getQualifiedName().hashCode();
    }
}
