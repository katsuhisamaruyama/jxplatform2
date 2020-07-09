/*
 *  Copyright 2018-2020
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg.builder;

import java.util.Map;

/**
 * An object that represents the cached data of a field.
 * All methods of this class are not intended to be directly called by clients.
 * 
 * @author Katsuhisa Maruyama
 */
class JFieldCache extends JField {
    
    protected JFieldCache(JClass declaringClass, CFGStore cfgStore, Map<String, String> cacheData) {
        super(cfgStore, cacheData);
        this.declaringClass = declaringClass;
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
}
