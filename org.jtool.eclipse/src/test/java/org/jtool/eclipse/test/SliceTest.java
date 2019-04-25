/*
 *  Copyright 2019
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.test;

import org.jtool.eclipse.batch.ModelBuilderBatch;
import org.jtool.eclipse.cfg.JLocalVarReference;
import org.jtool.eclipse.cfg.JReference;
import org.jtool.eclipse.pdg.PDG;
import org.jtool.eclipse.pdg.PDGNode;
import org.jtool.eclipse.pdg.PDGStatement;
import org.jtool.eclipse.pdg.SDG;
import org.jtool.eclipse.slice.Slice;
import org.jtool.eclipse.slice.SliceCriterion;
import org.jtool.eclipse.pdg.ClDG;
import org.jtool.eclipse.cfg.CFGMethodEntry;
import org.jtool.eclipse.javamodel.JavaClass;
import org.jtool.eclipse.javamodel.JavaMethod;
import org.jtool.eclipse.javamodel.JavaProject;
import org.jtool.eclipse.javamodel.CodeRange;
import java.io.File;
import java.util.List;
import java.util.Set;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.Ignore;

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
    
    private SliceCriterion findSliceCriterion(String fqn, int lineNumber, int offset) {
        JavaClass jclass = jproject.getClass(fqn);
        if (jclass == null) {
            return null;
        }
        
        Set<JavaClass> classes = builder.getAllClassesBackward(jclass);
        SDG sdg = builder.getSDG(classes);
        PDG pdg = sdg.getClDG(fqn);
        // pdg.print();
        
        String code = jclass.getFile().getCode();
        return SliceCriterion.find(pdg, code, lineNumber, offset);
    }
    
    @Test
    public void testSlice1() {
        SliceCriterion criterion = findSliceCriterion("Test101", 9, 16); 
        if (criterion != null) {
            Slice slice = new Slice(criterion);
            System.out.println(slice.toString());
        }
    }
    
    @Test
    public void testSlice2() {
        SliceCriterion criterion = findSliceCriterion("Test101", 10, 16); 
        if (criterion != null) {
            Slice slice = new Slice(criterion);
            System.out.println(slice.toString());
        }
    }
    
    @Test
    public void testSlice3() {
        SliceCriterion criterion = findSliceCriterion("Test101", 10, 18); 
        if (criterion != null) {
            Slice slice = new Slice(criterion);
            System.out.println(slice.toString());
        }
    }
    
    @Test
    public void testSlice4() {
        SliceCriterion criterion = findSliceCriterion("Test101", 9, 12); 
        if (criterion != null) {
            Slice slice = new Slice(criterion);
            System.out.println(slice.toString());
        }
    }
    
    @Test
    public void testSlice5() {
        SliceCriterion criterion = findSliceCriterion("Test101", 10, 12); 
        if (criterion != null) {
            Slice slice = new Slice(criterion);
            System.out.println(slice.toString());
        }
    }
    
    @Test
    public void testSlice6() {
        SliceCriterion criterion = findSliceCriterion("Test101", 11, 12); 
        if (criterion != null) {
            Slice slice = new Slice(criterion);
            System.out.println(slice.toString());
        }
    }
    
    @Test
    public void testCustomer1() {
        SliceCriterion criterion = findSliceCriterion("Customer", 27, 31); 
        if (criterion != null) {
            Slice slice = new Slice(criterion);
            System.out.println(slice.toString());
        }
    }
    
    @Test
    public void testCustomer2() {
        SliceCriterion criterion = findSliceCriterion("Customer", 28, 12); 
        if (criterion != null) {
            Slice slice = new Slice(criterion);
            System.out.println(slice.toString());
        }
    }
    
    @Test
    public void testCustomer3() {
        SliceCriterion criterion = findSliceCriterion("Customer", 22, 31); 
        if (criterion != null) {
            //criterion.getPDG().print();
            
            Slice slice = new Slice(criterion);
            System.out.println(slice.toString());
        }
    }
    
    public static void main(String[] args) {
        build();
        
        SliceTest tester = new SliceTest();
        
        tester.testSlice1();
        tester.testSlice2();
        tester.testSlice3();
        tester.testSlice4();
        tester.testSlice5();
        tester.testSlice6();
        
        tester.testCustomer1();
        tester.testCustomer2();
        //tester.testCustomer3();
        
        unbuild();
    }
}
