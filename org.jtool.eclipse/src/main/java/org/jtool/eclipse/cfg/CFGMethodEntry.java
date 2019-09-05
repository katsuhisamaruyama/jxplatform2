/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg;

import org.jtool.eclipse.javamodel.JavaMethod;
import java.util.List;
import java.util.ArrayList;

/**
 * The entry node of a CFG for a method or a constructor.
 * 
 * @author Katsuhisa Maruyama
 */
public class CFGMethodEntry extends CFGEntry {
    
    private JavaMethod jmethod;
    
    private List<CFGParameter> formalIns = new ArrayList<CFGParameter>();
    private List<CFGParameter> formalOuts = new ArrayList<CFGParameter>();
    private List<CFGNode> catchNodes = new ArrayList<CFGNode>();
    
    protected CFGMethodEntry() {
    }
    
    public CFGMethodEntry(JavaMethod jmethod, CFGNode.Kind kind) {
        super(jmethod.getASTNode(), kind, jmethod.getName(), jmethod.getSignature(), jmethod.getQualifiedName());
        this.jmethod = jmethod;
    }
    
    public JavaMethod getJavaMethod() {
        return jmethod;
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
        if (ordinal < formalIns.size()) {
            return formalIns.get(ordinal);
        } else {
            return null;
        }
    }
    
    public CFGParameter getFormalOut(int ordinal) {
        if (ordinal < formalOuts.size()) {
            return formalOuts.get(ordinal);
        } else {
            return null;
        }
    }
    
    public boolean hasParameters() {
        return formalIns.size() != 0;
    }
    
    public void addCatchNode(String type, CFGNode node) {
        catchNodes.add(node);
    }
    
    public List<CFGNode> getCatchNodes() {
        return catchNodes;
    }
    
    @Override
    public String toString() {
        return super.toString() + " METHOD [ " + getName() + " ]";
    }
}
