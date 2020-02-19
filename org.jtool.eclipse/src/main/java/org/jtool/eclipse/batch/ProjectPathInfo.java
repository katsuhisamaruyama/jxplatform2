/*
 *  Copyright 2020
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.batch;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Obtains path information from the project setting.
 * 
 * @author Katsuhisa Maruyama
 */
abstract class ProjectPathInfo {
    
    protected String projectPath;
    protected String[] srcpath;
    protected String binpath;
    protected String[] classpath;
    
    protected Path base;
    
    ProjectPathInfo(String target) {
        try {
            String cdir = new File(".").getAbsoluteFile().getParent();
            File dir = new File(ModelBuilderBatch.getFullPath(target, cdir));
            projectPath = dir.getCanonicalPath();
            base = Paths.get(projectPath);
            setPaths();
        } catch (IOException e) {
            projectPath = null;
        }
    }
    
    abstract void setPaths() throws IOException;
    
    String getProjectPath() {
        return projectPath;
    }
    
    String[] getSrcPath() {
        return srcpath;
    }
    
    String getBinPath() {
        return binpath;
    }
    
    String[] getClassPath() {
        return classpath;
    }
}
