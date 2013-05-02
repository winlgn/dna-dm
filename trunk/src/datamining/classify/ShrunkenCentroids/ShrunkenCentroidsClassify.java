/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package datamining.classify.ShrunkenCentroids;

import datamining.attributeselection.IAttributeSelection;
import datamining.classify.Classify;
import datamining.core.Attribute;
import datamining.core.Instance;
import datamining.core.Instances;
import datamining.rough.Util;
import java.util.*;

/**
 * ShrunkenCentroids分类器
 *
 * @author lenovo
 */
public class ShrunkenCentroidsClassify implements Classify {

    public ShrunkenCentroidsClassify() {
    }
    Instances instances;
    int numClass;
    int numInstance;
    int numAttribute;
    double[][] dik;
    double[] mk;
    double[] xi;
    double[][] xik;
    double[] si;
    double[] numForClass;
    int[] posClass;
    double s0;
    double shreshold = 0;

    public void buildClassifier(Instances instances) {
        this.instances = instances;
        numClass = instances.numClasses();
        numInstance = instances.numInstances();
        numAttribute = instances.numAttributes() - 1;

        dik = new double[numAttribute][numClass];
        mk = new double[numClass];
        xi = new double[numAttribute];
        xik = new double[numAttribute][numClass];
        si = new double[numAttribute];

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

        for (int i = 0; i < numClass; i++) {
            mk[i] = Math.sqrt(1.0 / numForClass[i] - 1.0 / numInstance);
        }


        double total = 0;
        s0 = 0;
        for (int i = 0; i < numAttribute; i++) {
            total = 0;
            int b = 0;
            int e = 0;
            total = 0;
            for (int j = 0; j < posClass.length; j++) {
                b = e;
                e = posClass[j];

                for (int k = b; k < e; k++) {
                    xik[i][j] += instances.instance(k).value(i);
                }
                total += xik[i][j];
                xik[i][j] = xik[i][j] / numForClass[j];


                for (int k = b; k < e; k++) {
                    si[i] += Math.pow((instances.instance(k).value(i) - xik[i][j]), 2);
                }
            }


            xi[i] = total / numInstance;
            si[i] = Math.sqrt(si[i] / (numInstance - numClass));
            s0 += si[i];
        }

        s0 = s0 / numAttribute;


        for (int i = 0; i < numAttribute; i++) {

            for (int k = 0; k < numClass; k++) {
                dik[i][k] = (xik[i][k] - xi[i]) / (mk[k] * (si[i] + s0));
            }
        }


        for (int i = 0; i < numAttribute; i++) {

            for (int k = 0; k < numClass; k++) {
                if (Math.abs(dik[i][k]) - shreshold <= 0) {
                    dik[i][k] = 0;
                }
            }
        }

        for (int i = 0; i < numAttribute; i++) {

            for (int k = 0; k < numClass; k++) {
                xik[i][k] = xi[i] + mk[k] * (si[i] + s0) * dik[i][k];
            }
        }

    }

    public int classifyInstance(Instance instance) {
        double[] classVote = new double[numClass];
        for (int i = 0; i < numClass; i++) {
            classVote[i] = 0;
            for (int j = 0; j < numAttribute; j++) {
                classVote[i] += Math.pow((instance.value(j) - xik[j][i]), 2) / (si[j] + s0);
            }
            //	classVote[i] -= 2*Math.log(1.0/numForClass[i]);
            classVote[i] -= 2 * Math.log(numForClass[i] / numInstance);
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

                //		SetSoftShresholding(4.2);
                buildClassifier(trainInstances);

                result += classifyInstance(testInstances);
                cnt++;

            }
        }
        return result / cnt;
    }

    public double classifyInstance(Instances instances) {
        double cnt = 0;
        for (int i = 0; i < instances.numInstances(); i++) {
            if (instances.instance(i).classValue() == classifyInstance(instances.instance(i))) {
                cnt++;
            }
        }
        return cnt / instances.numInstances();
    }

    @Override
    public double evalScore(Instances instances) {
        double[] score = new double[instances.numClasses()];
        double[] num = new double[instances.numClasses()];
        Arrays.fill(score, 0);
        Arrays.fill(num, 0);
        ;
        for (int i = 0; i < instances.numInstances(); i++) {
            int tmp = (int) instances.instance(i).classValue();
            num[tmp]++;

            if (instances.instance(i).classValue() == classifyInstance(instances.instance(i))) {
                score[tmp]++;
            }
        }

        double res = 0;
        for (int i = 0; i < num.length; i++) {
            res += score[i] / num[i];
        }

        return res / instances.numClasses();
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

                //		SetSoftShresholding(4.2);
                int[] indexs = attributeSelector.getFirstKAttributes(trainInstances, instances.numAttributes() - 1);

                trainInstances = Util.reduceInstancesButClassAttribute(trainInstances, indexs);
                testInstances = Util.reduceInstancesButClassAttribute(testInstances, indexs);
                buildClassifier(trainInstances);

                result += classifyInstance(testInstances);
                cnt++;

            }
        }
        return result / cnt;
    }

    @Override
    public boolean hasProperties() {
        return false;
    }
//    @Override
//    public double crossTest(Instances instances, int fold, int times, IAttributeSelection attributeSelector, int K) throws Exception {
//        if (K <= 0 || K >= instances.numInstances()) {
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
//
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
//                //		SetSoftShresholding(4.2);
//                int[] indexs = attributeSelector.getFirstKAttributes(trainInstances, instances.numAttributes() - 1);
//
//                trainInstances = Util.reduceInstancesButClassAttribute(trainInstances, indexs);
//                testInstances = Util.reduceInstancesButClassAttribute(testInstances, indexs);
//                buildClassifier(trainInstances);
//
//                result += classifyInstance(testInstances);
//                cnt++;
//
//            }
//        }
//        return result / cnt;
//    }
}
