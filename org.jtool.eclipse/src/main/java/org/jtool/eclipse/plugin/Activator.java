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
 * 
 * @author Katsuhisa Maruyama
 */
public class Activator extends AbstractUIPlugin implements IStartup {
    
    public static final String PLUGIN_ID = "org.jtool.eclipse";
    
    private static Activator plugin;
    
    @Override
    public void earlyStartup() {
    }
    
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
    }
    
    @Override
    public void stop(BundleContext context) throws Exception {
        super.stop(context);
    }
    
    public static Activator getPlugin() {
        return plugin;
    }
    
    public IWorkbenchWindow getWorkbenchWindow() {
        return PlatformUI.getWorkbench().getActiveWorkbenchWindow();
    }
    
    public IWorkbenchPage getWorkbenchPage() {
        IWorkbenchWindow window = getWorkbenchWindow();
        return window.getActivePage();
    }
}
