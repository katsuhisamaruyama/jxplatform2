/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.metrics;

import org.jtool.eclipse.javamodel.JavaMethod;

import java.util.List;
import java.util.Collections;
import java.util.Comparator;

/**
 * An object storing metrics information on a method.
 * @author Katsuhisa Maruyama
 */
public class MethodMetrics extends CommonMetrics implements MetricsSort {
    
    public static final String Id = "MethodMetrics";
    
    protected JavaMethod.Kind kind;
    protected ClassMetrics classMetrics;
    
    public MethodMetrics(JavaMethod jmethod, ClassMetrics mclass) {
        super(jmethod.getQualifiedName(), jmethod.getSignature(), jmethod.getReturnType(), jmethod.getModifiers());
        
        kind = jmethod.getKind();
        classMetrics = mclass;
        
        int start = jmethod.getCodeRange().getStartPosition();
        int end = jmethod.getCodeRange().getEndPosition();
        int upper = jmethod.getCodeRange().getUpperLineNumber();
        int bottom = jmethod.getCodeRange().getBottomLineNumber();
        setCodeProperties(start, end, upper, bottom);
        
        collectMetrics(jmethod);
    }
    
    public MethodMetrics(String fqn, String name, String type, int modifiers, String kindStr, ClassMetrics mclass) {
        super(fqn, name, type, modifiers);
        kind = JavaMethod.Kind.valueOf(kindStr);
        classMetrics = mclass;
    }
    
    public String getReturnType() {
        return type;
    }
    
    public String getSignature() {
        return name;
    }
    
    public ClassMetrics getDeclaringClass() {
        return classMetrics;
    }
    
    public String getDeclaringClassName() {
        return classMetrics.getQualifiedName();
    }
    
    public JavaMethod.Kind getKind() {
        return kind;
    }
    
    public boolean isMethod() {
        return kind == JavaMethod.Kind.J_METHOD;
    }
    
    public boolean isConstructor() {
        return kind == JavaMethod.Kind.J_CONSTRUCTOR;
    }
    
    public boolean isInitializer() {
        return kind == JavaMethod.Kind.J_INITIALIZER;
    }
    
    public boolean isLambda() {
        return kind == JavaMethod.Kind.J_LAMBDA;
    }
    
    public String getSourceCode() {
        return super.getSourceCode(classMetrics.getFullPath());
    }
    
    private void collectMetrics(JavaMethod jmethod) {
        putMetricValue(LINES_OF_CODE, new Double(bottom - upper + 1));
        putMetricValue(NUMBER_OF_STATEMENTS, new Double(jmethod.getNumberOfStatements()));
        
        putMetricValue(NUMBER_OF_AFFERENT_METHODS, new Double(jmethod.getCallingMethods().size()));
        putMetricValue(NUMBER_OF_EFFERENT_METHODS, new Double(jmethod.getCalledMethods().size()));
        putMetricValue(NUMBER_OF_AFFERENT_FIELDS, new Double(jmethod.getAccessingFields().size()));
        
        putMetricValue(NUMBER_OF_PARAMETERS, new Double(jmethod.getParameterSize()));
        putMetricValue(CYCLOMATIC_COMPLEXITY, new Double(jmethod.getCyclomaticNumber()));
        putMetricValue(NUMBER_OF_VARIABLES, new Double(jmethod.getLocalVariables().size()));
        putMetricValue(MAX_NUMBER_OF_NESTING, new Double(jmethod.getMaximumNumberOfNesting()));
    }
    
    public static void sort(List<MethodMetrics> mmethods) {
        Collections.sort(mmethods, new Comparator<MethodMetrics>() {
            public int compare(MethodMetrics method1, MethodMetrics method2) {
                return method1.getSignature().compareTo(method2.getSignature());
            }
        });
    }
}
