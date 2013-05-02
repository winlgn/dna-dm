/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package datamining.layout.observer;

import datamining.core.Attribute;
import datamining.core.Instance;
import datamining.core.Instances;
import datamining.layout.ArffAttributeDetailTable;
import datamining.layout.Chart;
import datamining.util.MathUtil;
import java.text.DecimalFormat;
import java.util.*;
import javax.swing.JLabel;

/**
 *
 * @author LiuGuining
 */
public class ArffAttributeDetailObserver implements Observer {

    private ArffAttributeDetailTable model;
    private Instances dataSet;
    private int line;
    private JLabel nameLable;
    private JLabel typeLabel;
    private JLabel distinctLabel;
    private JLabel uniqueLabel;
    private Chart chart;

    public ArffAttributeDetailObserver(ArffAttributeDetailTable model, JLabel nameLable, JLabel typeLabel, JLabel distinctLabel, JLabel uniqueLabel, Chart chart) {
        this.model = model;
        this.nameLable = nameLable;
        this.typeLabel = typeLabel;
        this.distinctLabel = distinctLabel;
        this.uniqueLabel = uniqueLabel;
        this.chart = chart;
    }

    @Override
    public void update(Observable o, Object arg) {
        if (!(arg instanceof Integer) && !(arg instanceof Instances)) {
            return;
        }

        if (arg instanceof Instances) {
            dataSet = (Instances) arg;
            chart.setDataSet(dataSet);
        }
        if (arg instanceof Integer && dataSet != null) {
            line = (int) arg;
            List<Attribute> attributes = dataSet.getAttributes();
            Attribute attr = attributes.get(line);
//            List<Double> data = attr.getAttributeValues();
//            double d[] = new double[data.size()];
//            for (int i = 0; i < d.length; i++) {
//                d[i] = data.get(i).doubleValue();
//            }
            List<Instance> insts = dataSet.getInstances();
            double[] d = new double[insts.size()];
            for (int i = 0; i < d.length; i++) {
                d[i] = (insts.get(i).getAttrValues())[line];
            }
            if (attr.getType() == Attribute.NUMERIC) {
                MathUtil mathUtil = new MathUtil(d);
                Object[][] tableContent = new Object[4][2];
                DecimalFormat dFormat = new DecimalFormat("#.###");
                tableContent[0][0] = "最大值";
                tableContent[0][1] = dFormat.format(mathUtil.getMax());
                tableContent[1][0] = "最小值";
                tableContent[1][1] = dFormat.format(mathUtil.getMin());
                tableContent[2][0] = "平均值";
                tableContent[2][1] = dFormat.format(mathUtil.getMean());
                tableContent[3][0] = "标准差";
                tableContent[3][1] = dFormat.format(mathUtil.getStdDev());
                model.setDataVector(tableContent, new String[]{"统计", "值"});
                typeLabel.setText("数值");
                uniqueLabel.setText(mathUtil.getUnique() + "/" + mathUtil.length());



            } else if (attr.getType() == Attribute.DECISION) {
                Map<Integer, Integer> map = new HashMap<>();
                int[] decision = attr.getDecision();
                for (int i = 0; i < decision.length; i++) {
                    map.put(decision[i], 0);
                }
                for (int i = 0; i < d.length; i++) {
                    int count = map.get((int) d[i]);
                    map.put((int) d[i], count + 1);
                }
                Object[][] tableContent = new Object[decision.length][3];
                for (int i = 0; i < tableContent.length; i++) {
                    int count = map.get(decision[i]);
                    tableContent[i][0] = decision[i] + dataSet.getOffset();
                    tableContent[i][1] = count;
                    tableContent[i][2] = new DecimalFormat("#.#%").format((double) count / d.length);
                }
                model.setDataVector(tableContent, new String[]{"值", "数量", "比例"});
                typeLabel.setText("枚举");
                uniqueLabel.setText("N/A");
            }
            nameLable.setText(attr.getAttributeName());
            distinctLabel.setText(String.valueOf(insts.size()));
            chart.setIndex(line);
        }


    }
}
