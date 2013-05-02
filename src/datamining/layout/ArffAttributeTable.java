/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package datamining.layout;

import javax.swing.table.DefaultTableModel;

/**
 * 数据集属性列举TableModel.
 *
 * @author LiuGuining
 */
public class ArffAttributeTable extends DefaultTableModel {

    public ArffAttributeTable() {
        super();
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }
}
