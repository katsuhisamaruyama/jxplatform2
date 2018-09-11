/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg.builder;

import org.jtool.eclipse.cfg.JClass;
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
    
    protected JavaClass jclass;
    
    public InternalJClass(JavaClass jclass) {
        this(null, jclass);
    }
    
    public InternalJClass(JClass clazz, JavaClass jclass) {
        this.jclass = jclass;
        
        int num = 0;
        fields = new InternalJField[jclass.getFields().size()];
        for (JavaField jf : jclass.getFields()) {
            fields[num] = new InternalJField(this, jf);
            num++;
        }
        
        num = 0;
        methods = new InternalJMethod[jclass.getMethods().size()];
        for (JavaMethod jm: jclass.getMethods()) {
            methods[num] = new InternalJMethod(this, jm);
            num++;
        }
    }
    
    public JavaClass getJavaClass() {
        return jclass;
    }
    
    @Override
    public String getName() {
        return jclass.getName();
    }
    
    @Override
    public String getQualifiedName() {
        return jclass.getQualifiedName();
    }
    
    @Override
    public boolean isClass() {
        return jclass.isClass();
    }
    
    @Override
    public boolean isInterface() {
        return jclass.isInterface();
    }
    
    @Override
    public boolean isEnum() {
        return jclass.isEnum();
    }
    
    @Override
    public boolean isPublic() {
        return jclass.isPublic();
    }
    
    @Override
    public boolean isProtected() {
        return jclass.isProtected();
    }
    
    @Override
    public boolean isPrivate() {
        return jclass.isPrivate();
    }
    
    @Override
    public boolean isDefault() {
        return jclass.isDefault();
    }
    
    @Override
    public boolean isInProject() {
        return true;
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
