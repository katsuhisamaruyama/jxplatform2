/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.plugin;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IExecutionListener;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * A listener that will be notified when a command executed.
 * @author Katsuhisa Maruyama
 */
class CommandListener implements IExecutionListener {
    
    CommandListener() {
    }
    
    void register() {
        ICommandService cs = (ICommandService)PlatformUI.getWorkbench().getService(ICommandService.class);
        if (cs != null) {
            cs.addExecutionListener(this);
        }
    }
    
    void unregister() {
        ICommandService cs = (ICommandService)PlatformUI.getWorkbench().getService(ICommandService.class);
        if (cs != null) {
            cs.removeExecutionListener(this);
        }
    }
    
    @Override
    public void preExecute(String commandId, ExecutionEvent event) {
    }
    
    @Override
    public void postExecuteSuccess(String commandId, Object returnValue) {
        if (IWorkbenchCommandConstants.FILE_REFRESH.equals(commandId)) {
            IWorkbenchPage page = Activator.getDefault().getWorkbenchPage();
            ISelection selection = page.getSelection();
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
                    ProjectStore.getInstance().refresh(project);;
                }
            }
        }
    }
    
    @Override
    public void postExecuteFailure(String commandId, ExecutionException exception) {
    }
    
    @Override
    public void notHandled(String commandId, NotHandledException exception) {
    }
}
