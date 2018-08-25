/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.javamodel.builder;

import org.jtool.eclipse.javamodel.JavaProject;
import org.jtool.eclipse.plugin.ModelBuilderPlugin;
import org.jtool.eclipse.standalone.ModelBuilder;

import java.util.Map;
import java.util.HashMap;

/**
 * An object holds a collection of all projects.
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
}
