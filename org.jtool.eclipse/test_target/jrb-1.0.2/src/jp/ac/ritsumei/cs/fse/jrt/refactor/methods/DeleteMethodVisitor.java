/*
 *     DeleteMethodVisitor.java  Jan 17, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.refactor.methods;
import jp.ac.ritsumei.cs.fse.jrt.refactor.util.*;
import jp.ac.ritsumei.cs.fse.jrt.model.*;
import jp.ac.ritsumei.cs.fse.jrt.parser.ast.*;
import java.util.ArrayList;
import java.util.Iterator;

public class DeleteMethodVisitor extends RefactoringVisitor {
    private JavaMethod jmethod;

    public DeleteMethodVisitor(JavaMethod jm) {
        super(jm);
        jmethod = jm;
    }

    public Object visit(ASTMethodDeclarator node, Object data) {
        JavaMethod jm = node.getJavaMethod();
        Object obj = node.childrenAccept(this, data);

        if (jmethod.equals(jm)) {
            deleteMethod(node);
            setHighlight(node.jjtGetParent());
        }
        return obj;
    }
}
