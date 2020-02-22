/*
 *  Copyright 2018-2020
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.batch;

import org.jtool.eclipse.util.Options;
import org.jtool.eclipse.util.Logger;
import java.io.File;
import java.io.IOException;

/**
 * Builds a Java Model for stand-alone use.
 * 
 * @author Katsuhisa Maruyama
 */
public class JavaModelBuilder {
    
    private String projectName;
    private String projectPath;
    private String srcpath;
    private String binpath;
    private String classpath;
    private String logfile = "";
    private String autoCheckEnv;
    
    private ModelBuilderBatch modelBuilder;
    
    public JavaModelBuilder(String[] args) {
        try {
            String cdir = new File(".").getAbsoluteFile().getParent();
            Options options = new Options(args);
            String target = removeLastFileSeparator(options.get("-target", "."));
            target = removeLastFileSeparator(target);
            
            projectName = options.get("-name", getProjectName(target, cdir));
            File dir = new File(ModelBuilderBatch.getFullPath(target, cdir));
            projectPath = dir.getCanonicalPath();
            classpath = getPath(target, options.get("-classpath", target));
            
            autoCheckEnv = options.get("auto-check-env", "no");
            if (autoCheckEnv.contentEquals("yes")) {
                srcpath = target;
                binpath = target;
            } else {
                srcpath = getPath(target, options.get("-srcpath", target));
                binpath = getPath(target, options.get("-binpath", target));
            }
            
            logfile = options.get("-logfile", "");
            if (logfile.length() > 0) {
                Logger.getInstance().setLogFile(projectPath + File.separator + logfile);
            }
        } catch (IOException e) {
            System.err.println("Cannot build a Java model due to the invalid options/settings.");
        }
    }
    
    public JavaModelBuilder(String name, String target) {
        this(name, target, target, target, target);
    }
    
    public JavaModelBuilder(String name, String target, String classpath) {
        this(name, target, classpath, target, target);
    }
    
    public String getPath(String target, String option) {
        if (option == null) {
            return target;
        }
        
        String path[] = option.split(File.pathSeparator);
        for (int i = 0; i < path.length; i++) {
            if (!path[i].startsWith(File.separator)) {
                path[i] = target + File.separator + path[i];
            }
        }
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < path.length; i++) {
            buf.append(File.pathSeparator);
            buf.append(path[i]);
        }
        return buf.substring(1);
    }
    
    public JavaModelBuilder(String name, String target, String classpath, String srcpath, String binpath) {
        try {
            projectName = replaceFileSeparator(name);
            File dir = new File(target);
            projectPath = dir.getCanonicalPath();
            this.srcpath = srcpath;
            this.binpath = binpath;
            this.classpath = classpath;
            autoCheckEnv = "no";
        } catch (IOException e) {
            System.err.println("Cannot build a Java model due to the invalid options/settings.");
        }
    }
    
    private String getProjectName(String target, String cdir) {
        String name = removeLastFileSeparator(target);
        if (name.startsWith(cdir)) {
            name = name.substring(cdir.length() + 1);
        }
        int index = name.lastIndexOf(File.separatorChar + "src");
        if (index > 0) {
            name = name.substring(0, index);
        }
        return replaceFileSeparator(name);
    }
    
    private String removeLastFileSeparator(String path) {
        if (path.charAt(path.length() - 1) == File.separatorChar) {
            return path.substring(0, path.length() - 1);
        }
        return path;
    }
    
    private String replaceFileSeparator(String path) {
        return path.replace(File.separatorChar, '.');
    }
    
    public void build() {
        if (autoCheckEnv.equals("yes")) {
            modelBuilder = new ModelBuilderBatch(true);
            modelBuilder.build(projectName, projectPath, classpath, srcpath, binpath);
        } else {
            modelBuilder = new ModelBuilderBatch(true);
            modelBuilder.build(projectName, projectPath, true);
        }
    }
    
    public void unbuild() {
        if (modelBuilder != null) {
            modelBuilder.unbuild();
        }
    }
    
    public static void main(String[] args) {
        JavaModelBuilder builder = new JavaModelBuilder(args);
        builder.build();
    }
}
