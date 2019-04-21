/*
 * An example answer
 * Copyright 2014, Katsuhisa Maruyama
 */

package jp.ac.ritsumei.cs.draw;

import javax.swing.*;
import java.awt.*;

/**
 * Selects one of the predefined figures.
 */
public class FigureSelector extends JPanel {
    
    private static final long serialVersionUID = -8502935409963644243L;
    
    /**
     * The combo box for selecting the shape of a figure to be drawn.
     */
    private JComboBox<FigureIcon> shapeCombo;
    
    /**
     * The combo box for selecting the color of a figure to be drawn.
     */
    private JComboBox<FigureIcon> colorCombo;
    
    /**
     * The combo box for selecting the width of the outline of a figure to be drawn.
     */
    private JComboBox<FigureIcon> widthCombo;
    
    private final static FigureIcon[] shapeComboIcons = new FigureIcon[] {
        new FigureIcon(new Line()),
        new FigureIcon(new Rect()),
        new FigureIcon(new FilledRect()),
        new FigureIcon(new Oval()),
        new FigureIcon(new FilledOval())
    };
    
    private final static FigureIcon[] colorComboIcons = new FigureIcon[] {
        new FigureIcon(new Line(Color.black)),
        new FigureIcon(new Line(Color.yellow)),
        new FigureIcon(new Line(Color.green)),
        new FigureIcon(new Line(Color.red)),
        new FigureIcon(new Line(Color.pink)),
        new FigureIcon(new Line(Color.blue)),
        new FigureIcon(new Line(Color.magenta)),
        new FigureIcon(new Line(Color.orange)),
        new FigureIcon(new Line(Color.gray)),
        new FigureIcon(new Line(Color.darkGray)),
        new FigureIcon(new Line(Color.lightGray)),
        new FigureIcon(new Line(Color.cyan)),
        new FigureIcon(new Line(Color.white))
    };
    
    private final static FigureIcon[] widthComboIcons = new FigureIcon[] {
        new FigureIcon(new Line(1)),
        new FigureIcon(new Line(3)),
        new FigureIcon(new Line(5)),
        new FigureIcon(new Line(9)),
    };
    
    /**
     * Creates a new figure selector.
     */
    FigureSelector() {
        setLayout(new FlowLayout(FlowLayout.RIGHT));
        createComboBoxes();
    }
    
    /**
     * Creates the combo boxes for choosing the shape, color, and outline width of a figure to be drawn.
     */
    private void createComboBoxes() {
        JLabel shapeLabel = new JLabel("Shape:");
        add(shapeLabel);
        shapeCombo = new JComboBox<FigureIcon>();
        add(shapeCombo);
        
        shapeCombo.addItem(new CursorIcon());
        addItems(shapeCombo, shapeComboIcons);
        
        JLabel colorLabel = new JLabel("Color:");
        add(colorLabel);
        colorCombo = new JComboBox<FigureIcon>();
        add(colorCombo);
        
        addItems(colorCombo, colorComboIcons);
        
        JLabel lineWidthLabel = new JLabel("LineWidth:");
        add(lineWidthLabel);
        widthCombo = new JComboBox<FigureIcon>();
        add(widthCombo);
        
        addItems(widthCombo, widthComboIcons);
    }
    
    /**
     * Adds icons into a given combo box.
     * @param cb the combo box containing the added icons
     * @param icons the icons to be added
     */
    private void addItems(JComboBox<FigureIcon> cb, FigureIcon[] icons) {
        for (int i = 0; i < icons.length; i++) {
            cb.addItem(icons[i]);
        }
    }
    
    /**
     * Obtains the shape of a figure.
     * @return the selected figure
     */
    private Figure getShape() {
        FigureIcon icon = (FigureIcon)shapeCombo.getSelectedItem();
        return icon.getFigure();
    }
    
    /**
     * Obtains the color of a figure.
     * @return the selected color
     */
    private Color getColor() {
        FigureIcon icon = (FigureIcon)colorCombo.getSelectedItem();
        return icon.getFigure().getColor();
    }
    
    /**
     * Obtains the width of the outline of a figure.
     * @return the selected width of the outline
     */
    private float getLineWidth() {
        FigureIcon icon = (FigureIcon)widthCombo.getSelectedItem();
        return icon.getFigure().getOutlineWidth();
    }
    
    /**
     * Obtains a new figure the user specifies.
     * @return the newly created figure
     */
    public Figure createFigure() {
        Figure shape = getShape();
        if (shape != null) {
            return shape.create(getColor(), getLineWidth());
        }
        return null;
    }
    
    /**
     * Tests if the current selection is the grab mode.
     * @return <code>true</code> if the current selection is the grab mode, otherwise <code>false</code>
     */
    boolean isGrabMode() {
        return getShape() == null;
    }
}
