/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package datamining.rough;

import datamining.core.Attribute;
import datamining.core.Instance;
import datamining.core.Instances;
import java.util.ArrayList;
import java.util.List;

/**
 * 基于动态聚类的离散化算法
 *
 * @author lenovo
 */
public class GetBreakPointsByImportance_QDA implements Discretization {

    @Override
    public InstancesBreakPoint discrete(Instances instances) {
        InstancesBreakPoint instancesBreakPoint;
        instancesBreakPoint = FirstStep(instances);
        instancesBreakPoint = SecondStep(true, instancesBreakPoint, instances);
        return instancesBreakPoint;
    }

    @Override
    public Instances getDiscretizationInstances(Instances instances, InstancesBreakPoint instancesBreakPoint) {
        //得到属性的标号
        int[] indexAttributes = new int[instancesBreakPoint.numAttributeBreakPoints];
        for (int i = 0; i < indexAttributes.length; i++) {
            indexAttributes[i] = instancesBreakPoint.attributeBreakPoints[i].indexAttribute;
        }

        //新建一个离散化的决策表
        String name = "DiscretizationInstances";
        int numAttributes = indexAttributes.length + 1;
        int capacity = instances.numInstances();

//        FastVector attInfo = new FastVector(numAttributes);
//        for (int i = 0; i < numAttributes - 1; i++) {
//            attInfo.addElement(instances.attribute(indexAttributes[i]));
//        }
//        attInfo.addElement(instances.classAttribute());

        List<Attribute> attInfo = new ArrayList<>();
        for (int i = 0; i < numAttributes - 1; i++) {
            attInfo.add((Attribute) instances.getAttributes().get(indexAttributes[i]).clone());
        }
        attInfo.add((Attribute) instances.getAttributes().get(instances.classIndex()).clone());
//        List<Attribute> attInfo=new ArrayList<>();
//        attInfo=instances.getAttributes();

        Instances newInstances = new Instances(name, attInfo, capacity);
        newInstances.setClassIndex(newInstances.numAttributes() - 1);

        Instance instance;
        double[] data;

        for (int i = 0; i < instances.numInstances(); i++) {
            data = new double[numAttributes];

            for (int j = 0; j < newInstances.numAttributes() - 1; j++) {
                data[j] = instancesBreakPoint.attributeBreakPoints[j].GetDiscreteValue(instances.instance(i).value(indexAttributes[j]));
            }

            data[newInstances.numAttributes() - 1] = instances.instance(i).value(instances.numAttributes() - 1);
            instance = new Instance(1, data);
            instance.setDataSet(newInstances);
            newInstances.addInstance(instance);
        }

        return newInstances;
    }

    class EquivalentNode {

        int index;
        int nextDomain;
        int nextEntry;
        double classes;
    }

    public static double[] calcCluster(double[] impBreakPoints, double[] valueBreakPoints, int[] indexBreakPoints) {


        double e = 0;
        double average = 0;
        double total = 0;
        int num = impBreakPoints.length;
        for (int i = 0; i < num; i++) {
            total += impBreakPoints[i];
        }
        average = total / (double) num;
        total = 0;
        for (int i = 0; i < num; i++) {
            total += (impBreakPoints[i] - average)
                    * (impBreakPoints[i] - average);
        }
        e = Math.sqrt(total) / (double) num;
        int k = 0;
        double v = e + 1;


        while (true) {

            k++;

            double[] clusterCenter = new double[k];
            double[][] clusterDistance = new double[impBreakPoints.length][k];
            int[][] clusterContent = new int[k][impBreakPoints.length + 1];
            int _pos = 0;
            double _e = 0;
            double _e1 = 0;
            double _total = 0;
            double _average = 0;
            int _index = 0;
            int _num = 0;
            for (int i = 0; i < k; i++) {
                clusterCenter[i] = impBreakPoints[impBreakPoints.length / k / 2 + _pos];
                _pos += impBreakPoints.length / k;
            }


            _e = -1;

            boolean isSuccess = false;

            do {
                for (int i = 0; i < k; i++) {
                    clusterContent[i][0] = 0;
                }

                for (int i = 0; i < impBreakPoints.length; i++) {
                    double min = Double.MAX_VALUE;
                    _index = -1;
                    for (int j = 0; j < k; j++) {
                        clusterDistance[i][j] = Math.abs(impBreakPoints[i]
                                - clusterCenter[j]);
                        if (min > clusterDistance[i][j]) {
                            min = clusterDistance[i][j];
                            _index = j;
                        }
                    }

                    _pos = clusterContent[_index][0] + 1;
                    clusterContent[_index][_pos] = i;
                    clusterContent[_index][0]++;
                }
                _e1 = 0;
                for (int i = 0; i < k; i++) {
                    _total = 0;
                    _average = 0;
                    _num = clusterContent[i][0];

                    if (_num == 0) {
                        continue;
                    }

                    for (int j = 1; j <= _num; j++) {
                        _index = clusterContent[i][j];
                        _total += impBreakPoints[_index];
                    }
                    _average = _total / (double) _num;
                    clusterCenter[i] = _average;

                    _total = 0;
                    for (int j = 1; j < _num; j++) {
                        _index = clusterContent[i][j];
                        _total += (impBreakPoints[_index] - _average)
                                * (impBreakPoints[_index] - _average);
                    }

                    _e1 += Math.sqrt(_total / (double) _num);
                }
                _e1 = _e1 / (double) k;

                if (_e1 == _e) {
                    isSuccess = true;
                }

                _e = _e1;

            } while (isSuccess == false);

            v = _e;

            if (v <= e) {


                double[] _breakpoints = new double[k];
                int _numBreakPoints = 0;

                for (int i = 0; i < k; i++) {
                    double _value = Double.MAX_VALUE;
                    for (int j = 0; j < clusterContent[i][0]; j++) {

                        _index = clusterContent[i][j + 1];
                        _index = indexBreakPoints[_index];

                        if (_value > valueBreakPoints[_index]) {

                            _value = valueBreakPoints[_index];

                        }
                    }

                    if (_value == Double.MAX_VALUE) {
                        continue;
                    } else {
                        _breakpoints[_numBreakPoints++] = _value;
                    }

                }

                if (_numBreakPoints == k) {
                    return _breakpoints;
                } else {
                    double[] _newBreakPoints = new double[_numBreakPoints];

                    System.arraycopy(_breakpoints, 0, _newBreakPoints, 0, _numBreakPoints);

                    return _newBreakPoints;
                }

            }

        }

    }

    public static double[] getNumCluster(double[] impBreakPoints, double[] valueBreakPoins, int length) {

        double midValue = Double.MIN_NORMAL;
        int midIndex = -1;


        double[] leftImpBreakPoint;
        double[] rightImpBreakPoint;
        double[] newImpBreakPoint;
        for (int i = 0; i < length; i++) {
            if (impBreakPoints[i] >= midValue) {
                midValue = impBreakPoints[i];
                midIndex = i;
            }
        }
        for (int i = 0; i < length; i++) {
            impBreakPoints[i] = impBreakPoints[i] / midValue;
        }


        double[] _valueBreakPoints;
        if (midIndex == 0 || midIndex == length - 1) {
            newImpBreakPoint = new double[length];
            System.arraycopy(impBreakPoints, 0, newImpBreakPoint, 0, length);

            int[] indexBreakPoints = new int[length];
            for (int i = 0; i < indexBreakPoints.length; i++) {
                indexBreakPoints[i] = i;
            }

            _valueBreakPoints = calcCluster(newImpBreakPoint, valueBreakPoins, indexBreakPoints);
        } else {
            int num = midIndex + 1;
            leftImpBreakPoint = new double[num];
            System.arraycopy(impBreakPoints, 0, leftImpBreakPoint, 0, num);
            int[] leftIndexBreakPoints = new int[num];

            for (int i = 0; i < leftIndexBreakPoints.length; i++) {
                leftIndexBreakPoints[i] = i;
            }

            double[] _valueLeftBreakPoints = calcCluster(leftImpBreakPoint, valueBreakPoins, leftIndexBreakPoints);

            num = length - num;
            rightImpBreakPoint = new double[num];
            System.arraycopy(impBreakPoints, midIndex + 1, rightImpBreakPoint, 0, num);

            int[] rightIndexBreakPoints = new int[num];

            for (int i = 0; i < rightIndexBreakPoints.length; i++) {
                rightIndexBreakPoints[i] = leftIndexBreakPoints.length + i;
            }

            double[] _valueRightBreakPoints = calcCluster(rightImpBreakPoint, valueBreakPoins, rightIndexBreakPoints);


            _valueBreakPoints = new double[_valueLeftBreakPoints.length + _valueRightBreakPoints.length];

            System.arraycopy(_valueLeftBreakPoints, 0, _valueBreakPoints, 0, _valueLeftBreakPoints.length);

            System.arraycopy(_valueRightBreakPoints, 0, _valueBreakPoints, _valueLeftBreakPoints.length, _valueRightBreakPoints.length);

        }

        return _valueBreakPoints;

    }

    public InstancesBreakPoint FirstStep(Instances instances) {
        int numBreakPoints = instances.numInstances() - 1;
        int numClasses = instances.numClasses();

        int[][] numBreakInstances = new int[numClasses][numBreakPoints + 1];
        double[] impBreakPoints = new double[numBreakPoints];
        double[] valueBreakPoints = new double[numBreakPoints];

        InstancesBreakPoint instancesBreakPoint = new InstancesBreakPoint(instances.numAttributes() - 1);

        int numNewBreakPoints = 0;

        int tmpNumBreakPoint = 0;
        double[] tmpBreakPointValue = new double[instances.numInstances() + 1];

        for (int i = 0; i < instances.numAttributes() - 1; i++) {

            instances.sort(i);


            ///////////////// 如果断点个数小于tmpClusterBreakPoints就不聚类//////////////////////////////////
            int tmpClusterBreakPoints = 30;
            tmpNumBreakPoint = 0;
            for (int j = 1; j < instances.numInstances(); j++) {
                if (instances.instance(j - 1).value(i) != instances.instance(j).value(i)) {
                    tmpBreakPointValue[tmpNumBreakPoint] = (instances.instance(j - 1).value(i) + instances.instance(j).value(i)) / 2;
                    tmpNumBreakPoint++;
                }
            }
            if (tmpNumBreakPoint <= tmpClusterBreakPoints) {
                AttributeBreakPoint tmpAttributeBreakPoint = new AttributeBreakPoint(tmpNumBreakPoint, i);
                for (int j = 0; j < tmpNumBreakPoint; j++) {
                    tmpAttributeBreakPoint.breakPoints[j] = new BreakPoint();
                    tmpAttributeBreakPoint.breakPoints[j].value = tmpBreakPointValue[j];
                }
                instancesBreakPoint.attributeBreakPoints[i] = tmpAttributeBreakPoint;
                instancesBreakPoint.attributeBreakPoints[i].SortBreakPoint();
                continue;
            }
            ///////////////// 如果断点个数小于tmpClusterBreakPoints就不聚类//////////////////////////////////


            numNewBreakPoints = 0;

            for (int j = 0; j < numBreakInstances.length; j++) {
                numBreakInstances[j][0] = 0;
            }
            for (int j = 0; j < instances.numInstances(); j++) {

                for (int k = 0; k < numClasses; k++) {
                    if (j != 0) {
                        numBreakInstances[k][j] = numBreakInstances[k][j - 1];
                    }
                }
                int classIndex = (int) (instances.instance(j).classValue());

                numBreakInstances[classIndex][j]++;
            }

            double value = instances.instance(0).value(i);
            int index = 0;

            for (int j = 0; j < instances.numInstances(); j++) {


                if (value == instances.instance(j).value(i)) {
                    continue;
                }

                valueBreakPoints[numNewBreakPoints++] = (value + instances.instance(j).value(i)) / (double) 2;

                value = instances.instance(j).value(i);

                double lBreakPoint, rBreakPoint, lrMulProduct;
                lBreakPoint = rBreakPoint = lrMulProduct = 0;

                for (int k = 0; k < numClasses; k++) {
                    lBreakPoint += numBreakInstances[k][j - 1];
                    rBreakPoint += (numBreakInstances[k][numBreakPoints] - numBreakInstances[k][j - 1]);
                    lrMulProduct += (double) numBreakInstances[k][j - 1]
                            * (double) (numBreakInstances[k][numBreakPoints] - numBreakInstances[k][j - 1]);
                }

                impBreakPoints[index] = lBreakPoint * rBreakPoint - lrMulProduct;
                index++;
            }










            double[] _valueBreakPoints = getNumCluster(impBreakPoints, valueBreakPoints, index);

            AttributeBreakPoint attributeBreakPoint = new AttributeBreakPoint(_valueBreakPoints.length, i);

            for (int j = 0; j < _valueBreakPoints.length; j++) {

                attributeBreakPoint.breakPoints[j] = new BreakPoint();
                attributeBreakPoint.breakPoints[j].value = _valueBreakPoints[j];

            }

            instancesBreakPoint.attributeBreakPoints[i] = attributeBreakPoint;

            instancesBreakPoint.attributeBreakPoints[i].SortBreakPoint();

        }

        int num = instancesBreakPoint.attributeBreakPoints[0].numBreakPoints;
        int n = 0;

        for (int j = 0; j < instancesBreakPoint.numAttributeBreakPoints; j++) {

            if (instancesBreakPoint.attributeBreakPoints[j].numBreakPoints != num) {


                num = instancesBreakPoint.attributeBreakPoints[j].numBreakPoints;
                n = 0;

            }

            n++;

        }

        return instancesBreakPoint;
    }

    public InstancesBreakPoint SecondStep(boolean isCluster, InstancesBreakPoint instancesOriginalBreakPoint, Instances instances) {

        int numInstances = instances.numInstances();
        int numAttributes = instances.numAttributes();
        int numClasses = instances.numClasses();
        int numNewAttributeBreakPoints = 0;

        InstancesBreakPoint instancesNewBreakPoint = new InstancesBreakPoint(numAttributes - 1);
        int[] numAttributeAvailableBreakPoints = new int[numAttributes - 1];

        int index = 0;

        for (int i = 0; i < numAttributes - 1; i++) {

            instances.sort(i);

            numAttributeAvailableBreakPoints[i] = 0;
            instancesNewBreakPoint.attributeBreakPoints[i] = new AttributeBreakPoint(numInstances, i);
            instancesNewBreakPoint.attributeBreakPoints[i].numBreakPoints = 0;
            index = 0;

            for (int j = 1; j < numInstances; j++) {
                if (instances.instance(j - 1).value(i) != instances.instance(j).value(i)) {

                    instancesNewBreakPoint.attributeBreakPoints[i].breakPoints[index] = new BreakPoint();
                    instancesNewBreakPoint.attributeBreakPoints[i].breakPoints[index].value = (instances.instance(j - 1).value(i) + instances.instance(j).value(i)) / 2;

                    instancesNewBreakPoint.attributeBreakPoints[i].breakPoints[index].isSelect = false;
                    instancesNewBreakPoint.attributeBreakPoints[i].breakPoints[index].importance = 0;
                    instancesNewBreakPoint.attributeBreakPoints[i].breakPoints[index].entropy = 0;
                    instancesNewBreakPoint.attributeBreakPoints[i].numBreakPoints++;
                    index++;
                }
            }
        }


        EquivalentNode[] nodes = new EquivalentNode[numInstances];


        for (int i = 0; i < numInstances; i++) {
            nodes[i] = new EquivalentNode();
            nodes[i].index = i;
            nodes[i].nextDomain = -1;
            nodes[i].nextEntry = i + 1;
            nodes[i].classes = -1;
        }
        nodes[numInstances - 1].nextEntry = -1;

        int pDomainStart = 0;
        int pEntryStart = 0;
        int pNextDomain;

        int pDomain, pEntry;
        double breakPointValue = 0;

        int[] numLBreakPoint = new int[numClasses];
        int numLTotalBreakPoint;
        int[] numRBreakPoint = new int[numClasses];
        int numRTotalBreakPoint;


        if (isCluster) {
            for (int i = 0; i < numAttributes - 1; i++) {

                int originalJ = 0, newJ = 0;
                while (true) {

                    if (originalJ >= instancesOriginalBreakPoint.attributeBreakPoints[i].numBreakPoints || newJ >= instancesNewBreakPoint.attributeBreakPoints[i].numBreakPoints) {
                        break;
                    }

                    if (instancesOriginalBreakPoint.attributeBreakPoints[i].breakPoints[originalJ].value == instancesNewBreakPoint.attributeBreakPoints[i].breakPoints[newJ].value) {

                        instancesNewBreakPoint.attributeBreakPoints[i].breakPoints[newJ].isSelect = true;


                        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                        numAttributeAvailableBreakPoints[i]++;
                        if (numAttributeAvailableBreakPoints[i] == 1) {
                            numNewAttributeBreakPoints++;
                        }
                        int indexAttribute = i;
                        /////////////////////////////////////////////////////
                        double minBreakPointValue = instancesNewBreakPoint.attributeBreakPoints[i].breakPoints[newJ].value;///////////////////////////////////////////////////////////////

                        pDomain = pDomainStart;
                        do {
                            pNextDomain = nodes[pDomain].nextDomain;
                            if (nodes[pDomain].classes >= 0) {
                                pDomain = pNextDomain;
                                continue;
                            }
                            pEntryStart = pDomain;
                            int pFristDomain = -1;
                            int pSecondDomain = -1;
                            int pFirstDomainLink = -1;
                            int pSecondDomainLink = -1;
                            int pNextEntry;

                            pEntry = pEntryStart;
                            do {
                                pNextEntry = nodes[pEntry].nextEntry;
                                if (instances.instance(nodes[pEntry].index).value(indexAttribute) < minBreakPointValue) {
                                    if (pFristDomain == -1) {
                                        pFristDomain = pEntry;
                                        pFirstDomainLink = pFristDomain;
                                        nodes[pFristDomain].classes = instances.instance(nodes[pFristDomain].index).classValue();
                                    } else {

                                        nodes[pFirstDomainLink].nextEntry = pEntry;
                                        pFirstDomainLink = pEntry;
                                    }

                                    if (nodes[pFristDomain].classes != instances.instance(nodes[pEntry].index).classValue()) {
                                        nodes[pFristDomain].classes = -1;
                                    }
                                } else {
                                    if (pSecondDomain == -1) {
                                        pSecondDomain = pEntry;
                                        pSecondDomainLink = pSecondDomain;
                                        nodes[pSecondDomain].classes = instances.instance(nodes[pSecondDomain].index).classValue();
                                    } else {

                                        nodes[pSecondDomainLink].nextEntry = pEntry;
                                        pSecondDomainLink = pEntry;
                                    }

                                    if (nodes[pSecondDomain].classes != instances.instance(nodes[pEntry].index).classValue()) {
                                        nodes[pSecondDomain].classes = -1;
                                    }
                                }

                                nodes[pEntry].nextEntry = -1;
                                pEntry = pNextEntry;
                            } while (pNextEntry != -1);

                            if (pEntryStart == pFristDomain) {
                                if (pSecondDomain != -1) {
                                    nodes[pSecondDomain].nextDomain = nodes[pFristDomain].nextDomain;
                                    nodes[pFristDomain].nextDomain = pSecondDomain;
                                }
                            } else {
                                if (pFristDomain != -1) {
                                    nodes[pFristDomain].nextDomain = nodes[pSecondDomain].nextDomain;
                                    nodes[pSecondDomain].nextDomain = pFristDomain;
                                }
                            }

                            pDomain = pNextDomain;
                        } while (pNextDomain != -1);
                        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////


                        originalJ++;
                        newJ++;


                    } else {
                        newJ++;
                    }

                }
            }
        }


        double lBreakPoint;
        double rBreakPoint;
        double lrMulProduct;


        double maxImportance = Double.MIN_VALUE;
        int minPosI = -1;
        int minPosJ = -1;
        double minBreakPointValue = 0;
        int indexAttribute = 0;


        boolean isSuccess;
        while (true) {
            maxImportance = Double.MIN_VALUE;
            minPosI = -1;
            minPosJ = -1;
            isSuccess = true;
            for (pDomain = pDomainStart; pDomain != -1; pDomain = nodes[pDomain].nextDomain) {
                if (nodes[pDomain].classes < 0) {
                    isSuccess = false;
                    break;
                }
            }
            if (isSuccess == true) {
                break;
            }
            for (int i = 0; i < instancesNewBreakPoint.numAttributeBreakPoints; i++) {
                for (int j = 0; j < instancesNewBreakPoint.attributeBreakPoints[i].numBreakPoints; j++) {
                    if (instancesNewBreakPoint.attributeBreakPoints[i].breakPoints[j].isSelect == true) {

                        continue;
                    }

                    instancesNewBreakPoint.attributeBreakPoints[i].breakPoints[j].importance = 0;

                    for (pDomain = pDomainStart; pDomain != -1; pDomain = nodes[pDomain].nextDomain) {
                        if (nodes[pDomain].classes >= 0) {

                            continue;
                        }
                        for (int k = 0; k < numClasses; k++) {

                            numLBreakPoint[k] = numRBreakPoint[k] = 0;
                        }
                        numLTotalBreakPoint = numRTotalBreakPoint = 0;
                        breakPointValue = instancesNewBreakPoint.attributeBreakPoints[i].breakPoints[j].value;
                        pEntryStart = pDomain;

                        for (pEntry = pEntryStart; pEntry != -1; pEntry = nodes[pEntry].nextEntry) {
                            index = nodes[pEntry].index;

                            if (instances.instance(index).value(i) < breakPointValue) {

                                numLBreakPoint[(int) instances.instance(index).classValue()]++;
                                numLTotalBreakPoint++;
                            } else {

                                numRBreakPoint[(int) instances.instance(index).classValue()]++;
                                numRTotalBreakPoint++;
                            }
                        }

                        lBreakPoint = rBreakPoint = lrMulProduct = 0;
                        for (int k = 0; k < numClasses; k++) {

                            lBreakPoint += numLBreakPoint[k];
                            rBreakPoint += numRBreakPoint[k];
                            lrMulProduct += numLBreakPoint[k] * numRBreakPoint[k];
                        }

                        instancesNewBreakPoint.attributeBreakPoints[i].breakPoints[j].importance += lBreakPoint * rBreakPoint - lrMulProduct;

                    }
                    if (maxImportance < instancesNewBreakPoint.attributeBreakPoints[i].breakPoints[j].importance) {
                        maxImportance = instancesNewBreakPoint.attributeBreakPoints[i].breakPoints[j].importance;
                        minPosI = i;
                        minPosJ = j;
                    }
                }
            }
            if (minPosI == -1 || minPosJ == -1) {
                System.err.println("The original decision table is conflicting!");
                return null;
            }
            instancesNewBreakPoint.attributeBreakPoints[minPosI].breakPoints[minPosJ].isSelect = true;
            numAttributeAvailableBreakPoints[minPosI]++;
            if (numAttributeAvailableBreakPoints[minPosI] == 1) {
                numNewAttributeBreakPoints++;
            }
            indexAttribute = minPosI;
            minBreakPointValue = instancesNewBreakPoint.attributeBreakPoints[minPosI].breakPoints[minPosJ].value;

            pDomain = pDomainStart;
            do {
                pNextDomain = nodes[pDomain].nextDomain;
                if (nodes[pDomain].classes >= 0) {
                    pDomain = pNextDomain;
                    continue;
                }
                pEntryStart = pDomain;
                int pFristDomain = -1;
                int pSecondDomain = -1;
                int pFirstDomainLink = -1;
                int pSecondDomainLink = -1;
                int pNextEntry;

                pEntry = pEntryStart;
                do {
                    pNextEntry = nodes[pEntry].nextEntry;
                    if (instances.instance(nodes[pEntry].index).value(indexAttribute) < minBreakPointValue) {
                        if (pFristDomain == -1) {
                            pFristDomain = pEntry;
                            pFirstDomainLink = pFristDomain;
                            nodes[pFristDomain].classes = instances.instance(nodes[pFristDomain].index).classValue();
                        } else {

                            nodes[pFirstDomainLink].nextEntry = pEntry;
                            pFirstDomainLink = pEntry;
                        }

                        if (nodes[pFristDomain].classes != instances.instance(nodes[pEntry].index).classValue()) {
                            nodes[pFristDomain].classes = -1;
                        }
                    } else {
                        if (pSecondDomain == -1) {
                            pSecondDomain = pEntry;
                            pSecondDomainLink = pSecondDomain;
                            nodes[pSecondDomain].classes = instances.instance(nodes[pSecondDomain].index).classValue();
                        } else {

                            nodes[pSecondDomainLink].nextEntry = pEntry;
                            pSecondDomainLink = pEntry;
                        }

                        if (nodes[pSecondDomain].classes != instances.instance(nodes[pEntry].index).classValue()) {
                            nodes[pSecondDomain].classes = -1;
                        }
                    }

                    nodes[pEntry].nextEntry = -1;
                    pEntry = pNextEntry;
                } while (pNextEntry != -1);
                if (pEntryStart == pFristDomain) {
                    if (pSecondDomain != -1) {
                        nodes[pSecondDomain].nextDomain = nodes[pFristDomain].nextDomain;
                        nodes[pFristDomain].nextDomain = pSecondDomain;
                    }
                } else {
                    if (pFristDomain != -1) {
                        nodes[pFristDomain].nextDomain = nodes[pSecondDomain].nextDomain;
                        nodes[pSecondDomain].nextDomain = pFristDomain;
                    }
                }

                pDomain = pNextDomain;
            } while (pNextDomain != -1);


        }


        InstancesBreakPoint newInstancesBreakPoint = new InstancesBreakPoint(numNewAttributeBreakPoints);

        indexAttribute = 0;
        int indexBreakPoint = 0;
        for (int i = 0; i < instancesNewBreakPoint.numAttributeBreakPoints; i++) {

            if (numAttributeAvailableBreakPoints[i] > 0) {

                indexBreakPoint = 0;
                newInstancesBreakPoint.attributeBreakPoints[indexAttribute] = new AttributeBreakPoint(numAttributeAvailableBreakPoints[i], instancesNewBreakPoint.attributeBreakPoints[i].indexAttribute);

                for (int j = 0; j < instancesNewBreakPoint.attributeBreakPoints[i].numBreakPoints; j++) {
                    if (instancesNewBreakPoint.attributeBreakPoints[i].breakPoints[j].isSelect == true) {

                        newInstancesBreakPoint.attributeBreakPoints[indexAttribute].breakPoints[indexBreakPoint] = new BreakPoint();
                        newInstancesBreakPoint.attributeBreakPoints[indexAttribute].breakPoints[indexBreakPoint].value = instancesNewBreakPoint.attributeBreakPoints[i].breakPoints[j].value;
                        newInstancesBreakPoint.attributeBreakPoints[indexAttribute].breakPoints[indexBreakPoint].isSelect = instancesNewBreakPoint.attributeBreakPoints[i].breakPoints[j].isSelect;
                        newInstancesBreakPoint.attributeBreakPoints[indexAttribute].breakPoints[indexBreakPoint].entropy = instancesNewBreakPoint.attributeBreakPoints[i].breakPoints[j].entropy;
                        newInstancesBreakPoint.attributeBreakPoints[indexAttribute].breakPoints[indexBreakPoint].importance = instancesNewBreakPoint.attributeBreakPoints[i].breakPoints[j].importance;
                        indexBreakPoint++;
                    }
                }
                indexAttribute++;
            }

        }


        return newInstancesBreakPoint;
    }
}
