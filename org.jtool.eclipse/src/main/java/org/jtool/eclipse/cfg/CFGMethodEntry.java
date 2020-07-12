/*
 *  Copyright 2018-2020
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
    
    private List<CFGParameter> formalIns = new ArrayList<>();
    private List<CFGParameter> formalOuts = new ArrayList<>();
    private CFGParameter formalOutForReturn = null;
    private List<CFGCatch> exceptionNodes = new ArrayList<>();
    
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
        formalIns.addAll(params);
    }
    
    public void setFormalOuts(List<CFGParameter> params) {
        formalOuts.addAll(params);
    }
    
    public void setFormalOutForReturn(CFGParameter param) {
        formalOutForReturn = param;
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
        return (ordinal < formalIns.size()) ? formalIns.get(ordinal) : null;
    }
    
    public CFGParameter getFormalOut(int ordinal) {
        return (ordinal < formalOuts.size())? formalOuts.get(ordinal) : null;
    }
    
    public CFGParameter getFormalOutForReturn() {
        return formalOutForReturn;
    }
    
    public boolean hasParameters() {
        return formalIns.size() != 0;
    }
    
    public void addExceptionNode(CFGCatch node) {
        exceptionNodes.add(node);
    }
    
    public List<CFGCatch> getExceptionNodes() {
        return exceptionNodes;
    }
    
    public CFGCatch getExceptionNode(String type) {
        return exceptionNodes
                .stream()
                .filter(node -> node.getTypeName().equals(type)).findFirst().orElse(null);
    }
    
    @Override
    public String toString() {
        return super.toString() + " METHOD [ " + getSignature() + " ]";
    }
}
