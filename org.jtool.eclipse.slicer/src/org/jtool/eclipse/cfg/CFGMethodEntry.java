/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg;

import org.eclipse.jdt.core.dom.ASTNode;
import java.util.List;
import java.util.ArrayList;

/**
 * The entry node of a CFG for a method or a constructor.
 * @author Katsuhisa Maruyama
 */
public class CFGMethodEntry extends CFGEntry {
    
    private String returnType;
    private String simpleName;
    private List<CFGParameter> formalIns = new ArrayList<CFGParameter>();
    private List<CFGParameter> formalOuts = new ArrayList<CFGParameter>();
    
    protected CFGMethodEntry() {
    }
    
    public CFGMethodEntry(ASTNode node, CFGNode.Kind kind, String simpleName, String name, String fqn) {
        super(node, kind, name, fqn);
        this.simpleName = simpleName;
    }
    
    public void setReturnType(String type) {
        returnType = type;
    }
    
    public String getReturnType() {
        return returnType;
    }
    
    public String getSimpleName() {
        return simpleName;
    }
    
    public boolean isVoidType() {
        return returnType.equals("void");
    }
    
    public void addFormalIn(CFGParameter node) {
        formalIns.add(node);
    }
    
    public void addFormalOut(CFGParameter node) {
        formalOuts.add(node);
    }
    
    public void setFormalIns(List<CFGParameter> params) {
        for (CFGParameter param : params) {
            addFormalIn(param);
        }
    }
    
    public void setFormalOuts(List<CFGParameter> params) {
        for (CFGParameter param : params) {
            addFormalOut(param);
        }
    }
    
    public List<CFGParameter> getFormalIns() {
        return formalIns;
    }
    
    public List<CFGParameter> getFormalOuts() {
        return formalOuts;
    }
    
    public int getParameterSize() {
        return formalIns.size();
    }
    
    public CFGParameter getFormalIn(int ordinal) {
        return formalIns.get(ordinal);
    }
    
    public CFGParameter getFormalOut(int ordinal) {
        return formalOuts.get(ordinal);
    }
    
    public boolean hasParameters() {
        return formalIns.size() != 0;
    }
    
    @Override
    public String toString() {
        return super.toString() + " METHOD [ " + getName() + "]";
    }
}
