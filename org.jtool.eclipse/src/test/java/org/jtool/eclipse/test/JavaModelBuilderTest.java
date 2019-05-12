/*
 *  Copyright 2018-2019
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.test;

import org.jtool.eclipse.batch.ModelBuilderBatch;
import org.jtool.eclipse.javamodel.JavaProject;
import org.jtool.eclipse.javamodel.JavaClass;
import org.jtool.eclipse.javamodel.JavaMethod;
import org.jtool.eclipse.javamodel.JavaField;
import java.io.File;
import org.junit.Test;
import org.junit.Ignore;

/**
 * Tests a class that builds a Java Model.
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
        String dir = new File(".").getAbsoluteFile().getParent() + "/test_target/";
        String target = dir + "jrb-1.0.2/";
        ModelBuilderBatch builder = new ModelBuilderBatch();
        builder.build(target, target, target);
        builder.unbuild();
    }
    
    @Ignore
    public void testTetris() {
        String dir = "/Users/maru/Desktop/TestSamples/";
        String target = dir + "Tetris/";
        ModelBuilderBatch builder = new ModelBuilderBatch();
        builder.build(target, target, target);
        builder.unbuild();
    }
    
    @Ignore
    public void testCSSample() {
        String dir = "/Users/maru/Desktop/TestSamples/";
        String target = dir + "CS-Sample/";
        String classpath = dir + "CS-Sample/lib/*";
        ModelBuilderBatch builder = new ModelBuilderBatch();
        builder.build(target, target, classpath);
        builder.unbuild();
    }
    
    @Ignore
    public void testFindbugs() {
        String dir = "/Users/maru/Desktop/TestSamples/";
        String target = dir + "findbugs/";
        String classpath = dir + "findbugs/lib/*";
        ModelBuilderBatch builder = new ModelBuilderBatch();
        builder.build(target, target, classpath);
        builder.unbuild();
    }
    
    @Ignore
    public void testApacheAnt() {
        String dir = "/Users/maru/Desktop/TestSamples/";
        String target = dir + "apache-ant/";
        String classpath = dir + "apache-ant/lib/*";
        ModelBuilderBatch builder = new ModelBuilderBatch();
        builder.build(target, target, classpath);
        builder.unbuild();
    }
    
    @Ignore
    public void testJdk8() {
        String dir = "/Users/maru/Desktop/TestSamples/";
        String target = dir + "jdk1.8.0_131/";
        String classpath = dir + "jdk1.8.0_131/lib/*";
        ModelBuilderBatch builder = new ModelBuilderBatch();
        builder.build(target, target, classpath);
        builder.unbuild();
    }
    
    public static void print() {
        String dir = new File(".").getAbsoluteFile().getParent() + "/test_target/";
        String name = "Simple/";
        String target = dir + name;
        String classpath = target;
        
        ModelBuilderBatch builder = new ModelBuilderBatch();
        builder.setLogVisible(true);
        JavaProject jproject = builder.build(name, target, classpath);
        
        for (JavaClass jclass : jproject.getClasses()) {
            jclass.print();
            for (JavaMethod jmethod : jclass.getMethods()) {
                jmethod.print();
            }
            for (JavaField jfield : jclass.getFields()) {
                jfield.print();
            }
        }
        
        builder.unbuild();
    }
    
    public static void main(String[] args) {
        JavaModelBuilderTest tester = new JavaModelBuilderTest();
        
        tester.testSimple();
        tester.testDrawTool();
        tester.testLambda();
        tester.testJrb();
        
        // tester.testTetris();
        // tester.testCSSample();
        // tester.testFindbugs();
        // tester.testApacheAnt();
        // tester.testJdk8();
        
        // print();
    }
}
