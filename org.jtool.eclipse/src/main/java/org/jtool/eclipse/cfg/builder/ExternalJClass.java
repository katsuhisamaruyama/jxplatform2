/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg.builder;

import org.jtool.eclipse.cfg.JClass;
import org.jtool.eclipse.cfg.JMethod;
import org.jtool.eclipse.javamodel.JavaProject;
import org.jtool.eclipse.javamodel.JavaClass;
import org.jtool.eclipse.javamodel.builder.BytecodeClassStore;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtConstructor;
import javassist.Modifier;
import java.util.List;
import java.util.ArrayList;

/**
 * An object that represents a class outside the project.
 * All methods of this class are not intended to be directly called by clients.
 * 
 * @author Katsuhisa Maruyama
 */
public class ExternalJClass extends JClass {
    
    protected JavaProject jproject;
    protected CtClass cclass;
    
    public ExternalJClass(CtClass cclass, JavaProject jproject) {
        this.cclass = cclass;
        this.jproject = jproject;
        
        int num = 0;
        fields = new ExternalJField[cclass.getFields().length];
        for (CtField cf : cclass.getFields()) {
            fields[num] = new ExternalJField(this, cf);
            num++;
        }
        
        num = 0;
        methods = new JMethod[cclass.getMethods().length + cclass.getConstructors().length];
        for (CtMethod cm: cclass.getMethods()) {
            methods[num] = new ExternalJMethod(this, cm);
            num++;
        }
        for (CtConstructor cm: cclass.getConstructors()) {
            methods[num] = new ExternalJConstructor(this, cm);
            num++;
        }
    }
    
    public CtClass getCtClass() {
        return cclass;
    }
    
    @Override
    public String getName() {
        return cclass.getSimpleName();
    }
    
    @Override
    public String getQualifiedName() {
        return cclass.getName();
    }
    
    @Override
    public boolean isClass() {
        return !isInterface() && !isEnum();
    }
    
    @Override
    public boolean isInterface() {
        return cclass.isInterface();
    }
    
    @Override
    public boolean isEnum() {
        return cclass.isEnum();
    }
    
    @Override
    public boolean isPublic() {
        return Modifier.isPublic(cclass.getModifiers());
    }
    
    @Override
    public boolean isProtected() {
        return Modifier.isProtected(cclass.getModifiers());
    }
    
    @Override
    public boolean isPrivate() {
        return Modifier.isPrivate(cclass.getModifiers());
    }
    
    @Override
    public boolean isDefault() {
        return !isPublic() && !isProtected() && !isPrivate();
    }
    
    @Override
    public boolean isInProject() {
        return false;
    }
    
    @Override
    protected JClass[] findAncestors() {
        List<JClass> classes = new ArrayList<JClass>();
        BytecodeClassStore bytecodeClassStore = jproject.getBytecodeClassStore();
        if (bytecodeClassStore == null) {
            return new JClass[0];
        }
        
        for (CtClass cc : bytecodeClassStore.getAncestors(cclass.getName())) {
            JClass clazz = JInfoStore.getInstance().getJClass(cc.getName());
            if (clazz != null) {
                classes.add(clazz);
            }
        }
        return classes.toArray(new JClass[classes.size()]);
    }
    
    @Override
    protected JClass[] findDescendants() {
        List<JClass> classes = new ArrayList<JClass>();
        BytecodeClassStore bytecodeClassStore = jproject.getBytecodeClassStore();
        if (bytecodeClassStore == null) {
            return new JClass[0];
        }
        
        for (CtClass cc : bytecodeClassStore.getDescendants(cclass.getName())) {
            JClass clazz = JInfoStore.getInstance().getJClass(cc.getName());
            if (clazz != null) {
                classes.add(clazz);
            }
        }
        for (JavaClass jc : bytecodeClassStore.getJavaDescendants(cclass.getName())) {
            JClass clazz = JInfoStore.getInstance().getJClass(jc.getQualifiedName());
            if (clazz != null) {
                classes.add(clazz);
            }
        }
        return classes.toArray(new JClass[classes.size()]);
    }
}
