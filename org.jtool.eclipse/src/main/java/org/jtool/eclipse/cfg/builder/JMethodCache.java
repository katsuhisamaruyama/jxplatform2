/*
 *  Copyright 2018-2020
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg.builder;

import static org.jtool.eclipse.javamodel.JavaElement.QualifiedNameSeparator;
import java.util.Map;
import java.util.HashSet;

/**
 * An object that represents the cached data of a method.
 * All methods of this class are not intended to be directly called by clients.
 * 
 * @author Katsuhisa Maruyama
 */
class JMethodCache extends JMethod {
    
    protected static final char QualifiedNameSeparatorChar = QualifiedNameSeparator.charAt(0);
    
    protected JMethodCache(JClass declaringClass, CFGStore cfgStore, Map<String, String> cacheData) {
        super(cfgStore, cacheData);
        this.declaringClass = declaringClass;
        this.cacheData = cacheData;
        
        defFields = new HashSet<>();
        for (String str : collectFields(cacheData.get(DefAttr))) {
            if (!DefOrUseField.isUnknown(str)) {
                defFields.add(DefOrUseField.instance(str));
            }
        }
        
        useFields = new HashSet<>();
        for (String str : collectFields(cacheData.get(UseAttr))) {
            if (!DefOrUseField.isUnknown(str)) {
                useFields.add(DefOrUseField.instance(str));
            }
        }
    }
    
    protected String[] collectFields(String str) {
        return (str == null || str.indexOf(';') == -1) ? new String[0] : str.split(";", 0);
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
