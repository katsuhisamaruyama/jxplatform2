/*
 *     GraphFactory.java  Apr 10, 2003
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.graphs;
import jp.ac.ritsumei.cs.fse.jrt.graphs.util.GraphNode;
import jp.ac.ritsumei.cs.fse.jrt.graphs.cfg.CFGFactory;
import jp.ac.ritsumei.cs.fse.jrt.graphs.cfg.CCFG;
import jp.ac.ritsumei.cs.fse.jrt.graphs.cfg.CFG;
import jp.ac.ritsumei.cs.fse.jrt.graphs.pdg.ClDG;
import jp.ac.ritsumei.cs.fse.jrt.graphs.pdg.PDG;
import jp.ac.ritsumei.cs.fse.jrt.model.*;
import java.util.Iterator;

public class GraphFactory {
    private static GraphFactory factory = new GraphFactory();

    private GraphFactory() {
    }

    public static GraphFactory getInstance() {
        return factory;
    }

    public void makeCFG(JavaFile jfile) {
        JavaClass jclass = (JavaClass)jfile.getJavaClasses().get(0);
        if (jclass.getCCFG() != null) {
            return;
        }

        GraphNode.clearID();
        jfile.accept(CFGFactory.getInstance());
    }

    public void makeCFG(JavaClass jclass) {
        if (jclass.getCCFG() != null) {
            return;
        }
        makeCFG(jclass.getJavaFile());
    }

    public void makePDG(JavaFile jfile) {
        JavaClass jclass = (JavaClass)jfile.getJavaClasses().get(0);
        if (jclass.getClDG() != null) {
            return;
        }

        makeCFG(jfile);
        Iterator it = jfile.getJavaClasses().iterator();
        while (it.hasNext()) {
            jclass = (JavaClass)it.next();
            makePDG(jclass);
        }
    }

    public void makePDG(JavaClass jclass) {
        if (jclass.getClDG() != null) {
            return;
        }

        makeCFG(jclass);
        Iterator it = jclass.getJavaMethods().iterator();
        while (it.hasNext()) {
            JavaMethod jmethod = (JavaMethod)it.next();
            PDG pdg = new PDG((CFG)jmethod.getCFG());
            jmethod.setPDG(pdg);
        }
        // makeBinding();
        jclass.setClDG(new ClDG());
    }
}
