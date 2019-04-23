/*
 *     JavaVariableList.java  Sep 19, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.model;
import java.util.ArrayList;
import java.util.Iterator;

public class JavaVariableList implements java.io.Serializable {
    private ArrayList list = new ArrayList();

    public JavaVariableList() {
    }

    public JavaVariableList(JavaVariableList l) {
        Iterator it = l.iterator();
        while (it.hasNext()) {
            JavaVariable v = (JavaVariable)it.next();
            list.add(v);
        }
    }

    public JavaVariableList(ArrayList l) {
        list = l;
    }

    public void clear() {
        list.clear();
    }

    public boolean add(JavaVariable var) {
        if (!list.contains(var)) {
            list.add(var);
            return true;
        }
        return false;
    }

    public boolean remove(JavaVariable var) {
        if (list.contains(var)) {
            list.remove(var);
            return true;
        }
        return false;
    }

    public boolean contains(JavaVariable var) {
        JavaVariable v = getJavaVariable(var);
        if (v != null) {
            return true;
        }
        return false;
    }

    public boolean strictlyContains(JavaVariable var) {
        Iterator it = list.iterator();
        while (it.hasNext()) {
            JavaVariable v = (JavaVariable)it.next();
            if (var == v) {
                return true;
            }
        }
        return false;
    }

    public JavaVariable get(int i) {
        return (JavaVariable)list.get(i);
    }

    public JavaVariable getFirst() {
        return (JavaVariable)list.get(0);
    }

    public JavaVariable removeFirst() {
        return (JavaVariable)list.remove(0);
    }

    public JavaVariable getLast() {
        return (JavaVariable)list.get(list.size() - 1);
    }

    public JavaVariable removeLast() {
        return (JavaVariable)list.remove(list.size() - 1);
    }

    public boolean isEmpty() {
        if (list.size() == 0) {
            return true;
        }
        return false;
    }

    public int size() {
        return list.size();
    }

    public Iterator iterator() {
        return list.iterator();
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        Iterator it = list.iterator();
        while (it.hasNext()) {
            JavaVariable jvar = (JavaVariable)it.next();
            buf.append(" ");
            buf.append(jvar.getName());
        }
        if (buf.length() != 0) {
            return buf.substring(1);
        }
        return "";
    }

    public String toStringWithType() {
        if (list.size() == 0) {
            return "";
        }
        StringBuffer buf = new StringBuffer();
        Iterator it = list.iterator();
        while (it.hasNext()) {
            JavaVariable jvar = (JavaVariable)it.next();
            buf.append(" ");
            buf.append(jvar.getName());
            buf.append("(");
            buf.append(jvar.getID());
            buf.append(")");
            buf.append("#");
            buf.append(jvar.getType());
        }
        return buf.substring(1);
    }

    public String toStringWithPosition() {
        if (list.size() == 0) {
            return "";
        }
        StringBuffer buf = new StringBuffer();
        Iterator it = list.iterator();
        while (it.hasNext()) {
            JavaVariable jvar = (JavaVariable)it.next();
            buf.append(" ");
            buf.append(jvar.getNameWithPosition());
        }
        return buf.substring(1);
    }

    private JavaVariable getJavaVariable(JavaVariable var) {
        Iterator it = list.iterator();
        while (it.hasNext()) {
            JavaVariable v = (JavaVariable)it.next();
            if (var.equals(v)) {
                return v;
            }
        }
        return null;
    }

    public JavaVariable getJavaVariable(String name) {
        Iterator it = list.iterator();
        while (it.hasNext()) {
            JavaVariable v = (JavaVariable)it.next();
            if (name.compareTo(v.getName()) == 0) {
                return v;
            }
        }
        return null;
    }
}
