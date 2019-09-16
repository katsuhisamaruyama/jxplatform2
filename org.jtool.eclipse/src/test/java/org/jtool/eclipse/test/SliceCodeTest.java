/*
 *  Copyright 2019
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.test;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.jtool.eclipse.batch.ModelBuilderBatch;
import org.jtool.eclipse.pdg.ClDG;
import org.jtool.eclipse.pdg.SDG;
import org.jtool.eclipse.slice.Slice;
import org.jtool.eclipse.slice.SliceCriterion;
import org.jtool.eclipse.slice.SliceExtractor;
import org.jtool.eclipse.javamodel.JavaProject;
import org.jtool.eclipse.javamodel.JavaClass;
import org.jtool.eclipse.javamodel.JavaMethod;
import java.io.File;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import static org.junit.Assert.assertEquals;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;

/**
 * Tests a class that extracts a slice.
 * 
 * @author Katsuhisa Maruyama
 */
public class SliceCodeTest {
    
    private static ModelBuilderBatch builder;
    private static JavaProject jproject;
    
    @BeforeClass
    public static void build() {
        String dir = new File(".").getAbsoluteFile().getParent() + "/test_target/";
        String target = dir +  "Slice/";
        builder = new ModelBuilderBatch(true);
        jproject = builder.build(target, target, target, target, target);
    }
    
    @AfterClass
    public static void unbuild() {
        builder.unbuild();
    }
    
    private Slice slice(JavaClass jclass, int lineNumber, int offset) {
        if (jclass == null) {
            return null;
        }
        
        Set<JavaClass> classes = builder.getAllClassesBackward(jclass);
        SDG sdg = builder.getSDG(classes);
        ClDG cldg = sdg.getClDG(jclass.getQualifiedName());
        cldg.getCFG().print();
        sdg.print();
        
        String code = jclass.getFile().getCode();
        SliceCriterion criterion = SliceCriterion.find(cldg, code, lineNumber, offset);
        if (criterion != null) {
            return new Slice(criterion);
        }
        return null;
    }
    
    private String getSlicedCode(String fqn, int lineNumber, int offset) {
        JavaClass jclass = jproject.getClass(fqn);
        if (jclass != null) {
            Slice slice = slice(jclass, lineNumber, offset);
            if (slice != null) {
                System.out.println(slice.toString());
                
                Map<String, String> options = new HashMap<String, String>();
                options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.SPACE);
                options.put(DefaultCodeFormatterConstants.FORMATTER_INDENTATION_SIZE, "4");
                
                SliceExtractor extractor = new SliceExtractor(builder, slice, jclass);
                String code = extractor.extract(options);
                return code;
            }
        }
        return "Failed";
    }
    
    private String getSlicedCode(String fqn, String sig, int lineNumber, int offset) {
        JavaClass jclass = jproject.getClass(fqn);
        if (jclass != null) {
            JavaMethod jmethod = jclass.getMethod(sig);
            
            Slice slice = slice(jclass, lineNumber, offset); 
            if (slice != null) {
                System.out.println(slice.toString());
                
                SliceExtractor extractor = new SliceExtractor(builder, slice, jmethod);
                String code = extractor.extract();
                return code;
            }
        }
        return "Failed";
    }
    
    @Test
    public void testSlice101_1() {
        String code = getSlicedCode("Test101", 5, 12);
        //System.out.println(code);
        String expected = 
                "class Test101 {\n" + 
                "    public void m() {\n" + 
                "        int x = 10;\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testSlice101_1m() {
        String code = getSlicedCode("Test101", "m( )", 5, 12);
        //System.out.println(code);
        String expected = 
                "\n" +
                "public void m() {\n" + 
                "    int x = 10;\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testSlice101_2() {
        String code = getSlicedCode("Test101", 6, 12);
        //System.out.println(code);
        String expected = 
                "class Test101 {\n" + 
                "    public void m() {\n" + 
                "        int x = 10;\n" + 
                "        int y = x + 1;\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testSlice101_3() {
        String code = getSlicedCode("Test101", 7, 12);
        //System.out.println(code);
        String expected = 
                "class Test101 {\n" + 
                "    private int p = 1;\n" + 
                "    public void m() {\n" + 
                "        int x = 10;\n" + 
                "        int z = x + p;\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testSlice102_1() {
        String code = getSlicedCode("Test102", 7, 12);
        //System.out.println(code);
        String expected = 
                "class Test102 {\n" + 
                "    public void m() {\n" + 
                "        int x = inc(10);\n" + 
                "        int p = x;\n" + 
                "    }\n" + 
                "    public int inc(int n) {\n" + 
                "        return n + 1;\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testSlice102_2() {
        String code = getSlicedCode("Test102", 8, 12);
        //System.out.println(code);
        String expected = 
                "class Test102 {\n" + 
                "    public void m() {\n" + 
                "        int y = 0;\n" + 
                "        int q = y;\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testSlice102_3() {
        String code = getSlicedCode("Test102", 9, 12);
        //System.out.println(code);
        String expected = 
                "class Test102 {\n" + 
                "    public void m() {\n" + 
                "        int y = 0;\n" + 
                "        int z = inc(y);\n" + 
                "        int r = z;\n" + 
                "    }\n" + 
                "    public int inc(int n) {\n" + 
                "        return n + 1;\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testSlice102_4() {
        String code = getSlicedCode("Test102", 10, 27);
        //System.out.println(code);
        String expected = 
                "class Test102 {\n" + 
                "    public void m() {\n" + 
                "        int y = 0;\n" + 
                "        int z = inc(y);\n" + 
                "    }\n" + 
                "    public int inc(int n) {\n" + 
                "        return n + 1;\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testSlice103_1() {
        String code = getSlicedCode("Test103", 7, 12);
        //System.out.println(code);
        String expected = 
                "class Test103 {\n" + 
                "    private int a;\n" + 
                "    public void m() {\n" + 
                "        a = 2;\n" + 
                "        int p = a;\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testSlice103_2() {
        String code = getSlicedCode("Test103", 10, 12);
        //System.out.println(code);
        String expected = 
                "class Test103 {\n" + 
                "    private int a;\n" + 
                "    public void m() {\n" + 
                "        setA(2);\n" + 
                "        int q = a;\n" + 
                "    }\n" + 
                "    private void setA(int a) {\n" + 
                "        this.a = a;\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testSlice103_3() {
        String code = getSlicedCode("Test103", 11, 12);
        //System.out.println(code);
        String expected = 
                "class Test103 {\n" + 
                "    private int a;\n" + 
                "    public void m() {\n" + 
                "        setA(2);\n" + 
                "        int r = getA();\n" + 
                "    }\n" + 
                "    private void setA(int a) {\n" + 
                "        this.a = a;\n" + 
                "    }\n" + 
                "    private int getA() {\n" + 
                "        return a;\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testSlice103_4() {
        String code = getSlicedCode("Test103", 12, 12);
        //System.out.println(code);
        String expected = 
                "class Test103 {\n" + 
                "    private int a;\n" + 
                "    public void m() {\n" + 
                "        setA(2);\n" + 
                "        int s = getA();\n" + 
                "    }\n" + 
                "    private void setA(int a) {\n" + 
                "        this.a = a;\n" + 
                "    }\n" + 
                "    private int getA() {\n" + 
                "        return a;\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testSlice103_5() {
        String code = getSlicedCode("Test103", 15, 12);
        //System.out.println(code);
        String expected = 
                "class Test103 {\n" + 
                "    private int a;\n" + 
                "    public void m() {\n" + 
                "        setA(2);\n" + 
                "        incA();\n" + 
                "        incA();\n" + 
                "        int t = getA();\n" + 
                "    }\n" + 
                "    private void setA(int a) {\n" + 
                "        this.a = a;\n" + 
                "    }\n" + 
                "    private int getA() {\n" + 
                "        return a;\n" + 
                "    }\n" + 
                "    private void incA() {\n" + 
                "        a++;\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testSlice104_1() {
        String code = getSlicedCode("Test104", 5, 8);
        //System.out.println(code);
        String expected = 
                "class Test104 {\n" + 
                "    public void m() {\n" + 
                "        int x;\n" + 
                "        x = 10;\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testSlice104_2() {
        String code = getSlicedCode("Test104", 6, 8);
        //System.out.println(code);
        String expected = 
                "class Test104 {\n" + 
                "    public void m() {\n" + 
                "        int y = 1;\n" + 
                "        y = 20;\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testSlice104_3() {
        String code = getSlicedCode("Test104", 7, 8);
        //System.out.println(code);
        String expected = 
                "class Test104 {\n" + 
                "    public void m() {\n" + 
                "        int z;\n" + 
                "        z = 30;\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testSlice104_4() {
        String code = getSlicedCode("Test104", 8, 12);
        //System.out.println(code);
        String expected = 
                "class Test104 {\n" + 
                "    public void m() {\n" + 
                "        int y = 1, z;\n" + 
                "        y = 20;\n" + 
                "        z = 30;\n" + 
                "        int p = y + z;\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testSlice104_5() {
        String code = getSlicedCode("Test104", 9, 8);
        //System.out.println(code);
        String expected = 
                "class Test104 {\n" + 
                "    public void m() {\n" + 
                "        int x, y = 1;\n" + 
                "        x = 10;\n" + 
                "        y = x + 1;\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testSlice104_6() {
        String code = getSlicedCode("Test104", 10, 8);
        //System.out.println(code);
        String expected = 
                "class Test104 {\n" + 
                "    public void m() {\n" + 
                "        int x, z;\n" + 
                "        x = 10;\n" + 
                "        z = x + 2;\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testSlice105_1() {
        String code = getSlicedCode("Test105", 6, 12);
        //System.out.println(code);
        String expected = 
                "class Test105 {\n" + 
                "    public void m() {\n" + 
                "        int x = setA(1);\n" + 
                "        int y = x;\n" + 
                "    }\n" + 
                "    private int setA(int a) {\n" + 
                "        return a;\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testSlice105_2() {
        String code = getSlicedCode("Test105", 7, 12);
        //System.out.println(code);
        String expected = 
                "class Test105 {\n" + 
                "    private int a;\n" + 
                "    public void m() {\n" + 
                "        setA(1);\n" + 
                "        int z = a;\n" + 
                "    }\n" + 
                "    private int setA(int a) {\n" + 
                "        this.a = a;\n" + 
                "        return 0;\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testSlice106_1() {
        String code = getSlicedCode("Test106", 7, 12);
        //System.out.println(code);
        String expected = 
                "class Test106 {\n" + 
                "    public void m() {\n" + 
                "        int x;\n" + 
                "        x = setA(1);\n" + 
                "        int y = x;\n" + 
                "    }\n" + 
                "    private int setA(int a) {\n" + 
                "        return a;\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testSlice106_2() {
        String code = getSlicedCode("Test106", 8, 12);
        //System.out.println(code);
        String expected = 
                "class Test106 {\n" + 
                "    private int a;\n" + 
                "    public void m() {\n" + 
                "        setA(1);\n" + 
                "        int z = a;\n" + 
                "    }\n" + 
                "    private int setA(int a) {\n" + 
                "        this.a = a;\n" + 
                "        return 0;\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testSlice107_1() {
        String code = getSlicedCode("Test107", 7, 12);
        //System.out.println(code);
        String expected = 
                "class Test107 {\n" + 
                "    public void m() {\n" + 
                "        int i = 0;\n" + 
                "        a[i++] = 2;\n" + 
                "        int j = i;\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testSlice108_1() {
        String code = getSlicedCode("Test108", 12, 12);
        //System.out.println(code);
        String expected = 
                "class Test108 {\n" + 
                "    public void m() {\n" + 
                "        int x = 10;\n" + 
                "        int y = 0;;\n" + 
                "        if (x > 10) {\n" + 
                "            y++;\n" + 
                "        } else {\n" + 
                "        }\n" + 
                "        int p = y;\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testSlice108_2() {
        String code = getSlicedCode("Test108", 13, 12);
        //System.out.println(code);
        String expected = 
                "class Test108 {\n" + 
                "    public void m() {\n" + 
                "        int x = 10;\n" + 
                "        int z = 0;;\n" + 
                "        if (x > 10) {\n" + 
                "        } else {\n" + 
                "            z = x + 2;\n" + 
                "        }\n" + 
                "        int q = z;\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testSlice109_1() {
        String code = getSlicedCode("Test109", 11, 12);
        //System.out.println(code);
        String expected =
                "class Test109 {\n" + 
                "    public void m() {\n" + 
                "        int x = 10;\n" + 
                "        int y = 0;\n" + 
                "        if (x > 10)\n" + 
                "            y++;\n" + 
                "        int p = y;\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testSlice109_2() {
        String code = getSlicedCode("Test109", 12, 12);
        //System.out.println(code);
        String expected = 
                "class Test109 {\n" + 
                "    public void m() {\n" + 
                "        int x = 10;\n" + 
                "        int z = 0;\n" + 
                "        if (x > 10)\n" + 
                "            ;\n" + 
                "        else\n" + 
                "            z = x + 2;\n" + 
                "        int q = z;\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testSlice110_1() {
        String code = getSlicedCode("Test110", 9, 12);
        //System.out.println(code);
        String expected = 
                "class Test110 {\n" + 
                "    public void m() {\n" + 
                "        int x = 0;\n" + 
                "        while (x < 10) {\n" + 
                "            x = x + 1;\n" + 
                "        }\n" + 
                "        int p = x;\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testSlice110_2() {
        String code = getSlicedCode("Test110", 10, 12);
        //System.out.println(code);
        String expected = 
                "class Test110 {\n" + 
                "    public void m() {\n" + 
                "        int y = 0;\n" + 
                "        int q = y;\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testSlice110_3() {
        String code = getSlicedCode("Test110", 7, 16);
        //System.out.println(code);
        String expected = 
                "class Test110 {\n" + 
                "    public void m() {\n" + 
                "        int x = 0;\n" + 
                "        while (x < 10) {\n" + 
                "            x = x + 1;\n" + 
                "        }\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testSlice111_1() {
        String code = getSlicedCode("Test111", 15, 12);
        //System.out.println(code);
        String expected = 
                "class Test111 {\n" + 
                "    public void m() {\n" + 
                "        int x = 0;\n" + 
                "        int y = 0;\n" + 
                "        switch (x) {\n" + 
                "            case 1 :\n" + 
                "                y = 10;\n" + 
                "                break;\n" + 
                "            case 2 :\n" + 
                "                break;\n" + 
                "        }\n" + 
                "        int p = y;\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testSlice111_2() {
        String code = getSlicedCode("Test111", 16, 12);
        //System.out.println(code);
        String expected = 
                "class Test111 {\n" + 
                "    public void m() {\n" + 
                "        int x = 0;\n" + 
                "        int z = 0;\n" + 
                "        switch (x) {\n" + 
                "            case 1 :\n" + 
                "                break;\n" + 
                "            case 2 :\n" + 
                "                z = 20;\n" + 
                "                break;\n" + 
                "        }\n" + 
                "        int q = z;\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testSlice112_1() {
        String code = getSlicedCode("Test112", 17, 12);
        //System.out.println(code);
        String expected =
                "class Test112 {\n" + 
                "    public void m() {\n" + 
                "        int x = 0;\n" + 
                "        int y = 0;\n" + 
                "        switch (x) {\n" + 
                "            default :\n" + 
                "            case 1 :\n" + 
                "                y = 10;\n" + 
                "                break;\n" + 
                "            case 2 :\n" + 
                "                break;\n" + 
                "        }\n" + 
                "        int p = y;\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testSlice112_2() {
        String code = getSlicedCode("Test112", 18, 12);
        //System.out.println(code);
        String expected =
                "class Test112 {\n" + 
                "    public void m() {\n" + 
                "        int x = 0;\n" + 
                "        int z = 0;\n" + 
                "        switch (x) {\n" + 
                "            default :\n" + 
                "            case 1 :\n" + 
                "                break;\n" + 
                "            case 2 :\n" + 
                "                z = 20;\n" + 
                "                break;\n" + 
                "        }\n" + 
                "        int q = z;\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testSlice112_3() {
        String code = getSlicedCode("Test112", 19, 12);
        //System.out.println(code);
        String expected = 
                "class Test112 {\n" + 
                "    public void m() {\n" + 
                "        int x = 0;\n" + 
                "        switch (x) {\n" + 
                "            default :\n" + 
                "                x = 10;\n" + 
                "            case 1 :\n" + 
                "                break;\n" + 
                "            case 2 :\n" + 
                "                break;\n" + 
                "        }\n" + 
                "        int r = x;\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testSlice113_1() {
        String code = getSlicedCode("Test113", 12, 12);
        //System.out.println(code);
        String expected = 
                "class Test113 {\n" + 
                "    public void m() {\n" + 
                "        int x = 0;\n" + 
                "        if (x == 0) {\n" + 
                "            while (x < 10) {\n" + 
                "                x = x + 1;\n" + 
                "            }\n" + 
                "        }\n" + 
                "        int p = x;\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testSlice113_2() {
        String code = getSlicedCode("Test113", 13, 12);
        //System.out.println(code);
        String expected = 
                "class Test113 {\n" + 
                "    public void m() {\n" + 
                "        int x = 0;\n" + 
                "        int y = 0;\n" + 
                "        if (x == 0) {\n" + 
                "            y = 10;\n" + 
                "        }\n" + 
                "        int q = y;\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testSlice114_1() {
        String code = getSlicedCode("Test114", 12, 12);
        //System.out.println(code);
        String expected = 
                "class Test114 {\n" + 
                "    public void m() {\n" + 
                "        int x = 0;\n" + 
                "        if (x == 0)\n" + 
                "            while (x < 10) {\n" + 
                "                x = x + 1;\n" + 
                "            }\n" + 
                "        int p = x;\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testSlice114_2() {
        String code = getSlicedCode("Test114", 13, 12);
        //System.out.println(code);
        String expected = 
                "class Test114 {\n" + 
                "    public void m() {\n" + 
                "        int x = 0;\n" + 
                "        int y = 0;\n" + 
                "        if (x == 0)\n" + 
                "            ;\n" + 
                "        else\n" + 
                "            y = 10;\n" + 
                "        int q = y;\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testSlice115_1() {
        String code = getSlicedCode("Test115", 11, 12);
        //System.out.println(code);
        String expected = 
                "class Test115 {\n" + 
                "    public void m() {\n" + 
                "        int[] a = {1, 2, 3, 4, 5};\n" + 
                "        int x = 0;\n" + 
                "        for (int i = 0; i < 5; i++) {\n" + 
                "            x = x + a[i];\n" + 
                "        }\n" + 
                "        int p = x;\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testSlice115_2() {
        String code = getSlicedCode("Test115", 12, 12);
        //System.out.println(code);
        String expected = 
                "class Test115 {\n" + 
                "    public void m() {\n" + 
                "        int[] a = {1, 2, 3, 4, 5};\n" + 
                "        int y = 1;\n" + 
                "        for (int i = 0; i < 5; i++) {\n" + 
                "            y = y * a[i];\n" + 
                "        }\n" + 
                "        int q = y;\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testSlice116_1() {
        String code = getSlicedCode("Test116", 15, 12);
        //System.out.println(code);
        String expected = 
                "class Test116 {\n" + 
                "    public void m() {\n" + 
                "        int[] a = {1, 2, 3, 4, 5};\n" + 
                "        int x = 0;\n" + 
                "        for (int i = 0; i < 5; i++) {\n" + 
                "            if (x > 2) {\n" + 
                "                x += a[i];\n" + 
                "            }\n" + 
                "        }\n" + 
                "        int p = x;\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testSlice116_2() {
        String code = getSlicedCode("Test116", 16, 12);
        //System.out.println(code);
        String expected = 
                "class Test116 {\n" + 
                "    public void m() {\n" + 
                "        int[] a = {1, 2, 3, 4, 5};\n" + 
                "        int y = 1;\n" + 
                "        for (int i = 0; i < 5; i++) {\n" + 
                "            if (y > 3) {\n" + 
                "                y *= a[i];\n" + 
                "            }\n" + 
                "        }\n" + 
                "        int q = y;\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testSlice117_1() {
        String code = getSlicedCode("Test117", 9, 12);
        //System.out.println(code);
        String expected = 
                "class Test117 {\n" + 
                "    public void m() {\n" + 
                "        int x = 0;\n" + 
                "        for (int i = 0; i < 5; i++, x++) {\n" + 
                "        }\n" + 
                "        int p = x;\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testSlice117_2() {
        String code = getSlicedCode("Test117", 10, 12);
        //System.out.println(code);
        String expected = 
                "class Test117 {\n" + 
                "    public void m() {\n" + 
                "        int y = 0;\n" + 
                "        for (int i = 0; i < 5; i++) {\n" + 
                "            y++;\n" + 
                "        }\n" + 
                "        int q = y;\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testSlice118_1() {
        String code = getSlicedCode("Test118", 4, 12);
        //System.out.println(code);
        String expected = 
                "class Test118 {\n" + 
                "    public void m() {\n" + 
                "        int x = m0();\n" + 
                "    }\n" + 
                "    public int m0() {\n" + 
                "        return 1;\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    public void testSlice118_2() {
        String code = getSlicedCode("Test118", 5, 12);
        //System.out.println(code);
        String expected = 
                "class Test118 {\n" + 
                "    public void m() {\n" + 
                "        int y = m1(1);\n" + 
                "    }\n" + 
                "    public int m1(int a) {\n" + 
                "        return a + 1;\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    public void testSlice118_3() {
        String code = getSlicedCode("Test118", 6, 12);
        //System.out.println(code);
        String expected = 
                "class Test118 {\n" + 
                "    public void m() {\n" + 
                "        int z = m2(2);\n" + 
                "    }\n" + 
                "    public int m2(int b) {\n" + 
                "        return b + 1;\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    public void testSlice118_4() {
        String code = getSlicedCode("Test118", 7, 12);
        //System.out.println(code);
        String expected = 
                "class Test118 {\n" + 
                "    public void m() {\n" + 
                "        int p = m3(3);\n" + 
                "    }\n" + 
                "    public int m3(int c) {\n" + 
                "        return c + 1;\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    public void testSlice118_5() {
        String code = getSlicedCode("Test118", 8, 12);
        //System.out.println(code);
        String expected = 
                "class Test118 {\n" + 
                "    public void m() {\n" + 
                "        int q = m4(1, 2);\n" + 
                "    }\n" + 
                "    public int m4(int a, int b) {\n" + 
                "        return a + b;\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    public void testSlice118_6() {
        String code = getSlicedCode("Test118", 9, 12);
        //System.out.println(code);
        String expected = 
                "class Test118 {\n" + 
                "    public void m() {\n" + 
                "        int r = m5(2, 3);\n" + 
                "    }\n" + 
                "    public int m5(int b, int c) {\n" + 
                "        return b + c;\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    public void testSlice118_7() {
        String code = getSlicedCode("Test118", 10, 12);
        //System.out.println(code);
        String expected = 
                "class Test118 {\n" + 
                "    public void m() {\n" + 
                "        int s = m6(1, 3);\n" + 
                "    }\n" + 
                "    public int m6(int a, int c) {\n" + 
                "        return a + c;\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    public void testSlice118_8() {
        String code = getSlicedCode("Test118", 11, 12);
        //System.out.println(code);
        String expected = 
                "class Test118 {\n" + 
                "    public void m() {\n" + 
                "        int t = m7(1, 2, 3);\n" + 
                "    }\n" + 
                "    public int m7(int a, int b, int c) {\n" + 
                "        return a + b + c;\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testSlice119_1() {
        String code = getSlicedCode("Test119", 8, 12);
        //System.out.println(code);
        String expected = 
                "class Test119 {\n" + 
                "    public void m() {\n" + 
                "        A a = new A();\n" + 
                "        a.setX(1);\n" + 
                "        int b = a.getX();\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testSlice119_2() {
        String code = getSlicedCode("Test119", 8, 16);
        //System.out.println(code);
        String expected = 
                "class Test119 {\n" + 
                "    public void m() {\n" + 
                "        A a = new A();\n" + 
                "        a.setX(1);\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testSlice119_3() {
        String code = getSlicedCode("Test119", 10, 12); 
        //System.out.println(code);
        String expected = 
                "class Test119 {\n" + 
                "    public void m() {\n" + 
                "        A a = new A();\n" + 
                "        a.setX(2);\n" + 
                "        int c = a.getX();\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testSlice119_4() {
        String code = getSlicedCode("Test119", 10, 16); 
        //System.out.println(code);
        String expected = 
                "class Test119 {\n" + 
                "    public void m() {\n" + 
                "        A a = new A();\n" + 
                "        a.setX(2);\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testSlice119_5() {
        String code = getSlicedCode("Test119", 11, 12); 
        //System.out.println(code);
        String expected = 
                "class Test119 {\n" + 
                "    private int p;\n" + 
                "    public void m() {\n" + 
                "        p = 10;\n" + 
                "        A a = new A();\n" + 
                "        a.setX(2);\n" + 
                "        int d = a.x + p;\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testSlice119_6() {
        String code = getSlicedCode("Test119", 11, 16); 
        //System.out.println(code);
        String expected = 
                "class Test119 {\n" + 
                "    public void m() {\n" + 
                "        A a = new A();\n" + 
                "        a.setX(2);\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testSlice119_7() {
        String code = getSlicedCode("Test119", 11, 18); 
        //System.out.println(code);
        String expected = 
                "class Test119 {\n" + 
                "    public void m() {\n" + 
                "        A a = new A();\n" + 
                "        a.setX(2);\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testSlice119_8() {
        String code = getSlicedCode("Test119", 12, 12); 
        //System.out.println(code);
        String expected = 
                "class Test119 {\n" + 
                "    private int p;\n" + 
                "    public void m() {\n" + 
                "        p = 10;\n" + 
                "        int e = getP() + 2;\n" + 
                "    }\n" + 
                "    private int getP() {\n" + 
                "        return p;\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testSlice120_1() {
        String code = getSlicedCode("Test120", 5, 12);
        //System.out.println(code);
        String expected = 
                "class Test120 {\n" + 
                "    public void m() {\n" + 
                "        int p = m0();\n" + 
                "    }\n" + 
                "    public int m0() {\n" + 
                "        return 1;\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testSlice120_2() {
        String code = getSlicedCode("Test120", 6, 12);
        //System.out.println(code);
        String expected = 
                "class Test120 {\n" + 
                "    public void m() {\n" + 
                "        O o = new O();\n" + 
                "        int q = m1(o.x);\n" + 
                "    }\n" + 
                "    public int m1(int a) {\n" + 
                "        return a + 1;\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testSlice120_3() {
        String code = getSlicedCode("Test120", 7, 12);
        //System.out.println(code);
        String expected = 
                "class Test120 {\n" + 
                "    public void m() {\n" + 
                "        O o = new O();\n" + 
                "        int r = m2(o.y);\n" + 
                "    }\n" + 
                "    public int m2(int b) {\n" + 
                "        return b + 1;\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testSlice120_4() {
        String code = getSlicedCode("Test120", 8, 12);
        //System.out.println(code);
        String expected = 
                "class Test120 {\n" + 
                "    public void m() {\n" + 
                "        O o = new O();\n" + 
                "        int s = m3(o.x, o.y);\n" + 
                "    }\n" + 
                "    public int m3(int a, int b) {\n" + 
                "        return a + b;\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testSlice121_1() {
        String code = getSlicedCode("Test121", 10, 12);
        //System.out.println(code);
        String expected = 
                "class Test121 {\n" + 
                "    public int m(int x) {\n" + 
                "        int p = x + 1;\n" +
                "        return 0;\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testSlice121_2() {
        String code = getSlicedCode("Test121", 11, 12);
        //System.out.println(code);
        String expected = 
                "class Test121 {\n" + 
                "    public int m(int y) {\n" + 
                "        int q = y + 1;\n" +
                "        return 0;\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testSlice121_3() {
        String code = getSlicedCode("Test121", 6, 12);
        //System.out.println(code);
        String expected = 
                "class Test121 {\n" + 
                "    public void m() {\n" + 
                "        int b = 2;\n" +
                "        int c = m(b);\n" + 
                "    }\n" + 
                "    public int m(int y) {\n" + 
                "        int q = y + 1;\n" +
                "        return q;\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testSlice122_1() {
        String code = getSlicedCode("Test122", 15, 12);
        //System.out.println(code);
        String expected = 
                "class Test122 {\n" + 
                "    public void m() {\n" + 
                "        int a = 2;\n" + 
                "        int b = 0;\n" + 
                "        try {\n" + 
                "            b = n(a);\n" + 
                "        } catch (Exception e) {\n" + 
                "        } finally {\n" +
                "        }\n" + 
                "        int c = b;\n" + 
                "    }\n" + 
                "    public int n(int x) throws Exception {\n" + 
                "        if (x == 0) {\n" + 
                "            throw new Exception();\n" + 
                "        }\n" + 
                "        return 10 / x;\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testSlice122_2() {
        String code = getSlicedCode("Test122", 9, 16);
        //System.out.println(code);
        String expected = 
                "class Test122 {\n" + 
                "    public void m() {\n" + 
                "        int a = 2;\n" + 
                "        int b = 0;\n" + 
                "        try {\n" + 
                "            b = n(a);\n" + 
                "            int q = b + 5;\n" + 
                "        } catch (Exception e) {\n" + 
                "        } finally {\n" +
                "        }\n" + 
                "    }\n" + 
                "    public int n(int x) throws Exception {\n" + 
                "        if (x == 0) {\n" + 
                "            throw new Exception();\n" + 
                "        }\n" + 
                "        return 10 / x;\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testSlice122_3() {
        String code = getSlicedCode("Test122", 11, 22);
        //System.out.println(code);
        String expected = 
                "class Test122 {\n" + 
                "    public void m() {\n" + 
                "        int a = 2;\n" + 
                "        try {\n" + 
                "            n(a);\n" + 
                "        } catch (Exception e) {\n" + 
                "            Exception f = e;\n" + 
                "        } finally {\n" +
                "        }\n" + 
                "    }\n" + 
                "    public int n(int x) throws Exception {\n" + 
                "        if (x == 0) {\n" + 
                "            throw new Exception();\n" + 
                "        }\n" + 
                "        return 0;\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testSlice122_4() {
        String code = getSlicedCode("Test122", 13, 16);
        //System.out.println(code);
        String expected = 
                "class Test122 {\n" + 
                "    public void m() {\n" + 
                "        int a = 2;\n" + 
                "        int b = 0;\n" + 
                "        try {\n" + 
                "            b = n(a);\n" + 
                "        } catch (Exception e) {\n" + 
                "        } finally {\n" + 
                "            int r = b + 7;\n" + 
                "        }\n" + 
                "    }\n" + 
                "    public int n(int x) throws Exception {\n" + 
                "        if (x == 0) {\n" + 
                "            throw new Exception();\n" + 
                "        }\n" + 
                "        return 10 / x;\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testSlice122_5() {
        String code = getSlicedCode("Test122", 10, 27);
        //System.out.println(code);
        String expected = 
                "class Test122 {\n" + 
                "    public void m() {\n" + 
                "        int a = 2;\n" + 
                "        try {\n" + 
                "            n(a);\n" + 
                "        } catch (Exception e) {\n" + 
                "        } finally {\n" + 
                "        }\n" + 
                "    }\n" + 
                "    public int n(int x) throws Exception {\n" + 
                "        if (x == 0) {\n" + 
                "            throw new Exception();\n" + 
                "        }\n" + 
                "        return 0;\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testSlice123_1() {
        String code = getSlicedCode("Test123", 15, 12);
        //System.out.println(code);
        String expected = 
                "class Test123 {\n" + 
                "    public void m() {\n" + 
                "        int a = 2;\n" + 
                "        int b = 0;\n" + 
                "        try {\n" + 
                "            b = n(a);\n" + 
                "        } catch (Exception e) {\n" + 
                "        } finally {\n" +
                "        }\n" + 
                "        int c = b;\n" + 
                "    }\n" + 
                "    public int n(int x) throws SubException {\n" + 
                "        if (x == 0) {\n" + 
                "            throw new SubSubException();\n" + 
                "        }\n" + 
                "        return 10 / x;\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testSlice123_2() {
        String code = getSlicedCode("Test123", 9, 16);
        //System.out.println(code);
        String expected = 
                "class Test123 {\n" + 
                "    public void m() {\n" + 
                "        int a = 2;\n" + 
                "        int b = 0;\n" + 
                "        try {\n" + 
                "            b = n(a);\n" + 
                "            int q = b + 5;\n" + 
                "        } catch (Exception e) {\n" + 
                "        } finally {\n" +
                "        }\n" + 
                "    }\n" + 
                "    public int n(int x) throws SubException {\n" + 
                "        if (x == 0) {\n" + 
                "            throw new SubSubException();\n" + 
                "        }\n" + 
                "        return 10 / x;\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testSlice123_3() {
        String code = getSlicedCode("Test123", 11, 22);
        //System.out.println(code);
        String expected = 
                "class Test123 {\n" + 
                "    public void m() {\n" + 
                "        int a = 2;\n" + 
                "        try {\n" + 
                "            n(a);\n" + 
                "        } catch (Exception e) {\n" + 
                "            Exception f = e;\n" + 
                "        } finally {\n" +
                "        }\n" + 
                "    }\n" + 
                "    public int n(int x) throws SubException {\n" + 
                "        if (x == 0) {\n" + 
                "            throw new SubSubException();\n" + 
                "        }\n" + 
                "        return 0;\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testSlice123_4() {
        String code = getSlicedCode("Test123", 13, 16);
        //System.out.println(code);
        String expected = 
                "class Test123 {\n" + 
                "    public void m() {\n" + 
                "        int a = 2;\n" + 
                "        int b = 0;\n" + 
                "        try {\n" + 
                "            b = n(a);\n" + 
                "        } catch (Exception e) {\n" + 
                "        } finally {\n" + 
                "            int r = b + 7;\n" + 
                "        }\n" + 
                "    }\n" + 
                "    public int n(int x) throws SubException {\n" + 
                "        if (x == 0) {\n" + 
                "            throw new SubSubException();\n" + 
                "        }\n" + 
                "        return 10 / x;\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testSlice123_5() {
        String code = getSlicedCode("Test123", 10, 27);
        //System.out.println(code);
        String expected = 
                "class Test123 {\n" + 
                "    public void m() {\n" + 
                "        int a = 2;\n" + 
                "        try {\n" + 
                "            n(a);\n" + 
                "        } catch (Exception e) {\n" + 
                "        } finally {\n" + 
                "        }\n" + 
                "    }\n" + 
                "    public int n(int x) throws SubException {\n" + 
                "        if (x == 0) {\n" + 
                "            throw new SubSubException();\n" + 
                "        }\n" + 
                "        return 0;\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testSlice124_1() {
        String code = getSlicedCode("Test124", 7, 11);
        //System.out.println(code);
        String expected = 
                "class Test124 {\n" + 
                "    public void m() {\n" + 
                "        int p = 10;\n" + 
                "        AA a = new AA(p);\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testSlice124_2() {
        String code = getSlicedCode("Test124", 8, 12);
        //System.out.println(code);
        String expected = 
                "class Test124 {\n" + 
                "    public void m() {\n" + 
                "        int p = 10;\n" + 
                "        AA a = new AA(p);\n" + 
                "        int b = a.getX();\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testSlice124_3() {
        String code = getSlicedCode("Test124", 9, 12);
        //System.out.println(code);
        String expected = 
                "class Test124 {\n" + 
                "    public void m() {\n" + 
                "        int p = 10;\n" + 
                "        int q = 20;\n" + 
                "        AA a = new AA(p);\n" + 
                "        int c = a.inc(q);\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testSlice125_1() {
        String code = getSlicedCode("Test125", 6, 12);
        //System.out.println(code);
        String expected = 
                "class Test125 {\n" + 
                "    public void m() {\n" + 
                "        int p = 0;\n" + 
                "        int q = inc1(p);\n" + 
                "    }\n" + 
                "    public int inc1(int x) {\n" + 
                "        return x + 1;\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testSlice125_2() {
        String code = getSlicedCode("Test125", 7, 12);
        //System.out.println(code);
        String expected = 
                "class Test125 {\n" + 
                "    public void m() {\n" + 
                "        int p = 0;\n" + 
                "        int r = inc2(inc1(p));\n" + 
                "    }\n" + 
                "    public int inc1(int x) {\n" + 
                "        return x + 1;\n" + 
                "    }\n" + 
                "    public int inc2(int x) {\n" + 
                "        return x + 2;\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testSlice126_1() {
        String code = getSlicedCode("Test126", 8, 12);
        //System.out.println(code);
        String expected = 
                "class Test126 {\n" + 
                "    public void m() {\n" + 
                "        AAA a = new AAA();\n" + 
                "        int p = 0;\n" + 
                "        AAA a2 = a.add(p);\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testSlice126_2() {
        String code = getSlicedCode("Test126", 10, 12);
        //System.out.println(code);
        String expected = 
                "class Test126 {\n" + 
                "    public void m() {\n" + 
                "        AAA a = new AAA();\n" + 
                "        int p = 0;\n" + 
                "        AAA a2 = a.add(p);\n" + 
                "        int q = a2.getY();\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testSlice126_3() {
        String code = getSlicedCode("Test126", 11, 12);
        //System.out.println(code);
        String expected = 
                "class Test126 {\n" + 
                "    public void m() {\n" + 
                "        AAA a = new AAA();\n" + 
                "        int p = 0;\n" + 
                "        int r = a.add(p).getY();\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testSlice127_1() {
        String code = getSlicedCode("Test127", 8, 12);
        //System.out.println(code);
        String expected = 
                "class Test127 {\n" + 
                "    public void m() {\n" + 
                "        A2 a = new A2();\n" + 
                "        a.setY(2);\n" + 
                "        int p = a.getY();\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testSlice127_2() {
        String code = getSlicedCode("Test127", 9, 12);
        //System.out.println(code);
        String expected = 
                "class Test127 {\n" + 
                "    public void m() {\n" + 
                "        A2.z = 1;\n" + 
                "        int q = A2.z;\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testSlice128_1() {
        String code = getSlicedCode("Test128", 8, 12);
        //System.out.println(code);
        String expected = 
                "class Test128 {\n" + 
                "    public void m() {\n" + 
                "        A3 a = new A3();\n" + 
                "        int p = 0;\n" + 
                "        int r = n(a.add(p).getY());\n" + 
                "    }\n" + 
                "    public int n(int y) {\n" + 
                "        return y + 4;\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testCustomer1() {
        String code = getSlicedCode("Customer", 22, 12);
        //System.out.println(code);
        String expected = 
                "class Customer {\n" + 
                "    private String name = \"\";\n" + 
                "    public double discount = 0;\n" + 
                "    public Customer(String name) {\n" + 
                "        this.name = name;\n" + 
                "    }\n" + 
                "    public String statement(Order order) {\n" + 
                "        if (order == null) {\n" + 
                "            return \"No order\";\n" + 
                "        }\n" + 
                "        if (order.getSize() > 1 && discount < 0.2) {\n" + 
                "            discount = discount * 2;\n" + 
                "        }\n" + 
                "        int amount = getAmount(order);\n" + 
                "        return null;\n" + 
                "    }\n" + 
                "    public int getAmount(Order order) {\n" + 
                "        int amount = 0;\n" + 
                "        for (Rental rental : order.rentals) {\n" + 
                "            amount += rental.getCharge(discount);\n" + 
                "        }\n" + 
                "        return amount;\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testCustomer2() {
        String code = getSlicedCode("Customer", 27, 31);
        //System.out.println(code);
        String expected = 
                "class Customer {\n" + 
                "    public int getAmount(Order order) {\n" + 
                "        return 0;\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testCustomer3() {
        String code = getSlicedCode("Customer", 28, 12);
        //System.out.println(code);
        String expected = 
                "class Customer {\n" + 
                "    public int getAmount() {\n" + 
                "        int amount = 0;\n" + 
                "        return 0;\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testCustomer4() {
        String code = getSlicedCode("Customer", 22, 31);
        //System.out.println(code);
        String expected = 
                "class Customer {\n" + 
                "    public String statement(Order order) {\n" + 
                "        if (order == null) {\n" + 
                "            return \"No order\";\n" + 
                "        }\n" + 
                "        return null;\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testTest200_1() {
        String code = getSlicedCode("Test200", 11, 12);
        //System.out.println(code);
        String expected = 
                "/*\n" + 
                "  Class Comment\n" + 
                "*/\n" + 
                "class Test200 {\n" + 
                "    /*\n" + 
                "     * Method Comment\n" + 
                "     */\n" + 
                "    public void m(int x) {\n" + 
                "        // Comment 1\n" + 
                "        int a = x + 2; // Comment 6\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testTest200_2() {
        String code = getSlicedCode("Test200", 15, 12);
        //System.out.println(code);
        String expected = 
                "/*\n" + 
                "  Class Comment\n" + 
                "*/\n" + 
                "class Test200 {\n" + 
                "    /*\n" + 
                "     * Method Comment\n" + 
                "     */\n" + 
                "    public void m(int x) {\n" + 
                "        // Comment 3\n" + 
                "        int b = x + 2;\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    @Test
    public void testTest200_3() {
        String code = getSlicedCode("Test200", 19, 12);
        //System.out.println(code);
        String expected = 
                "/*\n" + 
                "  Class Comment\n" + 
                "*/\n" + 
                "class Test200 {\n" + 
                "    /*\n" + 
                "     * Method Comment\n" + 
                "     */\n" + 
                "    public void m(int x) {\n" + 
                "        // Comment 1\n" + 
                "        int a = x + 2; // Comment 6\n" + 
                "        /*\n" + 
                "         * Comment 4\n" + 
                "         */\n" + 
                "        int c = a;\n" + 
                "    }\n" + 
                "}\n";
        assertEquals(expected, code);
    }
    
    public static void main(String[] args) {
        build();
        
        SliceCodeTest tester = new SliceCodeTest();
        
        /*
        tester.testSlice101_1();
        tester.testSlice101_1m();
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
        
        tester.testSlice121_1();
        tester.testSlice121_2();
        tester.testSlice121_3();
        
        tester.testSlice122_1();
        tester.testSlice122_2();
        tester.testSlice122_3();
        tester.testSlice122_4();
        tester.testSlice122_5();
        
        tester.testSlice123_1();
        tester.testSlice123_2();
        tester.testSlice123_3();
        tester.testSlice123_4();
        tester.testSlice123_5();
        
        tester.testSlice124_1();
        tester.testSlice124_2();
        tester.testSlice124_3();
        
        tester.testSlice125_1();
        tester.testSlice125_2();
        
        tester.testSlice126_1();
        tester.testSlice126_2();
        tester.testSlice126_3();
        
        tester.testSlice127_1();
        tester.testSlice127_2();
        
        tester.testSlice128_1();
        
        tester.testCustomer1();
        tester.testCustomer2();
        tester.testCustomer3();
        tester.testCustomer4();
        
        tester.testTest200_1();
        tester.testTest200_2();
        tester.testTest200_3();
        */
        
        unbuild();
    }
}
