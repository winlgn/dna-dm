package datamining.attributeselection;

import datamining.core.AttributeIndex;
import datamining.core.Instance;
import datamining.core.Instances;
import datamining.util.ArffFileReader;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

/**
 *
 *
 * @author lenovo
 */
class Compare implements Comparator {

    public double value;
    public int attr;
    public Instances instances;

    @Override
    public int compare(Object arg0, Object arg1) {
        int a = ((InstanceIndex) arg0).index;
        int b = ((InstanceIndex) arg1).index;
        double c = Math.abs((instances.instance(a).value(attr) - value))
                - Math.abs((instances.instance(b).value(attr) - value));
        if (c > 0) {
            return 1;
        } else if (c < 0) {
            return -1;
        } else {
            return 0;
        }
    }
}

class InstanceIndex {

    int index;
}

/**
 * 计算复杂度较小的特征选择方法.<br/>在一定程度上考虑了属性间的相关性,以属性区分“相近”样本的能力作为评估属性重要性的标准.
 *
 * @author LiuGuining
 */
public class Relief implements IAttributeSelection {

    public Relief() {
    }

    public static AttributeIndex[] getAttributeWeight(Instances instances, Boolean isRandom, int M, int K) {

        double[] MAX = new double[instances.numAttributes() - 1];
        double[] MIN = new double[instances.numAttributes() - 1];

        double GMAX = -Double.MAX_VALUE;
        double GMIN = Double.MAX_VALUE;

        for (int i = 0; i < instances.numAttributes() - 1; i++) {
            MAX[i] = -Double.MAX_VALUE;
            MIN[i] = Double.MAX_VALUE;
        }

        for (int i = 0; i < instances.numAttributes() - 1; i++) {

            for (int j = 0; j < instances.numInstances(); j++) {
                MAX[i] = Math.max(MAX[i], instances.instance(j).value(i));
                MIN[i] = Math.min(MIN[i], instances.instance(j).value(i));

                GMAX = Math.max(GMAX, instances.instance(j).value(i));
                GMIN = Math.min(GMIN, instances.instance(j).value(i));
            }
        }


        int[] select = new int[M];
        if (isRandom == true) {
            Random random = new Random();
            for (int i = 0; i < select.length; i++) {
                select[i] = random.nextInt(instances.numInstances());
            }
        } else {
            int k = 0;
            while (k < M) {
                select[k] = k % instances.numInstances();
                k++;
            }
        }

        InstanceIndex[] instanceIndexs = new InstanceIndex[instances.numInstances()];

        for (int i = 0; i < instanceIndexs.length; i++) {
            instanceIndexs[i] = new InstanceIndex();
            instanceIndexs[i].index = i;
        }
        Compare compare = new Compare();
        compare.instances = instances;

        AttributeIndex[] attributeIndexs = new AttributeIndex[instances.numAttributes() - 1];

        for (int i = 0; i < attributeIndexs.length; i++) {
            attributeIndexs[i] = new AttributeIndex();
            attributeIndexs[i].index = i;
            attributeIndexs[i].value = 0;
        }

        int m = -1;
        int[][] KInstancesForClass = new int[instances.numClasses()][K + 1];
        double HValue = 0;
        double[] MValue = new double[instances.numClasses()];
        int RLable = 0;
        double RValue = 0;


        int[] numInstancesForClass = new int[instances.numClasses()];
        for (int i = 0; i < numInstancesForClass.length; i++) {
            numInstancesForClass[i] = 0;
        }

        for (int i = 0; i < instances.numInstances(); i++) {
            int label = (int) instances.instance(i).classValue();
            numInstancesForClass[label]++;
        }

        while (++m < M) {
            Instance R = instances.instance(select[m]);
            RLable = (int) R.classValue();

            for (int i = 0; i < instances.numAttributes() - 1; i++) {
                RValue = R.value(i);
                compare.attr = i;
                compare.value = RValue;
                Arrays.sort(instanceIndexs, compare);

                for (int j = 0; j < KInstancesForClass.length; j++) {
                    KInstancesForClass[j][K] = 0;
                }

                for (int j = 0; j < instanceIndexs.length; j++) {
                    int label = (int) instances.instance(instanceIndexs[j].index).classValue();
                    int tot = KInstancesForClass[label][K];
                    if (tot < K) {
                        KInstancesForClass[label][K]++;
                        KInstancesForClass[label][tot] = instanceIndexs[j].index;
                    }
                }

                HValue = 0;
                for (int j = 0; j < K; j++) {
                    HValue = Math.abs(instances.instance(KInstancesForClass[RLable][j]).value(i) - RValue);
                }
                HValue = HValue / (MAX[i] - MIN[i]);//��һ��


                for (int j = 0; j < MValue.length; j++) {
                    MValue[j] = 0;
                }

                for (int label = 0; label < instances.numClasses(); label++) {

                    if (label == RLable) {
                        continue;
                    }

                    int tot = KInstancesForClass[label][K];
                    for (int j = 0; j < tot; j++) {
                        MValue[label] += Math.abs(instances.instance(KInstancesForClass[label][j]).value(i) - RValue);
                    }
                }

                for (int j = 0; j < MValue.length; j++) {
                    if (j == RLable) {
                        continue;
                    }
                    MValue[j] = MValue[j] * (double) numInstancesForClass[j] / (double) (instances.numInstances() - numInstancesForClass[RLable]);
                    MValue[j] = MValue[j] / (MAX[i] - MIN[i]);//��һ��
                }

                HValue = HValue / (double) M;
                for (int j = 0; j < MValue.length; j++) {
                    MValue[j] = MValue[j] / (double) M;
                    attributeIndexs[i].value -= MValue[j];
                }
                attributeIndexs[i].value += HValue;
            }
        }

        Arrays.sort(attributeIndexs);

        return attributeIndexs;
    }

    @Override
    public int[] getFirstKAttributes(Instances instances, int K) {
        long start = System.currentTimeMillis();
        int[] numInstancesForClass = new int[instances.numClasses()];
        for (int i = 0; i < numInstancesForClass.length; i++) {
            numInstancesForClass[i] = 0;
        }

        for (int i = 0; i < instances.numInstances(); i++) {
            int label = (int) instances.instance(i).classValue();
            numInstancesForClass[label]++;
        }

        int MIN = Integer.MAX_VALUE;
        for (int i = 0; i < numInstancesForClass.length; i++) {
            MIN = Math.min(MIN, numInstancesForClass[i]);
        }

        //AttributeIndex[] attributeIndexs = Relief.GetAttributeWeight(instances, false, instances.numInstances(), MIN/2);

        AttributeIndex[] attributeIndexs = Relief.getAttributeWeight(instances, false, instances.numInstances(), (int) (MIN / 3.0 + 1.0));

        int[] index = new int[K];
        for (int i = 0; i < index.length; i++) {
            index[i] = attributeIndexs[i].index;
        }
        return index;
    }

    public static void main(String[] args) {
        Instances instances = new ArffFileReader("D:" + File.separator + "Data1.arff").getDataSet();
        Relief v = new Relief();
        v.getFirstKAttributes(instances, 19);

    }
}
