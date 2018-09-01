/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.javamodel.builder;

import org.jtool.eclipse.javamodel.JavaProject;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.Modifier;
import javassist.NotFoundException;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;

/**
 * An object holds a collection of classes restored from its bytecode.
 * This class uses Javassit modules.
 * 
 * All methods of this class are not intended to be directly called by clients.
 * 
 * @author Katsuhisa Maruyama
 */
public class BytecodeClassStore {
    
    private Map<String, BytecodeClassInfo> classStore = new HashMap<String, BytecodeClassInfo>();
    
    private Set<String> classNames = new HashSet<String>();
    private ClassPool classPool;
    
    private JavaProject jproject;
    
    public BytecodeClassStore(JavaProject jproject) {
        this.jproject = jproject;
        collectBytecodeClassNames();
    }
    
    public Set<String> getBytecodeClassNames() {
        return classNames;
    }
    
    public CtClass getExternalClass(String fqn) {
        BytecodeClassInfo classInfo = classStore.get(fqn);
        if (classInfo != null) {
            return classInfo.getCtClass();
        } else {
            return null;
        }
    }
    
    public Set<CtClass> getParents(String fqn) {
        BytecodeClassInfo classInfo = classStore.get(fqn);
        if (classInfo != null) {
            return classInfo.getParents();
        } else {
            return null;
        }
    }
    
    public Set<CtClass> getChildren(String fqn) {
        BytecodeClassInfo classInfo = classStore.get(fqn);
        if (classInfo != null) {
            return classInfo.getChildren();
        } else {
            return null;
        }
    }
    
    private void collectBytecodeClassNames() {
        classStore.clear();
        
        String[] classPath = getClassPath();
        for (String path : classPath) {
            collectBytecodeClassNames(path);
        }
    }
    
    private String[] getClassPath() {
        String cdir = new File(".").getAbsoluteFile().getParent();
        String pdir = jproject.getDir();
        String[] projectClassPath = jproject.getClassPath();
        String[] systemClassPath = System.getProperty("java.class.path").split(File.pathSeparator, 0);
        List<String> classpaths = new ArrayList<String>();
        for (int i = 0; i < projectClassPath.length; i++) {
            if (!projectClassPath[i].startsWith(cdir) && !projectClassPath[i].startsWith(pdir)) {
                classpaths.add(projectClassPath[i]);
            }
        }
        for (int i = 0; i < systemClassPath.length; i++) {
            if (!systemClassPath[i].startsWith(cdir) && !systemClassPath[i].startsWith(pdir)) {
                classpaths.add(systemClassPath[i]);
            }
        }
        
        String[] classPath = classpaths.toArray(new String[classpaths.size()]);
        try {
            classPool = getClassPool(classPath);
            return classPath;
        
        } catch (NotFoundException e) {
            return new String[0];
        }
    }
       
    
    private static ClassPool getClassPool(String[] classPath) throws NotFoundException {
        ClassPool classpool = ClassPool.getDefault();
        for (String path : classPath) {
            classpool.insertClassPath(path);
            // System.out.println("CLASSPATH = " + path);
        }
        return classpool;
    }
    
    private void collectBytecodeClassNames(String path) {
        File file = new File(path);
        if (file.isDirectory()) {
            collectClassFiles(path, "");
        } else if (file.isFile() && (path.endsWith(".jar") || path.endsWith(".zip"))) {
            collectClassFilesInJar(file);
        }
    }
    
    private void collectClassFiles(String classPath, String name) {
        File file = new File(classPath + File.separator + name);
        if (file.isDirectory()) {
            String[] names = file.list();
            for (int i = 0; i < names.length; i++) {
                if (name.length() == 0) {
                    collectClassFiles(classPath, names[i]);
                } else {
                    collectClassFiles(classPath, name + File.separatorChar + names[i]);
                }
            }
            
        } else if (file.isFile() && name.endsWith(".class")) {
            name = name.substring(0, name.length() - 6);
            name = name.replace(File.separatorChar, '.');
            registerClassName(name);
        }
    }
    
    private void collectClassFilesInJar(File file) {
        try (ZipFile zipFile = new ZipFile(file)) {
            for (Enumeration<? extends ZipEntry> entries = zipFile.entries(); entries.hasMoreElements(); ) {
                ZipEntry entry = entries.nextElement();
                String name = entry.getName();
                if (name.endsWith(".class")) {
                    registerClassName(name);
                }
            }
        } catch (IOException e) { /* empty */ }
    }
    
    private void registerClassName(String name) {
        String className = name.substring(0, name.length() - 6);
        className = className.replace(File.separatorChar, '.');
        classNames.add(className);
    }
    
    public void registerBytecodeClass(String className) {
        try {
            CtClass ctClass = classPool.get(className);
            if (ctClass.isInterface() || ctClass.getModifiers() == Modifier.PUBLIC) {
                BytecodeClassInfo classInfo = new BytecodeClassInfo(ctClass);
                classStore.put(className, classInfo);
            }
        } catch (NotFoundException e) { /* empty */ }
    }
    
    public void collectBytecodeClassInfo() {
        for (BytecodeClassInfo classInfo : classStore.values()) {
            for (CtClass parent : classInfo.getParents()) {
                BytecodeClassInfo parentInfo = classStore.get(parent.getName());
                if (parentInfo != null) {
                    parentInfo.addChild(classInfo.getCtClass());
                }
            }
        }
    }
    
    private class BytecodeClassInfo {
        
        private CtClass ctClass;
        private Set<CtClass> parents = new HashSet<CtClass>();
        private Set<CtClass> children = new HashSet<CtClass>();
        
        BytecodeClassInfo(CtClass ctClass) {
            this.ctClass = ctClass;
            
            try {
                CtClass superClass = ctClass.getSuperclass();
                if (superClass != null) {
                    parents.add(superClass);
                }
                for (CtClass c : ctClass.getInterfaces()) {
                    parents.add(c);
                }
            } catch (NotFoundException e) { /* empty */ }
        }
        
        CtClass getCtClass() {
            return ctClass;
        }
        
        Set<CtClass> getParents() {
            return parents;
        }
        
        Set<CtClass> getChildren() {
            return children;
        }
        
        void addChild(CtClass child) {
            children.add(child);
        }
    }
}
