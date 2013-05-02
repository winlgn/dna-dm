package datamining.classify.ModifiedDLDA;

import datamining.attributeselection.IAttributeSelection;
import datamining.classify.Classify;
import datamining.core.Attribute;
import datamining.core.Instance;
import datamining.core.Instances;
import datamining.rough.Util;
import datamining.util.ArffFileReader;
import java.io.File;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ModifiedDLDAClassify分类器
 *
 * @author lenovo
 */
public class ModifiedDLDAClassify implements Classify {

    public ModifiedDLDAClassify() {
    }
    Instances instances;
    int numClass;
    int numAttribute;
    int numInstance;
    int[] groupIndex;
    int[] groupClass;
    double[][] StdValue;
    double[][] MeanValue;
    double[] numForClass;
    double[] AllStdValue;
    double meanStdValue;
    int[] posClass;
    double s0;

    public static void GroupClasses(Instances instances, int[] index, int[] offset) {
        int[] bucket = new int[instances.numClasses()];
        int[] tmp = new int[instances.numInstances()];
        Arrays.fill(bucket, 0);

        for (int i = 0; i < instances.numInstances(); i++) {
            bucket[(int) instances.instance(i).classValue()]++;
        }

        for (int i = 1; i < instances.numClasses(); i++) {
            bucket[i] = bucket[i - 1] + bucket[i];
        }

        for (int i = 0; i < instances.numClasses(); i++) {
            offset[i] = bucket[i];
        }

        for (int i = instances.numInstances() - 1; i >= 0; i--) {
            tmp[i] = --bucket[(int) instances.instance(i).classValue()];
        }

        for (int i = 0; i < instances.numInstances(); i++) {
            index[tmp[i]] = i;
        }
    }

    public static double CalcStd(double[][] value, int index) {
        double mean = 0;
        for (int i = 0; i < value.length; i++) {
            mean += value[i][index];
        }
        mean = mean / value.length;
        double res = 0;
        for (int i = 0; i < value.length; i++) {
            res += (value[i][index] - mean) * (value[i][index] - mean);
        }
        return res;
    }

    public static double CalcMean(double[][] value, int index) {
        double mean = 0;
        for (int i = 0; i < value.length; i++) {
            mean += value[i][index];
        }
        mean = mean / value.length;
        return mean;
    }

    public void buildClassifier(Instances instances) {
        this.instances = instances;
        numClass = instances.numClasses();
        numInstance = instances.numInstances();
        numAttribute = instances.numAttributes() - 1;
        StdValue = new double[numAttribute][numClass];
        MeanValue = new double[numAttribute][numClass];
        AllStdValue = new double[numAttribute];
        numForClass = new double[numClass];
        posClass = new int[numClass];
        int seg = 0;
        int cnt = 1;
        instances.sort(instances.classAttribute());
        for (int i = 1; i < numInstance; i++) {
            if (instances.instance(i).classValue() != instances.instance(i - 1).classValue()) {
                numForClass[seg] = cnt;
                posClass[seg++] = i;
                cnt = 1;
            } else {
                cnt++;
            }
        }
        numForClass[seg] = cnt;
        posClass[seg++] = numInstance;
        double total = 0;
        for (int i = 0; i < numAttribute; i++) {
            total = 0;
            int b = 0;
            int e = 0;
            total = 0;
            for (int j = 0; j < posClass.length; j++) {
                b = e;
                e = posClass[j];
                for (int k = b; k < e; k++) {
                    MeanValue[i][j] += instances.instance(k).value(i);
                }
                total += MeanValue[i][j];
                MeanValue[i][j] = MeanValue[i][j] / numForClass[j];
                for (int k = b; k < e; k++) {
                    StdValue[i][j] += Math.pow((instances.instance(k).value(i) - MeanValue[i][j]), 2);
                }
                StdValue[i][j] = StdValue[i][j] / (e - b);
                s0 += StdValue[i][j];
            }
        }
        s0 = s0 / numAttribute;
        for (int i = 0; i < numAttribute; i++) {
            AllStdValue[i] = 0;
            for (int j = 0; j < numClass; j++) {
                AllStdValue[i] = AllStdValue[i] + StdValue[i][j];
            }
        }
    }

    public int classifyInstance(Instance instance) {
        double[] classVote = new double[numClass];
        for (int i = 0; i < numClass; i++) {
            classVote[i] = 0;
            for (int j = 0; j < numAttribute; j++) {
                classVote[i] += Math.pow((instance.value(j) - MeanValue[j][i]), 2) / (AllStdValue[j]);
            }
            //	classVote[i] -= Math.log(numForClass[i]/numInstance);
        }
        int res = 0;
        double value = Double.MAX_VALUE;
        for (int i = 0; i < numClass; i++) {
            if (classVote[i] < value) {
                value = classVote[i];
                res = i;
            }
        }
        return res;
    }

    @Override
    public double crossTest(Instances instances, int fold, int times) throws Exception {
        Random random = new Random();
        int crossV = fold;
        int numInstances = instances.numInstances();

        double result = 0;
        int cnt = 0;

        List<Attribute> afterAttr = new ArrayList<>();
        afterAttr = instances.getAttributes();

        int[] rightCount = new int[instances.numClasses()];
        int[] classAmount = new int[instances.numClasses()];
        for (int k = 0; k < instances.numClasses(); k++) {
            rightCount[k] = 0;
            classAmount[k] = 0;
        }

        int[] classes = new int[crossV];
        for (int j = 0; j < crossV; j++) {
            classes[j] = instances.numInstances() / crossV * j;
            if (instances.numInstances() % crossV > j) {
                classes[j] += j;
            } else {
                classes[j] += instances.numInstances() % crossV;
            }
        }

        for (; times > 0; times--) {
            int[] index = new int[instances.numInstances()];
            for (int j = 0; j < instances.numInstances(); j++) {
                index[j] = j;
            }
            for (int j = 0; j < numInstances; j++) {
                int seed = j + random.nextInt(numInstances - j);
                int tmp = index[j];
                index[j] = index[seed];
                index[seed] = tmp;
            }

//怎样选择测试集和训练集��		
            for (int j = 0; j < crossV; j++) {
                Instances trainInstances, testInstances;

                int end = classes[(j - 1 + crossV) % crossV];
                trainInstances = new Instances("train", afterAttr, end
                        - classes[j]);
                trainInstances.setClassIndex(instances.numAttributes() - 1);
                for (int k = classes[j]; k < end; k++) {
                    int tmpk = index[k % instances.numInstances()];
                    trainInstances.addInstance(instances.instance(tmpk));
                }
                classes[j] += instances.numInstances();
                testInstances = new Instances("test", afterAttr, classes[j]
                        - end);
                testInstances.setClassIndex(instances.numAttributes() - 1);
                for (int k = end; k < classes[j]; k++) {
                    int tmpk = index[k % instances.numInstances()];
                    testInstances.addInstance(instances.instance(tmpk));
                }



                buildClassifier(trainInstances);

                double tmptmp = evalScore(testInstances);

                result += tmptmp;

                cnt++;

            }
        }
        return result / cnt;
    }

//    @Override
//    public double[] classifyInstances(Instances instances) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
    @Override
    public double evalScore(Instances instances) {
        double cnt = 0;
        for (int i = 0; i < instances.numInstances(); i++) {
            double a = instances.instance(i).classValue();
            double b = classifyInstance(instances.instance(i));
            if (a == b) {
                cnt++;
            }
            //	System.out.println(a+" "+b);
        }
        return cnt / instances.numInstances();
    }

    @Override
    public Properties getprProperties() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setProperties(Properties prop) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public double crossTest(Instances instances, int fold, int times, IAttributeSelection attributeSelector) throws Exception {
        Random random = new Random();
        int crossV = fold;
        int numInstances = instances.numInstances();

        double result = 0;
        int cnt = 0;

        List<Attribute> afterAttr = new ArrayList<>();
        afterAttr = instances.getAttributes();

        int[] rightCount = new int[instances.numClasses()];
        int[] classAmount = new int[instances.numClasses()];
        for (int k = 0; k < instances.numClasses(); k++) {
            rightCount[k] = 0;
            classAmount[k] = 0;
        }

        int[] classes = new int[crossV];
        for (int j = 0; j < crossV; j++) {
            classes[j] = instances.numInstances() / crossV * j;
            if (instances.numInstances() % crossV > j) {
                classes[j] += j;
            } else {
                classes[j] += instances.numInstances() % crossV;
            }
        }

        for (; times > 0; times--) {
            int[] index = new int[instances.numInstances()];
            for (int j = 0; j < instances.numInstances(); j++) {
                index[j] = j;
            }
            for (int j = 0; j < numInstances; j++) {
                int seed = j + random.nextInt(numInstances - j);
                int tmp = index[j];
                index[j] = index[seed];
                index[seed] = tmp;
            }

//怎样选择测试集和训练集		
            for (int j = 0; j < crossV; j++) {
                Instances trainInstances, testInstances;

                int end = classes[(j - 1 + crossV) % crossV];
                trainInstances = new Instances("train", afterAttr, end
                        - classes[j]);
                trainInstances.setClassIndex(instances.numAttributes() - 1);
                for (int k = classes[j]; k < end; k++) {
                    int tmpk = index[k % instances.numInstances()];
                    trainInstances.addInstance(instances.instance(tmpk));
                }
                classes[j] += instances.numInstances();
                testInstances = new Instances("test", afterAttr, classes[j]
                        - end);
                testInstances.setClassIndex(instances.numAttributes() - 1);
                for (int k = end; k < classes[j]; k++) {
                    int tmpk = index[k % instances.numInstances()];
                    testInstances.addInstance(instances.instance(tmpk));
                }
                int[] indexs = attributeSelector.getFirstKAttributes(trainInstances, instances.numAttributes() - 1);
                trainInstances = Util.reduceInstancesButClassAttribute(trainInstances, indexs);
                testInstances = Util.reduceInstancesButClassAttribute(testInstances, indexs);

                buildClassifier(trainInstances);

                double tmptmp = evalScore(testInstances);

                result += tmptmp;

                cnt++;

            }
        }
        return result / cnt;
    }

    public static void main(String[] args) {
        try {
            File f = new File("D:" + File.separator + "data5.arff");
            ArffFileReader reader = new ArffFileReader(f);
            Instances inst = reader.getDataSet();
            ModifiedDLDAClassify sv = new ModifiedDLDAClassify();
            System.out.println(sv.crossTest(inst, 10, 2));
            System.out.println("result=" + sv.evalScore(inst));
        } catch (Exception ex) {
            Logger.getLogger(ModifiedDLDAClassify.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public boolean hasProperties() {
        return false;
    }
//    @Override
//    public double crossTest(Instances instances, int fold, int times, IAttributeSelection attributeSelector, int K) throws Exception {
//        if (K <= 0 || K > instances.numAttributes() - 1) {
//            throw new Exception("K值不规范");
//        }
//        Random random = new Random();
//        int crossV = fold;
//        int numInstances = instances.numInstances();
//
//        double result = 0;
//        int cnt = 0;
//
//        List<Attribute> afterAttr = new ArrayList<>();
//        afterAttr = instances.getAttributes();
//
//        int[] rightCount = new int[instances.numClasses()];
//        int[] classAmount = new int[instances.numClasses()];
//        for (int k = 0; k < instances.numClasses(); k++) {
//            rightCount[k] = 0;
//            classAmount[k] = 0;
//        }
//
//        int[] classes = new int[crossV];
//        for (int j = 0; j < crossV; j++) {
//            classes[j] = instances.numInstances() / crossV * j;
//            if (instances.numInstances() % crossV > j) {
//                classes[j] += j;
//            } else {
//                classes[j] += instances.numInstances() % crossV;
//            }
//        }
//
//        for (; times > 0; times--) {
//            int[] index = new int[instances.numInstances()];
//            for (int j = 0; j < instances.numInstances(); j++) {
//                index[j] = j;
//            }
//            for (int j = 0; j < numInstances; j++) {
//                int seed = j + random.nextInt(numInstances - j);
//                int tmp = index[j];
//                index[j] = index[seed];
//                index[seed] = tmp;
//            }
//
////怎样选择测试集和训练集		
//            for (int j = 0; j < crossV; j++) {
//                Instances trainInstances, testInstances;
//
//                int end = classes[(j - 1 + crossV) % crossV];
//                trainInstances = new Instances("train", afterAttr, end
//                        - classes[j]);
//                trainInstances.setClassIndex(instances.numAttributes() - 1);
//                for (int k = classes[j]; k < end; k++) {
//                    int tmpk = index[k % instances.numInstances()];
//                    trainInstances.addInstance(instances.instance(tmpk));
//                }
//                classes[j] += instances.numInstances();
//                testInstances = new Instances("test", afterAttr, classes[j]
//                        - end);
//                testInstances.setClassIndex(instances.numAttributes() - 1);
//                for (int k = end; k < classes[j]; k++) {
//                    int tmpk = index[k % instances.numInstances()];
//                    testInstances.addInstance(instances.instance(tmpk));
//                }
//
//                int[] indexs = attributeSelector.getFirstKAttributes(trainInstances, K);
//                trainInstances = Util.reduceInstancesButClassAttribute(trainInstances, indexs);
//                testInstances = Util.reduceInstancesButClassAttribute(testInstances, indexs);
//
//                buildClassifier(trainInstances);
//
//                double tmptmp = evalScore(testInstances);
//
//                result += tmptmp;
//
//                cnt++;
//
//            }
//        }
//        return result / cnt;
//    }
}
