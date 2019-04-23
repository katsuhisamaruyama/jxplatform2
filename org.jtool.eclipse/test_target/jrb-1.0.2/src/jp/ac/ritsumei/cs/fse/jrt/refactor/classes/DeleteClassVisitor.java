/*
 *     DeleteClassVisitor.java  Jan 17, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.refactor.classes;
import jp.ac.ritsumei.cs.fse.jrt.refactor.util.*;
import jp.ac.ritsumei.cs.fse.jrt.model.*;
import jp.ac.ritsumei.cs.fse.jrt.parser.Node;
import jp.ac.ritsumei.cs.fse.jrt.parser.ast.*;
import java.util.ArrayList;
import java.util.Iterator;

public class DeleteClassVisitor extends RefactoringVisitor {
    private JavaClass jclass;

    public DeleteClassVisitor(JavaClass jc) {
        super(jc);
        jclass = jc;
    }

    public Object visit(ASTUnmodifiedClassDeclaration node, Object data) {
        JavaClass jc = node.getJavaClass();
        Object obj = node.childrenAccept(this, data);

        if (jclass.equals(jc)) {
            deleteClass(node);
            setHighlight(node.jjtGetParent());
        }
        return obj;
    }
}
