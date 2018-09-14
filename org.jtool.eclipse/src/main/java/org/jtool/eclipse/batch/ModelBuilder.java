/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.batch;

import org.jtool.eclipse.javamodel.JavaClass;
import org.jtool.eclipse.javamodel.JavaFile;
import org.jtool.eclipse.javamodel.JavaProject;
import org.jtool.eclipse.javamodel.builder.IModelBuilder;
import org.jtool.eclipse.javamodel.builder.JavaASTVisitor;
import org.jtool.eclipse.javamodel.builder.ProjectStore;
import org.jtool.eclipse.javamodel.builder.BytecodeClassStore;
import org.jtool.eclipse.util.DetectCharset;
import org.jtool.eclipse.util.Logger;
import org.jtool.eclipse.util.ConsoleProgressMonitor;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
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
public class ModelBuilder implements IModelBuilder {
    
    private JavaProject currentProject;
    
    public ModelBuilder() {
    }
    
    public boolean isUnderPlugin() {
        return false;
    }
    
    public JavaProject getCurrentProject() {
        return currentProject;
    }
    
    public JavaProject build(String name, String target, String[] classPath) {
        try {
            File dir = new File(target);
            currentProject = new JavaProject(name, dir.getCanonicalPath());
            currentProject.setClassPath(classPath);
            ProjectStore.getInstance().addProject(currentProject);
            ProjectStore.getInstance().setModelBuilder(this);
            
            run();
            Logger.getInstance().writeLog();
        } catch (IOException e) {
            currentProject = null;
        }
        return currentProject;
    }
    
    public JavaProject update() {
        ProjectStore.getInstance().removeProject(currentProject.getPath());
        return build(currentProject.getName(), currentProject.getPath(), currentProject.getClassPath());
    }
    
    public void unbuild() {
        if (currentProject != null) {
            ProjectStore.getInstance().removeProject(currentProject.getPath());
            currentProject.clear();
        }
    }
    
    private void run() {
        List<File> sourceFiles = collectAllJavaFiles(currentProject.getPath());
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
    
    @SuppressWarnings("deprecation")
    private ASTParser getParser() {
        ASTParser parser = ASTParser.newParser(AST.JLS8);
        Map<String, String> options = JavaCore.getOptions();
        options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
        options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_8);
        options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
        options.put(JavaCore.COMPILER_DOC_COMMENT_SUPPORT, JavaCore.ENABLED);
        parser.setCompilerOptions(options);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setResolveBindings(true);
        parser.setStatementsRecovery(true);
        parser.setBindingsRecovery(true);
        parser.setEnvironment(currentProject.getClassPath(), null, null, true);
        return parser;
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
    
    public void resisterBytecodeClasses(BytecodeClassStore bytecodeClassStore) {
        int size = bytecodeClassStore.getBytecodeClassNames().size();
        Logger.getInstance().printMessage("** Ready to build java models of " + size + " bytecode-classes outside the project");
        ConsoleProgressMonitor pm = new ConsoleProgressMonitor();
        pm.begin(size);
        int count = 0;
        for (String className : bytecodeClassStore.getBytecodeClassNames()) {
            bytecodeClassStore.registerBytecodeClass(className);
            
            pm.work(1);
            count++;
            Logger.getInstance().printLog("-Parse " + className + " (" + count + "/" + size + ")");
        }
        pm.done();
    }
}
