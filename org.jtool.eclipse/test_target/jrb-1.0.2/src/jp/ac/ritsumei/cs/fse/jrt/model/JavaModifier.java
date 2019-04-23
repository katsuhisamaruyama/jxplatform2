/*
 *     JavaModifier.java  Sep 3, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.model;
import java.util.ArrayList;
import java.util.Iterator;

public class JavaModifier {
    private ArrayList modifiers = new ArrayList();  // String

    public JavaModifier() {
    }

    public void add(String modifier) {
        if (!modifiers.contains(modifier)) {
            if (modifier.equals("abstract")) {
                modifiers.add(0, modifier);
            } else {
                modifiers.add(modifier);
            }
        }
    }

    public void add(int index, String modifier) {
        modifiers.add(index, modifier);
    }

    public int remove(String modifier) {
        int index = modifiers.indexOf(modifier);
        modifiers.remove(modifier);
        return index;
    }

    public boolean has(String m) {
        Iterator it = modifiers.iterator();
        while (it.hasNext()) {
            String modifier = (String)it.next();
            if (modifier.compareTo(m) == 0) {
                return true;
            }
        }
        return false;
    }

    public boolean isEmpty() {
        if (modifiers.size() == 0) {
            return true;
        }
        return false;
    }

    public int indexOf(String modifier) {
        return modifiers.indexOf(modifier);
    }

    public String toString() {
        if (modifiers.size() == 0) {
            return "";
        }
        StringBuffer buf = new StringBuffer();
        Iterator it = modifiers.iterator();
        while (it.hasNext()) {
           buf.append(" ");
           buf.append((String)it.next());
        }
        return buf.substring(1);
    }
}
