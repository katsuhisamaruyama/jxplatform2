/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg.builder;

import org.jtool.eclipse.cfg.builder.JMethod.SideEffectStatus;
import org.jtool.eclipse.javamodel.JavaProject;
import org.jtool.eclipse.javamodel.JavaClass;
import org.jtool.eclipse.javamodel.JavaElement;
import org.jtool.eclipse.javamodel.builder.BytecodeClassStore;
import org.jtool.eclipse.javamodel.builder.ProjectStore;

import javassist.CtClass;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * An object holds a collection of all projects.
 * All methods of this class are not intended to be directly called by clients.
 * 
 * @author Katsuhisa Maruyama
 */
class JInfoStore {
    
    private JavaProject jproject;
    private int analysisLevel;
    
    private Map<String, JClassInternal> internalClassStore = new HashMap<String, JClassInternal>();
    private Map<String, JClassExternal> externalClassStore = new HashMap<String, JClassExternal>();
    
    private BytecodeClassStore bytecodeClassStore;
    private BytecodeCache bytecodeCache;
    
    JInfoStore() {
    }
    
    void create(JavaProject jproject, boolean analyzingBytecode) {
        this.jproject = jproject;
        analysisLevel = 0;
        if (analyzingBytecode) {
            bytecodeCache = new BytecodeCache(jproject);
            bytecodeCache.loadCache();
            analysisLevel = 1;
        }
    }
    
    public void destory() {
        writeCache();
        
        internalClassStore.clear();
        externalClassStore.clear();
        jproject = null;
        
        bytecodeClassStore = null;
        bytecodeCache = null;
    }
    
    public void analyzeBytecode() {
        if (bytecodeClassStore == null) {
            bytecodeClassStore = ProjectStore.getInstance().registerBytecodeClasses(jproject);
            bytecodeClassStore.collectBytecodeClassInfo();
            analysisLevel = 2;
        }
    }
    
    public BytecodeClassStore getBytecodeClassStore() {
        return bytecodeClassStore;
    }
    
    public JavaProject getProject() {
        return jproject;
    }
    
    public int getAnalysisLevel() {
        return analysisLevel;
    }
    
    public void writeCache() {
        if (bytecodeCache != null) {
            bytecodeCache.writeCache(new ArrayList<JClassExternal>(externalClassStore.values()));
        }
    }
    
    public JClass registerInternalClass(String fqn) {
        if (jproject != null) {
            JavaClass jclass = jproject.getClass(fqn);
            if (jclass != null) {
                JClassInternal clazz = new JClassInternal(jclass, jproject);
                internalClassStore.put(clazz.getQualifiedName(), clazz);
                return clazz;
            }
        }
        return null;
    }
    
    public JClass registerExternalClass(String fqn) {
        if (bytecodeClassStore != null) {
            CtClass ctClass = bytecodeClassStore.getCtClass(fqn);
            if (ctClass != null) {
                JClassExternal clazz = new JClassExternal(ctClass, jproject);
                externalClassStore.put(clazz.getQualifiedName(), clazz);
                return clazz;
            }
        }
        return null;
    }
    
    public JClass registerClass(String fqn) {
        JClass clazz = registerInternalClass(fqn);
        if (clazz == null) {
            if (analysisLevel < 3 && analysisLevel > 1) {
                
                System.out.println("BYTECODE = " + fqn);
                
                JInfoStore.getInstance().analyzeBytecode();
            }
            clazz = registerExternalClass(fqn);
        }
        return clazz;
    }
    
    public JavaClass getJavaClass(String fqn) {
        return jproject.getClass(fqn);
    }
    
    private JClass findClass(String fqn) {
        JClass clazz = internalClassStore.get(fqn);
        if (clazz == null) {
            clazz = externalClassStore.get(fqn);
        }
        return clazz;
    }
    
    public JClass getJClass(String fqn) {
        if (analysisLevel > 0) {
            JClass clazz = findClass(fqn);
            if (clazz != null) {
                return clazz;
            }
        } else {
            return null;
        }
        return registerClass(fqn);
    }
    
    public JMethod getJMethod(String className, String signature) {
        JClass clazz = getJClass(className);
        if (clazz == null) {
            return null;
        }
        
        JMethod method = clazz.getMethod(signature);
        if (method != null) {
            if (method.isInProject()) {
                return (JMethodInternal)method;
            } else {
                return (JMethodExternal)method;
            }
        }
        return null;
    }
    
    public JField getJField(String className, String name) {
        JClass clazz = getJClass(className);
        if (clazz == null) {
            return null;
        }
        
        JField field = clazz.getField(name);
        if (field != null) {
            return field;
        }
        return null;
    }
    
    public JMethodCache findCache(String className, String signature) {
        if (bytecodeCache != null) {
            JMethodCache cmethod = JInfoStore.getInstance().getCachedJMethod(className, signature);
            if (cmethod == null || SideEffectStatus.UNKNOWM.toString().equals(cmethod.sideEffects())) {
                return null;
            } else {
                return cmethod;
            }
        }
        return null;
    }
    
    JClassCache getCachedJClass(String fqn) {
        return bytecodeCache.getCachedJClass(fqn);
    }
    
    JMethodCache getCachedJMethod(String className, String signature) {
        return bytecodeCache.getCachedJMethod(className + JavaElement.QualifiedNameSeparator + signature);
    }
    
    JFieldCache getCachdeJMField(String className, String name) {
        return bytecodeCache.getCachedJField(className + JavaElement.QualifiedNameSeparator + name);
    }
}
