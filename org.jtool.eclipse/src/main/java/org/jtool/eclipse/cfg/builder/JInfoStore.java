/*
 *  Copyright 2018-2020
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg.builder;

import org.jtool.eclipse.javamodel.JavaProject;
import org.jtool.eclipse.javamodel.JavaClass;
import org.jtool.eclipse.javamodel.builder.BytecodeClassStore;
import org.jtool.eclipse.javamodel.builder.ModelBuilder;
import javassist.CtClass;
import java.util.Map;
import java.util.HashMap;

/**
 * An object that stores information on internal and external classes in the project.
 * All methods of this class are not intended to be directly called by clients.
 * 
 * @author Katsuhisa Maruyama
 */
class JInfoStore {
    
    private CFGStore cfgStore;
    private JavaProject jproject;
    private int analysisLevel;
    
    private Map<String, JClass> internalClassStore = new HashMap<String, JClass>();
    private Map<String, JClass> externalClassStore = new HashMap<String, JClass>();
    
    void create(CFGStore cfgStore, JavaProject jproject, ModelBuilder builder) {
        this.cfgStore = cfgStore;
        this.jproject = jproject;
        analysisLevel = 0;
        
        if (builder.isAnalyzingBytecode()) {
            analysisLevel = 1;
            if (builder.useBytecodeCache()) {
                BytecodeCacheManager.loadCache(jproject, cfgStore);
                analysisLevel = 2;
            }
        }
    }
    
    void destory() {
        if (analysisLevel > 1) {
            BytecodeCacheManager.writeCache(jproject, externalClassStore.values());
        }
        
        internalClassStore.clear();
        externalClassStore.clear();
        jproject = null;
    }
    
    JavaProject getJavaProject() {
        return jproject;
    }
    
    CFGStore getCFGStore() {
        return cfgStore;
    }
    
    int analysisLevel() {
        return analysisLevel;
    }
    
    JClass getJClass(String className) {
        JClass clazz = internalClassStore.get(className);
        if (clazz == null) {
            clazz = registerInternalClass(className);
        }
        if (analysisLevel == 0 || clazz != null) {
            return clazz;
        }
        
        clazz = externalClassStore.get(className);
        if (clazz == null) {
            clazz = registerExternalClass(className);
        }
        return clazz;
    }
    
    JMethod getJMethod(String className, String signature) {
        JClass clazz = getJClass(className);
        if (clazz != null) {
            return clazz.getMethod(signature);
        }
        return null;
    }
    
    JField getJField(String className, String name) {
        JClass clazz = getJClass(className);
        if (clazz != null) {
            return clazz.getField(name);
        }
        return null;
    }
    
    JClass findInternalClass(String fqn) {
        return internalClassStore.get(fqn);
    }
    
    JClass findExternalClass(String fqn) {
        return externalClassStore.get(fqn);
    }
    
    private JClass registerInternalClass(String fqn) {
        JavaClass jclass = jproject.getClass(fqn);
        if (jclass != null) {
            JClassInternal clazz = new JClassInternal(jclass, cfgStore);
            internalClassStore.put(clazz.getQualifiedName(), clazz);
            return clazz;
        }
        return null;
    }
    
    private JClass registerExternalClass(String fqn) {
        BytecodeClassStore bytecodeClassStore = jproject.getBytecodeClassStore();
        if (!bytecodeClassStore.existsBytecodeClassInfo(jproject)) {
            jproject.registerBytecodeClasses();
            analysisLevel = 3;
        }
        
        CtClass ctClass = bytecodeClassStore.getCtClassByCanonicalClassName(jproject, fqn);
        if (ctClass != null) {
            JClassExternal clazz = new JClassExternal(ctClass, cfgStore);
            externalClassStore.put(clazz.getQualifiedName(), clazz);
            return clazz;
        }
        return null;
    }
    
    void registerJClassCache(JClassCache clazz) {
        externalClassStore.put(clazz.getQualifiedName(), clazz);
    }
    
    void unregisterJClassCache(String fqn) {
        externalClassStore.remove(fqn);
    }
}
