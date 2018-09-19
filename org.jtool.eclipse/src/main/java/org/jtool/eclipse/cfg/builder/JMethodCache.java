/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg.builder;

import java.util.Map;

/**
 * An object that represents the cached data of a method.
 * All methods of this class are not intended to be directly called by clients.
 * 
 * @author Katsuhisa Maruyama
 */
class JMethodCache extends JMethod {
    
    protected JMethodCache(String fqn, CFGStore cfgStore, Map<String, String> cacheData) {
        super(fqn, cfgStore, cacheData.get(ClassNameAttr), cacheData.get(SignatureAttr), 0, "N/A", false);
        this.cacheData = cacheData;
    }
    
    /*
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
    */
    
    @Override
    protected boolean isCache() {
        return true;
    }
    
    @Override
    protected boolean isInProject() {
        return false;
    }
    
    @Override
    protected JMethod[] findAccessedMethods() {
        return emptyMethodArray;
    }
    
    @Override
    protected JField[] findAccessedFields() {
        return emptyFieldArray;
    }
    
    @Override
    protected JMethod[] findOverridingMethods() {
        return emptyMethodArray;
    }
    
    @Override
    protected JMethod[] findOverriddenMethods() {
        return emptyMethodArray;
    }
}
