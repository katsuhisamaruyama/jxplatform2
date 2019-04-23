/*
 *     GraphNodeSort.java  Sep 10, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.graphs.util;

public interface GraphNodeSort {
    public static final int entries          = 1;
    public static final int classEntry       = 11;  // ASTUnmodifiedClassDeclaration
    public static final int interfaceEntry   = 12;  // ASTUnmodifiedInterfaceDeclaration
    public static final int methodEntry      = 13;  // ASTMethodDeclarator
    public static final int constructorEntry = 14;  // ASTConstructorDeclaration

    public static final int exits           = 2;
    public static final int classExit       = 21;
    public static final int interfaceExit   = 22;
    public static final int methodExit      = 23;
    public static final int constructorExit = 24;

    public static final int callings        = 3;
    public static final int methodCall      = 31;  // ASTArguments, ASTExplicitConstructorInvocation
    public static final int polymorphicCall = 32;

    public static final int parameters = 4;
    public static final int formalIn   = 41;  // ASTFormalParameter, ASTFieldDeclaration
    public static final int formalOut  = 42;  // ASTFormalParameter, ASTFieldDeclaration,
                                              // ASTMethodDeclarator
    public static final int actualIn   = 43;  // ASTArguments
    public static final int actualOut  = 44;  // ASTArguments

    public static final int assignments  = 5;
    public static final int assignmentSt = 51;  // ASTStatementExpression
    public static final int variableDecl = 52;  // ASTVariableDeclarator
    public static final int returnSt     = 53;  // ASTReturnStatement

    public static final int branches     = 6;
    public static final int switchLabel  = 61;  // ASTSwitchLabel
    public static final int ifSt         = 62;  // ASTIfStatement
    public static final int whileSt      = 63;  // ASTWhileStatement
    public static final int doSt         = 64;  // ASTDoStatement
    public static final int forSt        = 65;  // ASTForStatement
    public static final int breakSt      = 66;  // ASTBreakStatement
    public static final int continueSt   = 67;  // ASTContinueStatement

    public static final int nulls          = 7;
    public static final int labelSt        = 71;  // ASTLabeledStatement
    public static final int switchSt       = 72;  // ASTSwitchStatement
    public static final int mergeSt        = 73;

    public static final int ignores        = 8;
    public static final int throwSt        = 81;  // ASTThrowStatement
    public static final int synchronizedSt = 82;  // ASTSynchronizedStatement
    public static final int trySt          = 83;  // ASTTryStatement
}
