/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg.builder;

import org.jtool.eclipse.cfg.JClass;
import org.jtool.eclipse.javamodel.JavaProject;
import org.jtool.eclipse.javamodel.builder.BytecodeClassStore;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtConstructor;
import javassist.Modifier;
import javassist.NotFoundException;
import java.util.List;
import java.util.ArrayList;

/**
 * An object that represents a class outside the project.
 * All methods of this class are not intended to be directly called by clients.
 * 
 * @author Katsuhisa Maruyama
 */
public class JExternalClass extends JClass {
    
    protected CtClass cclass;
    
    public JExternalClass(CtClass cclass) {
        this(null, cclass);
    }
    
    public JExternalClass(JClass clazz, CtClass cclass) {
        this.cclass = cclass;
        declaringClass = clazz;
        if (declaringClass != null) {
            try {
                if (cclass.getEnclosingMethod() != null) {
                    declaringMethod = declaringClass.getMethod(cclass.getEnclosingMethod().getSignature());
                } else {
                    declaringMethod = null;
                }
            } catch (NotFoundException e1) {
                declaringMethod = null;
            }
        } else {
            declaringMethod = null;
        }
        
        int num = 0;
        fields = new JInternalField[cclass.getFields().length];
        for (CtField cf : cclass.getFields()) {
            fields[num] = new JExternalField(this, cf);
            num++;
        }
        
        num = 0;
        methods = new JInternalMethod[cclass.getMethods().length + cclass.getConstructors().length];
        for (CtMethod cm: cclass.getMethods()) {
            methods[num] = new JExternalMethod(this, cm);
            num++;
        }
        for (CtConstructor cm: cclass.getConstructors()) {
            methods[num] = new JExternalConstructor(this, cm);
            num++;
        }
        
        num = 0;
        try {
            innerClasses = new JInternalClass[cclass.getNestedClasses().length];
            for (CtClass cc: cclass.getNestedClasses()) {
                innerClasses[num] = new JExternalClass(this, cc);
                num++;
            }
        } catch (NotFoundException e) {
            innerClasses = new JClass[0];
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
    
    void collectInfo(JavaProject jproject) {
        ancestors = findAncestors(jproject);
        descendants = findDescandants(jproject);
    }
    
    private JClass[] findAncestors(JavaProject jproject) {
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
    
    private JClass[] findDescandants(JavaProject jproject) {
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
}
