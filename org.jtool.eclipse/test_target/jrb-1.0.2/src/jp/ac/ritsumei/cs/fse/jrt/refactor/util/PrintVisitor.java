/*
 *     PrintVisitor.java  Nov 27, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.refactor.util;
import jp.ac.ritsumei.cs.fse.jrt.parser.JavaParserVisitor;
import jp.ac.ritsumei.cs.fse.jrt.parser.SimpleNode;
import jp.ac.ritsumei.cs.fse.jrt.parser.Node;
import jp.ac.ritsumei.cs.fse.jrt.parser.Token;
import jp.ac.ritsumei.cs.fse.jrt.parser.ast.*;
import jp.ac.ritsumei.cs.fse.jrt.model.JavaComponent;
import java.util.ArrayList;
import java.awt.Point;
import java.awt.Color;

public class PrintVisitor implements JavaParserVisitor {
    protected StringBuffer code = new StringBuffer();
    private ArrayList highliters = new ArrayList();  // HighlightToken

    public PrintVisitor() {
        super();
    }

    public String getCode(JavaComponent jc) {
        jc.accept(this, null);
        return code.toString();
    }

    public String getCode(Node node) {
        node.jjtAccept(this, null);
        return code.toString();
    }

    public void setHighlight(HighlightToken ht) {
        highliters.add(ht);
    }

    public ArrayList getHighlights() {
        return highliters;
    }

    protected Object print(SimpleNode node, Object data) {
        Token firstToken = node.getFirstToken();
        Token token = new Token();
        token.next = firstToken;

        for (int child = 0; child < node.jjtGetNumChildren(); child++) {
            SimpleNode n = (SimpleNode)node.jjtGetChild(child);

            if (n instanceof InsertCodeNode) {
                InsertCodeNode codeNode = (InsertCodeNode)n;

                int bpos = code.length();
                appendCode(codeNode.getCode());
                int epos = code.length();
                if (codeNode.hasChanged()) {
                    setHighlightForInsertNode(bpos, epos);
                }

            } else if (n instanceof DeleteCodeNode) {
                token = n.getLastToken();

            } else {
                while (true) {
                    token = token.next;
                    if (token == n.getFirstToken()) break;
                    print(node, token);
                }
                n.jjtAccept(this, data);
                token = n.getLastToken();
            }
        }

        while (token != node.getLastToken()) {
            token = token.next;
            print(node, token);
        }
        return data;
    }

    protected void print(SimpleNode node, Token token) {
        Token stoken = token.specialToken;
        if (stoken != null) {
            while (stoken.specialToken != null) {
                stoken = stoken.specialToken;
            }
            while (stoken != null) {
                appendCode(node, addUnicodeEscapes(stoken.image));
                stoken = stoken.next;
            }
        }

        if (token.changed) {
            int pos = code.length();
            setHighlight(new HighlightToken(pos, pos + token.image.length()));
        }
        if (token.toBeChanged) {
            int pos = code.length();
            setHighlight(new HighlightToken(pos, pos + token.image.length(), Color.red));
        }

        appendCode(node, addUnicodeEscapes(token.image));
    }

    protected void appendCode(String text) {
        code.append(text);
    }

    protected void appendCode(Node node, String text) {
        appendCode(text);
    }

    protected String addUnicodeEscapes(String str) {
        StringBuffer buf = new StringBuffer();
        String retval = "";
        for (int index = 0; index < str.length(); index++) {
            char ch = str.charAt(index);
            if ((ch < 0x20 || ch > 0x7e)
              && ch != '\t' && ch != '\n' && ch != '\r' && ch != '\f') {
                String s = "0000" + Integer.toString(ch, 16);
                buf.append("\\u" + s.substring(s.length() - 4, s.length()));
            } else {
                buf.append(ch);
            }
        }
        return buf.toString();
    }

    private void setHighlightForInsertNode(int bpos, int epos) {
        while (code.charAt(bpos) == '\n') {
            bpos++;
        }
        while (code.charAt(epos - 1) == '\n') {
            epos--;
        }

        if (bpos == epos) {
            while (code.charAt(bpos) == ' ') {
                bpos++;
            }
            while (code.charAt(epos - 1) == ' ') {
                epos--;
            }
        }
        setHighlight(new HighlightToken(bpos, epos));
    }

    public Object visit(SimpleNode node, Object data) {
        return print(node, data);
    }

    public Object visit(ASTCompilationUnit node, Object data) {
        return print(node, data);
    }

    public Object visit(ASTPackageDeclaration node, Object data) {
        return print(node, data);
    }

    public Object visit(ASTImportDeclaration node, Object data) {
        return print(node, data);
    }

    public Object visit(ASTTypeDeclaration node, Object data) {
        return print(node, data);
    }

    public Object visit(ASTClassDeclaration node, Object data) {
        return print(node, data);
    }

    public Object visit(ASTUnmodifiedClassDeclaration node, Object data) {
        return print(node, data);
    }

    public Object visit(ASTClassBody node, Object data) {
        return print(node, data);
    }

    public Object visit(ASTNestedClassDeclaration node, Object data) {
        return print(node, data);
    }

    public Object visit(ASTClassBodyDeclaration node, Object data) {
        return print(node, data);
    }

    public Object visit(ASTMethodDeclarationLookahead node, Object data) {
        return print(node, data);
    }

    public Object visit(ASTInterfaceDeclaration node, Object data) {
        return print(node, data);
    }

    public Object visit(ASTNestedInterfaceDeclaration node, Object data) {
        return print(node, data);
    }

    public Object visit(ASTUnmodifiedInterfaceDeclaration node, Object data) {
        return print(node, data);
    }

    public Object visit(ASTInterfaceMemberDeclaration node, Object data) {
        return print(node, data);
    }

    public Object visit(ASTFieldDeclaration node, Object data) {
        return print(node, data);
    }

    public Object visit(ASTVariableDeclarator node, Object data) {
        return print(node, data);
    }

    public Object visit(ASTVariableDeclaratorId node, Object data) {
        return print(node, data);
    }

    public Object visit(ASTVariableInitializer node, Object data) {
        return print(node, data);
    }

    public Object visit(ASTArrayInitializer node, Object data) {
        return print(node, data);
    }

    public Object visit(ASTMethodDeclaration node, Object data) {
        return print(node, data);
    }

    public Object visit(ASTMethodDeclarator node, Object data) {
        return print(node, data);
    }

    public Object visit(ASTFormalParameters node, Object data) {
        return print(node, data);
    }

    public Object visit(ASTFormalParameter node, Object data) {
        return print(node, data);
    }

    public Object visit(ASTConstructorDeclaration node, Object data) {
        return print(node, data);
    }

    public Object visit(ASTExplicitConstructorInvocation node, Object data) {
        return print(node, data);
    }

    public Object visit(ASTInitializer node, Object data) {
        return print(node, data);
    }

    public Object visit(ASTType node, Object data) {
        return print(node, data);
    }

    public Object visit(ASTPrimitiveType node, Object data) {
        return print(node, data);
    }

    public Object visit(ASTResultType node, Object data) {
        return print(node, data);
    }

    public Object visit(ASTName node, Object data) {
        return print(node, data);
    }

    public Object visit(ASTNameList node, Object data) {
        return print(node, data);
    }

    public Object visit(ASTExpression node, Object data) {
        return print(node, data);
    }

    public Object visit(ASTAssignmentOperator node, Object data) {
        return print(node, data);
    }

    public Object visit(ASTConditionalExpression node, Object data) {
        return print(node, data);
    }

    public Object visit(ASTConditionalOrExpression node, Object data) {
        return print(node, data);
    }

    public Object visit(ASTConditionalAndExpression node, Object data) {
        return print(node, data);
    }

    public Object visit(ASTInclusiveOrExpression node, Object data) {
        return print(node, data);
    }

    public Object visit(ASTExclusiveOrExpression node, Object data) {
        return print(node, data);
    }

    public Object visit(ASTAndExpression node, Object data) {
        return print(node, data);
    }

    public Object visit(ASTEqualityExpression node, Object data) {
        return print(node, data);
    }

    public Object visit(ASTInstanceOfExpression node, Object data) {
        return print(node, data);
    }

    public Object visit(ASTRelationalExpression node, Object data) {
        return print(node, data);
    }

    public Object visit(ASTShiftExpression node, Object data) {
        return print(node, data);
    }

    public Object visit(ASTAdditiveExpression node, Object data) {
        return print(node, data);
    }

    public Object visit(ASTMultiplicativeExpression node, Object data) {
        return print(node, data);
    }

    public Object visit(ASTUnaryExpression node, Object data) {
        return print(node, data);
    }

    public Object visit(ASTPreIncrementExpression node, Object data) {
        return print(node, data);
    }

    public Object visit(ASTPreDecrementExpression node, Object data) {
        return print(node, data);
    }

    public Object visit(ASTUnaryExpressionNotPlusMinus node, Object data) {
        return print(node, data);
    }

    public Object visit(ASTCastLookahead node, Object data) {
        return print(node, data);
    }

    public Object visit(ASTPostfixExpression node, Object data) {
        return print(node, data);
    }

    public Object visit(ASTCastExpression node, Object data) {
        return print(node, data);
    }

    public Object visit(ASTPrimaryExpression node, Object data) {
        return print(node, data);
    }

    public Object visit(ASTPrimaryPrefix node, Object data) {
        return print(node, data);
    }

    public Object visit(ASTPrimarySuffix node, Object data) {
        return print(node, data);
    }

    public Object visit(ASTResponsiveName node, Object data) {
        return print(node, data);
    }

    public Object visit(ASTLiteral node, Object data) {
        return print(node, data);
    }

    public Object visit(ASTBooleanLiteral node, Object data) {
        return print(node, data);
    }

    public Object visit(ASTNullLiteral node, Object data) {
        return print(node, data);
    }

    public Object visit(ASTArguments node, Object data) {
        return print(node, data);
    }

    public Object visit(ASTArgumentList node, Object data) {
        return print(node, data);
    }

    public Object visit(ASTAllocationExpression node, Object data) {
        return print(node, data);
    }

    public Object visit(ASTArrayDimsAndInits node, Object data) {
        return print(node, data);
    }

    public Object visit(ASTStatement node, Object data) {
        return print(node, data);
    }

    public Object visit(ASTLabeledStatement node, Object data) {
        return print(node, data);
    }

    public Object visit(ASTBlock node, Object data) {
        return print(node, data);
    }

    public Object visit(ASTBlockStatement node, Object data) {
        return print(node, data);
    }

    public Object visit(ASTLocalVariableDeclaration node, Object data) {
        return print(node, data);
    }

    public Object visit(ASTEmptyStatement node, Object data) {
        return print(node, data);
    }

    public Object visit(ASTStatementExpression node, Object data) {
        return print(node, data);
    }

    public Object visit(ASTSwitchStatement node, Object data) {
        return print(node, data);
    }

    public Object visit(ASTSwitchLabel node, Object data) {
        return print(node, data);
    }

    public Object visit(ASTIfStatement node, Object data) {
        return print(node, data);
    }

    public Object visit(ASTWhileStatement node, Object data) {
        return print(node, data);
    }

    public Object visit(ASTDoStatement node, Object data) {
        return print(node, data);
    }

    public Object visit(ASTForStatement node, Object data) {
        return print(node, data);
    }

    public Object visit(ASTForInit node, Object data) {
        return print(node, data);
    }

    public Object visit(ASTStatementExpressionList node, Object data) {
        return print(node, data);
    }

    public Object visit(ASTForUpdate node, Object data) {
        return print(node, data);
    }

    public Object visit(ASTBreakStatement node, Object data) {
        return print(node, data);
    }

    public Object visit(ASTContinueStatement node, Object data) {
        return print(node, data);
    }

    public Object visit(ASTReturnStatement node, Object data) {
        return print(node, data);
    }

    public Object visit(ASTThrowStatement node, Object data) {
        return print(node, data);
    }

    public Object visit(ASTSynchronizedStatement node, Object data) {
        return print(node, data);
    }

    public Object visit(ASTTryStatement node, Object data) {
        return print(node, data);
    }
}
