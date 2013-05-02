package datamining.layout;

import datamining.core.Attribute;
import datamining.core.Instance;
import datamining.core.Instances;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 * 数据集修改用TableModel.
 *
 * @author LiuGuining
 */
public class CustomTableModel extends AbstractTableModel {

    private Instances originalDataSet;
    private Instances dataSet;
    private int offset;
    private List<Modify> actionList = new ArrayList<>();
    private List<String> attrList = new ArrayList<>();
    private int actionIndex = 0;

    public CustomTableModel(Instances dataSet) {
        this.originalDataSet = dataSet;
        this.dataSet = dataSet.clone();
        this.offset = dataSet.getOffset();
        initCulumnName();
    }

    @Override
    public int getRowCount() {
        return dataSet.numInstances();
    }

    @Override
    public int getColumnCount() {
        return dataSet.numAttributes() + 1;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex != 0;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            return rowIndex + 1;
        } else if (columnIndex == getColumnCount() - 1) {
            return (int) (dataSet.getInstances().get(rowIndex).
                    getAttrValues())[columnIndex - 1] + offset;
        } else {
            return (dataSet.getInstances().get(rowIndex).
                    getAttrValues())[columnIndex - 1];
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        double oldValue;
        double newValue;
        if (columnIndex == getColumnCount() - 1) {
            oldValue = (dataSet.getInstances().get(rowIndex).
                    getAttrValues())[columnIndex - 1] + offset;
        } else {
            oldValue = (dataSet.getInstances().get(rowIndex).
                    getAttrValues())[columnIndex - 1];
        }

        try {
            newValue = Double.parseDouble(aValue.toString());
        } catch (NumberFormatException ex) {
            return;
        }
        if (Double.compare(oldValue, newValue) != 0) {

            if (columnIndex == getColumnCount() - 1) {
                (dataSet.getInstances().get(rowIndex).
                        getAttrValues())[columnIndex - 1] = newValue - offset;
            } else {
                (dataSet.getInstances().get(rowIndex).
                        getAttrValues())[columnIndex - 1] = newValue;
            }
            actionList.add(new Modify(rowIndex, columnIndex - 1, oldValue, newValue));
            actionIndex = actionList.size();
            this.fireTableCellUpdated(rowIndex, columnIndex);
        }


    }

    public void deleteRows(int[] rows) {
        if (rows == null) {
            return;
        }
        List<Instance> insts = new ArrayList<>();
        Arrays.sort(rows);
        int[] clone = rows.clone();
        if (rows[0] < 0 || rows[rows.length - 1] > dataSet.numInstances() - 1) {
            return;
        }
        for (int i = 0; i < rows.length; i++) {
            rows[i] -= i;
            Instance remove = dataSet.getInstances().remove(rows[i]);
            insts.add(remove);
            this.fireTableRowsDeleted(rows[i], rows[i]);
        }
        actionList.add(new Modify(clone, insts));
        actionIndex = actionList.size();
    }

    public void insertRows(int row, int num) {
        if (row < 0) {
            row = getRowCount();
        }
        for (int i = 0; i < num; i++) {
            double[] attrValues = new double[dataSet.numAttributes()];
            Instance inst = new Instance(0, attrValues);
            if (row + 1 + i >= dataSet.numInstances()) {
                dataSet.getInstances().add(inst);
            } else {
                dataSet.getInstances().add(row + 1 + i, inst);
            }
        }
        actionList.add(new Modify(row + 1, num));
        actionIndex = actionList.size();
        this.fireTableRowsInserted(row + 1, row + num);
    }

    @Override
    public String getColumnName(int column) {
        return attrList.get(column);
    }

    /**
     * 撤销操作.
     */
    public void undo() {
        if (actionIndex <= 0 || actionList.isEmpty()) {
            return;
        }
        Modify m = actionList.get(actionIndex - 1);
        switch (m.type) {
            case Modify.UPDATE:
                (dataSet.getInstances().get(m.row).getAttrValues())[m.col] = m.oldValue;
                this.fireTableCellUpdated(m.row, m.col + 1);
                break;
            case Modify.INSERT:
                for (int i = 0; i < m.num; i++) {
                    dataSet.getInstances().remove(m.row);
                    this.fireTableRowsDeleted(m.row, m.row);
                }
                //       this.fireTableRowsDeleted(m.row, m.row + m.num - 1);
                break;
            case Modify.DELETE:
                for (int i = 0; i < m.rows.length; i++) {
                    dataSet.getInstances().add((m.rows)[i], m.values.get(i));
                    this.fireTableRowsInserted((m.rows)[i], (m.rows)[i]);
                }
                break;
            default:
                actionIndex++;
        }
        actionIndex--;
    }

    /**
     * 重做操作.
     */
    public void redo() {
        if (actionIndex >= actionList.size() || actionList.isEmpty()) {
            return;
        }
        Modify m = actionList.get(actionIndex);
        switch (m.type) {
            case Modify.UPDATE:
                (dataSet.getInstances().get(m.row).getAttrValues())[m.col] = m.newValue;
                this.fireTableCellUpdated(m.row, m.col + 1);
                break;
            case Modify.INSERT:
                for (int i = 0; i < m.num; i++) {
                    double[] attrValues = new double[dataSet.numAttributes()];
                    Instance inst = new Instance(0, attrValues);
                    dataSet.getInstances().add(m.row, inst);
                }
                this.fireTableRowsInserted(m.row, m.row + m.num - 1);
                break;
            case Modify.DELETE:
                int[] newRows = m.rows.clone();
                for (int i = 0; i < m.rows.length; i++) {
                    newRows[i] -= i;
                    Instance remove = dataSet.getInstances().remove(newRows[i]);
                    this.fireTableRowsDeleted(newRows[i], newRows[i]);
                }
                break;
            default:
                actionIndex--;
        }
        actionIndex++;
    }

    public boolean canUndo() {
        if (actionIndex <= 0 || actionList.isEmpty()) {
            return false;
        }
        return true;
    }

    public boolean canRedo() {
        if (actionIndex >= actionList.size() || actionList.isEmpty()) {
            return false;
        }
        return true;
    }

    /**
     * 属性约减.
     *
     * @param indexes 约减属性索引
     * @return 约减状态
     */
    public boolean reduceAttribute(int[] indexes) {
        if (indexes.length >= dataSet.numAttributes()) {
            return false;
        }
        for (int i = 0; i < indexes.length; i++) {
            if (indexes[i] < 0 || indexes[i] >= dataSet.classIndex()) {
                return false;
            }
        }
        //修改Attribute
        List<Attribute> newAttrs = new ArrayList<>();
        for (int i = 0; i < indexes.length; i++) {
            newAttrs.add(dataSet.getAttributes().get(indexes[i]));
        }
        newAttrs.add(dataSet.classAttribute());
        dataSet.setAttributes(newAttrs);

        //修改Instances
        for (int i = 0; i < dataSet.numInstances(); i++) {
            double[] values = new double[indexes.length + 1];
            for (int j = 0; j < indexes.length; j++) {
                values[j] = (dataSet.getInstances().get(i).getAttrValues())[indexes[j]];
            }
            values[indexes.length] = dataSet.getInstances().get(i).classValue();
            dataSet.getInstances().get(i).setAttrValues(values);
            dataSet.getInstances().get(i).setClassIndex(indexes.length);
        }
        dataSet.setClassIndex(indexes.length);
        initCulumnName();
        this.fireTableStructureChanged();
        return true;
    }

    private class Modify {

        static final int INSERT = 0;
        static final int DELETE = 1;
        static final int UPDATE = 2;
        int row;
        int col;
        int num;
        int type;
        double oldValue;
        double newValue;
        int[] rows;
        List<Instance> values;

        /**
         * 修改单个单元格
         *
         * @param row
         * @param col
         * @param oldValue
         * @param newValue
         */
        Modify(int row, int col, double oldValue, double newValue) {
            this.row = row;
            this.col = col;
            this.oldValue = oldValue;
            this.newValue = newValue;
            this.type = UPDATE;
        }

        /**
         * 删除选定的行
         *
         * @param rows
         */
        Modify(int[] rows, List<Instance> insts) {
            this.rows = rows;
            this.values = insts;
            this.type = DELETE;
        }

        public Modify(int row, int num) {
            this.row = row;
            this.num = num;
            this.type = INSERT;
        }

        @Override
        public String toString() {

            switch (type) {
                case UPDATE:
                    return "UPDATE:[" + row + "," + col + "]:'" + oldValue + "' -> '" + newValue + "'";
                case INSERT:
                    return "INSERT:ROW:" + row + ",NUM=" + num;
                case DELETE:
                    StringBuilder sb = new StringBuilder("DELETE:ROW[ ");
                    for (int i : rows) {
                        sb.append(i).append(" ");
                    }
                    sb.append("]");
                    return sb.toString();
                default:
                    return "ERROR";
            }
        }
    }

    private void initCulumnName() {
        attrList.clear();
        attrList.add("#");
        for (Attribute attr : dataSet.getAttributes()) {
            attrList.add(attr.getAttributeName());
        }
    }
    /**
     * 取得当前数据集.
     * @return 当前数据集
     */
    public Instances getDataSet() {
        return dataSet;
    }

    public Integer[] getClassIndexes() {
        Integer[] indexes = new Integer[dataSet.classAttribute().getDecision().length];
        for (int i = 0; i < indexes.length; i++) {
            indexes[i] = (dataSet.classAttribute().getDecision())[i] + offset;

        }
        return indexes;
    }

    /**
     * 清空动作列表.
     */
    public void clearActionList() {
        actionList.clear();
    }

    public boolean isActionListEmpty() {
        return actionList.isEmpty();
    }

    /**
     * 保存修改到原数据集.
     */
    public void save() {
        originalDataSet.setAttributes(dataSet.getAttributes());
        originalDataSet.setInstances(dataSet.getInstances());
        originalDataSet.setClassIndex(dataSet.classIndex());
    }
}
