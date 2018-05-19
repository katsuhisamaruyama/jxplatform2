/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.test;

import org.jtool.eclipse.model.standalone.JavaModelBuilder;
//import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 * Tests a class that builds a Java Model.
 * @author Katsuhisa Maruyama
 */
public class JavaModelBuilderTest {
    
    @Test
    public void testSimple() {
        String target = "/Users/maru/Desktop/TestSamples/Simple/";
        JavaModelBuilder builder = new JavaModelBuilder(target, target);
        builder.build(true);
        builder.unbuild();
    }
    
    @Test
    public void testJrb() {
        String target = "/Users/maru/Desktop/TestSamples/jrb-1.0.2/src/";
        JavaModelBuilder builder = new JavaModelBuilder(target, target);
        builder.build(true);
        builder.unbuild();
    }
    
    @Test
    public void testTetris() {
        String target = "/Users/maru/Desktop/TestSamples/Tetris/src";
        JavaModelBuilder builder = new JavaModelBuilder(target, target);
        builder.build(true);
        builder.unbuild();
    }
    
    @Test
    public void testDrawTool() {
        String target = "/Users/maru/Desktop/TestSamples/DrawTool/src";
        JavaModelBuilder builder = new JavaModelBuilder(target, target);
        builder.build(true);
        builder.unbuild();
    }
    
    @Test
    public void testLambda() {
        String target = "/Users/maru/Desktop/TestSamples/Lambda/";
        JavaModelBuilder builder = new JavaModelBuilder(target, target);
        builder.build(true);
        builder.unbuild();
    }
    
    @Test
    public void testCSSample() {
        String target = "/Users/maru/Desktop/TestSamples/CS-Sample/";
        String classpathdir = "/Users/maru/Desktop/TestSamples/lib";
        JavaModelBuilder builder = new JavaModelBuilder(target, target, target, classpathdir);
        builder.build(true);
        builder.unbuild();
    }
    
    @Test
    public void testFindbugs() {
        String target = "/Users/maru/Desktop/TestSamples/findbugs/src/";
        String classpathdir = "/Users/maru/Desktop/TestSamples/findbugs/lib";
        JavaModelBuilder builder = new JavaModelBuilder(target, target, target, classpathdir);
        builder.build(true);
        builder.unbuild();
    }
    
    @Test
    public void testApacheAnt() {
        String target = "/Users/maru/Desktop/TestSamples/apache-ant/src/";
        String classpathdir = "/Users/maru/Desktop/TestSamples/apache-ant/lib";
        JavaModelBuilder builder = new JavaModelBuilder(target, target, target, classpathdir);
        builder.build(true);
        builder.unbuild();
    }
    
    @Test
    public void testJdk8() {
        String target = "/Users/maru/Desktop/TestSamples/jdk1.8.0_131/src/";
        String classpathdir = "/Users/maru/Desktop/TestSamples/jdk1.8.0_131/lib";
        JavaModelBuilder builder = new JavaModelBuilder(target, target, target, classpathdir);
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
