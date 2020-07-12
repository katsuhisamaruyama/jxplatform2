/*
 *  Copyright 2018-2020
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg.builder;

import org.jtool.eclipse.javamodel.JavaClass;
import org.jtool.eclipse.javamodel.JavaField;
import org.jtool.eclipse.javamodel.JavaMethod;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * An object that represents a class inside the project.
 * All methods of this class are not intended to be directly called by clients.
 * 
 * @author Katsuhisa Maruyama
 */
public class JClassInternal extends JClass {
    
    private JavaClass jclass;
    
    JClassInternal(JavaClass jclass, CFGStore cfgStore) {
        super(jclass.getQualifiedName(), jclass.getName(), jclass.getModifiers(), jclass.isInterface(), cfgStore);
        this.jclass = jclass;
        
        List<JavaMethod> jms = jclass.getMethods().stream()
                                                  .filter(jmethod -> jmethod.getKind() != JavaMethod.Kind.UNKNOWN)
                                                  .collect(Collectors.toList());
        int num = 0;
        methods = new JMethodInternal[jms.size()];
        for (JavaMethod jmethod : jms ) {
            methods[num] = new JMethodInternal(jmethod, this, cfgStore);
            num++;
        }
        
        List<JavaField> jfs = jclass.getFields().stream()
                                                .filter(jfield -> jfield.getKind() != JavaField.Kind.UNKNOWN)
                                                .collect(Collectors.toList());
        num = 0;
        fields = new JFieldInternal[jfs.size()];
        for (JavaField jfield : jfs) {
            fields[num] = new JFieldInternal(jfield, this, cfgStore);
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
        List<JClass> classes = new ArrayList<>();
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
