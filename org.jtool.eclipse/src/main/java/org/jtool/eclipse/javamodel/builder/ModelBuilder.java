/*
 *  Copyright 2018-2019
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
import org.jtool.eclipse.cfg.builder.CFGStore;
import org.jtool.eclipse.pdg.ClDG;
import org.jtool.eclipse.pdg.PDG;
import org.jtool.eclipse.pdg.SDG;
import org.jtool.eclipse.pdg.builder.PDGStore;
import org.jtool.eclipse.util.Logger;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.io.File;

/**
 * An interface for building a Java model.
 * 
 * @author Katsuhisa Maruyama
 */
public abstract class ModelBuilder {
    
    protected JavaProject currentProject;
    protected CFGStore cfgStore;
    protected PDGStore pdgStore;
    
    protected boolean analyzingBytecode;
    
    public abstract boolean isUnderPlugin();
    
    protected ModelBuilder(boolean analyzingBytecode) {
        cfgStore = new CFGStore();
        pdgStore = new PDGStore(cfgStore);
        this.analyzingBytecode = analyzingBytecode;
    }
    
    public JavaProject getCurrentProject() {
        return currentProject;
    }
    
    public CFGStore getCFGStore() {
        return cfgStore;
    }
    
    public PDGStore getPDGStore() {
        return pdgStore;
    }
    
    public void setAnalyzingBytecode(boolean analyzingBytecode) {
        this.analyzingBytecode = analyzingBytecode;
    }
    
    public boolean isAnalyzingBytecode() {
        return analyzingBytecode;
    }
    
    public void setCreatingActualNodes(boolean creatingActualNodes) {
        cfgStore.setCreatingActualNodes(creatingActualNodes);
    }
    
    public void setContainingFallThroughEdge(boolean containingFallThroughEdge) {
        pdgStore.setContainingFallThroughEdge(containingFallThroughEdge);
    }
    
    public void unbuild() {
        cfgStore.destroy();
        pdgStore.destroy();
        if (currentProject != null) {
            ProjectStore.getInstance().removeProject(currentProject.getPath());
            currentProject.clear();
        }
    }
    
    public JavaFile getUnregisteredJavaFile(String path, String code, JavaProject jproject) {
        ASTParser parser = getParser();
        
        int index = path.lastIndexOf(File.separatorChar);
        if (index != -1) {
            path = path.substring(index + 1);
        }
        parser.setUnitName(path);
        String[] encodings = new String[]{ JavaCore.getEncoding() };
        parser.setEnvironment(jproject.getClassPath(), jproject.getSourcePath(), encodings, true);
        parser.setSource(code.toCharArray());
        
        CompilationUnit cu = (CompilationUnit)parser.createAST(null);
        if (cu != null) {
            JavaFile jfile = new JavaFile(cu, path, code, JavaCore.getEncoding(), jproject);
            if (getParseErrors(cu).size() != 0) {
                System.err.println("Incomplete parse: " + path);
            }
            
            JavaASTVisitor visitor = new JavaASTVisitor(jfile);
            cu.accept(visitor);
            visitor.terminate();
            return jfile;
        }
        return null;
    }
    
    @SuppressWarnings("deprecation")
    protected ASTParser getParser() {
        ASTParser parser = ASTParser.newParser(AST.JLS8);
        Map<String, String> options = JavaCore.getOptions();
        options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
        options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_8);
        options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
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
    
    public CFG getCFG(String fqn) {
        return cfgStore.getCFG(fqn);
    }
    
    public CCFG getCCFG(JavaClass jclass) {
        return cfgStore.getCCFG(jclass);
    }
    
    public CFG getCFG(JavaMethod jmethod) {
        return cfgStore.getCFG(jmethod);
    }
    
    public CFG getCFG(JavaField jfield) {
        return cfgStore.getCFG(jfield);
    }
    
    public PDG getPDG(String fqn) {
        return pdgStore.getPDG(fqn);
    }
    
    public PDG getPDG(CFG cfg) {
        return pdgStore.getPDG(cfg);
    }
    
    public PDG getPDG(JavaMethod jmethod) {
        return pdgStore.getPDG(jmethod);
    }
    
    public PDG getPDG(JavaField jfield) {
        return pdgStore.getPDG(jfield);
    }
    
    public PDG getPDGWithinSDG(JavaMethod jmethod) {
        return pdgStore.getPDGWithinSDG(jmethod);
    }
    
    public PDG getPDGWithinSDG(JavaField jfield) {
        return pdgStore.getPDGWithinSDG(jfield);
    }
    
    public ClDG getClDG(JavaClass jclass) {
        return pdgStore.getClDG(jclass);
    }
    
    public ClDG getClDGWithinSDG(JavaClass jclass) {
        return pdgStore.getClDGWithinSDG(jclass);
    }
    
    public ClDG getClDG(JavaMethod jmethod) {
        return pdgStore.getClDG(jmethod);
    }
    
    public ClDG getClDGWithinSDG(JavaMethod jmethod) {
        return pdgStore.getClDGWithinSDG(jmethod);
    }
    
    public ClDG getClDG(JavaField jfield) {
        return pdgStore.getClDG(jfield);
    }
    
    public ClDG getClDGWithinSDG(JavaField jfield) {
        return pdgStore.getClDGWithinSDG(jfield);
    }
    
    public SDG getSDG(JavaClass jclass) {
        return pdgStore.getSDG(jclass);
    }
    
    public SDG getSDG() {
        return pdgStore.getSDG();
    }
    
    public void setLogVisible(boolean visible) {
        Logger.getInstance().setVisible(visible);
    }
    
    public abstract JavaProject update();
    
    public abstract void resisterBytecodeClasses(BytecodeClassStore bytecodeClassStore);
}
