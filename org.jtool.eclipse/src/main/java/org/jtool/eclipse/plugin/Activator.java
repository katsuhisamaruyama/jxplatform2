/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.plugin;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbenchPage;
import org.osgi.framework.BundleContext;

/**
 * The activator that controls the plug-in life cycle.
 */
public class Activator extends AbstractUIPlugin implements IStartup {
    
    public static final String PLUGIN_ID = "org.jtool.eclipse";
    
    private static Activator plugin;
    
    public Activator() {
    }
    
    public static Activator getDefault() {
        return plugin;
    }
    
    @Override
    public void earlyStartup() {
    }
    
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
        ModelBuilderPlugin.getInstance().start();
    }
    
    @Override
    public void stop(BundleContext context) throws Exception {
        ModelBuilderPlugin.getInstance().stop();
        plugin = null;
        super.stop(context);
    }
    
    public IWorkbenchWindow getWorkbenchWindow() {
        return PlatformUI.getWorkbench().getActiveWorkbenchWindow();
    }
    
    public IWorkbenchPage getWorkbenchPage() {
        IWorkbenchWindow window = getWorkbenchWindow();
        return window.getActivePage();
    }
}
