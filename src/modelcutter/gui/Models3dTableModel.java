/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelcutter.gui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import modelcutter.GUI;
import modelcutter.Model;

/**
 *
 * @author MICHAL
 */
public class Models3dTableModel extends AbstractTableModel {
    
    private List<Model> models;
    private GUI gui;

    public Models3dTableModel(GUI gui) {
        this.models = new ArrayList<>();
        this.gui = gui;
    }
    
    public void addModel3d(Model model){
        this.models.add(model);
        int lastRow = this.models.size() - 1;
        this.fireTableRowsInserted(lastRow, lastRow);
    }
    
    public void addModels3d(List<Model> models){
        this.models.addAll(models);
        int lastRow = this.models.size() - 1;
        this.fireTableRowsInserted(lastRow, lastRow);
    }
    
    public void removeModel3d(Model model){
        this.models.remove(model);
        int lastRow = this.models.size() - 1;
        this.fireTableRowsDeleted(lastRow, lastRow);
    }
    
    public void removeAllModels3d(){
        this.models.clear();
        int lastRow = this.models.size();
        this.fireTableRowsDeleted(lastRow, lastRow);
    }
    
    public Model getModel3d(int index){
        return this.models.get(index);
    }
    
    public List<Model> getAllmodels3d(){
        return this.models;
    }
    
    @Override
    public Class<?> getColumnClass(int columnIndex){
        switch(columnIndex) {
            case 0: 
                return Color.class;
            case 1:
                return String.class;
            case 2:
                return Boolean.class;
            default:
                throw new IllegalArgumentException("columnIndex");
        }
    }
    
    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        Model model = this.models.get(rowIndex);
        switch(columnIndex) {
            case 0:
                model.setColor((Color) aValue);
                break;
            case 1:
                model.setModelName((String) aValue);
                break;
            case 2:
                model.setVisible((Boolean) aValue);
                this.gui.rendererRepaint();
                this.gui.setSelectedModelInRenderer(model);
                break;
            default:
                throw new IllegalArgumentException("columnIndex");
        }
        this.fireTableCellUpdated(rowIndex, columnIndex);
    }
    
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        switch(columnIndex) {
            case 0:
            case 1:
                return true;
            case 2:
                return true;
            default:
                throw new IllegalArgumentException("columnIndex");
        }
    }
    
    @Override
    public String getColumnName(int columnIndex){
        switch(columnIndex) {
            case 0:
                return "";
            case 1:
                return "Name";
            case 2:
                return "Visible";
            default:
                throw new IllegalArgumentException("columnIndex");
        }
    }
    
    @Override
    public int getRowCount() {
        return this.models.size();
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Model model = this.models.get(rowIndex);
        switch(columnIndex){
            case 0 :
                return model.getColor();
            case 1 : 
                return model.getModelName();
            case 2 :
                return model.isVisible();
            default:
                throw new IllegalArgumentException("columnIndex");
        }
    }
    
}
