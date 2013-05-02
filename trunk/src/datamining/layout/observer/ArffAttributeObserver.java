/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package datamining.layout.observer;

import datamining.core.Attribute;
import datamining.core.Instances;
import datamining.layout.ArffAttributeTable;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import javax.swing.JLabel;

/**
 *
 * @author LiuGuining
 */
public class ArffAttributeObserver implements Observer {

    private ArffAttributeTable model = null;
    private JLabel relationLabel = null;
    private JLabel attrSumLabel = null;
    private String[] titles = {"Index", "属性"};

    public ArffAttributeObserver(ArffAttributeTable model, JLabel relationLabel, JLabel attrSumLabel) {
        this.model = model;
        this.relationLabel = relationLabel;
        this.attrSumLabel = attrSumLabel;
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof Instances) {
            Instances dataSet = (Instances) arg;
            List<Attribute> attribute = dataSet.getAttributes();
            String[][] list = new String[attribute.size()][2];
            for (int i = 0; i < list.length; i++) {
                Attribute tmp = attribute.get(i);
                list[i][0] = String.valueOf(tmp.getIndex());
                list[i][1] = tmp.getAttributeName();
            }
            model.setDataVector(list, titles);
            relationLabel.setText(dataSet.getRelationName());
            attrSumLabel.setText(String.valueOf(attribute.size()));
        }
    }
}
