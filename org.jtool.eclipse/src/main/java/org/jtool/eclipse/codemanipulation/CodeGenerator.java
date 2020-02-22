/*
 *  Copyright 2019
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.codemanipulation;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.BlockComment;
import org.eclipse.jdt.core.dom.LineComment;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.internal.core.dom.NaiveASTFlattener;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Generates Java source code from an AST.
 * 
 * @author Katsuhisa Maruyama
 */
@SuppressWarnings("restriction")
public class CodeGenerator {
    
    private Map<String, String> options = null;
    
    public CodeGenerator() {
    }
    
    public void setOptions(Map<String, String> options) {
        this.options = options;
    }
    
    public String generate(ASTNode node, String contents) {
        return generate(node, contents, null);
    }
    
    public String generate(ASTNode node, String contents, Set<ASTNode> nodes) {
        Set<Comment> comments = null;
        ASTNode root = node.getRoot();
        if (root instanceof CompilationUnit) {
            CommentVisitor commentVistor = new CommentVisitor((CompilationUnit)root, nodes);
            comments = commentVistor.collect();
        } else {
            comments = new HashSet<Comment>();
        }
        
        CodeRestrationVisitor restrationVisitor = new CodeRestrationVisitor(comments, contents);
        String code = restrationVisitor.restore(node);
        String newCode = contents;
        if (node instanceof AbstractTypeDeclaration) {
            newCode = format(code, CodeFormatter.K_COMPILATION_UNIT);
        } else if (node instanceof MethodDeclaration || node instanceof Initializer || node instanceof VariableDeclaration) {
            newCode = format(code, CodeFormatter.K_CLASS_BODY_DECLARATIONS);
        } else if (node instanceof Statement) {
            newCode = format(code, CodeFormatter.K_STATEMENTS);
        } else if (node instanceof Expression) {
            newCode = format(code, CodeFormatter.K_EXPRESSION);
        } else {
            newCode = format(code, CodeFormatter.K_UNKNOWN);
        }
        return newCode;
    }
    
    private String format(String code, int kind) {
        try {
            IDocument document = new Document(code);
            if (options == null) {
                options = new HashMap<String, String>();
                options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.SPACE);
                options.put(DefaultCodeFormatterConstants.FORMATTER_INDENTATION_SIZE, "4");
                options.put(DefaultCodeFormatterConstants.FORMATTER_BLANK_LINES_AFTER_PACKAGE, "1");
                options.put(DefaultCodeFormatterConstants.FORMATTER_BLANK_LINES_AFTER_IMPORTS, "1");
                options.put(DefaultCodeFormatterConstants.FORMATTER_BLANK_LINES_BEFORE_MEMBER_TYPE, "1");
                options.put(DefaultCodeFormatterConstants.FORMATTER_BLANK_LINES_BEFORE_FIRST_CLASS_BODY_DECLARATION, "1");
                options.put(DefaultCodeFormatterConstants.FORMATTER_BLANK_LINES_BEFORE_METHOD, "1");
                options.put(DefaultCodeFormatterConstants.FORMATTER_BLANK_LINES_BEFORE_FIELD, "1");
            }
            
            CodeFormatter codeFormatter = ToolFactory.createCodeFormatter(options, ToolFactory.M_FORMAT_NEW);
            TextEdit edit = codeFormatter.format(kind | CodeFormatter.F_INCLUDE_COMMENTS, code, 0, code.length(), 0, null);
            edit.apply(document);
            return document.get();
        } catch (MalformedTreeException | BadLocationException e) {
            // e.printStackTrace();
        }
        return code;
    }
    
    class CommentVisitor extends ASTVisitor {
        
        private CompilationUnit compilationUnit;
        private Set<ASTNode> nodes;
        
        private Set<Comment> comments = new HashSet<Comment>();
        
        CommentVisitor(CompilationUnit cu) {
            this(cu, null);
        }
        
        CommentVisitor(CompilationUnit cu, Set<ASTNode> nodes) {
            this.compilationUnit = cu;
            this.nodes = nodes;
        }
        
        Set<Comment> collect() {
            for (Object obj : compilationUnit.getCommentList()) {
                Comment comment = (Comment)obj;
                comment.accept(this);
            }
            return comments;
        }
        
        @Override
        public boolean visit(LineComment node) {
            visitComment(node);
            return false;
        }
        
        @Override
        public boolean visit(BlockComment node) {
            visitComment(node);
            return false;
        }
        
        private void visitComment(Comment comment) {
            int cstart = comment.getStartPosition();
            int cend = cstart + comment.getLength();
            int cstartLineNumber = compilationUnit.getLineNumber(cstart);
            int cendLineNumber = compilationUnit.getLineNumber(cend);
            
            if (nodes == null) {
                comments.add(comment);
                return;
            }
            
            for (ASTNode node : nodes) {
                int start = node.getStartPosition();
                int end = cstart + node.getLength();
                int startLineNumber = compilationUnit.getLineNumber(start);
                int endLineNumber = compilationUnit.getLineNumber(end);
                
                if (comment instanceof LineComment) {
                    if (cstartLineNumber == startLineNumber && cendLineNumber == endLineNumber) {
                        comments.add(comment);
                    }
                }
                
                if (cendLineNumber + 1 == startLineNumber) {
                    comments.add(comment);
                }
            }
        }
    }
    
    class CodeRestrationVisitor extends NaiveASTFlattener {
        
        private String contents;
        private Set<Comment> comments = new HashSet<Comment>();
        private int pos = 0;
        
        CodeRestrationVisitor(Set<Comment> comments, String contents) {
            this.comments = comments;
            this.contents = contents;
        }
        
        String restore(ASTNode node) {
            node.accept(this);
            return super.getResult();
        }
        
        @Override
        public void preVisit(ASTNode node) {
            insertCommentBefore(node);
            super.preVisit(node);
        }
        
        @Override
        public void postVisit(ASTNode node) {
            pos = node.getStartPosition() + node.getLength();
            insertCommentAfter(node);
            super.postVisit(node);
        }
        
        private void insertCommentBefore(ASTNode node) {
            Set<Comment> removedComments = new HashSet<Comment>();
            for (Comment comment : comments) {
                int start = comment.getStartPosition();
                int end = start + comment.getLength();
                
                if (pos <= start && end < node.getStartPosition()) {
                    String fragment = contents.substring(start, end) + "\n";
                    buffer.append(fragment);
                    pos = end;
                    
                    removedComments.add(comment);
                }
            }
            for (Comment comment : removedComments) {
                comments.remove(comment);
            }
        }
        
        private void insertCommentAfter(ASTNode node) {
            Set<Comment> removedComments = new HashSet<Comment>();
            for (Comment comment : comments) {
                int start = comment.getStartPosition();
                int end = start + comment.getLength();
                
                int nend = node.getStartPosition() + node.getLength();
                if (nend < start) {
                    String text = contents.substring(nend, start);
                    if (containsOnlyWhiteSpaces(text.toCharArray())) {
                        String fragment = contents.substring(start, end) + "\n";
                        if (buffer.charAt(buffer.length() - 1) == '\n') {
                            buffer.deleteCharAt(buffer.length() - 1);
                            buffer.append("  ");
                        }
                        buffer.append(fragment);
                        
                        pos = end;
                        removedComments.add(comment);
                    }
                }
            }
            for (Comment comment : removedComments) {
                comments.remove(comment);
            }
        }
        
        private boolean containsOnlyWhiteSpaces(char[] ch) {
            for (int i = 0; i < ch.length; i++) {
                if (ch[i] != '\t' && ch[i] != ' ') {
                    return false;
                }
            }
            return true;
        }
    }
}
