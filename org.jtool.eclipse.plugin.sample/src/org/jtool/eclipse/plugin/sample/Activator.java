/*
 *  Copyright 2019
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.plugin.sample;

import org.jtool.eclipse.plugin.ModelBuilderPlugin;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator that controls the plug-in life cycle.
 * 
 * @author Katsuhisa Maruyama
 */
public class Activator extends AbstractUIPlugin {
    
    public static final String PLUGIN_ID = "org.jtool.eclipse";
    
    private static Activator plugin;
    
    private ModelBuilderPlugin modelBuilder;
    
    public Activator() {
    }
    
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
        modelBuilder = new ModelBuilderPlugin(false);
    }
    
    @Override
    public void stop(BundleContext context) throws Exception {
        modelBuilder.unbuild();
        plugin = null;
        super.stop(context);
    }
    
    public static Activator getPlugin() {
        return plugin;
    }
    
    public ModelBuilderPlugin getModelBuilder() {
        return modelBuilder;
    }
}
