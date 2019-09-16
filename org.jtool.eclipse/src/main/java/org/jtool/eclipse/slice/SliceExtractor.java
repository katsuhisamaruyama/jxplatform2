/*
 *  Copyright 2019
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.slice;

import org.jtool.eclipse.pdg.PDGNode;
import org.jtool.eclipse.pdg.PDGStatement;
import org.jtool.eclipse.cfg.JReference;
import org.jtool.eclipse.javamodel.JavaFile;
import org.jtool.eclipse.javamodel.JavaClass;
import org.jtool.eclipse.javamodel.JavaMethod;
import org.jtool.eclipse.javamodel.JavaField;
import org.jtool.eclipse.javamodel.builder.ModelBuilder;
import org.jtool.eclipse.codemanipulation.ASTNodeOnCFGCollector;
import org.jtool.eclipse.codemanipulation.MethodInvocationCollector;
import org.jtool.eclipse.codemanipulation.CodeGenerator;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.core.dom.Type;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

/**
 * Extracts a slice and returns Java source code corresponding to the slice.
 * 
 * @author Katsuhisa Maruyama
 */
public class SliceExtractor extends ASTVisitor {
    
    private JavaFile jfile;
    private Set<ASTNode> sliceNodes = new HashSet<ASTNode>();
    private ASTNode astNode;
    
    public SliceExtractor(ModelBuilder builder, Slice slice, JavaClass jclass) {
        this(builder, slice.getNodes(), jclass);
    }
    
    public SliceExtractor(ModelBuilder builder, Set<PDGNode> nodes, JavaClass jclass) {
        JavaFile jfile = builder.copyJavaFile(jclass.getFile());
        for (JavaClass jc : jfile.getClasses()) {
            if (jc.getQualifiedName().equals(jclass.getQualifiedName())) {
                createSliceExtractor(nodes, jfile, jc.getASTNode());
            }
        }
    }
    
    public SliceExtractor(ModelBuilder builder, Slice slice, JavaMethod jmethod) {
        this(builder, slice.getNodes(), jmethod);
    }
    
    public SliceExtractor(ModelBuilder builder, Set<PDGNode> nodes, JavaMethod jmethod) {
        JavaFile jfile = builder.copyJavaFile(jmethod.getDeclaringClass().getFile());
        for (JavaClass jc : jfile.getClasses()) {
            if (jc.getQualifiedName().equals(jmethod.getDeclaringClass().getQualifiedName())) {
                for (JavaMethod jm : jc.getMethods()) {
                    if (jm.getQualifiedName().equals(jmethod.getQualifiedName())) {
                        createSliceExtractor(nodes, jfile, jm.getASTNode());
                    }
                }
            }
        }
    }
    
    public SliceExtractor(ModelBuilder builder, Slice slice, JavaField jfield) {
        this(builder, slice.getNodes(), jfield);
    }
    
    public SliceExtractor(ModelBuilder builder, Set<PDGNode> nodes, JavaField jfield) {
        JavaFile jfile = builder.copyJavaFile(jfield.getDeclaringClass().getFile());
        for (JavaClass jc : jfile.getClasses()) {
            if (jc.getQualifiedName().equals(jfield.getDeclaringClass().getQualifiedName())) {
                for (JavaField jf : jc.getFields()) {
                    if (jf.getQualifiedName().equals(jfield.getQualifiedName())) {
                        createSliceExtractor(nodes, jfile, jf.getASTNode());
                    }
                }
            }
        }
    }
    
    private void createSliceExtractor(Set<PDGNode> nodes, JavaFile jfile, ASTNode astNode) {
        ASTNodeOnCFGCollector collector = new ASTNodeOnCFGCollector(jfile.getCompilationUnit());
        Map<Integer, ASTNode> astNodeMap = collector.getNodeMap();
        sliceNodes.add(astNode);
        for (PDGNode pdfnode : nodes) {
            registerASTNode(pdfnode.getCFGNode().getASTNode(), astNodeMap);
            
            if (pdfnode.isStatement()) {
                PDGStatement stnode = (PDGStatement)pdfnode;
                for (JReference var : stnode.getDefVariables()) {
                    registerASTNode(var.getASTNode(), astNodeMap);
                }
            }
        }
        this.astNode = astNode;
        this.jfile = jfile;
    }
    
    private void registerASTNode(ASTNode astNode, Map<Integer, ASTNode> astNodeMap) {
        int pos = astNode.getStartPosition();
        ASTNode node = astNodeMap.get(pos);
        if (node != null) {
            sliceNodes.add(node);
        }
    }
    
    public ASTNode extractAST() {
        astNode.accept(this);
        return astNode;
    }
    
    public String extract() {
        return extract(null);
    }
    
    public String extract(Map<String, String> options) {
        astNode.accept(this);
        
        CodeGenerator codeGenerator = new CodeGenerator();
        codeGenerator.setOptions(options);
        
        String code = codeGenerator.generate(astNode, jfile.getCode(), sliceNodes);
        return code;
    }
    
    private boolean contains(ASTNode node) {
        if (node == null) {
            return false;
        }
        
        for (ASTNode n : sliceNodes) {
            if (node.getStartPosition() == n.getStartPosition()) {
                return true;
            }
        }
        return false;
    }
    
    private boolean containsAnyInSubTree(ASTNode node) {
        if (node == null) {
            return false;
        }
        
        ASTNodeOnCFGCollector collector = new ASTNodeOnCFGCollector(node);
        for (ASTNode n : collector.getNodeSet()) {
            if (contains(n)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean removeWholeElement(ASTNode node) {
        if (node == null) {
            return true;
        }
        
        if (!containsAnyInSubTree(node)) {
            node.delete();
            return true;
        } else {
            return false;
        }
    }
    
    @Override
    public boolean visit(MethodDeclaration node) {
        if (removeWholeElement(node)) {
            return false;
        }
        
        Set<ASTNode> removeNodes = new HashSet<ASTNode>();
        for (Object obj : node.parameters()) {
            SingleVariableDeclaration param = (SingleVariableDeclaration)obj;
            if (!containsAnyInSubTree(param)) {
                removeNodes.add(param);
            } else {
                param.accept(this);
            }
        }
        for (ASTNode rnode : removeNodes) {
            rnode.delete();
        }
        
        node.getBody().accept(this);
        return false;
    }
    
    @Override
    public boolean visit(Initializer node) {
        if (removeWholeElement(node)) {
            return false;
        }
        return true;
    }
    
    @Override
    public boolean visit(LambdaExpression node) {
        if (removeWholeElement(node)) {
            return false;
        }
        return true;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public boolean visit(FieldDeclaration node) {
        if (removeWholeElement(node)) {
            return false;
        }
        
        if (node.getJavadoc() != null) {
            node.getJavadoc().accept(this);
        }
        node.getType().accept(this);
        for (VariableDeclarationFragment frag : (List<VariableDeclarationFragment>)node.fragments()) {
            frag.accept(this);
        }
        
        checkDeclaration(node, node.fragments());
        return false;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public boolean visit(VariableDeclarationStatement node) {
        if (removeWholeElement(node)) {
            return false;
        }
        
        node.getType().accept(this);
        for (VariableDeclarationFragment frag : (List<VariableDeclarationFragment>)node.fragments()) {
            frag.accept(this);
        }
        
        checkDeclaration(node, node.fragments());
        return false;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public boolean visit(VariableDeclarationExpression node) {
        if (removeWholeElement(node)) {
            return false;
        }
        
        node.getType().accept(this);
        for (VariableDeclarationFragment frag : (List<VariableDeclarationFragment>)node.fragments()) {
            frag.accept(this);
        }
        
        checkDeclaration(node, node.fragments());
        return false;
    }
    
    private void checkDeclaration(ASTNode node, List<VariableDeclarationFragment> fragments) {
        List<VariableDeclarationFragment> removeNodes = new ArrayList<VariableDeclarationFragment>();
        for (VariableDeclarationFragment frag : fragments) {
            if (!containsAnyInSubTree(frag)) {
                removeNodes.add(frag);
            } else if (!contains(frag) && fragments.size() == 1) {
                pullUpExpressionInVariableDeclaration(frag, frag.getInitializer());
            }
        }
        for (VariableDeclarationFragment n : removeNodes) {
            n.delete();
        }
    }
    
    @SuppressWarnings("unchecked")
    private void pullUpExpressionInVariableDeclaration(ASTNode astnode, Expression expr) {
        if (expr != null && containsAnyInSubTree(expr)) {
            ASTNode parent = getEnclosingStatement(astnode.getParent()).getParent();
            
            if (parent instanceof Block) {
                Expression newExpression = (Expression)ASTNode.copySubtree(expr.getAST(), expr);
                ExpressionStatement newStatement = (ExpressionStatement)expr.getAST().newExpressionStatement(newExpression);
                
                Block block = (Block)parent;
                for (int index = 0; index < block.statements().size(); index++) {
                    Statement target = (Statement)block.statements().get(index);
                    if (target.getStartPosition() == astnode.getParent().getStartPosition() && target.getLength() == astnode.getParent().getLength()) {
                        block.statements().add(index, newStatement);
                        target.delete();
                        return;
                    }
                }
            }
        }
    }
    
    private void pullUpMethodInvocationInCondition(Statement statement, Expression expr) {
        List<Expression> exprs = new ArrayList<Expression>();
        exprs.add(expr);
        pullUpMethodInvocationInCondition(statement, exprs);
    }
    
    @SuppressWarnings("unchecked")
    private void pullUpMethodInvocationInCondition(Statement statement, List<Expression> exprs) {
        List<MethodInvocation> invocations = new ArrayList<MethodInvocation>();
        for (Expression expr : exprs) {
            MethodInvocationCollector collector = new MethodInvocationCollector(expr);
            for (ASTNode n : collector.getNodes()) {
                if (n instanceof MethodInvocation && contains(n)) {
                    invocations.add((MethodInvocation)n);
                }
            }
        }
        
        if (invocations.size() == 1) {
            Expression newExpression = (Expression)ASTNode.copySubtree(statement.getAST(), invocations.get(0));
            ExpressionStatement newStatement = (ExpressionStatement)statement.getAST().newExpressionStatement(newExpression);
            repalceStatement(statement, newStatement);
        } else if (invocations.size() > 1) {
            Block newBlock = (Block)statement.getAST().newBlock();
            repalceStatement(statement, newBlock);
            for (int index = 0; index < invocations.size(); index++) {
                Expression newExpression = (Expression)ASTNode.copySubtree(statement.getAST(), invocations.get(index));
                ExpressionStatement newStatement = (ExpressionStatement)statement.getAST().newExpressionStatement(newExpression);
                newBlock.statements().add(newStatement);
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private void repalceStatement(Statement statement, Statement newStatement) {
        StructuralPropertyDescriptor location = statement.getLocationInParent();
        if (location != null) {
            if (location.isChildProperty()) {
                statement.getParent().setStructuralProperty(location, newStatement);
            } else if (location.isChildListProperty()) {
                List<ASTNode> property = (List<ASTNode>)statement.getParent().getStructuralProperty(location);
                property.set(property.indexOf(statement), newStatement);
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private void repalceStatement(Statement statement, Block block) {
        StructuralPropertyDescriptor location = statement.getLocationInParent();
        if (location != null) {
            if (location.isChildProperty()) {
                statement.getParent().setStructuralProperty(location, block);
            } else if (location.isChildListProperty()) {
                List<ASTNode> property = (List<ASTNode>)statement.getParent().getStructuralProperty(location);
                int index = property.indexOf(statement);
                for (Statement st : (List<Statement>)block.statements()) {
                    Statement newStatement = (Statement)ASTNode.copySubtree(st.getAST(), st);
                    property.add(index, newStatement);
                    index++;
                }
                statement.delete();
            }
        }
    }
    
    @Override
    public boolean visit(Assignment node) {
        Statement statement = getEnclosingStatement(node);
        if (removeWholeElement(statement)) {
            return false;
        }
        
        if (contains(statement)) {
            return true;
        }
        
        Expression expr = node.getRightHandSide();
        if (containsAnyInSubTree(expr)) {
            if (statement instanceof ExpressionStatement) {
                Expression newExpression = (Expression)ASTNode.copySubtree(expr.getAST(), expr);
                
                ExpressionStatement parentExpression = (ExpressionStatement)statement;
                parentExpression.setExpression(newExpression);
            }
        } else {
            statement.delete();
            return false;
        }
        return true;
    }
    
    private Statement getEnclosingStatement(ASTNode node) {
        while (node != null) {
            if (node instanceof Statement) {
                return (Statement)node;
            }
            node = node.getParent();
        }
        return null;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public boolean visit(MethodInvocation node) {
        Statement statement = getEnclosingStatement(node);
        if (statement == null || removeWholeElement(statement)) {
            return false;
        }
        
        checkMethodCallArguments((List<Expression>)node.arguments());
        return true;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public boolean visit(SuperMethodInvocation node) {
        if (removeWholeElement(node)) {
            return false;
        }
        
        checkMethodCallArguments((List<Expression>)node.arguments());
        return true;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public boolean visit(ClassInstanceCreation node) {
        if (removeWholeElement(node)) {
            return false;
        }
        
        checkMethodCallArguments((List<Expression>)node.arguments());
        return true;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public boolean visit(ConstructorInvocation node) {
        if (removeWholeElement(node)) {
            return false;
        }
        
        checkMethodCallArguments((List<Expression>)node.arguments());
        return true;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public boolean visit(SuperConstructorInvocation node) {
        if (removeWholeElement(node)) {
            return false;
        }
        
        checkMethodCallArguments((List<Expression>)node.arguments());
        return true;
    }
    
    private void checkMethodCallArguments(List<Expression> arguments) {
        List<Expression> removeNodes = new ArrayList<Expression>();
        for (Expression expr : arguments) {
            if (!containsAnyInSubTree(expr)) {
                removeNodes.add(expr);
            }
        }
        
        for (Expression n : removeNodes) {
            n.delete();
        }
    }
    
    @Override
    public boolean visit(AssertStatement node) {
        if (removeWholeElement(node)) {
            return false;
        }
        return true;
    }
    
    @Override
    public boolean visit(BreakStatement node) {
        if (removeWholeElement(node.getParent())) {
            return false;
        }
        return true;
    }
    
    @Override
    public boolean visit(ContinueStatement node) {
        if (removeWholeElement(node.getParent())) {
            return false;
        }
        return true;
    }
    
    @Override
    public boolean visit(DoStatement node) {
        if (removeWholeElement(node)) {
            return false;
        }
        
        if (!containsAnyInSubTree(node.getBody())) {
            pullUpMethodInvocationInCondition(node, node.getExpression());
            node.delete();
        }
        
        return true;
    }
    
    @Override
    public boolean visit(EnhancedForStatement node) {
        if (removeWholeElement(node)) {
            return false;
        }
        
        if (!containsAnyInSubTree(node.getBody())) {
            pullUpMethodInvocationInCondition(node, node.getExpression());
            node.delete();
        }
        
        return true;
    }
    
    @Override
    public boolean visit(ExpressionStatement node) {
        if (removeWholeElement(node)) {
            return false;
        }
        return true;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public boolean visit(ForStatement node) {
        if (removeWholeElement(node)) {
            return false;
        }
        
        if (!containsAnyInSubTree(node.getBody()) && node.updaters().size() == 0) {
            List<Expression> exprs = new ArrayList<Expression>();
            exprs.addAll((List<Expression>)node.initializers());
            exprs.add(node.getExpression());
            exprs.addAll((List<Expression>)node.updaters());
            pullUpMethodInvocationInCondition(node, exprs);
            node.delete();
        }
        
        List<Expression> removeNodes = new ArrayList<Expression>();
        for (Expression expr : (List<Expression>)node.updaters()) {
            if (!contains(expr)) {
                removeNodes.add(expr);
            }
        }
        for (Expression n : removeNodes) {
            n.delete();
        }
        return true;
    }
    
    @Override
    public boolean visit(IfStatement node) {
        if (removeWholeElement(node)) {
            return false;
        }
        
        if (!containsAnyInSubTree(node.getThenStatement()) && !containsAnyInSubTree(node.getElseStatement())) {
            pullUpMethodInvocationInCondition(node, node.getExpression());
            node.delete();
            return false;
        }
        
        Statement thenStatement = node.getThenStatement();
        if (!(thenStatement instanceof Block) && !containsAnyInSubTree(thenStatement)) {
            EmptyStatement empty = node.getAST().newEmptyStatement();
            node.setThenStatement(empty);
        }
        return true;
    }
    
    @Override
    public boolean visit(SwitchCase node) {
        return true;
    }
    
    @Override
    public boolean visit(SwitchStatement node) {
        if (removeWholeElement(node)) {
            return false;
        }
        return true;
    }
    
    @Override
    public boolean visit(TypeDeclarationStatement node) {
        if (removeWholeElement(node)) {
            return false;
        }
        return true;
    }
    
    @Override
    public boolean visit(WhileStatement node) {
        if (removeWholeElement(node)) {
            return false;
        }
        
        if (!containsAnyInSubTree(node.getBody())) {
            pullUpMethodInvocationInCondition(node, node.getExpression());
            node.delete();
        }
        
        return true;
    }
    
    private MethodDeclaration getEnclosingMethod(ASTNode node) {
        while (node != null) {
            if (node instanceof MethodDeclaration) {
                return (MethodDeclaration)node;
            }
            node = node.getParent();
        }
        return null;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public boolean visit(ReturnStatement node) {
        if (contains(node)) {
            return true;
        }
        
        if (!contains(getEnclosingStatement(node.getParent())) && !contains(getEnclosingMethod(node.getParent()))) {
            node.delete();
            return false;
        }
        
        Expression returnExpression = getReturnExpression(node);
        Block newBlock = pullUpMethodInvocationInReturn(node);
        if (newBlock != null) {
            ReturnStatement newStatement = (ReturnStatement)ASTNode.copySubtree(node.getAST(), node);
            newStatement.setExpression(returnExpression);
            newBlock.statements().add(newStatement);
        } else {
            node.setExpression(returnExpression);
        }
        return true;
    }
    
    private Expression getReturnExpression(ReturnStatement node) {
        MethodDeclaration methodNode = getEnclosingMethod(node);
        if (methodNode == null) {
            return null;
        }
        
        Type type = methodNode.getReturnType2();
        if (type == null) {
            return null;
        }
        
        Expression newExpression;
        if (type.isPrimitiveType()) {
            if (type.toString().equals("boolean")) {
                newExpression = node.getAST().newBooleanLiteral(false);
            } else if (type.toString().equals("char")) {
                newExpression = node.getAST().newCharacterLiteral();
            } else if (type.toString().equals("void")) {
                newExpression = null;
            } else {
                newExpression = node.getAST().newNumberLiteral();
            }
        } else {
            newExpression = node.getAST().newNullLiteral();
        }
        return newExpression;
    }
    
    @SuppressWarnings("unchecked")
    private Block pullUpMethodInvocationInReturn(ReturnStatement statement) {
        Expression expr = statement.getExpression();
        if (!containsAnyInSubTree(expr)) {
            return null;
        }
        
        List<MethodInvocation> invocations = new ArrayList<MethodInvocation>();
        MethodInvocationCollector collector = new MethodInvocationCollector(expr);
        for (ASTNode astnode : collector.getNodes()) {
            if (astnode instanceof MethodInvocation) {
                invocations.add((MethodInvocation)astnode);
            }
        }
        if (invocations.size() == 0) {
            return null;
        }
        
        Block parentBlock;
        if (statement.getParent() instanceof Block) {
            parentBlock = (Block)statement.getParent();
        } else {
            parentBlock = (Block)statement.getAST().newBlock();
            repalceStatement(statement, parentBlock);
        }
        for (int index = 0; index < invocations.size(); index++) {
            Expression newExpression = (Expression)ASTNode.copySubtree(statement.getAST(), invocations.get(index));
            ExpressionStatement newStatement = (ExpressionStatement)statement.getAST().newExpressionStatement(newExpression);
            parentBlock.statements().add(newStatement);
        }
        return parentBlock;
    }
    
    @Override
    public boolean visit(SynchronizedStatement node) {
        if (removeWholeElement(node)) {
            return false;
        }
        return true;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public boolean visit(TryStatement node) {
        if (removeWholeElement(node)) {
            return false;
        }
        
        List<CatchClause> tmpCatchClauses = new ArrayList<CatchClause>(node.catchClauses());
        for (CatchClause catchClause : tmpCatchClauses) {
            if (removeWholeElement(catchClause)) {
                catchClause.delete();
            }
        }
        return true;
    }
    
    @Override
    public void endVisit(TryStatement node) {
        if (node.catchClauses().size() == 0 && !containsAnyInSubTree(node.getFinally())) {
            repalceStatement(node, node.getBody());
        }
    }
    
    @Override
    public boolean visit(ThrowStatement node) {
        if (removeWholeElement(node)) {
            return false;
        }
        return true;
    }
}
