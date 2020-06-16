/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.javamodel.builder;

import org.jtool.eclipse.javamodel.JavaClass;
import org.jtool.eclipse.javamodel.JavaField;
import org.jtool.eclipse.javamodel.JavaFile;
import org.jtool.eclipse.javamodel.JavaMethod;
import org.jtool.eclipse.javamodel.JavaProject;
import org.jtool.eclipse.cfg.CCFG;
import org.jtool.eclipse.cfg.CFG;
import org.jtool.eclipse.cfg.CallGraph;
import org.jtool.eclipse.cfg.builder.CallGraphBuilder;
import org.jtool.eclipse.pdg.ClDG;
import org.jtool.eclipse.pdg.PDG;
import org.jtool.eclipse.pdg.SDG;
import org.jtool.eclipse.util.Logger;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

/**
 * An interface for building a Java model.
 * 
 * @author Katsuhisa Maruyama
 */
public abstract class ModelBuilder {
    
    protected boolean analyzingBytecode;
    protected BytecodeClassStore bytecodeClassStore;
    
    protected ModelBuilder(boolean analyzingBytecode) {
        this.analyzingBytecode = analyzingBytecode;
        bytecodeClassStore = new BytecodeClassStore();
    }
    
    public abstract boolean isUnderPlugin();
    
    public abstract void update(JavaProject jproject);
    
    public abstract void resisterBytecodeClasses(JavaProject jproject);
    
    public BytecodeClassStore getBytecodeClassStore() {
        return bytecodeClassStore;
    }
    
    public void setAnalyzingBytecode(boolean analyzingBytecode) {
        this.analyzingBytecode = analyzingBytecode;
    }
    
    public boolean isAnalyzingBytecode() {
        return analyzingBytecode;
    }
    
    public void unbuild() {
        ProjectStore.getInstance().clear();
    }
    
    public JavaFile copyJavaFile(JavaFile jfile) {
        return getUnregisteredJavaFile(jfile.getPath(), jfile.getCode(), jfile.getProject(), jfile.getCharset());
    }
    
    public JavaFile getUnregisteredJavaFile(String filepath, String code, JavaProject jproject) {
        return getUnregisteredJavaFile(filepath, code, jproject, JavaCore.getEncoding());
    }
    
    public JavaFile getUnregisteredJavaFile(String filepath, String code, JavaProject jproject, String charset) {
        ASTParser parser = getParser();
        
        String[] sourcepaths = jproject.getSourcePath();
        parser.setUnitName(filepath);
        parser.setEnvironment(jproject.getClassPath(), sourcepaths, null, true);
        parser.setSource(code.toCharArray());
        
        CompilationUnit cu = (CompilationUnit)parser.createAST(null);
        if (cu != null) {
            JavaFile jfile = new JavaFile(cu, filepath, code, charset, jproject);
            if (getParseErrors(cu).size() != 0) {
                System.err.println("Incomplete parse: " + filepath);
            }
            
            JavaASTVisitor visitor = new JavaASTVisitor(jfile);
            cu.accept(visitor);
            visitor.terminate();
            return jfile;
        }
        return null;
    }
    
    protected ASTParser getParser() {
        ASTParser parser = ASTParser.newParser(AST.JLS13);
        Map<String, String> options = JavaCore.getOptions();
        options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_13);
        options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_13);
        options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_13);
        options.put(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
        options.put(JavaCore.COMPILER_DOC_COMMENT_SUPPORT, JavaCore.ENABLED);
        parser.setCompilerOptions(options);
        
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setResolveBindings(true);
        parser.setStatementsRecovery(true);
        parser.setBindingsRecovery(true);
        return parser;
    }
    
    protected Set<IProblem> getParseErrors(CompilationUnit cu) {
        Set<IProblem> errors = new HashSet<IProblem>();
        IProblem[] problems = cu.getProblems();
        if (problems.length > 0) {
            for (IProblem problem : problems) {
                if (problem.isError()) {
                    System.err.println("Error: " + problem.getMessage());
                    errors.add(problem);
                }
            }
        }
        return errors;
    }
    
    public Set<JavaClass> getAllClassesForward(JavaClass jclass) {
        Set<JavaClass> classes = new HashSet<JavaClass>();
        collectAllClassesForward(jclass, classes);
        return classes;
    }
    
    private void collectAllClassesForward(JavaClass jclass, Set<JavaClass> classes) {
        if (classes.contains(jclass)) {
            return;
        }
        classes.add(jclass);
        
        for (JavaClass jc : jclass.getEfferentClassesInProject()) {
            collectAllClassesForward(jc, classes);
        }
    }
    
    public Set<JavaClass> getAllClassesBackward(JavaClass jclass) {
        Set<JavaClass> classes = new HashSet<JavaClass>();
        collectAllClassesBackward(jclass, classes);
        return classes;
    }
    
    private void collectAllClassesBackward(JavaClass jclass, Set<JavaClass> classes) {
        if (classes.contains(jclass)) {
            return;
        }
        classes.add(jclass);
        
        for (JavaClass jc : jclass.getAfferentClassesInProject()) {
            collectAllClassesBackward(jc, classes);
        }
    }
    
    public Set<JavaMethod> getAllMethodsForward(JavaMethod jmethod) {
        Set<JavaMethod> methods = new HashSet<JavaMethod>();
        collectAllMethodsForward(jmethod, methods);
        return methods;
    }
    
    private void collectAllMethodsForward(JavaMethod jmethod, Set<JavaMethod> methods) {
        if (methods.contains(jmethod)) {
            return;
        }
        methods.add(jmethod);
        
        for (JavaMethod jm : jmethod.getCalledMethods()) {
            collectAllMethodsForward(jm, methods);
        }
    }
    
    public Set<JavaMethod> getAllMethodsBackward(JavaMethod jmethod) {
        Set<JavaMethod> methods = new HashSet<JavaMethod>();
        collectAllMethodsBackward(jmethod, methods);
        return methods;
    }
    
    private void collectAllMethodsBackward(JavaMethod jmethod, Set<JavaMethod> methods) {
        if (methods.contains(jmethod)) {
            return;
        }
        methods.add(jmethod);
        
        for (JavaMethod jm : jmethod.getCallingMethods()) {
            collectAllMethodsBackward(jm, methods);
        }
    }
    
    public CFG findCFG(JavaProject jproject, String fqn) {
        return jproject.getCFGStore().findCFG(fqn);
    }
    
    public CCFG findCCFG(JavaProject jproject, String fqn) {
        return jproject.getCFGStore().findCCFG(fqn);
    }
    
    public CFG getCFG(JavaMethod jmethod, boolean force) {
        return jmethod.getJavaProject().getCFGStore().getCFG(jmethod, force);
    }
    
    public CFG getCFG(JavaMethod jmethod) {
        return jmethod.getJavaProject().getCFGStore().getCFG(jmethod, false);
    }
    
    public CFG getCFG(JavaField jfield, boolean force) {
        return jfield.getJavaProject().getCFGStore().getCFG(jfield, force);
    }
    
    public CFG getCFG(JavaField jfield) {
        return jfield.getJavaProject().getCFGStore().getCFG(jfield, false);
    }
    
    public CCFG getCCFG(JavaClass jclass, boolean force) {
        return jclass.getJavaProject().getCFGStore().getCCFG(jclass, force);
    }
    
    public CCFG getCCFG(JavaClass jclass) {
        return jclass.getJavaProject().getCFGStore().getCCFG(jclass, false);
    }
    
    public CallGraph getCallGraph(JavaProject jproject) {
        return CallGraphBuilder.getCallGraph(jproject);
    }
    
    public CallGraph getCallGraph(JavaClass jclass) {
        return CallGraphBuilder.getCallGraph(jclass);
    }
    
    public CallGraph getCallGraph(JavaMethod jmethod) {
        return CallGraphBuilder.getCallGraph(jmethod);
    }
    
    public PDG findPDG(JavaProject jproject, String fqn) {
        return jproject.getPDGStore().findPDG(fqn);
    }
    
    public ClDG findClDG(JavaProject jproject, String fqn) {
        return jproject.getPDGStore().findClDG(fqn);
    }
    
    public SDG findSDG(JavaProject jproject) {
        return jproject.getPDGStore().findSDG();
    }
    
    public PDG getPDG(JavaProject jproject, CFG cfg, boolean force) {
        return jproject.getPDGStore().getPDG(cfg, force);
    }
    
    public PDG getPDG(JavaProject jproject, CFG cfg) {
        return jproject.getPDGStore().getPDG(cfg, false);
    }
    
    public PDG getPDG(JavaMethod jmethod, boolean force) {
        return jmethod.getJavaProject().getPDGStore().getPDG(jmethod, force);
    }
    
    public PDG getPDG(JavaMethod jmethod) {
        return jmethod.getJavaProject().getPDGStore().getPDG(jmethod, false);
    }
    
    public PDG getPDG(JavaField jfield, boolean force) {
        return jfield.getJavaProject().getPDGStore().getPDG(jfield, force);
    }
    
    public PDG getPDG(JavaField jfield) {
        return jfield.getJavaProject().getPDGStore().getPDG(jfield, false);
    }
    
    public PDG getPDGWithinSDG(JavaMethod jmethod, boolean force) {
        return jmethod.getJavaProject().getPDGStore().getPDGWithinSDG(jmethod, force);
    }
    
    public PDG getPDGWithinSDG(JavaMethod jmethod) {
        return jmethod.getJavaProject().getPDGStore().getPDGWithinSDG(jmethod, false);
    }
    
    public PDG getPDGWithinSDG(JavaField jfield, boolean force) {
        return jfield.getJavaProject().getPDGStore().getPDGWithinSDG(jfield, force);
    }
    
    public PDG getPDGWithinSDG(JavaField jfield) {
        return jfield.getJavaProject().getPDGStore().getPDGWithinSDG(jfield, false);
    }
    
    public ClDG getClDG(JavaProject jproject, CCFG ccfg, boolean force) {
        return jproject.getPDGStore().getClDG(ccfg, force);
    }
    
    public ClDG getClDG(JavaProject jproject, CCFG ccfg) {
        return jproject.getPDGStore().getClDG(ccfg, false);
    }
    
    public ClDG getClDG(JavaClass jclass, boolean force) {
        return jclass.getJavaProject().getPDGStore().getClDG(jclass, force);
    }
    
    public ClDG getClDG(JavaClass jclass) {
        return jclass.getJavaProject().getPDGStore().getClDG(jclass, false);
    }
    
    public ClDG getClDGWithinSDG(JavaClass jclass, boolean force) {
        return jclass.getJavaProject().getPDGStore().getClDGWithinSDG(jclass, force);
    }
    
    public ClDG getClDGWithinSDG(JavaClass jclass) {
        return jclass.getJavaProject().getPDGStore().getClDGWithinSDG(jclass, false);
    }
    
    public SDG getSDG(JavaClass jclass, boolean force) {
        return jclass.getJavaProject().getPDGStore().getSDG(jclass, force);
    }
    
    public SDG getSDG(JavaClass jclass) {
        return jclass.getJavaProject().getPDGStore().getSDG(jclass, false);
    }
    
    public SDG getSDG(Set<JavaClass> classes, boolean force) {
        if (classes.size() > 0) {
            JavaClass jclass = classes.iterator().next();
            return jclass.getJavaProject().getPDGStore().getSDG(classes, force);
        }
        return new SDG();
    }
    
    public SDG getSDG(Set<JavaClass> classes) {
        if (classes.size() > 0) {
            JavaClass jclass = classes.iterator().next();
            return jclass.getJavaProject().getPDGStore().getSDG(classes, false);
        }
        return new SDG();
    }
    
    public SDG getSDG(JavaProject jproject, boolean force) {
        return jproject.getPDGStore().getSDG(force);
    }
    
    public SDG getSDG(JavaProject jproject) {
        return jproject.getPDGStore().getSDG(false);
    }
    
    public SDG getSDGForClasses(Set<JavaClass> classes, boolean force) {
        if (classes.size() > 0) {
            JavaClass jclass = classes.iterator().next();
            return jclass.getJavaProject().getPDGStore().getSDGForClasses(classes, force);
        }
        return new SDG();
    }
    
    public SDG getSDGForClasses(Set<JavaClass> classes) {
        if (classes.size() > 0) {
            JavaClass jclass = classes.iterator().next();
            return jclass.getJavaProject().getPDGStore().getSDGForClasses(classes, false);
        }
        return new SDG();
    }
    
    public void setLogVisible(boolean visible) {
        Logger.getInstance().setVisible(visible);
    }
    
    public boolean isLogVisible() {
        return Logger.getInstance().isVisible();
    }
}
