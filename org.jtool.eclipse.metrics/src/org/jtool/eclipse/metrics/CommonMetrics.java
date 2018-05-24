/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.metrics;

/**
 * Stores metric values and information common to a class, method, and field.
 * @author Katsuhisa Maruyama
 */
public class CommonMetrics extends Metrics {
    
    protected String name;
    protected String type;
    protected int modifiers;
    
    protected int start = -1;
    protected int end = 0;
    protected int upper = -1;
    protected int bottom = 0;
    
    protected CommonMetrics(String fqn, String name, String type, int modifiers) {
        super(fqn);
        this.name = name;
        this.type = type;
        this.modifiers = modifiers;
    }
    
    protected void setCodeProperties(int start, int end, int upper, int bottom) {
        this.start = start;
        this.end = end;
        this.upper = upper;
        this.bottom = bottom;
    }
    
    public String getName() {
        return name;
    }
    
    public String getType() {
        return type;
    }
    
    public int getModifiers() {
        return modifiers;
    }
    
    public int getStartPosition() {
        return start;
    }
    
    public int getEndPosition() {
        return end;
    }
    
    public int getUpperLineNumber() {
        return upper;
    }
    
    public int getBottomLineNumber() {
        return bottom;
    }
    
    public String getSourceCode(String path) {
        String code = SourceCodeStore.getInstance().get(path);
        if (code != null && code.length() > 0 && end - start >= 0) {
            return code.substring(start, end + 1);
        }
        return "";
    }
}
