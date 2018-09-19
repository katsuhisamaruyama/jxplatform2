/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg.builder;

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
public class JClassInternal extends JClass {
    
    private JavaClass jclass;
    
    JClassInternal(JavaClass jclass, CFGStore cfgStore) {
        super(jclass.getQualifiedName(), cfgStore, jclass.getName(), jclass.getModifiers());
        this.jclass = jclass;
        
        int num = 0;
        methods = new JMethodInternal[jclass.getMethods().size()];
        for (JavaMethod jmethod: jclass.getMethods()) {
            methods[num] = new JMethodInternal(jmethod, cfgStore);
            num++;
        }
        
        num = 0;
        fields = new JFieldInternal[jclass.getFields().size()];
        for (JavaField jfield : jclass.getFields()) {
            fields[num] = new JFieldInternal(jfield, cfgStore);
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
            JClass clazz = cfgStore.getJInfoStore().getJClass(jc.getQualifiedName());
            if (clazz != null) {
                classes.add(clazz);
            }
        }
        return classes.toArray(new JClass[classes.size()]);
    }
    
    @Override
    protected JClass[] findDescendants() {
        List<JClass> classes = new ArrayList<JClass>();
        for (JavaClass jc : jclass.getDescendants()) {
            JClass clazz = cfgStore.getJInfoStore().getJClass(jc.getQualifiedName());
            if (clazz != null) {
                classes.add(clazz);
            }
        }
        return classes.toArray(new JClass[classes.size()]);
    }
}
