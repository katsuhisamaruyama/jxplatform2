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
    
    @Test
    public void testSimple() {
        String target = "/Users/maru/Desktop/TestSamples/Simple/";
        JavaMetricsCalculator calculator = new JavaMetricsCalculator(target, target);
        calculator.run();
    }
    
    @Test
    public void testJrb() {
        String target = "/Users/maru/Desktop/TestSamples/jrb-1.0.2/src/";
        JavaMetricsCalculator calculator = new JavaMetricsCalculator(target, target);
        calculator.run();
    }
    
    @Test
    public void testTetris() {
        String target = "/Users/maru/Desktop/TestSamples/Tetris/src";
        JavaMetricsCalculator calculator = new JavaMetricsCalculator(target, target);
        calculator.run();
    }
    
    @Test
    public void testDrawTool() {
        String target = "/Users/maru/Desktop/TestSamples/DrawTool/src";
        JavaMetricsCalculator calculator = new JavaMetricsCalculator(target, target);
        calculator.run();
    }
    
    @Test
    public void testLambda() {
        String target = "/Users/maru/Desktop/TestSamples/Lambda/";
        JavaMetricsCalculator calculator = new JavaMetricsCalculator(target, target);
        calculator.run();
    }
    
    @Test
    public void testCSSample() {
        String target = "/Users/maru/Desktop/TestSamples/CS-Sample/";
        String classpath = "/Users/maru/Desktop/TestSamples/lib/*";
        JavaMetricsCalculator calculator = new JavaMetricsCalculator(target, target, classpath);
        calculator.run();
    }
    
    @Test
    public void testFindbugs() {
        String target = "/Users/maru/Desktop/TestSamples/findbugs/src/";
        String classpath= "/Users/maru/Desktop/TestSamples/findbugs/lib/*";
        JavaMetricsCalculator calculator = new JavaMetricsCalculator(target, target, classpath);
        calculator.run();
    }
    
    @Test
    public void testApacheAnt() {
        String target = "/Users/maru/Desktop/TestSamples/apache-ant/src/";
        String classpath = "/Users/maru/Desktop/TestSamples/apache-ant/lib/*";
        JavaMetricsCalculator calculator = new JavaMetricsCalculator(target, target, classpath);
        calculator.run();
    }
    
    @Test
    public void testJdk8() {
        String target = "/Users/maru/Desktop/TestSamples/jdk1.8.0_131/src/";
        String classpath = "/Users/maru/Desktop/TestSamples/jdk1.8.0_131/lib/*";
        JavaMetricsCalculator calculator = new JavaMetricsCalculator(target, target, classpath);
        calculator.run();
    }
    
    public static void main(String[] args) {
        JavaMetricsCalculatorTest tester = new JavaMetricsCalculatorTest();
        //tester.testSimple();
        //tester.testJrb();
        //tester.testTetris();
        //tester.testDrawTool();
        //tester.testLambda();
        //testCSSample();
        tester.testFindbugs();
        //tester.testApacheAnt();
        //tester.testJdk8();
    }
}
