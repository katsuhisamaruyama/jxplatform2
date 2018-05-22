/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.test;

import org.jtool.eclipse.pdg.PDGStore;
import org.jtool.eclipse.standalone.JavaModelBuilder;
//import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 * Tests a class that builds a Java Model.
 * @author Katsuhisa Maruyama
 */
public class PDGModelBuilderTest {
    
    private final String TEST_PROECT_DIR = "/Users/maru/Desktop/TestSamples/";
    
    @Test
    public void testSimple() {
        String target = TEST_PROECT_DIR + "Simple/";
        JavaModelBuilder builder = new JavaModelBuilder(target, target);
        builder.build(false);
        PDGStore.getInstance().create(false);
        PDGStore.getInstance().buildPDGs(builder.getProject().getClasses());
        builder.unbuild();
    }
    
    @Test
    public void testJrb() {
        String target = TEST_PROECT_DIR +  "jrb-1.0.2/src/";
        JavaModelBuilder builder = new JavaModelBuilder(target, target);
        builder.build(false);
        PDGStore.getInstance().create(false);
        PDGStore.getInstance().buildPDGs(builder.getProject().getClasses());
        builder.unbuild();
    }
    
    @Test
    public void testTetris() {
        String target = TEST_PROECT_DIR + "Tetris/src/";
        JavaModelBuilder builder = new JavaModelBuilder(target, target);
        builder.build(false);
        PDGStore.getInstance().create(false);
        PDGStore.getInstance().buildPDGs(builder.getProject().getClasses());
        builder.unbuild();
    }
    
    @Test
    public void testDrawTool() {
        String target = TEST_PROECT_DIR +  "DrawTool/src/";
        JavaModelBuilder builder = new JavaModelBuilder(target, target);
        builder.build(false);
        PDGStore.getInstance().create(false);
        PDGStore.getInstance().buildPDGs(builder.getProject().getClasses());
        builder.unbuild();
    }
    
    @Test
    public void testLambda() {
        String target = TEST_PROECT_DIR +  "Lambda/";
        JavaModelBuilder builder = new JavaModelBuilder(target, target);
        builder.build(false);
        PDGStore.getInstance().create(false);
        PDGStore.getInstance().buildPDGs(builder.getProject().getClasses());
        builder.unbuild();
    }
    
    public static void main(String[] args) {
        PDGModelBuilderTest tester = new PDGModelBuilderTest();
        tester.testSimple();
        tester.testJrb();
        tester.testTetris();
        tester.testDrawTool();
        tester.testLambda();
    }
}
