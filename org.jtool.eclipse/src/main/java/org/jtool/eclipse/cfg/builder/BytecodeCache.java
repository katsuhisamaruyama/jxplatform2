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
    
    Map<String, CachedJClass> cachedClasses = new HashMap<String, CachedJClass>();
    Map<String, CachedJMethod> cachedMethods = new HashMap<String, CachedJMethod>();
    Map<String, CachedJField> cachedFields = new HashMap<String, CachedJField>();
    
    private JavaProject jproject;
    
    BytecodeCache(JavaProject jproject) {
        this.jproject = jproject;
    }
    
    public CachedJClass getCachedJClass(String fqn) {
        return cachedClasses.get(fqn);
    }
    
    public CachedJMethod getCachedJMethod(String fqn) {
        return cachedMethods.get(fqn);
    }
    
    public CachedJField getCachedJField(String fqn) {
        return cachedFields.get(fqn);
    }
    
    public void writeCache(List<ExternalJClass> classes) {
        try {
            String filename = jproject.getPath() + File.separator + BYTECODE_INFO_FILENAME;
            File file = new File(filename);
            if (file.exists()) {
                file.delete();
            }
            
            CacheExporter exporter = new CacheExporter(this);
            Document doc = exporter.getDocument(jproject, classes);
            write(file, doc);
        } catch (IOException e) {
            System.err.println("IO error " + e.getMessage());
        }
    }
    
    private void write(File file, Document doc) throws IOException {
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
    }
    
    public boolean loadCache() {
        String filename = jproject.getPath() + File.separator + BYTECODE_INFO_FILENAME;
        File file = new File(filename);
        if (!file.canRead()) {
            return false;
        }
        
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            CacheImporter handler = new CacheImporter(this);
            parser.parse(file, handler);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            return false;
        }
        return true;
    }
    
    static final String ProjectElem = "project";
    static final String ClassElem = "class";
    static final String MethodElem = "method";
    static final String FieldElem = "field";
    
    static final String ClassNameAttr = "cname";
    static final String FqnAttr = "fqn";
    static final String PathAttr = "path";
    static final String TimeAttr = "time";
    static final String SignatureAttr = "sig";
    static final String NameAttr = "name";
    
    static final String SideEffectsAttr = "sideEffects";
}

class CacheExporter {
    
    private BytecodeCache bytecodeCache;
    
    CacheExporter(BytecodeCache bytecodeCache) {
        this.bytecodeCache = bytecodeCache;
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
                    bytecodeCache.cachedClasses.put(clazz.getQualifiedName(), new CachedJClass(clazz));
                    for (JMethod method : clazz.getMethods()) {
                        bytecodeCache.cachedMethods.put(method.getQualifiedName(), new CachedJMethod(method));
                    }
                    for (JField field : clazz.getFields()) {
                        bytecodeCache.cachedFields.put(field.getQualifiedName(), new CachedJField(field));
                    }
                }
            }
            
            for (CachedJClass cclass : bytecodeCache.cachedClasses.values()) {
                export(doc, projectElem, cclass);
            }
            for (CachedJMethod cmethod : bytecodeCache.cachedMethods.values()) {
                export(doc, projectElem, cmethod);
            }
            for (CachedJField cfield : bytecodeCache.cachedFields.values()) {
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
        methodElem.setAttribute(BytecodeCache.SideEffectsAttr, cmethod.sideEffects());
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
    
    private BytecodeCache bytecodeCache;
    
    public CacheImporter(BytecodeCache bytecodeCache) {
        this.bytecodeCache = bytecodeCache;
    }
    
    @Override
    public void startElement(String uri, String name, String qname, Attributes attrs) {
        if (qname.equals(BytecodeCache.ClassElem)) {
            Map<String, String> map = getAttributes(attrs);
            String fqn = map.get(BytecodeCache.FqnAttr);
            map.remove(BytecodeCache.FqnAttr);
            
            CachedJClass cclass = new CachedJClass(fqn, map);
            bytecodeCache.cachedClasses.put(fqn, cclass);
            return;
        }
        
        if (qname.equals(BytecodeCache.MethodElem)) {
            Map<String, String> map = getAttributes(attrs);
            String fqn = map.get(BytecodeCache.FqnAttr);
            map.remove(BytecodeCache.FqnAttr);
            
            CachedJMethod cmethod = new CachedJMethod(fqn, map);
            bytecodeCache.cachedMethods.put(fqn, cmethod);
            return;
        }
        
        if (qname.equals(BytecodeCache.FieldElem)) {
            Map<String, String> map = getAttributes(attrs);
            String fqn = map.get(BytecodeCache.FqnAttr);
            map.remove(BytecodeCache.FqnAttr);
            
            CachedJField cfield = new CachedJField(fqn, map);
            bytecodeCache.cachedFields.put(fqn, cfield);
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
