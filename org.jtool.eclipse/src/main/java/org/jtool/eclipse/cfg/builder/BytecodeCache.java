/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg.builder;

import org.jtool.eclipse.cfg.JClass;
import org.jtool.eclipse.cfg.JMethod;
import org.jtool.eclipse.cfg.JField;
import org.jtool.eclipse.javamodel.JavaProject;
import org.jtool.eclipse.util.TimeInfo;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;

/**
 * Manages the cache of bytecode classes.
 * All methods of this class are not intended to be directly called by clients.
 * 
 * @author Katsuhisa Maruyama
 */
public class BytecodeCache {
    
    private static final String BYTECODE_INFO_FILENAME = ".bytecode.info";
    
    static Map<String, CachedJClass> cachedClasses = new HashMap<String, CachedJClass>();
    static Map<String, CachedJMethod> cachedMethods = new HashMap<String, CachedJMethod>();
    static Map<String, CachedJField> cachedFields = new HashMap<String, CachedJField>();
    
    public static void writeCache(JavaProject jproject, List<ExternalJClass> classes) {
        try {
            String filename = jproject.getPath() + File.separator + BYTECODE_INFO_FILENAME;
            File file = new File(filename);
            if (file.exists()) {
                file.delete();
            }
            
            CacheExporter exporter = new CacheExporter();
            Document doc = exporter.getDocument(jproject, classes);
            write(file, doc);
        } catch (IOException e) {
            System.err.println("IO error " + e.getMessage());
        }
    }
    
    private static void write(File file, Document doc) throws IOException {
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource src = new DOMSource(doc);
            
            StringWriter writer = new StringWriter();
            transformer.transform(src, new StreamResult(writer));
            
            BufferedWriter bwriter = new BufferedWriter(new FileWriter(file));
            bwriter.write(writer.toString());
            bwriter.flush();
            bwriter.close();
        } catch (TransformerException e) {
            System.err.println("DOM: Export error occurred: " + e.getMessage() + ".");
        }
        cachedClasses.clear();
        cachedMethods.clear();
        cachedFields.clear();
    }
    
    public static boolean loadCache(JavaProject jproject) {
        cachedClasses.clear();
        cachedMethods.clear();
        cachedFields.clear();
        
        String filename = jproject.getPath() + File.separator + BYTECODE_INFO_FILENAME;
        File file = new File(filename);
        if (!file.canRead()) {
            return false;
        }
        
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            CacheImporter handler = new CacheImporter();
            parser.parse(file, handler);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            return false;
        }
        return true;
    }
    
    static final String ProjectElem = "project";
    static final String ClassElem = "class";
    static final String MethodElem = "methods";
    static final String FieldElem = "field";
    
    static final String ClassNameAttr = "cname";
    static final String FqnAttr = "fqn";
    static final String PathAttr = "path";
    static final String TimeAttr = "time";
    static final String SignatureAttr = "sig";
    static final String NameAttr = "name";
}

class CacheExporter {
    
    CacheExporter() {
    }
    
    Document getDocument(JavaProject jproject, List<ExternalJClass> classes) {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.newDocument();
            Element projectElem = doc.createElement(BytecodeCache.ProjectElem);
            projectElem.setAttribute(BytecodeCache.NameAttr, jproject.getName());
            projectElem.setAttribute(BytecodeCache.PathAttr, jproject.getPath());
            projectElem.setAttribute(BytecodeCache.TimeAttr, TimeInfo.getTimeAsISOString(TimeInfo.getCurrentTime()));
            doc.appendChild(projectElem);
            
            for (JClass clazz : classes) {
                if (clazz.isTopLevelClass()) {
                    export(doc, projectElem, new CachedJClass(clazz));
                }
            }
            for (JClass clazz : classes) {
                if (clazz.isTopLevelClass()) {
                    for (JMethod method : clazz.getMethods()) {
                        export(doc, projectElem, new CachedJMethod(method));
                    }
                    for (JField field : clazz.getFields()) {
                        export(doc, projectElem, new CachedJField(field));
                    }
                }
            }
            
            for (CachedJClass cclass : BytecodeCache.cachedClasses.values()) {
                export(doc, projectElem, cclass);
            }
            for (CachedJMethod cmethod : BytecodeCache.cachedMethods.values()) {
                export(doc, projectElem, cmethod);
            }
            for (CachedJField cfield : BytecodeCache.cachedFields.values()) {
                export(doc, projectElem, cfield);
            }
            return doc;
            
        } catch (ParserConfigurationException e) {
            System.err.println("DOM: Export error occurred: " + e.getMessage() + ".");
        }
        return null;
    }
    
    private void export(Document doc, Element parent, CachedJClass cclass) {
        Element classElem = doc.createElement(BytecodeCache.ClassElem);
        classElem.setAttribute(BytecodeCache.NameAttr, cclass.getName());
        classElem.setAttribute(BytecodeCache.FqnAttr, cclass.getQualifiedName());
        parent.appendChild(classElem);
    }
    
    private void export(Document doc, Element parent, CachedJMethod cmethod) {
        Element methodElem = doc.createElement(BytecodeCache.MethodElem);
        methodElem.setAttribute(BytecodeCache.ClassNameAttr, cmethod.getClassName());
        methodElem.setAttribute(BytecodeCache.SignatureAttr, cmethod.getSignature());
        methodElem.setAttribute(BytecodeCache.FqnAttr, cmethod.getQualifiedName());
        parent.appendChild(methodElem);
    }
    
    private void export(Document doc, Element parent, CachedJField cfield) {
        Element fieldElem = doc.createElement(BytecodeCache.FieldElem);
        fieldElem.setAttribute(BytecodeCache.ClassNameAttr, cfield.getClassName());
        fieldElem.setAttribute(BytecodeCache.NameAttr, cfield.getName());
        fieldElem.setAttribute(BytecodeCache.FqnAttr, cfield.getQualifiedName());
        parent.appendChild(fieldElem);
    }
    
    @SuppressWarnings("unused")
    private String convert(String str) {
        if (str == null || str.length() == 0) {
            return str;
        }
        
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            
            if (ch == '&') {
                buf.append("&amp;");
                
            } else if (ch == '<') {
                buf.append("&lt;");
                
            } else if (ch == '>') {
                buf.append("&gt;");
                
            } else if (ch == '\'') {
                 buf.append("&apos;");
                 
            } else if (ch == '"') {
                buf.append("&quot;");
                
            } else {
                buf.append(ch);
            }
        }
        return buf.toString();
    }
}
    
class CacheImporter extends DefaultHandler {
    
    public CacheImporter() {
    }
    
    @Override
    public void startElement(String uri, String name, String qname, Attributes attrs) {
        if (qname.equals(BytecodeCache.ClassElem)) {
            Map<String, String> map = getAttributes(attrs);
            String fqn = map.get(BytecodeCache.FqnAttr);
            map.remove(BytecodeCache.FqnAttr);
            
            CachedJClass cclass = new CachedJClass(fqn, map);
            BytecodeCache.cachedClasses.put(fqn, cclass);
            return;
        }
        
        if (qname.equals(BytecodeCache.MethodElem)) {
            Map<String, String> map = getAttributes(attrs);
            String fqn = map.get(BytecodeCache.FqnAttr);
            map.remove(BytecodeCache.FqnAttr);
            
            CachedJMethod cmethod = new CachedJMethod(fqn, map);
            BytecodeCache.cachedMethods.put(fqn, cmethod);
            return;
        }
        
        if (qname.equals(BytecodeCache.FieldElem)) {
            Map<String, String> map = getAttributes(attrs);
            String fqn = map.get(BytecodeCache.FqnAttr);
            map.remove(BytecodeCache.FqnAttr);
            
            CachedJField cfield = new CachedJField(fqn, map);
            BytecodeCache.cachedFields.put(fqn, cfield);
            return;
        }
    }
    
    private Map<String, String> getAttributes(Attributes attrs) {
        Map<String, String> map = new HashMap<String, String>();
        for (int i = 0; i < attrs.getLength(); i++) {
            map.put(attrs.getQName(i), attrs.getValue(i));
        }
        return map;
    }
}
