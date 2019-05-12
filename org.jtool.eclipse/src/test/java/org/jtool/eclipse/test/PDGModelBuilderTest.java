/*
 *  Copyright 2018-2019
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.test;

import org.jtool.eclipse.batch.ModelBuilderBatch;
import org.jtool.eclipse.pdg.PDG;
import org.jtool.eclipse.pdg.ClDG;
import org.jtool.eclipse.javamodel.JavaClass;
import org.jtool.eclipse.javamodel.JavaProject;
import java.io.File;
import java.util.List;
import org.junit.Test;
import org.junit.Ignore;

/**
 * Tests a class that builds a PDG.
 * 
 * @author Katsuhisa Maruyama
 */
public class PDGModelBuilderTest {
    
    
    private ClDG[] buildPDGsForTest(ModelBuilderBatch builder, List<JavaClass> classes) {
        int size = classes.size();
        ClDG[] cldgs = new ClDG[size];
        System.out.println();
        System.out.println("** Building PDGs of " + size + " classes ");
        int count = 1;
        for (JavaClass jclass : classes) {
            System.out.print(" (" + count + "/" + size + ")");
            cldgs[count - 1] = builder.getClDG(jclass);
            System.out.print(" - " + jclass.getQualifiedName() + " - ClDG\n");
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
        String dir = new File(".").getAbsoluteFile().getParent() + "/test_target/";
        String target = dir + "Simple/";
        ModelBuilderBatch builder = new ModelBuilderBatch(false);
        JavaProject jproject = builder.build(target, target, target);
        
        builder.setCreatingActualNodes(false);
        ClDG[] cldgs = buildPDGsForTest(builder, jproject.getClasses());
        checkDetails(cldgs);
        builder.unbuild();
    }
    
    @Test
    public void testDrawTool() {
        String dir = new File(".").getAbsoluteFile().getParent() + "/test_target/";
        String target = dir +  "DrawTool/src/";
        ModelBuilderBatch builder = new ModelBuilderBatch(false);
        JavaProject jproject = builder.build(target, target, target);
        
        builder.setCreatingActualNodes(false);
        buildPDGsForTest(builder, jproject.getClasses());
        builder.unbuild();
    }
    
    @Test
    public void testLambda() {
        String dir = new File(".").getAbsoluteFile().getParent() + "/test_target/";
        String target = dir +  "Lambda/";
        ModelBuilderBatch builder = new ModelBuilderBatch(false);
        JavaProject jproject = builder.build(target, target, target);
        
        builder.setCreatingActualNodes(false);
        buildPDGsForTest(builder, jproject.getClasses());
        builder.unbuild();
    }
    
    @Test
    public void testJrb() {
        String dir = new File(".").getAbsoluteFile().getParent() + "/test_target/";
        String target = dir +  "jrb-1.0.2/src/";
        ModelBuilderBatch builder = new ModelBuilderBatch(false);
        JavaProject jproject = builder.build(target, target, target);
        
        builder.setCreatingActualNodes(false);
        buildPDGsForTest(builder, jproject.getClasses());
        builder.unbuild();
    }
    
    @Ignore
    public void testTetris() {
        String dir = "/Users/maru/Desktop/TestSamples/";
        String target = dir + "Tetris/src/";
        ModelBuilderBatch builder = new ModelBuilderBatch(false);
        JavaProject jproject = builder.build(target, target, target);
        
        builder.setCreatingActualNodes(false);
        buildPDGsForTest(builder, jproject.getClasses());
        builder.unbuild();
    }
    
    @Ignore
    public void testCSSample() {
        String dir = "/Users/maru/Desktop/TestSamples/";
        String target = dir + "CS-Sample/";
        String classpath = dir + "CS-Sample/lib/*";
        ModelBuilderBatch builder = new ModelBuilderBatch(false);
        JavaProject jproject = builder.build(target, target, classpath);
        
        builder.setCreatingActualNodes(false);
        buildPDGsForTest(builder, jproject.getClasses());
        builder.unbuild();
    }
    
    @Ignore
    public void testFindbugs() {
        String dir = "/Users/maru/Desktop/TestSamples/";
        String target = dir + "findbugs/";
        String classpath = dir + "findbugs/lib/*";
        ModelBuilderBatch builder = new ModelBuilderBatch(false);
        JavaProject jproject = builder.build(target, target, classpath);
        
        builder.setCreatingActualNodes(false);
        buildPDGsForTest(builder, jproject.getClasses());
        builder.unbuild();
    }
    
    @Ignore
    public void testApacheAnt() {
        String dir = "/Users/maru/Desktop/TestSamples/";
        String target = dir + "apache-ant/";
        String classpath = dir + "apache-ant/lib/*";
        ModelBuilderBatch builder = new ModelBuilderBatch(false);
        JavaProject jproject = builder.build(target, target, classpath);
        
        builder.setCreatingActualNodes(false);
        buildPDGsForTest(builder, jproject.getClasses());
        builder.unbuild();
    }
    
    @Ignore
    public void testJdk8() {
        String dir = "/Users/maru/Desktop/TestSamples/";
        String target = dir + "jdk1.8.0_131/";
        String classpath = dir + "jdk1.8.0_131/lib/*";
        ModelBuilderBatch builder = new ModelBuilderBatch(false);
        JavaProject jproject = builder.build(target, target, classpath);
        
        builder.setCreatingActualNodes(false);
        buildPDGsForTest(builder, jproject.getClasses());
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
        builder.setContainingFallThroughEdge(true); 
        JavaProject jproject = builder.build(name, target, classpath);
        
        for (JavaClass jclass : jproject.getClasses()) {
            ClDG cldg = builder.getClDG(jclass);
            for (PDG pdg : cldg.getPDGs()) {
                pdg.print();
            }
        }
        
        builder.unbuild();
    }
    
    public static void main(String[] args) {
        PDGModelBuilderTest tester = new PDGModelBuilderTest();
        
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
