/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.javamodel.builder;

import javassist.CtClass;
import javassist.NotFoundException;
import java.util.Set;
import java.util.HashSet;

/**
 * An object that represents a class restored from its byte-code.
 * This class uses Javassit modules.
 * 
 * All methods of this class are not intended to be directly called by clients.
 * 
 * @author Katsuhisa Maruyama
 */

class BytecodeClassInfo {
    
    private CtClass ctClass;
    private Set<BytecodeClassInfo> parents = new HashSet<BytecodeClassInfo>();
    private Set<BytecodeClassInfo> children = new HashSet<BytecodeClassInfo>();
    
    private Set<CtClass> ancestors = new HashSet<CtClass>();
    private Set<CtClass> descendants = new HashSet<CtClass>();
    
    BytecodeClassInfo(CtClass ctClass) {
        this.ctClass = ctClass;
        
        try {
            CtClass superClass = ctClass.getSuperclass();
            if (superClass != null) {
                parents.add(new BytecodeClassInfo(superClass));
            }
            for (CtClass c : ctClass.getInterfaces()) {
                parents.add(new BytecodeClassInfo(c));
            }
        } catch (NotFoundException e) { /* empty */ }
    }
    
    String getName() {
        return ctClass.getName();
    }
    
    CtClass getCtClass() {
        return ctClass;
    }
    
    Set<BytecodeClassInfo> getParents() {
        return parents;
    }
    
    Set<BytecodeClassInfo> getChildren() {
        return children;
    }
    
    void addChild(BytecodeClassInfo child) {
        children.add(child);
    }
    
    Set<CtClass> getAncestors() {
        return ancestors;
    }
    
    void setAncestors(BytecodeClassInfo classInfo) {
        for (BytecodeClassInfo parent : classInfo.getParents()) {
            ancestors.add(parent.getCtClass());
            setAncestors(parent);
        }
    }
    
    Set<CtClass> getDescendants() {
        return descendants;
    }
    
    void setDescendants(BytecodeClassInfo classInfo) {
        for (BytecodeClassInfo child : classInfo.getChildren()) {
            descendants.add(child.getCtClass());
            setDescendants(child);
        }
    }
}
