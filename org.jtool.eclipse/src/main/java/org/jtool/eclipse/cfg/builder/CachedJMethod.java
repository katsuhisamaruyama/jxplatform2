/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg.builder;

import org.jtool.eclipse.cfg.JMethod;
import org.jtool.eclipse.cfg.JMethod.SideEffectStatus;

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
    }
    
    CachedJMethod(JMethod method) {
        super(method.getQualifiedName());
        cachedData.put(BytecodeCache.ClassNameAttr, method.getDeclaringClass().getQualifiedName());
        cachedData.put(BytecodeCache.SignatureAttr, method.getQualifiedName());
        cachedData.put(BytecodeCache.SideEffectsAttr, method.sideEffects().toString());
    }
    
    public String getClassName() {
        return cachedData.get(BytecodeCache.ClassNameAttr);
    }
    
    public String getSignature() {
        return cachedData.get(BytecodeCache.SignatureAttr);
    }
    
    public String sideEffects() {
        return cachedData.get(BytecodeCache.SideEffectsAttr);
    }
    
    public boolean sideEffectsYes() {
        return SideEffectStatus.YES.toString().equals(cachedData.get(BytecodeCache.SideEffectsAttr));
    }
    
    public boolean sideEffectsNo() {
        return SideEffectStatus.NO.toString().equals(cachedData.get(BytecodeCache.SideEffectsAttr));
    }
    
    public boolean sideEffectsMaybe() {
        return SideEffectStatus.MAYBE.toString().equals(cachedData.get(BytecodeCache.SideEffectsAttr));
    }
}
