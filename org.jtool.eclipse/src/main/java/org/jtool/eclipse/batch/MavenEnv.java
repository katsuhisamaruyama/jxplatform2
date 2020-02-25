/*
 *  Copyright 2020
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.batch;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.io.File;
import java.util.Arrays;
import java.util.Properties;
import java.util.HashSet;
import org.apache.maven.model.Model;
import org.apache.maven.model.Build;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.DefaultInvoker;

/**
 * Obtains path information from the Maven setting.
 * 
 * @author Katsuhisa Maruyama
 */
class MavenEnv extends ProjectEnv {
    
    final static String configName = "pom.xml";
    
    MavenEnv(String target) {
        super(target);
    }
    
    @Override
    boolean isApplicable() {
        try {
            Path config = basePath.resolve(Paths.get(MavenEnv.configName));
            if (config.toFile().exists()) {
                setPaths(config.toString());
                return true;
            }
        } catch (Exception e) { }
        return false;
    }
    
    private void setPaths(String configFile) throws Exception {
        sourcePath = new HashSet<String>();
        binaryPath = new HashSet<String>();
        classPath = new HashSet<String>();
        
        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model model = reader.read(Files.newBufferedReader(Paths.get(configFile)));
        
        Build build = model.getBuild();
        String sourceDirectory  = build.getSourceDirectory();
        if (sourceDirectory == null) {
            sourceDirectory = basePath.resolve("src").resolve("main").resolve("java").toString();
            if (!new File(sourceDirectory).exists()) {
                sourceDirectory = basePath.toString();
            }
        }
        String testSourceDirectory  = build.getTestSourceDirectory();
        if (testSourceDirectory == null) {
            testSourceDirectory = basePath.resolve("src").resolve("test").resolve("java").toString();
        }
        sourcePath.add(resolvePath(sourceDirectory));
        sourcePath.add(resolvePath(testSourceDirectory));
        
        String buildDirectory  = build.getDirectory();
        if (buildDirectory == null) {
            buildDirectory = basePath.resolve("target").toString();
        }
        Path buildPath = Paths.get(buildDirectory);
        
        String outputDirectory = build.getOutputDirectory();
        if (outputDirectory == null) {
            outputDirectory = buildPath.resolve("classes").toString();
        }
        String testOutputDirectory = build.getTestOutputDirectory();
        if (testOutputDirectory == null) {
            testOutputDirectory = buildPath.resolve("test-classes").toString();
        }
        binaryPath.add(toAbsolutePath(outputDirectory));
        binaryPath.add(toAbsolutePath(testOutputDirectory));
        
        Path libpath = basePath.resolve("lib");
        classPath.add(libpath.toString());
        copyDependentLibraries(configFile, libpath);
    }
    
    private String resolvePath(String path) {
        String resolvedPath = toAbsolutePath(path);
        if (!new File(resolvedPath).exists()) {
            resolvedPath = basePath.toString();
        }
        return resolvedPath;
    }
    
    private String toAbsolutePath(String path) {
        if (path.charAt(0) == '/') {
            return path;
        } else {
             return basePath.toAbsolutePath() + File.separator + path;
        }
    }
    
    private void copyDependentLibraries(String configFile, Path libpath) throws Exception {
        if (libpath.toFile().exists()) {
            return;
        }
        
        Path userHome = Paths.get(System.getProperty("user.home"));
        String mvnCommand = findMvnCommand(userHome);
        if (mvnCommand == null) {
            System.out.println("****************************************************************************************");
            System.out.println("Please execute Maven command -- mvn dependency:copy-dependencies -DoutputDirectory=lib");
            System.out.println("****************************************************************************************");
            throw new Exception();
        }
        
        System.out.println("Copying dependency jar files to " + libpath.toString());
        Files.createDirectory(libpath);
        
        Properties properties = new Properties();
        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile(Paths.get(configFile).toFile());
        request.setInteractive(false);
        request.setProperties(properties);
        request.setGoals(Arrays.asList("dependency:copy-dependencies"));
        request.setMavenOpts("-DoutputDirectory=lib");
        
        Invoker invoker = new DefaultInvoker();
        invoker.setMavenHome(userHome.toFile());
        invoker.setMavenExecutable(new File(mvnCommand));
        invoker.setOutputHandler(null);
        invoker.execute(request);
        
        System.out.println("Copied dependency jar files to " + libpath.toString());
    }
    
    private String findMvnCommand(Path userHome) {
        if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
            return null;
        }
        
        if (userHome != null) {
            Path mvn = userHome.resolve("mvn");
            if (mvn.toFile().exists()) {
                return mvn.toString();
            }
        }
        
        Path mvn = Paths.get("/usr/local/bin/mvn");
        if (mvn.toFile().exists()) {
            return mvn.toString();
        }
        mvn = Paths.get("/usr/bin/mvn");
        if (mvn.toFile().exists()) {
            return mvn.toString();
        }
        return null;
    }
    
    @Override
    public String toString() {
        return "Maven Env " + basePath.toString();
    }
}
