/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.pdg;

import org.jtool.eclipse.cfg.CFGStatement;
import org.jtool.eclipse.cfg.JReference;
import java.util.List;

/**
 * A statement node of a PDG, which stores defined and used variables.
 * 
 * @author Katsuhisa Maruyama
 */
public class PDGStatement extends PDGNode {
    
    protected PDGStatement() {
        super();
    }
    
    public PDGStatement(CFGStatement node) {
        super(node);
    }
    
    public CFGStatement getCFGStatement() {
        return (CFGStatement)getCFGNode();
    }
    
    public List<JReference> getDefVariables() {
        return getCFGStatement().getDefVariables();
    }
    
    public List<JReference> getUseVariables() {
        return getCFGStatement().getUseVariables();
    }
    
    public boolean definesVariable(JReference jvar) {
        return getCFGStatement().getDefVariables().contains(jvar);
    }
    
    public boolean usesVariable(JReference jvar) {
        return getCFGStatement().getUseVariables().contains(jvar);
    }
    
    @Override
    public String toString() {
        return getCFGStatement().toString();
    }
}
