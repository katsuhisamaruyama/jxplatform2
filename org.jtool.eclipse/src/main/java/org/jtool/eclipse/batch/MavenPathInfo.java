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
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;
import java.util.List;
import java.util.ArrayList;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.MavenInvocationException;

/**
 * Obtains path information from the Maven setting.
 * 
 * @author Katsuhisa Maruyama
 */
class MavenPathInfo extends ProjectPathInfo {
    
    MavenPathInfo(String target) {
        super(target);
    }
    
    void setPaths() throws IOException {
        Path configPath = base.resolve(Paths.get("pom.xml"));
        if (!configPath.toFile().exists()) {
            throw new IOException();
        }
        
        String groupIdPath = getGroupId(configPath).replace('.', File.separatorChar);
        List<String> srcPaths = collectSrcPaths(base.toString(), groupIdPath);
        if (srcPaths.size() == 0) {
            srcpath = new String[1];
            srcpath[0] = base.toString();
        } else {
            srcpath = (String[])srcPaths.toArray(new String[srcPaths.size()]);
        }
        
        classpath = ModelBuilderBatch.getClassPath(base.toString() + File.separator + "lib/*");
        binpath = base.toString() + File.separator + "bin";
    }
    
    private String getGroupId(Path configPath) throws IOException {
        String groupId = null;
        try {
            MavenXpp3Reader reader = new MavenXpp3Reader();
            Model model = reader.read(Files.newBufferedReader(configPath));
            groupId = model.getGroupId();
            if (groupId == null) {
                groupId = model.getParent().getGroupId();
            }
        } catch (IOException | XmlPullParserException e) {
            groupId = null;
        }
        if (groupId == null) {
            throw new IOException();
        }
        return groupId;
    }
    
    private List<String> collectSrcPaths(String path, String groupIdPath) {
        List<String> paths = new ArrayList<String>();
        if (path.endsWith(groupIdPath)) {
            String srcPath = path.substring(0, path.length() - groupIdPath.length() - 1);
            List<File> files = ModelBuilderBatch.collectAllJavaFiles(srcPath);
            if (files.size() > 0) {
                paths.add(srcPath);
            }
            return paths;
        }
        
        if (path.endsWith("target")) {
            return paths;
        }
        
        File res = new File(path);
        if (res.isDirectory()) {
            for (File r : res.listFiles()) {
                paths.addAll(collectSrcPaths(r.getPath(), groupIdPath));
            }
        }
        return paths;
    }
    
    @SuppressWarnings("unused")
    private void copyDependentLibraries(Path configPath) {
        Path userHome = Paths.get(System.getProperty("user.home"));
        File mvn = userHome.resolve("mvn").toFile();
        if (!mvn.exists()) {
            mvn = Paths.get("/usr/local/bin/mvn").toFile();
        }
        if (!mvn.exists()) {
            return;
        }
        
        Properties properties = new Properties();
        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile(configPath.toFile());
        request.setInteractive(false);
        request.setProperties(properties);
        request.setGoals(Arrays.asList("dependency:copy-dependencies"));
        // request.setMavenOpts("-DincludeScope=test");  // default
        
        try {
            Invoker invoker = new DefaultInvoker();
            invoker.setMavenHome(userHome.toFile());
            invoker.setMavenExecutable(mvn);
            invoker.setOutputHandler(null);
            invoker.execute(request);
            
        } catch (MavenInvocationException e) {
            e.printStackTrace();
        }
    }
}
