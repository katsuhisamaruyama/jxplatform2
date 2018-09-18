/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg.builder;

import org.jtool.eclipse.cfg.JClass;
import org.jtool.eclipse.javamodel.JavaProject;
import org.jtool.eclipse.javamodel.JavaClass;
import org.jtool.eclipse.javamodel.JavaField;
import org.jtool.eclipse.javamodel.JavaMethod;
import java.util.List;
import java.util.ArrayList;

/**
 * An object that represents a class inside the project.
 * All methods of this class are not intended to be directly called by clients.
 * 
 * @author Katsuhisa Maruyama
 */
public class InternalJClass extends JClass {
    
    private JavaClass jclass;
    
    InternalJClass(JavaProject jproject) {
        super(jproject);
    }
    
    InternalJClass(JavaClass jclass, JavaProject jproject) {
        super(jproject);
        this.jclass = jclass;
        
        this.name = jclass.getName();
        this.fqn = jclass.getQualifiedName();
        this.kind = jclass.getKind();
        this.modifiers = jclass.getModifiers();
        
        int num = 0;
        fields = new InternalJField[jclass.getFields().size()];
        for (JavaField jfield : jclass.getFields()) {
            fields[num] = new InternalJField(jfield, this);
            num++;
        }
        
        num = 0;
        methods = new InternalJMethod[jclass.getMethods().size()];
        for (JavaMethod jmethod: jclass.getMethods()) {
            methods[num] = new InternalJMethod(jmethod, this);
            num++;
        }
    }
    
    @Override
    public boolean isInProject() {
        return true;
    }
    
    @Override
    public boolean isTopLevelClass() {
        return jclass.getDeclaringClass() == null;
    }
    
    @Override
    protected JClass[] findAncestors() {
        List<JClass> classes = new ArrayList<JClass>();
        for (JavaClass jc : jclass.getAncestors()) {
            JClass clazz = JInfoStore.getInstance().getJClass(jc.getQualifiedName());
            if (clazz != null) {
                classes.add(clazz);
            }
        }
        return classes.toArray(new JClass[classes.size()]);
    }
    
    @Override
    protected JClass[] findDescendants() {
        System.out.println("TARGET = " + this.getQualifiedName());
        
        List<JClass> classes = new ArrayList<JClass>();
        for (JavaClass jc : jclass.getDescendants()) {
            
            System.out.println("DESC = " + jc.getQualifiedName()+ " of " + this.getQualifiedName());
            
            JClass clazz = JInfoStore.getInstance().getJClass(jc.getQualifiedName());
            if (clazz != null) {
                
                System.out.println("DESC = " + clazz.getQualifiedName()+ " of " + this.getQualifiedName());
                
                classes.add(clazz);
            }
        }
        return classes.toArray(new JClass[classes.size()]);
    }
}
