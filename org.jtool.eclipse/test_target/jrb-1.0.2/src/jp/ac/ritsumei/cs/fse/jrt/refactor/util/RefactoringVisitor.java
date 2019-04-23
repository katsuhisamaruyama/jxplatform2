/*
 *     RefactoringVisitor.java  Dec 17, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.refactor.util;
import jp.ac.ritsumei.cs.fse.jrt.parser.Node;
import jp.ac.ritsumei.cs.fse.jrt.parser.SimpleNode;
import jp.ac.ritsumei.cs.fse.jrt.parser.SimpleVisitor;
import jp.ac.ritsumei.cs.fse.jrt.parser.Token;
import jp.ac.ritsumei.cs.fse.jrt.parser.summary.*;
import jp.ac.ritsumei.cs.fse.jrt.parser.ast.*;
import jp.ac.ritsumei.cs.fse.jrt.model.*;
import java.util.ArrayList;
import java.util.Iterator;

public class RefactoringVisitor extends SimpleVisitor {
    private String text;
    private String temporaryCode = null;
    private ArrayList highliters = new ArrayList();  // HighlightToken

    protected RefactoringVisitor() {
    }

    protected RefactoringVisitor(JavaClass jc) {
        text = jc.getJavaFile().getText();
    }

    protected RefactoringVisitor(JavaMethod jm) {
        text = jm.getJavaClass().getJavaFile().getText();
    }

    protected RefactoringVisitor(JavaVariable jv) {
        text = jv.getJavaClass().getJavaFile().getText();
    }

    protected RefactoringVisitor(JavaStatement jst) {
        text = jst.getJavaClass().getJavaFile().getText();
    }

    protected RefactoringVisitor(String text) {
        this.text = text;
    }
     
    public String getTempCode() {
        return temporaryCode;
    }

    protected void setTempCode(String code) {
        temporaryCode = code;
    }

    protected void addTempCode(String code) {
        if (temporaryCode != null) {
            temporaryCode = temporaryCode + code;
        } else {
            temporaryCode =  code;
        }
    }

    public void setHighlight(HighlightToken ht) {
        highliters.add(ht);
    }

    public ArrayList getHighlights() {
        return highliters;
    }

    protected int getPosition(int line, int column) {
        int pos = 0;
        if (text.startsWith("\n")) {
            line--;
            column++;
        }
        for (int i = 1; i < line; i++) {
            if (pos < text.length()) {
                pos = text.indexOf("\n", pos + 1);
            }
        }
        int end = pos + column - 1;
        for (int i = pos; i < end; i++) {
            if (text.charAt(i) == '\t') {
                pos = pos + 4;
            } else {
                pos = pos + 1;
            }
        }
        return pos;
    }

    protected void shiftRight(Node node, int index) {
        for (int i = node.jjtGetNumChildren(); i > index; i--) {
            node.jjtAddChild(node.jjtGetChild(i - 1), i);
        }
    }

    protected int getChildIndex(Node node) {
        Node parent = node.jjtGetParent();
        for (int i = 0; i < parent.jjtGetNumChildren(); i++) {
            if (parent.jjtGetChild(i).equals(node)) {
                return i;
            }
        }
        return -1;
    }

    protected InsertCodeNode insertCode(Node node, int index, String code) {
        InsertCodeNode in = new InsertCodeNode(code);
        shiftRight(node, index);
        node.jjtAddChild(in, index);
        return in;
    }

    protected InsertCodeNode insertClass(Node node, int index, String code) {
        return insertCode(node, index, code);
    }

    protected InsertCodeNode insertMethod(Node node, int index, String code) {
        return insertCode(node, index, code);
    }

    protected InsertCodeNode insertField(Node node, int index, String code) {
        return insertCode(node, index, code);
    }

    protected void deleteClass(Node node) {
        Node n = node.jjtGetParent().jjtGetParent();
        int deleteIndex = getChildIndex(n);
        DeleteCodeNode dn = new DeleteCodeNode(n);
        n.jjtGetParent().jjtAddChild(dn, deleteIndex);
    }

    protected void deleteMethod(Node node) {
        Node n = node.jjtGetParent();
        DeleteCodeNode dn = new DeleteCodeNode(n);
        n.jjtGetParent().jjtAddChild(dn, 0);
    }

    protected void deleteField(Node node) {
        Node n = node.jjtGetParent();
        if (n.jjtGetNumChildren() == 2) {
            DeleteCodeNode dn = new DeleteCodeNode(n);
            n.jjtGetParent().jjtAddChild(dn, 0);
        } else {
            int deleteIndex = getChildIndex(node);
            DeleteCodeNode dn = new DeleteCodeNode(node);
            n.jjtAddChild(dn, deleteIndex);

            if (deleteIndex == 1) {
                ((SimpleNode)node).getLastToken().next.image = "";
            }
        }
    }

    protected void deleteNode(Node node) {
        DeleteCodeNode dn = new DeleteCodeNode(node);
        node.jjtGetParent().jjtAddChild(dn, getChildIndex(node));
    }

    protected void deleteNode(Node node, int index) {
        DeleteCodeNode dn = new DeleteCodeNode(node.jjtGetChild(index));
        node.jjtAddChild(dn, index);
    }

    protected void setHighlight(Token token) {
        int bpos = getPosition(token.beginLine, token.beginColumn);
        int epos = bpos + token.image.length();
        setHighlight(new HighlightToken(bpos, epos));
    }

    protected void setHighlight(Node n) {
        SimpleNode node = (SimpleNode)n;
        Token first = node.getFirstToken();
        Token last = node.getLastToken();

        int bpos = getPosition(first.beginLine, first.beginColumn);
        int epos = getPosition(last.endLine, last.endColumn) + 1;
        setHighlight(new HighlightToken(bpos, epos));
    }

    protected SimpleNode getPreviousNode(Node node) {
        if (node != null) {
            Node parent = node.jjtGetParent();
            for (int i = 1; i < parent.jjtGetNumChildren(); i++) {
                if (parent.jjtGetChild(i) == node) {
                    return (SimpleNode)parent.jjtGetChild(i - 1);
                }
            }
        }
        return null;
    }

    protected String createNewName(String name, JavaMethod jm) {
        int num = 1;
        while (checkNewName(name, jm)) {
            name = name + String.valueOf(num);
            if (num < 10000) {
                num++;
            } else {
                return "0" + "-No-adequate-name";
            }
        }            
        return name;
    }

    private boolean checkNewName(String name, JavaMethod jm) {
        Iterator it = jm.getJavaVariables().iterator();
        while (it.hasNext()) {
            JavaVariable jv = (JavaVariable)it.next();
            if (name.compareTo(jv.getName()) == 0) {
                return true;
            }
        }
        return false;
    }

    protected String collectTokens(Node node) {
        StringBuffer buf = new StringBuffer();
        SimpleNode sn = (SimpleNode)node;
        if (node.jjtGetNumChildren() > 0) {
            SimpleNode n = (SimpleNode)node.jjtGetChild(0);
            Token token = sn.getFirstToken();

            while (token != n.getFirstToken()) {
                collectSpecialToken(token, buf);
                buf.append(token.image);
                token = token.next;
            }
        }
        return buf.toString();
    }

    protected void collectSpecialToken(Token t, StringBuffer buf) {
        Token token = t.specialToken;
        if (token == null) {
            return;
        }
        while (token.specialToken != null) {
            token = token.specialToken;
        }
        while (token != null) {
            buf.append(token.image);
            token = token.next;
        }
    }

    protected void changeModifier(Node node, String oldmod, String newmod) {
        SimpleNode n = (SimpleNode)node.jjtGetParent();
        Token token = n.getFirstToken();
        while (token != null && !token.image.equals("private")) {
            token = token.next;
        }

        if (token != null) {
            setHighlight(token);

            token.image = "protected";
            token.changed = true;
        }
    }

    protected void eliminatePrefix(Token token) {
        if (token.next.image.equals(".") && token.image.equals("super")) {
            setHighlight(token);
            setHighlight(token.next);
            setHighlight(token.next.next);

            token.image = "";
            token.next.image = "";
            token.next.next.changed = true;
        }
    }
}
