/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.slice;

import org.jtool.eclipse.javamodel.JavaMethod;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jface.text.IDocument;
import org.jtool.eclipse.javamodel.JavaMethod;
import org.jtool.eclipse.cfg.JLocalReference;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.LineComment;
import org.eclipse.jdt.core.dom.BlockComment;
import org.eclipse.jdt.core.dom.Javadoc;

import java.util.Map;
import java.util.HashMap;

public class JCodeFormatter {
    
    public JavaMethod jmethod;
    public int variableId;
    public String newName;
    
    public static String format(JavaMethod jmethod) {;
        CompilationUnit cu = (CompilationUnit)jmethod.getASTNode().getRoot();
        String contents = cu.toString();
        
        comments(cu);
        
        Map<String, String> options = new HashMap<String, String>();
        //CodeFormatter codeFormatter = ToolFactory.createCodeFormatter(options, ToolFactory.M_FORMAT_EXISTING);
        CodeFormatter codeFormatter = ToolFactory.createCodeFormatter(null);
        TextEdit edit = codeFormatter.format(CodeFormatter.K_COMPILATION_UNIT, contents, 0, contents.length(), 0, null);
        
        if (edit != null) {
            IDocument doc = new Document(contents);
            try {
                edit.apply(doc);
                return doc.get();
            } catch (MalformedTreeException | BadLocationException e) {
                e.printStackTrace();
            }
        }
        return contents;
    }
    
    private static void comments(CompilationUnit cu) {
        for (Object obj : cu.getCommentList()) {
            Comment comment = (Comment)obj;
            
            System.out.println(comment.toString());
            
        }
        
        
        // ASTRewrite rewriter = ASTRewrite.create(ast);
    }
}
