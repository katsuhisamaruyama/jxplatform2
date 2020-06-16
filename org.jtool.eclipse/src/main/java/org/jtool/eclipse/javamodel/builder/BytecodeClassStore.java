/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.javamodel.builder;

import org.jtool.eclipse.javamodel.JavaProject;
import org.jtool.eclipse.javamodel.JavaClass;
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
import java.util.stream.Collectors;
import java.util.Enumeration;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;

/**
 * An object that stores classes restored from its byte-code.
 * This class uses Javassit modules.
 * 
 * All methods of this class are not intended to be directly called by clients.
 * 
 * @author Katsuhisa Maruyama
 */
public class BytecodeClassStore {
    
    private static List<String> commonLibraryClassPath;
    private static Set<String> commonLibraryClassNames = new HashSet<String>();
    
    private ClassPool classPool;
    
    private Map<String, Map<String, BytecodeClassInfo>> bytecodeClassInfo = new HashMap<String, Map<String, BytecodeClassInfo>>();
    private Map<String, BytecodeClassInfo> classInfoMapCache = null;
    
    public BytecodeClassStore() {
        commonLibraryClassPath = getCommonLibraryClassPath();
        for (String path : commonLibraryClassPath) {
            commonLibraryClassNames.addAll(collectBytecodeClassNames(path));
        }
    }
    
    public Set<String> createBytecodeClassStore(JavaProject jproject) {
        List<String> classPath = getClassPath(jproject);
        Set<String> classNames = new HashSet<String>();
        for (String path : classPath) {
            classNames.addAll(collectBytecodeClassNames(path));
        }
        classPath.addAll(commonLibraryClassPath);
        classNames.addAll(commonLibraryClassNames);
        
        String[] classPaths = classPath.toArray(new String[classPath.size()]);
        try {
            classPool = getClassPool(classPaths);
        } catch (NotFoundException e) {
            try {
                String[] cclassPaths = commonLibraryClassPath.toArray(new String[commonLibraryClassPath.size()]);
                classPool = getClassPool(cclassPaths);
            } catch (NotFoundException e2) { /* empty */ }
        }
        return classNames;
    }
    
    public Set<CtClass> getCtClasses(JavaProject jproject) {
        Set<CtClass> classes = new HashSet<CtClass>();
        Map<String, BytecodeClassInfo> classInfoMap = bytecodeClassInfo.get(jproject.getPath());
        if (classInfoMap != null) {
            for (BytecodeClassInfo classInfo : classInfoMap.values()) {
                classes.add(classInfo.getCtClass());
            }
        }
        return classes;
    }
    
    public CtClass getCtClass(JavaProject jproject, String fqn) {
        Map<String, BytecodeClassInfo> classInfoMap = bytecodeClassInfo.get(jproject.getPath());
        if (classInfoMap != null) {
            BytecodeClassInfo classInfo = classInfoMap.get(fqn);
            if (classInfo != null) {
                return classInfo.getCtClass();
            }
        }
        return null;
    }
    
    public Set<CtClass> getAncestors(JavaProject jproject, String fqn) {
        Map<String, BytecodeClassInfo> classInfoMap = bytecodeClassInfo.get(jproject.getPath());
        if (classInfoMap != null) {
            BytecodeClassInfo classInfo = classInfoMap.get(fqn);
            if (classInfo != null) {
                return classInfo.getAncestors();
            }
        }
        return new HashSet<CtClass>();
    }
    
    public Set<CtClass> getDescendants(JavaProject jproject, String fqn) {
        Map<String, BytecodeClassInfo> classInfoMap = bytecodeClassInfo.get(jproject.getPath());
        if (classInfoMap != null) {
            BytecodeClassInfo classInfo = classInfoMap.get(fqn);
            if (classInfo != null) {
                return classInfo.getDescendants();
            }
        }
        return new HashSet<CtClass>();
    }
    
    public Set<JavaClass> getJavaDescendants(JavaProject jproject, String fqn) {
        Map<String, BytecodeClassInfo> classInfoMap = bytecodeClassInfo.get(jproject.getPath());
        if (classInfoMap != null) {
            BytecodeClassInfo classInfo = classInfoMap.get(fqn);
            if (classInfo != null) {
                Set<JavaClass> children = jproject.getClasses().stream()
                        .filter(jc -> isChildOf(jc, fqn)).collect(Collectors.toSet());
                children.stream().map(jc -> jc.getDescendants())
                        .map(list -> list.stream()).collect(Collectors.toSet());
            }
        }
        return new HashSet<JavaClass>();
    }
    
    private boolean isChildOf(JavaClass jclass, String fqn) {
        if (jclass.getSuperClassName() != null && jclass.getSuperClassName().equals(fqn)) {
            return true;
        }
        
        return jclass.getSuperInterfaceNames().stream().anyMatch(name -> name.equals(fqn));
    }
    
    private List<String> getCommonLibraryClassPath() {
        List<String> classpaths = new ArrayList<String>();
        
        String[] bootClassPath = System.getProperty("sun.boot.class.path").split(File.pathSeparator, 0);
        for (int i = 0; i < bootClassPath.length; i++) {
            File file = new File(bootClassPath[i]);
            if (file.exists()) {
                classpaths.add(bootClassPath[i]);
            }
        }
        
        String[] extDirs = System.getProperty("java.ext.dirs").split(File.pathSeparator, 0);
        for (int i = 0; i < extDirs.length; i++) {
            File file = new File(extDirs[i]);
            if (file.exists()) { 
                classpaths.add(extDirs[i]);
            }
        }
        
        String[] endorsedDirs = System.getProperty("java.endorsed.dirs").split(File.pathSeparator, 0);
        for (int i = 0; i < endorsedDirs.length; i++) {
            File file = new File(endorsedDirs[i]);
            if (file.exists()) {
                classpaths.add(endorsedDirs[i]);
            }
        }
        
        return classpaths;
    }
    
    private List<String> getClassPath(JavaProject jproject) {
        String cdir = new File(".").getAbsoluteFile().getParent();
        String pdir = jproject.getDir();
        String[] projectClassPath = jproject.getClassPath();
        
        List<String> classpaths = new ArrayList<String>();
        for (int i = 0; i < projectClassPath.length; i++) {
            if (!projectClassPath[i].startsWith(cdir) && !projectClassPath[i].startsWith(pdir)) {
                File file = new File(projectClassPath[i]);
                if (file.exists()) {
                    classpaths.add(projectClassPath[i]);
                }
            }
        }
        return classpaths;
    }
    
    private static ClassPool getClassPool(String[] classPath) throws NotFoundException {
        ClassPool classpool = ClassPool.getDefault();
        for (String path : classPath) {
            classpool.insertClassPath(path);
        }
        return classpool;
    }
    
    private Set<String> collectBytecodeClassNames(String path) {
        Set<String> classNames = new HashSet<String>();
        File file = new File(path);
        if (file.isDirectory()) {
            collectClassFiles(classNames, path, "");
        } else if (file.isFile() && (path.endsWith(".jar") || path.endsWith(".zip"))) {
            collectClassFilesInJar(classNames, file);
        }
        return classNames;
    }
    
    private void collectClassFiles(Set<String> classNames, String classPath, String name) {
        File file = new File(classPath + File.separator + name);
        if (file.isDirectory()) {
            String[] names = file.list();
            for (int i = 0; i < names.length; i++) {
                if (name.length() == 0) {
                    collectClassFiles(classNames, classPath, names[i]);
                } else {
                    collectClassFiles(classNames, classPath, name + File.separatorChar + names[i]);
                }
            }
            
        } else if (file.isFile() && name.endsWith(".class")) {
            name = name.substring(0, name.length() - 6);
            name = name.replace(File.separatorChar, '.');
            registerClassName(classNames, name);
        }
    }
    
    private void collectClassFilesInJar(Set<String> classNames, File file) {
        try (ZipFile zipFile = new ZipFile(file)) {
            for (Enumeration<? extends ZipEntry> entries = zipFile.entries(); entries.hasMoreElements(); ) {
                ZipEntry entry = entries.nextElement();
                String name = entry.getName();
                if (name.endsWith(".class")) {
                    registerClassName(classNames, name);
                }
            }
        } catch (IOException e) { /* empty */ }
    }
    
    private void registerClassName(Set<String> classNames, String name) {
        String className = name.substring(0, name.length() - 6);
        className = className.replace(File.separatorChar, '.');
        classNames.add(className);
    }
    
    public boolean existsBytecodeClassInfo(JavaProject jproject) {
        Map<String, BytecodeClassInfo> classInfoMap = bytecodeClassInfo.get(jproject.getPath());
        if (classInfoMap != null) {
            classInfoMapCache = classInfoMap;
            return true;
        }
        classInfoMap = new HashMap<String, BytecodeClassInfo>();
        bytecodeClassInfo.put(jproject.getPath(), classInfoMap);
        classInfoMapCache = classInfoMap;
        return false;
    }
    
    public void registerBytecodeClass(JavaProject jproject, String className) {
        try {
            CtClass ctClass = classPool.get(className);
            if (ctClass.isInterface() || ctClass.getModifiers() != Modifier.PRIVATE) {
                BytecodeClassInfo classInfo = new BytecodeClassInfo(ctClass);
                classInfoMapCache.put(className, classInfo);
            }
        } catch (NotFoundException e) { /* empty */ }
    }
    
    public void collectBytecodeClassInfo(JavaProject jproject) {
        for (BytecodeClassInfo classInfo : classInfoMapCache.values()) {
            for (BytecodeClassInfo parent : classInfo.getParents()) {
                BytecodeClassInfo parentInfo = classInfoMapCache.get(parent.getName());
                if (parentInfo != null) {
                    parentInfo.addChild(classInfo);
                }
            }
        }
        
        for (BytecodeClassInfo classInfo : classInfoMapCache.values()) {
            classInfo.setAncestors(classInfo);
            classInfo.setDescendants(classInfo);
        }
        
        classInfoMapCache = null;
    }
}
