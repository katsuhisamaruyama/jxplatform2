/*
 *  Copyright 2018-2020
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg.builder;

import org.eclipse.jdt.core.dom.Modifier;
import java.util.Map;
import java.util.HashMap;

/**
 * An abstract class that provides concise information on a field.
 * All methods of this class are not intended to be directly called by clients.
 * 
 * @author Katsuhisa Maruyama
 */
abstract class JField extends JElement {
    
    protected String className;
    protected String signature;
    protected int modifiers;
    protected String type;
    protected boolean isPrimitive;
    
    protected JClass declaringClass;
    
    protected JField(String fqn, String className, String signature,
            int modifiers, String returnType, boolean isPrimitive, CFGStore cfgStore) {
        super(fqn, cfgStore);
        this.className = className;
        this.signature = signature;
        this.type = returnType;
        this.isPrimitive = isPrimitive;
        
    }
    
    protected JField(CFGStore cfgStore, Map<String, String> cacheData) {
        super(cacheData.get(FqnAttr), cfgStore);
        this.className = cacheData.get(ClassNameAttr);
        this.signature = cacheData.get(SignatureAttr);
        this.type = "N/A";
        this.isPrimitive = false;
    }
    
    @Override
    protected void cache() {
        cacheData = new HashMap<String, String>();
        cacheData.put(FqnAttr, fqn);
        cacheData.put(ClassNameAttr, className);
        cacheData.put(SignatureAttr, signature);
    }
    
    protected String getSignature() {
        return signature;
    }
    
    protected String getType() {
        return type;
    }
    
    protected boolean isPrimitiveType() {
        return isPrimitive;
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
    
    public boolean equals(JField field) {
        return field != null && (this == field || getQualifiedName().equals(field.getQualifiedName()));
    }
    
    @Override
    public boolean equals(Object obj) {
        return (obj instanceof JField) ? equals((JField)obj) : false;
    }
    
    @Override
    public int hashCode() {
        return getQualifiedName().hashCode();
    }
}
