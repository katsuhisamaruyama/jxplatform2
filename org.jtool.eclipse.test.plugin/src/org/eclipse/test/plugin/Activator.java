/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.eclipse.test.plugin;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator that controls the plug-in life cycle.
 */
public class Activator extends AbstractUIPlugin {
    
    public static final String PLUGIN_ID = "org.eclipse.test.plugin";
    
    private static Activator plugin;
    
    public Activator() {
    }
    
    
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
    }
    
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }
    
    public static Activator getDefault() {
        return plugin;
    }
}