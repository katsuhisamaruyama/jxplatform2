/*
 *  Copyright 2019
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */
 
package org.jtool.eclipse.slicer.plugin.sample;

import org.jtool.eclipse.plugin.ModelBuilderPlugin;
import org.jtool.eclipse.javamodel.JavaProject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Performs the action that builds java models of elements within a project.
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
                modelBuilder.setVisible(true);
                JavaProject jproject = modelBuilder.build((IJavaProject)elem);
                
            } else if (elem instanceof IProject) {
                ModelBuilderPlugin modelBuilder = new ModelBuilderPlugin();
                modelBuilder.setVisible(true);
                JavaProject jproject = modelBuilder.build(JavaCore.create((IProject)elem));
            }
        }
        return null;
    }
}
