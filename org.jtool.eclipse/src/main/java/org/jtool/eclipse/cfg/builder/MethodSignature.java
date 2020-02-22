/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg.builder;

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
    
    protected static String methodSignatureToString(String signature) {
        StringBuilder buf = new StringBuilder();
        if (signature.charAt(0) == '(') {
            buf.append("(");
        } else {
            return INVALID_SIGNATURE;
        }
        int index = 1;
        while (signature.charAt(index) != ')') {
            String type = variableTypeToString(signature.substring(index));
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
            String type = fieldSignatureToString(signature.substring(index));
            buf.append(":");
            buf.append(type);
        }
        */
        
        return buf.toString();
    }
    
    protected static String fieldSignatureToString(String signature) {
        return variableTypeToString(signature);
    }
    
    protected static String variableTypeToString(String signature) {
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
                return signature.substring(1, index).replace(File.separatorChar, '.');
                
            case '[':
                StringBuilder brackets = new StringBuilder();
                int count;
                for (count = 0; signature.charAt(count) == '['; count++) {
                    brackets.append("[]");
                }
                String type = fieldSignatureToString(signature.substring(count));
                comsumed_chars_length = comsumed_chars_length + count;
                return type + brackets.toString();
                
            default:
                return INVALID_SIGNATURE;
        }
    }
}
