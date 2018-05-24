/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.standalone;

import org.jtool.eclipse.javamodel.JavaProject;
import org.jtool.eclipse.util.Logger;
import org.jtool.eclipse.metrics.ProjectMetrics;
import org.jtool.eclipse.metrics.MetricsManager;
import org.jtool.eclipse.standalone.JavaModelBuilder;

/**
 * Calculates metric values.
 * @author Katsuhisa Maruyama
 */
public class JavaMetricsCalculator {
    
    private JavaModelBuilder builder;
    
    public JavaMetricsCalculator(String[] args) {
        builder = new JavaModelBuilder(args);
    }
    
    public JavaMetricsCalculator(String name, String target) {
        this(name, target, target);
    }
    
    public JavaMetricsCalculator(String name, String target, String classpath) {
        builder = new JavaModelBuilder(name, target, classpath);
    }
    
    public void run() {
        builder.build(true);
        JavaProject jproject = builder.getProject();
        
        MetricsManager manager = new MetricsManager();
        ProjectMetrics mproject = manager.calculate(jproject);
        manager.exportXML(mproject);
        Logger.getInstance().writeLog();
        builder.unbuild();
    }
    
    public static void main(String[] args) {
        JavaMetricsCalculator builder = new JavaMetricsCalculator(args);
        builder.run();
    }
}
