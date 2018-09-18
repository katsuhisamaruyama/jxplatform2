/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg.builder;

import org.jtool.eclipse.cfg.JClass;
import org.jtool.eclipse.cfg.JMethod;
import org.jtool.eclipse.cfg.JMethod.SideEffectStatus;
import org.jtool.eclipse.cfg.JField;
import org.jtool.eclipse.javamodel.JavaProject;
import org.jtool.eclipse.javamodel.JavaClass;
import org.jtool.eclipse.javamodel.JavaElement;
import org.jtool.eclipse.javamodel.builder.BytecodeClassStore;
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
public class JInfoStore {
    
    private static JInfoStore instance = new JInfoStore();
    
    private Map<String, InternalJClass> internalClassStore = new HashMap<String, InternalJClass>();
    private Map<String, ExternalJClass> externalClassStore = new HashMap<String, ExternalJClass>();
    
    private JavaProject jproject;
    private int analysisLevel;
    private BytecodeClassStore bytecodeClassStore;
    private BytecodeCache bytecodeCache;
    
    private JInfoStore() {
    }
    
    public static JInfoStore getInstance() {
        return instance;
    }
    
    public void create() {
        internalClassStore.clear();
        externalClassStore.clear();
        jproject = null;
        analysisLevel = 0;
        bytecodeClassStore = null;
        bytecodeCache = null;
    }
    
    public void destory() {
        internalClassStore.clear();
        externalClassStore.clear();
        jproject = null;
        bytecodeClassStore = null;
        bytecodeCache = null;
    }
    
    public JavaProject getProject() {
        return jproject;
    }
    
    public void create(JavaProject jproject, boolean bytecodeAnalysized) {
        this.jproject = jproject;
        if (jproject != null) {
            analysisLevel = 1;
            
            System.out.println(jproject.getPath() + " " + bytecodeAnalysized);
            
            if (bytecodeAnalysized) {
                bytecodeCache = new BytecodeCache(jproject);
                bytecodeCache.loadCache();
                analysisLevel = 2;
            }
        }
    }
    
    public int getAnalysisLevel() {
        return analysisLevel;
    }
    
    public void writeCache() {
        if (bytecodeCache != null) {
            bytecodeCache.writeCache(new ArrayList<ExternalJClass>(externalClassStore.values()));
        }
    }
    
    public JClass registerInternalClass(String fqn) {
        if (jproject != null) {
            JavaClass jclass = jproject.getClass(fqn);
            if (jclass != null) {
                InternalJClass clazz = new InternalJClass(jclass, jproject);
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
                ExternalJClass clazz = new ExternalJClass(ctClass, jproject);
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
                return (InternalJMethod)method;
            } else {
                return (ExternalJMethod)method;
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
    
    public CachedJMethod findCache(String className, String signature) {
        if (bytecodeCache != null) {
            CachedJMethod cmethod = JInfoStore.getInstance().getCachedJMethod(className, signature);
            if (cmethod == null || SideEffectStatus.UNKNOWM.toString().equals(cmethod.sideEffects())) {
                return null;
            } else {
                return cmethod;
            }
        }
        return null;
    }
    
    public void analyzeBytecode() {
        if (bytecodeClassStore == null) {
            bytecodeClassStore = jproject.registerBytecodeClasses();
            analysisLevel = 3;
        }
    }
    
    CachedJClass getCachedJClass(String fqn) {
        return bytecodeCache.getCachedJClass(fqn);
    }
    
    CachedJMethod getCachedJMethod(String className, String signature) {
        return bytecodeCache.getCachedJMethod(className + JavaElement.QualifiedNameSeparator + signature);
    }
    
    CachedJField getCachdeJMField(String className, String name) {
        return bytecodeCache.getCachedJField(className + JavaElement.QualifiedNameSeparator + name);
    }
}
