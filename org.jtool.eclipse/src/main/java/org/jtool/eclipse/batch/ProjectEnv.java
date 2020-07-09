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
    
    private static final String DEFAULT_SOURCEPATH = "src";
    private static final String DEFAULT_BINARYPATH = "bin";
    private static final String DEFAULT_CLASSPATH = "lib";
    
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
        return new SimpleEnv(basePath);
    }
    
    boolean isApplicable() {
        return false;
    }
    
    Path getBasePath() {
        return basePath;
    }
    
    Set<String> getSourcePath() {
        if (sourcePath.size() == 0) {
            sourcePath.add(basePath.resolve(DEFAULT_SOURCEPATH).toString());
        }
        return sourcePath;
    }
    
    Set<String> getBinaryPath() {
        if (binaryPath.size() == 0) {
            binaryPath.add(basePath.resolve(DEFAULT_BINARYPATH).toString());
        }
        return binaryPath;
    }
    
    Set<String> getClassPath() {
        if (classPath.size() == 0) {
            classPath.add(basePath.resolve(DEFAULT_CLASSPATH).toString());
        }
        return classPath;
    }
}
