/*
 *  Copyright 2020
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.batch;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.model.eclipse.EclipseProject;

/**
 * Obtains path information from the Ant setting.
 * 
 * @author Katsuhisa Maruyama
 */
class GradleEnv extends ProjectEnv {
    
    final static String configName = ".gradle";
    
    GradleEnv(Path basePath) {
        super(basePath);
    }
    
    @Override
    boolean isApplicable() {
        try {
            String config = getFileName(basePath, GradleEnv.configName);
            if (config != null) {
                setPaths(basePath);
                return true;
            }
            return false;
        } catch (Exception e) { }
        return false;
    }
    
    private String getFileName(Path path, String sufix) {
        File[] files = path.toFile().listFiles((file, name) -> name.endsWith(sufix));
        if (files.length > 0) {
            return files[0].getAbsolutePath();
        }
        return null;
    }
    
    private void setPaths(Path path) throws Exception {
        sourcePath = new HashSet<>();
        binaryPath = new HashSet<>();
        classPath = new HashSet<>();
        
        ProjectConnection connection = GradleConnector.newConnector().forProjectDirectory(path.toFile()).connect();
        try {
            EclipseProject project = connection.model(EclipseProject.class).get();
            if (project != null) {
                sourcePath = project.getSourceDirectories().stream()
                            .map(elem -> basePath.resolve(elem.getPath()).toString()).collect(Collectors.toSet());
                
                binaryPath = project.getSourceDirectories().stream()
                        .map(elem -> basePath.resolve(elem.getOutput()).toString()).collect(Collectors.toSet());
                
                Set<String> dependencies = project.getClasspath().stream()
                        .filter(elem -> elem.getSource() != null)
                        .map(elem -> elem.getSource().getAbsolutePath()).collect(Collectors.toSet());
                
                final Path libpath = basePath.resolve("lib");
                if (!libpath.toFile().exists()) {
                    System.out.println("Copying dependency jar files to " + libpath.toString());
                    Files.createDirectory(libpath);
                    copyDependentLibraries(dependencies, libpath);
                }
                classPath.add(libpath.toString());
            }
        } finally {
           connection.close();
        }
    }
    
    private void copyDependentLibraries(Set<String> dependencies, Path libpath) {
        for (String dep : dependencies) {
            try {
                int index = dep.lastIndexOf(File.separator);
                String name = dep.substring(index + 1);
                Files.copy(Paths.get(dep), libpath.resolve(name));
            } catch (IOException e) { /* empty */ }
        }
    }
    
    @Override
    public String toString() {
        return "Gradle (Eclipse) Env " + basePath.toString();
    }
}
