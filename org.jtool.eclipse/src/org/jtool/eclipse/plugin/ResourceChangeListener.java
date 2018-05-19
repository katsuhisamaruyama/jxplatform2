/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.plugin;

import org.jtool.eclipse.model.java.JavaProject;
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
 * A listener that will be notified when a resource is changed.
 * @author Katsuhisa Maruyama
 */
public class ResourceChangeListener implements IResourceChangeListener {
    
    ResourceChangeListener() {
    }
    
    void register() {
        ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
    }
    
    void unregister() {
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
    }
    
    @Override
    public void resourceChanged(IResourceChangeEvent event) {
        try {
            if (event.getType() == IResourceChangeEvent.POST_CHANGE) {
                ResourceDeltaVisitor visitor = new ResourceDeltaVisitor();
                event.getDelta().accept(visitor);
                for (IResource resource : visitor.getChangedResources()) {
                    JavaProject jproject = ProjectStore.getInstance().getProject(resource.getProject().getFullPath().toString());
                    if (jproject != null) {
                        ProjectStore.getInstance().removeFile(resource.getFullPath().toString(), jproject);
                    }
                }
            }
        } catch (CoreException e) {
            e.printStackTrace();
        }
    }
    
    class ResourceDeltaVisitor implements IResourceDeltaVisitor {
        
        private Set<IResource> resources = new HashSet<IResource>();
        
        public boolean visit(IResourceDelta delta) throws CoreException {
            IResource resource = delta.getResource();
            
            switch (delta.getKind()) {
                case IResourceDelta.ADDED:
                    break;
                case IResourceDelta.REMOVED:
                    String rext = resource.getFileExtension();
                    if (rext != null && rext.compareTo("java") == 0) {
                        resources.add(resource);
                    }
                    break;
                case IResourceDelta.CHANGED:
                    String cext = resource.getFileExtension();
                    if (cext != null && cext.compareTo("java") == 0) {
                        resources.add(resource);
                    }
                    break;
            }
            return true;
        }
        
        Set<IResource> getChangedResources() {
            return resources;
        }
     }
}
