/*
 *  Copyright 2020
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.batch;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Obtains path information from the Eclipse setting.
 * 
 * @author Katsuhisa Maruyama
 */
class EclipsePathInfo extends ProjectPathInfo {
    
    EclipsePathInfo(String target) {
        super(target);
    }
    
    void setPaths() throws IOException {
        Path configPath = base.resolve(Paths.get(".classpath"));
        if (!configPath.toFile().exists()) {
            throw new IOException();
        }
        
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        ConfigParser parser = new ConfigParser();
        try {
            SAXParser saxParser = saxParserFactory.newSAXParser();
            saxParser.parse(configPath.toString(), parser);
        } catch (IOException | ParserConfigurationException | SAXException e) { /* empty */ }
        srcpath = parser.getSrcPaths();
        binpath = parser.getBinPath();;
        classpath = parser.getClassPaths();
    }
    
    String getProjectPath() {
        return projectPath;
    }
    
    String[] getSrcPath() {
        return srcpath;
    }
    
    String getBinPath() {
        return binpath;
    }
    
    String[] getClassPath() {
        return classpath;
    }
    
    private class ConfigParser extends DefaultHandler {
        private List<String> srcPaths = new ArrayList<String>();
        private String binPath = null;
        private List<String> classPaths = new ArrayList<String>();
        
        @Override
        public void startElement(String uri, String lname, String qname, Attributes attr) {
            if (qname.equals("classpathentry") && attr != null) {
                if (attr.getValue("kind").equals("src")) {
                    Path path = Paths.get(attr.getValue("path"));
                    srcPaths.add(base.resolve(path).toString());
                } else if (attr.getValue("kind").equals("output")) {
                    Path path = Paths.get(attr.getValue("path"));
                    binPath = base.resolve(path).toString();
                } else if (attr.getValue("kind").equals("lib")) {
                    Path path = Paths.get(attr.getValue("path"));
                    if (path.isAbsolute()) {
                        classPaths.add(path.toString());
                    } else {
                        classPaths.add(base.resolve(path).toString());
                    }
                }
            }
        }
        
        private String[] getSrcPaths() {
            String[] paths;
            if (srcPaths.size() == 0) {
                paths = new String[1];
                paths[0] = base.toString();
            } else {
                paths = (String[])srcPaths.toArray(new String[srcPaths.size()]);
            }
            return paths;
        }
        
        private String getBinPath() {
            if (binPath != null) {
                return binPath;
            } else {
                return base.resolve("bin").toString();
            }
        }
        
        private String[] getClassPaths() {
            String[] paths;
            if (classPaths.size() == 0) {
                paths = ModelBuilderBatch.getClassPath(base.toString() + File.separator + "lib/*");
            } else {
                String classPathStr = "";
                for (String path : classPaths) {
                    if (path.endsWith(".jar")) {
                        classPathStr = classPathStr + File.pathSeparator + path;
                    } else {
                        classPathStr = classPathStr + File.pathSeparator + path + "/*";
                    }
                }
                if (classPathStr.length() > 0) {
                    classPathStr = classPathStr.substring(1);
                }
                
                System.out.println(classPathStr);
                paths = ModelBuilderBatch.getClassPath(classPathStr);
            }
            return paths;
        }
    };
}
