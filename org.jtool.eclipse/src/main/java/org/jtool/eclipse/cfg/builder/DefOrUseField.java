/*
 *  Copyright 2020
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg.builder;

import static org.jtool.eclipse.javamodel.JavaElement.QualifiedNameSeparator;

/**
 * A class that represents a field accessed within a method body.
 * 
 * @author Katsuhisa Maruyama
 */
class DefOrUseField {
    
    private String className;
    private String name;
    private boolean isPrimitive;
    private int modifier;
    
    static final DefOrUseField UNKNOWN = new DefOrUseField("", "", false, -1);
    
    protected DefOrUseField(String className, String name, boolean isPrimitive, int modifier) {
        this.className = className;
        this.name = name;
        this.isPrimitive = isPrimitive;
        this.modifier = modifier;
    }
    
    String getQualifiedName() {
        return className + QualifiedNameSeparator + name;
    }
    
    String getClassName() {
        return className;
    }
    
    String getName() {
        return name;
    }
    
    boolean isPrimitive() {
        return isPrimitive;
    }
    
    int getModifier() {
        return modifier;
    }
    
    boolean equals(DefOrUseField field) {
        return field != null && (this == field || getQualifiedName().equals(field.getQualifiedName()));
    }
    
    @Override
    public boolean equals(Object obj) {
        return (obj instanceof DefOrUseField) ? equals((DefOrUseField)obj) : false;
    }
    
    @Override
    public int hashCode() {
        return getQualifiedName().hashCode();
    }
    
    @Override
    public String toString() {
        return getQualifiedName() + QualifiedNameSeparator +
                String.valueOf(isPrimitive) + QualifiedNameSeparator + String.valueOf(modifier);
    }
    
    static DefOrUseField instance(String str) {
        String[] s = str.split(QualifiedNameSeparator);
        return new DefOrUseField(s[0], s[1], Boolean.parseBoolean(s[2]), Integer.parseInt(s[3]));
    }
    
    static boolean isUnknown(String str) {
        return str.equals(UNKNOWN.toString());
    }
}
