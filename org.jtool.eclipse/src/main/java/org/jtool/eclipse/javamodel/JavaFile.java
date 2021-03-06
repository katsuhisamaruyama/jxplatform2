/*
 *  Copyright 2018-2020
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.javamodel;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ASTNode;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.io.File;

/**
 * An object representing a Java source file.
 * 
 * @author Katsuhisa Maruyama
 */
public class JavaFile {
    
    protected CompilationUnit compilationUnit;
    protected JavaProject jproject;
    protected String path;
    protected String code;
    protected String charset;
    
    protected JavaPackage jpackage;
    protected Set<JavaClass> classes = new HashSet<>();
    
    protected List<ASTNode> imports = new ArrayList<>();
    
    public JavaFile(CompilationUnit cu, String path, String code, String charset, JavaProject jproject) {
        this.compilationUnit = cu;
        this.jproject = jproject;
        this.path = path;
        this.code = code;
        this.charset = charset;
        jproject.addFile(this);
    }
    
    public void dispose() {
        jproject = null;
        path = null;
        code = null;
        charset = null;
        jpackage = null;
        classes.clear();
        classes = null;
    }
    
    public CompilationUnit getCompilationUnit() {
        return compilationUnit;
    }
    
    public JavaProject getProject() {
        return jproject;
    }
    
    public String getPath() {
        return path;
    }
    
    public String getRelativePath() {
        return path.substring(jproject.getPath().length() + 1);
    }
    
    public String getName() {
        return path.substring(path.lastIndexOf(File.separator) + 1);
    }
    
    public String getCode() {
        return code;
    }
    
    public String getCharset() {
        return charset;
    }
    
    public void setPackage(JavaPackage jpackage) {
        this.jpackage = jpackage;
    }
    
    public JavaPackage getPackage() {
        return jpackage;
    }
    
    protected void addClass(JavaClass jclass) {
        classes.add(jclass);
        jclass.getFile().getProject().addClass(jclass);
    }
    
    public Set<JavaClass> getClasses() {
        return classes;
    }
    
    public void addImport(ASTNode node) {
        imports.add(node);
    }
    
    public List<ASTNode> getImports() {
        return imports;
    }
    
    @Override
    public boolean equals(Object obj) {
        return (obj instanceof JavaFile) ? equals((JavaFile)obj) : false;
    }
    
    public boolean equals(JavaFile jfile) {
        return jfile != null && (this == jfile || path.equals(jfile.path));
    }
    
    @Override
    public int hashCode() {
        return path.hashCode();
    }
    
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("\n");
        buf.append("FILE: ");
        buf.append(getPath());
        return buf.toString();
    }
    
    public static String getRelativePath(String path, String base) {
        if (path.startsWith(base) && path.length() > base.length()) {
            return path.substring(base.length() + 1);
        }
        return null;
    }
    
    public static String changeExtension(String path, String ext) {
        int pos = path.lastIndexOf(".");
        if (pos != -1) {
            return path.substring(0, pos + 1) + ext;
        }
        return path + "." + ext;
    }
    
    public static void makeDir(File file) {
        int sep = file.getPath().lastIndexOf(File.separator);
        if (sep != -1) {
            String dirname = file.getPath().substring(0, sep);
            File dir = new File(dirname);
            dir.mkdirs();
        }
    }
    
    public static void deleteDir(File file) {
        if (file.isDirectory()) {
            String[] names = file.list();
            for (int i = 0; i < names.length; i++) {
                File f = new File(file.getPath() + File.separator + names[i]);
                f.delete();
            }
        }
        file.delete();
    }
}
