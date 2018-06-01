/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.javamodel;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

/**
 * Stores information on the range of code.
 * @author Katsuhisa Maruyama
 */
public class CodeRange {
    
    private int startPosition = 0;
    private int endPosition = -1;
    private int upperLineNumber = 0;
    private int bottomLineNumber = -1;
    
    private int extendedStartPosition = 0;
    private int extendedEndPosition = -1;
    private int extendedUpperLineNumber = 0;
    private int extendedBottomLineNumber = -1;
    
    public CodeRange(ASTNode node) {
        if (node != null) {
            CompilationUnit cu = (CompilationUnit)node.getRoot();
            
            startPosition = node.getStartPosition();
            endPosition = node.getStartPosition() + node.getLength() - 1;
            upperLineNumber = cu.getLineNumber(startPosition);
            bottomLineNumber = cu.getLineNumber(endPosition);
            
            extendedStartPosition = cu.getExtendedStartPosition(node);
            extendedEndPosition = cu.getExtendedStartPosition(node) + cu.getExtendedLength(node) - 1;
            extendedUpperLineNumber = cu.getLineNumber(extendedStartPosition);
            extendedBottomLineNumber = cu.getLineNumber(extendedEndPosition);
        }
    }
    
    public void setCodeRange(int start, int end, int upper, int bottom, int exstart, int exend, int exupper, int exbottom) {
        startPosition = start;
        endPosition = end;
        upperLineNumber = upper;
        bottomLineNumber = bottom;
        
        extendedStartPosition = exstart;
        extendedEndPosition = exend;
        extendedUpperLineNumber = exupper;
        extendedBottomLineNumber = exbottom;
    }
    
    public void setCodeRange(int start, int end, int upper, int bottom) {
        setCodeRange(start, end, upper, bottom, start, end, upper, bottom);
    }
    
    public int getStartPosition() {
        return startPosition;
    }
    
    public int getEndPosition() {
        return endPosition;
    }
    
    public int getCodeLength() {
        return endPosition - startPosition + 1;
    }
    
    public int getExtendedStartPosition() {
        return extendedStartPosition;
    }
    
    public int getExtendedEndPosition() {
        return extendedEndPosition;
    }
    
    public int getExtendedCodeLength() {
        return extendedEndPosition - extendedStartPosition + 1;
    }
    
    public int getUpperLineNumber() {
        return upperLineNumber;
    }
    
    public int getExtendedUpperLineNumber() {
        return extendedUpperLineNumber;
    }
    
    public int getBottomLineNumber() {
        return bottomLineNumber;
    }
    
    public int getExtendedBottomLineNumber() {
        return extendedBottomLineNumber;
    }
    
    public int getLoc() {
        return bottomLineNumber - upperLineNumber + 1;
    }
    
    public int getExtendedLoc() {
        return extendedBottomLineNumber - extendedUpperLineNumber + 1;
    }
    
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("[");
        if (getStartPosition() >= 0) {
            buf.append(getStartPosition());
            buf.append("-");
            buf.append(getEndPosition());
        }
        buf.append("] ");
        return buf.toString();
    }
}
