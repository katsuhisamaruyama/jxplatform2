/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.test;

import org.jtool.eclipse.batch.ModelBuilderBatch;
import org.jtool.eclipse.cfg.CCFG;
import org.jtool.eclipse.cfg.CFG;
import org.jtool.eclipse.javamodel.JavaClass;
import org.jtool.eclipse.javamodel.JavaProject;
import java.util.List;
import org.junit.Test;

/**
 * Tests a class that builds a CFG.
 * Change the constant value of TEST_PROECT_DIR according to your environment.
 * 
 * @author Katsuhisa Maruyama
 */
public class CFGModelBuilderTest {
    
    private final String TEST_PROECT_DIR = "/Users/maru/Desktop/TestSamples/";
    
    private CCFG[] buildCFGsForTest(ModelBuilderBatch builder, List<JavaClass> jclasses) {
        int size = jclasses.size();
        CCFG[] ccfgs = new CCFG[size];
        int count = 1;
        System.out.println();
        System.out.println("** Building CFGs of " + size + " classes ");
        for (JavaClass jclass : jclasses) {
            System.out.print("(" + count + "/" + size + ")");
            ccfgs[count - 1] = builder.getCCFG(jclass);
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
        String target = TEST_PROECT_DIR + "Simple/";
        ModelBuilderBatch builder = new ModelBuilderBatch();
        JavaProject jproject = builder.build(target, target, target);
        
        builder.setAnalysisLevel(jproject, false, false);
        builder.setCreatingActualNodes(false);
        CCFG[] ccfgs = buildCFGsForTest(builder, jproject.getClasses());
        checkDetails(ccfgs);
        builder.unbuild();
    }
    
    @Test
    public void testSimple1() {
        String target = TEST_PROECT_DIR + "Simple/";
        ModelBuilderBatch builder = new ModelBuilderBatch();
        JavaProject jproject = builder.build(target, target, target);
        
        builder.setAnalysisLevel(jproject, true, false);
        builder.setCreatingActualNodes(false);
        buildCFGsForTest(builder, jproject.getClasses());
        builder.unbuild();
    }
    
    @Test
    public void testSimple2() {
        String target = TEST_PROECT_DIR + "Simple/";
        ModelBuilderBatch builder = new ModelBuilderBatch();
        JavaProject jproject = builder.build(target, target, target);
        
        builder.setAnalysisLevel(jproject, false, true);
        builder.setCreatingActualNodes(false);
        buildCFGsForTest(builder, jproject.getClasses());
        builder.unbuild();
    }
    
    @Test
    public void testJrb() {
        String target = TEST_PROECT_DIR + "jrb-1.0.2/src/";
        ModelBuilderBatch builder = new ModelBuilderBatch();
        JavaProject jproject = builder.build(target, target, target);
        
        builder.setAnalysisLevel(jproject, false, false);
        builder.setCreatingActualNodes(false);
        buildCFGsForTest(builder, jproject.getClasses());
        builder.unbuild();
    }
    
    @Test
    public void testTetris() {
        String target = TEST_PROECT_DIR + "Tetris/src/";
        ModelBuilderBatch builder = new ModelBuilderBatch();
        JavaProject jproject = builder.build(target, target, target);
        
        builder.setAnalysisLevel(jproject, false, false);
        builder.setCreatingActualNodes(false);
        buildCFGsForTest(builder, jproject.getClasses());
        builder.unbuild();
    }
    
    @Test
    public void testDrawTool() {
        String target = TEST_PROECT_DIR + "DrawTool/src/";
        ModelBuilderBatch builder = new ModelBuilderBatch();
        JavaProject jproject = builder.build(target, target, target);
        
        builder.setAnalysisLevel(jproject, false, false);
        builder.setCreatingActualNodes(false);
        buildCFGsForTest(builder, jproject.getClasses());
        builder.unbuild();
    }
    
    @Test
    public void testLambda() {
        String target = TEST_PROECT_DIR + "Lambda/";
        ModelBuilderBatch builder = new ModelBuilderBatch();
        JavaProject jproject = builder.build(target, target, target);
        
        builder.setAnalysisLevel(jproject, false, false);
        builder.setCreatingActualNodes(false);
        buildCFGsForTest(builder, jproject.getClasses());
        builder.unbuild();
    }
    
    @Test
    public void testCSSample() {
        String target = TEST_PROECT_DIR + "CS-Sample/";
        String classpath = TEST_PROECT_DIR + "CS-Sample/lib/*";
        ModelBuilderBatch builder = new ModelBuilderBatch();
        JavaProject jproject = builder.build(target, target, classpath);
        
        builder.setAnalysisLevel(jproject, false, false);
        builder.setCreatingActualNodes(false);
        buildCFGsForTest(builder, jproject.getClasses());
        builder.unbuild();
    }
    
    @Test
    public void testFindbugs() {
        String target = TEST_PROECT_DIR + "findbugs/";
        String classpath = TEST_PROECT_DIR + "findbugs/lib/*";
        ModelBuilderBatch builder = new ModelBuilderBatch();
        JavaProject jproject = builder.build(target, target, classpath);
        
        builder.setAnalysisLevel(jproject, false, false);
        builder.setCreatingActualNodes(false);
        buildCFGsForTest(builder, jproject.getClasses());
        builder.unbuild();
    }
    
    @Test
    public void testApacheAnt() {
        String target = TEST_PROECT_DIR + "apache-ant/";
        String classpath = TEST_PROECT_DIR + "apache-ant/lib/*";
        ModelBuilderBatch builder = new ModelBuilderBatch();
        JavaProject jproject = builder.build(target, target, classpath);
        
        builder.setAnalysisLevel(jproject, false, false);
        builder.setCreatingActualNodes(false);
        buildCFGsForTest(builder, jproject.getClasses());
        builder.unbuild();
    }
    
    public void notestJdk8() {
        String target = TEST_PROECT_DIR + "jdk1.8.0_131/";
        String classpath = TEST_PROECT_DIR + "jdk1.8.0_131/lib/*";
        ModelBuilderBatch builder = new ModelBuilderBatch();
        JavaProject jproject = builder.build(target, target, classpath);
        
        builder.setAnalysisLevel(jproject, false, false);
        builder.setCreatingActualNodes(false);
        buildCFGsForTest(builder, jproject.getClasses());
        builder.unbuild();
    }
    
    public static void main(String[] args) {
        CFGModelBuilderTest tester = new CFGModelBuilderTest();
        //tester.testSimple();
        //tester.testSimple1();
        tester.testSimple2();
        /*
        tester.testJrb();
        tester.testTetris();
        tester.testDrawTool();
        tester.testLambda();
        tester.testCSSample();
        tester.testFindbugs();
        tester.testApacheAnt();
        */
        //tester.notestJdk8();
    }
}
