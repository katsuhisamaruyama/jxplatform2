/*
 *  Copyright 2018-2020
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.javamodel.builder;

import org.jtool.eclipse.javamodel.JavaProject;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * An object holds a collection of all projects.
 * All methods of this class are not intended to be directly called by clients.
 * 
 * @author Katsuhisa Maruyama
 */
public class ProjectStore {
    
    private static ProjectStore instance = new ProjectStore();
    
    private Map<String, JavaProject> projectStore = new HashMap<>();
    
    protected JavaProject currentProject;
    
    public static ProjectStore getInstance() {
        return instance;
    }
    
    public void clear() {
        projectStore.values().forEach(jproject -> jproject.clear());
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
    
    public List<JavaProject> getProjects() {
        return new ArrayList<>(projectStore.values());
    }
}
