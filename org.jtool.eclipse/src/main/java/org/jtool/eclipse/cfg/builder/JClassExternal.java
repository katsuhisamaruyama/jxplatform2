/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg.builder;

import org.jtool.eclipse.javamodel.JavaClass;
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
public class JClassExternal extends JClass {
    
    private CtClass ctClass;
    
    JClassExternal(CtClass ctClass, CFGStore cfgStore) {
        super(ctClass.getName(), cfgStore, ctClass.getSimpleName(), getModfifiers(ctClass));
        this.ctClass = ctClass;
        
        int num = 0;
        methods = new JMethod[ctClass.getMethods().length + ctClass.getConstructors().length];
        for (CtMethod ctMtheod: ctClass.getMethods()) {
            methods[num] = new JMethodExternal(ctMtheod, this, cfgStore);
            num++;
        }
        for (CtConstructor ctMtheod: ctClass.getConstructors()) {
            methods[num] = new JMethodExternal(ctMtheod, this, cfgStore);
            num++;
        }
        
        num = 0;
        fields = new JFieldExternal[ctClass.getFields().length];
        for (CtField ctField : ctClass.getFields()) {
            fields[num] = new JFieldExternal(ctField, this, cfgStore);
            num++;
        }
    }
    
    private static int getModfifiers(CtClass ctClass) {
        if (Modifier.isPublic(ctClass.getModifiers())) {
            return org.eclipse.jdt.core.dom.Modifier.PUBLIC;
        } else if (Modifier.isProtected(ctClass.getModifiers())) {
            return org.eclipse.jdt.core.dom.Modifier.PROTECTED;
        } else if (Modifier.isPrivate(ctClass.getModifiers())) {
            return org.eclipse.jdt.core.dom.Modifier.PRIVATE;
        } else {
            return org.eclipse.jdt.core.dom.Modifier.DEFAULT;
        }
    }
    
    @Override
    public boolean isTopLevelClass() {
        try {
            return ctClass.getDeclaringClass() == null;
        } catch (NotFoundException e) {
            return false;
        }
    }
    
    @Override
    public boolean isInProject() {
        return false;
    }
    
    @Override
    protected JClass[] findAncestors() {
        List<JClass> classes = new ArrayList<JClass>();
        BytecodeClassStore bytecodeClassStore = cfgStore.getJInfoStore().getBytecodeClassStore();
        if (bytecodeClassStore == null) {
            return emptyClassArray;
        }
        
        for (CtClass cc : bytecodeClassStore.getAncestors(ctClass.getName())) {
            JClass clazz = cfgStore.getJInfoStore().getJClass(cc.getName());
            if (clazz != null) {
                classes.add(clazz);
            }
        }
        return classes.toArray(new JClass[classes.size()]);
    }
    
    @Override
    protected JClass[] findDescendants() {
        List<JClass> classes = new ArrayList<JClass>();
        BytecodeClassStore bytecodeClassStore = cfgStore.getJInfoStore().getBytecodeClassStore();
        if (bytecodeClassStore == null) {
            return emptyClassArray;
        }
        
        for (CtClass cc : bytecodeClassStore.getDescendants(ctClass.getName())) {
            JClass clazz = cfgStore.getJInfoStore().getJClass(cc.getName());
            if (clazz != null) {
                classes.add(clazz);
            }
        }
        for (JavaClass jc : bytecodeClassStore.getJavaDescendants(ctClass.getName())) {
            JClass clazz = cfgStore.getJInfoStore().getJClass(jc.getQualifiedName());
            if (clazz != null) {
                classes.add(clazz);
            }
        }
        return classes.toArray(new JClass[classes.size()]);
    }
}
