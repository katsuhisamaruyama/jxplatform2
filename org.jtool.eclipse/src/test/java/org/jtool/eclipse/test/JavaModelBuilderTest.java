/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.test;

import org.jtool.eclipse.batch.ModelBuilderBatch;
import org.junit.Test;

/**
 * Tests a class that builds a Java Model.
 * Change the constant value of TEST_PROECT_DIR according to your environment.
 * 
 * @author Katsuhisa Maruyama
 */
public class JavaModelBuilderTest {
    
    private final String TEST_PROECT_DIR = "/Users/maru/Desktop/TestSamples/";
    
    @Test
    public void testSimple() {
        String target = TEST_PROECT_DIR + "Simple/";
        ModelBuilderBatch builder = new ModelBuilderBatch();
        builder.build(target, target, target);
        builder.unbuild();
    }
    
    @Test
    public void testJrb() {
        String target = TEST_PROECT_DIR + "jrb-1.0.2/";
        ModelBuilderBatch builder = new ModelBuilderBatch();
        builder.build(target, target, target);
        builder.unbuild();
    }
    
    @Test
    public void testTetris() {
        String target = TEST_PROECT_DIR + "Tetris/";
        ModelBuilderBatch builder = new ModelBuilderBatch();
        builder.build(target, target, target);
        builder.unbuild();
    }
    
    @Test
    public void testDrawTool() {
        String target = TEST_PROECT_DIR + "DrawTool/";
        ModelBuilderBatch builder = new ModelBuilderBatch();
        builder.build(target, target, target);
        builder.unbuild();
    }
    
    @Test
    public void testLambda() {
        String target = TEST_PROECT_DIR + "Lambda/";
        ModelBuilderBatch builder = new ModelBuilderBatch();
        builder.build(target, target, target);
        builder.unbuild();
    }
    
    @Test
    public void testCSSample() {
        String target = TEST_PROECT_DIR + "CS-Sample/";
        String classpath = TEST_PROECT_DIR + "CS-Sample/lib/*";
        ModelBuilderBatch builder = new ModelBuilderBatch();
        builder.build(target, target, classpath);
        builder.unbuild();
    }
    
    @Test
    public void testFindbugs() {
        String target = TEST_PROECT_DIR + "findbugs/";
        String classpath = TEST_PROECT_DIR + "findbugs/lib/*";
        ModelBuilderBatch builder = new ModelBuilderBatch();
        builder.build(target, target, classpath);
        builder.unbuild();
    }
    
    @Test
    public void testApacheAnt() {
        String target = TEST_PROECT_DIR + "apache-ant/";
        String classpath = TEST_PROECT_DIR + "apache-ant/lib/*";
        ModelBuilderBatch builder = new ModelBuilderBatch();
        builder.build(target, target, classpath);
        builder.unbuild();
    }
    
    public void notestJdk8() {
        String target = TEST_PROECT_DIR + "jdk1.8.0_131/";
        String classpath = TEST_PROECT_DIR + "jdk1.8.0_131/lib/*";
        ModelBuilderBatch builder = new ModelBuilderBatch();
        builder.build(target, target, classpath);
        builder.unbuild();
    }
    
    public static void main(String[] args) {
        JavaModelBuilderTest tester = new JavaModelBuilderTest();
        tester.testSimple();
        tester.testJrb();
        tester.testTetris();
        tester.testDrawTool();
        tester.testLambda();
        tester.testCSSample();
        tester.testFindbugs();
        tester.testApacheAnt();
        tester.notestJdk8();
    }
}
