/*
 *  Copyright 2018-2020
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
    
    private Map<String, ClassPool> classPools = new HashMap<>();
    
    private Map<String, Map<String, BytecodeClassInfo>> bytecodeClassInfo = new HashMap<>();
    
    public BytecodeClassStore() {
    }
    
    public Set<String> createBytecodeClassStore(JavaProject jproject) {
        List<String> commonLibraryClassPath = getCommonLibraryClassPath();
        List<String> classPath = getClassPath(jproject);
        classPath.addAll(commonLibraryClassPath);
        String[] classPaths = classPath.toArray(new String[classPath.size()]);
        try {
            classPools.put(jproject.getPath(), getClassPool(classPaths));
        } catch (NotFoundException e) {
            try {
                classPaths = commonLibraryClassPath.toArray(new String[commonLibraryClassPath.size()]);
                classPools.put(jproject.getPath(), getClassPool(classPaths));
            } catch (NotFoundException e2) { /* empty */ }
        }
        
        return classPath.stream()
                        .flatMap(path -> collectBytecodeClassNames(path).stream())
                        .collect(Collectors.toSet());
    }
    
    private static ClassPool getClassPool(String[] classPath) throws NotFoundException {
        ClassPool classpool = new ClassPool(true);
        for (String path : classPath) {
            classpool.insertClassPath(path);
        }
        return classpool;
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
    
    public CtClass getCtClassByCanonicalClassName(JavaProject jproject, String className) {
        Map<String, BytecodeClassInfo> classInfoMap = bytecodeClassInfo.get(jproject.getPath());
        if (classInfoMap != null) {
            BytecodeClassInfo classInfo = classInfoMap.get(className);
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
        String[] projectClassPath = jproject.getClassPath();
        List<String> classpaths = new ArrayList<String>();
        for (int i = 0; i < projectClassPath.length; i++) {
            File file = new File(projectClassPath[i]);
            if (file.exists()) {
                classpaths.add(projectClassPath[i]);
            }
        }
        return classpaths;
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
            return true;
        }
        classInfoMap = new HashMap<String, BytecodeClassInfo>();
        bytecodeClassInfo.put(jproject.getPath(), classInfoMap);
        return false;
    }
    
    public void registerBytecodeClass(JavaProject jproject, String className) {
        try {
            Map<String, BytecodeClassInfo> classInfoMap = bytecodeClassInfo.get(jproject.getPath());
            if (classInfoMap == null) {
                return;
            }
            ClassPool classPool = classPools.get(jproject.getPath());
            if (classPool != null) {
                CtClass ctClass = classPool.get(className);
                
                if (!ctClass.getName().startsWith("META-INF.")) { // javassist does not support multi-release JARs
                    
                    if (ctClass.isInterface() || ctClass.getModifiers() != Modifier.PRIVATE) {
                        BytecodeClassInfo classInfo = new BytecodeClassInfo(ctClass);
                        classInfoMap.put(getCanonicalClassName(ctClass), classInfo);
                    }
                }
            }
        } catch (NotFoundException e) { /* empty */ }
    }
    
    public void collectBytecodeClassInfo(JavaProject jproject) {
        Map<String, BytecodeClassInfo> classInfoMap = bytecodeClassInfo.get(jproject.getPath());
        for (BytecodeClassInfo classInfo : classInfoMap.values()) {
            for (BytecodeClassInfo parent : classInfo.getParents()) {
                BytecodeClassInfo parentInfo = classInfoMap.get(parent.getName());
                if (parentInfo != null) {
                    parentInfo.addChild(classInfo);
                }
            }
        }
        
        for (BytecodeClassInfo classInfo : classInfoMap.values()) {
            classInfo.setAncestors(classInfo);
            classInfo.setDescendants(classInfo);
        }
    }
    
    public String getCanonicalClassName(JavaProject jproject, String className) {
        try {
            ClassPool classPool = classPools.get(jproject.getPath());
            if (classPool != null) {
                CtClass ctClass = classPool.get(className);
                return getCanonicalClassName(ctClass);
            }
            return null;
        } catch (NotFoundException e) {
            return null;
        }
    }
    
    public static String getCanonicalClassName(CtClass ctClass) {
        String className = ctClass.getName();
        try {
            CtClass parent = ctClass.getDeclaringClass();
            while (parent != null) {
                String pname = parent.getName();
                String iname = className.substring(pname.length() + 1);
                if (isAnonymousClass(iname)) {
                    className = pname + "$" + iname;
                } else {
                    className = pname + "." + iname;
                }
                parent = parent.getDeclaringClass();
            }
            return className;
        } catch (NotFoundException e) {
            return className;
        }
    }
    
    private static boolean isAnonymousClass(String className) {
        return className.matches("\\d+?");
    }
    
    public static String getCanonicalSimpleClassName(CtClass ctClass) {
        String className = getCanonicalClassName(ctClass);
        return className.substring(ctClass.getPackageName().length() + 1);
        //return className.substring(ctClass.getName().length() - ctClass.getSimpleName().length());
    }
}
