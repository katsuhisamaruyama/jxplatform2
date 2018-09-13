/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg;

import org.jtool.eclipse.graph.GraphEdge;
import org.jtool.eclipse.graph.GraphNode;
import org.jtool.eclipse.pdg.PDGNode;
import org.eclipse.jdt.core.dom.ASTNode;
import java.util.Set;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

/**
 * A node of a CFG.
 * 
 * @author Katsuhisa Maruyama
 */
public class CFGNode extends GraphNode {
    
    private ASTNode astNode;
    private Kind kind;
    private PDGNode pdgNode = null;
    private BasicBlock basicBlock = null;
    
    private static long num = 1;
    
    public enum Kind {
        
        classEntry,                 // CFGClassEntry (TypeDeclaration, AnonymousClassDeclaration)
        interfaceEntry,             // CFGClassEntry (TypeDeclaration, AnonymousClassDeclaration)
        enumEntry,                  // CFGClassEntry (EnumDeclaration)
        methodEntry,                // CFGMethodEntry (MethodDeclaration)
        constructorEntry,           // CFGMethodEntry (MethodDeclaration)
        initializerEntry,           // CFGInitializerEntry (Initializer)
        fieldEntry,                 // CFGFieldEntry (VariableDeclarationFragment/FieldDeclaration)
        enumConstantEntry,          // CFGFieldEntry (EnumConstantDeclaration)
        
        classExit,                  // CFGExit
        interfaceExit,              // CFGExit
        enumExit,                   // CFGExit
        methodExit,                 // CFGExit
        constructorExit,            // CFGExit
        initializerExit,            // CFGExit
        fieldExit,                  // CFGExit
        enumConstantExit,           // CFGExit
        
        assignment,                 // CFGStatement (Assignment)
        methodCall,                 // CFGMethodInvocation (MethodInvocation/SuperMethodInvocation)
        constructorCall,            // CFGMethodInvocation (ConstructorInvocation/SuperConstructorInvocation)
        instanceCreation,           // CFGMethodInvocation (InstanceCreation)
        fieldDeclaration,           // CFGStatement (VariableDeclarationFragment)
        enumConstantDeclaration,    // CFGStatement (VariableDeclarationFragment)
        localDeclaration,           // CFGStatement (VariableDeclarationFragment)
        
        assertSt,                   // CFGStatement (AssertStatement)
        breakSt,                    // CFGStatement (BreakStatement)
        continueSt,                 // CFGStatement (ContinueStatement)
        doSt,                       // CFGStatement (DoStatement)
        forSt,                      // CFGStatement (ForStatement)    
        ifSt,                       // CFGStatement (IfStatement)
        returnSt,                   // CFGStatement (ReturnStatement)
        switchCaseSt,               // CFGStatement (SwitchCase)
        switchDefaultSt,            // CFGStatement (SwitchCase)
        whileSt,                    // CFGStatement (WhileStatement)
        emptySt,                    // CFGStatement (EmptyStatement)
        
        labelSt,                    // CFGStatement (Identifier in LabeledStatement)
        switchSt,                   // CFGStatement (SwitchStatement)
        synchronizedSt,             // CFGStatement (SynchronizedStatement)
        throwSt,                    // CFGStatement (ThrowStatement)
        trySt,                      // CFGStatement (TryStatement)
        catchSt,                    // CFGStatement (CatchClause in TryStatement)
        finallySt,                  // CFGStatement (Block in TryStatement)
        
        formalIn,                   // CFGParameter
        formalOut,                  // CFGParameter
        actualIn,                   // CFGParameter
        actualOut,                  // CFGParameter
        
        merge,                      // CFGMerge (for merge)
        dummy;                      // CFGDummy (for dummy)
    }
    
    public CFGNode() {
    }
    
    public CFGNode(ASTNode node, Kind kind) {
        this.astNode = node;
        this.kind = kind;
        
        if (kind == Kind.dummy) {
            super.setId(0);
        } else {
            super.setId(num);
            num++;
        }
    }
    
    public static void resetId() {
        num = 1;
    }
    
    public void setASTNode(ASTNode node) {
        astNode = node;
    }
    
    public ASTNode getASTNode() {
        return astNode;
    }
    
    public void setKind(Kind kind) {
        this.kind = kind;
    }
    
    public Kind getKind() {
        return kind;
    }
    
    public void setPDGNode(PDGNode node) {
        pdgNode = node;
    }
    
    public PDGNode getPDGNode() {
        return pdgNode;
    }
    
    public void setBasicBlock(BasicBlock block) {
        basicBlock = block;
    }
    
    public BasicBlock getBasicBlock() {
        return basicBlock;
    }
    
    public Set<CFGNode> getPredecessors() {
        return convertNodes(getSrcNodes());
    }
    
    public Set<CFGNode> getSuccessors() {
        return convertNodes(getDstNodes());
    }
    
    public int getNumOfPredecessors() {
        return getSrcNodes().size();
    }
    
    public int getNumOfSuccessors() {
        return getDstNodes().size();
    }
    
    public Set<ControlFlow> getIncomingFlows() {
        return convertEdges(getIncomingEdges());
    }
    
    public Set<ControlFlow> getOutgoingFlows() {
        return convertEdges(getOutgoingEdges());
    }
    
    private Set<CFGNode> convertNodes(Set<GraphNode> nodes) {
        Set<CFGNode> set = new HashSet<CFGNode>();
        for (GraphNode node : nodes) {
            set.add((CFGNode)node);
        }
        return set;
    }
    
    private Set<ControlFlow> convertEdges(Set<GraphEdge> edges) {
        Set<ControlFlow> set = new HashSet<ControlFlow>();
        for (GraphEdge edge : edges) {
            set.add((ControlFlow)edge);
        }
        return set;
    }
    
    public boolean isBranch() { 
        return getOutgoingEdges().size() > 1;
    }
    
    public boolean isLoop() {
        return kind == Kind.whileSt || kind == Kind.doSt || kind == Kind.forSt;
    }
    
    public boolean isJoin() {
        return getIncomingEdges().size() > 1;
    }
    
    public boolean isEntry() {
        return kind == Kind.classEntry ||
               kind == Kind.interfaceEntry ||
               kind == Kind.enumEntry ||
               kind == Kind.methodEntry ||
               kind == Kind.constructorEntry ||
               kind == Kind.fieldEntry ||
               kind == Kind.initializerEntry ||
               kind == Kind.enumConstantEntry;
    }
    
    public boolean isClassEntry() {
        return kind == Kind.classEntry;
    }
    
    public boolean isInterfaceEntry() {
        return kind == Kind.interfaceEntry;
    }
    
    public boolean isEnumEntry() {
        return kind == Kind.enumEntry;
    }
    
    public boolean isMethodEntry() {
        return kind == Kind.methodEntry;
    }
    
    public boolean isConstructorEntry() {
        return kind == Kind.constructorEntry;
    }
    
    public boolean isInitializerEntry() {
        return kind == Kind.initializerEntry;
    }
    
    public boolean isFieldEntry() {
        return kind == Kind.fieldEntry;
    }
    
    public boolean isEnumConstantEntry() {
        return kind == Kind.enumConstantEntry;
    }
    
    public boolean isExit() {
        return kind == Kind.classExit ||
               kind == Kind.interfaceExit ||
               kind == Kind.enumExit ||
               kind == Kind.methodExit ||
               kind == Kind.constructorExit ||
               kind == Kind.fieldExit ||
               kind == Kind.initializerExit ||
               kind == Kind.enumConstantExit;
    }
    
    public boolean isAssignment() {
        return kind == Kind.assignment;
    }
    
    public boolean isMethodCall() {
        return kind == Kind.methodCall ||
               kind == Kind.constructorCall ||
               kind == Kind.instanceCreation;
    }
    
    public boolean isFieldDeclaration() {
        return kind == Kind.fieldDeclaration;
    }
    
    public boolean isLocalDeclaration() {
        return kind == Kind.localDeclaration;
    }
    
    public boolean isAssert() {
        return kind == Kind.assertSt;
    }
    
    public boolean isBreak() {
        return kind == Kind.breakSt;
    }
    
    public boolean isContinue() {
        return kind == Kind.continueSt;
    }
    
    public boolean isDo() {
        return kind == Kind.doSt;
    }
    
    public boolean isFor() {
        return kind == Kind.forSt;
    }
    
    public boolean isIf() {
        return kind == Kind.ifSt;
    }
    
    public boolean isReturn() {
        return kind == Kind.returnSt;
    }
    
    public boolean isSwitchCase() {
        return kind == Kind.switchCaseSt;
    }
    
    public boolean isSwitchDefault() {
        return kind == Kind.switchDefaultSt;
    }
    
    public boolean isWhile() {
        return kind == Kind.whileSt;
    }
    
    public boolean isLabel() {
        return kind == Kind.labelSt;
    }
    
    public boolean isSwitch() {
        return kind == Kind.switchSt;
    }
    
    public boolean isSynchronized() {
        return kind == Kind.throwSt;
    }
    
    public boolean isThrow() {
        return kind == Kind.throwSt;
    }
    
    public boolean isTry() {
        return kind == Kind.trySt;
    }
    
    public boolean isCatch() {
        return kind == Kind.catchSt;
    }
    
    public boolean isFinally() {
        return kind == Kind.finallySt;
    }
    
    public boolean isParameter() {
        return kind == Kind.formalIn ||
               kind == Kind.formalOut ||
               kind == Kind.actualIn ||
               kind == Kind.actualOut;
    }
    
    public boolean isFormal() {
        return isFormalIn() || isFormalOut();
    }
    
    public boolean isFormalIn() {
        return kind == Kind.formalIn;
    }
    
    public boolean isFormalOut() {
        return kind == Kind.formalOut;
    }
    
    public boolean isActual() {
        return isActualIn() || isActualOut();
    }
    
    public boolean isActualIn() {
        return kind == Kind.actualIn;
    }
    
    public boolean isActualOut() {
        return kind == Kind.actualOut;
    }
    
    public boolean isStatementNotParameter() {
        return (this instanceof CFGStatement) && !(this instanceof CFGParameter);
    }
    
    public boolean isStatement() {
        return (this instanceof CFGStatement);
    }
    
    public boolean isMerge() {
        return kind == Kind.merge;
    }
    
    public boolean isDummy() {
        return kind == Kind.dummy;
    }
    
    public boolean isNextToBranch() {
        for (CFGNode node : getPredecessors()) {
            if (node.isBranch()) {
                return true;
            }
        }
        return false;
    }
    
    public boolean hasDefVariable() {
        return false;
    }
    
    public boolean hasUseVariable() {
        return false;
    }
    
    public boolean isLeader() {
        return basicBlock != null && equals(basicBlock.getLeader());
    }
    
    public boolean equals(CFGNode node) {
        return super.equals((GraphNode)node);
    }
    
    @Override
    public int hashCode() {
        return super.hashCode();
    }
    
    @Override
    public CFGNode clone() {
        CFGNode cloneNode = new CFGNode(astNode, kind);
        super.setClone(cloneNode);
        setClone(cloneNode);
        return cloneNode;
    }
    
    protected void setClone(CFGNode cloneNode) {
        cloneNode.setPDGNode(pdgNode);
        cloneNode.setBasicBlock(basicBlock);
    }
    
    public static <E extends GraphNode> CFGNode[] toArray(Set<E> set) {
        CFGNode[] nodes = new CFGNode[set.size()];
        int i = 0;
        for (GraphNode node : set) {
            nodes[i++] = (CFGNode)node;
        }
        return nodes;
    }
    
    public void print() {
        System.out.println(toString());
    }
    
    public static List<CFGNode> sortCFGNode(Collection<? extends CFGNode> co) {
        List<CFGNode> nodes = new ArrayList<CFGNode>(co);
        Collections.sort(nodes, new Comparator<CFGNode>() {
            public int compare(CFGNode node1, CFGNode node2) {
                if (node2.id == node1.id) {
                    return 0;
                } else if (node1.id > node2.id) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });
        return nodes;
    }
    
    @Override
    public String toString() {
        if (getKind() != null) {
            return super.getIdString()  + " " + getKind().toString();
        } else {
            return super.getIdString();
        }
    }
}
