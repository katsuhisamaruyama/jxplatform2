/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg.builder;

import static org.jtool.eclipse.javamodel.JavaElement.QualifiedNameSeparator;
import javassist.CtClass;
import javassist.CtBehavior;
import javassist.CtMethod;
import javassist.CtConstructor;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.CannotCompileException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import javassist.expr.ConstructorCall;
import javassist.expr.FieldAccess;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

/**
 * An object that represents a method outside the project.
 * All methods of this class are not intended to be directly called by clients.
 * 
 * @author Katsuhisa Maruyama
 */
public class JMethodExternal extends JMethod {
    
    protected CtBehavior ctMethod;
    
    JMethodExternal(CtMethod ctMethod, JClass declaringClass, CFGStore cfgStore) {
        super(declaringClass.getQualifiedName(), cfgStore, declaringClass.getQualifiedName(), getSignature(ctMethod),
              getModfifiers(ctMethod), findReturnType(ctMethod), checkPrimitiveReturnType(ctMethod));
        fqn = fqn + QualifiedNameSeparator + signature;
        this.declaringClass = declaringClass;
        this.ctMethod = ctMethod;
    }
    
    public JMethodExternal(CtConstructor ctMethod, JClass declaringClass, CFGStore cfgStore) {
        super(declaringClass.getQualifiedName(), cfgStore, declaringClass.getQualifiedName(), getSignature(ctMethod),
              getModfifiers(ctMethod), declaringClass.getQualifiedName(), false);
        fqn = fqn + QualifiedNameSeparator + signature;
        this.declaringClass = declaringClass;
        this.ctMethod = ctMethod;
    }
    
    private static String getSignature(CtBehavior ctMethod) {
        return ctMethod.getName() + MethodSignature.methodSignatureToString(ctMethod.getSignature());
    }
    
    private static int getModfifiers(CtBehavior ctMethod) {
        if (Modifier.isPublic(ctMethod.getModifiers())) {
            return org.eclipse.jdt.core.dom.Modifier.PUBLIC;
        } else if (Modifier.isProtected(ctMethod.getModifiers())) {
            return org.eclipse.jdt.core.dom.Modifier.PROTECTED;
        } else if (Modifier.isPrivate(ctMethod.getModifiers())) {
            return org.eclipse.jdt.core.dom.Modifier.PRIVATE;
        } else {
            return org.eclipse.jdt.core.dom.Modifier.DEFAULT;
        }
    }
    
    private static String findReturnType(CtMethod ctMethod) {
        try {
            if (ctMethod.getReturnType().equals(CtClass.voidType)) {
                return "void";
            }
            return ctMethod.getReturnType().getName();
        } catch (NotFoundException e) {
            return "";
        }
    }
    
    private static boolean checkPrimitiveReturnType(CtMethod ctMethod) {
        try {
            return ctMethod.getReturnType().isPrimitive();
        } catch (NotFoundException e) {
            return false;
        }
    }
    
    @Override
    public boolean isInProject() {
        return false;
    }
    
    @Override
    protected JMethod[] findAccessedMethods() {
        Set<JMethod> methods = new HashSet<JMethod>();
        try {
            ctMethod.instrument(new ExprEditor() {
                
                @Override
                public void edit(MethodCall cm) throws CannotCompileException {
                    try {
                        JClass clazz = cfgStore.getJInfoStore().getJClass(cm.getClassName());
                        if (clazz != null) {
                            JMethod method = clazz.getMethod(JMethodExternal.getSignature(cm.getMethod()));
                            if (method != null) {
                                methods.add(method);
                            }
                        }
                    } catch (ClassCastException e) {
                        // javassit's bug related to invocation for Lambda interface methods
                        // javassist.bytecode.InterfaceMethodrefInfo cannot be cast to javassist.bytecode.MethodrefInfo
                    } catch (NotFoundException e) { /* empty */ }
                }
                
                @Override
                public void edit(ConstructorCall cm) throws CannotCompileException {
                    try {
                        JClass clazz = cfgStore.getJInfoStore().getJClass(cm.getClassName());
                        if (clazz != null) {
                            JMethod method = clazz.getMethod(JMethodExternal.getSignature(cm.getMethod()));
                            if (method != null) {
                                methods.add(method);
                            }
                        }
                    } catch (NotFoundException e) { /* empty */ }
                }
            });
        } catch (CannotCompileException e) { /* empty */ }
        return methods.toArray(new JMethod[methods.size()]);
    }
    
    @Override
    protected JField[] findAccessedFields() {
        Set<JField> fields = new HashSet<JField>();
        try {
            ctMethod.instrument(new ExprEditor() {
                
                @Override
                public void edit(FieldAccess cf) throws CannotCompileException {
                    JClass clazz = cfgStore.getJInfoStore().getJClass(cf.getClassName());
                    if (clazz != null) {
                        JField field = clazz.getField(cf.getFieldName());
                        if (field != null) {
                            fields.add(field);
                        }
                    }
                }
            });
        } catch (CannotCompileException e) { /* empty */ }
        return fields.toArray(new JField[fields.size()]);
    }
    
    @Override
    protected JMethod[] findOverridingMethods() {
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
    
    @Override
    protected JMethod[] findOverriddenMethods() {
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
    
    @Override
    protected void findDefUseFieldsInThisMethod(Set<JMethod> visited, boolean recursivelyCollect) {
        try {
            ctMethod.instrument(new ExprEditor() {
                
                @Override
                public void edit(FieldAccess cf) throws CannotCompileException {
                    if (cf.isWriter()) {
                        addDefField(cf.getClassName() + QualifiedNameSeparator + cf.getFieldName());
                    }
                    if (cf.isReader()) {
                        addUseField(cf.getClassName() + QualifiedNameSeparator + cf.getFieldName());
                    }
                }
            });
        } catch (CannotCompileException e) {
            addDefField(UNKNOWN_FIELD_NAME);
            addDefField(UNKNOWN_FIELD_NAME);
        }
    }
    
    @Override
    protected void findDefUseFieldsInAccessedMethods(Set<JMethod> visited, boolean recursivelyCollect) {
        Set<JMethod> current = new HashSet<JMethod>(visited);
        
        for (JMethod method : getOverridingMethods()) {
            if (!visited.contains(method)) {
                visited.add(method);
                method.findDefUseFields(visited, false);
            }
        }
        
        if (!recursivelyCollect) {
            for (JMethod method : visited) {
                if (!current.contains(method)) {
                    addDefFields(method.getDefFields());
                    addUseFields(method.getUseFields());
                }
            }
            return;
        }
        
        for (JMethod method : getAccessedMethods()) {
            if (!visited.contains(method)) {
                visited.add(method);
                method.findDefUseFields(visited, false);
            }
        }
        
        for (JMethod method : visited) {
            if (!current.contains(method)) {
                addDefFields(method.getDefFields());
                addUseFields(method.getUseFields());
            }
        }
    }
}
