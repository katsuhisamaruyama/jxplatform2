/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.test;

import org.jtool.eclipse.batch.ModelBuilderBatch;
import org.jtool.eclipse.cfg.JLocalReference;
import org.jtool.eclipse.pdg.PDG;
import org.jtool.eclipse.pdg.ClDG;
import org.jtool.eclipse.cfg.CFGMethodEntry;
import org.jtool.eclipse.javamodel.JavaClass;
import org.jtool.eclipse.javamodel.JavaMethod;
import org.jtool.eclipse.javamodel.JavaProject;
import org.jtool.eclipse.slice.JCodeFormatter;
import org.jtool.eclipse.slice.RenameLocalVariable;
import java.util.List;
import org.junit.Test;

/**
 * Tests a class that builds a PDG.
 * Please change the constant value of TEST_PROECT_DIR according to your environment.
 * 
 * @author Katsuhisa Maruyama
 */
public class SliceTest {
    
    private final String TEST_PROECT_DIR = "/Users/maru/Desktop/TestSamples/";
    
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
    
    @Test
    public void testRename() {
        String target = TEST_PROECT_DIR + "Slice/";
        ModelBuilderBatch builder = new ModelBuilderBatch(false);
        JavaProject jproject = builder.build(target, target, target);
        
        builder.setCreatingActualNodes(false);
        ClDG[] cldgs = buildPDGsForTest(builder, jproject.getClasses());
        rename(cldgs);
        
        builder.unbuild();
    }
    
    private void rename(ClDG[] cldgs) {
        for (ClDG cldg : cldgs) {
            for (PDG pdg : cldg.getPDGs()) {
                pdg.print();
                
                CFGMethodEntry entry = (CFGMethodEntry)pdg.getCFG().getStartNode();
                JavaMethod jmethod = entry.getJavaMethod();
                
                JLocalReference var = (JLocalReference)entry.getFormalIn(0).getDefVariable();
                RenameLocalVariable rename = new RenameLocalVariable(jmethod, var, "ABC");
                rename.perform();
                
                String code = JCodeFormatter.format(jmethod);
                System.out.println(code);
            }
        }
    }
    
    public static void main(String[] args) {
        SliceTest tester = new SliceTest();
        tester.testRename();
    }
}
