/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelcutter.gui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import modelcutter.Model;

/**
 *
 * @author MICHAL
 */
public class Models3dExportingTableModel extends AbstractTableModel {

    private List<Model> modelsToSave;
    private List<Boolean> isModelToExportList;
    private Boolean[] isToExport;
    
    public Models3dExportingTableModel(){
        this.modelsToSave = new ArrayList<>();
        this.isModelToExportList = new ArrayList<>();
        this.isToExport = new Boolean[this.modelsToSave.size()];
        Arrays.fill(this.isToExport, false);
    }
    
    public void addModels3d(List<Model> models){
        this.modelsToSave.addAll(models);
        this.isToExport = new Boolean[this.modelsToSave.size()];
        Arrays.fill(this.isToExport, false);
        int lastRow = this.modelsToSave.size() - 1;
        this.fireTableRowsInserted(lastRow, lastRow);
    }
    
    public Model getModel3d(int rowIndex){
        return this.modelsToSave.get(rowIndex);
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
        Model model = this.modelsToSave.get(rowIndex);
        switch(columnIndex) {
            case 0:
                model.setColor((Color) aValue);
                break;
            case 1:
                model.setModelName((String) aValue);
                break;
            case 2:
                this.isToExport[rowIndex] = (Boolean) aValue;
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
                return "Export";
            default:
                throw new IllegalArgumentException("columnIndex");
        }
    }
    
    @Override
    public int getRowCount() {
        return this.modelsToSave.size();
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Model model = this.modelsToSave.get(rowIndex);
        switch(columnIndex){
            case 0 :
                return model.getColor();
            case 1 : 
                return model.getModelName();
            case 2:
                return this.isToExport[rowIndex];
            default:
                throw new IllegalArgumentException("columnIndex");
        }
    }
    
}
