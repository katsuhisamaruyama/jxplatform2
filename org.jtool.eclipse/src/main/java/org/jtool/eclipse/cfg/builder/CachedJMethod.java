/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg.builder;

import org.jtool.eclipse.cfg.JMethod;
import java.util.Map;

/**
 * An object that represents the cached data of a method.
 * All methods of this class are not intended to be directly called by clients.
 * 
 * @author Katsuhisa Maruyama
 */
public class CachedJMethod extends CachedJElement {
    
    CachedJMethod(String fqn, Map<String, String> cachedData) {
        super(fqn, cachedData);
        
        System.out.println("METHOD = "  + fqn + " " + getSignature());
        
    }
    
    CachedJMethod(JMethod method) {
        super(method.getQualifiedName());
        cachedData.put(BytecodeCache.ClassNameAttr, method.getDeclaringClass().getQualifiedName());
        cachedData.put(BytecodeCache.SignatureAttr, method.getQualifiedName());
    }
    
    public String getClassName() {
        return cachedData.get(BytecodeCache.ClassNameAttr);
    }
    
    public String getSignature() {
        return cachedData.get(BytecodeCache.SignatureAttr);
    }
}
