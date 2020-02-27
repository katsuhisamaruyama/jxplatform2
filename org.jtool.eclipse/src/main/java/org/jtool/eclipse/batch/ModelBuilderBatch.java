/*
 *  Copyright 2018-2020
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.batch;

import org.jtool.eclipse.javamodel.JavaClass;
import org.jtool.eclipse.javamodel.JavaFile;
import org.jtool.eclipse.javamodel.JavaProject;
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
import java.util.Set;
import java.util.HashSet;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.stream.Collectors;

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
    
    @Override
    public boolean isUnderPlugin() {
        return false;
    }
    
    public List<JavaProject> build(String name, String target, boolean checkSubProject) {
        ModelBuilderBatch builder = new ModelBuilderBatch(true);
        if (checkSubProject) {
            return buildWithSubProject(builder, name, target);
        } else {
            return buildWithoutSubProject(builder, name, target);
        }
    }
    
    private List<JavaProject> buildWithoutSubProject(ModelBuilderBatch builder, String name, String target) {
        List<JavaProject> projects = new ArrayList<JavaProject>();
        JavaProject project = builder.build(name, target);
        projects.add(project);
        return projects;
    }
    
    private List<JavaProject> buildWithSubProject(ModelBuilderBatch builder, String name, String target) {
        List<JavaProject> projects = new ArrayList<JavaProject>();
        List<String> subProjects = getSubProject(new File(target));
        
        if (subProjects.size() == 0) {
            JavaProject project = builder.build(name, target);
            projects.add(project);
        } else {
            for (String subproject : subProjects) {
                int index = subproject.lastIndexOf(File.separatorChar);
                String subname = name + "#" + subproject.substring(index + 1);
                System.out.println("Checking sub-project " + subproject);
                JavaProject project = builder.build(subname, subproject);
                projects.add(project);
            }
        }
        return projects;
    }
    
    public JavaProject build(String name, String target) {
        String cdir = new File(".").getAbsoluteFile().getParent();
        Path basePath = Paths.get(getFullPath(target, cdir));
        
        ProjectEnv env = ProjectEnv.getProjectEnv(basePath);
        if (env != null) {
            
            System.out.println("Env = " + env.toString());
            System.out.println("Target = " + target);
            String[] classPath = getClassPath(basePath, env.getClassPath(), "lib");
            String[] sourcePath = getPath(basePath, env, env.getSourcePath(), "src");
            String[] binaryPath = getPath(basePath, env, env.getBinaryPath(), "bin");
                    
            for (String s : classPath) {
                System.out.println("C="+s);
            }
            for (String s : sourcePath) {
                System.out.println("S="+s);
            }
            for (String s : binaryPath) {
                System.out.println("B="+s);
            }
            
            return build(name, basePath, classPath, sourcePath, binaryPath);
        } else {
            
            System.out.println("NO ENV SPECIFIED");
            
            return build(name, basePath, target, null, null);
        }
    }
    
    private String[] getPath(Path basePath, ProjectEnv env, Set<String> pathSet, String dirname) {
        if (pathSet.size() == 0) {
            String[] srcpaths = new String[1];
            srcpaths[0] = basePath.resolve(dirname).toString();
            return srcpaths;
        }
        return (String[])pathSet.toArray(new String[pathSet.size()]);
    }
    
    private String[] getClassPath(Path basePath, Set<String> pathSet, String dirname) {
        String classPathStr;
        if (pathSet.size() == 0) {
            classPathStr = basePath.toString() + File.separator + dirname + File.separator + "*";
        } else {
            Set<String> libPath = pathSet.stream()
                        .map(path -> getLibrarryPath(path)).collect(Collectors.toCollection(HashSet::new));
            classPathStr = String.join(File.pathSeparator, libPath);
        }
        return getClassPath(classPathStr);
    }
    
    private String getLibrarryPath(String path) {
        return (path.endsWith(".jar")) ? path : path + File.separator + "*";
    }
    
    public JavaProject build(String name, String target, String classpath) {
        return build(name, target, classpath, (String)null, (String)null);
    }
    
    public JavaProject build(String name, String target, String classpath, String srcpath, String binpath) {
        String cdir = new File(".").getAbsoluteFile().getParent();
        Path basePath = Paths.get(getFullPath(target, cdir));
        return build(name, basePath, classpath, srcpath, binpath);
    }
    
    public JavaProject build(String name, String target, String[] classpath, String[] srcpath, String[] binpath) {
        String cdir = new File(".").getAbsoluteFile().getParent();
        Path basePath = Paths.get(getFullPath(target, cdir));
        return build(name, basePath, classpath, srcpath, binpath);
    }
    
    private JavaProject build(String name, Path basePath, String classpath, String srcpath, String binpath) {
        String[] classpaths = getClassPath(classpath);
        String[] srcpaths = getSourcePath(srcpath, basePath);
        String[] binpaths = getBinaryPath(binpath, basePath);
        return build(name, basePath, classpaths, srcpaths, binpaths);
    }
    
    private JavaProject build(String name, Path basePath, String[] classpath, String[] srcpath, String[] binpath) {
        JavaProject jproject = new JavaProject(name, basePath.toString(), basePath.toString());
        jproject.getCFGStore().create(jproject, analyzingBytecode);
        jproject.setModelBuilder(this);
        jproject.setClassPath(classpath);
        jproject.setSourceBinaryPaths(srcpath, binpath);
        ProjectStore.getInstance().addProject(jproject);
        
        run(jproject);
        Logger.getInstance().writeLog();
        return jproject;
    }
    
    private List<String> getSubProject(File dir) {
        List<String> projects = new ArrayList<String>();
        
        for (File file : dir.listFiles()) {
            if (file.isDirectory() && isSubProject(file)) {
                String path = file.getAbsolutePath();
                if (ModelBuilderBatch.collectAllJavaFiles(path + File.separator + "src").size() > 0) {
                    projects.add(path);
                }
                projects.addAll(getSubProject(new File(file.getAbsolutePath())));
            }
        }
        return projects;
    }
    
    private boolean isSubProject(File dir) {
        File[] files = dir.listFiles((file, name)
                -> (name.equals(AntEnv.configName) || name.equals(MavenEnv.configName) || name.endsWith(GradleEnv.configName)));
        return files.length > 0;
    }
    
    private String[] getSourcePath(String srcPath, Path basePath) {
        if (srcPath == null) {
            String[] srcpaths = new String[1];
            srcpaths[0] = basePath.resolve("src").toString();
            return srcpaths;
        }
        return srcPath.split(File.pathSeparator);
    }
    
    
    
    private String[] getBinaryPath(String binPath, Path basePath) {
        if (binPath == null) {
            String[] binPaths = new String[1];
            binPaths[0] = basePath.resolve("bin").toString();
            return binPaths;
        }
        return binPath.split(File.pathSeparator);
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
        if (path == null) {
            return cdir;
        }
        if (path.charAt(0) == File.separatorChar) {
            return path;
        } else {
            return cdir + File.separatorChar + path;
        }
    }
    
    @Override
    public void update(JavaProject jproject) {
        ProjectStore.getInstance().removeProject(jproject.getPath());
        build(jproject.getName(), jproject.getPath(), jproject.getClassPath(), jproject.getSourcePath(), jproject.getBinaryPath());
    }
    
    private void run(JavaProject jproject) {
        List<File> sourceFiles = collectAllJavaFiles(jproject.getSourcePath());
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
            
            parse(jproject, paths, encodings, sources, charsets);
            collectInfo(jproject);
        } else {
            System.err.println("Found no Java source files in " + jproject.getPath());
        }
    }
    
    private void parse(JavaProject jproject, String[] paths, String[] encodings, Map<String, String> sources, Map<String, String> charsets) {
        final int size = paths.length;
        ConsoleProgressMonitor pm = new ConsoleProgressMonitor();
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
        parser.setEnvironment(jproject.getClassPath(), null, null, true);
        parser.createASTs(paths, encodings, new String[]{ }, requestor, null);
        pm.done();
    }
    
    public void collectInfo(JavaProject jproject) {
        int size = jproject.getClasses().size();
        Logger.getInstance().printMessage("** Ready to build java models of " + size + " classes");
        ConsoleProgressMonitor pm = new ConsoleProgressMonitor();
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
    
    public static List<File> collectAllJavaFiles(String[] paths) {
        List<File> files = new ArrayList<File>();
        for (String path : paths) {
            files.addAll(collectAllJavaFiles(path));
        }
        return files;
    }
    
    public static List<File> collectAllJavaFiles(String path) {
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
    public void resisterBytecodeClasses(JavaProject jproject) {
        Set<String> names = bytecodeClassStore.createBytecodeClassStore(jproject);
        Logger.getInstance().printMessage("** Ready to build java models of " + names.size() + " bytecode-classes");
        ConsoleProgressMonitor pm = new ConsoleProgressMonitor();
        pm.begin(names.size());
        for (String className : names) {
            bytecodeClassStore.registerBytecodeClass(jproject, className);
            pm.work(1);
        }
        pm.done();
        bytecodeClassStore.collectBytecodeClassInfo(jproject);
    }
}
