/*
 *  Copyright 2020
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.batch;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    
    ProjectEnv(String target) {
        String cdir = new File(".").getAbsoluteFile().getParent();
        File dir = new File(ModelBuilderBatch.getFullPath(target, cdir));
        basePath = Paths.get(dir.getAbsolutePath());
    }
    
    static ProjectEnv getProjectEnv(String target) {
        List<ProjectEnv> envs = new ArrayList<ProjectEnv>();
        envs.add(new EclipseEnv(target));
        envs.add(new AntEnv(target));
        envs.add(new MavenEnv(target));
        envs.add(new GradleEnv(target));
        
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
    
    String getProjectPath() {
        return basePath.toString();
    }
    
    String[] getSourcePath() {
        String[] resolvedPath;
        if (sourcePath.size() == 0) {
            resolvedPath = new String[1];
            Path srcDir = basePath.resolve("src");
            if (srcDir.toFile().exists()) {
                resolvedPath[0] = srcDir.toString();
            } else {
                resolvedPath[0] = basePath.toString();
            }
        } else {
            resolvedPath = (String[])sourcePath.toArray(new String[sourcePath.size()]);
        }
        return resolvedPath;
    }
    
    String[] getBinaryPath() {
        String[] resolvedPath;
        if (binaryPath.size() == 0) {
            Path binDir = basePath.resolve("bin");
            resolvedPath = new String[1];
            if (binDir.toFile().exists()) {
                resolvedPath[0] = binDir.toString();
            } else {
                resolvedPath[0] = basePath.toString();
            }
        } else {
            resolvedPath = (String[])binaryPath.toArray(new String[binaryPath.size()]);
        }
        return resolvedPath;
    }
    
    String[] getClassPath() {
        String[] resolvedPath;
        if (classPath.size() == 0) {
            resolvedPath = ModelBuilderBatch.getClassPath(basePath.toString() + File.separator + "lib/*");
        } else {
            String classPathStr = "";
            for (String path : classPath) {
                if (path.endsWith(".jar")) {
                    classPathStr = classPathStr + File.pathSeparator + path;
                } else {
                    classPathStr = classPathStr + File.pathSeparator + path + "/*";
                }
            }
            if (classPathStr.length() > 0) {
                classPathStr = classPathStr.substring(1);
            }
            resolvedPath = ModelBuilderBatch.getClassPath(classPathStr);
        }
        return resolvedPath;
    }
}
