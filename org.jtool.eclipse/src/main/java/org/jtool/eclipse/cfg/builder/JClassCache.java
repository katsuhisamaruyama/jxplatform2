/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg.builder;

import java.util.List;
import java.util.Map;

/**
 * An object that represents the cached data of a class.
 * All methods of this class are not intended to be directly called by clients.
 * 
 * @author Katsuhisa Maruyama
 */
class JClassCache extends JClass {
    
    protected JClassCache(CFGStore cfgStore, Map<String, String> cacheData) {
        super(cacheData.get(FqnAttr), cfgStore, cacheData.get(NameAttr), 0);
        this.cacheData = cacheData;
    }
    
    void setMethods(List<JMethodCache> cmethods) {
        methods = cmethods.toArray(new JMethodCache[cmethods.size()]);
    }
    
    void setFields(List<JFieldCache> cfields) {
        fields = cfields.toArray(new JFieldCache[cfields.size()]);
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
    protected boolean isTopLevelClass() {
        return true;
    }
    
    @Override
    protected JClass[] findAncestors() {
        return emptyClassArray;
    }
    
    @Override
    protected JClass[] findDescendants() {
        return emptyClassArray;
    }
}
