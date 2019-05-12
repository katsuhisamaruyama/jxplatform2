/*
 *  Copyright 2018-2019
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.pdg;

import org.jtool.eclipse.cfg.CFGEntry;
import org.jtool.eclipse.cfg.CFG;
import org.jtool.eclipse.cfg.CommonCFG;

/**
 * An object storing information about a program dependence graph (PDG).
 * 
 * @author Katsuhisa Maruyama
 */
public class PDG extends CommonPDG {
    
    public PDG() {
        super();
    }
    
    public CFG getCFG() {
        CFGEntry node = (CFGEntry)entry.getCFGNode();
        CommonCFG cfg = node.getCFG();
        if (cfg instanceof CFG) {
            return (CFG)cfg;
        }
        return null;
    }
    
    public void append(PDG pdg) {
        for (PDGNode node : pdg.getNodes()) {
            add(node);
        }
        for (Dependence edge : pdg.getEdges()) {
            add(edge);
        }
    }
}
