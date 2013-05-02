package datamining.util;

import datamining.core.Instance;
import datamining.core.Instances;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * 图表数据预处理.
 *
 * @author LiuGuining
 */
public class ChartDataUtil {

    private static final double OFFSET = 0.0005;
    private static final double OFFSET_1 = 0.001;
    private Instances dataSet;
    private int parts;
    private int index;
    private double min;
    private double max;
    private double width;
    private int[][] result;
    private double[] d;
    private int[] classIndexes;

    /**
     * 初始化ChartDataUtil.
     *
     * @param dataSet 数据集
     * @param parts 分段数
     */
    public ChartDataUtil(Instances dataSet, int parts) {
        this.dataSet = dataSet;
        this.index = dataSet.classIndex();
        this.parts = parts;
        if (index == dataSet.classIndex()) {
            doItClass();
        } else {
            doIt();
        }
    }

    /**
     * 初始化ChartDataUtil.
     *
     * @param dataSet 数据集
     * @param index 属性索引
     * @param parts 分段数
     */
    public ChartDataUtil(Instances dataSet, int index, int parts) {
        this.dataSet = dataSet;
        this.index = index;
        this.parts = parts;
        if (index == dataSet.classIndex()) {
            doItClass();
        } else {
            doIt();
        }
    }

    /**
     * 设置属性.
     *
     * @param index 属性索引
     */
    public void setIndex(int index) {
        this.index = index;
        if (index == dataSet.classIndex()) {
            doItClass();
        } else {
            doIt();
        }
    }

    /**
     * 设置分段数.
     *
     * @param parts 分段数
     */
    public void setParts(int parts) {
        this.parts = parts;
        doIt();
    }

    /**
     * 取得分段数.
     *
     * @return 分段数
     */
    public int getParts() {
        return parts;
    }

    /**
     * 取得当前属性最大值.
     *
     * @return 当前属性最大值
     */
    public double getMax() {
        return max;
    }

    /**
     * 取得当前属性最小值.
     *
     * @return 当前属性最小值
     */
    public double getMin() {
        return min;
    }

    /**
     * 对Decision属性数据进行处理.
     */
    private void doItClass() {
        List<Instance> insts = dataSet.getInstances();
        SortedMap<Integer, Integer> map = new TreeMap<>();
        int[] decision = dataSet.classAttribute().getDecision();
        for (int i : decision) {
            map.put(i, 0);
        }
        for (Instance inst : insts) {
            int value = (int) (inst.getAttrValues())[index];
            int sum = map.get(value).intValue();
            map.put(value, sum + 1);
        }

        result = new int[dataSet.numClasses()][1];
        Set<Entry<Integer, Integer>> set = map.entrySet();
        int i = 0;
        for (Entry<Integer, Integer> entry : set) {
            result[i][0] = entry.getValue();
            i++;
        }

    }

    /**
     * 对非Decision属性数据进行处理.
     */
    private void doIt() {
        List<Instance> insts = dataSet.getInstances();
        d = new double[dataSet.numInstances()];

        for (int i = 0; i < d.length; i++) {
            d[i] = (insts.get(i).getAttrValues())[index];
        }

        classIndexes = new int[dataSet.numInstances()];
        for (int i = 0; i < classIndexes.length; i++) {
            classIndexes[i] = (int) (insts.get(i).getAttrValues())[dataSet.classIndex()];

        }
        MathUtil mathUtil = new MathUtil(d);
        max = mathUtil.getMax() + OFFSET_1 + OFFSET;
        min = mathUtil.getMin() - OFFSET_1 + OFFSET;
        width = (max - min) / parts;
        double[] boundary = new double[parts - 1];
        for (int i = 0; i < boundary.length; i++) {
            boundary[i] = min + width * (i + 1) + OFFSET;
        }

        result = new int[parts][dataSet.numClasses()];
        for (int i = 0; i < result.length; i++) {
            for (int j = 0; j < result[i].length; j++) {
                result[i][j] = 0;
            }
        }
        for (int i = 0; i < d.length; i++) {
            int j;
            for (j = 0; j < boundary.length; j++) {
                if (d[i] < boundary[j]) {
                    result[j][classIndexes[i]]++;
                    break;
                }
            }
            if (j == boundary.length) {
                result[boundary.length][classIndexes[i]]++;
            }
        }
    }

    /**
     * 取得处理结果.
     *
     * @return 数据集处理结果
     */
    public int[][] getResult() {

        return result;
    }
}
