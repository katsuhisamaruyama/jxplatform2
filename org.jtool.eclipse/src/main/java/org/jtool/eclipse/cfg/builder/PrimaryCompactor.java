/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg.builder;

import org.jtool.eclipse.cfg.CFG;
import org.jtool.eclipse.cfg.CFGNode;
import org.jtool.eclipse.cfg.CFGMethodCall;
import org.jtool.eclipse.cfg.JFieldReference;
import org.jtool.eclipse.cfg.JReference;
import java.util.List;
import java.util.ArrayList;

/**
 * Compacts accesses to fields outside the project.
 * All methods of this class are not intended to be directly called by clients.
 * 
 * @author Katsuhisa Maruyama
 */
class PrimaryCompactor {
    
    protected static final String ExternalFieldAccess = "$";
    
    static void compact(CFG cfg) {
        for (CFGNode node : cfg.getNodes()) {
            if (node.isMethodCall()) {
                CFGMethodCall callNode = (CFGMethodCall)node;
                
                List<JReference> vars = new ArrayList<JReference>(callNode.getDefVariables());
                int count = 0;
                for (JReference var : vars) {
                    if (!var.isInProject()) {
                        callNode.getDefVariables().remove(var);
                        count++;
                    }
                }
                
                if (count > 0) {
                    JReference primary = callNode.getPrimary();
                    if (primary != null) {
                        callNode.addDefVariable(primary);
                    }
                    
                    String type = callNode.getDeclaringClassName();
                    JReference var = new JFieldReference(primary.getASTNode(), type, ExternalFieldAccess, type, false, false);
                    callNode.addDefVariable(var);
                }
            }
        }
    }
}
