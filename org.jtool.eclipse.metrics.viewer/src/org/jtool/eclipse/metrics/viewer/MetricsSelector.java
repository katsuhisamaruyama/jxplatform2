/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.metrics.viewer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Combo;
import java.util.Map;


/**
 * Selects a metric measurement of interest.
 * @author Katsuhisa Maruyama
 */
public class MetricsSelector extends Composite {
    
    private MetricsViewer viewer;
    private String target;
    
    private MetricDataExtractor extractor = new MetricDataExtractor();
    
    public MetricsSelector(MetricsViewer viewer, String target) {
        super(viewer.getShell(), SWT.BORDER);
        this.viewer = viewer;
        this.target = target;
        
        Composite composite = createRepositoryControl(this);
        
        setLayout(new FormLayout());
        FormData data = new FormData();
        data.top = new FormAttachment(0, 0);
        data.left = new FormAttachment(0, 0);
        data.right = new FormAttachment(100, 0);
        data.bottom = new FormAttachment(100, 0);
        composite.setLayoutData(data);
    }
    
    protected Composite createRepositoryControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        
        GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        composite.setLayout(layout);
        
        Combo metricSel = new Combo(composite, SWT.FLAT);
        metricSel.setItems(extractor.getMetricMenuItems());
        metricSel.select(0);
        metricSel.setEnabled(false);
        
        metricSel.addSelectionListener(new SelectionListener() {
            
            public void widgetDefaultSelected(SelectionEvent evt) {
            }
            
            public void widgetSelected(SelectionEvent evt) {
                Combo combo = (Combo)evt.getSource();
                String name = combo.getItem(combo.getSelectionIndex());
                Map<String, double[]> data = extractor.extract(name);
                viewer.showData(data);
            }
        });
        
        Button openButton = new Button(composite, SWT.FLAT);
        openButton.setText("Open");
        openButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
        
        Label locLabel = new Label(composite, SWT.NONE);
        locLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL));
        if (target != null && target.length() > 0) {
            locLabel.setText(target);
            String[] titles = extractor.collectProjectMetrics(target);
            viewer.showTitles(titles);
            metricSel.select(0);
            metricSel.setEnabled(true);
        } else {
            locLabel.setText("Not specified");
        }
        
        openButton.addSelectionListener(new SelectionListener() {
            
            public void widgetDefaultSelected(SelectionEvent evt) {
            }
            
            public void widgetSelected(SelectionEvent evt) {
                DirectoryDialog dialog = new DirectoryDialog(parent.getShell());
                dialog.setFilterPath(target);
                String target = dialog.open();
                if (target != null) {
                    locLabel.setText(target);
                    String[] titles = extractor.collectProjectMetrics(target);
                    viewer.showTitles(titles);
                    metricSel.select(0);
                    metricSel.setEnabled(true);
                }
            }
        });
        
        return composite;
    }
}
