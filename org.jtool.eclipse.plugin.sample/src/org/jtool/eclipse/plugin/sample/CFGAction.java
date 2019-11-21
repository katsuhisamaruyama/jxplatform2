/*
 *  Copyright 2019
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */
 
package org.jtool.eclipse.plugin.sample;

import org.jtool.eclipse.plugin.ModelBuilderPlugin;
import org.jtool.eclipse.plugin.JXConsole;
import org.jtool.eclipse.javamodel.JavaProject;
import org.jtool.eclipse.javamodel.JavaClass;
import org.jtool.eclipse.cfg.CCFG;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.ui.handlers.HandlerUtil;
import java.util.List;

/**
 * Performs the action that builds CFGs of Java source files within a project.
 * 
 * @author Katsuhisa Maruyama
 */
public class CFGAction extends AbstractHandler {
    
    JXConsole console;
    
    public CFGAction() {
    }
    
    @SuppressWarnings("unused")
    public Object execute(ExecutionEvent event) throws ExecutionException {
        ISelection selection = HandlerUtil.getActiveMenuSelection(event);
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection structured = (IStructuredSelection)selection;
            Object elem = structured.getFirstElement();
            if (elem instanceof IJavaProject) {
                ModelBuilderPlugin modelBuilder = Activator.getPlugin().getModelBuilder();
                modelBuilder.setLogVisible(true);
                console = modelBuilder.getConsole();
                
                JavaProject jproject = modelBuilder.build((IJavaProject)elem);
                CCFG[] ccfgs = buildCFGsForTest(modelBuilder, jproject.getClasses());
            }
        }
        return null;
    }
    
    private CCFG[] buildCFGsForTest(ModelBuilderPlugin builder, List<JavaClass> classes) {
        int size = classes.size();
        CCFG[] ccfgs = new CCFG[size];
        int count = 1;
        console.println();
        console.println("** Building CFGs of " + size + " classes ");
        for (JavaClass jclass : classes) {
            console.print("(" + count + "/" + size + ")");
            ccfgs[count - 1] = builder.getCCFG(jclass);
            console.print(" - " + jclass.getQualifiedName() + " - CCFG\n");
            count++;
        }
        return ccfgs;
    }
}
