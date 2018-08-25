/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.plugin;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import java.util.Set;
import java.util.HashSet;

/**
 * A listener implementation that sends added, removed, and changed files.
 * @author Katsuhisa Maruyama
 */
public class ResourceChangeListener implements IResourceChangeListener {
    
    private Set<IFileChangeListener> fileChangeListeners = new HashSet<IFileChangeListener>();
    
    ResourceChangeListener() {
    }
    
    void register() {
        ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
    }
    
    void unregister() {
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
    }
    
    void addFileChangeListener(IFileChangeListener listener) {
        fileChangeListeners.add(listener);
    }
    
    void removeFileChangeListener(IFileChangeListener listener) {
        fileChangeListeners.remove(listener);
    }
    
    private void notify(Set<IFile> addedFiles, Set<IFile> removedFiles, Set<IFile> changedFiles) {
        for (IFileChangeListener listener : fileChangeListeners) {
            listener.fileAdded(addedFiles);
            listener.fileRemoved(removedFiles);
            listener.fileChanged(changedFiles);
        }
    }
    
    @Override
    public void resourceChanged(IResourceChangeEvent event) {
        try {
            if (event.getType() == IResourceChangeEvent.POST_CHANGE) {
                ResourceDeltaVisitor visitor = new ResourceDeltaVisitor();
                event.getDelta().accept(visitor);
                notify(visitor.addedFiles, visitor.removedFiles, visitor.changedFiles);
            }
        } catch (CoreException e) {
            e.printStackTrace();
        }
    }
    
    class ResourceDeltaVisitor implements IResourceDeltaVisitor {
        
        Set<IFile> addedFiles = new HashSet<IFile>();
        Set<IFile> removedFiles = new HashSet<IFile>();
        Set<IFile> changedFiles = new HashSet<IFile>();
        
        @Override
        public boolean visit(IResourceDelta delta) throws CoreException {
            IResource resource = delta.getResource();
            if (resource != null && resource.getType() == IResource.FILE && ".java".equals(resource.getFileExtension())) {
                if (delta.getKind() == IResourceDelta.ADDED) {
                    addedFiles.add((IFile)resource);
                    ModelBuilderPlugin.getInstance().addFile((IFile)resource);
                } else if (delta.getKind() == IResourceDelta.REMOVED) {
                    removedFiles.add((IFile)resource);
                    ModelBuilderPlugin.getInstance().removeFile((IFile)resource);
                } else if (delta.getKind() == IResourceDelta.CHANGED) {
                    changedFiles.add((IFile)resource);
                    ModelBuilderPlugin.getInstance().changeFile((IFile)resource);
                }
            }
            return true;
        }
    }
}
