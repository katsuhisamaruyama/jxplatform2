/*
 *  Copyright 2019
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */
 
package org.jtool.eclipse.plugin.sample;

import org.jtool.eclipse.plugin.ModelBuilderPlugin;
import org.jtool.eclipse.javamodel.JavaProject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Performs the action that builds models of Java source files within a project.
 * 
 * @author Katsuhisa Maruyama
 */
public class BuildAction extends AbstractHandler {
    
    public BuildAction() {
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
                JavaProject jproject = modelBuilder.build((IJavaProject)elem);
            }
        }
        return null;
    }
}
