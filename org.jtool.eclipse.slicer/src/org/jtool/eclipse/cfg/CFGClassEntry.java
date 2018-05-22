/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg;

import org.eclipse.jdt.core.dom.ASTNode;
import java.util.Set;
import java.util.HashSet;

/**
 * The entry node of a CFG for a class or an interface.
 * @author Katsuhisa Maruyama
 */
public class CFGClassEntry extends CFGEntry {
    
    private Set<CFG> methods = new HashSet<CFG>();
    private Set<CFG> fields = new HashSet<CFG>();
    private Set<CFG> types = new HashSet<CFG>();
    
    protected CFGClassEntry() {
    }
    
    public CFGClassEntry(ASTNode node, CFGNode.Kind kind, String name, String fqn) {
        super(node, kind, name, fqn);
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
    
    public void addType(CFG cfg) {
        types.add(cfg);
    }
    
    public Set<CFG> getTypes() {
        return types;
    }
    
    @Override
    public String toString() {
        return super.toString() + " CLASS [ " + getName() + "]";
    }
}
