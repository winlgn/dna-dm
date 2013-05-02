package datamining.attributeselection;

import datamining.core.AttributeIndex;
import datamining.core.Instances;
import datamining.rough.Util;
import java.util.Arrays;

/**
 * 考虑样本不平衡的模型无关的特征选择方法.
 *
 * @author lenovo
 */
public class NewGeneMethod implements IAttributeSelection {

    public NewGeneMethod() {
    }

    public static double CalcStdev(double[] varValue) {
        double totalValue = 0;
        for (int i = 0; i < varValue.length; i++) {
            totalValue += varValue[i];
        }
        double averageValue = totalValue / varValue.length;

        totalValue = 0;
        for (int i = 0; i < varValue.length; i++) {
            totalValue += (varValue[i] - averageValue) * (varValue[i] - averageValue);
        }

        return Math.sqrt(totalValue / varValue.length - 1);
    }

    public static double CalcMean(double[] varValue, int begin, int end) {
        double totalValue = 0;
        for (int i = begin; i < end; i++) {
            totalValue += varValue[i];
        }
        return totalValue / (end - begin);
    }

    public static AttributeIndex[] GetAttributeWeight(Instances instances) {

        instances = Util.NormalizationInstances(instances);

        int numClass = instances.numClasses();
        int numAttribute = instances.numAttributes() - 1;
        int numInstance = instances.numInstances();
        AttributeIndex[] attributeIndexs = new AttributeIndex[numAttribute];

        double[] disVectorForAttr = new double[numInstance];
        double[] averageForClass = new double[numClass];
        double[] numForClass = new double[numClass];
        double[] w = new double[numClass];
        double W = 0;
        double mean = 0;
        double stdDis = 0;
        double stdAverage = 0;

        for (int i = 0; i < numForClass.length; i++) {
            numForClass[i] = 0;
        }
        for (int i = 0; i < numInstance; i++) {
            numForClass[(int) instances.instance(i).classValue()]++;
        }
        for (int i = 0; i < w.length; i++) {
            w[i] = 1.0 / numForClass[i];
        }
        W = numClass;
        for (int i = 0; i < numAttribute; i++) {
            for (int j = 0; j < averageForClass.length; j++) {
                averageForClass[j] = 0;
            }
            for (int j = 0; j < disVectorForAttr.length; j++) {
                disVectorForAttr[j] = 0;
            }
            //注意越界，如果记录比较多就先除以每类个数
            for (int j = 0; j < numInstance; j++) {
                averageForClass[(int) instances.instance(j).classValue()] += instances.instance(j).value(i);
            }
            for (int j = 0; j < averageForClass.length; j++) {
                averageForClass[j] = averageForClass[j] / numForClass[j];
            }
            for (int j = 0; j < disVectorForAttr.length; j++) {
                disVectorForAttr[j] = Math.abs(instances.instance(j).value(i) - averageForClass[(int) instances.instance(j).classValue()]);
            }

//			for (int j = 0; j < disVectorForAttr.length; j++) {
//				disVectorForAttr[j] = instances.instance(j).value(i)-averageForClass[(int)instances.instance(j).classValue()];
//			}

            mean = 0;
            for (int j = 0; j < disVectorForAttr.length; j++) {
                mean += w[(int) instances.instance(j).classValue()] / W * disVectorForAttr[j];
            }
            stdDis = 0;
            for (int j = 0; j < disVectorForAttr.length; j++) {
                stdDis += Math.pow(disVectorForAttr[j] - mean, 2);
            }
            double tmp = (double) (numInstance - 1) / (double) (numInstance) * W;
            stdDis = stdDis / tmp;
            stdDis = Math.sqrt(stdDis);
            mean = NewGeneMethod.CalcMean(disVectorForAttr, 0, disVectorForAttr.length);
            stdDis = NewGeneMethod.CalcStdev(disVectorForAttr);
            stdDis = Math.sqrt(stdDis);
            stdAverage = NewGeneMethod.CalcStdev(averageForClass);
            stdAverage = Math.sqrt(stdAverage);
            attributeIndexs[i] = new AttributeIndex();
            attributeIndexs[i].index = i;
            attributeIndexs[i].value = mean * stdDis / stdAverage;
        }
        AttributeIndex.order = 0;
        Arrays.sort(attributeIndexs);
        return attributeIndexs;
    }

    @Override
    public int[] getFirstKAttributes(Instances instances, int K) {
        AttributeIndex[] attributeIndexs = NewGeneMethod.GetAttributeWeight(instances);
        int[] index = new int[K];
        for (int i = 0; i < index.length; i++) {
            index[i] = attributeIndexs[i].index;
        }
        return index;
    }
}
