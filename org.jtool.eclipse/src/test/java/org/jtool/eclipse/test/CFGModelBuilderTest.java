/*
 *  Copyright 2018-2019
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.test;

import org.jtool.eclipse.batch.ModelBuilderBatch;
import org.jtool.eclipse.javamodel.JavaProject;
import org.jtool.eclipse.javamodel.JavaClass;
import org.jtool.eclipse.cfg.CCFG;
import org.jtool.eclipse.cfg.CFG;
import java.io.File;
import java.util.List;
import org.junit.Test;
import org.junit.Ignore;

/**
 * Tests a class that builds a CFG.
 * 
 * @author Katsuhisa Maruyama
 */
public class CFGModelBuilderTest {
    
    private final static String testDirInside = new File(".").getAbsoluteFile().getParent() + "/test_target/";
    private final static String testDirOutside = "/Users/maru/Desktop/TestSamples/";
    
    private CCFG[] buildCFGsForTest(ModelBuilderBatch builder, List<JavaClass> classes) {
        int size = classes.size();
        CCFG[] ccfgs = new CCFG[size];
        int count = 1;
        System.out.println();
        System.out.println("** Building CFGs of " + size + " classes ");
        for (JavaClass jclass : classes) {
            System.out.print("(" + count + "/" + size + ")");
            ccfgs[count - 1] = builder.getCCFG(jclass);
            System.out.print(" - " + jclass.getQualifiedName() + " - CCFG\n");
            count++;
        }
        return ccfgs;
    }
    
    private boolean checkDetails(CCFG[] ccfgs) {
        for (CCFG ccfg : ccfgs) {
            StringBuilder buf = new StringBuilder();
            buf.append(ccfg.getQualifiedName());
            buf.append("\n");
            for (CFG cfg : ccfg.getCFGs()) {
                buf.append(cfg.toString());
            }
            System.out.println(buf.toString());
        }
        return true;
    }
    
    @Test
    public void testSimple() {
        String target = testDirInside + "Simple/";
        ModelBuilderBatch builder = new ModelBuilderBatch(false);  // without byte-code analysis
        JavaProject jproject = builder.build(target, target, target, target, target);
        
        CCFG[] ccfgs = buildCFGsForTest(builder, jproject.getClasses());
        checkDetails(ccfgs);
        builder.unbuild();
    }
    
    @Test
    public void testSimpleBytecode() {
        String target = testDirInside + "Simple/";
        ModelBuilderBatch builder = new ModelBuilderBatch(true);  // with byte-code analysis
        JavaProject jproject = builder.build(target, target, target, target, target);
        
        buildCFGsForTest(builder, jproject.getClasses());
        builder.unbuild();
    }
    
    @Test
    public void testDrawTool() {
        String target = testDirInside + "DrawTool/";
        ModelBuilderBatch builder = new ModelBuilderBatch(false);
        JavaProject jproject = builder.build(target, target, target, target + "/src", target);
        
        buildCFGsForTest(builder, jproject.getClasses());
        builder.unbuild();
    }
    
    @Test
    public void testLambda() {
        String target = testDirInside + "Lambda/";
        ModelBuilderBatch builder = new ModelBuilderBatch(false);
        JavaProject jproject = builder.build(target, target, target, target, target);
        
        buildCFGsForTest(builder, jproject.getClasses());
        builder.unbuild();
    }
    
    @Test
    public void testJrb() {
        String target = testDirInside + "jrb-1.0.2/";
        ModelBuilderBatch builder = new ModelBuilderBatch(false);
        JavaProject jproject = builder.build(target, target, target, target + "/src", target);
        
        buildCFGsForTest(builder, jproject.getClasses());
        builder.unbuild();
    }
    
    @Test
    public void testTetris() {
        String target = testDirInside + "Tetris/";
        ModelBuilderBatch builder = new ModelBuilderBatch(false);
        JavaProject jproject = builder.build(target, target, target, target, target);
        
        buildCFGsForTest(builder, jproject.getClasses());
        builder.unbuild();
    }
    
    @Test
    public void testCSClassroom() {
        String target = testDirInside + "CS-classroom/";
        String classpath = target + "../lib/*";
        ModelBuilderBatch builder = new ModelBuilderBatch(false);
        JavaProject jproject = builder.build(target, target, classpath, target + "/src/", target);
        
        buildCFGsForTest(builder, jproject.getClasses());
        builder.unbuild();
    }
    
    @Ignore
    public void testApacheAnt() {
        String target = testDirOutside + "apache-ant/";
        String classpath = target + "../libs/*";
        ModelBuilderBatch builder = new ModelBuilderBatch(false);
        JavaProject jproject = builder.build(target, target, classpath, target + "/src", target);
        
        buildCFGsForTest(builder, jproject.getClasses());
        builder.unbuild();
    }
    
    public static void print() {
        String target = testDirInside + "Simple/";
        ModelBuilderBatch builder = new ModelBuilderBatch();
        JavaProject jproject = builder.build(target, target, target, target, target);
        
        for (JavaClass jclass : jproject.getClasses()) {
            CCFG ccfg = builder.getCCFG(jclass);
            for (CFG cfg : ccfg.getCFGs()) {
                cfg.print();
            }
        }
        
        builder.unbuild();
    }
    
    @SuppressWarnings("unused")
    public static void main(String[] args) {
        CFGModelBuilderTest tester = new CFGModelBuilderTest();
        
        /*
        tester.testSimple();
        tester.testSimpleBytecode();
        tester.testDrawTool();
        tester.testLambda();
        tester.testJrb();
        tester.testTetris();
        tester.testCSClassroom();
        
        tester.testApacheAnt();
        */
        
        print();
    }
}
