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
import org.eclipse.ui.handlers.HandlerUtil;
import org.jtool.eclipse.ProjectStore;

/**
 * Performs the build action for a project of interest.
 * @author Katsuhisa Maruyama
 */
public class BuildAction extends AbstractHandler {
    
    public BuildAction() {
    }
    
    public Object execute(ExecutionEvent event) throws ExecutionException {
        ISelection selection = HandlerUtil.getActiveMenuSelection(event);
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection structured = (IStructuredSelection)selection;
            Object elem = structured.getFirstElement();
            if (elem instanceof IJavaProject) {
                ProjectStore.getInstance().build((IJavaProject)elem);
            } else if (elem instanceof IProject) {
                ProjectStore.getInstance().build((IJavaProject)JavaCore.create((IProject)elem));
            }
        }
        return null;
    }
}
