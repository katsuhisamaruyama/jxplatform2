/*
 *  Copyright 2019
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.codemanipulation;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodInvocation;
import java.util.List;
import java.util.ArrayList;

/**
 * Collects AST nodes appearing on a CFG.
 * 
 * @author Katsuhisa Maruyama
 */
public class MethodInvocationCollector extends ASTVisitor {
    
    private List<MethodInvocation> nodes = new ArrayList<MethodInvocation>();
    
    public MethodInvocationCollector(ASTNode node) {
        node.accept(this);
    }
    
    public List<MethodInvocation> getNodes() {
        return nodes;
    }
    
    @Override
    public boolean visit(MethodInvocation node) {
        nodes.add(node);
        return true;
    }
}
