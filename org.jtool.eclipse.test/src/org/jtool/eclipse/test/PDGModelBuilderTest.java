/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.test;

import org.jtool.eclipse.model.standalone.JavaModelBuilder;
import org.jtool.eclipse.model.pdg.PDGStore;
//import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 * Tests a class that builds a Java Model.
 * @author Katsuhisa Maruyama
 */
public class PDGModelBuilderTest {
    
    @Test
    public void testSimple() {
        String target = "/Users/maru/Desktop/TestSamples/Simple/";
        JavaModelBuilder builder = new JavaModelBuilder(target, target);
        builder.build(false);
        PDGStore.getInstance().create(false);
        PDGStore.getInstance().buildPDGs(builder.getProject().getClasses());
        builder.unbuild();
    }
    
    @Test
    public void testJrb() {
        String target = "/Users/maru/Desktop/TestSamples/jrb-1.0.2/src/";
        JavaModelBuilder builder = new JavaModelBuilder(target, target);
        builder.build(false);
        PDGStore.getInstance().create(false);
        PDGStore.getInstance().buildPDGs(builder.getProject().getClasses());
        builder.unbuild();
    }
    
    @Test
    public void testTetris() {
        String target = "/Users/maru/Desktop/TestSamples/Tetris/src";
        JavaModelBuilder builder = new JavaModelBuilder(target, target);
        builder.build(false);
        PDGStore.getInstance().create(false);
        PDGStore.getInstance().buildPDGs(builder.getProject().getClasses());
        builder.unbuild();
    }
    
    @Test
    public void testDrawTool() {
        String target = "/Users/maru/Desktop/TestSamples/DrawTool/src";
        JavaModelBuilder builder = new JavaModelBuilder(target, target);
        builder.build(false);
        PDGStore.getInstance().create(false);
        PDGStore.getInstance().buildPDGs(builder.getProject().getClasses());
        builder.unbuild();
    }
    
    @Test
    public void testLambda() {
        String target = "/Users/maru/Desktop/TestSamples/Lambda/";
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
