/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.util;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Writes the contents of a file.
 * @author Katsuhisa Maruyama
 */
public class FileWriter {
    
    public static void write(String name, String text) throws FileNotFoundException, IOException {
        write(new File(name), text);
    }
    
    public static void write(String name, String text, String charsetName)
            throws FileNotFoundException, IOException, UnsupportedEncodingException {
        write(new File(name), text, charsetName);
    }
    
    public static void write(File file, String text) throws FileNotFoundException, IOException {
        write(new OutputStreamWriter(new FileOutputStream(file)), text);
    }
    
    public static void write(File file, String text, String charsetName)
            throws FileNotFoundException, IOException, UnsupportedEncodingException {
        write(new OutputStreamWriter(new FileOutputStream(file), charsetName), text);
    }
    
    private static void write(Writer writer, String text) throws IOException, UnsupportedEncodingException {
        BufferedWriter bwriter = new BufferedWriter(writer);
        bwriter.write(text);
        bwriter.flush();
        bwriter.close();
    }
}
