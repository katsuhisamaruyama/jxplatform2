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
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Obtains path information from the Ant setting.
 * 
 * @author Katsuhisa Maruyama
 */
class AntPathInfo extends ProjectPathInfo {
    
    AntPathInfo(String target) {
        super(target);
    }
    
    void setPaths() throws IOException {
        Path configPath = base.resolve(Paths.get("build.xml"));
        if (!configPath.toFile().exists()) {
            throw new IOException();
        }
        
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        ConfigParser parser = new ConfigParser();
        
        try {
            SAXParser saxParser = saxParserFactory.newSAXParser();
            saxParser.parse(configPath.toString(), parser);
        } catch (IOException | ParserConfigurationException | SAXException e) { /* empty */ }
        
        parser.setPaths();
        srcpath = parser.getSrcPaths();
        binpath = parser.getBinPath();;
        classpath = parser.getClassPaths();
    }
    
    private class ConfigParser extends DefaultHandler {
        private Map<String, String> properties = new HashMap<String, String>();
        private boolean isClasspathElem = false;
        
        private List<String> spaths = new ArrayList<String>();
        private String bpath = null;
        private List<String> cpaths = new ArrayList<String>();
        
        private List<String> srcPaths = new ArrayList<String>();
        private String binPath = null;
        private List<String> classPaths = new ArrayList<String>();
        
        @Override
        public void startElement(String uri, String lname, String qname, Attributes attr) {
            if (qname.equals("property") && attr != null) {
                String name = attr.getValue("name");
                String value = attr.getValue("value");
                if (name != null && value != null) {
                    String cvalue = replace(value);
                    if (cvalue != null) {
                        properties.put(name, cvalue);
                    }
                }
                
            } else if (qname.equals("javac") && attr != null) {
                String srcdir = attr.getValue("srcdir");
                if (srcdir != null) {
                    spaths.add(srcdir);
                }
                String destdir = attr.getValue("destdir");
                if (destdir != null) {
                    bpath = destdir;
                }
                
            } else if (qname.contentEquals("path") && attr != null) {
                String id = attr.getValue("id");
                if (id != null && id.equals("classpath")) {
                    isClasspathElem = true;
                }
                
            } else if (isClasspathElem && qname.contentEquals("fileset")) {
                String path = attr.getValue("dir");
                if (path != null) {
                    cpaths.add(base.resolve(path).toString());
                }
            }
        }
        
        @Override
        public void endElement (String uri, String lname, String qname) {
            if (isClasspathElem && qname.equals("path")) {
                isClasspathElem = false;
            }
        }
        
        private String replace(String value) {
            int beginIndex = value.indexOf("${", 0);
            while (beginIndex != -1) {
                int endIndex = value.indexOf("}", beginIndex + 1);
                if (endIndex == -1) {
                    return null;
                }
                String key = value.substring(beginIndex + 2, endIndex);
                String cvalue = properties.get(key);
                if (cvalue != null) {
                    value = value.substring(0, beginIndex) + cvalue + value.substring(endIndex + 1);
                } else {
                    return null;
                }
                beginIndex = value.indexOf("${", 0);
            }
            return value;
        }
        
        private void setPaths() {
            for (String path : spaths) {
                path = replace(path);
                srcPaths.add(base.resolve(path).toString());
            }
            for (String path : cpaths) {
                path = replace(path);
                classPaths.add(base.resolve(path).toString());
            }
            classPaths.add(base.resolve("lib").toString());
            if (bpath != null) {
                String path = replace(bpath);
                binPath = base.resolve(path).toString();
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
                paths = ModelBuilderBatch.getClassPath(classPathStr);
            }
            return paths;
        }
    };
}
