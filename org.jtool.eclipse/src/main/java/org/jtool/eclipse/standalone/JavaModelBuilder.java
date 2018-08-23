/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.standalone;

import org.jtool.eclipse.javamodel.JavaClass;
import org.jtool.eclipse.javamodel.JavaFile;
import org.jtool.eclipse.javamodel.JavaProject;
import org.jtool.eclipse.javamodel.builder.JavaASTVisitor;
import org.jtool.eclipse.javamodel.builder.ProjectStore;
import org.jtool.eclipse.util.DetectCharset;
import org.jtool.eclipse.util.Options;
import org.jtool.eclipse.util.Logger;
import org.jtool.eclipse.util.ProgressMonitor;
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
 * @author Katsuhisa Maruyama
 */
public class JavaModelBuilder {
    
    private JavaProject jproject;
    private List<String> classpaths = new ArrayList<String>();
    private String logfile = "";
    
    public JavaModelBuilder(String[] args) {
        String cdir = new File(".").getAbsoluteFile().getParent();
        Options options = new Options(args);
        String target = removeLastFileSeparator(options.get("-target", "."));
        String classpath = options.get("-classpath", ".");
        String name = options.get("-name", getProjectName(target, cdir));
        logfile = options.get("-logfile", "");
        
        File dir = new File(getFullPath(target, cdir));
        try {
            jproject = new JavaProject(name, dir.getCanonicalPath(), dir.getCanonicalPath());
            ProjectStore.getInstance().setCurrentProject(jproject);
            setClasspath(classpath, cdir);
            if (logfile.length() > 0) {
                Logger.getInstance().setLogFile(jproject.getPath() + File.separator + logfile);
            }
        } catch (IOException e) {
            jproject = null;
        }
    }
    
    public JavaModelBuilder(String name, String target) {
        this(name, target, target);
    }
    
    public JavaModelBuilder(String name, String target, String classpath) {
        try {
            target = removeLastFileSeparator(target);
            name = replaceFileSeparator(name);
            
            File dir = new File(target);
            jproject = new JavaProject(name, dir.getCanonicalPath());
            ProjectStore.getInstance().setCurrentProject(jproject);
            
            String cdir = new File(".").getAbsoluteFile().getParent();
            setClasspath(classpath, cdir);
        } catch (IOException e) {
            jproject = null;
        }
    }
    
    private String removeLastFileSeparator(String path) {
        if (path.charAt(path.length() - 1) == File.separatorChar) {
            return path.substring(0, path.length() - 1);
        }
        return path;
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
    
    private String replaceFileSeparator(String path) {
        return path.replace(File.separatorChar, '.');
    }
    
    private void setClasspath(String classpath, String cdir) throws IOException {
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
    
    public JavaProject getProject() {
        return jproject;
    }
    
    public void build(boolean resolveBinding) {
        if (jproject == null) {
            System.err.println("Cannot build a Java model due to the invalid options/settings.");
            return;
        }
        run(resolveBinding);
        Logger.getInstance().writeLog();
    }
    
    public void unbuild() {
        if (jproject != null) {
            jproject.clear();
        }
    }
    
    private void run(boolean resolveBinding) {
        List<File> sourceFiles = collectAllJavaFiles(jproject.getPath());
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
                } catch (IOException e) { /* rmpty */ }
            }
            
            parse(paths, encodings, sources, charsets);
            if (resolveBinding) {
                collectInfo();
            }
        }
    }
    
    private void parse(String[] paths, String[] encodings, Map<String, String> sources, Map<String, String> charsets) {
        final int size = paths.length;
        ProgressMonitor pm = new ProgressMonitor();
        pm.begin(size);
        FileASTRequestor requestor = new FileASTRequestor() {
            private int count = 0;
            
            public void acceptAST(String path, CompilationUnit cu) {
                JavaFile jfile = new JavaFile(cu, path, sources.get(path), charsets.get(path), jproject);
                JavaASTVisitor visitor = new JavaASTVisitor(jfile);
                cu.accept(visitor);
                visitor.terminate();
                jproject.addFile(jfile);
                
                pm.work(1);
                count++;
                Logger.getInstance().printLog("-Parsed " + jfile.getRelativePath() + " (" + count + "/" + size + ")");
            }
        };
        
        Logger.getInstance().printMessage("Target = " + jproject.getPath() + " (" + jproject.getName() + ")");
        
        Logger.getInstance().printMessage("** Ready to parse " + size + " files");
        ASTParser parser = getParser();
        parser.createASTs(paths, encodings, new String[]{ }, requestor, null);
        pm.done();
    }
    
    public void collectInfo() {
        int size = jproject.getClasses().size();
        Logger.getInstance().printMessage("** Ready to build java models of " + size + " classes");
        ProgressMonitor pm = new ProgressMonitor();
        pm.begin(size);
        int count = 0;
        for (JavaClass jclass : jproject.getClasses()) {
            jproject.collectInfo(jclass);
            
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
        parser.setEnvironment(toArray(classpaths), null, null, true);
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
    
    private static String read(File file) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            char[] buf = new char[10];
            int count = 0;
            while ((count = reader.read(buf)) != -1) {
                String data = String.valueOf(buf, 0, count);
                content.append(data);
                buf = new char[1024];
            }
        }
        return  content.toString();
    }
    
    public void setVisible(boolean visible) {
        Logger.getInstance().setVisible(visible);
    }
    
    public static void main(String[] args) {
        JavaModelBuilder builder = new JavaModelBuilder(args);
        builder.build(true);
    }
}
