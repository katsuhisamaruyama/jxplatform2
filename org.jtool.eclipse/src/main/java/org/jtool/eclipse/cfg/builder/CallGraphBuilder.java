/*
 *  Copyright 2019
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg.builder;

import org.jtool.eclipse.cfg.CFG;
import org.jtool.eclipse.cfg.CFGNode;
import org.jtool.eclipse.cfg.CFGMethodCall;
import org.jtool.eclipse.cfg.ControlFlow;
import org.jtool.eclipse.cfg.CallGraph;
import org.jtool.eclipse.javamodel.JavaProject;
import org.jtool.eclipse.javamodel.JavaClass;
import org.jtool.eclipse.javamodel.JavaMethod;

/**
 * Builds a call graph that corresponds to a method, a class, or a project.
 * All methods of this class are not intended to be directly called by clients.
 * 
 * @author Katsuhisa Maruyama
 */
public class CallGraphBuilder {
    
    public static CallGraph getCallGraph(JavaProject jproject, CFGStore cfgStore) {
        CallGraph callGraph = new CallGraph(jproject.getName());
        for (JavaClass jclass : jproject.getClasses()) {
            CallGraph graph = getCallGraph(jclass, cfgStore);
            callGraph.append(graph);
        }
        return callGraph;
    }
    
    public static CallGraph getCallGraph(JavaClass jclass, CFGStore cfgStore) {
        CallGraph callGraph = new CallGraph(jclass.getQualifiedName());
        for (JavaMethod jmethod : jclass.getMethods()) {
            CallGraph graph = getCallGraph(jmethod, cfgStore);
            callGraph.append(graph);
        }
        return callGraph;
    }
    
    public static CallGraph getCallGraph(JavaMethod jmethod, CFGStore cfgStore) {
        CallGraph callGraph = new CallGraph(jmethod.getQualifiedName());
        CFG cfg = cfgStore.getCFG(jmethod);
        if (cfg == null) {
            return null;
        }
        
        for (CFGNode cfgNode : cfg.getNodes()) {
            if (cfgNode.isMethodCall()) {
                CFGMethodCall call = (CFGMethodCall)cfgNode;
                CFG callee = cfgStore.getCFG(call.getQualifiedName());
                if (callee != null) {
                    ControlFlow flow = new ControlFlow(cfg.getStartNode(), callee.getStartNode());
                    callGraph.add(flow);
                }
            }
        }
        return callGraph;
    }
}
