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
    
    private final static String testDirInside = new File(".").getAbsoluteFile().getParent() + "/test_target/";
    private final static String testDirOutside = "/Users/maru/Desktop/TestSamples/";
    
    @Test
    public void testSimple() {
        String target = testDirInside + "Simple/";
        ModelBuilderBatch builder = new ModelBuilderBatch();
        builder.build(target, target, target, target, target);
        builder.unbuild();
    }
    
    @Test
    public void testDrawTool() {
        String target = testDirInside + "DrawTool/";
        ModelBuilderBatch builder = new ModelBuilderBatch();
        builder.build(target, target, target, target + "/src", target);
        builder.unbuild();
    }
    
    @Test
    public void testLambda() {
        String target = testDirInside + "Lambda/";
        ModelBuilderBatch builder = new ModelBuilderBatch();
        builder.build(target, target, target, target, target);
        builder.unbuild();
    }
    
    @Test
    public void testJrb() {
        String target = testDirInside + "jrb-1.0.2/";
        ModelBuilderBatch builder = new ModelBuilderBatch();
        builder.build(target, target, target, target + "/src", target);
        builder.unbuild();
    }
    
    @Test
    public void testTetris() {
        String target = testDirInside + "Tetris/";
        ModelBuilderBatch builder = new ModelBuilderBatch(false);
        builder.build(target, target, target, target, target);
        builder.unbuild();
    }
    
    @Test
    public void testCSClassroom() {
        String target = testDirInside + "CS-classroom/";
        String classpath = target + "lib/*";
        ModelBuilderBatch builder = new ModelBuilderBatch(false);
        builder.build(target, target, classpath, target + "/src/", target);
        builder.unbuild();
    }
    
    @Ignore
    public void testAntlr() {
        String target = testDirOutside + "antlr-4.7.2/";
        String binpath = target  + "bin";
        String classpath = target + "../libs/*";
        ModelBuilderBatch builder = new ModelBuilderBatch();
        builder.build(target, target, classpath, target, binpath);
        builder.unbuild();
    }
    
    @Ignore
    public void testApacheAnt() {
        String target = testDirOutside + "apache-ant-1.10.7/";
        String binpath = target  + "bin";
        String classpath = target + "../libs/*";
        ModelBuilderBatch builder = new ModelBuilderBatch();
        builder.build(target, target, classpath, target, binpath);
        builder.unbuild();
    }
    
    @Ignore
    public void testApacheLog4j() {
        String target = testDirOutside + "apache-log4j-2.12.1/";
        String binpath = target  + "bin";
        String classpath = target + "../libs/*";
        ModelBuilderBatch builder = new ModelBuilderBatch();
        builder.build(target, target, classpath, target, binpath);
        builder.unbuild();
    }
    @Ignore
    public void testCassandra() {
        String target = testDirOutside + "cassandra-3.11.4/";
        String[] srcpath = new String[2];
        srcpath[0] = target + "src/java";
        srcpath[1] = target + "test";
        String binpath = target  + "bin";
        String classpath = target + "../libs/*";
        ModelBuilderBatch builder = new ModelBuilderBatch();
        builder.build(target, target, classpath, srcpath, binpath);
        builder.unbuild();
    }
    
    @Ignore
    public void testCommonsCollections() {
        String target = testDirOutside + "commons-collections4-4.2/";
        String binpath = target  + "bin";
        String classpath = target + "lib/*";
        ModelBuilderBatch builder = new ModelBuilderBatch();
        builder.build(target, target, classpath, target, binpath);
        builder.unbuild();
    }
    
    @Ignore
    public void testCommonsMath() {
        String target = testDirOutside + "commons-math3-3.6.1/";
        String binpath = target  + "bin";
        String classpath = target + "lib/*";
        ModelBuilderBatch builder = new ModelBuilderBatch();
        builder.build(target, target, classpath, target, binpath);
        builder.unbuild();
    }
    
    @Ignore
    public void testGuava() {
        String target = testDirOutside + "guava-28.1/";
        String binpath = target  + "bin";
        String classpath = target + "../libs/*";
        ModelBuilderBatch builder = new ModelBuilderBatch();
        builder.build(target, target, classpath, target, binpath);
        builder.unbuild();
    }
    
    @Ignore
    public void testJunit() {
        String target = testDirOutside + "junit-5.5.2/";
        String binpath = target  + "bin";
        String classpath = target + "../libs/*";
        ModelBuilderBatch builder = new ModelBuilderBatch();
        builder.build(target, target, classpath, target, binpath);
        builder.unbuild();
    }
    
    @Ignore
    public void testPMD() {
        String target = testDirOutside + "pmd-6.18.0/";
        String binpath = target  + "bin";
        String classpath = target + "../libs/*";
        ModelBuilderBatch builder = new ModelBuilderBatch();
        builder.build(target, target, classpath, target, binpath);
        builder.unbuild();
    }
    
    public static void print() {
        String target = testDirInside + "Simple/";
        ModelBuilderBatch builder = new ModelBuilderBatch();
        JavaProject jproject = builder.build(target, target, target, target, target);
        
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
        
        /* The files are stored inside the workspace */
        tester.testSimple();
        tester.testDrawTool();
        tester.testLambda();
        tester.testJrb();
        tester.testTetris();
        tester.testCSClassroom();
        
        /* The files are stored outside the workspace */
        //tester.testAntlr();
        //tester.testApacheAnt();
        //tester.testApacheLog4j();
        //tester.testCassandra();
        //tester.testCommonsCollections();
        //tester.testCommonsMath();
        //tester.testGuava();
        //tester.testJunit();
        //tester.testPMD();
        
        print();
    }
}
