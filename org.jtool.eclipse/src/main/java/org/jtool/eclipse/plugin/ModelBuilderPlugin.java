/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.plugin;

import org.jtool.eclipse.javamodel.JavaProject;
import org.jtool.eclipse.javamodel.JavaFile;
import org.jtool.eclipse.javamodel.JavaClass;
import org.jtool.eclipse.javamodel.JavaPackage;
import org.jtool.eclipse.javamodel.builder.BytecodeClassStore;
import org.jtool.eclipse.javamodel.builder.JavaASTVisitor;
import org.jtool.eclipse.javamodel.builder.ModelBuilder;
import org.jtool.eclipse.javamodel.builder.ProjectStore;
import org.jtool.eclipse.util.Logger;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * Manages resources within Eclipse's projects.
 * 
 * @author Katsuhisa Maruyama
 */

public class ModelBuilderPlugin extends ModelBuilder {
    
    private IJavaProject javaProject;
    
    private ResourceChangeListener resourceChangeListener;
    
    private Map<String, ICompilationUnit> compilationUnitMap = new HashMap<String, ICompilationUnit>();
    private Set<IFile> dirtyFiles = new HashSet<IFile>();
    
    private JXConsole console = new JXConsole();
    
    public ModelBuilderPlugin() {
        super(true);
        resourceChangeListener = new ResourceChangeListener(this);
    }
    
    public ModelBuilderPlugin(boolean analyzingBytecode) {
        super(analyzingBytecode);
        resourceChangeListener = new ResourceChangeListener(this);
    }
    
    public boolean isUnderPlugin() {
        return true;
    }
    
    public void start() {
        resourceChangeListener.register();
    }
    
    public void stop() {
        resourceChangeListener.unregister();
    }
    
    public JXConsole getConsole() {
        return console;
    }
    
    public void addFileChangeListener(IFileChangeListener listener) {
        resourceChangeListener.addFileChangeListener(listener);
    }
    
    public void removeFileChangeListener(IFileChangeListener listener) {
        resourceChangeListener.removeFileChangeListener(listener);
    }
    
    void addFile(IFile file) {
        dirtyFiles.add(file);
    }
    
    void removeFile(IFile file) {
        JavaProject jproject = ProjectStore.getInstance().getProject(file.getProject().getFullPath().toString());
        if (jproject == null) {
            return;
        }
        
        Set<JavaFile> files = collectDanglingClasses(jproject, file);
        for (JavaFile jfile : files) {
            for (JavaClass jc : jfile.getClasses()) {
                jproject.removeClass(jc);
            }
            jproject.removeFile(jfile.getPath());
        }
        
        for (JavaPackage jpackage : jproject.getPackages()) {
            if (jpackage.getClasses().size() == 0) {
                jproject.removePackage(jpackage);
            }
        }
    }
    
    void changeFile(IFile file) {
        removeFile(file);
        addFile(file);
    }
    
    private Set<JavaFile> collectDanglingClasses(JavaProject jproject, IFile file) {
        JavaFile jfile = jproject.getFile(file.getFullPath().toString());
        Set<JavaClass> classes = new HashSet<JavaClass>();
        for (JavaClass jclass : jfile.getClasses()) {
            classes.addAll(jfile.getProject().collectDanglingClasses(jclass));
        }
        
        Set<JavaFile> files = new HashSet<JavaFile>();
        for (JavaClass jclass : classes) {
            files.add(jclass.getFile());
        }
        return files;
    }
    
    private void setPaths(JavaProject jproject, IJavaProject project) {
        jproject.setClassPath(getClassPath(project));
        jproject.setSourceBinaryPaths(getSourcePath(project), getBinaryPath(project));
    }
    
    private String[] getClassPath(IJavaProject project) {
        try {
            List<String> classPathList = new ArrayList<String>();
            for (IClasspathEntry entry : project.getResolvedClasspath(true)) {
                if (entry.getEntryKind() != IClasspathEntry.CPE_SOURCE) {
                    classPathList.add(entry.getPath().makeAbsolute().toOSString());
                }
            }
            return classPathList.toArray(new String[classPathList.size()]);
        } catch (JavaModelException e) {
            return new String[0];
        }
    }
    
    private String[] getSourcePath(IJavaProject project) {
        try {
            IWorkspaceRoot workSpaceRoot = ResourcesPlugin.getWorkspace().getRoot();
            List<String> sourcePathList = new ArrayList<String>();
            for (IPackageFragmentRoot root : project.getAllPackageFragmentRoots()) {
                if (root.getKind() == IPackageFragmentRoot.K_SOURCE) {
                    sourcePathList.add(workSpaceRoot.getFolder(root.getPath()).getLocation().toOSString());
                }
            }
            return sourcePathList.toArray(new String[sourcePathList.size()]);
        } catch (JavaModelException e) {
            return new String[0];
        }
    }
    
    private String getBinaryPath(IJavaProject project) {
        try {
            IWorkspaceRoot workSpaceRoot = ResourcesPlugin.getWorkspace().getRoot();
            return workSpaceRoot.getFolder(project.getOutputLocation()).getLocation().toOSString();
        } catch (JavaModelException e) {
            return "";
        }
    }
    
    public void build(IFile file) {
        IJavaProject project = JavaCore.create(file.getProject());
        ProjectStore.getInstance().setModelBuilder(this);
        JavaProject jproject = ProjectStore.getInstance().getProject(file.getProject().getFullPath().toString());
        if (jproject == null || jproject.getFiles().size() == 0) {
            if (project != null) {
                buildWhole(project);
            }
        } else {
            Set<ICompilationUnit> compilationUnits = new HashSet<ICompilationUnit>();
            javaProject = project;
            currentProject = ProjectStore.getInstance().getProject(project.getPath().toString());
            setPaths(jproject, project);
            
            if (project != null) {
                compilationUnits.add(JavaCore.createCompilationUnitFrom(file));
                Set<JavaFile> files = collectDanglingClasses(jproject, file);
                for (JavaFile jf : files) {
                    ICompilationUnit icu = compilationUnitMap.get(jf.getPath());
                    if (icu != null) {
                        compilationUnits.add(icu);
                        compilationUnitMap.put(icu.getPath().toString(), icu);
                    }
                }
            }
            buildJavaModel(compilationUnits, jproject);
        }
    }
    
    public void build(IProject project) {
        build(JavaCore.create(project));
    }
    
    public JavaProject build(IJavaProject project) {
        ProjectStore.getInstance().setModelBuilder(this);
        JavaProject jproject = ProjectStore.getInstance().getProject(project.getPath().toString());
        if (jproject == null || jproject.getFiles().size() == 0) {
            return buildWhole(project);
        }
        
        Set<ICompilationUnit> compilationUnits = new HashSet<ICompilationUnit>();
        javaProject = project;
        currentProject = ProjectStore.getInstance().getProject(project.getPath().toString());
        setPaths(jproject, project);
        
        for (IFile file : dirtyFiles) {
            compilationUnits.add(JavaCore.createCompilationUnitFrom(file));
            Set<JavaFile> files = collectDanglingClasses(jproject, file);
            for (JavaFile jf : files) {
                ICompilationUnit icu = compilationUnitMap.get(jf.getPath());
                if (icu != null) {
                    compilationUnits.add(icu);
                    compilationUnitMap.put(icu.getPath().toString(), icu);
                }
            }
        }
        dirtyFiles.clear();
        
        buildJavaModel(compilationUnits, jproject);
        return jproject;
    }
    
    public JavaProject update() {
        cfgStore.create(currentProject, analyzingBytecode);
        
        ProjectStore.getInstance().removeProject(currentProject.getPath());
        return buildWhole(javaProject);
    }
    
    private JavaProject buildWhole(IJavaProject project) {
        String name = project.getProject().getName();
        String path = project.getProject().getFullPath().toString();
        String dir = project.getProject().getLocation().toString();
        javaProject = project;
        currentProject = new JavaProject(name, path, dir);
        currentProject.setModelBuilder(this);
        setPaths(currentProject, project);
        ProjectStore.getInstance().addProject(currentProject);
        
        cfgStore.create(currentProject, analyzingBytecode);
        
        Set<ICompilationUnit> compilationUnits = collectCompilationUnits(project, currentProject);
        buildJavaModel(compilationUnits, currentProject);
        return currentProject;
    }
    
    private Set<ICompilationUnit> collectCompilationUnits(IJavaProject project, JavaProject jproject) {
        Set<ICompilationUnit> compilationUnits = new HashSet<ICompilationUnit>();
        if (project.getElementName().equals("RemoteSystemsTempFiles")) {
            return compilationUnits;
        }
        
        try {
            IPackageFragment[] packages = project.getPackageFragments();
            for (int i = 0; i < packages.length; i++) {
                ICompilationUnit[] units = packages[i].getCompilationUnits();
                for (int j = 0; j < units.length; j++) {
                    ICompilationUnit icu = units[j];
                    if (icu.hasUnsavedChanges()) {
                        icu.save(null, true);
                    }
                    compilationUnits.add(icu);
                    compilationUnitMap.put(icu.getPath().toString(), icu);
                }
            }
        } catch (JavaModelException e) {
            printError("JavaModelException occurred: " + e.getMessage());
        }
        return compilationUnits;
    }
    
    private boolean buildJavaModel(final Set<ICompilationUnit> cunits, JavaProject jproject) {
        try {
            final ASTParser parser = getParser();
            IWorkbenchWindow workbenchWindow = Activator.getPlugin().getWorkbenchWindow();
            workbenchWindow.run(true, true, new IRunnableWithProgress() {
                
                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    int size = cunits.size();
                    monitor.beginTask("Parsing files... ", size);
                    int count = 1;
                    for (ICompilationUnit icu : cunits) {
                        monitor.subTask(count + "/" + size + " - " + icu.getPath().toString());
                        try {
                            buildJavaModel(parser, icu, jproject);
                        } catch (NullPointerException e) {
                            printError("* Fatal error occurred. Skip the paser of " + icu.getPath().toString());
                            e.printStackTrace();
                        }
                        if (monitor.isCanceled()) {
                            monitor.done();
                            throw new InterruptedException();
                        }
                        monitor.worked(1);
                        count++;
                    }
                    monitor.done();
                }
            });
            
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            printError("* InvocationTargetException occurred because " + cause);
            return false;
        } catch (InterruptedException e) {
            return false;
        }
        return true;
    }
    
    private void buildJavaModel(ASTParser parser, ICompilationUnit icu, JavaProject jproject) {
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setResolveBindings(true);
        parser.setStatementsRecovery(true);
        parser.setBindingsRecovery(true);
        parser.setSource(icu);
        
        CompilationUnit cu = (CompilationUnit)parser.createAST(null);
        if (cu != null) {
            String code = getSource(icu);
            if (code != null) {
                JavaFile jfile = new JavaFile(cu, icu.getPath().toString(), code, JavaCore.getEncoding(), jproject);
                if (getParseErrors(cu).size() != 0) {
                    printError("Incomplete parse: " + icu.getPath().makeRelative().toString());
                }
                
                JavaASTVisitor visitor = new JavaASTVisitor(jfile);
                cu.accept(visitor);
                visitor.terminate();
                jproject.addFile(jfile);
            }
        }
    }
    
    public String getSource(ICompilationUnit icu) {
        try {
            return icu.getSource();
        } catch (JavaModelException e) {
            return null;
        }
    }
    
    private void printError(String mesg) {
        Logger.getInstance().printError(mesg);
        console.println(mesg);
    }
    
    @Override
    public void resisterBytecodeClasses(BytecodeClassStore bytecodeClassStore) {
        try {
            IWorkbenchWindow workbenchWindow = Activator.getPlugin().getWorkbenchWindow();
            workbenchWindow.run(true, true, new IRunnableWithProgress() {
                
                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    int size = bytecodeClassStore.getBytecodeClassNames().size();
                    monitor.beginTask("Parsing external classes... ", size);
                    int count = 1;
                    for (String className : bytecodeClassStore.getBytecodeClassNames()) {
                        monitor.subTask(count + "/" + size + " - " + className);
                        try {
                            bytecodeClassStore.registerBytecodeClass(className);
                        } catch (NullPointerException e) {
                            printError("* Fatal error occurred. Skip the paser of " + className);
                            e.printStackTrace();
                        }
                        if (monitor.isCanceled()) {
                            monitor.done();
                            throw new InterruptedException();
                        }
                        monitor.worked(1);
                        count++;
                    }
                    monitor.done();
                }
            });
            
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            printError("* InvocationTargetException occurred because " + cause);
        } catch (InterruptedException e) { /* empty */ }
    }
}
