/*
 *  Copyright 2020
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.batch;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Obtains path information from the Eclipse setting.
 * 
 * @author Katsuhisa Maruyama
 */
class EclipseEnv extends ProjectEnv {
    
    EclipseEnv(String target) {
        super(target);
    }
    
    @Override
    boolean isApplicable() {
        try {
            Path config = basePath.resolve(Paths.get(".classpath"));
            if (config.toFile().exists()) {
                setPaths(config.toString());
                return true;
            }
        } catch (Exception e) { }
        return false;
    }
    
    private void setPaths(String configFile) throws Exception {
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        ConfigParser parser = new ConfigParser();
        SAXParser saxParser = saxParserFactory.newSAXParser();
        saxParser.parse(configFile, parser);
        
        sourcePath = parser.srcpath;
        binaryPath = parser.binpath;
        classPath = parser.classpath;
    }
    
    private class ConfigParser extends DefaultHandler {
        Set<String> srcpath = new HashSet<String>();
        Set<String> binpath = new HashSet<String>();
        Set<String> classpath = new HashSet<String>();
        
        @Override
        public void startElement(String uri, String lname, String qname, Attributes attr) {
            if (qname.equals("classpathentry") && attr != null) {
                if (attr.getValue("kind").equals("src")) {
                    Path path = Paths.get(attr.getValue("path"));
                    String srcPath = basePath.resolve(path).toString();
                    List<File> files = ModelBuilderBatch.collectAllJavaFiles(srcPath.toString());
                    if (files.size() > 0) {
                        srcpath.add(srcPath);
                    }
                    
                } else if (attr.getValue("kind").equals("output")) {
                    Path path = Paths.get(attr.getValue("path"));
                    binpath.add(basePath.resolve(path).toString());
                    
                } else if (attr.getValue("kind").equals("lib")) {
                    Path path = Paths.get(attr.getValue("path"));
                    if (path.isAbsolute()) {
                        classpath.add(path.toString());
                    } else {
                        classpath.add(basePath.resolve(path).toString());
                    }
                }
            }
        }
    }
}
