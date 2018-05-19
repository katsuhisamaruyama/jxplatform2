/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.plugin;

import org.jtool.eclipse.model.java.builder.JavaASTVisitor;
import org.jtool.eclipse.model.java.JavaFile;
import org.jtool.eclipse.model.java.JavaPackage;
import org.jtool.eclipse.model.java.JavaProject;
import org.jtool.eclipse.model.java.JavaClass;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * An object representing a Eclipse's project.
 * @author Katsuhisa Maruyama
 */
public class ProjectStore {
    
    private static ProjectStore instance = new ProjectStore();
    
    protected static Map<String, JavaProject> projectStore = new HashMap<String, JavaProject>();
    
    private Shell shell;
    
    private ProjectStore() {
    }
    
    public static ProjectStore getInstance() {
        return instance;
    }
    
    public void setShell(Shell shell) {
        this.shell = shell;
    }
    
    public void build(IJavaProject project, boolean resolveBinding) {
        String name = project.getProject().getName();
        String path = project.getProject().getFullPath().toString();
        JavaProject jproject = projectStore.get(path);
        if (jproject == null) {
            jproject = new JavaProject(name, path);
            projectStore.put(path, jproject);
        }
        
        Set<ICompilationUnit> cunits = collectAllCompilationUnits(project);
        removeUnchangedCompilationUnits(cunits, jproject);
        Set<ICompilationUnit> pcunits = collectCompilationUnitsToBeParsed(cunits, jproject);
        boolean result = buildJavaModel(pcunits, jproject);
        if (!result) {
            return;
        }
        
        if (resolveBinding) {
            jproject.collectBindingInfo();
        }
    }
    
    public void refresh(IJavaProject project) {
        String path = project.getProject().getFullPath().toString();
        JavaProject jproject = projectStore.get(path);
        if (jproject != null) {
            jproject.clear();
        }
    }
    
    private Set<ICompilationUnit> collectAllCompilationUnits(IJavaProject project) {
        Set<ICompilationUnit> newUnits = new HashSet<ICompilationUnit>();
        if (project.getElementName().equals("RemoteSystemsTempFiles")) {
            return newUnits;
        }
        
        try {
            IPackageFragment[] packages = project.getPackageFragments();
            for (int i = 0; i < packages.length; i++) {
                ICompilationUnit[] units = packages[i].getCompilationUnits();
                for (int j = 0; j < units.length; j++) {
                    IResource res = units[j].getResource();
                    if (res.getType() == IResource.FILE) {
                        
                        String pathname = units[j].getPath().toString();
                        if (pathname.endsWith(".java")) { 
                            newUnits.add(units[j]);
                        }
                    }
                }
            }
        } catch (JavaModelException e) {
            printError("JavaModelException occurred: " + e.getMessage());
        }
        return newUnits;
    }
    
    private void removeUnchangedCompilationUnits(Set<ICompilationUnit> cunits, JavaProject jproject) {
        for (ICompilationUnit icu : cunits) {
            try {
                if (icu.hasUnsavedChanges()) {
                    removeFile(icu.getPath().toString(), jproject);
                }
            } catch (JavaModelException e) {
                printError("JavaModelException occurred: " + e.getMessage());
            }
        }
    }
    
    private void printError(String mesg) {
        MessageDialog.openError(shell, "Error", mesg);
    }
    
    private Set<ICompilationUnit> collectCompilationUnitsToBeParsed(Set<ICompilationUnit> cunits, JavaProject jproject) {
        Set<ICompilationUnit> newUnits = new HashSet<ICompilationUnit>();
        for (ICompilationUnit icu : cunits) {
            String path = icu.getPath().toString();
            if (jproject.getFile(path) == null) {
                newUnits.add(icu);
            }
        }
        return newUnits;
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
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    int size = cunits.size();
                    monitor.beginTask("Parsing files... ", size);
                    int count = 1;
                    for (ICompilationUnit icu : cunits) {
                        monitor.subTask(count + "/" + size + " - " + icu.getPath().toString());
                        try {
                            buildJavaModel(parser, icu, jproject);
                        } catch (NullPointerException e) {
                            JtoolConsole.println("* Fatal error occurred. Skip the paser of " + icu.getPath().toString());
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
            JtoolConsole.println("* InvocationTargetException occurred because " + cause);
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
                List<IProblem> errors = getParseErrors(cu);
                if (errors.size() == 0) {
                    // JtoolConsole.println("Complete parse: " + icu.getPath().toString());
                } else {
                    jfile.setParseErrors(errors);
                    // JtoolConsole.println("Incomplete parse: " + icu.getPath().toString());
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
    
    private List<IProblem> getParseErrors(CompilationUnit cu) {
        List<IProblem> errors = new ArrayList<IProblem>();
        IProblem[] problems = cu.getProblems();
        if (problems.length != 0) {
            for (IProblem problem : problems) {
                if (problem.isError()) {
                    errors.add(problem);
                }
            }
        }
        return errors;
    }
    
    public void dispose() {
        if (projectStore != null) {
            projectStore.clear();
            projectStore = null;
        }
    }
    
    public JavaProject getProject(String path) {
        return projectStore.get(path);
    }
    
    protected void removeFile(String path, JavaProject jproject) {
        if (jproject != null) {
            for (JavaClass jclass : jproject.getClasses()) {
                if (path.equals(jclass.getFile().getPath())) {
                    removeClassesRelatedTo(jclass, jproject);
                }
            }
            for (JavaPackage jpackage : jproject.getPackages()) {
                if (jpackage.getClasses().size() == 0) {
                    jproject.removePackage(jpackage);
                }
            }
        }
    }
    
    private void removeClassesRelatedTo(JavaClass jclass, JavaProject jproject) {
        if (jclass != null && jproject.getClass(jclass.getQualifiedName()) != null) {
            jproject.removeClass(jclass);
            jproject.removeFile(jclass.getFile().getPath());
            for (JavaClass jc : jclass.getDescendants()) {
                removeClassesRelatedTo(jc, jproject);
            }
                
            for (JavaClass jc: jclass.getAfferentClassesInProject()) {
                removeClassesRelatedTo(jc, jproject);
            }
        }
    }
}
