/*
 *  Copyright 2018-2019
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.batch;

import org.jtool.eclipse.javamodel.JavaClass;
import org.jtool.eclipse.javamodel.JavaFile;
import org.jtool.eclipse.javamodel.JavaProject;
import org.jtool.eclipse.javamodel.builder.BytecodeClassStore;
import org.jtool.eclipse.javamodel.builder.JavaASTVisitor;
import org.jtool.eclipse.javamodel.builder.ModelBuilder;
import org.jtool.eclipse.javamodel.builder.ProjectStore;
import org.jtool.eclipse.util.ConsoleProgressMonitor;
import org.jtool.eclipse.util.DetectCharset;
import org.jtool.eclipse.util.Logger;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.FileASTRequestor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;

/**
 * Builds a Java Model.
 * 
 * @author Katsuhisa Maruyama
 */
public class ModelBuilderBatch extends ModelBuilder {
    
    public ModelBuilderBatch() {
        super(false);
    }
    
    public ModelBuilderBatch(boolean analyzingBytecode) {
        super(analyzingBytecode);
    }
    
    public boolean isUnderPlugin() {
        return false;
    }
    
    public JavaProject build(String name, String target, String classPath) {
        return build(name, target, getClassPath(classPath));
    }
    
    public JavaProject build(String name, String target, String[] classPath) {
        try {
            File file = new File(target);
            String dir = file.getCanonicalPath();
            String[] sourcePath = new String[1];
            sourcePath[0] = dir + File.separatorChar + "src";
            String binaryPath = dir + File.separatorChar + "bin";
            return build(name, target, classPath, sourcePath, binaryPath);
        } catch (IOException e) {
            currentProject = null;
            return null;
        }
    }
    
    public JavaProject build(String name, String target, String classPath, String sourcePath, String binaryPath) {
        return build(name, target, getClassPath(classPath), sourcePath, binaryPath);
    }
    
    public JavaProject build(String name, String target, String classPath, String[] sourcePath, String binaryPath) {
        return build(name, target, getClassPath(classPath), sourcePath, binaryPath);
    }
    
    public JavaProject build(String name, String target, String[] classPath, String sourcePath, String binaryPath) {
        String[] sourcePaths = new String[1];
        sourcePaths[0] = sourcePath;
        return build(name, target, classPath, sourcePaths, binaryPath);
    }
    
    public JavaProject build(String name, String target, String[] classPath, String[] sourcePath, String binaryPath) {
        try {
            File file = new File(target);
            String path = file.getCanonicalPath();
            return build(name, target, path, classPath, sourcePath, binaryPath);
        } catch (IOException e) {
            currentProject = null;
            return null;
        }
    }
    
    public JavaProject build(String name, String target, String path, String[] classPath, String[] sourcePath, String binaryPath) {
        currentProject = new JavaProject(name, path, path);
        currentProject.setModelBuilder(this);
        currentProject.setClassPath(classPath);
        currentProject.setSourceBinaryPaths(sourcePath, binaryPath);
        
        ProjectStore.getInstance().addProject(currentProject);
        ProjectStore.getInstance().setModelBuilder(this);
        
        cfgStore.create(currentProject, analyzingBytecode);
        
        run();
        Logger.getInstance().writeLog();
        return currentProject;
    }
    
    static String[] getClassPath(String classpath) {
        List<String> classpaths = new ArrayList<String>();
        try {
            String cdir = new File(".").getAbsoluteFile().getParent();
            if (classpath != null && classpath.length() != 0) {
                String[] paths = classpath.split(File.pathSeparator);
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
        return classpaths.toArray(new String[classpaths.size()]);
    }
    
    static String getFullPath(String path, String cdir) {
        if (path.charAt(0) == File.separatorChar) {
            return path;
        } else {
            return cdir + File.separatorChar + path;
        }
    }
    
    public JavaProject update() {
        ProjectStore.getInstance().removeProject(currentProject.getPath());
        return build(currentProject.getName(), currentProject.getPath(), currentProject.getClassPath());
    }
    
    private void run() {
        List<File> sourceFiles = collectAllJavaFiles(currentProject.getSourcePath());
        if (sourceFiles.size() > 0) {
            String[] paths = new String[sourceFiles.size()];
            String[] encodings = new String[sourceFiles.size()];
            Map<String, String> sources = new HashMap<String, String>();
            Map<String, String> charsets = new HashMap<String, String>();
            
            int count = 0;
            for (File file : sourceFiles) {
                try {
                    String path = file.getCanonicalPath();
                    String source = read(file);
                    String charset = DetectCharset.getCharsetName(source.getBytes());
                    paths[count] = path;
                    encodings[count] = charset;
                    sources.put(path, source);
                    charsets.put(path, charset);
                    count++;
                } catch (IOException e) { /* empty */ }
            }
            
            parse(paths, encodings, sources, charsets);
            collectInfo();
        } else {
            System.err.println("Found no Java source files in " + currentProject.getPath());
        }
    }
    
    private void parse(String[] paths, String[] encodings, Map<String, String> sources, Map<String, String> charsets) {
        final int size = paths.length;
        ConsoleProgressMonitor pm = new ConsoleProgressMonitor();
        pm.begin(size);
        FileASTRequestor requestor = new FileASTRequestor() {
            private int count = 0;
            
            public void acceptAST(String path, CompilationUnit cu) {
                JavaFile jfile = new JavaFile(cu, path, sources.get(path), charsets.get(path), currentProject);
                JavaASTVisitor visitor = new JavaASTVisitor(jfile);
                cu.accept(visitor);
                visitor.terminate();
                currentProject.addFile(jfile);
                
                pm.work(1);
                count++;
                Logger.getInstance().printLog("-Parsed " + jfile.getRelativePath() + " (" + count + "/" + size + ")");
            }
        };
        
        Logger.getInstance().printMessage("Target = " + currentProject.getPath() + " (" + currentProject.getName() + ")");
        
        Logger.getInstance().printMessage("** Ready to parse " + size + " files");
        ASTParser parser = getParser();
        parser.setEnvironment(currentProject.getClassPath(), null, null, true);
        parser.createASTs(paths, encodings, new String[]{ }, requestor, null);
        pm.done();
    }
    
    public void collectInfo() {
        int size = currentProject.getClasses().size();
        Logger.getInstance().printMessage("** Ready to build java models of " + size + " classes");
        ConsoleProgressMonitor pm = new ConsoleProgressMonitor();
        pm.begin(size);
        int count = 0;
        for (JavaClass jclass : currentProject.getClasses()) {
            currentProject.collectInfo(jclass);
            
            pm.work(1);
            count++;
            Logger.getInstance().printLog("-Built " + jclass.getQualifiedName() + " (" + count + "/" + size + ")");
        }
        pm.done();
    }
    
    private static List<File> collectAllJavaFiles(String[] paths) {
        List<File> files = new ArrayList<File>();
        for (String path : paths) {
            files.addAll(collectAllJavaFiles(path));
        }
        return files;
    }
    
    private static List<File> collectAllJavaFiles(String path) {
        List<File> files = new ArrayList<File>();
        File res = new File(path);
        if (res.isFile()) {
            if (path.endsWith(".java")) {
                files.add(res);
            }
        } else if (res.isDirectory()) {
            for (File r : res.listFiles()) {
                files.addAll(collectAllJavaFiles(r.getPath()));
            }
        }
        return files;
    }
    
    private String read(File file) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            char[] buf = new char[128];
            int count = 0;
            while ((count = reader.read(buf)) != -1) {
                String data = String.valueOf(buf, 0, count);
                content.append(data);
                buf = new char[1024];
            }
        }
        return  content.toString();
    }
    
    @Override
    public void resisterBytecodeClasses(BytecodeClassStore bytecodeClassStore) {
        int size = bytecodeClassStore.getBytecodeClassNames().size();
        Logger.getInstance().printMessage("** Ready to build java models of " + size + " bytecode-classes");
        ConsoleProgressMonitor pm = new ConsoleProgressMonitor();
        pm.begin(size);
        for (String className : bytecodeClassStore.getBytecodeClassNames()) {
            bytecodeClassStore.registerBytecodeClass(className);
            pm.work(1);
        }
        pm.done();
    }
}
