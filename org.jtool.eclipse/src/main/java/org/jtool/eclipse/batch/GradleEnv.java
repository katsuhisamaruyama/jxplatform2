/*
 *  Copyright 2020
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.batch;

import java.nio.file.Path;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

/**
 * Obtains path information from the Ant setting.
 * 
 * @author Katsuhisa Maruyama
 */
class GradleEnv extends ProjectEnv {
    
    GradleEnv(String target) {
        super(target);
    }
    
    @Override
    boolean isApplicable() {
        return false;
    }
    private void setPaths(String configFile) throws Exception {
        /*
        String settingFile = getFileName(projectpath, "settings.gradle");
        List<String> srcPaths = getSrcPaths(settingFile);
        
        if (srcPaths.size() == 0) {
            addSrcPath(projectpath, srcPaths);
        }
        if (srcPaths.size() == 0) {
            srcpath = new String[1];
            srcpath[0] = projectpath.toString();
        } else {
            srcpath = (String[])srcPaths.toArray(new String[srcPaths.size()]);
        }
        
        classpath = ModelBuilderBatch.getClassPath(projectpath.toString() + File.separator + "lib/*");
        binpath = projectpath.toString() + File.separator + "bin";
        */
    }
    
    private List<String> getSrcPaths(String settingFile) throws IOException {
        List<String> srcPaths = new ArrayList<String>();
        if (settingFile == null) {
            return srcPaths;
        }
        
        try (BufferedReader in = new BufferedReader(new FileReader(new File(settingFile)))) {
            String line;
            while ((line = in.readLine()) != null) {
                if (line.startsWith("include")) {
                    int beginIndex = line.indexOf("\"");
                    int endIndex = line.lastIndexOf("\"");
                    String included = line.substring(beginIndex + 1, endIndex);
                    
                    Path subproject = basePath.resolve(included);
                    if (subproject.toFile().exists()) {
                        addSrcPath(subproject, srcPaths);
                    }
                }
            }
        } catch (IOException e) {
            throw e;
        }
        return srcPaths;
    }
    
    private void addSrcPath(Path path, List<String> srcPaths) {
        Path srcPath = path.resolve("src").resolve("main").resolve("java");
        if (srcPath != null) {
            srcPaths.add(srcPath.toString());
        }
        Path testPath = path.resolve("src").resolve("test").resolve("java");
        if (testPath != null) {
            srcPaths.add(testPath.toString());
        }
    }
}
