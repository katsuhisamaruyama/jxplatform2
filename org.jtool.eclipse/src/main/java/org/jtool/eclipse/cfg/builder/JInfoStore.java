/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg.builder;

import org.jtool.eclipse.cfg.JClass;
import org.jtool.eclipse.cfg.JMethod;
import org.jtool.eclipse.cfg.JField;
import org.jtool.eclipse.javamodel.JavaProject;
import org.jtool.eclipse.javamodel.JavaClass;
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
    private BytecodeClassStore bytecodeClassStore = null;
    
    private JInfoStore() {
    }
    
    public static JInfoStore getInstance() {
        return instance;
    }
    
    public JavaProject getProject() {
        return jproject;
    }
    
    public void build(JavaProject jproject, boolean bytecodeAnalysized, boolean usingBytecodeCache) {
        this.jproject = jproject;
        if (jproject != null) {
            if (bytecodeAnalysized) {
                bytecodeClassStore = jproject.registerBytecodeClasses();
            } else if (usingBytecodeCache) {
                if (!BytecodeCache.loadCache(jproject)) {
                    bytecodeClassStore = jproject.registerBytecodeClasses();
                };
            }
        }
    }
    
    public void writeCache() {
        BytecodeCache.writeCache(jproject, new ArrayList<ExternalJClass>(externalClassStore.values()));
    }
    
    public void clearInternalOnly() {
        internalClassStore.clear();
    }
    
    public void clearAll() {
        clearInternalOnly();
        externalClassStore.clear();
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
            clazz = registerExternalClass(fqn);
        }
        return clazz;
    }
    
    private JClass findClass(String fqn) {
        JClass clazz = internalClassStore.get(fqn);
        if (clazz == null) {
            clazz = externalClassStore.get(fqn);
        }
        return clazz;
    }
    
    public JClass getJClass(String fqn) {
        if (jproject != null) {
            JClass clazz = findClass(fqn);
            if (clazz != null) {
                return clazz;
            }
        } else {
            return UnregisteredJClass.getInstance();
        }
        if (bytecodeClassStore != null) {
            JClass clazz = registerClass(fqn);
            if (clazz != null) {
                return clazz;
            }
        } else {
            return UnregisteredJClass.getInstance();
        }
        return UnregisteredJClass.getInstance();
    }
    
    public JMethod getJMethod(String classFqn, String methodSig) {
        JClass clazz = getJClass(classFqn);
        JMethod method = clazz.getMethod(methodSig);
        if (method != null) {
            if (method.isInProject()) {
                return (InternalJMethod)method;
            } else {
                return (ExternalJMethod)method;
            }
        }
        return UnregisteredJMethod.getInstance();
    }
    
    public JField getJField(String classFqn, String fieldName) {
        JClass clazz = getJClass(classFqn);
        JField field = clazz.getField(fieldName);
        if (field != null) {
            return field;
        }
        return UnregisteredJField.getInstance();
    }
}
