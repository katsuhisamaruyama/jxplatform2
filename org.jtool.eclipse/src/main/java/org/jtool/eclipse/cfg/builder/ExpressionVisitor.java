/*
 *  Copyright 2018-2020
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg.builder;

import org.jtool.eclipse.cfg.CFG;

import org.jtool.eclipse.cfg.CFGNode;
import org.jtool.eclipse.cfg.CFGStatement;
import org.jtool.eclipse.cfg.CFGParameter;
import org.jtool.eclipse.cfg.CFGFieldAccess;
import org.jtool.eclipse.cfg.CFGMethodCall;
import org.jtool.eclipse.cfg.CFGReceiver;
import org.jtool.eclipse.cfg.ControlFlow;
import org.jtool.eclipse.cfg.JReference;
import org.jtool.eclipse.cfg.JMethodReference;
import org.jtool.eclipse.cfg.JFieldReference;
import org.jtool.eclipse.cfg.JLocalVarReference;
import org.jtool.eclipse.cfg.JSpecialVarReference;
import org.jtool.eclipse.graph.GraphEdge;
import org.jtool.eclipse.javamodel.JavaClass;
import org.jtool.eclipse.javamodel.JavaMethod;
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
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TypeLiteral;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Stack;

/**
 * Visits AST nodes within an expression.
 * All methods of this class are not intended to be directly called by clients.
 * 
 * @see org.eclipse.jdt.core.dom.Expression
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
 *   SingleVariableDeclaration
 *   VariableDeclarationFragment
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
 * @author Katsuhisa Maruyama
 */
public class ExpressionVisitor extends ASTVisitor {
    
    protected StatementVisitor statementVisitor;
    
    protected CFG cfg;
    protected CFGStatement curNode;
    protected CFGStatement entryNode;
    
    protected static int temporaryVariableId = 1;
    
    private Stack<AnalysisMode> analysisMode = new Stack<>();
    private enum AnalysisMode {
        DEF, USE,
    }
    
    private JInfoStore infoStore;
    
    private Set<JMethod> visited;
    
    protected ExpressionVisitor(CFG cfg, CFGStatement node, JInfoStore infoStore, Set<JMethod> visited) {
        this(null, cfg, node, infoStore, visited);
    }
    
    protected ExpressionVisitor(StatementVisitor visitor, CFG cfg, CFGStatement node, JInfoStore infoStore, Set<JMethod> visited) {
        this.statementVisitor = visitor;
        this.cfg = cfg;
        this.infoStore = infoStore;
        this.visited = visited;
        
        curNode = node;
        entryNode = node;
        analysisMode.push(AnalysisMode.USE);
    }
    
    public CFGNode getEntryNode() {
        return entryNode;
    }
    
    public CFGNode getExitNode() {
        return curNode;
    }
    
    protected void insertBeforeCurrentNode(CFGStatement node) {
        Set<GraphEdge> edges = new HashSet<>(curNode.getIncomingEdges());
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
        Expression expr = node.getLeftOperand();
        analysisMode.push(AnalysisMode.USE);
        expr.accept(this);
        analysisMode.pop();
        
        expr = node.getRightOperand();
        analysisMode.push(AnalysisMode.USE);
        expr.accept(this);
        analysisMode.pop();
        
        for (Object obj : node.extendedOperands()) {
            Expression e = (Expression)obj;
            analysisMode.push(AnalysisMode.USE);
            e.accept(this);
            analysisMode.pop();
        }
        return false;
    }
    
    @Override
    public boolean visit(FieldAccess node) {
        String receiverName = null;
        Expression receiver = node.getExpression();
        if (receiver != null) {
            analysisMode.push(AnalysisMode.USE);
            receiver.accept(this);
            analysisMode.pop();
            
            List<JReference> uses = curNode.getUseVariables();
            if (uses.size() > 0) {
                JReference ref = uses.get(uses.size() - 1);
                receiverName = ref.getReferenceName();
            }
        }
        
        JFieldReference jfacc = registVariable(node.getExpression(), node.getName());
        CFGFieldAccess filedAccessNode = new CFGFieldAccess(node, jfacc, CFGNode.Kind.fieldAccess);
        
        CFGReceiver receiverNode = null;
        if (receiverName != null) {
            receiverNode = new CFGReceiver(receiver, CFGNode.Kind.receiver);
            jfacc.setReceiver(receiverNode);
            receiverNode.addUseVariables(curNode.getUseVariables());
            
            if (receiverNode.isMethodRef()) {
                JReference receiverVar = new JSpecialVarReference(receiver, "$" + String.valueOf(temporaryVariableId),
                        receiverNode.getType(), false);
                receiverNode.setDefVariable(receiverVar);
                filedAccessNode.addUseVariable(receiverVar);
            } else {
                filedAccessNode.addUseVariables(curNode.getUseVariables());
            }
        }
        
        insertBeforeCurrentNode(filedAccessNode);
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
        JReference jvar;
        if (name != null) {
            jvar = new JSpecialVarReference(node, "$this", name.resolveTypeBinding());
        } else {
            jvar = new JSpecialVarReference(node, "$this", false);
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
        
        String receiverName = null;
        Expression receiver = node.getExpression();
        if (receiver != null) {
            int useNum = curNode.getUseVariables().size();
            analysisMode.push(AnalysisMode.USE);
            receiver.accept(this);
            analysisMode.pop();
            
            List<JReference> uses = curNode.getUseVariables();
            if (uses.size() - useNum > 0) {
                JReference ref = uses.get(uses.size() - 1);
                receiverName = ref.getReferenceName();
            }
        }
        
        JMethodReference jcall = new JMethodReference(node, node.getName(), node.getExpression(),
                receiverName, mbinding, node.arguments());
        CFGMethodCall callNode = new CFGMethodCall(node, jcall, CFGNode.Kind.methodCall);
        callNode.addUseVariables(curNode.getUseVariables());
        
        CFGStatement receiverNode = null;
        if (receiverName != null) {
            receiverNode = new CFGStatement(node, CFGNode.Kind.receiver);
            jcall.setReceiver(receiverNode);
            receiverNode.addUseVariables(curNode.getUseVariables());
            
            setDefUseFields(callNode, jcall, receiverNode, receiverName);
        }
        
        setActualNodes(callNode, node.arguments(), receiverNode);
        setExceptionFlow(callNode, jcall);
        return false;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public boolean visit(SuperMethodInvocation node) {
        IMethodBinding mbinding = node.resolveMethodBinding();
        if (mbinding == null) {
            return false;
        }
        
        JMethodReference jcall = new JMethodReference(node, node.getName(), null, null, mbinding, node.arguments());
        CFGMethodCall callNode = new CFGMethodCall(node, jcall, CFGNode.Kind.methodCall);
        
        setActualNodes(callNode, node.arguments(), null);
        setExceptionFlow(callNode, jcall);
        return false;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public boolean visit(ConstructorInvocation node) {
        IMethodBinding mbinding = node.resolveConstructorBinding();
        if (mbinding == null) {
            return false;
        }
        
        JMethodReference jcall = new JMethodReference(node, node, null, null, mbinding, node.arguments());
        CFGMethodCall callNode = new CFGMethodCall(node, jcall, CFGNode.Kind.constructorCall);
        
        setActualNodes(callNode, node.arguments(), null);
        setExceptionFlow(callNode, jcall);
        return false;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public boolean visit(SuperConstructorInvocation node) {
        IMethodBinding mbinding = node.resolveConstructorBinding();
        if (mbinding == null) {
            return false;
        }
        
        JMethodReference jcall = new JMethodReference(node, node, null, null, mbinding, node.arguments());
        CFGMethodCall callNode = new CFGMethodCall(node, jcall, CFGNode.Kind.constructorCall);
        
        setActualNodes(callNode, node.arguments(), null);
        setExceptionFlow(callNode, jcall);
        return false;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public boolean visit(ClassInstanceCreation node) {
        IMethodBinding mbinding = node.resolveConstructorBinding();
        if (mbinding == null) {
            return false;
        }
        
        String receiverName = null;
        Expression receiver = node.getExpression();
        if (receiver != null) {
            int useNum = curNode.getUseVariables().size();
            analysisMode.push(AnalysisMode.USE);
            receiver.accept(this);
            analysisMode.pop();
            
            List<JReference> uses = curNode.getUseVariables();
            if (uses.size() - useNum > 0) {
                JReference ref = uses.get(uses.size() - 1);
                receiverName = ref.getReferenceName();
            }
        }
        
        JMethodReference jcall = new JMethodReference(node, node.getType(), node.getExpression(),
                receiverName, mbinding, node.arguments());
        CFGMethodCall callNode = new CFGMethodCall(node, jcall, CFGNode.Kind.instanceCreation);
        callNode.addUseVariables(curNode.getUseVariables());
        
        CFGStatement receiverNode = null;
        if (receiverName != null) {
            receiverNode = new CFGStatement(node, CFGNode.Kind.receiver);
            jcall.setReceiver(receiverNode);
            receiverNode.addUseVariables(curNode.getUseVariables());
            
            setDefUseFields(callNode, jcall, receiverNode, receiverName);
        }
        
        setActualNodes(callNode, node.arguments(), receiverNode);
        setExceptionFlow(callNode, jcall);
        return false;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public boolean visit(EnumConstantDeclaration node) {
        IMethodBinding mbinding = node.resolveConstructorBinding();
        if (mbinding == null) {
            return false;
        }
        
        JMethodReference jcall = new JMethodReference(node, node.getName(), null, null, mbinding, node.arguments());
        CFGMethodCall callNode = new CFGMethodCall(node, jcall, CFGNode.Kind.constructorCall);
        
        setActualNodes(callNode, node.arguments(), null);
        setExceptionFlow(callNode, jcall);
        return false;
    }
    
    private void setDefUseFields(CFGMethodCall callNode, JMethodReference jcall, CFGStatement receiverNode, String receiverName) {
        ASTNode node = callNode.getASTNode();
        String type = jcall.getDeclaringClassName();
        
        Set<JMethod> methods = setDefUseFieldsInCalledMethod(jcall);
        for (JMethod method : methods) {
            for (DefOrUseField def : method.getDefFields()) {
                JReference ref = createFieldReference(node, def, type, receiverName);
                callNode.addDefVariable(ref);
                if (receiverNode != null) {
                    receiverNode.addUseVariable(ref);
                }
            }
            
            for (DefOrUseField use : method.getUseFields()) {
                JReference ref = createFieldReference(node, use, type, receiverName);
                callNode.addUseVariable(ref);
            }
        }
    }
    
    private JReference createFieldReference(ASTNode node, DefOrUseField field, String type, String receiverName) {
        String rname = receiverName + "." + field.getName();
        if (infoStore.findInternalClass(field.getClassName()) != null) {
            return new JFieldReference(node, field.getClassName(), field.getName(), rname,
                    type, field.isPrimitive(), field.getModifier(), true);
        } else {
            return new JFieldReference(node, field.getClassName(), field.getName(), rname,
                    type, field.isPrimitive(), field.getModifier(), false);
        }
    }
    
    private Set<JMethod> setDefUseFieldsInCalledMethod(JMethodReference jcall) {
        Set<JMethod> methods = new HashSet<>();
        JMethod method = infoStore.getJMethod(jcall.getDeclaringClassName(), jcall.getSignature());
        if (method != null) {
            if (!method.defuseDecided() && visited != null && !visited.contains(method)) {
                method.findDefUseFields(visited, true);
                visited.add(method);
            }
            methods.add(method);
        }
        return methods;
    }
    
    private void setActualNodes(CFGMethodCall callNode, List<Expression> arguments, CFGStatement receiverNode) {
        boolean actual = callNode.getMethodCall().isInProject() && !callNode.getMethodCall().callSelfDirectly();
        if (actual) {
            createActualIns(callNode, arguments);
            insertBeforeCurrentNode(callNode);
        } else {
            insertBeforeCurrentNode(callNode);
            mergeActualIn(callNode, arguments);
        }
        
        if (receiverNode != null) {
            insertBeforeCurrentNode(receiverNode);
        }
        
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
        
        String type = callNode.getMethodCall().getArgumentType(ordinal);
        boolean primitive = callNode.getMethodCall().getArgumentPrimitiveType(ordinal);
        JReference actualIn = new JSpecialVarReference(node,
                "$" + String.valueOf(temporaryVariableId), type, primitive);
        
        actualInNode.addDefVariable(actualIn);
        temporaryVariableId++;
        
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
            createActualOut(callNode, actualIn);
        }
    }
    
    private void createActualOut(CFGMethodCall callNode, CFGParameter actualIn) {
        CFGParameter actualOutNode = new CFGParameter(actualIn.getASTNode(),
                CFGNode.Kind.actualOut, actualIn.getOrdinal());
        actualOutNode.setParent(callNode);
        callNode.addActualOut(actualOutNode);
        
        if (actualIn.getUseVariables().size() == 1 && actualIn.getASTNode() instanceof Name) {
            JReference jvar = actualIn.getUseVariable();
            if (!jvar.isPrimitiveType()) {
                actualOutNode.addDefVariable(actualIn.getUseVariable());
                actualOutNode.addUseVariable(actualIn.getDefVariable());
            }
        }
        
        insertBeforeCurrentNode(actualOutNode);
    }
    
    private void createActualOutForReturnValue(CFGMethodCall callNode) {
        if (!callNode.isConstructorCall() && callNode.isVoidType()) {
            return;
        }
        
        CFGParameter actualOutNodeForReturn = new CFGParameter(callNode.getASTNode(), CFGNode.Kind.actualOut, 0);
        actualOutNodeForReturn.setParent(callNode);
        callNode.setActualOutForReturn(actualOutNodeForReturn);
        
        String type = callNode.getReturnType();
        boolean primitive = callNode.isPrimitiveType();
        JReference actualIn = new JSpecialVarReference(callNode.getASTNode(),
                "$" + String.valueOf(temporaryVariableId), type, primitive);
        JReference actualOut = new JSpecialVarReference(callNode.getASTNode(),
                "$" + String.valueOf(temporaryVariableId), type, primitive);
        actualOutNodeForReturn.addDefVariable(actualIn);
        actualOutNodeForReturn.addUseVariable(actualOut);
        temporaryVariableId++;
        
        insertBeforeCurrentNode(actualOutNodeForReturn);
        
        curNode.addUseVariable(actualOutNodeForReturn.getDefVariable());
    }
    
    private void mergeActualIn(CFGMethodCall callNode, List<Expression> arguments) {
        CFGStatement tmpNode = curNode;
        curNode = callNode;
        
        for (int ordinal = 0; ordinal < arguments.size(); ordinal++) {
            analysisMode.push(AnalysisMode.USE);
            arguments.get(ordinal).accept(this);
            analysisMode.pop();
        }
        curNode = tmpNode;
    }
    
    private void mergeActualOut(CFGMethodCall callNode) {
        String type = callNode.getReturnType();
        if (callNode.isConstructorCall()) {
            type = callNode.getQualifiedName();
        }
        boolean primitive = callNode.isPrimitiveType();
        String name = "$" + String.valueOf(temporaryVariableId);
        JReference jvar = new JSpecialVarReference(callNode.getASTNode(), name, type, primitive);
        
        callNode.addDefVariable(jvar);
        temporaryVariableId++;
    }
    
    private void setExceptionFlow(CFGMethodCall callNode, JMethodReference jcall) {
        if (statementVisitor != null) {
            for (ITypeBinding type : jcall.getExceptionTypes()) {
                statementVisitor.setExceptionFlowOnMethodCall(callNode, type);
            }
            
            JavaClass jclass = statementVisitor.getInfoStore().getJavaProject().getClass(jcall.getDeclaringClassName());
            if (jclass != null) {
                JavaMethod jmethod = jclass.getMethod(jcall.getSignature());
                if (jmethod != null) {
                    ExceptionTypeCollector collector = new ExceptionTypeCollector(infoStore.getJavaProject());
                    for (ITypeBinding type : collector.getExceptions(jmethod)) {
                        statementVisitor.setExceptionFlowOnMethodCall(callNode, type);
                    }
                }
            }
        }
    }
    
    @Override
    public boolean visit(SimpleName node) {
        registVariable(node);
        return false;
    }
    
    @Override
    public boolean visit(QualifiedName node) {
        IVariableBinding vbinding = getVariableBinding(node);
        if (vbinding != null && vbinding.isField()) {
            Name prefix = node.getQualifier();
            if (prefix.isSimpleName()) {
                analysisMode.push(AnalysisMode.USE);
                JReference jvar = registVariable((SimpleName)prefix);
                analysisMode.pop();
                
                JReference fvar;
                if (jvar != null) {
                    String name = jvar.getReferenceName() + "." + node.getName().getIdentifier();
                    fvar = new JFieldReference(node, node.getName(), name, vbinding);
                } else {
                    fvar = new JFieldReference(node, node.getName(), node.getFullyQualifiedName(), vbinding);
                }
                if (analysisMode.peek() == AnalysisMode.DEF) {
                    curNode.addDefVariable(fvar);
                } else {
                    curNode.addUseVariable(fvar);
                }
            } else {
                analysisMode.push(AnalysisMode.USE);
                node.getQualifier().accept(this);
                analysisMode.pop();
            }
        }
        return false;
    }
    
    private JFieldReference registVariable(Expression exp, SimpleName node) {
        JReference ref = registVariable(node);
        if (ref != null && ref instanceof JFieldReference) {
            JFieldReference fref = (JFieldReference)ref;
            fref.setReceiverNode(exp);
            return fref;
        }
        return null;
    }
    
    private JReference registVariable(SimpleName node) {
        IVariableBinding vbinding = getVariableBinding(node);
        if (vbinding != null) {
            JReference jvar;
            if (vbinding.isField()) {
                jvar = new JFieldReference(node, node, node.getIdentifier(), vbinding);
            } else {
                jvar = new JLocalVarReference(node, vbinding);
            }
            
            if (analysisMode.peek() == AnalysisMode.DEF) {
                curNode.addDefVariable(jvar);
            } else {
                curNode.addUseVariable(jvar);
            }
            return jvar;
        }
        return null;
    }
    
    private IVariableBinding getVariableBinding(Name node) {
        IBinding binding = node.resolveBinding();
        if (binding != null && binding.getKind() == IBinding.VARIABLE) {
            return (IVariableBinding)binding;
        }
        return null;
    }
    
    public static boolean isCFGNode(ASTNode node) {
        return (
                node instanceof Assignment ||
                node instanceof ClassInstanceCreation ||
                node instanceof MethodInvocation ||
                node instanceof SuperMethodInvocation ||
                node instanceof PostfixExpression ||
                node instanceof PrefixExpression ||
                node instanceof SingleVariableDeclaration ||
                node instanceof VariableDeclarationFragment
            );
    }
    
    public static boolean isCFGNodeOnLiteral(ASTNode node) {
        return (
                node instanceof BooleanLiteral ||
                node instanceof CharacterLiteral ||
                node instanceof NullLiteral ||
                node instanceof NumberLiteral ||
                node instanceof StringLiteral ||
                node instanceof TypeLiteral ||
                node instanceof Name
            );
    }
}
