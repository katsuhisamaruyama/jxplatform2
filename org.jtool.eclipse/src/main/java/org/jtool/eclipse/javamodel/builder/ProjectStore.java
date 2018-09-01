/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.javamodel.builder;

import org.jtool.eclipse.batch.ModelBuilder;
import org.jtool.eclipse.javamodel.JavaProject;
import org.jtool.eclipse.plugin.ModelBuilderPlugin;

import java.util.Map;
import java.util.HashMap;

/**
 * An object holds a collection of all projects.
 * All methods of this class are not intended to be directly called by clients.
 * 
 * @author Katsuhisa Maruyama
 */
public class ProjectStore {
    
    private static ProjectStore instance = new ProjectStore();
    
    private Map<String, JavaProject> projectStore = new HashMap<String, JavaProject>();
    
    private boolean underPlugin = false;
    
    private ProjectStore() {
    }
    
    public static ProjectStore getInstance() {
        return instance;
    }
    
    public void setUnderPlugin(boolean bool) {
        underPlugin = bool;
    }
    
    public boolean isUnderPlugin(boolean bool) {
        return underPlugin;
    }
    
    public JavaProject getCurrentProject() {
        if (underPlugin) {
            return ModelBuilderPlugin.getInstance().getCurrentProject();
        } else {
            return ModelBuilder.getInstance().getCurrentProject();
        }
    }
    
    public JavaProject updateCurrentProject() {
        if (underPlugin) {
            return ModelBuilderPlugin.getInstance().update();
        } else {
            return ModelBuilder.getInstance().update();
        }
    }
    
    public void clear() {
        for (JavaProject jproject : projectStore.values()) {
            jproject.clear();
        }
        projectStore.clear();
    }
    
    public void clear(String path) {
        JavaProject jproject = projectStore.get(path);
        if (jproject != null) {
            jproject.clear();
        }
    }
    
    public void dispose() {
        if (projectStore != null) {
            projectStore.clear();
            projectStore = null;
        }
    }
    
    public void dispose(String path) {
        JavaProject jproject = projectStore.get(path);
        if (jproject != null) {
            jproject.dispose();
        }
    }
    
    public void addProject(JavaProject jproject) {
        projectStore.put(jproject.getPath(), jproject);
    }
    
    public void removeProject(String path) {
        projectStore.remove(path);
    }
    
    public JavaProject getProject(String path) {
        return projectStore.get(path);
    }
    
    public BytecodeClassStore registerBytecodeClasses(JavaProject jproject) {
        BytecodeClassStore bytecodeClassStore = new BytecodeClassStore(jproject);
        if (underPlugin) {
            ModelBuilderPlugin.getInstance().resisterBytecodeClasses(bytecodeClassStore);
        } else {
            ModelBuilder.getInstance().resisterBytecodeClasses(bytecodeClassStore);
        }
        return bytecodeClassStore;
    }
}
