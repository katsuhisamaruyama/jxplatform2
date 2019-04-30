/*
 *  Copyright 2019
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.test;

import org.jtool.eclipse.batch.ModelBuilderBatch;
import org.jtool.eclipse.pdg.ClDG;
import org.jtool.eclipse.pdg.SDG;
import org.jtool.eclipse.slice.Slice;
import org.jtool.eclipse.slice.SliceCriterion;
import org.jtool.eclipse.javamodel.JavaClass;
import org.jtool.eclipse.javamodel.JavaProject;
import java.io.File;
import java.util.Set;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;

/**
 * Tests a class that extracts a slice.
 * 
 * @author Katsuhisa Maruyama
 */
public class SliceTest {
    
    private static ModelBuilderBatch builder;
    private static JavaProject jproject;
    
    @BeforeClass
    public static void build() {
        String dir = new File(".").getAbsoluteFile().getParent() + "/test_target/";
        String target = dir +  "Slice/";
        builder = new ModelBuilderBatch(true);
        builder.setCreatingActualNodes(true);
        jproject = builder.build(target, target, target);
    }
    
    @AfterClass
    public static void unbuild() {
        builder.unbuild();
    }
    
    private Slice slice(String fqn, int lineNumber, int offset) {
        JavaClass jclass = jproject.getClass(fqn);
        if (jclass == null) {
            return null;
        }
        
        Set<JavaClass> classes = builder.getAllClassesBackward(jclass);
        SDG sdg = builder.getSDG(classes);
        ClDG cldg = sdg.getClDG(fqn);
        cldg.print();
        //cldg.getCFG().print();
        
        String code = jclass.getFile().getCode();
        SliceCriterion criterion = SliceCriterion.find(cldg, code, lineNumber, offset);
        if (criterion != null) {
            return new Slice(criterion);
        }
        return null;
    }
    
    @Test
    public void testSlice101_1() {
        Slice slice = slice("Test101", 5, 12);
        System.out.println(slice.toString());
    }
    
    @Test
    public void testSlice101_2() {
        Slice slice = slice("Test101", 6, 12);
        System.out.println(slice.toString());
    }
    
    @Test
    public void testSlice101_3() {
        Slice slice = slice("Test101", 7, 12);
        System.out.println(slice.toString());
    }
    
    @Test
    public void testSlice102_1() {
        Slice slice = slice("Test102", 7, 12);
        System.out.println(slice.toString());
    }
    
    @Test
    public void testSlice102_2() {
        Slice slice = slice("Test102", 8, 12);
        System.out.println(slice.toString());
    }
    
    @Test
    public void testSlice102_3() {
        Slice slice = slice("Test102", 9, 12);
        System.out.println(slice.toString());
    }
    
    @Test
    public void testSlice102_4() {
        Slice slice = slice("Test102", 10, 27);
        System.out.println(slice.toString());
    }
    
    public void testSlice103_1() {
        Slice slice = slice("Test103", 7, 12);
        System.out.println(slice.toString());
    }
    
    public void testSlice103_2() {
        Slice slice = slice("Test103", 10, 12);
        System.out.println(slice.toString());
    }
    
    public void testSlice103_3() {
        Slice slice = slice("Test103", 11, 12);
        System.out.println(slice.toString());
    }
    
    public void testSlice103_4() {
        Slice slice = slice("Test103", 12, 12);
        System.out.println(slice.toString());
    }
    
    public void testSlice103_5() {
        Slice slice = slice("Test103", 15, 12);
        System.out.println(slice.toString());
    }
    
    @Test
    public void testSlice104_1() {
        Slice slice = slice("Test104", 5, 8);
        System.out.println(slice.toString());
    }
    
    @Test
    public void testSlice104_2() {
        Slice slice = slice("Test104", 6, 8);
        System.out.println(slice.toString());
    }
    
    @Test
    public void testSlice104_3() {
        Slice slice = slice("Test104", 7, 8);
        System.out.println(slice.toString());
    }
    
    @Test
    public void testSlice104_4() {
        Slice slice = slice("Test104", 8, 12);
        System.out.println(slice.toString());
    }
    
    @Test
    public void testSlice104_5() {
        Slice slice = slice("Test104", 9, 8);
        System.out.println(slice.toString());
    }
    
    @Test
    public void testSlice104_6() {
        Slice slice = slice("Test104", 10, 8);
        System.out.println(slice.toString());
    }
    
    @Test
    public void testSlice105_1() {
        Slice slice = slice("Test105", 6, 12);
        System.out.println(slice.toString());
    }
    
    @Test
    public void testSlice105_2() {
        Slice slice = slice("Test105", 7, 12);
        System.out.println(slice.toString());
    }
    
    @Test
    public void testSlice106_1() {
        Slice slice = slice("Test106", 7, 12);
        System.out.println(slice.toString());
    }
    
    @Test
    public void testSlice106_2() {
        Slice slice = slice("Test106", 8, 12);
        System.out.println(slice.toString());
    }
    
    @Test
    public void testSlice107_1() {
        Slice slice = slice("Test107",7, 12);
        System.out.println(slice.toString());
    }
    
    @Test
    public void testSlice108_1() {
        Slice slice = slice("Test108", 12, 12);
        System.out.println(slice.toString());
    }
    
    @Test
    public void testSlice108_2() {
        Slice slice = slice("Test108", 13, 12);
        System.out.println(slice.toString());
    }
    
    @Test
    public void testSlice109_1() {
        Slice slice = slice("Test109", 11, 12);
        System.out.println(slice.toString());
    }
    
    @Test
    public void testSlice109_2() {
        Slice slice = slice("Test109", 12, 12);
        System.out.println(slice.toString());
    }
    
    @Test
    public void testSlice110_1() {
        Slice slice = slice("Test110", 9, 12);
        System.out.println(slice.toString());
    }
    
    @Test
    public void testSlice110_2() {
        Slice slice = slice("Test110", 10, 12);
        System.out.println(slice.toString());
    }
    
    @Test
    public void testSlice110_3() {
        Slice slice = slice("Test110", 7, 16);
        System.out.println(slice.toString());
    }
    
    @Test
    public void testSlice111_1() {
        Slice slice = slice("Test111", 15, 12);
        System.out.println(slice.toString());
    }
    
    @Test
    public void testSlice111_2() {
        Slice slice = slice("Test111", 16, 12);
        System.out.println(slice.toString());
    }
    
    @Test
    public void testSlice112_1() {
        Slice slice = slice("Test112", 17, 12);
        System.out.println(slice.toString());
    }
    
    @Test
    public void testSlice112_2() {
        Slice slice = slice("Test112", 18, 12);
        System.out.println(slice.toString());
    }
    
    @Test
    public void testSlice112_3() {
        Slice slice = slice("Test112", 19, 12);
        System.out.println(slice.toString());
    }
    
    @Test
    public void testSlice113_1() {
        Slice slice = slice("Test113", 12, 12); 
        System.out.println(slice.toString());
    }
    
    @Test
    public void testSlice113_2() {
        Slice slice = slice("Test113", 13, 12); 
        System.out.println(slice.toString());
    }
    
    @Test
    public void testSlice114_1() {
        Slice slice = slice("Test114", 12, 12); 
        System.out.println(slice.toString());
    }
    
    @Test
    public void testSlice114_2() {
        Slice slice = slice("Test114", 13, 12); 
        System.out.println(slice.toString());
    }
    
    @Test
    public void testSlice115_1() {
        Slice slice = slice("Test115", 11, 12); 
        System.out.println(slice.toString());
    }
    
    @Test
    public void testSlice115_2() {
        Slice slice = slice("Test115", 12, 12); 
        System.out.println(slice.toString());
    }
    
    @Test
    public void testSlice116_1() {
        Slice slice = slice("Test116", 15, 12); 
        System.out.println(slice.toString());
    }
    
    @Test
    public void testSlice116_2() {
        Slice slice = slice("Test116", 16, 12); 
        System.out.println(slice.toString());
    }
    
    @Test
    public void testSlice117_1() {
        Slice slice = slice("Test117", 9, 12); 
        System.out.println(slice.toString());
    }
    
    @Test
    public void testSlice117_2() {
        Slice slice = slice("Test117", 10, 12); 
        System.out.println(slice.toString());
    }
    
    @Test
    public void testSlice118_1() {
        Slice slice = slice("Test118", 4, 12); 
        System.out.println(slice.toString());
    }
    
    @Test
    public void testSlice118_2() {
        Slice slice = slice("Test118", 5, 12); 
        System.out.println(slice.toString());
    }
    
    @Test
    public void testSlice118_3() {
        Slice slice = slice("Test118", 6, 12); 
        System.out.println(slice.toString());
    }
    
    @Test
    public void testSlice118_4() {
        Slice slice = slice("Test118", 7, 12); 
        System.out.println(slice.toString());
    }
    
    @Test
    public void testSlice118_5() {
        Slice slice = slice("Test118", 8, 12); 
        System.out.println(slice.toString());
    }
    
    @Test
    public void testSlice118_6() {
        Slice slice = slice("Test118", 9, 12); 
        System.out.println(slice.toString());
    }
    
    @Test
    public void testSlice118_7() {
        Slice slice = slice("Test118", 10, 12); 
        System.out.println(slice.toString());
    }
    
    @Test
    public void testSlice118_8() {
        Slice slice = slice("Test118", 11, 12); 
        System.out.println(slice.toString());
    }
    
    @Test
    public void testSlice119_1() {
        Slice slice = slice("Test119", 9, 16); 
        System.out.println(slice.toString());
    }
    
    @Test
    public void testSlice119_2() {
        Slice slice = slice("Test119", 10, 16); 
        System.out.println(slice.toString());
    }
    
    @Test
    public void testSlice119_3() {
        Slice slice = slice("Test119", 10, 18); 
        System.out.println(slice.toString());
    }
    
    @Test
    public void testSlice119_4() {
        Slice slice = slice("Test119", 9, 12); 
        System.out.println(slice.toString());
    }
    
    @Test
    public void testSlice119_5() {
        Slice slice = slice("Test119", 10, 12); 
        System.out.println(slice.toString());
    }
    
    @Test
    public void testSlice119_6() {
        Slice slice = slice("Test119", 11, 12); 
        System.out.println(slice.toString());
    }
    
    @Test
    public void testSlice120_1() {
        Slice slice = slice("Test120", 5, 12); 
        System.out.println(slice.toString());
    }
    
    @Test
    public void testSlice120_2() {
        Slice slice = slice("Test120", 6, 12); 
        System.out.println(slice.toString());
    }
    
    @Test
    public void testSlice120_3() {
        Slice slice = slice("Test120", 7, 12); 
        System.out.println(slice.toString());
    }
    
    @Test
    public void testSlice120_4() {
        Slice slice = slice("Test120", 8, 12); 
        System.out.println(slice.toString());
    }
    
    @Test
    public void testCustomer1() {
        Slice slice = slice("Customer", 22, 12); 
        System.out.println(slice.toString());
    }
    
    @Test
    public void testCustomer2() {
        Slice slice = slice("Customer", 27, 31); 
        System.out.println(slice.toString());
    }
    
    @Test
    public void testCustomer3() {
        Slice slice = slice("Customer", 28, 12); 
        System.out.println(slice.toString());
    }
    
    @Test
    public void testCustomer4() {
        Slice slice = slice("Customer", 22, 31); 
        System.out.println(slice.toString());
    }
    
    public static void main(String[] args) {
        build();
        
        SliceTest tester = new SliceTest();
        
        tester.testSlice101_1();
        tester.testSlice101_2();
        tester.testSlice101_3();
        
        tester.testSlice102_1();
        tester.testSlice102_2();
        tester.testSlice102_3();
        tester.testSlice102_4();
        
        tester.testSlice103_1();
        tester.testSlice103_2();
        tester.testSlice103_3();
        tester.testSlice103_4();
        tester.testSlice103_5();
        
        tester.testSlice104_1();
        tester.testSlice104_2();
        tester.testSlice104_3();
        tester.testSlice104_4();
        tester.testSlice104_5();
        tester.testSlice104_6();
        
        tester.testSlice105_1();
        tester.testSlice105_2();
        
        tester.testSlice106_1();
        tester.testSlice106_2();
        
        tester.testSlice107_1();
        
        tester.testSlice108_1();
        tester.testSlice108_2();
        
        tester.testSlice109_1();
        tester.testSlice109_2();
        
        tester.testSlice110_1();
        tester.testSlice110_2();
        tester.testSlice110_3();
        
        tester.testSlice111_1();
        tester.testSlice111_2();
        
        tester.testSlice112_1();
        tester.testSlice112_2();
        tester.testSlice112_3();
        
        tester.testSlice113_1();
        tester.testSlice113_2();
        
        tester.testSlice114_1();
        tester.testSlice114_2();
        
        tester.testSlice115_1();
        tester.testSlice115_2();
        
        tester.testSlice116_1();
        tester.testSlice116_2();
        tester.testSlice117_1();
        tester.testSlice117_2();
        
        tester.testSlice118_1();
        tester.testSlice118_2();
        tester.testSlice118_3();
        tester.testSlice118_4();
        tester.testSlice118_5();
        tester.testSlice118_6();
        tester.testSlice118_7();
        tester.testSlice118_8();
        
        tester.testSlice119_1();
        tester.testSlice119_2();
        tester.testSlice119_3();
        tester.testSlice119_4();
        tester.testSlice119_5();
        tester.testSlice119_6();
        
        tester.testSlice120_1();
        tester.testSlice120_2();
        tester.testSlice120_3();
        tester.testSlice120_4();
        
        tester.testCustomer1();
        tester.testCustomer2();
        tester.testCustomer3();
        tester.testCustomer4();
        
        unbuild();
    }
}
