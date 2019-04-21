/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.test;

import java.io.File;

import org.jtool.eclipse.batch.ModelBuilderBatch;
import org.junit.Test;

/**
 * Tests a class that builds a Java Model.
 * Change the constant value of TEST_PROECT_DIR according to your environment.
 * 
 * @author Katsuhisa Maruyama
 */
public class JavaModelBuilderTest {
    
    
    
    @Test
    public void testSimple() {
        String dir = new File(".").getAbsoluteFile().getParent() + "/test_target/";
        String target = dir + "Simple/";
        ModelBuilderBatch builder = new ModelBuilderBatch();
        builder.build(target, target, target);
        builder.unbuild();
    }
    
    @Test
    public void testDrawTool() {
        String dir = new File(".").getAbsoluteFile().getParent() + "/test_target/";
        String target = dir + "DrawTool/";
        ModelBuilderBatch builder = new ModelBuilderBatch();
        builder.build(target, target, target);
        builder.unbuild();
    }
    
    @Test
    public void testLambda() {
        String dir = new File(".").getAbsoluteFile().getParent() + "/test_target/";
        String target = dir + "Lambda/";
        ModelBuilderBatch builder = new ModelBuilderBatch();
        builder.build(target, target, target);
        builder.unbuild();
    }
    
    @Test
    public void testJrb() {
        String dir = "/Users/maru/Desktop/TestSamples/";
        String target = dir + "jrb-1.0.2/";
        ModelBuilderBatch builder = new ModelBuilderBatch();
        builder.build(target, target, target);
        builder.unbuild();
    }
    
    @Test
    public void testTetris() {
        String dir = "/Users/maru/Desktop/TestSamples/";
        String target = dir + "Tetris/";
        ModelBuilderBatch builder = new ModelBuilderBatch();
        builder.build(target, target, target);
        builder.unbuild();
    }
    
    @Test
    public void testCSSample() {
        String dir = "/Users/maru/Desktop/TestSamples/";
        String target = dir + "CS-Sample/";
        String classpath = dir + "CS-Sample/lib/*";
        ModelBuilderBatch builder = new ModelBuilderBatch();
        builder.build(target, target, classpath);
        builder.unbuild();
    }
    
    @Test
    public void testFindbugs() {
        String dir = "/Users/maru/Desktop/TestSamples/";
        String target = dir + "findbugs/";
        String classpath = dir + "findbugs/lib/*";
        ModelBuilderBatch builder = new ModelBuilderBatch();
        builder.build(target, target, classpath);
        builder.unbuild();
    }
    
    @Test
    public void testApacheAnt() {
        String dir = "/Users/maru/Desktop/TestSamples/";
        String target = dir + "apache-ant/";
        String classpath = dir + "apache-ant/lib/*";
        ModelBuilderBatch builder = new ModelBuilderBatch();
        builder.build(target, target, classpath);
        builder.unbuild();
    }
    
    public void notestJdk8() {
        String dir = "/Users/maru/Desktop/TestSamples/";
        String target = dir + "jdk1.8.0_131/";
        String classpath = dir + "jdk1.8.0_131/lib/*";
        ModelBuilderBatch builder = new ModelBuilderBatch();
        builder.build(target, target, classpath);
        builder.unbuild();
    }
    
    public static void main(String[] args) {
        JavaModelBuilderTest tester = new JavaModelBuilderTest();
        
        String internalDir= new File(".").getAbsoluteFile().getParent() + "/test_target/";
        System.out.println(internalDir);
        tester.testSimple();
        tester.testDrawTool();
        tester.testLambda();
        
        tester.testJrb();
        tester.testTetris();
        tester.testCSSample();
        tester.testFindbugs();
        tester.testApacheAnt();
        tester.notestJdk8();
    }
}
