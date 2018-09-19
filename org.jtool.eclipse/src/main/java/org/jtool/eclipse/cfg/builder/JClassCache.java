/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg.builder;

import java.util.Map;

/**
 * An object that represents the cached data of a class.
 * All methods of this class are not intended to be directly called by clients.
 * 
 * @author Katsuhisa Maruyama
 */
class JClassCache extends JClass {
    
    protected JClassCache(String fqn, CFGStore cfgStore, Map<String, String> cacheData) {
        super(fqn, cfgStore, cacheData.get(NameAttr), 0);
        this.cacheData = cacheData;
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
