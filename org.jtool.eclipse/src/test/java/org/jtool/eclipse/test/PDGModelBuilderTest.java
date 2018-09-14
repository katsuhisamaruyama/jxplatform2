/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.test;

import org.jtool.eclipse.pdg.PDGStore;
import org.jtool.eclipse.batch.JavaModelBuilder;
import org.jtool.eclipse.cfg.CFGStore;
import org.jtool.eclipse.javamodel.JavaProject;
import org.junit.Test;

/**
 * Tests a class that builds a PDG.
 * Please change the constant value of TEST_PROECT_DIR according to your environment.
 * 
 * @author Katsuhisa Maruyama
 */
public class PDGModelBuilderTest {
    
    private final String TEST_PROECT_DIR = "/Users/maru/Desktop/TestSamples/";
    
    @Test
    public void testSimple() {
        String target = TEST_PROECT_DIR + "Simple/";
        JavaModelBuilder builder = new JavaModelBuilder(target, target);
        JavaProject jproject = builder.build();
        
        CFGStore.getInstance().setAnalysisLevel(jproject, false);
        CFGStore.getInstance().setCreatingActualNodes(false);
        
        PDGStore.getInstance().buildPDGsForTest(jproject.getClasses());
        PDGStore.getInstance().destroy();
        builder.unbuild();
    }
    
    @Test
    public void testJrb() {
        String target = TEST_PROECT_DIR +  "jrb-1.0.2/src/";
        JavaModelBuilder builder = new JavaModelBuilder(target, target);
        JavaProject jproject = builder.build();
        
        CFGStore.getInstance().setAnalysisLevel(jproject, false);
        CFGStore.getInstance().setCreatingActualNodes(false);
        
        PDGStore.getInstance().buildPDGsForTest(jproject.getClasses());
        PDGStore.getInstance().destroy();
        builder.unbuild();
    }
    
    @Test
    public void testTetris() {
        String target = TEST_PROECT_DIR + "Tetris/src/";
        JavaModelBuilder builder = new JavaModelBuilder(target, target);
        JavaProject jproject = builder.build();
        
        CFGStore.getInstance().setAnalysisLevel(jproject, false);
        CFGStore.getInstance().setCreatingActualNodes(false);
        
        PDGStore.getInstance().buildPDGsForTest(jproject.getClasses());
        PDGStore.getInstance().destroy();
        builder.unbuild();
    }
    
    @Test
    public void testDrawTool() {
        String target = TEST_PROECT_DIR +  "DrawTool/src/";
        JavaModelBuilder builder = new JavaModelBuilder(target, target);
        JavaProject jproject = builder.build();
        
        CFGStore.getInstance().setAnalysisLevel(jproject, false);
        CFGStore.getInstance().setCreatingActualNodes(false);
        
        PDGStore.getInstance().buildPDGsForTest(jproject.getClasses());
        PDGStore.getInstance().destroy();
        builder.unbuild();
    }
    
    @Test
    public void testLambda() {
        String target = TEST_PROECT_DIR +  "Lambda/";
        JavaModelBuilder builder = new JavaModelBuilder(target, target);
        JavaProject jproject = builder.build();
        
        CFGStore.getInstance().setAnalysisLevel(jproject, false);
        CFGStore.getInstance().setCreatingActualNodes(false);
        
        PDGStore.getInstance().buildPDGsForTest(jproject.getClasses());
        PDGStore.getInstance().destroy();
        builder.unbuild();
    }
    
    @Test
    public void testCSSample() {
        String target = TEST_PROECT_DIR + "CS-Sample/";
        String classpath = TEST_PROECT_DIR + "CS-Sample/lib/*";
        JavaModelBuilder builder = new JavaModelBuilder(target, target, classpath);
        JavaProject jproject = builder.build();
        
        CFGStore.getInstance().setAnalysisLevel(jproject, false);
        CFGStore.getInstance().setCreatingActualNodes(false);
        
        PDGStore.getInstance().buildPDGsForTest(jproject.getClasses());
        PDGStore.getInstance().destroy();
        builder.unbuild();
    }
    
    @Test
    public void testFindbugs() {
        String target = TEST_PROECT_DIR + "findbugs/";
        String classpath = TEST_PROECT_DIR + "findbugs/lib/*";
        JavaModelBuilder builder = new JavaModelBuilder(target, target, classpath);
        JavaProject jproject = builder.build();
        
        CFGStore.getInstance().setAnalysisLevel(jproject, false);
        CFGStore.getInstance().setCreatingActualNodes(false);
        
        PDGStore.getInstance().buildPDGsForTest(jproject.getClasses());
        PDGStore.getInstance().destroy();
        builder.unbuild();
    }
    
    @Test
    public void testApacheAnt() {
        String target = TEST_PROECT_DIR + "apache-ant/";
        String classpath = TEST_PROECT_DIR + "apache-ant/lib/*";
        JavaModelBuilder builder = new JavaModelBuilder(target, target, classpath);
        JavaProject jproject = builder.build();
        
        CFGStore.getInstance().setAnalysisLevel(jproject, false);
        CFGStore.getInstance().setCreatingActualNodes(false);
        
        PDGStore.getInstance().buildPDGsForTest(jproject.getClasses());
        PDGStore.getInstance().destroy();
        builder.unbuild();
    }
    
    public void notestJdk8() {
        String target = TEST_PROECT_DIR + "jdk1.8.0_131/";
        String classpath = TEST_PROECT_DIR + "jdk1.8.0_131/lib/*";
        JavaModelBuilder builder = new JavaModelBuilder(target, target, classpath);
        JavaProject jproject = builder.build();
        
        CFGStore.getInstance().setAnalysisLevel(jproject, false);
        CFGStore.getInstance().setCreatingActualNodes(false);
        
        PDGStore.getInstance().buildPDGsForTest(jproject.getClasses());
        PDGStore.getInstance().destroy();
        builder.unbuild();
    }
    
    public static void main(String[] args) {
        PDGModelBuilderTest tester = new PDGModelBuilderTest();
        //tester.testSimple();
        //tester.testJrb();
        //tester.testTetris();
        //tester.testDrawTool();
        //tester.testLambda();
        //tester.testCSSample();
        //tester.testFindbugs();
        //tester.testApacheAnt();
        tester.notestJdk8();
    }
}
