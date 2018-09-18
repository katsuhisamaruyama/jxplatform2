/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.batch;

import org.jtool.eclipse.javamodel.JavaProject;
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
    private String projectClasspath;
    
    private ModelBuilderBatch modelBuilder;
    
    private String logfile = "";
    
    public JavaModelBuilder(String[] args) {
        try {
            String cdir = new File(".").getAbsoluteFile().getParent();
            Options options = new Options(args);
            String target = removeLastFileSeparator(options.get("-target", "."));
            target = removeLastFileSeparator(target);
            
            projectName = options.get("-name", getProjectName(target, cdir));
            File dir = new File(ModelBuilderBatch.getFullPath(target, cdir));
            projectPath = dir.getCanonicalPath();
            projectClasspath = options.get("-classpath", ".");
            
            logfile = options.get("-logfile", "");
            if (logfile.length() > 0) {
                Logger.getInstance().setLogFile(projectPath + File.separator + logfile);
            }
        } catch (IOException e) {
            System.err.println("Cannot build a Java model due to the invalid options/settings.");
        }
    }
    
    public JavaModelBuilder(String name, String target) {
        this(name, target, target);
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
    
    public JavaModelBuilder(String name, String target, String classpath) {
        try {
            projectName = replaceFileSeparator(name);
            File dir = new File(target);
            projectPath = dir.getCanonicalPath();
            projectClasspath = classpath;
        } catch (IOException e) {
            System.err.println("Cannot build a Java model due to the invalid options/settings.");
        }
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
    
    public JavaProject build() {
        modelBuilder = new ModelBuilderBatch();
        return modelBuilder.build(projectName, projectPath, projectClasspath);
    }
    
    public static void main(String[] args) {
        JavaModelBuilder builder = new JavaModelBuilder(args);
        builder.build();
    }
}
