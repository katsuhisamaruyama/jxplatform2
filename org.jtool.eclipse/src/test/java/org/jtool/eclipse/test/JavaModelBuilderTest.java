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
        builder.build(target, target, target, target, target);
        builder.unbuild();
    }
    
    @Test
    public void testDrawTool() {
        String dir = new File(".").getAbsoluteFile().getParent() + "/test_target/";
        String target = dir + "DrawTool/";
        ModelBuilderBatch builder = new ModelBuilderBatch();
        builder.build(target, target, target, target + "/src", target);
        builder.unbuild();
    }
    
    @Test
    public void testLambda() {
        String dir = new File(".").getAbsoluteFile().getParent() + "/test_target/";
        String target = dir + "Lambda/";
        ModelBuilderBatch builder = new ModelBuilderBatch();
        builder.build(target, target, target, target, target);
        builder.unbuild();
    }
    
    @Test
    public void testJrb() {
        String dir = new File(".").getAbsoluteFile().getParent() + "/test_target/";
        String target = dir + "jrb-1.0.2/";
        ModelBuilderBatch builder = new ModelBuilderBatch();
        builder.build(target, target, target, target + "/src", target);
        builder.unbuild();
    }
    
    @Ignore
    public void testAntlr() {
        String dir = "/Users/maru/Desktop/TestSamples/";
        String target = dir + "antlr-4.7.2/";
        String binpath = target  + "bin";
        String classpath = target + "../libs/*";
        ModelBuilderBatch builder = new ModelBuilderBatch();
        builder.build(target, target, classpath, target, binpath);
        builder.unbuild();
    }
    
    @Ignore
    public void testApacheAnt() {
        String dir = "/Users/maru/Desktop/TestSamples/";
        String target = dir + "apache-ant-1.10.7/";
        String binpath = target  + "bin";
        String classpath = target + "../libs/*";
        ModelBuilderBatch builder = new ModelBuilderBatch();
        builder.build(target, target, classpath, target, binpath);
        builder.unbuild();
    }
    
    @Ignore
    public void testApacheLog4j() {
        String dir = "/Users/maru/Desktop/TestSamples/";
        String target = dir + "apache-log4j-2.12.1/";
        String binpath = target  + "bin";
        String classpath = target + "../libs/*";
        ModelBuilderBatch builder = new ModelBuilderBatch();
        builder.build(target, target, classpath, target, binpath);
        builder.unbuild();
    }
    @Ignore
public void testCassandra() {
        String dir = "/Users/maru/Desktop/TestSamples/";
        String target = dir + "cassandra-3.11.4/";
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
        String dir = "/Users/maru/Desktop/TestSamples/";
        String target = dir + "commons-collections4-4.2/";
        String binpath = target  + "bin";
        String classpath = target + "lib/*";
        ModelBuilderBatch builder = new ModelBuilderBatch();
        builder.build(target, target, classpath, target, binpath);
        builder.unbuild();
    }
    
    @Ignore
    public void testCommonsMath() {
        String dir = "/Users/maru/Desktop/TestSamples/";
        String target = dir + "commons-math3-3.6.1/";
        String binpath = target  + "bin";
        String classpath = target + "lib/*";
        ModelBuilderBatch builder = new ModelBuilderBatch();
        builder.build(target, target, classpath, target, binpath);
        builder.unbuild();
    }
    
    @Ignore
public void testElasticSearch() {
        String dir = "/Users/maru/Desktop/TestSamples/";
        String target = dir + "elasticsearch-6.7.2/";
        String[] srcpath = new String[6];
        srcpath[0] = target + "server/src/main";
        srcpath[1] = target + "server/src/test";
        srcpath[2] = target + "client/src/main";
        srcpath[3] = target + "client/src/test";
        srcpath[4] = target + "libs";
        srcpath[5] = target + "modules";
        String binpath = target  + "bin";
        String classpath = target + "lib/*";
        ModelBuilderBatch builder = new ModelBuilderBatch();
        builder.build(target, target, classpath, target, binpath);
        builder.unbuild();
    }
    
    @Ignore
    public void testGuava() {
        String dir = "/Users/maru/Desktop/TestSamples/";
        String target = dir + "guava-28.1/";
        String binpath = target  + "bin";
        String classpath = target + "../libs/*";
        ModelBuilderBatch builder = new ModelBuilderBatch();
        builder.build(target, target, classpath, target, binpath);
        builder.unbuild();
    }
    
    @Ignore
    public void testJdk() {
        String dir = "/Users/maru/Desktop/TestSamples/";
        String target = dir + "jdk1.8.0_131/";
        String classpath = dir + "jdk1.8.0_131/lib/*";
        ModelBuilderBatch builder = new ModelBuilderBatch();
        builder.build(target, target, classpath, target + "/src", target);
        builder.unbuild();
    }
    
    @Ignore
    public void testJunit() {
        String dir = "/Users/maru/Desktop/TestSamples/";
        String target = dir + "junit-5.5.2/";
        String binpath = target  + "bin";
        String classpath = target + "../libs/*";
        ModelBuilderBatch builder = new ModelBuilderBatch();
        builder.build(target, target, classpath, target, binpath);
        builder.unbuild();
    }
    
    @Ignore
    public void testPMD() {
        String dir = "/Users/maru/Desktop/TestSamples/";
        String target = dir + "pmd-6.18.0/";
        String binpath = target  + "bin";
        String classpath = target + "../libs/*";
        ModelBuilderBatch builder = new ModelBuilderBatch();
        builder.build(target, target, classpath, target, binpath);
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
        
        //tester.testSimple();
        //tester.testDrawTool();
        //tester.testLambda();
        //tester.testJrb();
        
        //tester.testAntlr();
        //tester.testApacheAnt();
        //tester.testApacheLog4j();
//tester.testCassandra();
        //tester.testCommonsCollections();
        //tester.testCommonsMath();
//tester.testElasticSearch();
        //tester.testGuava();
//tester.testJdk();
tester.testJunit();
        //tester.testPMD();
        
        // print();
    }
}
