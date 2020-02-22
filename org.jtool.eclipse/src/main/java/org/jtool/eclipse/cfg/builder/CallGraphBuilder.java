/*
 *  Copyright 2019
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg.builder;

import org.jtool.eclipse.cfg.CFG;
import org.jtool.eclipse.cfg.CFGNode;
import org.jtool.eclipse.cfg.CFGMethodCall;
import org.jtool.eclipse.cfg.CFGStatement;
import org.jtool.eclipse.cfg.JReference;
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
    
    public static CallGraph getCallGraph(JavaProject jproject) {
        CallGraph callGraph = new CallGraph(jproject.getName());
        for (JavaClass jclass : jproject.getClasses()) {
            CallGraph graph = getCallGraph(jclass);
            callGraph.append(graph);
        }
        return callGraph;
    }
    
    public static CallGraph getCallGraph(JavaClass jclass) {
        CallGraph callGraph = new CallGraph(jclass.getQualifiedName());
        for (JavaMethod jmethod : jclass.getMethods()) {
            CallGraph graph = getCallGraph(jmethod);
            callGraph.append(graph);
        }
        return callGraph;
    }
    
    public static CallGraph getCallGraph(JavaMethod jmethod) {
        CFGStore cfgStore = jmethod.getJavaProject().getCFGStore();
        CallGraph callGraph = new CallGraph(jmethod.getQualifiedName());
        CFG cfg = cfgStore.findCFG(jmethod.getQualifiedName());
        if (cfg == null) {
            return null;
        }
        
        for (CFGNode cfgNode : cfg.getNodes()) {
            if (cfgNode.isMethodCall()) {
                CFGMethodCall call = (CFGMethodCall)cfgNode;
                CFG methodCFG = cfgStore.findCFG(call.getQualifiedName());
                if (methodCFG != null) {
                    ControlFlow flow = new ControlFlow(cfg.getStartNode(), methodCFG.getStartNode());
                    callGraph.add(flow);
                }
            } else if (cfgNode.isStatement()) {
                CFGStatement statement = (CFGStatement)cfgNode;
                for (JReference def : statement.getDefVariables()) {
                    if (def.isFieldAccess()) {
                        CFG fieldCFG = cfgStore.findCFG(def.getQualifiedName());
                        if (fieldCFG != null) {
                            ControlFlow flow = new ControlFlow(cfg.getStartNode(), fieldCFG.getStartNode());
                            callGraph.add(flow);
                        }
                    }
                }
                for (JReference use : statement.getUseVariables()) {
                    if (use.isFieldAccess()) {
                        CFG fieldCFG = cfgStore.findCFG(use.getQualifiedName());
                        if (fieldCFG != null) {
                            ControlFlow flow = new ControlFlow(cfg.getStartNode(), fieldCFG.getStartNode());
                            callGraph.add(flow);
                        }
                    }
                }
            }
        }
        return callGraph;
    }
}
