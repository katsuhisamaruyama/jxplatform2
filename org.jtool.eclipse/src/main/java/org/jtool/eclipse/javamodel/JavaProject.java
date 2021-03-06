/*
 *  Copyright 2018-2020
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.javamodel;

import org.jtool.eclipse.javamodel.builder.BytecodeClassStore;
import org.jtool.eclipse.javamodel.builder.ModelBuilder;
import org.jtool.eclipse.javamodel.builder.ProjectStore;
import org.jtool.eclipse.cfg.builder.CFGStore;
import org.jtool.eclipse.pdg.builder.PDGStore;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

/**
 * An object representing a collection of Java source files, packages, and classes to be analyzed.
 * 
 * @author Katsuhisa Maruyama
 */
public class JavaProject {
    
    protected String name;
    protected String path;
    protected String dir;
    
    protected Map<String, JavaFile> fileStore = new HashMap<>();
    protected Map<String, JavaPackage> packageStore = new HashMap<>();
    protected Map<String, JavaClass> classStore = new HashMap<>();
    
    private Map<String, JavaClass> externalClasseStore = new HashMap<>();
    
    private String[] classPath;
    private String[] sourcePath;
    private String[] binaryPath;
    
    protected CFGStore cfgStore;
    protected PDGStore pdgStore;
    
    private ModelBuilder modelBuilder;
    
    public JavaProject(String name, String path, String dir) {
        this.name = name;
        this.path = path;
        this.dir = dir;
        
        cfgStore = new CFGStore();
        pdgStore = new PDGStore(cfgStore);
    }
    
    public void setModelBuilder(ModelBuilder modelBuilder) {
        this.modelBuilder = modelBuilder;
    }
    
    public ModelBuilder getModelBuilder() {
        return modelBuilder;
    }
    
    public boolean isUnderPlugin() {
        return modelBuilder.isUnderPlugin();
    }
    
    public static JavaProject findProject(String path) {
        return ProjectStore.getInstance().getProject(path);
    }
    
    public void clear() {
        cfgStore.destroy();
        pdgStore.destroy();
        
        this.name = null;
        this.path = null;
        this.dir = null;
        
        for (JavaFile jfile : fileStore.values()) {
            jfile.dispose();
        }
        fileStore.clear();
        
        for (JavaPackage jpackage : packageStore.values()) {
            jpackage.dispose();
        }
        packageStore.clear();
        
        for (JavaClass jclass : classStore.values()) {
            jclass.dispose();
        }
        classStore.clear();
        
        for (JavaClass jclass : externalClasseStore.values()) {
            jclass.dispose();
        }
        externalClasseStore.clear();
        
        classPath = null;
        sourcePath = null;
        binaryPath = null;
    }
    
    public void dispose() {
        clear();
        fileStore = null;
        packageStore = null;
        classStore = null;
        externalClasseStore = null;
    }
    
    public String getName() {
        return name;
    }
    
    public String getPath() {
        return path;
    }
    
    public String getDir() {
        return dir;
    }
    
    public void addFile(JavaFile jfile) {
        fileStore.put(jfile.getPath(), jfile);
    }
    
    public void removeFile(String path) {
        JavaFile jfile = fileStore.get(path);
        if (jfile != null) {
            fileStore.remove(path);
            jfile.dispose();
        }
    }
    
    public JavaFile getFile(String path) {
        return fileStore.get(path);
    }
    
    public List<JavaFile> getFiles() {
        return new ArrayList<>(fileStore.values());
    }
    
    public List<JavaFile> getSortedFiles() {
        return sortFiles(fileStore.values());
    }
    
    protected void addPackage(JavaPackage jpackage) {
        packageStore.put(jpackage.getName(), jpackage);
    }
    
    public void removePackage(JavaPackage jpackage) {
        packageStore.remove(jpackage.getName());
        jpackage.dispose();
    }
    
    public List<JavaPackage> getPackages() {
        return new ArrayList<>(packageStore.values());
    }
    
    public List<JavaPackage> getSortedPackages() {
        return sortPackages(packageStore.values());
    }
    
    public JavaPackage getPackage(String name) {
        if (name != null && name.length() > 0) {
            return packageStore.get(name);
        }
        return null;
    }
    
    protected void addClass(JavaClass jclass) {
        classStore.put(jclass.getQualifiedName(), jclass);
    }
    
    public void removeClass(JavaClass jclass) {
        classStore.remove(jclass.getQualifiedName());
        jclass.dispose();
    }
    
    public List<JavaClass> getClasses() {
        return new ArrayList<>(classStore.values());
    }
    
    public List<JavaClass> getSortedClasses() {
        return sortClasses(classStore.values());
    }
    
    public JavaClass getClass(String fqn) {
        if (fqn != null && fqn.length() != 0) {
            return classStore.get(fqn);
        }
        return null;
    }
    
    public BytecodeClassStore getBytecodeClassStore() {
        return modelBuilder.getBytecodeClassStore();
    }
    
    public void registerBytecodeClasses() {
        modelBuilder.resisterBytecodeClasses(this);
    }
    
    public void addExternalClass(JavaClass jclass) {
        externalClasseStore.put(jclass.getQualifiedName(), jclass);
    }
    
    public JavaClass getExternalClass(String fqn) {
        if (fqn != null && fqn.length() > 0) {
            return externalClasseStore.get(fqn);
        }
        return null;
    }
    
    public Set<JavaClass> collectDanglingClasses(JavaClass jclass) {
        Set<JavaClass> classes = new HashSet<>();
        collectDanglingClasses(jclass, classes);
        return classes;
    }
    
    private void collectDanglingClasses(JavaClass jclass, Set<JavaClass> classes) {
        if (jclass != null && getClass(jclass.getQualifiedName()) != null) {
            for (JavaClass jc : jclass.getDescendants()) {
                classes.add(jc);
                collectDanglingClasses(jc, classes);
            }
            for (JavaClass jc: jclass.getAfferentClassesInProject()) {
                classes.add(jc);
                collectDanglingClasses(jc, classes);
            }
        }
    }
    
    public void removeClasses(List<JavaClass> classes) {
        for (JavaClass jclass : classes) {
            removeFile(jclass.getFile().getPath());
            removeClass(jclass);
        }
    }
    
    public void setClassPath(String[] classPath) {
        this.classPath = classPath;
    }
    
    public void setSourceBinaryPaths(String sourcePath, String binaryPath) {
        String[] srcPath = new String[1];
        srcPath[0] = sourcePath;
        String[] binPath = new String[1];
        binPath[0] = binaryPath;
        setSourceBinaryPaths(srcPath, binPath);
    }
    
    public void setSourceBinaryPaths(String[] sourcePath, String[] binaryPath) {
        this.sourcePath = sourcePath;
        this.binaryPath = binaryPath;
    }
    
    public String[] getClassPath() {
        return classPath;
    }
    
    public String[] getSourcePath() {
        return sourcePath;
    }
    
    public String[] getBinaryPath() {
        return binaryPath;
    }
    
    public void collectInfo(JavaClass jclass) {
        jclass.collectInfo();
    }
    
    public CFGStore getCFGStore() {
        return cfgStore;
    }
    
    public PDGStore getPDGStore() {
        return pdgStore;
    }
    
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("\n");
        buf.append("JPROJECT: ");
        buf.append(getName());
        buf.append(" [");
        buf.append(getPath());
        buf.append("]");
        return buf.toString();
    }
    
    private List<JavaFile> sortFiles(Collection<? extends JavaFile> co) {
        List<JavaFile> jfiles = new ArrayList<>(co);
        Collections.sort(jfiles, new Comparator<>() {
            public int compare(JavaFile jf1, JavaFile jf2) {
                return jf1.getPath().compareTo(jf2.getPath());
            }
        });
        return jfiles;
    }
    
    private List<JavaPackage> sortPackages(Collection<? extends JavaPackage> collection) {
        return collection
                .stream()
                .sorted((jp1, jp2) -> jp1.getName().compareTo(jp2.getName()))
                .collect(Collectors.toList());
    }
    
    private List<JavaClass> sortClasses(Collection<? extends JavaClass> collection) {
        return collection
                .stream()
                .sorted((jc1, jc2) -> jc1.getQualifiedName().compareTo(jc2.getQualifiedName()))
                .collect(Collectors.toList());
    }
}
