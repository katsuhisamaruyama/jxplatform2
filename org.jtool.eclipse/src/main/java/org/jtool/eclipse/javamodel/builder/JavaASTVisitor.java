/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.javamodel.builder;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.jtool.eclipse.javamodel.JavaClass;
import org.jtool.eclipse.javamodel.JavaField;
import org.jtool.eclipse.javamodel.JavaFile;
import org.jtool.eclipse.javamodel.JavaMethod;
import org.jtool.eclipse.javamodel.JavaPackage;
import java.util.List;
import java.util.Stack;

/**
 * Visits Java source code and stores its information.
 * All methods of this class are not intended to be directly called by clients.
 * 
 * @author Katsuhisa Maruyama
 */
public class JavaASTVisitor extends ASTVisitor {
    
    protected JavaFile jfile;
    private Stack<JavaClass> outerClasses = new Stack<JavaClass>();
    private JavaPackage jpackage = null;
    
    public JavaASTVisitor(JavaFile jfile) {
        this.jfile = jfile;
    }
    
    public void terminate() {
        jfile = null;
        outerClasses.clear();
        jpackage = null;
    }
    
    @Override
    public boolean visit(PackageDeclaration node) {
        jpackage = JavaPackage.create(node, jfile);
        jfile.setPackage(jpackage);
        return true;
    }
    
    @Override
    public boolean visit(ImportDeclaration node) {
        jfile.addImport(node);
        return true;
    }
    
    @Override
    public boolean visit(TypeDeclaration node) {
        JavaClass jclass = new JavaClass(node, jfile);
        visitClass(jclass);
        return true;
    }
    
    @Override
    public void endVisit(TypeDeclaration node) {
        endVisitClass();
    }
    
    @Override
    public boolean visit(AnonymousClassDeclaration node) {
        JavaClass jclass = new JavaClass(node, jfile);
        visitClass(jclass);
        return true;
    }
    
    public void endVisit(AnonymousClassDeclaration node) {
        endVisitClass();
    }
    
    @Override
    public boolean visit(EnumDeclaration node) {
        JavaClass jclass = new JavaClass(node, jfile);
        visitClass(jclass);
        return true;
    }
    
    @Override
    public void endVisit(EnumDeclaration node) {
        endVisitClass();
    }
    
    private void visitClass(JavaClass jclass) {
        if (!outerClasses.empty()) {
            JavaClass jc = outerClasses.peek();
            jc.addInnerClass(jclass);
        }
        outerClasses.push(jclass);
    }
    
    private void endVisitClass() {
        outerClasses.pop();
    }
    
    @Override
    public boolean visit(MethodDeclaration node) {
        if (outerClasses.empty()) {
            return false;
        }
        
        JavaClass jclass = outerClasses.peek();
        new JavaMethod(node, jclass);
        return true;
    }
    
    @Override
    public boolean visit(Initializer node) {
        if (outerClasses.empty()) {
            return false;
        }
        
        JavaClass jclass = outerClasses.peek();
        new JavaMethod(node, jclass);
        return true;
    }
    
    @Override
    public boolean visit(FieldDeclaration node) {
        if (outerClasses.empty()) {
            return false;
        }
        
        JavaClass jclass = outerClasses.peek();
        @SuppressWarnings("unchecked")
        List<VariableDeclarationFragment> fields = (List<VariableDeclarationFragment>)node.fragments();
        for (VariableDeclarationFragment fragment : fields) {
            new JavaField(fragment, jclass);
        }
        return false;
    }
    
    @Override
    public boolean visit(EnumConstantDeclaration node) {
        if (outerClasses.empty()) {
            return false;
        }
        
        JavaClass jclass = outerClasses.peek();
        new JavaField(node, jclass);
        return false;
    }
}
