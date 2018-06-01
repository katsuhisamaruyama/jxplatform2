/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.plugin;

import org.eclipse.core.resources.IFile;
import java.util.Set;

/**
 * An interface for handling received added, removed, and changed files.
 * @author Katsuhisa Maruyama
 */
public interface IFileChangeListener {
    
    public void fileAdded(Set<IFile> files);
    
    public void fileRemoved(Set<IFile> files);
    
    public void fileChanged(Set<IFile> files);
}
