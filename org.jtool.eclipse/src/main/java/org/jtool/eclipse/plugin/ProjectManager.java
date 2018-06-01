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
import org.jtool.eclipse.javamodel.builder.JavaASTVisitor;
import org.jtool.eclipse.javamodel.builder.ProjectStore;
import org.jtool.eclipse.plugin.IFileChangeListener;
import org.jtool.eclipse.plugin.ResourceChangeListener;
import org.jtool.eclipse.util.Logger;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

/**
 * Manages resources within Eclipse's projects.
 * @author Katsuhisa Maruyama
 */
public class ProjectManager {
    
    private static ProjectManager instance = new ProjectManager();
    
    private ResourceChangeListener resourceChangeListener = new ResourceChangeListener();
    
    private Map<String, ICompilationUnit> compilationUnitMap = new HashMap<String, ICompilationUnit>();
    private Set<IFile> dirtyFiles = new HashSet<IFile>();
    
    private JXConsole console = new JXConsole();
    
    private ProjectManager() {
    }
    
    public void start() {
        resourceChangeListener.register();
    }
    
    public void stop() {
        resourceChangeListener.unregister();
    }
    
    public static ProjectManager getInstance() {
        return instance;
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
        
        Set<JavaFile> files = collectFilesRelatedTo(jproject, file);
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
    
    private Set<JavaFile> collectFilesRelatedTo(JavaProject jproject, IFile file) {
        JavaFile jfile = jproject.getFile(file.getFullPath().toString());
        Set<JavaClass> classes = new HashSet<JavaClass>();
        for (JavaClass jclass : jfile.getClasses()) {
            collectClassesRelatedTo(jfile.getProject(), jclass, classes);
        }
        
        Set<JavaFile> files = new HashSet<JavaFile>();
        for (JavaClass jclass : classes) {
            files.add(jclass.getFile());
        }
        return files;
    }
    
    private void collectClassesRelatedTo(JavaProject jproject, JavaClass jclass, Set<JavaClass> classes) {
        if (jclass != null && jproject.getClass(jclass.getQualifiedName()) != null) {
            jproject.removeClass(jclass);
            jproject.removeFile(jclass.getFile().getPath());
            for (JavaClass jc : jclass.getDescendants()) {
                classes.add(jc);
                collectClassesRelatedTo(jproject, jc, classes);
            }
            for (JavaClass jc: jclass.getAfferentClassesInProject()) {
                classes.add(jc);
                collectClassesRelatedTo(jproject, jc, classes);
            }
        }
    }
    
    public void build(IFile file) {
        JavaProject jproject = ProjectStore.getInstance().getProject(file.getProject().getFullPath().toString());
        if (jproject == null || jproject.getFiles().size() == 0) {
            IJavaProject project = JavaCore.create(file.getProject());
            if (project != null) {
                buildWhole(project);
            }
        } else {
            Set<ICompilationUnit> compilationUnits = new HashSet<ICompilationUnit>();
            IJavaProject project = JavaCore.create(file.getProject());
            jproject = ProjectStore.getInstance().getProject(project.getPath().toString());
            if (project != null) {
                compilationUnits.add(JavaCore.createCompilationUnitFrom(file));
                Set<JavaFile> files = collectFilesRelatedTo(jproject, file);
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
        JavaProject jproject = ProjectStore.getInstance().getProject(project.getPath().toString());
        if (jproject == null || jproject.getFiles().size() == 0) {
            return buildWhole(project);
        }
        
        Set<ICompilationUnit> compilationUnits = new HashSet<ICompilationUnit>();
        jproject = ProjectStore.getInstance().getProject(project.getPath().toString());
        ProjectStore.getInstance().setCurrentProject(jproject);
        for (IFile file : dirtyFiles) {
            compilationUnits.add(JavaCore.createCompilationUnitFrom(file));
            Set<JavaFile> files = collectFilesRelatedTo(jproject, file);
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
    
    private JavaProject buildWhole(IJavaProject project) {
        String name = project.getProject().getName();
        String path = project.getProject().getFullPath().toString();
        JavaProject jproject = new JavaProject(name, path);
        ProjectStore.getInstance().addProject(jproject);
        ProjectStore.getInstance().setCurrentProject(jproject);
        
        Set<ICompilationUnit> compilationUnits = collectCompilationUnits(project, jproject);
        buildJavaModel(compilationUnits, jproject);
        return jproject;
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
    
    @SuppressWarnings("deprecation")
    private ASTParser getParser() {
        ASTParser parser = ASTParser.newParser(AST.JLS8);
        Map<String, String> options = JavaCore.getOptions();
        options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
        options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_8);
        options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
        options.put(JavaCore.COMPILER_DOC_COMMENT_SUPPORT, JavaCore.ENABLED);
        parser.setCompilerOptions(options);
        return parser;
    }
    
    private boolean buildJavaModel(final Set<ICompilationUnit> cunits, JavaProject jproject) {
        try {
            final ASTParser parser = getParser();
            IWorkbenchWindow workbenchWindow = Activator.getDefault().getWorkbenchWindow();
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
    
    private Set<IProblem> getParseErrors(CompilationUnit cu) {
        Set<IProblem> errors = new HashSet<IProblem>();
        IProblem[] problems = cu.getProblems();
        if (problems.length > 0) {
            for (IProblem problem : problems) {
                if (problem.isError()) {
                    errors.add(problem);
                }
            }
        }
        return errors;
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
}
