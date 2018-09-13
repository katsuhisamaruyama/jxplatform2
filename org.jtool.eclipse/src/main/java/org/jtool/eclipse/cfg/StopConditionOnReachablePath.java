/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg;

/**
 * Calculates reachable nodes on a CFG.
 * 
 * @author Katsuhisa Maruyama
 */
@FunctionalInterface
public interface StopConditionOnReachablePath {
    
    public boolean isStop(CFGNode node);
}
