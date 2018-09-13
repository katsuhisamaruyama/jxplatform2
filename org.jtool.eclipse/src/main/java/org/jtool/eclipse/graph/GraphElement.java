/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.graph;

import java.util.Set;
import java.util.HashSet;

/**
 * An element of a graph.
 * @author Katsuhsa Maruyama
 */
public abstract class GraphElement {
    
    protected GraphElement() {
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof GraphElement) {
            return equals((GraphElement)obj);
        }
        return false;
    }
    
    public abstract boolean equals(GraphElement elem);
    
    public static <E extends GraphElement> boolean equals(Set<E> s1, Set<E> s2) {
        if (s1.size() != s2.size()) {
            return false;
        }
        Set<E> s = difference(s1, s2);
        return s.isEmpty();
    }
    
    public static <E extends GraphElement> Set<E> difference(Set<E> s1, Set<E> s2) {
        Set<E> s = new HashSet<E>();
        s.addAll(s1);
        for (E e : s2) {
            s.remove(e);
        }
        return s;
    }
    
    public static <E extends GraphElement> Set<E> union(Set<E> s1, Set<E> s2) {
        Set<E> s = new HashSet<E>();
        s.addAll(s1);
        s.addAll(s2);
        return s;
    }
    
    public static <E extends GraphElement> Set<E> intersection(Set<E> s1, Set<E> s2) {
        Set<E> s = new HashSet<E>();
        if (s1.size() > s2.size()) {
            for (E e : s2) {
                if (s1.contains(e)) {
                    s.add(e);
                }
            }
        } else {
            for (E e : s1) {
                if (s2.contains(e)) {
                    s.add(e);
                }
            }
        }
        return s;
    }
    
    public static <E extends GraphElement> boolean subsetEqual(Set<E> s1, Set<E> s2) {
        Set<E> s = difference(s1, s2);
        return s.isEmpty();
    }
    
    public static <E extends GraphElement> boolean subset(Set<E> s1, Set<E> s2) {
        return subsetEqual(s1, s2) && s1.size() < s2.size();
    }
}
