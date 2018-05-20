/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.model.java;

import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.IPackageBinding;
import java.util.Set;
import java.util.HashSet;

/**
 * An object representing a package.
 * @author Katsuhisa Maruyama
 */
public class JavaPackage extends JavaElement {
    
    protected String name;
    
    protected Set<JavaClass> classes = new HashSet<JavaClass>();
    
    protected JavaPackage() {
    }
    
    private JavaPackage(PackageDeclaration node, JavaFile jfile, String name) {
        super(node, jfile);
        this.name = name;
    }
    
    public static JavaPackage createDefault(JavaFile jfile) {
        return create(null, jfile);
    }
    
    public static JavaPackage create(PackageDeclaration node, JavaFile jfile) {
        String name = ".UNKNOWN";
        if (node != null) {
            IPackageBinding binding = node.resolveBinding();
            if (binding != null) {
                name = binding.getName();
            }
        } else {
            name = "(default)";
        }
        
        JavaPackage jpackage = jfile.getProject().getPackage(name);
        if (jpackage != null) {
            return jpackage;
        }
        
        jpackage = new JavaPackage(node, jfile, name);
        jfile.getProject().addPackage(jpackage);
        return jpackage;
    }
    
    @Override
    public void dispose() {
        name = null;
        classes.clear();
        classes = null;
        super.dispose();
    }
    
    public String getName() {
        return name;
    }
    
    protected void addClass(JavaClass jclass) {
        classes.add(jclass);
    }
    
    protected void removeClass(JavaClass jclass) {
        classes.remove(jclass);
    }
    
    public Set<JavaClass> getClasses() {
        return classes;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof JavaPackage) {
            return equals((JavaPackage)obj);
        }
        return false;
    }
    
    public boolean equals(JavaPackage jpackage) {
        if (jpackage == null) {
            return false;
        }
        return this == jpackage || name.equals(jpackage.name); 
    }
    
    @Override
    public int hashCode() {
        return name.hashCode();
    }
    
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("\n");
        buf.append("PACKAGE: ");
        buf.append(getName());
        return buf.toString();
    }
    
    protected boolean resolved = false;
    protected Set<JavaPackage> afferentPackages = null;
    protected Set<JavaPackage> efferentPackages = null;
    
    public Set<JavaPackage> getAfferentJavaPackages() {
        if (afferentPackages == null) {
            findEfferentPackages();
        }
        return afferentPackages;
    }
    
    public Set<JavaPackage> getEfferentJavaPackages() {
        if (efferentPackages == null) {
            findEfferentPackages();
        }
        return efferentPackages;
    }
    
    private void findEfferentPackages() {
        afferentPackages = new HashSet<JavaPackage>();
        efferentPackages = new HashSet<JavaPackage>();
        for (JavaClass jclass : classes) {
            for (JavaClass jc : jclass.getEfferentClassesInProject()) {
                JavaPackage jp = jc.getPackage();
                if (jp != null) {
                    efferentPackages.add(jp);
                }
            }
        }
        for (JavaPackage jp : efferentPackages) {
            jp.addAfferentPackage(this);
        }
    }
    
    private void addAfferentPackage(JavaPackage jp) {
        if (jp != null && !afferentPackages.contains(jp)) {
            afferentPackages.add(jp);
        }
    }
}
