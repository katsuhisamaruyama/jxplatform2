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
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Statement;
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
        astNode.accept(this);
        
        CodeGenerator codeGenerator = new CodeGenerator();
        String code = codeGenerator.generate(astNode, jfile.getCode(), sliceNodes);
        
        return code;
    }
    
    private boolean contains(ASTNode astnode) {
        for (ASTNode node : sliceNodes) {
            if (astnode.getStartPosition() == node.getStartPosition()) {
                return true;
            }
        }
        return false;
    }
    
    private boolean containsAnyInSubTree(ASTNode astnode) {
        ASTNodeOnCFGCollector collector = new ASTNodeOnCFGCollector(astnode);
        for (ASTNode node : collector.getNodeSet()) {
            if (contains(node)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean removeWholeElement(ASTNode node) {
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
        
        checkDeclaration(node, node.fragments());
        return true;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public boolean visit(VariableDeclarationStatement node) {
        if (removeWholeElement(node)) {
            return false;
        }
        
        checkDeclaration(node, node.fragments());
        return true;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public boolean visit(VariableDeclarationExpression node) {
        if (removeWholeElement(node)) {
            return false;
        }
        
        checkDeclaration(node, node.fragments());
        return true;
    }
    
    private void checkDeclaration(ASTNode node, List<VariableDeclarationFragment> fragments) {
        List<VariableDeclarationFragment> removeNodes = new ArrayList<VariableDeclarationFragment>();
        for (VariableDeclarationFragment frag : fragments) {
            if (!containsAnyInSubTree(frag)) {
                removeNodes.add(frag);
            } else if (!contains(frag) && fragments.size() == 1) {
                pullUpExpression(frag, frag.getInitializer());
            }
        }
        for (VariableDeclarationFragment n : removeNodes) {
            n.delete();
        }
    }
    
    @SuppressWarnings("unchecked")
    private void pullUpExpression(ASTNode astnode, Expression expr) {
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
        if (removeWholeElement(node)) {
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
        return true;
    }
    
    @Override
    public boolean visit(EnhancedForStatement node) {
        if (removeWholeElement(node)) {
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
    
    @Override
    public boolean visit(ReturnStatement node) {
        if (contains(node)) {
            return true;
        }
        
        if (!contains(getEnclosingStatement(node.getParent())) && !contains(getEnclosingMethod(node.getParent()))) {
            node.delete();
            return false;
        }
        
        MethodDeclaration methodNode = getEnclosingMethod(node);
        if (methodNode != null) {
            Type type = methodNode.getReturnType2();
            if (type == null) {
                return false;
            }
            
            Expression newExpression;
            if (type.isPrimitiveType()) {
                if (type.toString().equals("boolean")) {
                    newExpression = node.getAST().newBooleanLiteral(false);
                } else if (type.toString().equals("char")) {
                    newExpression = node.getAST().newCharacterLiteral();
                } else {
                    newExpression = node.getAST().newNumberLiteral();
                }
            } else {
                newExpression = node.getAST().newNullLiteral();
            }
            node.setExpression(newExpression);
        }
        return true;
    }
}
