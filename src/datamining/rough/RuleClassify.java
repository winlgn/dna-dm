/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package datamining.rough;

import datamining.attributeselection.IAttributeSelection;
import datamining.classify.Classify;
import datamining.core.Attribute;
import datamining.core.Instance;
import datamining.core.Instances;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author lenovo
 */
public class RuleClassify implements Classify {

    int numClass;
    Instances instancesRule;

    public void buildClassify(Instances instances) throws Exception {
//        值约简
//        instances = ValueReduction.GetReducedInstancesByDivide(instances);
//        ValueReductionByDecisionMatrix valueReductionByDivide = new ValueReductionByDecisionMatrix();
//        instances = valueReductionByDivide.GetReducedInstances(instances);
        // instances =
        // ValueReduction.GetReducedInstancesByDecisionMatrix(instances);
        // System.out.println(instances);

        this.instancesRule = instances;
        this.numClass = instances.numClasses();
    }

    private boolean InstanceCompare(Instance instance1, Instance instance2) {
        for (int i = 0; i < instance1.numAttributes() - 1; i++) {
            if (instance1.isMissing(i) == true) {
                continue;
            } else if (instance2.isMissing(i) == true) {
                continue;
            } else if (instance1.value(i) == instance2.value(i)) {
                continue;
            } else {
                return false;
            }

        }
        return true;
    }

    private double ClassifyInstance(Instance instance) throws Exception {
        double[] classVote = new double[numClass];
        Arrays.fill(classVote, 0);
        for (int i = 0; i < instancesRule.numInstances(); i++) {

            if (InstanceCompare(instancesRule.instance(i), instance) == true) {
                classVote[(int) instancesRule.instance(i).classValue()]++;
            }
        }

        int res = 0;
        double value = -Double.MAX_VALUE;
        for (int i = 0; i < numClass; i++) {
            if (classVote[i] > value) {
                value = classVote[i];
                res = i;
            }
        }

        if (value == 0) {
            System.out.println("拒识");
            double[] numVote = new double[numClass];
            Arrays.fill(classVote, 0);
            Arrays.fill(numVote, 0);
            for (int i = 0; i < instancesRule.numInstances(); i++) {
                classVote[(int) instancesRule.instance(i).classValue()] += InstanceSimilarity(
                        instancesRule.instance(i), instance);
                numVote[(int) instancesRule.instance(i).classValue()]++;
            }

            for (int i = 0; i < numVote.length; i++) {
                if (numVote[i] != 0) {
                    classVote[i] = classVote[i] / numVote[i];
                }
            }

            value = -Double.MAX_VALUE;
            for (int i = 0; i < numClass; i++) {
                if (classVote[i] > value) {
                    value = classVote[i];
                    res = i;
                }
            }
        }

        return res;
    }

    private double InstanceSimilarity(Instance instance1, Instance instance2) {
        double a = 0;
        double b = instance1.numAttributes() - 1;
        for (int i = 0; i < instance1.numAttributes() - 1; i++) {
            if (instance1.isMissing(i) == true) {
                continue;
            } else if (instance2.isMissing(i) == true) {
                continue;
            } else if (instance1.value(i) == instance2.value(i)) {
                continue;
            } else {
                return a++;
            }

        }
        return (b - a) / b;
    }

    public Instances PreprocessInstances(Instances instances) throws Exception {

        // 离散化

        InstancesBreakPoint instancesBreakPoint;
        GetBreakPointsByImportance_QDA qda = new GetBreakPointsByImportance_QDA();
        AttributeReduction attributeReduction = new AttributeReduction();

        // 基于动态聚类的两步离散化算法

        // 动态聚类
        instancesBreakPoint = qda.FirstStep(instances);
        //
        // //添加断点
        instancesBreakPoint = qda.SecondStep(true,
                instancesBreakPoint, instances);
        //
        //
        // //得到新的instances
        instances = qda.getDiscretizationInstances(instances,
                instancesBreakPoint);

        // 再次离散化
        DiscreteBasedOnImportance di = new DiscreteBasedOnImportance();
        instancesBreakPoint = di.discrete(instances);
        // instancesBreakPoint =
        // Discretization.DiscreteBasedOnEntropy_ByLCJ(instances);

        instances = di.getDiscretizationInstances(instances,
                instancesBreakPoint);

        // 基于断点重要性的离散化算法
        // instancesBreakPoint =
        // Discretization.DiscreteBasedOnImportance_ByLH(instances);
        // instancesBreakPoint.ShowBreakPoint();

        // instancesBreakPoint =
        // Discretization.DiscreteBasedOnImportance_ByLCJ(instances);
        // instancesBreakPoint.ShowBreakPoint();

        // 基于信息熵的离散化算法
        // instancesBreakPoint =
        // Discretization.DiscreteBasedOnEntropy_ByLH(instances);
        // instancesBreakPoint.ShowBreakPoint();

        // instancesBreakPoint =
        // Discretization.DiscreteBasedOnEntropy_ByLCJ(instances);

        // 得到离散化后的决策表
        // instances = Discretization.GetDiscretizationInstances(instances,
        // instancesBreakPoint);
        // bSystem.out.println(instances);

        // 属性约简
        int[] indexs = attributeReduction.getReducedAttributeByAttributeOrder(instances);

        instances = Util.ReduceInstances(instances, indexs);

        return instances;
    }

    @Override
    public double crossTest(Instances instances, int fold, int times) throws Exception {

        // 预先处理
        instances = PreprocessInstances(instances);

        Random random = new Random();
        int crossV = fold;
        int numInstances = instances.numInstances();

        // for (int i = 0; i < numInstances; i++) {
        // int r = random.nextInt(numInstances - i);
        // Instance tmpInstance = instances.instance(r);
        // instances.delete(r);
        // instances.add(tmpInstance);
        // }
//
//        FastVector afterAttr = new FastVector();
//        for (int j = 0; j < instances.numAttributes(); j++) {
//            afterAttr.addElement(instances.attribute(j));
//        }
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

                buildClassify(trainInstances);

                for (int k = 0; k < testInstances.numInstances(); k++) {
                    int rightClass = (int) testInstances.instance(k).classValue();
                    classAmount[rightClass]++;
                    int resultClass = (int) (ClassifyInstance(testInstances.instance(k)));

                    if (rightClass == resultClass) {
                        rightCount[rightClass]++;
                    }
                }
            }
        }
        int tmpright = 0;
        int tmptotal = 0;
        for (int j = 0; j < instances.numClasses(); j++) {
            tmpright += rightCount[j];
            tmptotal += classAmount[j];
        }
        return tmpright * 1.0 / tmptotal;
    }

    @Override
    public double evalScore(Instances instances) {
        double cnt = 0;
        for (int i = 0; i < instances.numInstances(); i++) {
            try {
                double a = instances.instance(i).classValue();
                double b = ClassifyInstance(instances.instance(i));
                if (a == b) {
                    cnt++;
                }
            } catch (Exception ex) {
                Logger.getLogger(RuleClassify.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return cnt / instances.numInstances();
    }

    @Override
    public Properties getprProperties() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean hasProperties() {
//        throw new UnsupportedOperationException("Not supported yet.");
        return false;
    }

    @Override
    public void setProperties(Properties prop) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public double crossTest(Instances instances, int fold, int times, IAttributeSelection attributeSelector) throws Exception {

        // 预先处理
        instances = PreprocessInstances(instances);

        Random random = new Random();
        int crossV = fold;
        int numInstances = instances.numInstances();

        // for (int i = 0; i < numInstances; i++) {
        // int r = random.nextInt(numInstances - i);
        // Instance tmpInstance = instances.instance(r);
        // instances.delete(r);
        // instances.add(tmpInstance);
        // }
//
//        FastVector afterAttr = new FastVector();
//        for (int j = 0; j < instances.numAttributes(); j++) {
//            afterAttr.addElement(instances.attribute(j));
//        }
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
                int[] indexs = attributeSelector.getFirstKAttributes(trainInstances, instances.numAttributes() - 1);

                trainInstances = Util.reduceInstancesButClassAttribute(trainInstances, indexs);
                testInstances = Util.reduceInstancesButClassAttribute(testInstances, indexs);
                buildClassify(trainInstances);

                for (int k = 0; k < testInstances.numInstances(); k++) {
                    int rightClass = (int) testInstances.instance(k).classValue();
                    classAmount[rightClass]++;
                    int resultClass = (int) (ClassifyInstance(testInstances.instance(k)));

                    if (rightClass == resultClass) {
                        rightCount[rightClass]++;
                    }
                }
            }
        }
        int tmpright = 0;
        int tmptotal = 0;
        for (int j = 0; j < instances.numClasses(); j++) {
            tmpright += rightCount[j];
            tmptotal += classAmount[j];
        }
        return tmpright * 1.0 / tmptotal;
    }
}
