/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.metrics;

import org.jtool.eclipse.util.DetectCharset;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

/**
 * Manages source code information within a Java project.
 * @author Katsuhisa Maruyama
 */
public class SourceCodeStore {
    
    private static SourceCodeStore instance = new SourceCodeStore();
    
    protected Map<String, SourceCodeInfo> sourceCodeStore = new HashMap<String, SourceCodeInfo>();
    
    private SourceCodeStore() {
    }
    
    public static SourceCodeStore getInstance() {
        return instance;
    }
    
    public String get(String path) {
        SourceCodeInfo sinfo = sourceCodeStore.get(path);
        if (sinfo == null) {
            sinfo = readJavaFile(new File(path));
            if (sinfo != null) {
                return sinfo.content;
            }
        }
        return "";
    }
    
    public String getCharset(String path) {
        SourceCodeInfo sinfo = sourceCodeStore.get(path);
        if (sinfo != null) {
            return sinfo.charset;
        }
        return "UTF-8";
    }
    
    public void collectAllJavaFiles(String path) {
        File resource = new File(path);
        if (resource.isFile()) {
            readJavaFile(resource);
        } else if (resource.isDirectory()) {
            for (File res : resource.listFiles()) {
                collectAllJavaFiles(res.getPath());
            }
        }
    }
    
    private SourceCodeInfo readJavaFile(File resource) {
        if (resource.getName().endsWith(".java")) {
            try {
                String content = read(resource);
                String charset = DetectCharset.getCharsetName(content.getBytes());
                SourceCodeInfo sinfo = new SourceCodeInfo(content, charset);
                sourceCodeStore.put(resource.getAbsolutePath(), sinfo);
                return sinfo;
            } catch (IOException e) {
                return null;
            }
        }
        return null;
    }
    
    private String read(File file) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            char[] buf = new char[10];
            int count = 0;
            while ((count = reader.read(buf)) != -1) {
                String data = String.valueOf(buf, 0, count);
                content.append(data);
                buf = new char[1024];
            }
        }
        return  content.toString();
    }
    
    class SourceCodeInfo {
        String content;
        String charset;
        
        SourceCodeInfo(String content, String charset) {
            this.content = content;
            this.charset = charset;
        }
    }
}