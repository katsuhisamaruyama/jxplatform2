/*
 *     SliceOnVariableVisitor.java  Jan 8, 2002
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.refactor.variables;
import jp.ac.ritsumei.cs.fse.jrt.refactor.util.*;
import jp.ac.ritsumei.cs.fse.jrt.parser.SimpleNode;
import jp.ac.ritsumei.cs.fse.jrt.parser.Node;
import jp.ac.ritsumei.cs.fse.jrt.graphs.GraphFactory;
import jp.ac.ritsumei.cs.fse.jrt.graphs.util.*;
import jp.ac.ritsumei.cs.fse.jrt.graphs.slice.*;
import jp.ac.ritsumei.cs.fse.jrt.graphs.pdg.*;
import jp.ac.ritsumei.cs.fse.jrt.graphs.cfg.*;
import jp.ac.ritsumei.cs.fse.jrt.parser.ast.*;
import jp.ac.ritsumei.cs.fse.jrt.model.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

public class SliceOnVariableVisitor extends RefactoringVisitor {
    private JavaVariable jvar;
    private int insertIndex;
    private String insertCode;

    public SliceOnVariableVisitor(JavaVariable jv) {
        super(jv);
        jvar = jv;
    }

    public Object visit(ASTClassBody node, Object data) {
        insertIndex = -1;
        Object obj = node.childrenAccept(this, data);

        if (insertIndex != -1) {
            insertCode(node, insertIndex, "\n" + insertCode);
        }
        return obj;
    }

    public Object visit(ASTMethodDeclarator node, Object data) {
        JavaMethod jm = node.getJavaMethod();
        Object obj = node.childrenAccept(this, data);

        if (jm.equals(jvar.getJavaMethod())) {
            PDG pdg = (PDG)jm.getPDG();
            // pdg.print();
            Slice slice = new Slice(jvar);
            // slice.print();
            List sliceNodes = getASTNodes(slice);
            sliceNodes.remove(node);
            List prettyNodes = adjust(sliceNodes);

            StringBuffer buf = new StringBuffer();
            buf.append("\n    /*\n");
            buf.append("    ");
            buf.append("public ");
            buf.append(jvar.getPrettyType());
            buf.append(" ");

            String topChar = jvar.getName().substring(0, 1);
            String methodName = topChar.toUpperCase() + jvar.getName().substring(1);
            buf.append("get");
            buf.append(methodName);
            buf.append(getFormalParameterCode(jm, slice));
            buf.append(" {");

            PrintVisitor printer = new PartialPrintVisitor(getASTNodes(pdg), prettyNodes);
            buf.append(printer.getCode(node.jjtGetParent()));

            buf.append("\n");
            buf.append(getReturnCode(jvar, slice));
            buf.append("    }\n");
            buf.append("    */\n");

            insertCode = buf.toString();
            Node n = node.jjtGetParent().jjtGetParent();
            insertIndex = getChildIndex(n) + 1;

            setHighlight(jvar.getToken());
        }
        return obj;
    }

    private List getASTNodes(PDG pdg) {
        List nodes = new ArrayList();

        Iterator it = pdg.getNodes().iterator();
        while (it.hasNext()) {
            PDGNode pdgNode = (PDGNode)it.next();
            CFGNode cfgNode = pdgNode.getCFGNode();
            Node astNode = cfgNode.getJavaComponent().getASTNode();
            if (astNode != null) {
                nodes.add(astNode);
            }
        }
        return nodes;
    }

    private List adjust(List pnodes) {
        List nodes = new ArrayList();

        Iterator it = pnodes.iterator();
        while (it.hasNext()) {
            SimpleNode node = (SimpleNode)it.next();
            nodes.add(node);
            
            if (node instanceof ASTVariableDeclarator) {
                nodes.add(node.jjtGetParent());
            }
        }
        return nodes;
    }

    private String getFormalParameterCode(JavaMethod jm, Slice slice) {
        StringBuffer buf = new StringBuffer();

        Iterator it = getFormalParameters(jm, slice).iterator();
        while (it.hasNext()) {
            JavaVariable jv = (JavaVariable)it.next();
            buf.append(" ");
            buf.append(jv.getPrettyType());
            buf.append(" ");
            buf.append(jv.getName());
        }
        String pCode = buf.toString();
        if (pCode.length() > 0) {
            pCode = pCode.substring(1);
        }

        return "(" + pCode + ")";
    }

    private List getFormalParameters(JavaMethod jm, Slice slice) {
        List params = new ArrayList();
        Iterator it = jm.getParameters().iterator();
        while (it.hasNext()) {
            JavaStatement jst = (JavaStatement)it.next();
            JavaVariable jv = (JavaVariable)jst.getDeclaration();
            if (isUsedInSlice(jv, slice)) {
                params.add(jv);
            }
        }
        return params;
    }

    private boolean isUsedInSlice(JavaVariable jv, Slice slice) {
        Iterator it = slice.getNodes().iterator();
        while (it.hasNext()) {
            PDGNode node = (PDGNode)it.next();
            if (node instanceof PDGStatementNode) {
                JavaVariableList defs = ((PDGStatementNode)node).getDefVariables();
                JavaVariableList uses = ((PDGStatementNode)node).getUseVariables();
                if (defs.contains(jv) || uses.contains(jv)) {
                    return true;
                }
            }
        }
        return false;
    }

    private String getReturnCode(JavaVariable jv, Slice slice) {
        PDGNode pdgNode = slice.getNode(jv);
        CFGNode cfgNode = pdgNode.getCFGNode();
        if (cfgNode.isReturnSt()) {
            return "";
        }
        return "        return " + jv.getName() + ";\n";
    }
}
