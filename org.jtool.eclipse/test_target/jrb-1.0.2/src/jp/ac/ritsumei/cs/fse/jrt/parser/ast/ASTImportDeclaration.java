/*
 *     ASTImportDeclaration.java  Aug 27, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.jrt.cs.ritsumei.ac.jp)
 */

/* Generated By:JJTree: Do not edit this line. ASTImportDeclaration.java */

package jp.ac.ritsumei.cs.fse.jrt.parser.ast;
import jp.ac.ritsumei.cs.fse.jrt.parser.*;

public class ASTImportDeclaration extends SimpleNode {
    public ASTImportDeclaration(int id) {
        super(id);
    }

    public ASTImportDeclaration(JavaParser p, int id) {
        super(p, id);
    }

    public Object jjtAccept(JavaParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}
