/*
 *  Copyright 2018-2020
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg.builder;

import org.jtool.eclipse.javamodel.builder.BytecodeClassStore;
import java.io.File;

/**
 * Obtains the signature string for a method or a field from its bytecode signature descriptor.
 * All methods of this class are not intended to be directly called by clients.
 * 
 * MethodDescriptor    := ( ParameterDescriptor* ) ReturnDescriptor
 * ParameterDescriptor := FieldType
 * ReturnDescriptor    := FieldType | VoidDescriptor
 * VoidDescriptor      := V
 *
 * FieldDescriptor := FieldType
 * FieldType       := BaseType | ObjectType | ArrayType
 * BaseType        := B | C | D | F | I | J | S | Z
 * ObjectType      := L ClassName ;
 * ArrayType       := [ ComponentType
 * ComponentType   := FieldType
 * 
 * @author Katsuhisa Maruyama
 */
class MethodSignature {
    
    private static int comsumed_chars_length;
    
    static final String INVALID_SIGNATURE = "!";
    
    protected static String methodSignatureToString(String signature, CFGStore cfgStore) {
        StringBuilder buf = new StringBuilder();
        if (signature.charAt(0) == '(') {
            buf.append("(");
        } else {
            return INVALID_SIGNATURE;
        }
        
        int index = 1;
        while (signature.charAt(index) != ')') {
            String type = variableTypeToString(signature.substring(index), cfgStore);
            buf.append(" ");
            buf.append(type);
            index = index + comsumed_chars_length;
        }
        buf.append(" )");
        
        /*
         * Without a return type
         *
        index++;
        if (signature.charAt(index) != 'V') {
            buf.append(": void");
        } else {
            String type = fieldSignatureToString(signature.substring(index), cfgStore);
            buf.append(":");
            buf.append(type);
        }
        */
        
        return buf.toString();
    }
    
    private static String fieldSignatureToString(String signature, CFGStore cfgStore) {
        return variableTypeToString(signature, cfgStore);
    }
    
    private static String variableTypeToString(String signature, CFGStore cfgStore) {
        comsumed_chars_length = 1;
        switch (signature.charAt(0)) {
            case 'B':
                return "byte";
            case 'C':
                return "char";
            case 'D':
                return "double";
            case 'F':
                return "float";
            case 'I':
                return "int";
            case 'J':
                return "long";
            case 'S':
                return "short";
            case 'Z':
                return "boolean";
                
            case 'L':
                int index = signature.indexOf(';');
                if (index < 0) {
                    return INVALID_SIGNATURE;
                }
                comsumed_chars_length = index + 1;
                String className = signature.substring(1, index).replace(File.separatorChar, '.');
                return getCanonicalClassName(className, cfgStore);
                
            case '[':
                StringBuilder brackets = new StringBuilder();
                int count;
                for (count = 0; signature.charAt(count) == '['; count++) {
                    brackets.append("[]");
                }
                String type = fieldSignatureToString(signature.substring(count), cfgStore);
                comsumed_chars_length = comsumed_chars_length + count;
                return type + brackets.toString();
                
            default:
                return INVALID_SIGNATURE;
        }
    }
    
    private static String getCanonicalClassName(String className, CFGStore cfgStore) {
        BytecodeClassStore bytecodeClassStore = cfgStore.getJavaProject().getBytecodeClassStore();
        if (bytecodeClassStore != null) {
            String cname = bytecodeClassStore.getCanonicalClassName(cfgStore.getJavaProject(), className);
            if (cname != null) {
                return cname;
            }
        }
        return INVALID_SIGNATURE;
    }
}
