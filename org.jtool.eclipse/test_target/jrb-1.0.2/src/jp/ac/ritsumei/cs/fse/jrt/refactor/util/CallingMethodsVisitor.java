/*
 *     CallingMethodsVisitor.java  Jan 8, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.refactor.util;
import jp.ac.ritsumei.cs.fse.jrt.refactor.util.*;
import jp.ac.ritsumei.cs.fse.jrt.parser.Node;
import jp.ac.ritsumei.cs.fse.jrt.parser.SimpleNode;
import jp.ac.ritsumei.cs.fse.jrt.parser.Token;
import jp.ac.ritsumei.cs.fse.jrt.parser.ast.*;
import jp.ac.ritsumei.cs.fse.jrt.parser.summary.*;
import jp.ac.ritsumei.cs.fse.jrt.model.*;
import java.util.Iterator;

public class CallingMethodsVisitor extends RefactoringVisitor {
    private SummaryJavaMethod smethod;

    public CallingMethodsVisitor(JavaMethod jm, JavaFile jf) {
        super(jf.getText());
        SummaryJavaClass sc = jm.getJavaClass().getSummaryJavaClass();
        smethod = sc.getJavaMethod(jm.getSignature());
    }

    public Object visit(ASTArguments node, Object data) {
        Object obj = node.childrenAccept(this, data);

        SummaryJavaMethod sm = node.getCalledMethod();
        if (sm != null && sm.equals(smethod)) {
            SimpleNode n = getPreviousNode(node.jjtGetParent());
            if (n != null) {
                Token token = n.getLastToken();
                token.toBeChanged = true;
            }
        }
        return obj;
    }
}
