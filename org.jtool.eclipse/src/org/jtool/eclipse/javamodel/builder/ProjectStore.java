/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.javamodel.builder;

import org.jtool.eclipse.javamodel.JavaProject;
import org.jtool.eclipse.util.FileWriter;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;

/**
 * An object representing a Eclipse's project.
 * @author Katsuhisa Maruyama
 */
public class ProjectStore {
    
    private static ProjectStore instance = new ProjectStore();
    
    private Map<String, JavaProject> projectStore = new HashMap<String, JavaProject>();
    
    private JavaProject currentProject;
    
    private List<String> logMessages = new ArrayList<String>();
    
    private JXConsole console = new JXConsole();
    
    private ProjectStore() {
    }
    
    public static ProjectStore getInstance() {
        return instance;
    }
    
    public void setCurrentProject(String path) {
        JavaProject jproject = projectStore.get(path);
        if (jproject != null) {
            currentProject = jproject;
        }
    }
    
    public void setCurrentProject(JavaProject jproject) {
        currentProject = jproject;
    }
    
    public JavaProject getCurrentProject() {
        return currentProject;
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
    
    public JavaProject getProject(String path) {
        return projectStore.get(path);
    }
    
    public void writeLog(String path) {
        StringBuilder buf = new StringBuilder();
        for (String mesg : logMessages) {
            buf.append(mesg);
            buf.append("\n");
        }
        try {
            FileWriter.write(path, buf.toString());
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.err.flush();
        }
        logMessages.clear();
    }
    
    public void printProgress(String mesg) {
        System.out.print(mesg);
        System.out.flush();
    }
    
    public void printMessage(String mesg) {
        logMessages.add(mesg);
        
        System.out.println(mesg);
        System.out.flush();
    }
    
    public void printLog(String mesg) {
        logMessages.add(mesg);
    }
    
    public void printError(String mesg) {
        logMessages.add(mesg);
        
        System.err.println(mesg);
        System.err.flush();
        console.println(mesg);
    }
    
    public void printUnresolvedError(String mesg) {
        printError("!Unresolved : " + mesg);
    }
}
