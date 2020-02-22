/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.javamodel;

import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.IPackageBinding;
import java.util.Set;
import java.util.HashSet;

/**
 * An object representing a package.
 * 
 * @author Katsuhisa Maruyama
 */
public class JavaPackage extends JavaElement {
    
    protected String name;
    
    protected Set<JavaClass> classes = new HashSet<JavaClass>();
    
    private static final String DEFAUL_PACKAGE_NAME = "(default)";
    
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
            name = DEFAUL_PACKAGE_NAME;
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
    
    @Override
    public String getQualifiedName() {
        return name;
    }
    
    public boolean isDefault() {
        return name.equals(DEFAUL_PACKAGE_NAME);
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
        return (obj instanceof JavaPackage) ? equals((JavaPackage)obj) : false;
    }
    
    public boolean equals(JavaPackage jpackage) {
        return jpackage != null && (this == jpackage || name.equals(jpackage.name)); 
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
        if (efferentPackages == null) {
            efferentPackages = new HashSet<JavaPackage>();
        }
        if (afferentPackages == null) {
            afferentPackages = new HashSet<JavaPackage>();
        }
        for (JavaClass jclass : classes) {
            for (JavaClass jc : jclass.getEfferentClassesInProject()) {
                JavaPackage jpackage = jc.getPackage();
                if (jpackage != null && !jpackage.equals(this)) {
                    efferentPackages.add(jpackage);
                    jpackage.addAfferentPackage(this);
                }
            }
        }
    }
    
    private void addAfferentPackage(JavaPackage jpackage) {
        if (afferentPackages == null) {
            afferentPackages = new HashSet<JavaPackage>();
        }
        if (jpackage != null && !afferentPackages.contains(jpackage)) {
            afferentPackages.add(jpackage);
        }
    }
}
