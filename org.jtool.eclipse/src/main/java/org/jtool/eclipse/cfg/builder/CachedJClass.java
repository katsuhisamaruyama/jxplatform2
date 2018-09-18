/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg.builder;

import org.jtool.eclipse.cfg.JClass;
import java.util.Map;

/**
 * An object that represents the cached data of a class.
 * All methods of this class are not intended to be directly called by clients.
 * 
 * @author Katsuhisa Maruyama
 */
public class CachedJClass extends CachedJElement {
    
    CachedJClass(String fqn, Map<String, String> cachedData) {
        super(fqn, cachedData);
    }
    
    CachedJClass(JClass clazz) {
        super(clazz.getQualifiedName());
        cachedData.put(BytecodeCache.NameAttr, clazz.getName());
    }
    
    public String getName() {
        return cachedData.get(BytecodeCache.NameAttr);
    }
}
