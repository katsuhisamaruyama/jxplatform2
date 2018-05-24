/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.metrics;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import java.util.Map;

/**
 * Exports the metric values related to elements within a project.
 * @author Katsuhisa Maruyama
 */
public class DataExporter {
    
    public DataExporter() {
    }
    
    public Document getDocument(ProjectMetrics mproject) {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.newDocument();
            Element projectElem = doc.createElement(MetricsManager.ProjectElem);
            projectElem.setAttribute(MetricsManager.NameAttr, mproject.getName());
            projectElem.setAttribute(MetricsManager.PathAttr, mproject.getPath());
            projectElem.setAttribute(MetricsManager.TimeAttr, mproject.getTimeAsString());
            doc.appendChild(projectElem);
            
            for (PackageMetrics mpackage : mproject.getPackages()) {
                export(doc, projectElem, mpackage);
            }
            return doc;
            
        } catch (ParserConfigurationException e) {
            System.err.println("DOM: Export error occurred: " + e.getMessage() + ".");
        }
        return null;
    }
    
    private void export(Document doc, Element parent, PackageMetrics mpackage) {
        Element packageElem = doc.createElement(MetricsManager.PackageElem);
        packageElem.setAttribute(MetricsManager.NameAttr, mpackage.getName());
        parent.appendChild(packageElem);
        
        mpackage.sortNames(mpackage.getAfferentPackages());
        for (String name : mpackage.getAfferentPackages()) {
            Element afferentElem = doc.createElement(MetricsManager.AfferentElem);
            afferentElem.setAttribute(MetricsManager.FqnAttr, name);
            packageElem.appendChild(afferentElem);
        }
        mpackage.sortNames(mpackage.getEfferentPackages());
        for (String name : mpackage.getEfferentPackages()) {
            Element efferentElem = doc.createElement(MetricsManager.EfferentElem);
            efferentElem.setAttribute(MetricsManager.FqnAttr, name);
            packageElem.appendChild(efferentElem);
        }
        
        exportMetricAttributes(doc, packageElem, mpackage.getMetricValues());
        
        mpackage.sortClasses();
        for (ClassMetrics mclass : mpackage.getClasses()) {
            export(doc, packageElem, mclass);
        }
    }
    
    private void export(Document doc, Element parent, ClassMetrics mclass) {
        Element classElem = doc.createElement(MetricsManager.ClassElem);
        classElem.setAttribute(MetricsManager.FqnAttr, mclass.getQualifiedName());
        classElem.setAttribute(MetricsManager.NameAttr, mclass.getName());
        classElem.setAttribute(MetricsManager.ModifiersAttr, String.valueOf(mclass.getModifiers()));
        classElem.setAttribute(MetricsManager.PathAttr, mclass.getPath());
        classElem.setAttribute(MetricsManager.KindAttr, mclass.getKind().toString());
        parent.appendChild(classElem);
        
        exportCodeAttributes(doc, classElem, mclass);
        
        Element superclassElem = doc.createElement(MetricsManager.SuperClassElem);
        superclassElem.setAttribute(MetricsManager.FqnAttr, mclass.getSuperClassName());
        classElem.appendChild(superclassElem);
        
        mclass.sortNames(mclass.getSuperInterfaceNames());
        for (String name : mclass.getSuperInterfaceNames()) {
            Element superinterfaceElem = doc.createElement(MetricsManager.SuperInterfaceElem);
            superinterfaceElem.setAttribute(MetricsManager.FqnAttr, name);
            classElem.appendChild(superinterfaceElem);
        }
        
        mclass.sortNames(mclass.getAfferentClasses());
        for (String name : mclass.getAfferentClasses()) {
            Element afferentElem = doc.createElement(MetricsManager.AfferentElem);
            afferentElem.setAttribute(MetricsManager.FqnAttr, name);
            classElem.appendChild(afferentElem);
        }
        mclass.sortNames(mclass.getEfferentClasses());
        for (String name : mclass.getEfferentClasses()) {
            Element efferentElem = doc.createElement(MetricsManager.EfferentElem);
            efferentElem.setAttribute(MetricsManager.FqnAttr, name);
            classElem.appendChild(efferentElem);
        }
        
        exportMetricAttributes(doc, classElem, mclass.getMetricValues());
        
        mclass.sortMethods();
        for (MethodMetrics mmethod : mclass.getMethods()) {
            export(doc, classElem, mmethod);
        }
        mclass.sortFields();
        for (FieldMetrics mfield : mclass.getFields()) {
            export(doc, classElem, mfield);
        }
    }
    
    private void export(Document doc, Element parent, MethodMetrics mmethod) {
        Element methodElem = doc.createElement(MetricsManager.MethodElem);
        methodElem.setAttribute(MetricsManager.FqnAttr, mmethod.getQualifiedName());
        methodElem.setAttribute(MetricsManager.NameAttr, mmethod.getName());
        methodElem.setAttribute(MetricsManager.TypeAttr, mmethod.getReturnType());
        methodElem.setAttribute(MetricsManager.ModifiersAttr, String.valueOf(mmethod.getModifiers()));
        methodElem.setAttribute(MetricsManager.KindAttr, mmethod.getKind().toString());
        parent.appendChild(methodElem);
        
        exportCodeAttributes(doc, methodElem, mmethod);
        exportMetricAttributes(doc, methodElem, mmethod.getMetricValues());
    }
    
    private void export(Document doc, Element parent, FieldMetrics mfield) {
        Element fieldElem = doc.createElement(MetricsManager.FieldElem);
        fieldElem.setAttribute(MetricsManager.FqnAttr, mfield.getQualifiedName());
        fieldElem.setAttribute(MetricsManager.NameAttr, mfield.getName());
        fieldElem.setAttribute(MetricsManager.TypeAttr, mfield.getType());
        fieldElem.setAttribute(MetricsManager.ModifiersAttr, String.valueOf(mfield.getModifiers()));
        fieldElem.setAttribute(MetricsManager.KindAttr, mfield.getKind().toString());
        parent.appendChild(fieldElem);
        
        exportCodeAttributes(doc, fieldElem, mfield);
        exportMetricAttributes(doc, fieldElem, mfield.getMetricValues());
    }
    
    private void exportCodeAttributes(Document doc, Element parent, CommonMetrics codeInfo) {
        Element codeElem = doc.createElement(MetricsManager.CodeElem);
        codeElem.setAttribute(MetricsManager.StartPositionAttr, String.valueOf(codeInfo.getStartPosition()));
        codeElem.setAttribute(MetricsManager.EndPositionAttr, String.valueOf(codeInfo.getEndPosition()));
        codeElem.setAttribute(MetricsManager.UpperLineNumberAttr, String.valueOf(codeInfo.getUpperLineNumber()));
        codeElem.setAttribute(MetricsManager.BottomLineNumberAttr, String.valueOf(codeInfo.getBottomLineNumber()));
        parent.appendChild(codeElem);
    }
    
    private void exportMetricAttributes(Document doc, Element parent, Map<String, Double> metrics) {
        Element metricsElem = doc.createElement(MetricsManager.MetricsElem);
        for (String sort : metrics.keySet()) {
            double value = metrics.get(sort).doubleValue();
            metricsElem.setAttribute(sort, String.valueOf(value));
        }
        parent.appendChild(metricsElem);
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
    
    @SuppressWarnings("unused")
    private String getBoolean(boolean bool) {
        if (bool) {
            return MetricsManager.Yes;
        }
        return MetricsManager.No;
    }
}
