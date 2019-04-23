/*
 *     InsertClassVisitor.java  Dec 6, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.refactor.util;
import jp.ac.ritsumei.cs.fse.jrt.refactor.*;
import jp.ac.ritsumei.cs.fse.jrt.parser.ast.*;

public class InsertClassVisitor extends RefactoringVisitor {
    private String insertCode;

    public InsertClassVisitor(String code) {
        super();
        insertCode = "\n" + code;
    }

    public Object visit(ASTCompilationUnit node, Object data) {
        Object obj = node.childrenAccept(this, data);

        if (insertCode != null) {
            insertClass(node, node.jjtGetNumChildren(), insertCode);
        }
        return obj;
    }
}
