/*
 *     ASTMethodDeclaration.java  Aug 24, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.jrt.cs.ritsumei.ac.jp)
 */

/* Generated By:JJTree: Do not edit this line. ASTMethodDeclaration.java */

package jp.ac.ritsumei.cs.fse.jrt.parser.ast;
import jp.ac.ritsumei.cs.fse.jrt.parser.*;
import jp.ac.ritsumei.cs.fse.jrt.model.JavaVariable;

public class ASTMethodDeclaration extends SimpleNode {
    private JavaVariable formalOut = null;

    public ASTMethodDeclaration(int id) {
        super(id);
    }

    public ASTMethodDeclaration(JavaParser p, int id) {
        super(p, id);
    }

    public void setFormalOut(JavaVariable jvar) {
        formalOut = jvar;
    }

    public JavaVariable getFormalOut() {
        return formalOut;
    }

    public Object jjtAccept(JavaParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}
