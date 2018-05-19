/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.model.cfg.builder;

import org.jtool.eclipse.model.cfg.CFG;
import org.jtool.eclipse.model.cfg.CFGMethodCall;
import org.jtool.eclipse.model.cfg.CFGNode;
import org.jtool.eclipse.model.cfg.CFGParameter;
import org.jtool.eclipse.model.cfg.CFGStatement;
import org.jtool.eclipse.model.cfg.CFGStore;
import org.jtool.eclipse.model.cfg.ControlFlow;
import org.jtool.eclipse.model.cfg.JApparentAccess;
import org.jtool.eclipse.model.cfg.JFieldAccess;
import org.jtool.eclipse.model.cfg.JLocalAccess;
import org.jtool.eclipse.model.cfg.JMethodCall;
import org.jtool.eclipse.model.cfg.JVariable;
import org.jtool.eclipse.model.graph.GraphEdge;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Stack;

/**
 * Visits AST nodes within an expression.
 * 
 * Expression:
 *   ArrayAccess
 *   Assignment
 *   PrefixExpression
 *   PostfixExpression
 *   InfixExpression
 *   FieldAccess
 *   SuperFieldAccess
 *   ThisExpression
 *   VariableDeclarationExpression (SingleVariableDeclaration/VariableDeclarationFragment)
 *   MethodInvocation
 *   SuperMethodInvocation
 *   ClassInstanceCreation
 *   ConstructorInvocation (this originally belongs to Statement)
 *   SuperConstructorInvocation (this originally belongs to Statement)
 *   EnumConstantDeclaration  (this originally belongs to BodyDeclaration)
 *   
 * Nothing to do for the following AST nodes:
 *   Annotation
 *   ArrayCreation
 *   ArrayInitializer
 *   MethodReference
 *   CastExpression
 *   ConditionalExpression
 *   InstanceofExpression
 *   ParenthesizedExpression
 *   LambdaExpression
 *   CreationReference
 *   ExpressionMethodReference
 *   SuperMethodReference
 *   TypeMethodReference
 *   BooleanLiteral
 *   CharacterLiteral
 *   NullLiteral
 *   NumberLiteral
 *   StringLiteral
 *   TypeLiteral
 *   Name (SimpleName/QualifiedName)
 * 
 * @see org.eclipse.jdt.core.dom.Expression
 * @author Katsuhisa Maruyama
 */
public class ExpressionVisitor extends ASTVisitor {
    
    protected CFG cfg;
    protected CFGStatement curNode;
    protected CFGStatement entryNode;
    
    protected static int paramNumber = 1;
    
    private boolean creatingActuals = false;
    protected Stack<AnalysisMode> analysisMode = new Stack<AnalysisMode>();
    private enum AnalysisMode {
        DEF, USE,
    }
    
    protected ExpressionVisitor(CFG cfg, CFGStatement node) {
        this.cfg = cfg;
        curNode = node;
        entryNode = node;
        
        analysisMode.push(AnalysisMode.USE);
        creatingActuals = CFGStore.getInstance().creatingActualNodes();
    }
    
    public CFGNode getEntryNode() {
        return entryNode;
    }
    
    public CFGNode getExitNode() {
        return curNode;
    }
    
    protected void insertBeforeCurrentNode(CFGStatement node) {
        Set<GraphEdge> edges = new HashSet<GraphEdge>(curNode.getIncomingEdges());
        for (GraphEdge edge : edges) {
            ControlFlow flow = (ControlFlow)edge;
            flow.setDstNode(node);
        }
        cfg.add(node);
        
        ControlFlow flow = new ControlFlow(node, curNode);
        flow.setTrue();
        cfg.add(flow);
    }
    
    @Override
    public boolean visit(ArrayAccess node) {
        Expression array = node.getArray();
        array.accept(this);
        
        Expression index = node.getIndex();
        analysisMode.push(AnalysisMode.USE);
        index.accept(this);
        analysisMode.pop();
        return false;
    }
    
    @Override
    public boolean visit(Assignment node) {
        curNode.setASTNode(node);
        curNode.setKind(CFGNode.Kind.assignment);
        
        Expression lefthand = node.getLeftHandSide();
        analysisMode.push(AnalysisMode.DEF);
        lefthand.accept(this);
        analysisMode.pop();
        
        if (node.getOperator() != Assignment.Operator.ASSIGN) {
            analysisMode.push(AnalysisMode.USE);
            lefthand.accept(this);
            analysisMode.pop();
        }
        
        Expression righthand = node.getRightHandSide();
        analysisMode.push(AnalysisMode.USE);
        righthand.accept(this);
        analysisMode.pop();
        return false;
    }
    
    @Override
    public boolean visit(PrefixExpression node) {
        curNode.setASTNode(node);
        curNode.setKind(CFGNode.Kind.assignment);
        
        Expression expr = node.getOperand();
        analysisMode.push(AnalysisMode.USE);
        expr.accept(this);
        analysisMode.pop();
        
        PrefixExpression.Operator operator = node.getOperator();
        if (operator == PrefixExpression.Operator.INCREMENT || operator == PrefixExpression.Operator.DECREMENT) {
            analysisMode.push(AnalysisMode.DEF);
            expr.accept(this);
            analysisMode.pop();
        }
        return false;
    }
    
    @Override
    public boolean visit(PostfixExpression node) {
        curNode.setASTNode(node);
        curNode.setKind(CFGNode.Kind.assignment);
        
        Expression expr = node.getOperand();
        analysisMode.push(AnalysisMode.USE);
        expr.accept(this);
        analysisMode.pop();
        
        PostfixExpression.Operator operator = node.getOperator();
        if (operator == PostfixExpression.Operator.INCREMENT || operator == PostfixExpression.Operator.DECREMENT) {
            analysisMode.push(AnalysisMode.DEF);
            expr.accept(this);
            analysisMode.pop();
        }
        return false;
    }
    
    @Override
    public boolean visit(InfixExpression node) {
        Expression lefthand = node.getLeftOperand();
        analysisMode.push(AnalysisMode.USE);
        lefthand.accept(this);
        analysisMode.pop();
        
        Expression righthand = node.getRightOperand();
        analysisMode.push(AnalysisMode.USE);
        righthand.accept(this);
        analysisMode.pop();
        return false;
    }
    
    @Override
    public boolean visit(FieldAccess node) {
        Expression expr = node.getExpression();
        analysisMode.push(AnalysisMode.USE);
        expr.accept(this);
        analysisMode.pop();
        
        SimpleName name = node.getName();
        name.accept(this);
        return false;
    }
    
    @Override
    public boolean visit(SuperFieldAccess node) {
        SimpleName name = node.getName();
        name.accept(this);
        return false;
    }
    
    @Override
    public boolean visit(ThisExpression node) {
        Name name = node.getQualifier();
        JVariable jvar;
        if (name != null) {
            jvar = new JApparentAccess(node, "$this", name.resolveTypeBinding());
        } else {
            jvar = new JApparentAccess(node, "$this");
        }
        curNode.addUseVariable(jvar);
        return false;
    }
    
    @Override
    public boolean visit(SingleVariableDeclaration node) {
        visitVariableDeclaration(node);
        return false;
    }
    
    @Override
    public boolean visit(VariableDeclarationFragment node) {
        visitVariableDeclaration(node);
        return false;
    }
    
    private void visitVariableDeclaration(VariableDeclaration node) {
        IVariableBinding vbinding = getVariableBinding(node.getName());
        if (vbinding == null) {
            return;
        }
        
        if (vbinding.isEnumConstant()) {
            curNode.setASTNode(node);
            curNode.setKind(CFGNode.Kind.enumConstantDeclaration);
        } else if (vbinding.isField()) {
            curNode.setASTNode(node);
            curNode.setKind(CFGNode.Kind.fieldDeclaration);
        } else {
            curNode.setASTNode(node);
            curNode.setKind(CFGNode.Kind.localDeclaration);
        }
        
        SimpleName name = node.getName();
        analysisMode.push(AnalysisMode.DEF);
        name.accept(this);
        analysisMode.pop();
        
        Expression initializer = node.getInitializer();
        if (initializer != null) {
            analysisMode.push(AnalysisMode.USE);
            initializer.accept(this);
            analysisMode.pop();
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public boolean visit(MethodInvocation node) {
        IMethodBinding mbinding = node.resolveMethodBinding();
        if (mbinding == null) {
            return false;
        }
        
        JMethodCall jcall = new JMethodCall(node, mbinding, node.arguments());
        CFGMethodCall callNode = new CFGMethodCall(node, jcall, CFGNode.Kind.methodCall);
        setActualNodes(callNode, node, node.arguments());
        
        Expression primary = node.getExpression();
        if (primary != null) {
            analysisMode.push(AnalysisMode.USE);
            primary.accept(this);
            analysisMode.pop();
        }
        return false;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public boolean visit(SuperMethodInvocation node) {
        IMethodBinding mbinding = node.resolveMethodBinding();
        if (mbinding == null) {
            return false;
        }
        
        JMethodCall jcall = new JMethodCall(node, mbinding, node.arguments());
        CFGMethodCall callNode = new CFGMethodCall(node, jcall, CFGNode.Kind.methodCall);
        setActualNodes(callNode, node, node.arguments());
        return false;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public boolean visit(ClassInstanceCreation node) {
        IMethodBinding mbinding = node.resolveConstructorBinding();
        if (mbinding == null) {
            return false;
        }
        
        JMethodCall jcall = new JMethodCall(node, mbinding, node.arguments());
        CFGMethodCall callNode = new CFGMethodCall(node, jcall, CFGNode.Kind.instanceCreation);
        setActualNodes(callNode, node, node.arguments());
        
        Expression primary = node.getExpression();
        if (primary != null) {
            analysisMode.push(AnalysisMode.USE);
            primary.accept(this);
            analysisMode.pop();
        }
        return false;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public boolean visit(EnumConstantDeclaration node) {
        IMethodBinding mbinding = node.resolveConstructorBinding();
        if (mbinding == null) {
            return false;
        }
        
        JMethodCall jcall = new JMethodCall(node, mbinding, node.arguments());
        CFGMethodCall callNode = new CFGMethodCall(node, jcall, CFGNode.Kind.constructorCall);
        setActualNodes(callNode, node, node.arguments());
        return false;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public boolean visit(ConstructorInvocation node) {  
        IMethodBinding binding = node.resolveConstructorBinding();
        if (binding == null) {
            return false;
        }
        
        JMethodCall jcall = new JMethodCall(node, binding, node.arguments());
        CFGMethodCall callNode = new CFGMethodCall(node, jcall, CFGNode.Kind.constructorCall);
        setActualNodes(callNode, node, node.arguments());
        return false;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public boolean visit(SuperConstructorInvocation node) {
        IMethodBinding mbinding = node.resolveConstructorBinding();
        if (mbinding == null) {
            return false;
        }
        
        JMethodCall jcall = new JMethodCall(node, mbinding, node.arguments());
        CFGMethodCall callNode = new CFGMethodCall(node, jcall, CFGNode.Kind.constructorCall);
        setActualNodes(callNode, node, node.arguments());
        return false;
    }
    
    private void setActualNodes(CFGMethodCall callNode, ASTNode node, List<Expression> arguments) {
        boolean actual = creatingActuals && callNode.getMethodCall().isInProject() && !callNode.getMethodCall().callSelfDirectly();
        if (actual) {
            createActualIns(callNode, arguments);
        } else {
            mergeActualIn(callNode, arguments);
        }
        
        insertBeforeCurrentNode(callNode);
        
        if (actual) {
            createActualOuts(callNode, arguments);
            createActualOutForReturnValue(callNode);
        } else {
            mergeActualOut(callNode);
            curNode.addUseVariable(callNode.getDefVariables().get(0));
        }
    }
    
    private void createActualIns(CFGMethodCall callNode, List<Expression> arguments) {
        for (int ordinal = 0; ordinal < arguments.size(); ordinal++) {
            createActualIn(callNode, arguments.get(ordinal), ordinal);
        }
    }
    
    private void createActualIn(CFGMethodCall callNode, Expression node, int ordinal) {
        CFGParameter actualInNode = new CFGParameter(node, CFGNode.Kind.actualIn, ordinal);
        actualInNode.setParent(callNode);
        callNode.addActualIn(actualInNode);
        
        JVariable actualIn = new JApparentAccess(node, "$" + String.valueOf(paramNumber), callNode.getMethodCall().getArgumentType(ordinal));
        actualInNode.addDefVariable(actualIn);
        paramNumber++;
        
        insertBeforeCurrentNode(actualInNode);
        
        CFGStatement tmpNode = curNode;
        curNode = actualInNode;
        analysisMode.push(AnalysisMode.USE);
        node.accept(this);
        analysisMode.pop();
        curNode = tmpNode;
    }
    
    private void createActualOuts(CFGMethodCall callNode, List<Expression> arguments) {
        for (int ordinal = 0; ordinal < arguments.size(); ordinal++) {
            CFGParameter actualIn = callNode.getActualIn(ordinal);
            if (actualIn.getUseVariables().size() == 1) {
                JVariable jacc = actualIn.getDefVariable();
                if (!jacc.isPrimitiveType()) {
                    createActualOut(callNode, actualIn);
                }
            }
        }
    }
    
    private void createActualOut(CFGMethodCall callNode, CFGParameter actualIn) {
        CFGParameter actualOutNode = new CFGParameter(actualIn.getASTNode(), CFGNode.Kind.actualOut, actualIn.getOrdinal());
        actualOutNode.setParent(callNode);
        callNode.addActualOut(actualOutNode);
        actualOutNode.addDefVariable(actualIn.getUseVariable());
        actualOutNode.addUseVariable(actualIn.getDefVariable());
        
        insertBeforeCurrentNode(actualOutNode);
    }
    
    private void createActualOutForReturnValue(CFGMethodCall callNode) {
        if (callNode.getMethodCall().isVoidType()) {
            return;
        }
        
        CFGParameter returnNode = new CFGParameter(callNode.getASTNode(), CFGNode.Kind.actualOut, 0);
        returnNode .setParent(callNode);
        callNode.addActualOut(returnNode );
        
        JVariable actualIn = new JApparentAccess(callNode.getASTNode(), "$" + String.valueOf(paramNumber), callNode.getReturnType());
        JVariable actualOut = new JApparentAccess(callNode.getASTNode(), "$" + String.valueOf(paramNumber) + "!" + callNode.getName(), callNode.getReturnType());
        returnNode.addDefVariable(actualIn);
        returnNode.addUseVariable(actualOut);
        paramNumber++;
        
        insertBeforeCurrentNode(returnNode);
        
        curNode.addUseVariable(returnNode.getDefVariable());
    }
    
    private void mergeActualIn(CFGMethodCall callNode, List<Expression> arguments) {
        for (int ordinal = 0; ordinal < arguments.size(); ordinal++) {
            analysisMode.push(AnalysisMode.USE);
            arguments.get(ordinal).accept(this);
            analysisMode.pop();
            
            List<JVariable> uses = new ArrayList<JVariable>(curNode.getUseVariables());
            for (JVariable jvar : uses) {
                callNode.addUseVariable(jvar);
                curNode.removeUseVariable(jvar);
            }
        }
    }
    
    private void mergeActualOut(CFGMethodCall callNode) {
        JVariable jvar = new JApparentAccess(callNode.getASTNode(),
                "$" + String.valueOf(paramNumber) + "!" + callNode.getName(), callNode.getReturnType());
        callNode.addDefVariable(jvar);
        paramNumber++;
    }
    
    @Override
    public boolean visit(SimpleName node) {
        registVariable(node);
        return false;
    }
    
    @Override
    public boolean visit(QualifiedName node) {
        registVariable(node);
        return false;
    }
    
    private void registVariable(Name node) {
        IVariableBinding vbinding = getVariableBinding(node);
        if (vbinding != null) {
            JVariable jvar;
            if (vbinding.isField()) {
                jvar = new JFieldAccess(node, vbinding);
            } else {
                jvar = new JLocalAccess(node, vbinding);
            }
            if (analysisMode.peek() == AnalysisMode.DEF) {
                curNode.addDefVariable(jvar);
            } else {
                curNode.addUseVariable(jvar);
            }
        }
    }
    
    private IVariableBinding getVariableBinding(Name node) {
        IBinding binding = node.resolveBinding();
        if (binding != null && binding.getKind() == IBinding.VARIABLE) {
            return (IVariableBinding)binding;
        }
        return null;
    }
}
