/*
 *     GraphComponentSet.java  Sep 13, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.graphs.util;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

public class GraphComponentSet implements java.io.Serializable {
    private List set = new ArrayList();

    public GraphComponentSet() {
    }

    public GraphComponentSet(GraphComponentSet s) {
        Iterator it = s.iterator();
        while (it.hasNext()) {
            GraphComponent c = (GraphComponent)it.next();
            set.add(c);
        }
    }

    public GraphComponentSet(Iterator it) {
        while (it.hasNext()) {
            GraphComponent c = (GraphComponent)it.next();
            set.add(c);
        }
    }    

    public void clear() {
        set.clear();
    }

    public boolean add(GraphComponent com) {
        GraphComponent c = getComponent(com);
        if (c == null) {
            set.add(com);
            return true;
        }
        return false;
    }

    public boolean remove(GraphComponent com) {
        GraphComponent c = getComponent(com);
        if (c != null) {
            set.remove(c);
            return true;
        }
        return false;
    }

    public void copy(GraphComponentSet s) {
        set.clear();
        Iterator it = s.iterator();
        while (it.hasNext()) {
            GraphComponent c = (GraphComponent)it.next();
            set.add(c);
        }
    }

    public boolean contains(GraphComponent com) {
        GraphComponent c = getComponent(com);
        if (c != null) {
            return true;
        }
        return false; 
    }

    public boolean isEmpty() {
        return set.isEmpty();
    }

    public int size() {
        return set.size();
    }

    public Iterator iterator() {
        return set.iterator();
    }

    public GraphComponent getFirst() {
        return (GraphComponent)set.get(0);
    }

    public boolean equals(GraphComponentSet s) {
        GraphComponentSet set1 = difference(s);        
        GraphComponentSet set2 = s.difference(this);        
        if (set1.isEmpty() && set2.isEmpty()) {
            return true;
        }
        return false;
    }

    public GraphComponentSet union(GraphComponentSet s) {
        GraphComponentSet newSet = new GraphComponentSet(this);
        Iterator it = s.iterator();
        while (it.hasNext()) {
            GraphComponent c = (GraphComponent)it.next();
            newSet.add(c);
        }
        return newSet;
    }                    

    public GraphComponentSet intersection(GraphComponentSet s) {
        GraphComponentSet newSet = new GraphComponentSet();
        Iterator it = s.iterator();
        while (it.hasNext()) {
            GraphComponent c = (GraphComponent)it.next();
            if (set.contains(c)) {
                newSet.add(c);
            }
        }
        return newSet;
    }

    public GraphComponentSet difference(GraphComponentSet s) {
        GraphComponentSet newSet = new GraphComponentSet(this);
        Iterator it = s.iterator();
        while (it.hasNext()) {
            GraphComponent c = (GraphComponent)it.next();
            newSet.remove(c);
        }
        return newSet;
    }

    public boolean subsetEqual(GraphComponentSet s) {
        GraphComponentSet newSet = difference(s);
        if (newSet.isEmpty()) {
            return true;
        }
        return false;
    }

    public boolean subset(GraphComponentSet s) {
        if (subsetEqual(s) && !equals(s)) {
            return true;
        }
        return false;
    }

    private GraphComponent getComponent(GraphComponent com) {
        Iterator it = set.iterator();
        while (it.hasNext()) {
            GraphComponent c = (GraphComponent)it.next();
            if (com.equals(c)) {
                return c;
            }
        }
        return null;
    }

    public GraphComponent[] toArray() {
        GraphComponent[] comp = new GraphComponent[set.size()];
        int i = 0;
        Iterator it = set.iterator();
        while (it.hasNext()) {
            comp[i] = (GraphComponent)it.next();
            i++;
        }
        return comp;
    }

    public void print() {
        Iterator it = set.iterator();
        while (it.hasNext()) {
            GraphComponent gc = (GraphComponent)it.next();
            if (gc instanceof GraphNode) {
                ((GraphNode)gc).print();
            } else if (gc instanceof GraphEdge) {
                ((GraphEdge)gc).print();
            }
        }
    }
}
