/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.metrics.viewer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.TableColumn;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Collections;
import java.util.Comparator;

/**
 * Displays metric values of a specific metric measurement.
 * @author Katsuhisa Maruyama
 */
public class MetricsTable extends Composite {
    
    private Table table;
    
    public MetricsTable(MetricsViewer viewer) {
        super(viewer.getShell(), SWT.BORDER);
        
        Composite composite = createTable(this);
        
        setLayout(new FormLayout());
        FormData data = new FormData();
        data.top = new FormAttachment(0, 0);
        data.bottom = new FormAttachment(100, 0);
        data.left = new FormAttachment(0, 0);
        data.right = new FormAttachment(100, 0);
        composite.setLayoutData(data);
    }
    
    private Composite createTable(Composite parent) {
        table = new Table(parent, SWT.BORDER | SWT.SINGLE | SWT.VIRTUAL | SWT.H_SCROLL | SWT.V_SCROLL);
        table.setLinesVisible(true);
        table.setHeaderVisible(true);
        
        TableColumn idColumn = new TableColumn(table, SWT.LEFT);
        idColumn.setText("class name");
        idColumn.setWidth(250);
        idColumn.setResizable(true);
        return table;
    }
    
    public void dispose() {
        table.dispose();
    }
    
    public void showTitles(String[] titles) {
        for (int i = 0; i < titles.length; i++) {
            TableColumn nameColumn = new TableColumn(table, SWT.RIGHT);
            nameColumn.setText(titles[i]);
            nameColumn.setWidth(70);
            nameColumn.setResizable(true);
        }
    }
    
    public void showData(Map<String, double[]> data) {
        table.removeAll();
        
        List<OutputData> outputData = new ArrayList<OutputData>();
        for (String fqn : data.keySet()) {
            outputData.add(new OutputData(fqn, data.get(fqn)));
        }
        sort(outputData);
        
        for (OutputData odata :outputData) {
            TableItem item = new TableItem(table, SWT.NONE);
            item.setText(0, odata.fqn);
            for (int i = 0; i < odata.values.length; i++) {
                item.setText(i + 1, String.valueOf(odata.values[i]));
            }
        }
        table.update();
    }
    
    class OutputData {
        String fqn;
        double[] values;
        
        OutputData(String fqn, double[] values) {
            this.fqn = fqn;
            this.values = values;
        }
    }
    
    public void sort(List<OutputData> data) {
        Collections.sort(data, new Comparator<OutputData>() {
            public int compare(OutputData data1, OutputData data2) {
                return data1.fqn.compareTo(data2.fqn);
            }
        });
    }
}
