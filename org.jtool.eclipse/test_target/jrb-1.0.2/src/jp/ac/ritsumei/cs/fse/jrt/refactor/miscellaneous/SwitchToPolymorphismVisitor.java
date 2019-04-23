/*
 *     SwitchToPolymorphismVisitor.java  Dec 11, 2001
 *
 *     Akihiko Kakimoto (kaki@fse.cs.ritsumei.ac.jp)
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.refactor.miscellaneous;
import jp.ac.ritsumei.cs.fse.jrt.refactor.util.*;
import jp.ac.ritsumei.cs.fse.jrt.parser.Node;
import jp.ac.ritsumei.cs.fse.jrt.parser.SimpleNode;
import jp.ac.ritsumei.cs.fse.jrt.parser.Token;
import jp.ac.ritsumei.cs.fse.jrt.parser.ast.*;
import jp.ac.ritsumei.cs.fse.jrt.model.*;
import jp.ac.ritsumei.cs.fse.jrt.graphs.cfg.*;
import jp.ac.ritsumei.cs.fse.jrt.graphs.util.*;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

public class SwitchToPolymorphismVisitor extends RefactoringVisitor {
    private JavaStatement jstatement;
    private JavaClass jclass;
    private JavaMethod jmethod;
    private List calledMethods;
    private JavaVariableList usedPrivateFields;
    private HashMap classMap;
    private CFGNode switchNode;
    private ArrayList caseBlockNodes = new ArrayList();
    private String methodDecl;
    private String newMethodDecl;
    private int rewriteIndex;
    private HashMap dstCodes = new HashMap();
    private List newCodes = new ArrayList();

    public SwitchToPolymorphismVisitor(JavaStatement jst, HashMap map, List methods, JavaVariableList vars) {
        super(jst);
        jstatement = jst;
        jmethod = jst.getJavaMethod();
        jclass = jst.getJavaClass();
        classMap = map;
        calledMethods = methods;
        usedPrivateFields = vars;
    }
    
    public HashMap getDstCodes() {
        return dstCodes;
    }

    public Object visit(ASTCompilationUnit node, Object data) {
        Object obj = node.childrenAccept(this, data);

        Iterator it = newCodes.iterator();
        while (it.hasNext()) {
            String newCode = (String)it.next();
            insertClass(node, node.jjtGetNumChildren(), "\n\n" + newCode);
        }
        return obj;
    }

    public Object visit(ASTUnmodifiedClassDeclaration node, Object data) {
        JavaClass jc = node.getJavaClass();
        Object obj = node.childrenAccept(this, data);

        if (jclass.equals(jc)) {
            Token token = node.getFirstToken();
            setHighlight(token);
            
            if (!jc.isAbstract()) {
                token.image = "\nabstract " + token.image;
            }
            token.changed = true;
        }
        return obj;
    }

    public Object visit(ASTClassBody node, Object data) {
        rewriteIndex = -1;
        Object obj = node.childrenAccept(this, data);

        if (rewriteIndex != -1) {
            insertMethod(node, rewriteIndex, "\n" + newMethodDecl);
        }
        return obj;
    }

    public Object visit(ASTVariableDeclarator node, Object data) {
        JavaStatement jst = node.getJavaStatement();
        Object obj = node.childrenAccept(this, data);

        JavaVariable jv = jst.getDeclaration();
        if (usedPrivateFields.contains(jv)) {
            changeModifier(node, "private", "protected");
        }
        return obj;
    }


    public Object visit(ASTMethodDeclarator node, Object data) {
        JavaMethod jm = node.getJavaMethod();
        Object obj = data;

        if (calledMethods.contains(jm)) {
            if (jm.isPrivate()) {
                changeModifier(node, "private", "protected");
            }
        }

        if (jmethod.equals(jm)) {
            methodDecl = createSrcMethod(node, jm);
            rewriteIndex = getChildIndex(node.jjtGetParent().jjtGetParent());

            obj = node.childrenAccept(this, data);

            StringBuffer buf = new StringBuffer();
            buf.append("    ");
            buf.append("abstract ");
            buf.append(methodDecl);
            buf.append(";");
            newMethodDecl = buf.toString();
            
            deleteMethod(node);
        }
        return obj;
    }

    private String createSrcMethod(Node node, JavaMethod jm) {
        jm.getModifier().remove("private");
        jm.getModifier().remove("protected");

        StringBuffer buf = new StringBuffer();
        buf.append(jm.getModifier().toString());
        buf.append(" ");
        buf.append(jm.getPrettyType());
        buf.append(" ");
        buf.append(jm.getName());

        PrintVisitor printer = new PrintVisitor();
        buf.append(printer.getCode(node.jjtGetChild(0)));

        return buf.toString();
    }

    public Object visit(ASTSwitchStatement node, Object data) {
        JavaStatement jst = node.getJavaStatement();
        Object obj = node.childrenAccept(this, data);

        if (jstatement.equals(jst)) {
            switchNode = (CFGNode)jst.getCFGNode();
            Token token = node.getFirstToken();
            int deleteNum =  token.beginColumn - 1;

            setHighlight(node);

            for (int i = 1; i < node.jjtGetNumChildren(); i++) {
                // node.jjtGetChild(i) instanceof ASTSwitchLabel
                ASTSwitchLabel switchLabelNode = (ASTSwitchLabel)node.jjtGetChild(i);

                JavaClass jc = (JavaClass)classMap.get(switchLabelNode.getLabel());
                if (jc.getJavaFile() != null) {
                    dstCodes.put(jc, createDstMethodCode(switchLabelNode, jc.getName(), deleteNum));
                } else {
                    newCodes.add(createDstClassCode(switchLabelNode, jc.getName(), deleteNum));
                }
            }
        }
        return obj;
    }

    private String createDstClassCode(ASTSwitchLabel node, String name, int num) {
        StringBuffer buf = new StringBuffer();
        buf.append("class ");
        buf.append(name);
        buf.append(" extends ");
        buf.append(jclass.getName());

        buf.append(" {\n");
        buf.append(createDstMethodCode(node, name, num));
        buf.append("}");

        return buf.toString();
    }

    private String createDstMethodCode(ASTSwitchLabel node, String name, int num) {
        JavaStatement jcase = node.getJavaStatement();
        List nodes = collectStatementsInSwitchLabel(jcase);
        nodes.remove(node);

        StringBuffer buf = new StringBuffer();
        buf.append("    ");
        buf.append(methodDecl);
        buf.append(" {");
        
        PrintVisitor printer = new PartialPrintVisitor(nodes);
        String text = printer.getCode(node);
        buf.append(eliminateSpaceAtHeadOfLine(text, num));

        buf.append("\n");
        buf.append("    }\n");

        return buf.toString();
    }

    private List collectStatementsInSwitchLabel(JavaStatement jcase) {
        caseBlockNodes.clear();
        CFGNode switchLabelNode = (CFGNode)jcase.getCFGNode();

        walkForward(switchLabelNode);
        return caseBlockNodes;
    }    

    private void walkForward(CFGNode cfgNode) {
        JavaComponent jc = cfgNode.getJavaComponent();
        if (cfgNode.getJavaComponent() != null) {
            Node node = cfgNode.getJavaComponent().getASTNode();

            if (caseBlockNodes.contains(node)) {
                return;
            }
            caseBlockNodes.add(node);
        }

        if (isSwichLabelEnd(cfgNode)) {
            return;
        }

        Iterator it = cfgNode.getOutgoingEdges().iterator();
        while (it.hasNext()) {
            Flow flow = (Flow)it.next();
            if (!flow.isFallThrough()) {  // flow.isTrue() || flow.isFalse()
                CFGNode succ = (CFGNode)flow.getDstNode();
                if (succ != null) {
                    walkForward(succ);
                }
            }
        }
    }

    private boolean isSwichLabelEnd(CFGNode cfgNode) {
        if (cfgNode.isMergeSt()) {
            CFGMergeNode merge = (CFGMergeNode)cfgNode;
            if (switchNode.equals(merge.getBranchNode())) {

                int last = caseBlockNodes.size() - 1;
                Node node = (Node)caseBlockNodes.get(last);
                if (node instanceof ASTBreakStatement) {
                    caseBlockNodes.remove(last);
                }
                return true;
            }
        }
        return false;
    }

    private String eliminateSpaceAtHeadOfLine(String text, int deleteNum) {
        StringBuffer buf = new StringBuffer();
        StringTokenizer st = new StringTokenizer(text, "\n", true);
        while (st.hasMoreTokens()) {
            String line = st.nextToken();
            int num = countSpacesAtHeadOfLine(line);
            if (deleteNum <= num) {
                num = deleteNum;
            }
            buf.append(line.substring(num));
        }
        return buf.toString();
    }

    private int countSpacesAtHeadOfLine(String text) {
        int count = 0;
        while (text.charAt(count) == ' ') {
            count++;
        }
        return count;
    }
}
