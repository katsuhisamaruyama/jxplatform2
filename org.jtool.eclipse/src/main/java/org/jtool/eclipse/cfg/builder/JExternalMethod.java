/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg.builder;

import org.jtool.eclipse.cfg.JClass;
import org.jtool.eclipse.cfg.JMethod;
import org.jtool.eclipse.cfg.JField;
import org.jtool.eclipse.javamodel.JavaElement;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.CannotCompileException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import javassist.expr.ConstructorCall;
import javassist.expr.FieldAccess;
import java.util.List;
import java.util.ArrayList;

/**
 * An object that represents a method outside the project.
 * All methods of this class are not intended to be directly called by clients.
 * 
 * @author Katsuhisa Maruyama
 */
public class JExternalMethod extends JMethod {
    
    protected CtMethod cmethod;
    
    public JExternalMethod(JClass clazz, CtMethod cmethod) {
        this.cmethod = cmethod;
        declaringClass = clazz;
    }
    
    public CtMethod getCtMethod() {
        return cmethod;
    }
    
    @Override
    public String getName() {
        return cmethod.getName();
    }
    
    @Override
    public String getQualifiedName() {
        return declaringClass.getQualifiedName() + JavaElement.QualifiedNameSeparator + cmethod.getSignature();
    }
    
    @Override
    public String getSignature() {
        return cmethod.getSignature();
    }
    
    @Override
    public String getReturnType() {
        try {
            return cmethod.getReturnType().getName();
        } catch (NotFoundException e) {
            return "";
        }
    }
    
    @Override
    public boolean isPrimitiveReturnType() {
        try {
            return cmethod.getReturnType().isPrimitive();
        } catch (NotFoundException e) {
            return false;
        }
    }
    
    @Override
    public boolean isVoid() {
        try {
            return cmethod.getReturnType().equals(CtClass.voidType);
        } catch (NotFoundException e) {
            return false;
        }
    }
    
    @Override
    public boolean isMethod() {
        return true;
    }
    
    @Override
    public boolean isConstructor() {
        return false;
    }
    
    @Override
    public boolean isInitializer() {
        return false;
    }
    
    @Override
    public boolean isPublic() {
        return Modifier.isPublic(cmethod.getModifiers());
    }
    
    @Override
    public boolean isProtected() {
        return Modifier.isProtected(cmethod.getModifiers());
    }
    
    @Override
    public boolean isPrivate() {
        return Modifier.isPrivate(cmethod.getModifiers());
    }
    
    @Override
    public boolean isDefault() {
        return !isPublic() && !isProtected() && !isPrivate();
    }
    
    @Override
    public boolean isInProject() {
        return false;
    }
    
    void collectInfo() {
        accessedMethods = findAccessedMethods();
        accessedFields = findAccessedFields();
        overrindingMethods = findOverridingMethods();
        overriddenMethods = findOverriddenMethods();
    }
    
    private JMethod[] findAccessedMethods() {
        List<JMethod> methods = new ArrayList<JMethod>();
        try {
            cmethod.instrument(new ExprEditor() {
                
                @Override
                public void edit(MethodCall cm) throws CannotCompileException {
                    JClass clazz = JInfoStore.getInstance().getJClass(cm.getClassName());
                    JMethod method = clazz.getMethod(cm.getSignature());
                    methods.add(method);
                }
                
                @Override
                public void edit(ConstructorCall cm) throws CannotCompileException {
                    JClass clazz = JInfoStore.getInstance().getJClass(cm.getClassName());
                    JMethod method = clazz.getMethod(cm.getSignature());
                    methods.add(method);
                }
            });
        } catch (CannotCompileException e) { /* empty */ }
        return methods.toArray(new JMethod[methods.size()]);
    }
    
    private JField[] findAccessedFields() {
        List<JField> fields = new ArrayList<JField>();
        try {
            cmethod.instrument(new ExprEditor() {
                
                public void edit(FieldAccess cf) throws CannotCompileException {
                    JClass clazz = JInfoStore.getInstance().getJClass(cf.getClassName());
                    JField field = clazz.getField(cf.getFieldName());
                    fields.add(field);
                }
            });
        } catch (CannotCompileException e) { /* empty */ }
        return fields.toArray(new JField[fields.size()]);
    }
    
    private JMethod[] findOverridingMethods() {
        List<JMethod> methods = new ArrayList<JMethod>();
        for (JClass clazz : declaringClass.getDescendants()) {
            for (JMethod method : clazz.getMethods()) {
                if (!isPrivate() && getSignature().equals(method.getSignature())) {
                    methods.add(method);
                }
            }
        }
        return methods.toArray(new JMethod[methods.size()]);
    }
    
    private JMethod[] findOverriddenMethods() {
        List<JMethod> methods = new ArrayList<JMethod>();
        for (JClass clazz : declaringClass.getAncestors()) {
            for (JMethod method : clazz.getMethods()) {
                if (!isPrivate() && getSignature().equals(method.getSignature())) {
                    methods.add(method);
                }
            }
        }
        return methods.toArray(new JMethod[methods.size()]);
    }
}
