/*
 *  Copyright 2020
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.batch;

import java.nio.file.Path;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

/**
 * Obtains path information from the project setting.
 * 
 * @author Katsuhisa Maruyama
 */
abstract class ProjectEnv {
    
    protected Path basePath;
    protected Set<String> sourcePath;
    protected Set<String> binaryPath;
    protected Set<String> classPath;
    
    ProjectEnv(Path basePath) {
        this.basePath = basePath;
    }
    
    static ProjectEnv getProjectEnv(Path basePath) {
        List<ProjectEnv> envs = new ArrayList<ProjectEnv>();
        envs.add(new EclipseEnv(basePath));
        envs.add(new AntEnv(basePath));
        envs.add(new MavenEnv(basePath));
        envs.add(new GradleEnv(basePath));
        
        for (ProjectEnv env : envs) {
            if (env.isApplicable()) {
                
                return env;
            }
        }
        return null;
    }
    
    boolean isApplicable() {
        return false;
    }
    
    Path getBasePath() {
        return basePath;
    }
    
    Set<String> getSourcePath() {
        return sourcePath;
    }
    
    Set<String> getBinaryPath() {
        return binaryPath;
    }
    
    Set<String> getClassPath() {
        return classPath;
    }
}
