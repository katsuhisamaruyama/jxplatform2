package org.jtool.eclipse.test;
/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

import org.jtool.eclipse.standalone.JavaMetricsCalculator;
//import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 * Tests a class that calculates.
 * @author Katsuhisa Maruyama
 */
public class JavaMetricsCalculatorTest {
    
    private final String TEST_PROECT_DIR = "/Users/maru/Desktop/TestSamples/";
    
    @Test
    public void testSimple() {
        String target = TEST_PROECT_DIR + "Simple/";
        JavaMetricsCalculator calculator = new JavaMetricsCalculator("Simple", target);
        calculator.run();
    }
    
    @Test
    public void testTetris() {
        String target = TEST_PROECT_DIR + "Tetris/";
        JavaMetricsCalculator calculator = new JavaMetricsCalculator("Tetris", target);
        calculator.run();
    }
    
    @Test
    public void testDrawTool() {
        String target = TEST_PROECT_DIR + "DrawTool/";
        JavaMetricsCalculator calculator = new JavaMetricsCalculator("DrawTool", target);
        calculator.run();
    }
    
    @Test
    public void testCSSample() {
        String target = TEST_PROECT_DIR + "CS-Sample/";
        String classpath = "/Users/maru/Desktop/TestSamples/lib/*";
        JavaMetricsCalculator calculator = new JavaMetricsCalculator("CS-Sample", target, classpath);
        calculator.run();
    }
    
    @Test
    public void testFindbugs() {
        String target = TEST_PROECT_DIR + "findbugs/";
        String classpath= "/Users/maru/Desktop/TestSamples/findbugs/lib/*";
        JavaMetricsCalculator calculator = new JavaMetricsCalculator("findbugs", target, classpath);
        calculator.run();
    }
    
    @Test
    public void testApacheAnt() {
        String target = TEST_PROECT_DIR + "apache-ant/";
        String classpath = TEST_PROECT_DIR + "apache-ant/lib/*";
        JavaMetricsCalculator calculator = new JavaMetricsCalculator("apache-ant", target, classpath);
        calculator.run();
    }
    
    @Test
    public void testJdk8() {
        String target = TEST_PROECT_DIR + "jdk1.8.0_131/";
        String classpath = TEST_PROECT_DIR + "jdk1.8.0_131/lib/*";
        JavaMetricsCalculator calculator = new JavaMetricsCalculator("jdk1.8.0_131", target, classpath);
        calculator.run();
    }
    
    public static void main(String[] args) {
        JavaMetricsCalculatorTest tester = new JavaMetricsCalculatorTest();
        tester.testSimple();
        tester.testTetris();
        tester.testDrawTool();
        tester.testCSSample();
        tester.testFindbugs();
        tester.testApacheAnt();
        //tester.testJdk8();
    }
}
