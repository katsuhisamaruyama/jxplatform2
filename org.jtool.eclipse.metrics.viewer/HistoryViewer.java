/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changemorphing.replayer;

import org.jtool.changetracker.operation.CommandOperation;
import org.jtool.changetracker.operation.CopyOperation;
import org.jtool.changetracker.operation.DocumentOperation;
import org.jtool.changetracker.operation.FileOperation;
import org.jtool.changetracker.operation.IChangeOperation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import java.util.List;

public class HistoryViewer extends Composite {
    
    protected ReplayerBase base;
    
    protected Table operationTable;
    
    protected Button goPrecButton;
    protected Button goSuccButton;
    protected Button goPrevButton;
    protected Button goNextButton;
    protected Button goFirstButton;
    protected Button goLastButton;
    
    public HistoryViewer(Composite parent, int style) {
        super(parent, style);
    }
    
    public HistoryViewer(ReplayerBase base) {
        super(base.getShell(), SWT.BORDER);
        this.base = base;
        
        setLayout(new FormLayout());
        Composite buttonControl = createButtonControl(this);
        Composite tableControl = createTableControl(this);
        
        FormData data = new FormData();
        data.top = new FormAttachment(0, 0);
        data.bottom = new FormAttachment(buttonControl, -2);
        data.left = new FormAttachment(0, 0);
        data.right = new FormAttachment(100, 0);
        tableControl.setLayoutData(data);
    }
    
    protected Composite createButtonControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        
        goFirstButton = new Button(composite, SWT.FLAT);
        goFirstButton.setToolTipText("Go to the first change operation");
        goFirstButton.setText("|<");
        goFirstButton.setEnabled(true);
        goFirstButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
        
        goFirstButton.addSelectionListener(new SelectionListener() {
            
            public void widgetDefaultSelected(SelectionEvent evt) {
            }
            
            public void widgetSelected(SelectionEvent evt) {
                int index = base.getFirstOperationIndex();
                if (index != -1) {
                    base.setPresentIndex(index);
                }
            }
        });
        
        goPrevButton = new Button(composite, SWT.FLAT);
        goPrevButton.setToolTipText("Go to the previous file operation");
        goPrevButton.setText("<<");
        goPrevButton.setEnabled(true);
        goPrevButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
        
        goPrevButton.addSelectionListener(new SelectionListener() {
            
            public void widgetDefaultSelected(SelectionEvent evt) {
            }
            
            public void widgetSelected(SelectionEvent evt) {
                int index = base.getPrevousFileOperationIndex();
                if (index != -1) {
                    base.setPresentIndex(index);
                }
            }
        });
        
        goPrecButton = new Button(composite, SWT.FLAT);
        goPrecButton.setToolTipText("Go to the precedent change operation");
        goPrecButton.setText("<");
        goPrecButton.setEnabled(true);
        goPrecButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
        
        goPrecButton.addSelectionListener(new SelectionListener() {
            
            public void widgetDefaultSelected(SelectionEvent evt) {
            }
            
            public void widgetSelected(SelectionEvent evt) {
                int index = base.getPrecedentOperationIndex();
                if (index != -1) {
                    base.setPresentIndex(index);
                }
            }
        });
        
        goSuccButton = new Button(composite, SWT.FLAT);
        goSuccButton.setToolTipText("Go to the successive change operation");
        goSuccButton.setText(">");
        goSuccButton.setEnabled(true);
        goSuccButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
        
        goSuccButton.addSelectionListener(new SelectionListener() {
            
            public void widgetDefaultSelected(SelectionEvent evt) {
            }
            
            public void widgetSelected(SelectionEvent evt) {
                int index = base.getSuccessiveOperationIndex();
                if (index != -1) {
                    base.setPresentIndex(index);
                }
            }
        });
        
        goNextButton = new Button(composite, SWT.FLAT);
        goNextButton.setToolTipText("Go to the next file operation");
        goNextButton.setText(">>");
        goNextButton.setEnabled(true);
        goNextButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
        
        goNextButton.addSelectionListener(new SelectionListener() {
            
            public void widgetDefaultSelected(SelectionEvent evt) {
            }
            
            public void widgetSelected(SelectionEvent evt) {
                int index = base.getNextFileOperationIndex();
                if (index != -1) {
                    base.setPresentIndex(index);
                }
            }
        });
        
        goLastButton = new Button(composite, SWT.FLAT);
        goLastButton.setToolTipText("Go to the last change operation");
        goLastButton.setText(">|");
        goLastButton.setEnabled(true);
        goLastButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
        
        goLastButton.addSelectionListener(new SelectionListener() {
            
            public void widgetDefaultSelected(SelectionEvent evt) {
            }
            
            public void widgetSelected(SelectionEvent evt) {
                int index = base.getLastOperationIndex();
                if (index != -1) {
                    base.setPresentIndex(index);
                }
            }
        }); 
        
        goFirstButton.addSelectionListener(new SelectionListener() {
            
            public void widgetDefaultSelected(SelectionEvent evt) {
            }
            
            public void widgetSelected(SelectionEvent evt) {
                int index = base.getFirstOperationIndex();
                if (index != -1) {
                    base.setPresentIndex(index);
                }
            }
        });
        
        final int MARGIN = 2;
        GridLayout btlayout = new GridLayout();
        btlayout.numColumns = 6;
        btlayout.makeColumnsEqualWidth = true;
        btlayout.marginWidth = 0;
        btlayout.marginHeight = 0;
        btlayout.horizontalSpacing = MARGIN;
        btlayout.marginHeight = MARGIN;
        btlayout.marginWidth = MARGIN;
        btlayout.marginLeft = MARGIN;
        btlayout.marginRight = MARGIN;
        btlayout.marginTop = MARGIN;
        btlayout.marginBottom = MARGIN;
        composite.setLayout(btlayout);
        
        FormData btdata = new FormData();
        btdata.bottom = new FormAttachment(100, 0);
        btdata.left = new FormAttachment(0, 0);
        btdata.right = new FormAttachment(100, 0);
        composite.setLayoutData(btdata);
        
        resetButtons();
        
        return composite;
    }
    
    public void updateButtons() {
        int index = base.getFirstOperationIndex();
        if (base.getPresentIndex() != index) {
            goFirstButton.setEnabled(true);
        } else {
            goFirstButton.setEnabled(false);
        }
        
        index = base.getPrevousFileOperationIndex();
        if (index != -1) {
            goPrevButton.setEnabled(true);
        } else {
            goPrevButton.setEnabled(false);
        }
        
        index = base.getPrecedentOperationIndex();
        if (index != -1) {
            goPrecButton.setEnabled(true);
        } else {
            goPrecButton.setEnabled(false);
        }
        
        index = base.getSuccessiveOperationIndex();
        if (index != -1) {
            goSuccButton.setEnabled(true);
        } else {
            goSuccButton.setEnabled(false);
        }
        
        index = base.getNextFileOperationIndex();
        if (index != -1) {
            goNextButton.setEnabled(true);
        } else {
            goNextButton.setEnabled(false);
        }
        
        index = base.getLastOperationIndex();
        if (base.getPresentIndex() != index) {
            goLastButton.setEnabled(true);
        } else {
            goLastButton.setEnabled(false);
        }
    }
    
    public void resetButtons() {
        goFirstButton.setEnabled(false);
        goPrevButton.setEnabled(false);
        goPrecButton.setEnabled(false);
        goSuccButton.setEnabled(false);
        goNextButton.setEnabled(false);
        goLastButton.setEnabled(false);
    }
    
    protected Composite createTableControl(Composite parent) {
        operationTable = new Table(parent, SWT.BORDER | SWT.SINGLE | SWT.VIRTUAL | SWT.H_SCROLL | SWT.V_SCROLL);
        operationTable.setLinesVisible(true);
        operationTable.setHeaderVisible(true);
        
        TableColumn idColumn = new TableColumn(operationTable, SWT.LEFT);
        idColumn.setText("no");
        idColumn.setWidth(30);
        idColumn.setResizable(true);
        
        TableColumn timeColumn = new TableColumn(operationTable, SWT.LEFT);
        timeColumn.setText("time");
        timeColumn.setWidth(100);
        timeColumn.setResizable(true);
        
        TableColumn actionColumn = new TableColumn(operationTable, SWT.LEFT);
        actionColumn.setText("action");
        actionColumn.setWidth(70);
        actionColumn.setResizable(true);
        
        TableColumn detailsColumn = new TableColumn(operationTable, SWT.LEFT);
        detailsColumn.setText("contents");
        detailsColumn.setWidth(150);
        detailsColumn.setResizable(true);
        
        operationTable.addSelectionListener(new SelectionListener() {
            
            public void widgetDefaultSelected(SelectionEvent evt) {
            }
            
            public void widgetSelected(SelectionEvent evt) {
                Table table = (Table)evt.getSource();
                int index = (int)table.getSelectionIndex();
                if (index != -1) {
                    base.setPresentIndex(index);
                }
            }
        });
        
        operationTable.addKeyListener(new KeyListener() {
            
            public void keyPressed(KeyEvent evt) {
            }
            
            public void keyReleased(KeyEvent evt) {
                int index = -1;
                if (evt.keyCode == SWT.ARROW_UP || evt.keyCode == SWT.ARROW_LEFT) {
                    index = base.getPrecedentOperationIndex();
                } else if (evt.keyCode == SWT.ARROW_DOWN || evt.keyCode == SWT.ARROW_RIGHT) {
                    index = base.getSuccessiveOperationIndex();
                }
                if (index != -1) {
                    base.setPresentIndex(index);
                }
            }
        });
        
        operationTable.addTraverseListener(new TraverseListener() {
            
            public void keyTraversed(TraverseEvent evt) {
                if (evt.detail == SWT.TRAVERSE_ARROW_PREVIOUS || evt.detail == SWT.TRAVERSE_ARROW_NEXT) {
                    evt.detail = SWT.TRAVERSE_NONE;
                    evt.doit = true;
                }
            }
        });
        
        resetTable();
        
        return operationTable;
    }
    
    public void setTable() {
        List<IChangeOperation> ops = base.getOperations();
        if (ops.size() == 0) {
            return;
        }
        
        createTableItems(ops);
        operationTable.deselectAll();
        operationTable.update();
        int index = base.getPresentIndex();
        if (index != -1) {
            operationTable.select(index);
        }
    }
    
    protected void createTableItems(List<IChangeOperation> ops) {
        operationTable.removeAll();
        for (int index = 0; index < ops.size(); index++) {
            IChangeOperation op = ops.get(index);
            TableItem item = new TableItem(operationTable, SWT.NONE);
            item.setText(0, String.valueOf(index + 1));
            item.setText(1, op.getFormatedTime());
            item.setText(2, op.getAction());
            item.setText(3, createOperationTextualRepresentation(op));
        }
    }
    
    public void updateTable() {
        int index = base.getPresentIndex();
        if (index != -1) {
            operationTable.select(index);
        }
    }
    
    protected void resetTable() {
        operationTable.removeAll();
        operationTable.update();
    }
    
    protected String createOperationTextualRepresentation(IChangeOperation op) {
        if (op.isDocument()) {
            return createDocumentOperationTextualRepresentation((DocumentOperation)op);
        } else if (op.isCopy()) {
            return createCopyOperationTextualRepresentation((CopyOperation)op);
        } else if (op.isFile()) {
            return createFileOperationTextualRepresentation((FileOperation)op);
        } else if (op.isCommand()) {
            return createCommandOperationTextualRepresentation((CommandOperation)op);
        }
        return "";
    }
    
    protected String createDocumentOperationTextualRepresentation(DocumentOperation op) {
        StringBuilder buf = new StringBuilder();
        buf.append(String.format("%3d", op.getStart()));
        buf.append(" ins[");
        buf.append(getShortText(op.getInsertedText()));
        buf.append("]");
        buf.append(" del[");
        buf.append(getShortText(op.getDeletedText()));
        buf.append("]");
        return buf.toString();
    }
    
    protected String createCopyOperationTextualRepresentation(CopyOperation op) {
        StringBuilder buf = new StringBuilder();
        buf.append(String.format("%3d", op.getStart()));
        buf.append(" ");
        buf.append(String.valueOf(op.getStart()));
        buf.append(" cop[");
        buf.append(getShortText(op.getCopiedText()));
        buf.append("]");
        return buf.toString();
    }
    
    protected String createFileOperationTextualRepresentation(FileOperation op) {
        StringBuilder buf = new StringBuilder();
        return buf.toString();
    }
    
    protected String createCommandOperationTextualRepresentation(CommandOperation op) {
        StringBuilder buf = new StringBuilder();
        buf.append(op.getCommandId());
        return buf.toString();
    }
    
    protected String getShortText(String text) {
        final int LESS_LEN = 9;
        String text2;
        if (text.length() < LESS_LEN + 1) {
            text2 = text;
        } else {
            text2 = text.substring(0, LESS_LEN + 1) + "...";
        }
        return text2.replace('\n', '~');
    }
    
    protected void revealTableItem() {
        int index = base.getPresentIndex();
        int size = base.getOperations().size();
        Rectangle area = operationTable.getClientArea();
        int num = area.height / operationTable.getItemHeight() - 1;
        int top = operationTable.getTopIndex();
        if (index < top) {
            if (size < num / 2) {
                operationTable.setTopIndex(top);
            } else {
                operationTable.setTopIndex(top - num / 2);
            }
        } else if (index >= top + num) {
            top = index - num + 1;
            if (size < num / 2) {
                operationTable.setTopIndex(top);
            } else {
                operationTable.setTopIndex(top + num / 2);
            }
        }
    }
}
