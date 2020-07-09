/*
 *  Copyright 2018-2020
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg.builder;

import java.util.Map;

/**
 * An abstract class that provides concise information on a class.
 * All methods of this class are not intended to be directly called by clients.
 * 
 * @author Katsuhisa Maruyama
 */
abstract class JElement {
    
    protected static final String FqnAttr = "fqn";
    protected static final String ClassNameAttr = "cname";
    protected static final String SignatureAttr = "sig";
    protected static final String NameAttr = "name";
    protected static final String ModifierAttr = "mod";
    protected static final String InterfaceAttr = "inf";
    protected static final String DefAttr = "def";
    protected static final String UseAttr = "use";
    
    protected CFGStore cfgStore;
    protected String fqn;
    protected Map<String, String> cacheData;
    
    protected static JClass[] emptyClassArray = new JClass[0];
    protected static JMethod[] emptyMethodArray = new JMethod[0];
    protected static JField[] emptyFieldArray = new JField[0];
    
    protected JElement(String fqn, CFGStore cfgStore) {
        this.fqn = fqn;
        this.cfgStore = cfgStore;
    }
    
    protected String getQualifiedName() {
        return fqn;
    }
    
    protected void cache() {
    }
    
    protected Map<String, String> getCacheData() {
        cache();
        return cacheData;
    }
    
    protected static int getInteger(String value) {
        return Integer.parseInt(value);
    }
    
    protected static boolean getBoolean(String value) {
        return Boolean.parseBoolean(value);
    }
    
    protected boolean isCache() {
        return false;
    }
    
    protected abstract boolean isInProject();
    
    @Override
    public String toString() {
        return getQualifiedName();
    }
}
