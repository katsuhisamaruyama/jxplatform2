/*
 *  Copyright 2018-2020
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

/**
 * Tests a class that builds a Java Model.
 * 
 * @author Katsuhisa Maruyama
 */
public class JavaModelBuilderTest {
    
    private final static String testDirInside = new File(".").getAbsoluteFile().getParent() + "/test_target/";
    
    @Test
    public void testSimple() {
        String target = testDirInside + "Simple/";
        ModelBuilderBatch builder = new ModelBuilderBatch(false); // without byte-code analysis
        builder.build(target, target, target, target, target);
        builder.unbuild();
    }
    
    @Test
    public void testSimpleBytecode() {
        String target = testDirInside + "Simple/";
        ModelBuilderBatch builder = new ModelBuilderBatch(true); // with byte-code analysis
        builder.build(target, target, target, target, target);
        builder.unbuild();
    }
    
    @Test
    public void testDrawTool() {
        String target = testDirInside + "DrawTool/";
        ModelBuilderBatch builder = new ModelBuilderBatch(true);
        builder.build(target, target, target, target + "/src", target);
        builder.unbuild();
    }
    
    @Test
    public void testLambda() {
        String target = testDirInside + "Lambda/";
        ModelBuilderBatch builder = new ModelBuilderBatch(true);
        builder.build(target, target, target, target, target);
        builder.unbuild();
    }
    
    @Test
    public void testJrb() {
        String target = testDirInside + "jrb-1.0.2/";
        ModelBuilderBatch builder = new ModelBuilderBatch(true);
        builder.build(target, target, target, target + "/src", target);
        builder.unbuild();
    }
    
    @Test
    public void testTetris() {
        String target = testDirInside + "Tetris/";
        ModelBuilderBatch builder = new ModelBuilderBatch(true);
        builder.build(target, target, target, target, target);
        builder.unbuild();
    }
    
    @Test
    public void testCSClassroom() {
        String target = testDirInside + "CS-classroom/";
        String classpath = target + "lib/*";
        ModelBuilderBatch builder = new ModelBuilderBatch(true);
        builder.build(target, target, classpath, target + "/src/", target);
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
    
    private final static String testDirOutside = "/Users/maru/Desktop/TestSamples/";
    
    private void run(String name) {
        String target = testDirOutside + name + "/";
        
        File dir = new File(target);
        if (!dir.exists()) {
            System.err.println("Not found project " + target);
            return;
        }
        
        ModelBuilderBatch builder = new ModelBuilderBatch(true);
        builder.build(name, target, true);
        System.out.println("Fin.");
    }
    
    public static void main(String[] args) {
        JavaModelBuilderTest tester = new JavaModelBuilderTest();
        
        /* The files are stored inside the workspace */
        //tester.testSimple();
        //tester.testSimpleBytecode();
        //tester.testDrawTool();
        //tester.testLambda();
        //tester.testJrb();
        //tester.testTetris();
        //tester.testCSClassroom();
        
        //print();
        
        /* The files are stored outside the workspace */
        //tester.run("antlr4-4.8");  // Maven
        //tester.run("apache-ant-1.9.14");  // Ant
        //tester.run("apache-ant-1.10.7");  // Ant
        //tester.run("apache-log4j-2.13.0");  // Maven
        //tester.run("bcel-6.4.1");  // Maven
        //tester.run("findbugs-3.0.1");  // Eclipse / Maven
        //tester.run("commons-collections4-4.4");  // Maven
        //tester.run("commons-math3-3.6.1");  // Maven
                    //tester.run("guava-28.2");  // Maven // NULL RETURN TYPE
        //tester.run("jfreechart-1.0.19");  // Maven
        //tester.run("jsoup-1.12.2");  // Maven
        //tester.run("junit4-4.13");  // Maven
        //tester.run("pmd-6.21.0");
                    //tester.run("RxJava-3.x");  // Gradle  // NULL RETURN TYPE
                    //tester.run("Twitter4J-2.2");  // Maven // NULL RETURN TYPE
        
                //tester.run("spring-framework-5.2");  // Gradle
                //tester.run("spring-framework-5.2/spring-aop");  // Unresolved
        //tester.run("spring-framework-5.2/spring-aspects");
                //tester.run("spring-framework-5.2/spring-beans");  // Unresolved
                //tester.run("spring-framework-5.2/spring-context");  // NULL RETURN TYPE
        //tester.run("spring-framework-5.2/spring-context-indexer");
                //tester.run("spring-framework-5.2/spring-context-support");  // NULL RETURN TYPE
                //tester.run("spring-framework-5.2/spring-core");  // NULL RETURN TYPE
        //tester.run("spring-framework-5.2/spring-expression");
        //tester.run("spring-framework-5.2/spring-instrument");
        //tester.run("spring-framework-5.2/spring-jcl");
                //tester.run("spring-framework-5.2/spring-jdbc");  // NULL RETURN TYPE
                //tester.run("spring-framework-5.2/spring-jms");  // Unresolved
                //tester.run("spring-framework-5.2/spring-messaging");  / NULL RETURN TYPE Unresolved
                //tester.run("spring-framework-5.2/spring-orm");  // NULL RETURN TYPE Unresolved
                //tester.run("spring-framework-5.2/spring-oxm");  // Unresolved
                //tester.run("spring-framework-5.2/spring-test");  // Unresolved
                //tester.run("spring-framework-5.2/spring-tx");  // NULL RETURN TYPE Unresolved
                //tester.run("spring-framework-5.2/spring-web");  // NULL RETURN TYPE Unresolved
                //ester.run("spring-framework-5.2/spring-webflux");  // NULL RETURN TYPE Unresolved
                //tester.run("spring-framework-5.2/spring-webmvc");  // NULL RETURN TYPE Unresolved
                //tester.run("spring-framework-5.2/spring-websocket");  // NULL RETURN TYPE Unresolved
    }
}
