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
 * A node that represents a method call node within a CFG.
 * @author Katsuhisa Maruyama
 */
public class CFGMethodCall extends CFGStatement {
    
    private JMethodCall jmethodCall;
    
    private List<CFGParameter> actualIns = new ArrayList<CFGParameter>();
    private List<CFGParameter> actualOuts = new ArrayList<CFGParameter>();
    
    protected CFGMethodCall() {
        super();
    }
    
    public CFGMethodCall(ASTNode node, JMethodCall jcall, CFGNode.Kind kind) {
        super(node, kind);
        jmethodCall = jcall;
    }
    
    public JMethodCall getMethodCall() {
        return jmethodCall;
    }
    
    public boolean hasPrimary() {
        return jmethodCall.hasPrimary();
    }
    
    public JVariable getPrimary() {
        return jmethodCall.getPrimary();
    }
    
    public String getPrimaryType() {
        return jmethodCall.getPrimaryType();
    }
    
    public String getName() {
        return jmethodCall.getName();
    }
    
    public String getSignature() {
        return jmethodCall.getSignature();
    }
    
    public String getQualifiedName() {
        return jmethodCall.getQualifiedName();
    }
    
    public String getDeclaringClassName() {
        return jmethodCall.getDeclaringClassName();
    }
    
    public String getReturnType() {
        return jmethodCall.getType();
    }
    
    public boolean isPrimitiveType() {
        return jmethodCall.isPrimitiveType();
    }
    
    public boolean isVoidType() {
        return jmethodCall.isVoidType();
    }
    
    public void addActualIn(CFGParameter node) {
        actualIns.add(node);
    }
    
    public void addActualOut(CFGParameter node) {
        actualOuts.add(node);
    }
    
    public void setActualIns(List<CFGParameter> nodes) {
        actualIns.addAll(nodes);
    }
    
    public void setActualOuts(List<CFGParameter> nodes) {
        actualOuts.addAll(nodes);
    }
    
    public List<CFGParameter> getActualIns() {
        return actualIns;
    }
    
    public List<CFGParameter> getActualOuts() {
        return actualOuts;
    }
    
    public int getParameterSize() {
        return actualIns.size();
    }
    
    public CFGParameter getActualIn(int pos) {
        return actualIns.get(pos);
    }
    
    public CFGParameter getActualOut(int pos) {
        return actualOuts.get(pos);
    }
    
    public boolean hasParameters() {
        return actualIns.size() != 0;
    }
    
    public boolean hasReturnValue() {
        return actualOuts.size() != 0;
    }
    
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(super.toString());
        buf.append(" CALL = " + jmethodCall.getSignature());
        return buf.toString();
    }
}
