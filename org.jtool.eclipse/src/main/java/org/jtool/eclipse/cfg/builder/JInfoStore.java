/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg.builder;

import org.jtool.eclipse.javamodel.JavaProject;
import org.jtool.eclipse.javamodel.JavaClass;
import org.jtool.eclipse.javamodel.builder.BytecodeClassStore;
import org.jtool.eclipse.javamodel.builder.ProjectStore;
import javassist.CtClass;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

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
    
    private BytecodeClassStore bytecodeClassStore;
    
    JInfoStore() {
    }
    
    void create(CFGStore cfgStore, JavaProject jproject, boolean analyzingBytecode) {
        this.cfgStore = cfgStore;
        this.jproject = jproject;
        analysisLevel = 0;
        
        if (analyzingBytecode) {
            BytecodeCacheManager.loadCache(jproject, cfgStore);
            analysisLevel = 1;
        }
    }
    
    void destory() {
        writeCache();
        
        internalClassStore.clear();
        externalClassStore.clear();
        jproject = null;
        
        bytecodeClassStore = null;
    }
    
    JavaProject getJavaProject() {
        return jproject;
    }
    
    BytecodeClassStore getBytecodeClassStore() {
        return bytecodeClassStore;
    }
    
    int analysisLevel() {
        return analysisLevel;
    }
    
    boolean creatingActualNodes() {
        return cfgStore.creatingActualNodes();
    }
    
    void writeCache() {
        if (analysisLevel > 0) {
            List<JClass> classes = new ArrayList<JClass>();
            for (JavaClass jclass : jproject.getClasses()) {
                JClass clazz = externalClassStore.get(jclass.getQualifiedName());
                if (clazz != null && !classes.contains(clazz)) {
                    classes.add(clazz);
                }
                for (JavaClass jc : jclass.getEfferentClasses()) {
                    clazz = externalClassStore.get(jc.getQualifiedName());
                    if (clazz != null && !classes.contains(clazz)) {
                        classes.add(clazz);
                    }
                }
            }
            
            BytecodeCacheManager.writeCache(jproject, classes);
        }
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
        if (bytecodeClassStore == null) {
            bytecodeClassStore = ProjectStore.getInstance().registerBytecodeClasses(jproject);
            bytecodeClassStore.collectBytecodeClassInfo();
            analysisLevel = 2;
        }
        
        CtClass ctClass = bytecodeClassStore.getCtClass(fqn);
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
    
    JClass getJClass(String fqn) {
        JClass clazz = internalClassStore.get(fqn);
        if (clazz == null) {
            clazz = registerInternalClass(fqn);
        }
        if (analysisLevel == 0 || clazz != null) {
            return clazz;
        }
        
        clazz = externalClassStore.get(fqn);
        if (clazz == null) {
            clazz = registerExternalClass(fqn);
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
}
