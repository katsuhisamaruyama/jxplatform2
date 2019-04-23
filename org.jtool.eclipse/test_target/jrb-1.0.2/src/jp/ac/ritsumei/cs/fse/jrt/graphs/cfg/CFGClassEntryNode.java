/*
 *     CFGClassEntryNode.java  Sep 19, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.graphs.cfg;
import jp.ac.ritsumei.cs.fse.jrt.graphs.util.*;
import jp.ac.ritsumei.cs.fse.jrt.model.*;

public class CFGClassEntryNode extends CFGNode {
    private CFGClassEntryNode() {
        super();
    }

    public CFGClassEntryNode(int sort, JavaComponent comp) {
        super(sort, comp);
    }

    public String getName() {
        JavaClass jclass = (JavaClass)javaComp;
        return jclass.getName();
    }

    public void print() {
        String mesg = "";
        switch (sort) {
            case classEntry: mesg = "class entry"; break;
            case interfaceEntry: mesg = "interface entry"; break;
        }
        print(mesg + " " + getName());
    }
}
