/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.metrics;

import org.jtool.eclipse.util.TimeInfo;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import java.time.ZonedDateTime;

/**
 * Imports the metric values related to element within a project from an XML file.
 * @author Katsuhisa Maruyama
 */
public class DataImporter extends DefaultHandler {
    
    private ProjectMetrics projectMetrics;
    private PackageMetrics packageMetrics;
    private ClassMetrics classMetrics;
    private MethodMetrics methodMetrics;
    private FieldMetrics fieldMetrics;
    private Metrics parent;
    
    public DataImporter() {
    }
    
    public ProjectMetrics getProjectMetrics() {
        return projectMetrics;
    }
    
    @Override
    public void startDocument() throws SAXException {
    }
    
    @Override
    public void endDocument() throws SAXException {
        projectMetrics.collectMetricsAfterXMLImport();
    }
    
    @Override
    public void startElement(String uri, String name, String qname, Attributes attrs) {
        if (qname.equals(MetricsManager.ProjectElem)) {
            setProjectAttributes(attrs);
            parent = projectMetrics;
            return;
        }
        
        if (qname.equals(MetricsManager.PackageElem)) {
            setPackageAttributes(attrs);
            parent = packageMetrics;
            return;
        }
        
        if (qname.equals(MetricsManager.ClassElem)) {
            setClassAttributes(attrs);
            parent = classMetrics;
            return;
        }
        
        if (qname.equals(MetricsManager.MethodElem)) {
            setMethodAttributes(attrs);
            parent = methodMetrics;
            return;
        }
        
        if (qname.equals(MetricsManager.FieldElem)) {
            setFieldAttributes(attrs);
            parent = fieldMetrics;
            return;
        }
        
        if (qname.equals(MetricsManager.SuperClassElem)) {
            if (parent == classMetrics) {
                if (attrs.getQName(0).equals(MetricsManager.FqnAttr)) {
                    classMetrics.setSuperClass(attrs.getValue(0));
                }
            }
            return;
        }
        
        if (qname.equals(MetricsManager.SuperInterfaceElem)) {
            if (parent == classMetrics) {
                if (attrs.getQName(0).equals(MetricsManager.FqnAttr)) {
                    classMetrics.addSuperInterface(attrs.getValue(0));
                }
            }
            return;
        }
        
        if (qname.equals(MetricsManager.AfferentElem)) {
            if (parent == packageMetrics) {
                if (attrs.getQName(0).equals(MetricsManager.FqnAttr)) {
                    packageMetrics.addAfferentPackage(attrs.getValue(0));
                }
            } else if (parent == classMetrics) {
                if (attrs.getQName(0).equals(MetricsManager.FqnAttr)) {
                    classMetrics.addAfferentClass(attrs.getValue(0));
                }
            }
            return;
        }
        
        if (qname.equals(MetricsManager.EfferentElem)) {
            if (parent == packageMetrics) {
                if (attrs.getQName(0).equals(MetricsManager.FqnAttr)) {
                    packageMetrics.addEfferentPackage(attrs.getValue(0));
                }
            } else if (parent == classMetrics) {
                if (attrs.getQName(0).equals(MetricsManager.FqnAttr)) {
                    classMetrics.addEfferentClass(attrs.getValue(0));
                }
            }
            return;
        }
        
        if (qname.equals(MetricsManager.MetricsElem)) {
            setMetricAttributes(attrs);
            return;
        }
        
        if (qname.equals(MetricsManager.CodeElem)) {
            setCodeAttributes(attrs);
            return;
        }
    }
    
    @Override
    public void endElement(String uri, String name, String qname) {
        if (qname.equals(MetricsManager.ProjectElem)) {
            parent = null;
            return;
        }
        
        if (qname.equals(MetricsManager.PackageElem)) {
            parent = projectMetrics;
            return;
        }
        
        if (qname.equals(MetricsManager.ClassElem)) {
            parent = packageMetrics;
            return;
        }
        
        if (qname.equals(MetricsManager.MethodElem)) {
            parent = classMetrics;
            return;
        }
        
        if (qname.equals(MetricsManager.FieldElem)) {
            parent = classMetrics;
            return;
        }
    }
    
    private void setProjectAttributes(Attributes attrs) {
        String name = null;
        String path = null;
        ZonedDateTime time = null;
        for (int i = 0; i < attrs.getLength(); i++) {
            if (attrs.getQName(i).equals(MetricsManager.NameAttr)) {
                name = attrs.getValue(i);
            } else if (attrs.getQName(i).equals(MetricsManager.PathAttr)) {
                path = attrs.getValue(i);
            } else if (attrs.getQName(i).equals(MetricsManager.TimeAttr)) {
                time = TimeInfo.getTime(attrs.getValue(i));
            }
        }
        if (name != null && path != null && time != null) {
            projectMetrics = new ProjectMetrics(name, path, time);
        }
    }
    
    private void setPackageAttributes(Attributes attrs) {
        String name = null;
        for (int i = 0; i < attrs.getLength(); i++) {
            if (attrs.getQName(i).equals(MetricsManager.NameAttr)) {
                name = attrs.getValue(i);
            }
        }
        if (name != null) {
            packageMetrics = new PackageMetrics(name, projectMetrics);
            projectMetrics.addPackage(packageMetrics);
        }
    }
    
    private void setClassAttributes(Attributes attrs) {
        String fqn = null;
        String name = null;
        int modifiers = 0;
        String path = null;
        String kindStr = null;
        for (int i = 0; i < attrs.getLength(); i++) {
            if (attrs.getQName(i).equals(MetricsManager.FqnAttr)) {
                fqn = attrs.getValue(i);
            } else if (attrs.getQName(i).equals(MetricsManager.NameAttr)) {
                name = attrs.getValue(i);
            } else if (attrs.getQName(i).equals(MetricsManager.ModifiersAttr)) {
                modifiers = getInteger(attrs.getValue(i));
            } else if (attrs.getQName(i).equals(MetricsManager.PathAttr)) {
                path = attrs.getValue(i);
            } else if (attrs.getQName(i).equals(MetricsManager.KindAttr)) {
                kindStr = attrs.getValue(i);
            }
        }
        
        if (fqn != null) {
            classMetrics = new ClassMetrics(fqn, name, modifiers, kindStr, path, packageMetrics);
            packageMetrics.addClass(classMetrics);
        }
    }
    
    private void setMethodAttributes(Attributes attrs) {
        String fqn = null;
        String name = null;
        String type = null;
        int modifiers = 0;
        String kindStr = null;
        
        for (int i = 0; i < attrs.getLength(); i++) {
            if (attrs.getQName(i).equals(MetricsManager.FqnAttr)) {
                fqn = attrs.getValue(i);
            } else if (attrs.getQName(i).equals(MetricsManager.NameAttr)) {
                name = attrs.getValue(i);
            }  else if (attrs.getQName(i).equals(MetricsManager.TypeAttr)) {
                type = attrs.getValue(i);
            } else if (attrs.getQName(i).equals(MetricsManager.ModifiersAttr)) {
                modifiers = getInteger(attrs.getValue(i));
            } else if (attrs.getQName(i).equals(MetricsManager.KindAttr)) {
                kindStr = attrs.getValue(i);
            }
        }
        
        if (fqn != null) {
            methodMetrics = new MethodMetrics(fqn, name, type, modifiers, kindStr, classMetrics);
            classMetrics.addMethod(methodMetrics);
        }
    }
    
    private void setFieldAttributes(Attributes attrs) {
        String fqn = null;
        String name = null;
        String type = null;
        int modifiers = 0;
        String kindStr = null;
        
        for (int i = 0; i < attrs.getLength(); i++) {
            if (attrs.getQName(i).equals(MetricsManager.FqnAttr)) {
                fqn = attrs.getValue(i);
            } else if (attrs.getQName(i).equals(MetricsManager.NameAttr)) {
                name = attrs.getValue(i);
            }  else if (attrs.getQName(i).equals(MetricsManager.TypeAttr)) {
                type = attrs.getValue(i);
            } else if (attrs.getQName(i).equals(MetricsManager.ModifiersAttr)) {
                modifiers = getInteger(attrs.getValue(i));
            } else if (attrs.getQName(i).equals(MetricsManager.KindAttr)) {
                kindStr = attrs.getValue(i);
            }
        }
        
        if (fqn != null) {
            fieldMetrics = new FieldMetrics(fqn, name, type, modifiers, kindStr, classMetrics);
            classMetrics.addField(fieldMetrics);
        }
    }
    
    private void setMetricAttributes(Attributes attrs) {
        if (parent == null) {
            return;
        }
        for (int i = 0; i < attrs.getLength(); i++) {
            parent.putMetricValue(attrs.getQName(i), Double.parseDouble(attrs.getValue(i)));
        }
    }
    
    private void setCodeAttributes(Attributes attrs) {
        if (parent == null) {
            return;
        }
        
        int start = -1;
        int end = 0;
        int upper = -1;
        int bottom = 0;
        
        for (int i = 0; i < attrs.getLength(); i++) {
            if (attrs.getQName(i).equals(MetricsManager.StartPositionAttr)) {
                start = getInteger(attrs.getValue(i));
            } else if (attrs.getQName(i).equals(MetricsManager.EndPositionAttr)) {
                end = getInteger(attrs.getValue(i));
            } else if (attrs.getQName(i).equals(MetricsManager.UpperLineNumberAttr)) {
                upper = getInteger(attrs.getValue(i));
            } else if (attrs.getQName(i).equals(MetricsManager.BottomLineNumberAttr)) {
                bottom = getInteger(attrs.getValue(i));
            }
        }
        
        if (start >= 0 && upper >= 0) {
            parent.setCodeProperties(start, end, upper, bottom);
        }
    }
    
    @SuppressWarnings("unused")
    private boolean getBoolean(String value) {
        return value.equals(MetricsManager.Yes);
    }
    
    private int getInteger(String value) {
        return Integer.parseInt(value);
    }
    
    @SuppressWarnings("unused")
    private long getLong(String value) {
        return Long.parseLong(value);
    }
}
