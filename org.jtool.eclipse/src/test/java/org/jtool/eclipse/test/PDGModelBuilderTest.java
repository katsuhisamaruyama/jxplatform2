/*
 *  Copyright 2018-2020
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.test;

import org.jtool.eclipse.batch.ModelBuilderBatch;
import org.jtool.eclipse.pdg.PDG;
import org.jtool.eclipse.util.TimeInfo;
import org.jtool.eclipse.pdg.ClDG;
import org.jtool.eclipse.javamodel.JavaClass;
import org.jtool.eclipse.javamodel.JavaProject;
import java.io.File;
import java.util.List;
import org.junit.Test;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Tests a class that builds a PDG.
 * 
 * @author Katsuhisa Maruyama
 */
public class PDGModelBuilderTest {
    
    private final static String testDirInside = new File(".").getAbsoluteFile().getParent() + "/test_target/";
    
    private ClDG[] buildPDGsForTest(ModelBuilderBatch builder, JavaProject jproject) {
        int size = jproject.getClasses().size();
        ClDG[] cldgs = new ClDG[size];
        System.out.println();
        System.out.println("** Building CFGs of " + size + " classes in " + jproject.getName());
        
        int count = 1;
        for (JavaClass jclass : jproject.getClasses()) {
            System.out.print("(" + count + "/" + size + ")");
            cldgs[count - 1] = builder.getClDG(jclass);
            System.out.print(" - " + jclass.getQualifiedName() + " - ClDG\n");
            count++;
        }
        return cldgs;
    }
    
    private boolean checkDetails(ClDG[] ccldgs) {
        for (ClDG cldg : ccldgs) {
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
        ModelBuilderBatch builder = new ModelBuilderBatch(true);
        JavaProject jproject = builder.build(target, target, target, target, target);
        
        ClDG[] cldgs = buildPDGsForTest(builder, jproject);
        checkDetails(cldgs);
        builder.unbuild();
    }
    
    @Test
    public void testDrawTool() {
        String target = testDirInside +  "DrawTool/";
        ModelBuilderBatch builder = new ModelBuilderBatch(true);
        JavaProject jproject = builder.build(target, target, target, target + "/src", target);
        
        buildPDGsForTest(builder, jproject);
        builder.unbuild();
    }
    
    @Test
    public void testLambda() {
        String target = testDirInside +  "Lambda/";
        ModelBuilderBatch builder = new ModelBuilderBatch(true);
        JavaProject jproject = builder.build(target, target, target, target, target);
        
        buildPDGsForTest(builder, jproject);
        builder.unbuild();
    }
    
    @Test
    public void testJrb() {
        String target = testDirInside +  "jrb-1.0.2/";
        ModelBuilderBatch builder = new ModelBuilderBatch(true);
        JavaProject jproject = builder.build(target, target, target, target + "/src", target);
        
        buildPDGsForTest(builder, jproject);
        builder.unbuild();
    }
    
    @Test
    public void testTetris() {
        String target = testDirInside + "Tetris/";
        ModelBuilderBatch builder = new ModelBuilderBatch(true);
        JavaProject jproject = builder.build(target, target, target, target, target);
        
        buildPDGsForTest(builder, jproject);
        builder.unbuild();
    }
    
    @Test
    public void testCSClassroom() {
        String target = testDirInside + "CS-classroom/";
        String classpath = target + "../lib/*";
        ModelBuilderBatch builder = new ModelBuilderBatch(true);
        JavaProject jproject = builder.build(target, target, classpath, target + "/src/", target);
        
        buildPDGsForTest(builder, jproject);
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
    
    private final static String testDirOutside = "/Users/maru/Desktop/TestGenExp/";
    
    private void run(String name) {
        String target = testDirOutside + name + "/";
        
        File dir = new File(target);
        if (!dir.exists()) {
            System.err.println("Not found project " + target);
            return;
        }
        
        ZonedDateTime startTime = TimeInfo.getCurrentTime();
        ModelBuilderBatch builder = new ModelBuilderBatch(true);
        builder.useBytecodeCache(true);
        
        List<JavaProject> jprojects = builder.build(name, target);
        for (JavaProject jproject : jprojects) {
            buildPDGsForTest(builder, jproject);
        }
        ZonedDateTime endTime = TimeInfo.getCurrentTime();
        builder.unbuild();
        
        System.out.println("** Execution time (" + name + ") = " +
                ChronoUnit.MILLIS.between(startTime, endTime) +
                " (" + TimeInfo.getFormatedTime(startTime) + " - " + TimeInfo.getFormatedTime(endTime) + ")");
    }
    
    @SuppressWarnings("unused")
    private void run(String target, String classpath, String srcpath, String binpath) {
        String dir = testDirOutside + target + File.separator;
        run0(target, dir + classpath, dir + srcpath, dir + binpath);
    }
    
    private void run0(String target, String classpath, String srcpath, String binpath) {
        ModelBuilderBatch builder = new ModelBuilderBatch(true);
        JavaProject jproject = builder.build(target, target, classpath, srcpath, binpath);
        
        ZonedDateTime startTime = ZonedDateTime.now();
        buildPDGsForTest(builder, jproject);
        builder.unbuild();
        ZonedDateTime endTime = ZonedDateTime.now();
        System.out.println("** Execution time (" + target + ") = " +
                ChronoUnit.MILLIS.between(startTime, endTime) +
                " (" + TimeInfo.getFormatedTime(startTime) + " - " + TimeInfo.getFormatedTime(endTime) + ")");
    }
    
    public static void main(String[] args) {
        PDGModelBuilderTest tester = new PDGModelBuilderTest();
        
        /* The files are stored inside the workspace */
        //tester.testSimple();
        //tester.testDrawTool();
        //tester.testLambda();
        //tester.testJrb();
        //tester.testTetris();
        //tester.testCSClassroom();
        
        //print();
        
        /* The files are stored outside the workspace */
        tester.run("ant-1.9.14");
        //tester.run("ant-1.10.8");
        //tester.run("antlr4-4.8");
        //tester.run("closure-compiler-v20200614");  // Long time
        //tester.run("commons-bcel-6.5.0");
        //tester.run("commons-cli-1.4");
        //tester.run("commons-codec-1.14");
        //tester.run("commons-collections-4.4");
        //tester.run("commons-compress-1.20");
        //tester.run("commons-csv-1.8");
        //tester.run("commons-jxpath-1.3");
        //tester.run("commons-lang-3.10");
        //tester.run("commons-math-3.6.1");
        //tester.run("findbugs-3.0.1");
        //tester.run("gson-2.8.6");
        //tester.run("jackson-core-2.10.4");
        //tester.run("jackson-databind-2.10.4");
        //tester.run("jackson-dataformat-xml-2.10.4");
        //tester.run("jfreechart-1.5.0");
        //tester.run("joda-time-2.10.6");
        //tester.run("jsoup-1.13.1");
        //tester.run("junit-4.13");
        //tester.run("log4j-2.13.3");
        //tester.run("mockito-3.3.13");
        //tester.run("pmd-6.24.0");
        
            //tester.run("spotbugs-4.0.4");
            //tester.run("guava-29.0");
    }
}
