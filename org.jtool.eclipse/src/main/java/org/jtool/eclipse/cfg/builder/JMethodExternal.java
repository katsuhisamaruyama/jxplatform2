/*
 *  Copyright 2018-2020
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg.builder;

import org.jtool.eclipse.javamodel.builder.BytecodeClassStore;
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
import javassist.expr.NewExpr;
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
        super(declaringClass.getQualifiedName(), declaringClass.getQualifiedName(),
                getMethodSignature(ctMethod, cfgStore), getModfifiers(ctMethod),
                findReturnType(ctMethod), checkPrimitiveReturnType(ctMethod), cfgStore);
        fqn = fqn + QualifiedNameSeparator + signature;
        this.declaringClass = declaringClass;
        this.ctMethod = ctMethod;
    }
    
    public JMethodExternal(CtConstructor ctMethod, JClass declaringClass, CFGStore cfgStore) {
        super(declaringClass.getQualifiedName(), declaringClass.getQualifiedName(),
                getConstructorSignature(ctMethod, cfgStore), getModfifiers(ctMethod),
                declaringClass.getQualifiedName(), false, cfgStore);
        fqn = fqn + QualifiedNameSeparator + signature;
        this.declaringClass = declaringClass;
        this.ctMethod = ctMethod;
    }
    
    private static String getMethodSignature(CtBehavior cm, CFGStore cfgStore) {
        return cm.getName() + MethodSignature.methodSignatureToString(cm.getSignature(), cfgStore);
    }
    
    private static String getConstructorSignature(CtBehavior cm, CFGStore cfgStore) {
        String className = BytecodeClassStore.getCanonicalSimpleClassName(cm.getDeclaringClass()); 
        return className + MethodSignature.methodSignatureToString(cm.getSignature(), cfgStore);
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
        Set<JMethod> methods = new HashSet<>();
        try {
            ctMethod.instrument(new ExprEditor() {
                
                @Override
                public void edit(MethodCall cm) throws CannotCompileException {
                    try {
                        CtClass cc = cm.getMethod().getDeclaringClass();
                        JClass clazz = cfgStore.getJInfoStore().getJClass(BytecodeClassStore.getCanonicalClassName(cc));
                        if (clazz != null) {
                            JMethod method = clazz.getMethod(getMethodSignature(cm.getMethod(), cfgStore));
                            if (method != null) {
                                methods.add(method);
                            }
                        }
                    } catch (NotFoundException e) { /* empty */ }
                }
                
                @Override
                public void edit(ConstructorCall cm) throws CannotCompileException {
                    try {
                        CtClass cc = cm.getMethod().getDeclaringClass();
                        JClass clazz = cfgStore.getJInfoStore().getJClass(BytecodeClassStore.getCanonicalClassName(cc));
                        if (clazz != null) {
                            JMethod method = clazz.getMethod(getConstructorSignature(cm.getMethod(), cfgStore));
                            if (method != null) {
                                methods.add(method);
                            }
                        }
                    } catch (NotFoundException e) { /* empty */ }
                }
                
                @Override
                public void edit(NewExpr cm) throws CannotCompileException {
                    try {
                        CtClass cc = cm.getConstructor().getDeclaringClass();
                        JClass clazz = cfgStore.getJInfoStore().getJClass(BytecodeClassStore.getCanonicalClassName(cc));
                        if (clazz != null) {
                            JMethod method = clazz.getMethod(getConstructorSignature(cm.getConstructor(), cfgStore));
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
        Set<JField> fields = new HashSet<>();
        try {
            ctMethod.instrument(new ExprEditor() {
                
                @Override
                public void edit(FieldAccess cf) throws CannotCompileException {
                    try {
                        CtClass cc = cf.getField().getDeclaringClass();
                        JClass clazz = cfgStore.getJInfoStore().getJClass(BytecodeClassStore.getCanonicalClassName(cc));
                        if (clazz != null) {
                            JField field = clazz.getField(cf.getFieldName());
                            if (field != null) {
                                fields.add(field);
                            }
                        }
                    } catch (NotFoundException e) { /* empty */ }
                }
            });
        } catch (CannotCompileException e) { /* empty */ }
        return fields.toArray(new JField[fields.size()]);
    }
    
    @Override
    protected JMethod[] findOverridingMethods() {
        if (!getDeclaringClass().isInterface()) {
            return new JMethod[0];
        }
        
        List<JMethod> methods = new ArrayList<>();
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
        List<JMethod> methods = new ArrayList<>();
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
                        try {
                            addDefField(new DefOrUseField(cf.getClassName(), cf.getFieldName(),
                                    cf.getField().getType().isPrimitive(), cf.getField().getModifiers()));
                        } catch (NotFoundException e) {
                            addDefField(DefOrUseField.UNKNOWN);
                        }
                    }
                    if (cf.isReader()) {
                        try {
                            addUseField(new DefOrUseField(cf.getClassName(), cf.getFieldName(),
                                    cf.getField().getType().isPrimitive(), cf.getField().getModifiers()));
                        } catch (NotFoundException e) {
                            addDefField(DefOrUseField.UNKNOWN);
                        }
                    }
                }
            });
        } catch (CannotCompileException e) { /* empty */ }
    }
    
    @Override
    protected void findDefUseFieldsInAccessedMethods(Set<JMethod> visited, boolean recursivelyCollect) {
        Set<JMethod> current = new HashSet<>(visited);
        
        for (JMethod method : getOverridingMethods()) {
            if (!visited.contains(method)) {
                method.findDefUseFields(visited, false);
                visited.add(method);
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
                method.findDefUseFields(visited, false);
                visited.add(method);
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
