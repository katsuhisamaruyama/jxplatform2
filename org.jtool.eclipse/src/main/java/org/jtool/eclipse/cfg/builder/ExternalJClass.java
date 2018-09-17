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
    protected CtClass ctClass;
    
    public ExternalJClass(CtClass ctClass, JavaProject jproject) {
        this.ctClass = ctClass;
        this.jproject = jproject;
        
        int num = 0;
        fields = new ExternalJField[ctClass.getFields().length];
        for (CtField cf : ctClass.getFields()) {
            fields[num] = new ExternalJField(this, cf);
            num++;
        }
        
        num = 0;
        methods = new JMethod[ctClass.getMethods().length + ctClass.getConstructors().length];
        for (CtMethod cm: ctClass.getMethods()) {
            methods[num] = new ExternalJMethod(this, cm);
            num++;
        }
        for (CtConstructor cm: ctClass.getConstructors()) {
            methods[num] = new ExternalJConstructor(this, cm);
            num++;
        }
    }
    
    public CtClass getCtClass() {
        return ctClass;
    }
    
    @Override
    public String getName() {
        return ctClass.getSimpleName();
    }
    
    @Override
    public String getQualifiedName() {
        return ctClass.getName();
    }
    
    @Override
    public boolean isClass() {
        return !isInterface() && !isEnum();
    }
    
    @Override
    public boolean isInterface() {
        return ctClass.isInterface();
    }
    
    @Override
    public boolean isEnum() {
        return ctClass.isEnum();
    }
    
    @Override
    public boolean isPublic() {
        return Modifier.isPublic(ctClass.getModifiers());
    }
    
    @Override
    public boolean isProtected() {
        return Modifier.isProtected(ctClass.getModifiers());
    }
    
    @Override
    public boolean isPrivate() {
        return Modifier.isPrivate(ctClass.getModifiers());
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
        
        for (CtClass cc : bytecodeClassStore.getAncestors(ctClass.getName())) {
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
        
        for (CtClass cc : bytecodeClassStore.getDescendants(ctClass.getName())) {
            JClass clazz = JInfoStore.getInstance().getJClass(cc.getName());
            if (clazz != null) {
                classes.add(clazz);
            }
        }
        for (JavaClass jc : bytecodeClassStore.getJavaDescendants(ctClass.getName())) {
            JClass clazz = JInfoStore.getInstance().getJClass(jc.getQualifiedName());
            if (clazz != null) {
                classes.add(clazz);
            }
        }
        return classes.toArray(new JClass[classes.size()]);
    }
}
