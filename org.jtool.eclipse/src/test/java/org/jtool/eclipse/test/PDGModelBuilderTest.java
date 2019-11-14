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
    
    private final static String testDirInside = new File(".").getAbsoluteFile().getParent() + "/test_target/";
    private final static String testDirOutside = "/Users/maru/Desktop/TestSamples/";
    
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
        String target = testDirInside + "Simple/";
        ModelBuilderBatch builder = new ModelBuilderBatch(false);
        JavaProject jproject = builder.build(target, target, target, target, target);
        
        ClDG[] cldgs = buildPDGsForTest(builder, jproject.getClasses());
        checkDetails(cldgs);
        builder.unbuild();
    }
    
    @Test
    public void testDrawTool() {
        String target = testDirInside +  "DrawTool/";
        ModelBuilderBatch builder = new ModelBuilderBatch(false);
        JavaProject jproject = builder.build(target, target, target, target + "/src", target);
        
        buildPDGsForTest(builder, jproject.getClasses());
        builder.unbuild();
    }
    
    @Test
    public void testLambda() {
        String target = testDirInside +  "Lambda/";
        ModelBuilderBatch builder = new ModelBuilderBatch(false);
        JavaProject jproject = builder.build(target, target, target, target, target);
        
        buildPDGsForTest(builder, jproject.getClasses());
        builder.unbuild();
    }
    
    @Test
    public void testJrb() {
        String target = testDirInside +  "jrb-1.0.2/";
        ModelBuilderBatch builder = new ModelBuilderBatch(false);
        JavaProject jproject = builder.build(target, target, target, target + "/src", target);
        
        buildPDGsForTest(builder, jproject.getClasses());
        builder.unbuild();
    }
    
    @Test
    public void testTetris() {
        String target = testDirInside + "Tetris/";
        ModelBuilderBatch builder = new ModelBuilderBatch(false);
        JavaProject jproject = builder.build(target, target, target, target, target);
        
        buildPDGsForTest(builder, jproject.getClasses());
        builder.unbuild();
    }
    
    @Test
    public void testCSClassroom() {
        String target = testDirInside + "CS-classroom/";
        String classpath = target + "../lib/*";
        ModelBuilderBatch builder = new ModelBuilderBatch(false);
        JavaProject jproject = builder.build(target, target, classpath, target + "/src/", target);
        
        buildPDGsForTest(builder, jproject.getClasses());
        builder.unbuild();
    }
    
    @Ignore
    public void testFindbugs() {
        String dir = "/Users/maru/Desktop/TestSamples/";
        String target = dir + "findbugs/";
        String classpath = dir + "findbugs/lib/*";
        ModelBuilderBatch builder = new ModelBuilderBatch(false);
        JavaProject jproject = builder.build(target, target, classpath, target + "/src", target);
        
        buildPDGsForTest(builder, jproject.getClasses());
        builder.unbuild();
    }
    
    @Ignore
    public void testApacheAnt() {
        String target = testDirOutside + "apache-ant/";
        String classpath = target + "../libs/*";
        ModelBuilderBatch builder = new ModelBuilderBatch(false);
        JavaProject jproject = builder.build(target, target, classpath, target + "/src", target);
        
        buildPDGsForTest(builder, jproject.getClasses());
        builder.unbuild();
    }
    
    public static void print() {
        String target = testDirInside + "Simple/";
        ModelBuilderBatch builder = new ModelBuilderBatch();
        JavaProject jproject = builder.build(target, target, target, target, target);
        
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
        //tester.testJrb();
        tester.testTetris();
        tester.testCSClassroom();
        
        //tester.testApacheAnt();
        
        print();
    }
}
