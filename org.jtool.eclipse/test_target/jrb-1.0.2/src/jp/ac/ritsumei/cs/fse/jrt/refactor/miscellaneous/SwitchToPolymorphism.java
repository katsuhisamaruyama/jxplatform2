/*
 *     SwitchToPolymorphism.java  Dec 7, 2001
 *
 *     Akihiko Kakimoto (kaki@fse.cs.ritsumei.ac.jp)
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.refactor.miscellaneous;
import jp.ac.ritsumei.cs.fse.jrt.refactor.MiscellaneousRefactoring;
import jp.ac.ritsumei.cs.fse.jrt.refactor.util.*;
import jp.ac.ritsumei.cs.fse.jrt.refactor.dialog.*;
import jp.ac.ritsumei.cs.fse.jrt.model.*;
import jp.ac.ritsumei.cs.fse.jrt.graphs.cfg.*;
import jp.ac.ritsumei.cs.fse.jrt.graphs.util.*;
import jp.ac.ritsumei.cs.fse.jrt.parser.SimpleNode;
import jp.ac.ritsumei.cs.fse.jrt.parser.ast.ASTSwitchLabel;
import jp.ac.ritsumei.cs.fse.jrt.parser.summary.*;
import java.util.*;
import javax.swing.JFrame;

public class SwitchToPolymorphism extends MiscellaneousRefactoring {
    private JavaMethod jmethod;
    private JavaStatement jstatement;
    private HashMap dsts = new HashMap();
    private List calledMethods;
    private JavaVariableList usedPrivateFields;

    public SwitchToPolymorphism() {
        super();
    }

    public SwitchToPolymorphism(JFrame f, String dir, JavaStatement jst) {
        super(f, jst.getJavaClass().getJavaFile(), jst);
        setRootDir(dir);
        jstatement = jst;
        jmethod = jstatement.getJavaMethod();
        jclass = jstatement.getJavaClass();
    }

    protected void setUp() {
        jstatement = (JavaStatement)javaComp;
        jmethod = jstatement.getJavaMethod();
        jclass = jstatement.getJavaClass();  // jmethod != null
    }

    protected void preconditions() throws RefactoringException {
        impl.makeCFG(jclass);

        if (!existsOnlyOneSwitchStatementInMethod(jmethod)) {
            throw new RefactoringException("not change:"
              + " method " + jmethod.getName() + " contains multiple statements");
        }

        SummaryJavaClass sclass = impl.getJavaClass(jclass);
        List subclasses = sclass.getDescendants();
        HashMap classMap = createClassMap(subclasses);
        ArrayList switchLabels = getSwitchLabes(jstatement);
        ArrayList candidates = getCandidates(subclasses, switchLabels);
        HashMap map = CorrespondDialog.show(frame, "Correspondings", switchLabels, candidates);
        if (map.isEmpty()) {
            throw new RefactoringException("");
        }

        dsts = collectCorrespondingClasses(map, classMap);

        if (existsSameMethodInClasses(jmethod, dsts.values())) {
            throw new RefactoringException("already exists:"
              + " method " + jmethod.getName() + " in classes " + dsts.values());
        }

        calledMethods = impl.collectCalledMethodInClass(jmethod, jclass);
        usedPrivateFields = impl.collectVariablesReferringToPrivateFields(jmethod);
    }

    protected void transform() throws RefactoringException {
        SwitchToPolymorphismVisitor stransformer
          = new SwitchToPolymorphismVisitor(jstatement, dsts, calledMethods, usedPrivateFields);
        jfile.accept(stransformer);
        HashMap dstCodes = stransformer.getDstCodes();

        PrintVisitor printer = new PrintVisitor();
        String newCode = printer.getCode(jfile);

        DisplayedFile file = new DisplayedFile(jfile.getName(), jfile.getText(), newCode);
        file.setOldHighlight(stransformer.getHighlights());
        file.setNewHighlight(printer.getHighlights());
        changedFiles.add(file);

        Iterator it = dstCodes.keySet().iterator();
        while (it.hasNext()) {
            JavaClass dst = (JavaClass)it.next();
            newCode = (String)dstCodes.get(dst);
            
            RefactoringVisitor transformer = new InsertMethodVisitor(newCode, dst, jmethod);
            JavaFile dfile = dst.getJavaFile();
            dfile.accept(transformer);

            printer = new PrintVisitor();
            String dstCode = printer.getCode(dfile);

            file = new DisplayedFile(dfile.getName(), dfile.getText(), dstCode);
            file.setOldHighlight(transformer.getHighlights());
            file.setNewHighlight(printer.getHighlights());
            changedFiles.add(file);
        }
    }

    protected String getLog() {
        return "Replace Switch with Polymorphism: " + jclass.getName();
    }

    private boolean existsOnlyOneSwitchStatementInMethod(JavaMethod jm) {
        SimpleNode methodDecl = (SimpleNode)jm.getASTNode();
        SimpleNode block = (SimpleNode)methodDecl.jjtGetParent().jjtGetChild(2);
        if (block.jjtGetNumChildren() == 1) {
            return true;
        }
        return false;
    }

    private ArrayList getSwitchLabes(JavaStatement jst) {
        JavaMethod jm = jst.getJavaMethod();
        CFG cfg = (CFG)jm.getCFG();
        ArrayList labels = new ArrayList();

        Iterator it = cfg.getNodes().iterator();
        while (it.hasNext()) {
            CFGNode node = (CFGNode)it.next();
            if (node.isSwitchLabel()) {  // ASTSwitchLabel 
                ASTSwitchLabel astNode = (ASTSwitchLabel)node.getJavaComponent().getASTNode();
                labels.add(astNode.getLabel());
            }
        }
        return labels;
    }

    private HashMap createClassMap(List subclasses) {
        HashMap map = new HashMap();
        Iterator it = subclasses.iterator();
        while (it.hasNext()) {
            SummaryJavaClass sc = (SummaryJavaClass)it.next();
            map.put(sc.getLongName(), sc);
        }
        return map;
    }

    private ArrayList getCandidates(List subclasses, ArrayList labels) {
        ArrayList list = new ArrayList();
        Iterator it = subclasses.iterator();
        while (it.hasNext()) {
            SummaryJavaClass sc = (SummaryJavaClass)it.next();
            list.add(sc.getLongName());
        }

        it = labels.iterator();
        while (it.hasNext()) {
            String label = (String)it.next();
            
            String upperLabel = label.toUpperCase().replace('.', '_');

            System.out.println("UPPER" + upperLabel);

            while (checkLabel(list, upperLabel)) {
                upperLabel = upperLabel.charAt(0) + upperLabel;
            }
            list.add(upperLabel);
        }
        return list;
    }
    
    private boolean checkLabel(List list, String label) {
        Iterator it = list.iterator();
        while (it.hasNext()) {
            String name = (String)it.next();
            if (label.compareTo(name) == 0) {
                return true;
            }
        }
        return false;
    }

    private HashMap collectCorrespondingClasses(HashMap map, HashMap classMap) {
        HashMap dstMap = new HashMap(); 

        Iterator it = map.keySet().iterator();
        while (it.hasNext()) {
            String label = (String)it.next();
            String name = (String)map.get(label);
            SummaryJavaClass sc = (SummaryJavaClass)classMap.get(name);
            JavaClass jc = null;
            if (sc != null) {
                jc = impl.getJavaClass(sc);
            }
            if (jc == null) {
                jc = new JavaClass(name);
            }
            dstMap.put(label, jc);
        }
        return dstMap;
    }

    public boolean existsSameMethodInClasses(JavaMethod jm, Collection collection) {
        Iterator it = collection.iterator();
        while (it.hasNext()) {
            JavaClass jc = (JavaClass)it.next();
            if (impl.existsSameMethodInClass(jm, jc)) {
                return true;
            }
        }
        return false;
    }

    /*
    private boolean isConformable(CFGStatementNode node) {
        if (containsMethodCallInConditionalExpression(node)) {
            CFGCallNode callNode = getCallNode(node);
            if (callNode == null || callNode.getNumArguments() != 0) {
                return false;
            }
        }
        return true;
    }

    private boolean hasOperatorInConditionalExpression(CFGStatementNode node) {
        if (node.getUseVariables().size() > 1) {
            return true;
        }
        return false;
    }

    private boolean containsMethodCallInConditionalExpression(CFGStatementNode node) {
        JavaVariable jv = node.getUseVariables().getFirst();
        if (jv.getName().endsWith("{}")) {
            return true;
        }
        return false;
    }

    private CFGCallNode getCallNode(CFGNode node) {
        while (!node.isEntrySt()) {
            if (node.isCallSt()) {
                return (CFGCallNode)node;
            }
            node = (CFGNode)node.getPredecessors().getFirst();
        }
        return null;
    }
    */
}
