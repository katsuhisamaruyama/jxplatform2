/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.metrics;

import org.jtool.eclipse.javamodel.JavaField;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * An object storing metrics information on a method.
 * @author Katsuhisa Maruyama
 */
public class FieldMetrics extends CommonMetrics implements MetricsSort {
    
    public static final String Id = "FieldMetrics";
    
    protected JavaField.Kind kind;
    protected ClassMetrics classMetrics;
    
    public FieldMetrics(JavaField jfield, ClassMetrics mclass) {
        super(jfield.getQualifiedName(), jfield.getName(), jfield.getType(), jfield.getModifiers());
        
        kind = jfield.getKind();
        classMetrics = mclass;
        
        int start = jfield.getCodeRange().getStartPosition();
        int end = jfield.getCodeRange().getEndPosition();
        int upper = jfield.getCodeRange().getUpperLineNumber();
        int bottom = jfield.getCodeRange().getBottomLineNumber();
        setCodeProperties(start, end, upper, bottom);
        
        collectMetrics(jfield);
    }
    
    public FieldMetrics(String fqn, String name, String type, int modifiers, String kindStr, ClassMetrics mclass) {
        super(fqn, name, type, modifiers);
        
        this.kind = JavaField.Kind.valueOf(kindStr);
        classMetrics = mclass;
    }
    
    public ClassMetrics getDeclaringClass() {
        return classMetrics;
    }
    
    public String getDeclaringClassName() {
        return classMetrics.getQualifiedName();
    }
    
    public JavaField.Kind getKind() {
        return kind;
    }
    
    public boolean isField() {
        return kind == JavaField.Kind.J_FIELD;
    }
    
    public boolean isEnumConstant() {
        return kind == JavaField.Kind.J_ENUM_CONSTANT;
    }
    
    public String getSourceCode() {
        return super.getSourceCode(classMetrics.getFullPath());
    }
    
    private void collectMetrics(JavaField jfield) {
        putMetricValue(LINES_OF_CODE, new Double(bottom - upper + 1));
        putMetricValue(NUMBER_OF_STATEMENTS, new Double(1.0));
    }
    
    public static void sort(List<FieldMetrics> mfields) {
        Collections.sort(mfields, new Comparator<FieldMetrics>() {
            public int compare(FieldMetrics mfield1, FieldMetrics mfield2) {
                return mfield1.getName().compareTo(mfield2.getName());
            }
        });
    }
}
