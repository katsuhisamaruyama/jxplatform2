/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg.builder;

import org.jtool.eclipse.cfg.CFGStore;
import org.jtool.eclipse.cfg.JReference;
import org.jtool.eclipse.cfg.CFG;
import org.jtool.eclipse.cfg.CFGNode;
import org.jtool.eclipse.cfg.CFGStatement;
import org.jtool.eclipse.cfg.JClass;
import org.jtool.eclipse.cfg.JMethod;
import org.jtool.eclipse.cfg.JField;
import org.jtool.eclipse.javamodel.JavaField;
import org.jtool.eclipse.javamodel.JavaMethod;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

/**
 * An object that represents a method inside the project.
 * All methods of this class are not intended to be directly called by clients.
 * 
 * @author Katsuhisa Maruyama
 */
public class InternalJMethod extends JMethod {
    
    protected JavaMethod jmethod;
    
    public InternalJMethod(JClass clazz, JavaMethod jmethod) {
        this.jmethod = jmethod;
        declaringClass = clazz;
    }
    
    public JavaMethod getJavaMethod() {
        return jmethod;
    }
    
    @Override
    public String getName() {
        return jmethod.getName();
    }
    
    @Override
    public String getQualifiedName() {
        return jmethod.getQualifiedName();
    }
    
    @Override
    public String getSignature() {
        return jmethod.getSignature();
    }
    
    @Override
    public String getReturnType() {
        return jmethod.getReturnType();
    }
    
    @Override
    public boolean isPrimitiveReturnType() {
        return jmethod.isPrimitiveReturnType();
    }
    
    @Override
    public boolean isVoid() {
        return jmethod.isVoid();
    }
    
    @Override
    public boolean isMethod() {
        return jmethod.isMethod();
    }
    
    @Override
    public boolean isConstructor() {
        return jmethod.isConstructor();
    }
    
    @Override
    public boolean isInitializer() {
        return jmethod.isInitializer();
    }
    
    @Override
    public boolean isPublic() {
        return jmethod.isPublic();
    }
    
    @Override
    public boolean isProtected() {
        return jmethod.isProtected();
    }
    
    public boolean isPrivate() {
        return jmethod.isPrivate();
    }
    
    @Override
    public boolean isDefault() {
        return jmethod.isDefault();
    }
    
    @Override
    public boolean isInProject() {
        return true;
    }
    
    @Override
    protected JMethod[] findAccessedMethods() {
        List<JMethod> methods = new ArrayList<JMethod>();
        for (JavaMethod jm : jmethod.getCalledMethods()) {
            JClass clazz = JInfoStore.getInstance().getJClass(jm.getDeclaringClass().getQualifiedName());
            JMethod method = clazz.getMethod(jm.getSignature());
            if (method != null) {
                methods.add(method);
            }
        }
        return methods.toArray(new JMethod[methods.size()]);
    }
    
    @Override
    protected JField[] findAccessedFields() {
        List<JField> fields = new ArrayList<JField>();
        for (JavaField jf : jmethod.getAccessedFields()) {
            JClass clazz = JInfoStore.getInstance().getJClass(jf.getDeclaringClass().getQualifiedName());
            JField field = clazz.getField(jf.getName());
            if (field != null) {
                fields.add(field);
            }
        }
        return fields.toArray(new JField[fields.size()]);
    }
    
    @Override
    protected JMethod[] findOverridingMethods() {
        List<JMethod> methods = new ArrayList<JMethod>();
        for (JavaMethod jm : jmethod.getOverridingMethods()) {
            JClass clazz = JInfoStore.getInstance().getJClass(jm.getDeclaringClass().getQualifiedName());
            JMethod method = clazz.getMethod(jm.getSignature());
            if (method != null) {
                methods.add(method);
            }
        }
        return methods.toArray(new JMethod[methods.size()]);
    }
    
    @Override
    protected JMethod[] findOverriddenMethods() {
        List<JMethod> methods = new ArrayList<JMethod>();
        for (JavaMethod jm : jmethod.getOverriddenMethods()) {
            JClass clazz = JInfoStore.getInstance().getJClass(jm.getDeclaringClass().getQualifiedName());
            JMethod method = clazz.getMethod(jm.getSignature());
            if (method != null) {
                methods.add(method);
            }
        }
        return methods.toArray(new JMethod[methods.size()]);
    }
    
    @Override
    protected void checkSideEffectsOnFields(Set<JMethod> visitedMethods) {
        CFG cfg = CFGStore.getInstance().getCFG(jmethod, visitedMethods);
        for (CFGNode node : cfg.getNodes()) {
            if (node instanceof CFGStatement) {
                CFGStatement stNode = (CFGStatement)node;
                for (JReference jv : stNode.getDefVariables()) {
                    if (jv.isFieldAccess()) {
                        sideEffects = SideEffectStatus.YES;
                        return;
                    }
                }
            }
        }
    }
}
