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
import org.jtool.eclipse.javamodel.JavaMethod;
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

/**
 * An object that represents a method outside the project.
 * All methods of this class are not intended to be directly called by clients.
 * 
 * @author Katsuhisa Maruyama
 */
public class ExternalJMethod extends JMethod {
    
    protected CtBehavior ctMethod;
    
    ExternalJMethod(JClass clazz) {
        super(clazz);
    }
    
    ExternalJMethod(CtMethod ctMethod, JClass clazz) {
        super(clazz);
        this.ctMethod = ctMethod;
        
        name = ctMethod.getName();
        signature = ctMethod.getName() + MethodSignature.methodSignatureToString(ctMethod.getSignature());
        fqn = declaringClass.getQualifiedName() + JavaElement.QualifiedNameSeparator + signature;
        returnType = findReturnType(ctMethod);
        isPrimitiveType = checkPrimitiveReturnType(ctMethod);
        kind = JavaMethod.Kind.J_METHOD;
        modifiers = getModfifiers(ctMethod);
    }
    
    public ExternalJMethod(CtConstructor ctMethod, JClass clazz) {
        super(clazz);
        this.ctMethod = ctMethod;
        
        name = ctMethod.getName();
        signature = ctMethod.getName() + MethodSignature.methodSignatureToString(ctMethod.getSignature());
        fqn = declaringClass.getQualifiedName() + JavaElement.QualifiedNameSeparator + signature;
        returnType = declaringClass.getQualifiedName();
        isPrimitiveType = false;
        if (ctMethod.isClassInitializer()) {
            kind = JavaMethod.Kind.J_INITIALIZER;
        } else {
            kind = JavaMethod.Kind.J_CONSTRUCTOR;
        }
        modifiers = getModfifiers(ctMethod);
    }
    
    private String findReturnType(CtMethod ctMethod) {
        try {
            if (ctMethod.getReturnType().equals(CtClass.voidType)) {
                return "void";
            }
            return ctMethod.getReturnType().getName();
        } catch (NotFoundException e) {
            return "";
        }
    }
    
    private boolean checkPrimitiveReturnType(CtMethod ctMethod) {
        try {
            return ctMethod.getReturnType().isPrimitive();
        } catch (NotFoundException e) {
            return false;
        }
    }
    
    private int getModfifiers(CtBehavior ctMethod) {
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
    
    @Override
    public boolean isInProject() {
        return false;
    }
    
    @Override
    protected JMethod[] findAccessedMethods() {
        List<JMethod> methods = new ArrayList<JMethod>();
        try {
            ctMethod.instrument(new ExprEditor() {
                
                @Override
                public void edit(MethodCall cm) throws CannotCompileException {
                    try {
                        JClass clazz = JInfoStore.getInstance().getJClass(cm.getClassName());
                        JMethod method = clazz.getMethod(cm.getSignature());
                        methods.add(method);
                    } catch (ClassCastException e) {
                        // javassit's bug related to invocation for Lambda interface methods
                        // javassist.bytecode.InterfaceMethodrefInfo cannot be cast to javassist.bytecode.MethodrefInfo
                    }
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
    
    @Override
    protected JField[] findAccessedFields() {
        List<JField> fields = new ArrayList<JField>();
        try {
            ctMethod.instrument(new ExprEditor() {
                
                @Override
                public void edit(FieldAccess cf) throws CannotCompileException {
                    JClass clazz = JInfoStore.getInstance().getJClass(cf.getClassName());
                    JField field = clazz.getField(cf.getFieldName());
                    fields.add(field);
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
    protected void checkSideEffectsOnFields(Set<JMethod> visitedMethods) {
        try {
            ctMethod.instrument(new ExprEditor() {
                
                @Override
                public void edit(FieldAccess cf) throws CannotCompileException {
                    if (cf.isWriter()) {
                        sideEffects = SideEffectStatus.YES;
                        return;
                    }
                }
            });
        } catch (CannotCompileException e) {
            if (sideEffects == SideEffectStatus.UNKNOWM) {
                sideEffects = SideEffectStatus.MAYBE;
            }
        }
    }
}
