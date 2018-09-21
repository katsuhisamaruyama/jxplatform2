/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg.builder;

import java.util.Map;
import java.util.Set;

/**
 * An object that represents the cached data of a method.
 * All methods of this class are not intended to be directly called by clients.
 * 
 * @author Katsuhisa Maruyama
 */
class JMethodCache extends JMethod {
    
    protected JMethodCache(JClass declaringClass, CFGStore cfgStore, Map<String, String> cacheData) {
        super(cacheData.get(FqnAttr), cfgStore, cacheData.get(ClassNameAttr), cacheData.get(SignatureAttr), 0, "N/A", false);
        this.declaringClass = declaringClass;
        this.cacheData = cacheData;
        
        sideEffects = SideEffectStatus.valueOf(cacheData.get(SideEffectsAttr));
    }
    
    @Override
    protected boolean hasSideEffects(Set<JMethod> visitedMethods) {
        cfgStore.getJInfoStore().unregisterJClassCache(declaringClass.getQualifiedName());
        return super.checkSideEffects(SideEffectCheckCount, visitedMethods);
    }
    
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
