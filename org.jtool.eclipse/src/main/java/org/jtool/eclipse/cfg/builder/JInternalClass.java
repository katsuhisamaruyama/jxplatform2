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
public class JInternalClass extends JClass {
    
    protected JavaClass jclass;
    
    public JInternalClass(JavaClass jclass) {
        this(null, jclass);
    }
    
    public JInternalClass(JClass clazz, JavaClass jclass) {
        this.jclass = jclass;
        declaringClass = clazz;
        if (clazz != null) {
            if (jclass.getDeclaringMethod() != null) {
                declaringMethod = declaringClass.getMethod(jclass.getDeclaringMethod().getSignature());
            } else {
                declaringMethod = null;
            }
        } else {
            declaringMethod = null;
        }
        
        int num = 0;
        fields = new JInternalField[jclass.getFields().size()];
        for (JavaField jf : jclass.getFields()) {
            fields[num] = new JInternalField(this, jf);
            num++;
        }
        
        num = 0;
        methods = new JInternalMethod[jclass.getMethods().size()];
        for (JavaMethod jm: jclass.getMethods()) {
            methods[num] = new JInternalMethod(this, jm);
            num++;
        }
        
        num = 0;
        innerClasses = new JInternalClass[jclass.getInnerClasses().size()];
        for (JavaClass jc: jclass.getInnerClasses()) {
            innerClasses[num] = new JInternalClass(this, jc);
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
    
    void collectInfo() {
        ancestors = findAncestors();
        descendants = findDescendants();
    }
    
    private JClass[] findAncestors() {
        List<JClass> classes = new ArrayList<JClass>();
        for (JavaClass jc : jclass.getAncestors()) {
            JClass clazz = JInfoStore.getInstance().getJClass(jc.getQualifiedName());
            if (clazz != null) {
                classes.add(clazz);
            }
        }
        return classes.toArray(new JClass[classes.size()]);
    }
    
    private JClass[] findDescendants() {
        List<JClass> classes = new ArrayList<JClass>();
        for (JavaClass jc : jclass.getDescendants()) {
            JClass clazz = JInfoStore.getInstance().getJClass(jc.getQualifiedName());
            if (clazz != null) {
                classes.add(clazz);
            }
        }
        return classes.toArray(new JClass[classes.size()]);
    }
}
