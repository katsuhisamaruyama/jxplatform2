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
        String dir = new File(".").getAbsoluteFile().getParent() + "/test_target/";
        String target = dir + "Simple/";
        ModelBuilderBatch builder = new ModelBuilderBatch(false);  // without byte-code analysis
        JavaProject jproject = builder.build(target, target, target, target, target);
        
        builder.setCreatingActualNodes(false);
        CCFG[] ccfgs = buildCFGsForTest(builder, jproject.getClasses());
        checkDetails(ccfgs);
        builder.unbuild();
    }
    
    @Test
    public void testSimpleBytecode() {
        String dir = new File(".").getAbsoluteFile().getParent() + "/test_target/";
        String target = dir + "Simple/";
        ModelBuilderBatch builder = new ModelBuilderBatch(true);  // with byte-code analysis
        JavaProject jproject = builder.build(target, target, target, target, target);
        
        builder.setCreatingActualNodes(false);
        buildCFGsForTest(builder, jproject.getClasses());
        builder.unbuild();
    }
    
    @Test
    public void testDrawTool() {
        String dir = new File(".").getAbsoluteFile().getParent() + "/test_target/";
        String target = dir + "DrawTool/";
        ModelBuilderBatch builder = new ModelBuilderBatch(false);
        JavaProject jproject = builder.build(target, target, target, target + "/src", target);
        
        builder.setCreatingActualNodes(false);
        buildCFGsForTest(builder, jproject.getClasses());
        builder.unbuild();
    }
    
    @Test
    public void testLambda() {
        String dir = new File(".").getAbsoluteFile().getParent() + "/test_target/";
        String target = dir + "Lambda/";
        ModelBuilderBatch builder = new ModelBuilderBatch(false);
        JavaProject jproject = builder.build(target, target, target, target, target);
        
        builder.setCreatingActualNodes(false);
        buildCFGsForTest(builder, jproject.getClasses());
        builder.unbuild();
    }
    
    @Test
    public void testJrb() {
        String dir = new File(".").getAbsoluteFile().getParent() + "/test_target/";
        String target = dir + "jrb-1.0.2/";
        ModelBuilderBatch builder = new ModelBuilderBatch(false);
        JavaProject jproject = builder.build(target, target, target, target + "/src", target);
        
        builder.setCreatingActualNodes(false);
        buildCFGsForTest(builder, jproject.getClasses());
        builder.unbuild();
    }
    
    @Ignore
    public void testTetris() {
        String dir = "/Users/maru/Desktop/TestSamples/";
        String target = dir + "Tetris/";
        ModelBuilderBatch builder = new ModelBuilderBatch(false);
        JavaProject jproject = builder.build(target, target, target, target + "/src", target);
        
        builder.setCreatingActualNodes(false);
        buildCFGsForTest(builder, jproject.getClasses());
        builder.unbuild();
    }
    
    @Ignore
    public void testCSSample() {
        String dir = "/Users/maru/Desktop/TestSamples/";
        String target = dir + "CS-Sample/";
        String classpath = dir + "CS-Sample/lib/*";
        ModelBuilderBatch builder = new ModelBuilderBatch(false);
        JavaProject jproject = builder.build(target, target, classpath, target + "/src", target);
        
        builder.setCreatingActualNodes(false);
        buildCFGsForTest(builder, jproject.getClasses());
        builder.unbuild();
    }
    
    @Ignore
    public void testFindbugs() {
        String dir = "/Users/maru/Desktop/TestSamples/";
        String target = dir + "findbugs/";
        String classpath = dir + "findbugs/lib/*";
        ModelBuilderBatch builder = new ModelBuilderBatch(false);
        JavaProject jproject = builder.build(target, target, classpath, target + "/src", target);
        
        builder.setCreatingActualNodes(false);
        buildCFGsForTest(builder, jproject.getClasses());
        builder.unbuild();
    }
    
    @Ignore
    public void testApacheAnt() {
        String dir = "/Users/maru/Desktop/TestSamples/";
        String target = dir + "apache-ant/";
        String classpath = dir + "apache-ant/lib/*";
        ModelBuilderBatch builder = new ModelBuilderBatch(false);
        JavaProject jproject = builder.build(target, target, classpath, target + "/src", target);
        
        builder.setCreatingActualNodes(false);
        buildCFGsForTest(builder, jproject.getClasses());
        builder.unbuild();
    }
    
    @Ignore
    public void testJdk8() {
        String dir = "/Users/maru/Desktop/TestSamples/";
        String target = dir + "jdk1.8.0_131/";
        String classpath = dir + "jdk1.8.0_131/lib/*";
        ModelBuilderBatch builder = new ModelBuilderBatch(false);
        JavaProject jproject = builder.build(target, target, classpath, target + "/src", target);
        
        builder.setCreatingActualNodes(false);
        buildCFGsForTest(builder, jproject.getClasses());
        builder.unbuild();
    }
    
    public static void print() {
        String dir = new File(".").getAbsoluteFile().getParent() + "/test_target/";
        String name = "Simple/";
        String target = dir + name;
        String classpath = target;
        
        ModelBuilderBatch builder = new ModelBuilderBatch();
        builder.setLogVisible(true);
        builder.setCreatingActualNodes(true);
        JavaProject jproject = builder.build(name, target, classpath);
        
        for (JavaClass jclass : jproject.getClasses()) {
            CCFG ccfg = builder.getCCFG(jclass);
            for (CFG cfg : ccfg.getCFGs()) {
                cfg.print();
            }
        }
        
        builder.unbuild();
    }
    
    public static void main(String[] args) {
        CFGModelBuilderTest tester = new CFGModelBuilderTest();
        
        tester.testSimple();
        tester.testSimpleBytecode();
        tester.testDrawTool();
        tester.testLambda();
        tester.testJrb();
        
        //tester.testTetris();
        //tester.testCSSample();
        //tester.testFindbugs();
        //tester.testApacheAnt();
        //tester.testJdk8();
        
        // print();
    }
}
