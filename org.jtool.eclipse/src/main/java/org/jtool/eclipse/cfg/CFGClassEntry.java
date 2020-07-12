/*
 *  Copyright 2018-2020
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg;

import org.jtool.eclipse.javamodel.JavaClass;
import java.util.Set;
import java.util.HashSet;

/**
 * The entry node of a CFG for a class or an interface.
 * 
 * @author Katsuhisa Maruyama
 */
public class CFGClassEntry extends CFGEntry {
    
    private JavaClass jclass;
    
    private Set<CFG> methods = new HashSet<>();
    private Set<CFG> fields = new HashSet<>();
    private Set<CCFG> types = new HashSet<>();
    
    public CFGClassEntry(JavaClass jclass, CFGNode.Kind kind) {
        super(jclass.getASTNode(), kind, jclass.getName(), jclass.getQualifiedName(), jclass.getQualifiedName());
        this.jclass = jclass;
    }
    
    public JavaClass getJavaClass() {
        return jclass;
    }
    
    public void addMethod(CFG cfg) {
        methods.add(cfg);
    }
    
    public Set<CFG> getMethods() {
        return methods;
    }
    
    public void addField(CFG cfg) {
        fields.add(cfg);
    }
    
    public Set<CFG> getFields() {
        return fields;
    }
    
    public void addType(CCFG ccfg) {
        types.add(ccfg);
    }
    
    public Set<CCFG> getTypes() {
        return types;
    }
    
    @Override
    public String toString() {
        return super.toString() + " CLASS [ " + getName() + " ]";
    }
}
