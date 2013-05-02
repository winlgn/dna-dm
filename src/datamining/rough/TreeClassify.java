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
public class TreeClassify implements Classify {

    private int length;
    private Vector<NODE> vecNode;
    private int vecIndex;
    private Instances instances;
    private NODE root;
    private double[] classVote;

//    private double[] rule;
//    static private PrintStream curOut;
//    private int total;
    class NODE {

        NODE brother;
        NODE child;
        double value;
        double total;

        public NODE() {
            brother = null;
            child = null;
            value = 0;
            total = 0;
        }
    }

    private NODE GetNode() {
        if (vecIndex < vecNode.size()) {
            return vecNode.get(vecIndex++);
        } else {
            vecNode.add(new NODE());
            return vecNode.get(vecIndex++);
        }
    }

    public TreeClassify() {
        vecNode = new Vector<NODE>();
    }

    public void InsertInstance(Instance instance, NODE parent, int index) {
        NODE child = parent.child;
        if (child == null) //孩子不存在，直接添加
        {
            if (index == length) //到达叶子节点，加入决策值，并返回
            {
//				child = new NODE();
                child = GetNode();
                child.brother = child.child = null;
                child.value = child.total = 0;

                child.value = instance.value(index); //决策值，必须把决策值放在最后一个属性，此时与instance.classValue()相同
                child.total = 1;
                parent.child = child;
            } //没有到达叶子节点，加入条件值，并递归
            else {
//				child = new NODE();
                child = GetNode();
                child.brother = child.child = null;
                child.value = child.total = 0;

                child.value = instance.value(index); //条件值
                parent.child = child;
                InsertInstance(instance, child, index + 1);
            }
        } //孩子节点存在
        else {
            NODE pLink = child;
            //寻找是否存在当前值\
            //特别注意需要用Douele.compare，因为涉及到NaN
            while (pLink != null && (Double.compare(pLink.value, instance.value(index)) != 0)) {
                pLink = pLink.brother;
            }

            //如果当前值不存在，则需要添加，并且继续递归
            if (pLink == null) {
//				pLink = new NODE();
                pLink = GetNode();
                pLink.brother = pLink.child = null;
                pLink.value = pLink.total = 0;

                pLink.value = instance.value(index);
                pLink.brother = child;
                parent.child = pLink;
                if (index == length) //到达叶子节点，加入决策值，并返回
                {
                    pLink.total = 1;
                    return;
                } else {
                    InsertInstance(instance, pLink, index + 1);
                }
            } //如果当前值存在
            else {
                if (index == length) //到达叶子节点，加入决策值，并返回
                {
                    pLink.total++;
                    return;
                } else {
                    InsertInstance(instance, pLink, index + 1);
                }
            }
        }
    }

    private void visitTree(Instance instance, NODE root, int index) {
        NODE child = root.child;
        if (child == null) //孩子不存在，规则肯定无法匹配
        {
            return;
        }

        if (index == length) {
            while (child != null) {
                classVote[(int) child.value] += child.total;
                child = child.brother;
            }
        } //孩子节点存在
        else {
            NODE pLink = child;
            //寻找是否存在当前值\
            //特别注意需要用Douele.compare，因为涉及到NaN
            while (pLink != null) {
                if (instance.value(index) == pLink.value || (Double.isNaN(instance.value(index)) == true || Double.isNaN(pLink.value) == true)) {
                    visitTree(instance, pLink, index + 1);
                }
                pLink = pLink.brother;
            }
        }
    }

    public void buildClassify(Instances instances) throws Exception {
//        instances = ValueReduction.GetReducedInstancesByDivide(instances);
//        ValueReductionByDivide valueReductionByDivide = new ValueReductionByDivide();
//        instances = valueReductionByDivide.GetReducedInstances(instances);
        vecIndex = 0;

        this.instances = instances;
//		this.root = new NODE();

        this.root = GetNode();
        this.root.child = this.root.brother = null;
        this.root.value = this.root.total = 0;

        this.length = instances.numAttributes() - 1;
        this.classVote = new double[instances.numClasses()];

        for (int i = 0; i < instances.numInstances(); i++) {
            InsertInstance(instances.instance(i), root, 0);
        }
    }

    public double classifyInstance(Instance instance) {
        Arrays.fill(classVote, 0);

        visitTree(instance, root, 0);

        int res = 0;
        double value = -Double.MAX_VALUE;
        for (int i = 0; i < instances.numClasses(); i++) {
            if (classVote[i] > value) {
                value = classVote[i];
                res = i;
            }
        }

        if (value == -Double.MAX_VALUE) {
            System.out.println("拒识");
        }
        return res;
    }

    @Override
    public double crossTest(Instances instances, int fold, int times) throws Exception {
        Random random = new Random();
        int crossV = fold;
        int numInstances = instances.numInstances();



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

                //showRules();

                for (int k = 0; k < testInstances.numInstances(); k++) {
                    int rightClass = (int) testInstances.instance(k).classValue();
                    classAmount[rightClass]++;
                    int resultClass = (int) (classifyInstance(testInstances.instance(k)));

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
            double a = instances.instance(i).classValue();
            double b = classifyInstance(instances.instance(i));
            if (a == b) {
                cnt++;
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
        Random random = new Random();
        int crossV = fold;
        int numInstances = instances.numInstances();



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

                //showRules();

                for (int k = 0; k < testInstances.numInstances(); k++) {
                    int rightClass = (int) testInstances.instance(k).classValue();
                    classAmount[rightClass]++;
                    int resultClass = (int) (classifyInstance(testInstances.instance(k)));

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
