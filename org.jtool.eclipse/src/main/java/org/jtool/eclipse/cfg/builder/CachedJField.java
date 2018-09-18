/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg.builder;

import org.jtool.eclipse.cfg.JField;
import java.util.Map;

/**
 * An object that represents the cached data of a field.
 * All methods of this class are not intended to be directly called by clients.
 * 
 * @author Katsuhisa Maruyama
 */
public class CachedJField extends CachedJElement {
    
    CachedJField(String fqn, Map<String, String> cachedData) {
        super(fqn, cachedData);
    }
    
    CachedJField(JField field) {
        super(field.getQualifiedName());
        cachedData.put(BytecodeCache.ClassNameAttr, field.getDeclaringClass().getQualifiedName());
        cachedData.put(BytecodeCache.SignatureAttr, field.getQualifiedName());
    }
    
    public String getClassName() {
        return cachedData.get(BytecodeCache.ClassNameAttr);
    }
    
    public String getName() {
        return cachedData.get(BytecodeCache.NameAttr);
    }
}
