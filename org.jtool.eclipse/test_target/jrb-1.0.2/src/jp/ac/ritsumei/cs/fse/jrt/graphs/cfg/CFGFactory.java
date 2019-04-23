/*
 *     CFGFactory.java  Dec 2, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.graphs.cfg;
import jp.ac.ritsumei.cs.fse.jrt.graphs.util.*;
import jp.ac.ritsumei.cs.fse.jrt.model.*;
import jp.ac.ritsumei.cs.fse.jrt.parser.SimpleVisitor;
import jp.ac.ritsumei.cs.fse.jrt.parser.SimpleNode;
import jp.ac.ritsumei.cs.fse.jrt.parser.ast.*;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Stack;
 
public class CFGFactory extends SimpleVisitor implements GraphNodeSort {
    private static CFGFactory factory = new CFGFactory();
    private JavaFile jfile;
    private CFGNode prevNode;
    private CFGNode nextNode = new CFGNode();
    private Stack loopEntries = new Stack();
    private Stack loopExits = new Stack();
    private HashMap labels = new HashMap();
    private int formals, actuals;

    private CFGFactory() {
        super();
    }

    public static CFGFactory getInstance() {
        return factory;
    }

    private void reconnect(CFGNode node, Graph graph) {
        GraphComponentSet edges = new GraphComponentSet(nextNode.getIncomingEdges());
        Iterator it = edges.iterator();
        while (it.hasNext()) {
            Flow edge = (Flow)it.next();
            edge.setDstNode(node);
        }
        graph.add(node);
        nextNode.clear();
        prevNode = node;
    }

    private void reconnect(CFGNode onode, CFGNode node, Graph graph) {
        GraphComponentSet edges = new GraphComponentSet(onode.getIncomingEdges());
        Iterator it = edges.iterator();
        while (it.hasNext()) {
            Flow edge = (Flow)it.next();
            edge.setDstNode(node);
        }
        graph.add(node);
        onode.clear();
        prevNode = node;
    }

    private Flow createFlow(CFGNode src, CFGNode dst, Graph graph) {
        Flow edge = new Flow(src, dst);
        graph.add(edge);
        return edge;
    }

    private void print(String message) {
        System.out.println(message);
    }

    public Object visit(ASTCompilationUnit node, Object data) {
        jfile = (JavaFile)data;
        String name = jfile.getName();
        node.childrenAccept(this, data);
        return data;
    }

    public Object visit(ASTUnmodifiedClassDeclaration node, Object data) {
        JavaClass jclass = node.getJavaClass();
        CCFG ccfg = createCCFG(jclass);
        node.childrenAccept(this, ccfg);
        return data;
    }

    public Object visit(ASTUnmodifiedInterfaceDeclaration node, Object data) {
        JavaClass jclass = node.getJavaClass();
        CCFG ccfg = createCCFG(jclass);
        node.childrenAccept(this, ccfg);
        return data;
    }

    private CCFG createCCFG(JavaClass jclass) {
        CCFG ccfg = new CCFG();
        jclass.setCCFG(ccfg);

        CFGClassEntryNode curNode = new CFGClassEntryNode(classEntry, jclass);
        reconnect(curNode, ccfg);
        ccfg.setStartNode(curNode);

        Flow edge = createFlow(curNode, nextNode, ccfg);
        edge.setTrue();
        return ccfg;
    }

    public Object visit(ASTClassBody node, Object data) {
        CCFG ccfg = (CCFG)data;

        CFGBranchNode curNode = new CFGBranchNode(whileSt, new JavaStatement());
        reconnect(curNode, ccfg);

        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            Flow edge = createFlow(curNode, nextNode, ccfg);
            edge.setTrue();
            
            node.childAccept(this, data, i);
            
            edge = ccfg.getFlow(prevNode, nextNode);
            edge.setLoopBack(true);
            reconnect(curNode, ccfg);
        }

        CFGExitNode exitNode = new CFGExitNode(classExit);
        Flow edge = createFlow(curNode, exitNode, ccfg);
        edge.setFalse();
        ccfg.setEndNode(exitNode);
        ccfg.add(exitNode);

        return data;
    }        

    public Object visit(ASTClassBodyDeclaration node, Object data) {
        if (!(node.jjtGetChild(0) instanceof ASTNestedClassDeclaration)
          && !(node.jjtGetChild(0) instanceof ASTNestedInterfaceDeclaration)) {
            node.childrenAccept(this, data);
        }
        return data;
    }

    public Object visit(ASTVariableDeclarator node, Object data) {
        Graph graph = (Graph)data;  // CCFG or CFG
        JavaStatement jst = node.getJavaStatement();

        JavaVariable jvar = jst.getDeclaration();
        if (jvar.isField()) {
            jst.addUseVariable(jvar);
        }

        node.childrenAccept(this, data);

        CFGAssignmentNode curNode = new CFGAssignmentNode(variableDecl, jst);
        reconnect(curNode, graph);
        Flow edge = createFlow(curNode, nextNode, graph);
        edge.setTrue();

        return data;
    }

    public Object visit(ASTMethodDeclaration node, Object data) {
        CFG cfg = new CFG();
        CCFG ccfg = (CCFG)data;
        ccfg.add(cfg);

        CFGExitNode exitNode = new CFGExitNode(methodExit);
        cfg.setEndNode(exitNode);
        ccfg.add(cfg.getEndNode());

        node.childrenAccept(this, cfg);
        reconnect(exitNode, cfg);
        ccfg.add(cfg.getStartNode());

        if (node.getFormalOut() != null) {
            CFGParameterNode formalOutNode = createFormalOut(node, cfg);
            reconnect(exitNode, formalOutNode, cfg);
            cfg.getStartNode().addFormalOut(formalOutNode);
            reconnect(exitNode, cfg);
        }

        Flow edge = createFlow(exitNode, nextNode, ccfg);
        edge.setTrue();

        cfg.setSrcDstNodes();
        // cfg.createBasicBlock();
        return data;
    }

    public Object visit(ASTMethodDeclarator node, Object data) {
        CFG cfg = (CFG)data;
        JavaMethod jmethod = node.getJavaMethod();
        jmethod.setCFG(cfg);

        CFGMethodEntryNode curNode = new CFGMethodEntryNode(methodEntry, jmethod);
        reconnect(curNode, cfg);
        cfg.setStartNode(curNode);

        Flow edge = createFlow(curNode, nextNode, cfg);
        edge.setTrue();
        createFormalIns(jmethod, cfg);

        node.childrenAccept(this, data);
        return data;
    }

    public Object visit(ASTConstructorDeclaration node, Object data) {
        CFG cfg = new CFG();
        CCFG ccfg = (CCFG)data;
        ccfg.add(cfg);
        JavaMethod jmethod = node.getJavaMethod();
        jmethod.setCFG(cfg);

        CFGMethodEntryNode curNode = new CFGMethodEntryNode(constructorEntry, jmethod);
        reconnect(curNode, cfg);
        cfg.setStartNode(curNode);
        ccfg.add(cfg.getStartNode());

        Flow edge = createFlow(curNode, nextNode, cfg);
        edge.setTrue();
        createFormalIns(jmethod, cfg);

        node.childrenAccept(this, cfg);

        CFGExitNode exitNode = new CFGExitNode(constructorExit);
        reconnect(exitNode, cfg);
        cfg.setEndNode(exitNode);
        ccfg.add(cfg.getEndNode());
        edge = createFlow(exitNode, nextNode, ccfg);
        edge.setTrue();

        cfg.setSrcDstNodes();
        // cfg.createBasicBlock();
        return data;
    }

    private void createFormalIns(JavaMethod jmethod, CFG cfg) {
        formals = 0;
        Iterator it = jmethod.getParameters().iterator();
        while (it.hasNext()) {
            JavaStatement jst = (JavaStatement)it.next();
            JavaVariable def = jst.getDefVariables().getFirst();
            JavaVariable use = new JavaVariable(def);
            use.setName(def.getName() + "_in");
            jst.addUseVariable(use);

            CFGParameterNode curNode = new CFGParameterNode(formalIn, jst, formals++);
            reconnect(curNode, cfg);
            cfg.getStartNode().addFormalIn(curNode);
            Flow edge = createFlow(curNode, nextNode, cfg);
            edge.setTrue();
        }
    }

    private CFGParameterNode createFormalOut(ASTMethodDeclaration node, CFG cfg) {
        JavaStatement jst = new JavaStatement();
        JavaVariable use = node.getFormalOut();
        jst.addUseVariable(use);
        JavaVariable def = new JavaVariable(use);
        def.setName(use.getName() + "_out");
        jst.addDefVariable(def);

        CFGParameterNode curNode = new CFGParameterNode(formalOut, jst, formals++);
        reconnect(curNode, cfg);
        Flow edge = createFlow(curNode, nextNode, cfg);
        edge.setTrue();
        return curNode;
    }

    public Object visit(ASTExpression node, Object data) {
        Graph graph = (Graph)data;
        JavaStatement jst = node.getJavaStatement();

        node.childrenAccept(this, data);

        if (!jst.getDefVariables().isEmpty()) {
            CFGAssignmentNode curNode = new CFGAssignmentNode(assignmentSt, jst);
            reconnect(curNode, graph);
            Flow edge = createFlow(curNode, nextNode, graph);
            edge.setTrue();
        }
        return data;
    }

    public Object visit(ASTPreIncrementExpression node, Object data) {
        Graph graph = (Graph)data;
        JavaStatement jst = node.getJavaStatement();

        node.childrenAccept(this, data);

        if (!jst.getDefVariables().isEmpty()) {
            CFGAssignmentNode curNode = new CFGAssignmentNode(assignmentSt, jst);
            reconnect(curNode, graph);
            Flow edge = createFlow(curNode, nextNode, graph);
            edge.setTrue();
        }
        return data;
    }

    public Object visit(ASTPreDecrementExpression node, Object data) {
        Graph graph = (Graph)data;
        JavaStatement jst = node.getJavaStatement();

        node.childrenAccept(this, data);

        if (!jst.getDefVariables().isEmpty()) {
            CFGAssignmentNode curNode = new CFGAssignmentNode(assignmentSt, jst);
            reconnect(curNode, graph);
            Flow edge = createFlow(curNode, nextNode, graph);
            edge.setTrue();
        }
        return data;
    }

    public Object visit(ASTPostfixExpression node, Object data) {
        Graph graph = (Graph)data;
        JavaStatement jst = node.getJavaStatement();

        node.childrenAccept(this, data);

        if (!jst.getDefVariables().isEmpty()) {
            CFGAssignmentNode curNode = new CFGAssignmentNode(assignmentSt, jst);
            reconnect(curNode, graph);
            Flow edge = createFlow(curNode, nextNode, graph);
            edge.setTrue();
        }
        return data;
    }

    public Object visit(ASTArguments node, Object data) {
        Graph graph = (Graph)data;  // CCFG or CFG
        JavaStatement jst = node.getJavaStatement();

        CFGCallNode curNode = new CFGCallNode(methodCall, jst);
        curNode.setName(node.getName());
        createActualIns(node, graph, curNode);
        curNode.setCalledSummaryMethod(node.getCalledMethod());

        reconnect(curNode, graph);

        Flow edge = createFlow(curNode, nextNode, graph);
        edge.setTrue();
        node.childrenAccept(this, data);

        if (node.getActualOut() != null) {
            CFGParameterNode actualOut = createActualOut(node, graph);
            curNode.addActualOut(actualOut);
        }
        return data;
    }

    public void createActualIns(ASTArguments node, Graph graph, CFGCallNode callNode) {
        actuals = 0;
        Iterator it = node.getArguments().iterator();
        while (it.hasNext()) {
            JavaStatement jst = (JavaStatement)it.next();

            CFGParameterNode curNode = new CFGParameterNode(actualIn, jst, actuals++);
            reconnect(curNode, graph);
            callNode.addActualIn(curNode);
            Flow edge = createFlow(curNode, nextNode, graph);
            edge.setTrue();
        }
    }

    private CFGParameterNode createActualOut(ASTArguments node, Graph graph) {
        JavaStatement jst = new JavaStatement();
        JavaVariable def = node.getActualOut();
        jst.addDefVariable(def);
        JavaVariable use = new JavaVariable(def);
        use.setName("$_out");
        jst.addUseVariable(use);

        CFGParameterNode curNode = new CFGParameterNode(actualOut, jst, actuals++);
        reconnect(curNode, graph);
        Flow edge = createFlow(curNode, nextNode, graph);
        edge.setTrue();
        return curNode;
    }

    public Object visit(ASTLabeledStatement node, Object data) {
        CFG cfg = (CFG)data;
        CFGNode curNode = new CFGNode(labelSt);
        reconnect(curNode, cfg);
        labels.put(node.getName(), curNode);
        Flow edge = createFlow(curNode, nextNode, cfg);
        edge.setTrue();

        node.childrenAccept(this, data);
        return data;
    }

    public Object visit(ASTStatementExpression node, Object data) {
        CFG cfg = (CFG)data;
        JavaStatement jst = node.getJavaStatement();

        node.childrenAccept(this, data);

        if (!jst.getDefVariables().isEmpty()) {
            CFGAssignmentNode curNode = new CFGAssignmentNode(assignmentSt, jst);
            reconnect(curNode, cfg);
            Flow edge = createFlow(curNode, nextNode, cfg);
            edge.setTrue();
        }
        return data;
    }

    public Object visit(ASTSwitchStatement node, Object data) {
        CFG cfg = (CFG)data;
        JavaStatement jst = node.getJavaStatement();
        Flow entryEdge = cfg.getFlow(prevNode, nextNode);
        node.childAccept(this, data, 0);

        CFGNode curNode = new CFGStatementNode(switchSt, jst);
        CFGNode exitNode = new CFGNode();
        reconnect(curNode, cfg);
        Flow edge = createFlow(curNode, nextNode, cfg);
        edge.setTrue();

        CFGNode entryNode = (CFGNode)entryEdge.getDstNode();
        loopEntries.push(entryNode);  // for continue
        loopExits.push(exitNode);     // for break
        for (int i = 1; i < node.jjtGetNumChildren(); i++) {
            node.childAccept(this, data, i);
        }

        nextNode.addIncomingEdges(exitNode.getIncomingEdges());
        CFGNode mergeNode = new CFGMergeNode(curNode);
        reconnect(mergeNode, cfg);
        loopEntries.pop();
        loopExits.pop();

        edge = createFlow(mergeNode, nextNode, cfg);
        edge.setTrue();
        return data;
    }

    public Object visit(ASTSwitchLabel node, Object data) {
        CFG cfg = (CFG)data;
        JavaStatement jst = node.getJavaStatement();

        CFGBranchNode curNode = new CFGBranchNode(switchLabel, jst);
        reconnect(curNode, cfg);
        Flow edge = createFlow(curNode, nextNode, cfg);
        edge.setTrue();

        Iterator it = cfg.getFlowsTo(curNode).iterator();
        while (it.hasNext()) {
            edge = (Flow)it.next();
            if (edge.isTrue() && edge.getSrcNode().getSort() != switchSt) {
                edge.setDstNode(nextNode);
            }
        }

        node.childrenAccept(this, data);

        if (!node.isDefaultLabel()) {
            edge = createFlow(curNode, nextNode, cfg);
            edge.setFalse();
        }
        return data;
    }

    public Object visit(ASTIfStatement node, Object data) {
        CFG cfg = (CFG)data;
        JavaStatement jst = node.getJavaStatement();

        node.childAccept(this, data, 0);

        CFGBranchNode curNode = new CFGBranchNode(ifSt, jst);
        reconnect(curNode, cfg);
        Flow edge = createFlow(curNode, nextNode, cfg);
        edge.setTrue();

        node.childAccept(this, data, 1);

        Flow mergeEdge = cfg.getFlow(prevNode, nextNode);
        edge = createFlow(curNode, nextNode, cfg);
        edge.setFalse();
        if (node.jjtGetNumChildren() == 3) {
            node.childAccept(this, data, 2);
            mergeEdge.setDstNode(nextNode);
        }

        CFGNode mergeNode = new CFGMergeNode(curNode);
        reconnect(mergeNode, cfg);
        edge = createFlow(mergeNode, nextNode, cfg);
        edge.setTrue();

        return data;
    }

    public Object visit(ASTWhileStatement node, Object data) {
        CFG cfg = (CFG)data;
        JavaStatement jst = node.getJavaStatement();
        Flow entryEdge = cfg.getFlow(prevNode, nextNode);

        node.childAccept(this, data, 0);

        CFGBranchNode curNode = new CFGBranchNode(whileSt, jst);
        CFGNode exitNode = new CFGNode();
        reconnect(curNode, cfg);
        Flow edge = createFlow(curNode, nextNode, cfg);
        edge.setTrue();

        CFGNode entryNode = (CFGNode)entryEdge.getDstNode();
        loopEntries.push(entryNode);  // for continue
        loopExits.push(exitNode);     // for break

        node.childAccept(this, data, 1);

        Flow mergeEdge = cfg.getFlow(prevNode, nextNode);
        mergeEdge.setDstNode(entryNode);
        if (mergeEdge.isFallThrough()) {
            mergeEdge.setLoopBack(true);

            mergeEdge = createFlow(prevNode, entryNode, cfg);
            mergeEdge.setTrue();
        }
        mergeEdge.setLoopBack(true);

        edge = createFlow(curNode, nextNode, cfg);
        edge.setFalse();
        prevNode = entryNode;
        nextNode.addIncomingEdges(exitNode.getIncomingEdges());

        loopEntries.pop();
        loopExits.pop();
        return data;
    }

    public Object visit(ASTDoStatement node, Object data) {
        CFG cfg = (CFG)data;
        JavaStatement jst = node.getJavaStatement();

        CFGNode exitNode = new CFGNode();
        loopExits.push(exitNode);  // for break
        Flow entryEdge = cfg.getFlow(prevNode, nextNode);

        node.childrenAccept(this, data);

        CFGBranchNode curNode = new CFGBranchNode(doSt, jst);
        reconnect(curNode, cfg);
        CFGNode entryNode = (CFGNode)entryEdge.getDstNode();
        Flow edge = createFlow(curNode, entryNode, cfg);
        edge.setTrue();
        edge.setLoopBack(true);

        edge = createFlow(curNode, nextNode, cfg);
        edge.setFalse();
        nextNode.addIncomingEdges(exitNode.getIncomingEdges());
        loopExits.pop();
        return data;
    }

    public Object visit(ASTForStatement node, Object data) {
        CFG cfg = (CFG)data;
        JavaStatement jst = node.getJavaStatement();
        int num = 0;
        if (node.jjtGetChild(num) instanceof ASTForInit) {
            node.childAccept(this, data, num);
            num++;
        }
        Flow entryEdge = cfg.getFlow(prevNode, nextNode);
        if (node.jjtGetChild(num) instanceof ASTExpression) {
            node.childAccept(this, data, num);
            num++;
        }

        CFGBranchNode curNode = new CFGBranchNode(forSt, jst);
        CFGNode exitNode = new CFGNode();
        reconnect(curNode, cfg);
        Flow edge = createFlow(curNode, nextNode, cfg);
        edge.setTrue();

        CFGNode entryNode = (CFGNode)entryEdge.getDstNode();
        loopEntries.push(entryNode);  // for continue
        loopExits.push(exitNode);     // for break

        if (node.jjtGetChild(num) instanceof ASTForUpdate) {
            node.childAccept(this, data, num + 1);
            node.childAccept(this, data, num);
        } else {
            node.childAccept(this, data, num);
        }

        Flow mergeEdge = cfg.getFlow(prevNode, nextNode);
        mergeEdge.setDstNode(entryNode);
        mergeEdge.setLoopBack(true);
        edge = createFlow(curNode, nextNode, cfg);
        edge.setFalse();
        prevNode = entryNode;
        nextNode.addIncomingEdges(exitNode.getIncomingEdges());

        loopEntries.pop();
        loopExits.pop();
        return data;
    }

    public Object visit(ASTBreakStatement node, Object data) {
        CFG cfg = (CFG)data;
        JavaStatement jst = node.getJavaStatement();
        CFGBranchNode curNode = new CFGBranchNode(breakSt, jst);
        reconnect(curNode, cfg);

        CFGNode jumpNode;
        if (node.getName() != null) {
            jumpNode = (CFGNode)labels.get(node.getName());
        } else {
            jumpNode = (CFGNode)loopExits.peek();
        }
        Flow edge = createFlow(curNode, jumpNode, cfg);
        edge.setTrue();

        edge = createFlow(curNode, nextNode, cfg);
        edge.setFallThrough();
        return data;
    }

    public Object visit(ASTContinueStatement node, Object data) {
        CFG cfg = (CFG)data;
        JavaStatement jst = node.getJavaStatement();
        CFGBranchNode curNode = new CFGBranchNode(continueSt, jst);
        reconnect(curNode, cfg);

        CFGNode jumpNode;
        if (node.getName() != null) {
            jumpNode = (CFGNode)labels.get(node.getName());
        } else {
            jumpNode = (CFGNode)loopEntries.peek();
        }
        Flow edge = createFlow(curNode, jumpNode, cfg);
        edge.setTrue();

        edge = createFlow(curNode, nextNode, cfg);
        edge.setFallThrough();
        return data;
    }

    public Object visit(ASTReturnStatement node, Object data) {
        CFG cfg = (CFG)data;
        JavaStatement jst = node.getJavaStatement();

        node.childrenAccept(this, data);
     
        CFGAssignmentNode curNode = new CFGAssignmentNode(returnSt, jst);
        reconnect(curNode, cfg);
        Flow edge = createFlow(curNode, cfg.getEndNode(), cfg);
        edge.setTrue();

        edge = createFlow(curNode, nextNode, cfg);
        edge.setFallThrough();
        return data;
    }

    // The followings are not treated.
    // public Object visit(ASTThrowStatement node, Object data) // throwSt
    // public Object visit(ASTSynchronizedStatement node, Object data) // synchronizedSt
    // public Object visit(ASTTryStatement node, Object data) // trySt
}
