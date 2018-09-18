/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg.builder;

import java.util.Map;
import java.util.HashMap;

/**
 * An object that represents the cached data of a class.
 * All methods of this class are not intended to be directly called by clients.
 * 
 * @author Katsuhisa Maruyama
 */
public class CachedJElement {
    
    protected String fqn;
    
    protected Map<String, String> cachedData;
    
    protected CachedJElement(String fqn, Map<String, String> cachedData) {
        this.fqn = fqn;
        this.cachedData = cachedData;
    }
    
    protected CachedJElement(String fqn) {
        this(fqn, new HashMap<String, String>());
    }
    
    public String getQualifiedName() {
        return fqn;
    }
    
    protected static int getInteger(String value) {
        return Integer.parseInt(value);
    }
    
    protected static boolean getBoolean(String value) {
        return Boolean.parseBoolean(value);
    }
}
