/*
 *     ASTUnmodifiedInterfaceDeclaration.java  Aug 30, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.jrt.cs.ritsumei.ac.jp)
 */

/* Generated By:JJTree: Do not edit this line. ASTUnmodifiedInterfaceDeclaration.java */

package jp.ac.ritsumei.cs.fse.jrt.parser.ast;
import jp.ac.ritsumei.cs.fse.jrt.parser.*;
import jp.ac.ritsumei.cs.fse.jrt.model.JavaClass;

public class ASTUnmodifiedInterfaceDeclaration extends SimpleNode {
    private JavaClass jclass = new JavaClass(this);

    public ASTUnmodifiedInterfaceDeclaration(int id) {
        super(id);
    }

    public ASTUnmodifiedInterfaceDeclaration(JavaParser p, int id) {
        super(p, id);
    }

    public JavaClass getJavaClass() {
        return jclass;
    }

    public void setResponsive(Token token) {
        jclass.setResponsive(token.beginLine, token.beginColumn,
                             token.endLine, token.endColumn);
    }

    public Object jjtAccept(JavaParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}
