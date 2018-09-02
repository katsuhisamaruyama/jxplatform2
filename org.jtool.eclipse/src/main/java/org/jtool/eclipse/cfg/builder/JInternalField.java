/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg.builder;

import org.jtool.eclipse.cfg.JClass;
import org.jtool.eclipse.cfg.JMethod;
import org.jtool.eclipse.cfg.JField;
import org.jtool.eclipse.javamodel.JavaMethod;
import org.jtool.eclipse.javamodel.JavaField;
import java.util.List;
import java.util.ArrayList;

/**
 * An object that represents a field inside the project.
 * All methods of this class are not intended to be directly called by clients.
 * 
 * @author Katsuhisa Maruyama
 */
public class JInternalField extends JField {
    
    protected JavaField jfield;
    
    public JInternalField(JClass clazz, JavaField jfield) {
        this.jfield = jfield;
        declaringClass = clazz;
    }
    
    public JavaField getJavaField() {
        return jfield;
    }
    
    @Override
    public String getName() {
        return jfield.getName();
    }
    
    @Override
    public String getQualifiedName() {
        return jfield.getQualifiedName();
    }
    
    @Override
    public String getType() {
        return jfield.getType();
    }
    
    @Override
    public boolean isPrimitiveType() {
        return jfield.isPrimitiveType();
    }
    
    @Override
    public boolean isPublic() {
        return jfield.isPublic();
    }
    
    @Override
    public boolean isProtected() {
        return jfield.isProtected();
    }
    
    @Override
    public boolean isPrivate() {
        return jfield.isPrivate();
    }
    
    @Override
    public boolean isDefault() {
        return jfield.isDefault();
    }
    
    @Override
    public boolean isInProject() {
        return true;
    }
    
    void collectInfo() {
        accessedMethods = findAccessedMethods();
        accessedFields = findAccessedFields();
    }
    
    private JMethod[] findAccessedMethods() {
        List<JMethod> methods = new ArrayList<JMethod>();
        for (JavaMethod jm : jfield.getCalledMethods()) {
            JClass clazz = JInfoStore.getInstance().getJClass(jm.getDeclaringClass().getQualifiedName());
            JMethod method = clazz.getMethod(jm.getSignature());
            if (method != null) {
                methods.add(method);
            }
        }
        return methods.toArray(new JMethod[methods.size()]);
    }
    
    private JField[] findAccessedFields() {
        List<JField> fields = new ArrayList<JField>();
        for (JavaField jf : jfield.getAccessedFields()) {
            JClass clazz = JInfoStore.getInstance().getJClass(jf.getDeclaringClass().getQualifiedName());
            JField field = clazz.getField(jf.getName());
            if (field != null) {
                fields.add(field);
            }
        }
        return fields.toArray(new JField[fields.size()]);
    }
}
