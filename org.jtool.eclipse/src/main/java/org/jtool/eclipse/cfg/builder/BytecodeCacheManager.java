/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg.builder;

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
import java.util.Map.Entry;
import java.util.List;
import java.util.ArrayList;
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
class BytecodeCacheManager {
    
    private static final String BYTECODE_INFO_FILENAME = ".bytecode.info";
    
    static final String ProjectElem = "project";
    static final String ClassElem = "class";
    static final String MethodElem = "method";
    static final String FieldElem = "field";
    
    static final String PathAttr = "path";
    static final String TimeAttr = "time";
    
    static void writeCache(JavaProject jproject, List<JClass> classes) {
        try {
            String filename = jproject.getDir() + File.separator + BYTECODE_INFO_FILENAME;
            File file = new File(filename);
            
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
            if (file.exists()) {
                file.delete();
            }
            
            BufferedWriter bwriter = new BufferedWriter(new FileWriter(file));
            bwriter.write(writer.toString());
            bwriter.flush();
            bwriter.close();
        } catch (TransformerException e) {
            System.err.println("DOM: Export error occurred: " + e.getMessage() + ".");
        }
    }
    
    static boolean loadCache(JavaProject jproject, CFGStore cfgStore) {
        String filename = jproject.getPath() + File.separator + BYTECODE_INFO_FILENAME;
        File file = new File(filename);
        if (!file.canRead()) {
            return false;
        }
        
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            
            SAXParser parser = factory.newSAXParser();
            CacheImporter handler = new CacheImporter(cfgStore);
            parser.parse(file, handler);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            return false;
        }
        return true;
    }
}

class CacheExporter {
    
    CacheExporter() {
    }
    
    Document getDocument(JavaProject jproject, List<JClass> classes) {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.newDocument();
            Element projectElem = doc.createElement(BytecodeCacheManager.ProjectElem);
            projectElem.setAttribute(BytecodeCacheManager.PathAttr, jproject.getPath());
            projectElem.setAttribute(BytecodeCacheManager.TimeAttr, TimeInfo.getTimeAsISOString(TimeInfo.getCurrentTime()));
            doc.appendChild(projectElem);
            
            for (JClass clazz : classes) {
                if (clazz.isTopLevelClass()) {
                    export(doc, projectElem, clazz);
                }
            }
            return doc;
            
        } catch (ParserConfigurationException e) {
            System.err.println("DOM: Export error occurred: " + e.getMessage() + ".");
        }
        return null;
    }
    
    private void export(Document doc, Element parent, JClass clazz) {
        Element classElem = doc.createElement(BytecodeCacheManager.ClassElem);
        for (Entry<String, String> entry : clazz.getCacheData().entrySet()) {
            classElem.setAttribute(entry.getKey(), entry.getValue());
        }
        
        for (JMethod method : clazz.getMethods()) {
            export(doc, classElem, method);
        }
        for (JField field : clazz.getFields()) {
            export(doc, classElem, field);
        }
        parent.appendChild(classElem);
    }
    
    private void export(Document doc, Element parent, JMethod method) {
        Element methodElem = doc.createElement(BytecodeCacheManager.MethodElem);
        for (Entry<String, String> entry : method.getCacheData().entrySet()) {
            methodElem.setAttribute(entry.getKey(), entry.getValue());
        }
        parent.appendChild(methodElem);
    }
    
    private void export(Document doc, Element parent, JField field) {
        Element fieldElem = doc.createElement(BytecodeCacheManager.FieldElem);
        for (Entry<String, String> entry : field.getCacheData().entrySet()) {
            fieldElem.setAttribute(entry.getKey(), entry.getValue());
        }
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
    
    private CFGStore cfgStore;
    
    private JClassCache clazz = null;
    private List<JMethodCache> cmethods = new ArrayList<JMethodCache>();
    private List<JFieldCache> cfields = new ArrayList<JFieldCache>();
    
    public CacheImporter(CFGStore cfgStore) {
        this.cfgStore = cfgStore;
    }
    
    @Override
    public void startElement(String uri, String name, String qname, Attributes attrs) {
        if (qname.equals(BytecodeCacheManager.ClassElem)) {
            clazz = new JClassCache(cfgStore, attributes(attrs));
            return;
        }
        
        if (qname.equals(BytecodeCacheManager.MethodElem)) {
            if (clazz != null) {
                JMethodCache method = new JMethodCache(clazz, cfgStore, attributes(attrs));
                cmethods.add(method);
            }
            return;
        }
        
        if (qname.equals(BytecodeCacheManager.FieldElem)) {
            if (clazz != null) {
                JFieldCache field = new JFieldCache(clazz, cfgStore, attributes(attrs));
                cfields.add(field);
            }
            return;
        }
    }
    
    @Override
    public void endElement(String uri, String name, String qname) throws SAXException {
        if (qname.equals(BytecodeCacheManager.ClassElem)) {
            clazz.setMethods(cmethods);
            clazz.setFields(cfields);
            cfgStore.getJInfoStore().registerJClassCache(clazz);
            
            clazz = null;
            cmethods.clear();
            cfields.clear();
        }
    }
    
    private Map<String, String> attributes(Attributes attrs) {
        Map<String, String> map = new HashMap<String, String>();
        for (int i = 0; i < attrs.getLength(); i++) {
            map.put(attrs.getQName(i), attrs.getValue(i));
        }
        return map;
    }
}
