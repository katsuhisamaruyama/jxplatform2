/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.metrics.viewer;

import org.jtool.eclipse.metrics.ProjectMetrics;
import org.jtool.eclipse.metrics.UnsupportedMetricsException;
import org.jtool.eclipse.metrics.ClassMetrics;
import org.jtool.eclipse.metrics.MetricsManager;
import org.jtool.eclipse.metrics.measurement.CBO;
import org.jtool.eclipse.metrics.measurement.DIT;
import org.jtool.eclipse.metrics.measurement.IMetric;
import org.jtool.eclipse.metrics.measurement.LCOM;
import org.jtool.eclipse.metrics.measurement.LOC;
import org.jtool.eclipse.metrics.measurement.NOACL;
import org.jtool.eclipse.metrics.measurement.NOAFD;
import org.jtool.eclipse.metrics.measurement.NOAMD;
import org.jtool.eclipse.metrics.measurement.NOC;
import org.jtool.eclipse.metrics.measurement.NOECL;
import org.jtool.eclipse.metrics.measurement.NOEMD;
import org.jtool.eclipse.metrics.measurement.NOFD;
import org.jtool.eclipse.metrics.measurement.NOMD;
import org.jtool.eclipse.metrics.measurement.NOMF;
import org.jtool.eclipse.metrics.measurement.NOPF;
import org.jtool.eclipse.metrics.measurement.NOPM;
import org.jtool.eclipse.metrics.measurement.NOST;
import org.jtool.eclipse.metrics.measurement.RFC;
import org.jtool.eclipse.metrics.measurement.WMC;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.Comparator;
import java.io.File;

/**
 * Extracts metric data corresponding to a specific metric measurement.
 * @author Katsuhisa Maruyama
 */
public class MetricDataExtractor {
    
    private List<ProjectMetrics> projects = new ArrayList<ProjectMetrics>();
    
    private static final IMetric[] SELECTABLE_METRICS = {
            new LOC(), new NOST(),
            new NOMD(), new NOFD(), new NOMF(), new NOPM(), new NOPF(),
            new NOACL(), new NOECL(), new NOAMD(), new NOEMD(), new NOAFD(),
            new CBO(), new DIT(), new NOC(), new RFC(), new WMC(), new LCOM()
        };
    
    public MetricDataExtractor() {
    }
    
    public IMetric getMetric(String name) {
        for (int i = 0; i < SELECTABLE_METRICS.length; i++) {
            if (SELECTABLE_METRICS[i].getName().equals(name)) {
                return SELECTABLE_METRICS[i];
            }
        }
        return null;
    }
    
    public String[] getMetricMenuItems() {
        String[] names = new String[SELECTABLE_METRICS.length + 1];
        names[0] = " - ";
        for (int i = 0; i < SELECTABLE_METRICS.length; i++) {
            names[i + 1] = SELECTABLE_METRICS[i].getName();
        }
        return names;
    }
    
    public String[] collectProjectMetrics(String target) {
        MetricsManager metricsManager = new MetricsManager();
        File dir = new File(target);
        for (File file : dir.listFiles()) {
            ProjectMetrics mproject = metricsManager.importXML(file);
            if (mproject != null) {
                projects.add(mproject);
                
            }
        }
        
        sort(projects);
        String[] titles = new String[projects.size()];
        for (int i = 0; i < projects.size(); i++) {
            titles[i] = String.valueOf(projects.get(i).getFormatedDate());
        }
        return titles;
    }
    
    public Map<String, double[]> extract(String name) {
        IMetric metric = getMetric(name);
        if (metric != null) {
            return extract(metric);
        }
        return new HashMap<String, double[]>();
    }
    
    public Map<String, double[]> extract(IMetric metric) {
        Map<String, double[]> data = new HashMap<String, double[]>();
        for (int i = 0; i < projects.size(); i++) {
            ProjectMetrics mproject = projects.get(i);
            for (ClassMetrics mclass : mproject.getClasses()) {
                try {
                    double value = metric.valueOf(mclass);
                    String fqn = mclass.getQualifiedName();
                    double[] values = data.get(fqn);
                    if (values == null) {
                        values = new double[projects.size()];
                        data.put(fqn, values);
                    }
                    values[i] = value;
                } catch (UnsupportedMetricsException e) { /* empty */ }
            }
        }
        return data;
    }
    
    public void sort(List<ProjectMetrics> projects) {
        Collections.sort(projects, new Comparator<ProjectMetrics>() {
            public int compare(ProjectMetrics project1, ProjectMetrics project2) {
                long time1 = project1.getTimeAsLong();
                long time2 = project2.getTimeAsLong();
                if (time1 > time2) {
                    return 1;
                } else if (time1 < time2) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });
    }
    
    public void print(Map<String, double[]> data) {
        for (String fqn : data.keySet()) {
            double[] values = data.get(fqn);
            System.out.print(fqn + "  ");
            for (int i = 0; i < values.length; i++) {
                System.out.print(" " + values[i]);
            }
            System.out.println();
        }
    }
    
    public static void main(String[] args) {
        String target = "/Users/maru/Desktop/TestSamples/data/";
        MetricDataExtractor extractor = new MetricDataExtractor();
        extractor.collectProjectMetrics(target);
        extractor.extract("LOC");
    }
}
