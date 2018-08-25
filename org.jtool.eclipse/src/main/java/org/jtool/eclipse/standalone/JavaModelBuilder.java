/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.standalone;

import org.jtool.eclipse.javamodel.JavaProject;
import org.jtool.eclipse.javamodel.builder.ModelBuilder;
import org.jtool.eclipse.util.Options;
import org.jtool.eclipse.util.Logger;
import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.io.IOException;

/**
 * Builds a Java Model for stand-alone use.
 * @author Katsuhisa Maruyama
 */
public class JavaModelBuilder {
    
    private ModelBuilder builder;
    private String logfile = "";
    
    public JavaModelBuilder(String[] args) {
        String cdir = new File(".").getAbsoluteFile().getParent();
        Options options = new Options(args);
        String target = removeLastFileSeparator(options.get("-target", "."));
        String classpath = options.get("-classpath", ".");
        String name = options.get("-name", getProjectName(target, cdir));
        logfile = options.get("-logfile", "");
        
        File dir = new File(getFullPath(target, cdir));
        String path;
        try {
            path = dir.getCanonicalPath();
            builder = new ModelBuilder(name, dir.getCanonicalPath());
            builder.setClassPath(getClassPath(classpath));
            if (logfile.length() > 0) {
                Logger.getInstance().setLogFile(path + File.separator + logfile);
            }
        } catch (IOException e) {
            System.err.println("Cannot build a Java model due to the invalid options/settings.");
        }
    }
    
    public JavaModelBuilder(String name, String target) {
        this(name, target, target);
    }
    
    public JavaModelBuilder(String name, String target, String classpath) {
        builder = new ModelBuilder(name, target);
        builder.setClassPath(getClassPath(classpath));
    }
    
    public JavaProject getProject() {
        return builder.getProject();
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
    
    private String[] getClassPath(String classpath) {
        List<String> classpaths = new ArrayList<String>();
        try {
            String cdir = new File(".").getAbsoluteFile().getParent();
            if (classpath != null && classpath.length() != 0) {
                String[] paths = classpath.split(":");
                if (paths != null) {
                    for (int i = 0; i < paths.length; i++) {
                        String path = getFullPath(paths[i], cdir); 
                        if (path.endsWith(File.separator + "*")) {
                            path = path.substring(0, path.length() - 1);
                            File dir = new File(path);
                            if (dir != null && dir.exists()) {
                                for (File file : dir.listFiles()) {
                                    if (file.getAbsolutePath().endsWith(".jar")) {
                                        classpaths.add(file.getCanonicalPath());
                                    }
                                }
                            }
                        } else {
                            File file = new File(path);
                            if (file != null && file.exists()) {
                                classpaths.add(path);
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            return new String[0];
        }
        return toArray(classpaths);
    }
    
    private String getFullPath(String path, String cdir) {
        if (path.charAt(0) == File.separatorChar) {
            return path;
        } else {
            return cdir + File.separatorChar + path;
        }
    }
    
    private String[] toArray(List<String> list) {
        String[] array = new String[list.size()];
        int index = 0;
        for (String elem : list) {
            array[index] = elem;
            index++;
        }
        return array;
    }
    
    public JavaProject build(boolean resolveBinding) {
        return builder.build(resolveBinding);
    }
    
    public void unbuild() {
        builder.unbuild();
    }
    
    public static void main(String[] args) {
        JavaModelBuilder builder = new JavaModelBuilder(args);
        builder.build(true);
    }
}
