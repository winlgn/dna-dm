/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package datamining.layout;

import javax.swing.table.DefaultTableModel;

/**
 * 数据集属性细节TableModel.
 *
 * @author LiuGuining
 */
public class ArffAttributeDetailTable extends DefaultTableModel {

    public ArffAttributeDetailTable() {
        super();
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }
}
