
package datamining.attributeselection;

import datamining.core.Attribute;
import datamining.core.AttributeIndex;
import datamining.core.Instances;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 *基于序列随机性的特征选择方法.<br/>通过计算单个属性的混乱程度即随机性进行特征选择.
 * @author lenovo
 * @param LRNoise 该变量没有在该算法中发挥作用
 */
public class DecisionEntropy implements IAttributeSelection {

    int LRNoise;
    Instances instances;
    AttributeIndex[] attributeIndexs;

    public DecisionEntropy() {
    }

    public DecisionEntropy(Instances instances) {
        this.instances = instances;
    }

    public Instances reduceInstances(int K) {
        boolean[] isNoise = new boolean[instances.numInstances()];
        for (int i = 0; i < isNoise.length; i++) {
            isNoise[i] = false;
        }
        boolean[] tmp = null;
        for (int i = 0; i < K; i++) {
            tmp = attributeIndexs[i].getAttrNoise();
            for (int j = 0; j < tmp.length; j++) {
                if (tmp[j] == true) {
                    isNoise[j] = true;
                }
            }
        }

//		FastVector afterAttr = new FastVector();
//		for (int i = 0; i < instances.numAttributes() - 1; i++) {
//			afterAttr.addElement(instances.attribute(i));
//		}afterAttr.addElement(instances.classAttribute());
        List<Attribute> afterAttr = new ArrayList<>();
        afterAttr = instances.getAttributes();


        Instances AfterInstances;
        AfterInstances = new Instances("after", afterAttr, instances.numInstances());
        AfterInstances.setClassIndex(instances.numAttributes() - 1);

        for (int i = 0; i < instances.numInstances(); i++) {
            if (isNoise[i] == false) { 
                AfterInstances.addInstance(instances.instance(i));
            }
        }
        return AfterInstances;
    }

//    public int[] getFirstKAttributes(int K) {
//
//        int[] numInstancesForClass = new int[instances.numClasses()];
//        for (int i = 0; i < numInstancesForClass.length; i++) {
//            numInstancesForClass[i] = 0;
//        }
//
//        for (int i = 0; i < instances.numInstances(); i++) {
//            int label = (int) instances.instance(i).classValue();
//            numInstancesForClass[label]++;
//        }
//
//        int MIN = Integer.MAX_VALUE;
//        for (int i = 0; i < numInstancesForClass.length; i++) {
//            MIN = Math.min(MIN, numInstancesForClass[i]);
//        }
//
//        attributeIndexs = DecisionEntropy.getAttributeWeight(instances, 0);
//        int[] index = new int[K];
//        for (int i = 0; i < index.length; i++) {
//
//            index[i] = attributeIndexs[i].index;
//        }
//        return index;
//    }

//    public static int[] AutoGetFirstKAttributes(Instances instances, double rate) throws IOException {
//
//        int[] decValue = new int[instances.numInstances()];
//
//        for (int j = 0; j < instances.numInstances(); j++) {
//            decValue[j] = (int) instances.instance(j).classValue();
//        }
//
//        int times = 10000;
//        double[] rdValue = new double[times];
//        int index = 0;
//
//        FileWriter fileWriter = new FileWriter("rdValue//" + instances.relationName() + ".txt");
//
//        int tmp = (int) (times * rate);
//
//        while (times-- != 0) {
//            decValue = Shuffle(decValue);
//            double value = calcSeqValue(decValue, instances.numClasses());
//            rdValue[index] = value;
//            fileWriter.write(value + "\n");
//            index++;
//        }
//
//
//        fileWriter.close();
//
//        fileWriter = new FileWriter("pValue//" + instances.relationName() + ".txt");
//
//        Arrays.sort(rdValue);
//
//
//
//        double pValue = rdValue[tmp];
//
//        //	System.out.println(tmp+" "+ pValue);
//
//        AttributeIndex[] attributeIndexs = new AttributeIndex[instances.numAttributes() - 1];
//
//
//        for (int i = 0; i < instances.numAttributes() - 1; i++) {
//
//            instances.sort(i);
//
//            for (int j = 0; j < instances.numInstances(); j++) {
//                decValue[j] = (int) instances.instance(j).classValue();
//            }
//            double value = calcSeqValue(decValue, instances.numClasses());
//            fileWriter.write(value + "\n");
//
//            if (value < 0) {
//                double tmptmp = calcSeqValue(decValue, instances.numClasses());
//                System.err.println(tmptmp);
//            }
//
//            attributeIndexs[i] = new AttributeIndex();
//            attributeIndexs[i].index = i;
//            attributeIndexs[i].value = value;
//        }
//        fileWriter.close();
//
//
//        AttributeIndex.order = 0;
//
//        Arrays.sort(attributeIndexs);
//
//        Vector<Integer> vecIntegers = new Vector<Integer>();
//
//        for (int i = 0; i < attributeIndexs.length; i++) {
//
//            if (attributeIndexs[i].value < pValue) {
//                vecIntegers.add(attributeIndexs[i].index);
//            }
//        }
//
//        int[] res = new int[vecIntegers.size()];
//        for (int i = 0; i < res.length; i++) {
//            res[i] = vecIntegers.get(i);
//        }
//
//
//        return res;
//
//    }
    /**
     * 获取前k个属性
     * 
     * @return index 前k个属性的索引序号数组
     */
    @Override
    public int[] getFirstKAttributes(Instances instances, int K) {

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

        // AttributeIndex[] attributeIndexs =
        // DecisionEntropy.GetAttributeWeight(instances,
        // instances.numInstances()/instances.numClasses());

        AttributeIndex[] attributeIndexs = DecisionEntropy.getAttributeWeight(
                instances, 0);

        int[] index = new int[(K <= attributeIndexs.length) ? K : attributeIndexs.length];
        for (int i = 0; i < index.length; i++) {
            index[i] = attributeIndexs[i].index;
        }
        return index;
    }
    
    /**
     * 返回指定实例中包含的属性的权重
     * 
     * @return attattributeIndexs 
     */
    public static AttributeIndex[] getAttributeWeight(Instances instances,
            int LRNoise) {

        int numClass = instances.numClasses();
        int numAttribute = instances.numAttributes() - 1;
        int numInstance = instances.numInstances();


        int[] decValue = new int[numInstance];
        AttributeIndex[] attributeIndexs = new AttributeIndex[numAttribute];

        for (int i = 0; i < instances.numAttributes() - 1; i++) {

            instances.sort(i);

            for (int j = 0; j < instances.numInstances(); j++) {
                decValue[j] = (int) instances.instance(j).classValue();
            }

            double value = calcSeqValue(decValue, numClass);


            attributeIndexs[i] = new AttributeIndex();
            attributeIndexs[i].index = i;
            attributeIndexs[i].value = value;
        }

        AttributeIndex.order = 0;

        Arrays.sort(attributeIndexs);
        for (int i = 0; i < attributeIndexs.length; i++) {
            attributeIndexs[i].value = attributeIndexs[i].value
                    / attributeIndexs[attributeIndexs.length - 1].value;
        }
        return attributeIndexs;
    }

    /**
     * 返回v的H(v)混乱度的单次运算 
     */
    public static double LOG(double v) {
        return v * Math.log(v) / Math.log(2);
    }

    //返回打乱顺序的数组value
    public static int[] Shuffle(int[] value) {
        Random random = new Random(System.currentTimeMillis());
        int numInstances = value.length;
        for (int i = 0; i < numInstances; i++) {
            int j = random.nextInt(numInstances - i);
            j += i;
            int tmp = value[i];
            value[i] = value[j];
            value[j] = tmp;
        }
        return value;
    }

    //返回seqValue的混乱度
    public static double calcSeqValue(int[] seqValue, int num) {
        int[] numForClass = new int[num];

        for (int i = 0; i < seqValue.length; i++) {
            numForClass[seqValue[i]]++;
        }

        double p = 1;
        double q = 0;
        double res = 0;

        for (int i = 1; i < seqValue.length; i++) {

            if (seqValue[i] != seqValue[i - 1]) {
                q = numForClass[seqValue[i - 1]];
                res -= LOG(p / q);
                p = 1;
            } else {
                p++;
            }
        }
        q = numForClass[seqValue[seqValue.length - 1]];
        res -= LOG(p / q);
        return res;

    }
}
