/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */
 
package org.jtool.eclipse.plugin;

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
                ModelBuilderPlugin modelBuilder = new ModelBuilderPlugin();
                JavaProject jproject = modelBuilder.build((IJavaProject)elem);
                
            } else if (elem instanceof IProject) {
                ModelBuilderPlugin modelBuilder = new ModelBuilderPlugin();
                JavaProject jproject = modelBuilder.build(JavaCore.create((IProject)elem));
            }
        }
        return null;
    }
}
