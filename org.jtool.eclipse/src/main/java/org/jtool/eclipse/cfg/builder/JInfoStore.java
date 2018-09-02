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
import org.jtool.eclipse.javamodel.builder.BytecodeClassStore;
import org.jtool.eclipse.javamodel.JavaClass;
import javassist.CtClass;
import java.util.Map;
import java.util.HashMap;

/**
 * An object holds a collection of all projects.
 * All methods of this class are not intended to be directly called by clients.
 * 
 * @author Katsuhisa Maruyama
 */
public class JInfoStore {
    
    private static JInfoStore instance = new JInfoStore();
    
    private Map<String, JInternalClass> internalClassStore = new HashMap<String, JInternalClass>();
    private Map<String, JExternalClass> externalClassStore = new HashMap<String, JExternalClass>();
    
    private JavaProject jproject;
    private BytecodeClassStore bytecodeClassStore;
    
    private JInfoStore() {
    }
    
    public static JInfoStore getInstance() {
        return instance;
    }
    
    public void build(JavaProject jproject, boolean bytecodeAnalysized) {
        this.jproject = jproject;
        if (bytecodeAnalysized) {
            bytecodeClassStore = jproject.registerBytecodeClasses();
        } else {
            bytecodeClassStore = null;
        }
    }
    
    public void clearInternalOnly() {
        internalClassStore.clear();
    }
    
    public void clearAll() {
        clearInternalOnly();
        externalClassStore.clear();
    }
    
    private JClass registerClassFromJavaClass(String fqn) {
        if (jproject != null) {
            JavaClass jclass = jproject.getClass(fqn);
            if (jclass != null) {
                JInternalClass clazz = new JInternalClass(jclass);
                if (clazz != null) {
                    internalClassStore.put(clazz.getQualifiedName(), clazz);
                }
                return clazz;
            }
        }
        return null;
    }
    
    private JClass registerClassFromCtClass(String fqn) {
        if (bytecodeClassStore != null) {
            CtClass cclass = bytecodeClassStore.getCtClass(fqn);
            if (cclass != null) {
                JExternalClass clazz = new JExternalClass(cclass);
                if (clazz != null) {
                    externalClassStore.put(clazz.getQualifiedName(), clazz);
                }
                return clazz;
            }
        }
        return null;
    }
    
    private JClass registerJClassFromStore(String fqn) {
        JClass jclass = registerClassFromJavaClass(fqn);
        if (jclass == null) {
            jclass = registerClassFromCtClass(fqn);
        }
        return jclass;
    }
    
    private JClass getJClassFromStore(String fqn) {
        JClass jclass = internalClassStore.get(fqn);
        if (jclass == null) {
            jclass = externalClassStore.get(fqn);
        }
        return jclass;
    }
    
    public JClass getJClass(String fqn) {
        JClass clazz = getJClassFromStore(fqn);
        if (clazz == null) {
            clazz = registerJClassFromStore(fqn);
        }
        return clazz;
    }
    
    public JMethod getJMethod(String classFqn, String methodSig) {
        JClass jclass = getJClass(classFqn);
        if (jclass != null) {
            return jclass.getMethod(methodSig);
        }
        return null;
    }
    
    public JField getJField(String classFqn, String fieldName) {
        JClass jclass = getJClass(classFqn);
        if (jclass != null) {
            return jclass.getField(fieldName);
        }
        return null;
    }
}
