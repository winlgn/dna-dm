package datamining.attributeselection;

import datamining.core.AttributeIndex;
import datamining.core.Instances;
import datamining.util.ArffFileReader;
import java.io.File;
import java.util.Arrays;

/**
 * 通过方差分析进行特征选择的方法.<br/>通过组内方差和组间方差将数据进行分隔，单个组内方差尽可能小，而每组之间的方差尽可能大.<br/>适用于高维数据.
 *
 * @author lenovo
 */
public class BSSWSS implements IAttributeSelection {

    /**
     * 取得偏离值之和.
     *
     * @param varValue
     * @return 偏离值之和
     */
    public static double calcSquareDev(double[] varValue) {
        return calcSquareDev(varValue, 0, varValue.length);
    }

    /**
     * 数组中一段数值的偏离值之和
     *
     * @param varValue
     * @param begin
     * @param end
     * @return 选定位置的偏离值之和
     */
    public static double calcSquareDev(double[] varValue, int begin, int end) {
        double averageValue = calcMean(varValue, begin, end);
        double totalValue = 0;
        for (int i = begin; i < end; i++) {
            totalValue += (varValue[i] - averageValue) * (varValue[i] - averageValue);
        }
        return totalValue;
    }

    /**
     * 求平均值
     *
     * @param varValue
     * @param begin
     * @param end
     * @return 选定区间的平均数
     */
    public static double calcMean(double[] varValue, int begin, int end) {
        double totalValue = 0;
        for (int i = begin; i < end; i++) {
            totalValue += varValue[i];
        }
        return totalValue / (end - begin);
    }

    /**
     * 获取属性权重
     *
     * @param instances
     * @return 获得依照权重排完序后的属性标号数组
     */
    public static AttributeIndex[] getAttributeWeight(Instances instances) {
        int numAttribute = instances.numAttributes() - 1;
        int numInstance = instances.numInstances();
        int numClass = instances.numClasses();
        instances.sort(instances.classIndex());
        double[] meanValues = new double[numClass];
        int[] partitionIndexs = new int[numClass];
        double[] value = new double[numInstance];
        AttributeIndex[] attributeIndexs = new AttributeIndex[numAttribute];
        int[] numInstancesForClass = new int[instances.numClasses()];

        int group = 0;
        int n = 1;
        for (int i = 1; i < numInstance; i++) {
            if (instances.instance(i).classValue() != instances.instance(i - 1).classValue()) {
                partitionIndexs[group] = i;
                numInstancesForClass[group++] = n;
                n = 1;
            } else {
                n++;
            }
        }
        numInstancesForClass[group] = n;
        partitionIndexs[group++] = numInstance;

        for (int i = 0; i < numAttribute; i++) {
            for (int j = 0; j < numInstance; j++) {
                value[j] = instances.instance(j).value(i);
            }

            double betweenGroup = 0;
            double insideGroup = 0;
            int begin = 0;
            int end = 0;

            double mean = calcMean(value, 0, value.length);

            for (int j = 0; j < group; j++) {
                begin = end;
                end = partitionIndexs[j];
                insideGroup += calcSquareDev(value, begin, end);
                meanValues[j] = calcMean(value, begin, end);
                betweenGroup += Math.pow((meanValues[j] - mean), 2) * numInstancesForClass[j];
            }
            attributeIndexs[i] = new AttributeIndex();
            attributeIndexs[i].index = i;
            attributeIndexs[i].value = insideGroup / betweenGroup;
        }
        Arrays.sort(attributeIndexs);

        for (int i = 0; i < attributeIndexs.length; i++) {
            attributeIndexs[i].value = attributeIndexs[i].value / attributeIndexs[attributeIndexs.length - 1].value;
        }

        return attributeIndexs;
    }

    @Override
    public int[] getFirstKAttributes(Instances instances, int K) {
        AttributeIndex[] attributeIndexs = getAttributeWeight(instances);
        int[] index = new int[(K <= attributeIndexs.length) ? K : attributeIndexs.length];
        for (int i = 0; i < index.length; i++) {
            index[i] = attributeIndexs[i].index;
        }
        return index;
    }

    public static void main(String[] args) {
        Instances instances = new ArffFileReader("D:" + File.separator + "colon.arff").getDataSet();
        AttributeIndex[] b;
        b = BSSWSS.getAttributeWeight(instances);
        for (int i = 0; i < b.length; i++) {
            System.out.println(b[i].value + " ");
            System.out.println(b[i].index);
        }
//        instances.sort(19);
//        List<Instance> li = instances.getInstances();
//        for (int i = 0; i < li.size(); i++) {
//            Instance inst = instances.instance(i);
//            double[] tmp = inst.getAttrValues();
//            System.out.println((i + 1) + ":" + tmp[0] + " " + tmp[1]);
//
//        }
    }
}
