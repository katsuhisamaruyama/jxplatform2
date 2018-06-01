/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.test;

import org.jtool.eclipse.cfg.CFG;
import org.jtool.eclipse.cfg.CFGStore;
import org.jtool.eclipse.javamodel.JavaClass;
import org.jtool.eclipse.javamodel.JavaField;
import org.jtool.eclipse.javamodel.JavaMethod;
import org.jtool.eclipse.standalone.JavaModelBuilder;
import org.junit.Test;

/**
 * Tests a class that builds a CFG.
 * Please the constant value of TEST_PROECT_DIR will be changed according to your environment.
 * @author Katsuhisa Maruyama
 */
public class CFGModelBuilderTest {
    
    private final String TEST_PROECT_DIR = "/Users/maru/Desktop/TestSamples/";
    
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
        String target = TEST_PROECT_DIR + "Simple/";
        JavaModelBuilder builder = new JavaModelBuilder(target, target);
        builder.build(false);
        CFGStore.getInstance().create(false);
        CFGStore.getInstance().buildCFGs(builder.getProject().getClasses());
        // checkDetails(builder);
        builder.unbuild();
    }
    
    @Test
    public void testJrb() {
        String target = TEST_PROECT_DIR + "jrb-1.0.2/src/";
        JavaModelBuilder builder = new JavaModelBuilder(target, target);
        builder.build(false);
        CFGStore.getInstance().create(false);
        CFGStore.getInstance().buildCFGs(builder.getProject().getClasses());
        
        builder.unbuild();
    }
    
    @Test
    public void testTetris() {
        String target = TEST_PROECT_DIR + "Tetris/src/";
        JavaModelBuilder builder = new JavaModelBuilder(target, target);
        builder.build(false);
        CFGStore.getInstance().create(false);
        CFGStore.getInstance().buildCFGs(builder.getProject().getClasses());
        CFGStore.getInstance().destroy();
        builder.unbuild();
    }
    
    @Test
    public void testDrawTool() {
        String target = TEST_PROECT_DIR + "DrawTool/src/";
        JavaModelBuilder builder = new JavaModelBuilder(target, target);
        builder.build(false);
        CFGStore.getInstance().create(false);
        CFGStore.getInstance().buildCFGs(builder.getProject().getClasses());
        builder.unbuild();
    }
    
    @Test
    public void testLambda() {
        String target = TEST_PROECT_DIR + "Lambda/";
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
