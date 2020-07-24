/*
 *  Copyright 2019-2020
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.slice;

import org.jtool.eclipse.pdg.PDGNode;
import org.jtool.eclipse.pdg.PDGStatement;
import org.jtool.eclipse.cfg.JReference;
import org.jtool.eclipse.javamodel.JavaFile;
import org.jtool.eclipse.javamodel.JavaClass;
import org.jtool.eclipse.javamodel.JavaElement;
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
import org.eclipse.jdt.core.dom.EnumDeclaration;
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
import org.eclipse.jdt.core.dom.TypeDeclaration;

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
    
    protected JavaFile jfile;
    protected Set<ASTNode> sliceNodes = new HashSet<>();
    protected ASTNode astNode;
    
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
        sliceNodes.add(astNode);
        for (PDGNode pdfnode : nodes) {
            registerASTNode(pdfnode.getCFGNode().getASTNode(), collector);
            
            if (pdfnode.isStatement()) {
                PDGStatement stnode = (PDGStatement)pdfnode;
                for (JReference var : stnode.getDefVariables()) {
                    registerASTNode(var.getASTNode(), collector);
                }
            }
        }
        this.astNode = astNode;
        this.jfile = jfile;
    }
    
    private void registerASTNode(ASTNode astNode, ASTNodeOnCFGCollector collector) {
        ASTNode correspondingNode = collector.get(astNode);
        if (correspondingNode != null) {
            sliceNodes.add(correspondingNode);
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
    
    protected boolean contains(ASTNode node) {
        if (node == null) {
            return false;
        }
        
        return sliceNodes
                .stream()
                .anyMatch(n -> node.getStartPosition() == n.getStartPosition() &&
                               node.getLength() == n.getLength());
    }
    
    protected boolean containsAnyInSubTree(ASTNode node) {
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
    
    protected boolean removeWholeElement(ASTNode node) {
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
        
        Set<ASTNode> removeNodes = new HashSet<>();
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
    
    protected void checkDeclaration(ASTNode node, List<VariableDeclarationFragment> fragments) {
        List<VariableDeclarationFragment> removeNodes = new ArrayList<>();
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
    protected void pullUpExpressionInVariableDeclaration(ASTNode astnode, Expression expr) {
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
    
    protected void pullUpMethodInvocations(Statement statement, Expression expr) {
        List<Expression> exprs = new ArrayList<>();
        exprs.add(expr);
        pullUpMethodInvocations(statement, exprs);
    }
    
    @SuppressWarnings("unchecked")
    protected void pullUpMethodInvocations(Statement statement, List<Expression> exprs) {
        List<MethodInvocation> invocations = new ArrayList<>();
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
            
            replaceStatementWithStatement(statement, newStatement);
            
        } else if (invocations.size() > 1) {
            Block newBlock = (Block)statement.getAST().newBlock();
            for (int index = 0; index < invocations.size(); index++) {
                Expression newExpression = (Expression)ASTNode.copySubtree(statement.getAST(), invocations.get(index));
                ExpressionStatement newStatement = (ExpressionStatement)statement.getAST().newExpressionStatement(newExpression);
                newBlock.statements().add(newStatement);
            }
            
            replaceStatementWithBlock(statement, newBlock);
        }
    }
    
    @SuppressWarnings("unchecked")
    protected void replaceStatementWithStatement(Statement statement, Statement newStatement) {
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
    protected void replaceStatementWithBlock(Statement statement, Block block) {
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
        if (removeWholeElement(node)) {
            return false;
        }
        if (contains(node)) {
            return true;
        }
        
        Statement statement = getEnclosingStatement(node);
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
    
    protected Statement getEnclosingStatement(ASTNode node) {
        while (node != null) {
            if (node instanceof Statement) {
                return (Statement)node;
            }
            node = node.getParent();
        }
        return null;
    }
    
    protected String findEnclosingClass(ASTNode node) {
        TypeDeclaration tnode = (TypeDeclaration)JavaElement.getAncestor(node, ASTNode.TYPE_DECLARATION);
        if (tnode != null) {
            return tnode.resolveBinding().getQualifiedName();
        }
        
        EnumDeclaration enode = (EnumDeclaration)JavaElement.getAncestor(node, ASTNode.ENUM_DECLARATION);
        if (enode != null) {
            return enode.resolveBinding().getQualifiedName();
        }
        return null;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public boolean visit(MethodInvocation node) {
        Statement statement = getEnclosingStatement(node);
        if (removeWholeElement(statement)) {
            return false;
        }
        
        if (contains(node)) {
            removeMethodCallArgument(node, (List<Expression>)node.arguments());
            return true;
            
        } else {
            if (node.getName() != null) {
                node.getName().accept(this);
            }
            if (node.getExpression() != null) {
                node.getExpression().accept(this);
            }
            for (Type type : (List<Type>)node.typeArguments()) {
                type.accept(this);
            }
            for (Expression expr : (List<Expression>)node.arguments()) {
                expr.accept(this);
            }
            
            pullUpMethodInvocations(statement, (List<Expression>)node.arguments());
        }
        return false;
    }
    
    protected void removeMethodCallArgument(MethodInvocation node, List<Expression> arguments) {
        if (node.resolveMethodBinding() != null) {
            String declaringClassName = node.resolveMethodBinding().getDeclaringClass().getQualifiedName();
            String enclosingClassName = findEnclosingClass(node);
            if (declaringClassName.equals(enclosingClassName)) {
                removeMethodCallArgument(arguments);
            }
        }
    }
    
    protected void removeMethodCallArgument(List<Expression> arguments) {
        List<Expression> removeNodes = new ArrayList<>();
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
    public boolean visit(SuperMethodInvocation node) {
        if (removeWholeElement(node)) {
            return false;
        }
        return true;
    }
    
    @Override
    public boolean visit(ClassInstanceCreation node) {
        if (removeWholeElement(node)) {
            return false;
        }
        return true;
    }
    
    @Override
    public boolean visit(ConstructorInvocation node) {
        if (removeWholeElement(node)) {
            return false;
        }
        return true;
    }
    
    @Override
    public boolean visit(SuperConstructorInvocation node) {
        if (removeWholeElement(node)) {
            return false;
        }
        return true;
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
            pullUpMethodInvocations(node, node.getExpression());
            node.delete();
            return false;
        }
        
        return true;
    }
    
    @Override
    public boolean visit(EnhancedForStatement node) {
        if (removeWholeElement(node)) {
            return false;
        }
        
        if (!containsAnyInSubTree(node.getBody())) {
            pullUpMethodInvocations(node, node.getExpression());
            node.delete();
            return false;
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
            List<Expression> exprs = new ArrayList<>();
            exprs.addAll((List<Expression>)node.initializers());
            exprs.add(node.getExpression());
            exprs.addAll((List<Expression>)node.updaters());
            pullUpMethodInvocations(node, exprs);
            node.delete();
            return false;
        }
        
        List<Expression> removeNodes = new ArrayList<>();
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
            pullUpMethodInvocations(node, node.getExpression());
            node.delete();
            return false;
        }
        
        if (!(node.getThenStatement() instanceof Block) && !containsAnyInSubTree(node.getThenStatement())) {
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
            pullUpMethodInvocations(node, node.getExpression());
            node.delete();
            return false;
        }
        
        return true;
    }
    
    protected MethodDeclaration getEnclosingMethod(ASTNode node) {
        while (node != null) {
            if (node instanceof MethodDeclaration) {
                return (MethodDeclaration)node;
            }
            node = node.getParent();
        }
        return null;
    }
    
    @Override
    public boolean visit(ReturnStatement node) {
        if (contains(node)) {
            return true;
        }
        
        if (!contains(getEnclosingStatement(node.getParent())) && !contains(getEnclosingMethod(node.getParent()))) {
            node.delete();
            return false;
        }
        
        pullUpMethodInvocationInReturn(node);
        return true;
    }
    
    @SuppressWarnings("unchecked")
    protected Block pullUpMethodInvocationInReturn(ReturnStatement statement) {
        Expression returnExpression = getReturnExpression(statement);
        
        Expression expr = statement.getExpression();
        if (!containsAnyInSubTree(expr)) {
            statement.setExpression(returnExpression);
            return null;
        }
        
        List<MethodInvocation> invocations = new ArrayList<>();
        MethodInvocationCollector collector = new MethodInvocationCollector(expr);
        for (ASTNode astnode : collector.getNodes()) {
            if (astnode instanceof MethodInvocation) {
                invocations.add((MethodInvocation)astnode);
            }
        }
        
        if (invocations.size() > 0) {
            Block block = (Block)statement.getAST().newBlock();
            for (int index = 0; index < invocations.size(); index++) {
                Expression newExpression = (Expression)ASTNode.copySubtree(statement.getAST(), invocations.get(index));
                ExpressionStatement newStatement = (ExpressionStatement)statement.getAST().newExpressionStatement(newExpression);
                block.statements().add(newStatement);
            }
            
            ReturnStatement newStatement = (ReturnStatement)ASTNode.copySubtree(statement.getAST(), statement);
            newStatement.setExpression(returnExpression);
            block.statements().add(newStatement);
            
            replaceStatementWithBlock(statement, block);
            return block;
            
        } else {
            statement.setExpression(returnExpression);
            return null;
        }
    }
    
    protected Expression getReturnExpression(ReturnStatement node) {
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
            replaceStatementWithBlock(node, node.getBody());
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
