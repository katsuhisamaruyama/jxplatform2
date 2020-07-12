/*
 *  Copyright 2018-2020
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg.builder;

import org.jtool.eclipse.cfg.JReference;
import org.jtool.eclipse.cfg.CFG;
import org.jtool.eclipse.cfg.CFGNode;
import org.jtool.eclipse.cfg.CFGStatement;
import org.jtool.eclipse.javamodel.JavaMethod;
import org.jtool.eclipse.javamodel.JavaField;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

/**
 * An object that represents a method inside the project.
 * All methods of this class are not intended to be directly called by clients.
 * 
 * @author Katsuhisa Maruyama
 */
public class JMethodInternal extends JMethod {
    
    protected JavaMethod jmethod;
    
    JMethodInternal(JavaMethod jmethod, JClass declaringClass, CFGStore cfgStore) {
        super(jmethod.getQualifiedName(), jmethod.getDeclaringClass().getQualifiedName(), jmethod.getSignature(),
              jmethod.getModifiers(), jmethod.getSignature(), jmethod.isPrimitiveReturnType(), cfgStore);
        this.declaringClass = declaringClass;
        this.jmethod = jmethod;
    }
    
    @Override
    public boolean isInProject() {
        return true;
    }
    
    @Override
    protected JMethod[] findAccessedMethods() {
        Set<JMethod> methods = new HashSet<>();
        for (JavaMethod jm : jmethod.getCalledMethods()) {
            JClass clazz = cfgStore.getJInfoStore().getJClass(jm.getDeclaringClass().getQualifiedName());
            if (clazz != null) {
                JMethod method = clazz.getMethod(jm.getSignature());
                if (method != null) {
                    methods.add(method);
                }
            }
        }
        return methods.toArray(new JMethod[methods.size()]);
    }
    
    @Override
    protected JField[] findAccessedFields() {
        Set<JField> fields = new HashSet<>();
        for (JavaField jf : jmethod.getAccessedFields()) {
            JClass clazz = cfgStore.getJInfoStore().getJClass(jf.getDeclaringClass().getQualifiedName());
            if (clazz != null) {
                JField field = clazz.getField(jf.getName());
                if (field != null) {
                    fields.add(field);
                }
            }
        }
        return fields.toArray(new JField[fields.size()]);
    }
    
    @Override
    protected JMethod[] findOverridingMethods() {
        List<JMethod> methods = new ArrayList<>();
        for (JavaMethod jm : jmethod.getOverridingMethods()) {
            JClass clazz = cfgStore.getJInfoStore().getJClass(jm.getDeclaringClass().getQualifiedName());
            if (clazz != null) {
                JMethod method = clazz.getMethod(jm.getSignature());
                if (method != null) {
                    methods.add(method);
                }
            }
        }
        return methods.toArray(new JMethod[methods.size()]);
    }
    
    @Override
    protected JMethod[] findOverriddenMethods() {
        List<JMethod> methods = new ArrayList<>();
        for (JavaMethod jm : jmethod.getOverriddenMethods()) {
            JClass clazz = cfgStore.getJInfoStore().getJClass(jm.getDeclaringClass().getQualifiedName());
            if (clazz != null) {
                JMethod method = clazz.getMethod(jm.getSignature());
                if (method != null) {
                    methods.add(method);
                }
            }
        }
        return methods.toArray(new JMethod[methods.size()]);
    }
    
    @Override
    protected void findDefUseFieldsInThisMethod(Set<JMethod> visited, boolean recursivelyCollect) {
        Set<JMethod> current = new HashSet<>(visited);
        
        CFG cfg = cfgStore.getCFG(jmethod, visited, false);
        for (CFGNode node : cfg.getNodes()) {
            if (node instanceof CFGStatement) {
                CFGStatement stNode = (CFGStatement)node;
                for (JReference var : stNode.getDefVariables()) {
                    if (var.isFieldAccess()) {
                        addDefField(var.getQualifiedName());
                    }
                }
                for (JReference var : stNode.getUseVariables()) {
                    if (var.isFieldAccess()) {
                        addUseField(var.getQualifiedName());
                    }
                }
            }
        }
        
        if (recursivelyCollect) {
            for (JMethod method : visited) {
                if (!current.contains(method)) {
                    addDefFields(method.getDefFields());
                    addUseFields(method.getUseFields());
                }
            }
        }
    }
    
    @Override
    protected void findDefUseFieldsInAccessedMethods(Set<JMethod> visited, boolean recursivelyCollect) {
        Set<JMethod> current = new HashSet<>(visited);
        
        for (JMethod method : getOverridingMethods()) {
            if (!visited.contains(method)) {
                method.findDefUseFields(visited, true);
            }
        }
        
        for (JMethod method : visited) {
            if (!current.contains(method)) {
                addDefFields(method.getDefFields());
                addUseFields(method.getUseFields());
            }
        }
    }
}
