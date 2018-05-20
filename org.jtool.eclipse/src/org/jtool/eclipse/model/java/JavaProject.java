/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.model.java;

import java.util.HashMap;
import java.util.Map;

import org.jtool.eclipse.ProjectStore;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

/**
 * An object representing a Eclipse's project.
 * @author Katsuhisa Maruyama
 */
public class JavaProject {
    
    protected String name;
    protected String path;
    
    protected Map<String, JavaFile> fileStore = new HashMap<String, JavaFile>();
    protected Map<String, JavaPackage> packageStore = new HashMap<String, JavaPackage>();
    protected Map<String, JavaClass> classStore = new HashMap<String, JavaClass>();
    protected Map<String, JavaClass> externalClasseStore = new HashMap<String, JavaClass>();
    
    public JavaProject(String name, String path) {
        this.name = name;
        this.path = path;
    }
    
    public void clear() {
        this.name = null;
        this.path = null;
        
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
        return new ArrayList<JavaFile>(fileStore.values());
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
        return new ArrayList<JavaPackage>(packageStore.values());
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
        return new ArrayList<JavaClass>(classStore.values());
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
    
    public void addExternalClass(JavaClass jclass) {
        externalClasseStore.put(jclass.getQualifiedName(), jclass);
    }
    
    JavaClass getExternalClass(String fqn) {
        if (fqn != null && fqn.length() > 0) {
            return externalClasseStore.get(fqn);
        }
        return null;
    }
    
    public void collectInfo() {
        int count = 0;
        int num = classStore.values().size();
        for (JavaClass jclass : classStore.values()) {
            count++;
            jclass.collectInfo();
            printLog(" - " + jclass.getQualifiedName() + " (" + count + "/" + num + ")");
        }
    }
    
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("\n");
        buf.append("JSTORE: ");
        buf.append(getName());
        buf.append(" [");
        buf.append(getPath());
        buf.append("]");
        return buf.toString();
    }
    
    private List<JavaFile> sortFiles(Collection<? extends JavaFile> co) {
        List<JavaFile> jfiles = new ArrayList<JavaFile>(co);
        Collections.sort(jfiles, new Comparator<JavaFile>() {
            public int compare(JavaFile jf1, JavaFile jf2) {
                return jf1.getPath().compareTo(jf2.getPath());
            }
        });
        return jfiles;
    }
    
    private List<JavaPackage> sortPackages(Collection<? extends JavaPackage> co) {
        List<JavaPackage> jpackages = new ArrayList<JavaPackage>(co);
        Collections.sort(jpackages, new Comparator<JavaPackage>() {
            public int compare(JavaPackage jp1, JavaPackage jp2) {
                return jp1.getName().compareTo(jp2.getName());
            }
        });
        return jpackages;
    }
    
    private List<JavaClass> sortClasses(Collection<? extends JavaClass> co) {
        List<JavaClass> jclasses = new ArrayList<JavaClass>(co);
        Collections.sort(jclasses, new Comparator<JavaClass>() {
            public int compare(JavaClass jc1, JavaClass jc2) {
                return jc1.getQualifiedName().compareTo(jc2.getQualifiedName());
            }
        });
        return jclasses;
    }
    
    public void printLog(String mesg) {
        ProjectStore.getInstance().printLog(mesg);
    }
    
    public void printError(String mesg) {
        ProjectStore.getInstance().printError(mesg);
    }
    
    public void printUnresolvedError(String mesg) {
        ProjectStore.getInstance().printUnresolvedError(mesg);
    }
}
