/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */
 
package org.eclipse.test.plugin;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.jtool.eclipse.plugin.ProjectStore;

/**
 * Performs the build action for a project of interest.
 * @author Katsuhisa Maruyama
 */
public class BuildAction extends AbstractHandler {
    
    public BuildAction() {
    }
    
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
        ISelection selection = HandlerUtil.getActiveMenuSelection(event);
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection structured = (IStructuredSelection)selection;
            Object elem = structured.getFirstElement();
            IJavaProject project = null;
            if (elem instanceof IJavaProject) {
                project = (IJavaProject)elem;
            } else if (elem instanceof IProject) {
                project = (IJavaProject)JavaCore.create((IProject)elem);
            }
            if (project != null) {
                ProjectStore.getInstance().setShell(window.getShell());
                ProjectStore.getInstance().build(project, true);
            }
        }
        return null;
    }
}
