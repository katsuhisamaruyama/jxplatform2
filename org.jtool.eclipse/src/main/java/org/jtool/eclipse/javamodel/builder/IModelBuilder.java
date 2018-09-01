/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.javamodel.builder;

import org.jtool.eclipse.javamodel.JavaProject;

/**
 * An interface for building a Java model.
 * 
 * @author Katsuhisa Maruyama
 */
public interface IModelBuilder {
    
    public boolean isUnderPlugin();
    
    public JavaProject getCurrentProject();
    
    public JavaProject update();
    
    public void resisterBytecodeClasses(BytecodeClassStore bytecodeClassStore);
}
