/*
 *     DeleteFieldVisitor.java  Jan 17, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.refactor.fields;
import jp.ac.ritsumei.cs.fse.jrt.refactor.util.*;
import jp.ac.ritsumei.cs.fse.jrt.model.*;
import jp.ac.ritsumei.cs.fse.jrt.parser.Node;
import jp.ac.ritsumei.cs.fse.jrt.parser.ast.*;
import java.util.ArrayList;
import java.util.Iterator;

public class DeleteFieldVisitor extends RefactoringVisitor {
    private JavaVariable jvar;
    private String fieldCode;
    private int deleteIndex;
    
    public DeleteFieldVisitor(JavaVariable jv) {
        super(jv);
        jvar = jv;
    }

    public Object visit(ASTVariableDeclarator node, Object data) {
        JavaStatement jst = node.getJavaStatement();
        JavaVariable jv = jst.getDeclaration();
        Object obj = node.childrenAccept(this, data);

        if (jvar == jv) {
            deleteField(node);
            setHighlight(node);
        }
        return obj;
    }
}
