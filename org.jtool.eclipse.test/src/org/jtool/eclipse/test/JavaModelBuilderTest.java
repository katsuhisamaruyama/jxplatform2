/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.test;

import org.jtool.eclipse.standalone.JavaModelBuilder;
//import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 * Tests a class that builds a Java Model.
 * @author Katsuhisa Maruyama
 */
public class JavaModelBuilderTest {
    
    private final String TEST_PROECT_DIR = "/Users/maru/Desktop/TestSamples/";
    
    @Test
    public void testSimple() {
        String target = TEST_PROECT_DIR + "Simple/";
        JavaModelBuilder builder = new JavaModelBuilder(target, target);
        builder.build(true);
        builder.unbuild();
    }
    
    @Test
    public void testJrb() {
        String target = TEST_PROECT_DIR + "jrb-1.0.2/src/";
        JavaModelBuilder builder = new JavaModelBuilder(target, target);
        builder.build(true);
        builder.unbuild();
    }
    
    @Test
    public void testTetris() {
        String target = TEST_PROECT_DIR + "Tetris/src/";
        JavaModelBuilder builder = new JavaModelBuilder(target, target);
        builder.build(true);
        builder.unbuild();
    }
    
    @Test
    public void testDrawTool() {
        String target = TEST_PROECT_DIR + "DrawTool/src/";
        JavaModelBuilder builder = new JavaModelBuilder(target, target);
        builder.build(true);
        builder.unbuild();
    }
    
    @Test
    public void testLambda() {
        String target = TEST_PROECT_DIR + "Lambda/";
        JavaModelBuilder builder = new JavaModelBuilder(target, target);
        builder.build(true);
        builder.unbuild();
    }
    
    @Test
    public void testCSSample() {
        String target = TEST_PROECT_DIR + "CS-Sample/";
        String classpath = TEST_PROECT_DIR + "CS-Sample/lib/*";
        JavaModelBuilder builder = new JavaModelBuilder(target, target, classpath);
        builder.build(true);
        builder.unbuild();
    }
    
    @Test
    public void testFindbugs() {
        String target = TEST_PROECT_DIR + "findbugs/src/";
        String classpath = TEST_PROECT_DIR + "findbugs/lib/*";
        JavaModelBuilder builder = new JavaModelBuilder(target, target, classpath);
        builder.build(true);
        builder.unbuild();
    }
    
    @Test
    public void testApacheAnt() {
        String target = TEST_PROECT_DIR + "apache-ant/src/";
        String classpath = TEST_PROECT_DIR + "apache-ant/lib/*";
        JavaModelBuilder builder = new JavaModelBuilder(target, target, classpath);
        builder.build(true);
        builder.unbuild();
    }
    
    @Test
    public void testJdk8() {
        String target = "/Users/maru/Desktop/TestSamples/jdk1.8.0_131/src/";
        String classpath = "/Users/maru/Desktop/TestSamples/jdk1.8.0_131/lib/*";
        JavaModelBuilder builder = new JavaModelBuilder(target, target, classpath);
        builder.build(true);
        builder.unbuild();
    }
    
    public static void main(String[] args) {
        JavaModelBuilderTest tester = new JavaModelBuilderTest();
        //tester.testSimple();
        //tester.testJrb();
        //tester.testTetris();
        //tester.testDrawTool();
        //tester.testLambda();
        //tester.testCSSample();
        //tester.testFindbugs();
        //tester.testApacheAnt();
        tester.testJdk8();
    }
}
