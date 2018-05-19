/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.test;

import org.jtool.eclipse.model.cfg.CFG;
import org.jtool.eclipse.model.cfg.CFGStore;
import org.jtool.eclipse.model.java.JavaClass;
import org.jtool.eclipse.model.java.JavaField;
import org.jtool.eclipse.model.java.JavaMethod;
import org.jtool.eclipse.model.standalone.JavaModelBuilder;
//import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 * Tests a class that builds a Java Model.
 * @author Katsuhisa Maruyama
 */
public class CFGModelBuilderTest {
    
    @SuppressWarnings("unused")
    private boolean checkDetails(JavaModelBuilder builder) {
        for (JavaClass jclass : builder.getProject().getClasses()) {
            CFG cfg = CFGStore.getInstance().getCFG(jclass);
            StringBuilder buf = new StringBuilder();
            buf.append(cfg.toString());
            for (JavaMethod jmethod : jclass.getMethods()) {
                CFG mcfg = CFGStore.getInstance().getCFG(jmethod);
                buf.append(mcfg.toString());
            }
            for (JavaField jfield : jclass.getFields()) {
                CFG fcfg = CFGStore.getInstance().getCFG(jfield);
                buf.append(fcfg.toString());
            }
            System.out.println(buf.toString());
        }
        return true;
    }
    
    @Test
    public void testSimple() {
        String target = "/Users/maru/Desktop/TestSamples/Simple/";
        JavaModelBuilder builder = new JavaModelBuilder(target, target);
        builder.build(false);
        CFGStore.getInstance().create(false);
        CFGStore.getInstance().buildCFGs(builder.getProject().getClasses());
        // checkDetails(builder);
        builder.unbuild();
    }
    
    @Test
    public void testJrb() {
        String target = "/Users/maru/Desktop/TestSamples/jrb-1.0.2/src/";
        JavaModelBuilder builder = new JavaModelBuilder(target, target);
        builder.build(false);
        CFGStore.getInstance().create(false);
        CFGStore.getInstance().buildCFGs(builder.getProject().getClasses());
        
        builder.unbuild();
    }
    
    @Test
    public void testTetris() {
        String target = "/Users/maru/Desktop/TestSamples/Tetris/src";
        JavaModelBuilder builder = new JavaModelBuilder(target, target);
        builder.build(false);
        CFGStore.getInstance().create(false);
        CFGStore.getInstance().buildCFGs(builder.getProject().getClasses());
        CFGStore.getInstance().destroy();
        builder.unbuild();
    }
    
    @Test
    public void testDrawTool() {
        String target = "/Users/maru/Desktop/TestSamples/DrawTool/src";
        JavaModelBuilder builder = new JavaModelBuilder(target, target);
        builder.build(false);
        CFGStore.getInstance().create(false);
        CFGStore.getInstance().buildCFGs(builder.getProject().getClasses());
        builder.unbuild();
    }
    
    @Test
    public void testLambda() {
        String target = "/Users/maru/Desktop/TestSamples/Lambda/";
        JavaModelBuilder builder = new JavaModelBuilder(target, target);
        builder.build(false);
        CFGStore.getInstance().create(false);
        CFGStore.getInstance().buildCFGs(builder.getProject().getClasses());
        builder.unbuild();
    }
    
    public static void main(String[] args) {
        CFGModelBuilderTest tester = new CFGModelBuilderTest();
        tester.testSimple();
        tester.testJrb();
        tester.testTetris();
        tester.testDrawTool();
        tester.testLambda();
    }
}
