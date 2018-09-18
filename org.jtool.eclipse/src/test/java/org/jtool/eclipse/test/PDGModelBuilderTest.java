/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.test;

import org.jtool.eclipse.batch.ModelBuilderBatch;
import org.jtool.eclipse.pdg.PDG;
import org.jtool.eclipse.pdg.ClDG;
import org.jtool.eclipse.javamodel.JavaClass;
import org.jtool.eclipse.javamodel.JavaProject;
import java.util.List;
import org.junit.Test;

/**
 * Tests a class that builds a PDG.
 * Please change the constant value of TEST_PROECT_DIR according to your environment.
 * 
 * @author Katsuhisa Maruyama
 */
public class PDGModelBuilderTest {
    
    private final String TEST_PROECT_DIR = "/Users/maru/Desktop/TestSamples/";
    
    private ClDG[] buildPDGsForTest(ModelBuilderBatch builder, List<JavaClass> jclasses) {
        int size = jclasses.size();
        ClDG[] cldgs = new ClDG[size];
        System.out.println();
        System.out.println("** Building PDGs of " + size + " classes ");
        int count = 1;
        for (JavaClass jclass : jclasses) {
            cldgs[count - 1] = builder.getClDG(jclass);
            System.out.println(" (" + count + "/" + size + ")");
            count++;
        }
        return cldgs;
    }
    
    private boolean checkDetails(ClDG[] cclgs) {
        for (ClDG cldg : cclgs) {
            StringBuilder buf = new StringBuilder();
            buf.append(cldg.getQualifiedName());
            buf.append("\n");
            for (PDG pdg : cldg.getPDGs()) {
                buf.append(pdg.toString());
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
        ClDG[] cldgs = buildPDGsForTest(builder, jproject.getClasses());
        checkDetails(cldgs);
        builder.unbuild();
    }
    
    @Test
    public void testJrb() {
        String target = TEST_PROECT_DIR +  "jrb-1.0.2/src/";
        ModelBuilderBatch builder = new ModelBuilderBatch();
        JavaProject jproject = builder.build(target, target, target);
        
        builder.setAnalysisLevel(jproject, false, false);
        builder.setCreatingActualNodes(false);
        buildPDGsForTest(builder, jproject.getClasses());
        builder.unbuild();
    }
    
    @Test
    public void testTetris() {
        String target = TEST_PROECT_DIR + "Tetris/src/";
        ModelBuilderBatch builder = new ModelBuilderBatch();
        JavaProject jproject = builder.build(target, target, target);
        
        builder.setAnalysisLevel(jproject, false, false);
        builder.setCreatingActualNodes(false);
        buildPDGsForTest(builder, jproject.getClasses());
        builder.unbuild();
    }
    
    @Test
    public void testDrawTool() {
        String target = TEST_PROECT_DIR +  "DrawTool/src/";
        ModelBuilderBatch builder = new ModelBuilderBatch();
        JavaProject jproject = builder.build(target, target, target);
        
        builder.setAnalysisLevel(jproject, false, false);
        builder.setCreatingActualNodes(false);
        buildPDGsForTest(builder, jproject.getClasses());
        builder.unbuild();
    }
    
    @Test
    public void testLambda() {
        String target = TEST_PROECT_DIR +  "Lambda/";
        ModelBuilderBatch builder = new ModelBuilderBatch();
        JavaProject jproject = builder.build(target, target, target);
        
        builder.setAnalysisLevel(jproject, false, false);
        builder.setCreatingActualNodes(false);
        buildPDGsForTest(builder, jproject.getClasses());
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
        buildPDGsForTest(builder, jproject.getClasses());
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
        buildPDGsForTest(builder, jproject.getClasses());
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
        buildPDGsForTest(builder, jproject.getClasses());
        builder.unbuild();
    }
    
    public void notestJdk8() {
        String target = TEST_PROECT_DIR + "jdk1.8.0_131/";
        String classpath = TEST_PROECT_DIR + "jdk1.8.0_131/lib/*";
        ModelBuilderBatch builder = new ModelBuilderBatch();
        JavaProject jproject = builder.build(target, target, classpath);
        
        builder.setAnalysisLevel(jproject, false, false);
        builder.setCreatingActualNodes(false);
        buildPDGsForTest(builder, jproject.getClasses());
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
