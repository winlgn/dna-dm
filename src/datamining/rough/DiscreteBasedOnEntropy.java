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
 * 基于信息熵的离散化算法
 *
 * @author lenovo
 */
public class DiscreteBasedOnEntropy implements Discretization {

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

    @Override
    public InstancesBreakPoint discrete(Instances instances) {
        int numInstances = instances.numInstances();
        int numAttributes = instances.numAttributes();
        int numClasses = instances.numClasses();
        int numNewAttributeBreakPoints = 0;
        InstancesBreakPoint instancesBreakPoint = new InstancesBreakPoint(numAttributes - 1);
        int[] numAttributeAvailableBreakPoints = new int[numAttributes - 1];

        int index = 0;

        for (int i = 0; i < numAttributes - 1; i++) {
            instances.sort(i);
            numAttributeAvailableBreakPoints[i] = 0;
            instancesBreakPoint.attributeBreakPoints[i] = new AttributeBreakPoint(numInstances, i);
            instancesBreakPoint.attributeBreakPoints[i].numBreakPoints = 0;
            index = 0;

            for (int j = 1; j < numInstances; j++) {
                if (instances.instance(j - 1).value(i) != instances.instance(j).value(i)) {
                    instancesBreakPoint.attributeBreakPoints[i].breakPoints[index] = new BreakPoint();
                    instancesBreakPoint.attributeBreakPoints[i].breakPoints[index].isSelect = false;
                    instancesBreakPoint.attributeBreakPoints[i].breakPoints[index].value = (instances.instance(j - 1).value(i) + instances.instance(j).value(i)) / 2;
                    instancesBreakPoint.attributeBreakPoints[i].breakPoints[index].importance = 0;
                    instancesBreakPoint.attributeBreakPoints[i].breakPoints[index].entropy = 0;
                    instancesBreakPoint.attributeBreakPoints[i].numBreakPoints++;
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

        double p = 0;
        double q = 0;
        double HL = 0;
        double HR = 0;

        double minEntropy = Double.MAX_VALUE;
        int minPosI = -1;
        int minPosJ = -1;
        double minBreakPointValue = 0;
        int indexAttribute = 0;

        boolean isSuccess;
        while (true) {
            minEntropy = Double.MAX_VALUE;
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
            for (int i = 0; i < instancesBreakPoint.numAttributeBreakPoints; i++) {
                for (int j = 0; j < instancesBreakPoint.attributeBreakPoints[i].numBreakPoints; j++) {
                    if (instancesBreakPoint.attributeBreakPoints[i].breakPoints[j].isSelect == true) {

                        continue;
                    }
                    instancesBreakPoint.attributeBreakPoints[i].breakPoints[j].entropy = 0;
                    for (pDomain = pDomainStart; pDomain != -1; pDomain = nodes[pDomain].nextDomain) {
                        if (nodes[pDomain].classes >= 0) {

                            continue;
                        }
                        for (int k = 0; k < numClasses; k++) {

                            numLBreakPoint[k] = numRBreakPoint[k] = 0;
                        }
                        numLTotalBreakPoint = numRTotalBreakPoint = 0;
                        breakPointValue = instancesBreakPoint.attributeBreakPoints[i].breakPoints[j].value;
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
                        p = q = HL = HR = 0;
                        for (int k = 0; k < numClasses; k++) {
                            if (numLTotalBreakPoint > 0) {
                                p = (double) numLBreakPoint[k] / (double) numLTotalBreakPoint;
                                if (p > 0) {
                                    HL += (-p) * Math.log(p) / Math.log(2);
                                }
                            }
                            if (numRTotalBreakPoint > 0) {
                                q = (double) numRBreakPoint[k] / (double) numRTotalBreakPoint;
                                if (q > 0) {
                                    HR += (-q) * Math.log(q) / Math.log(2);
                                }
                            }
                        }

                        instancesBreakPoint.attributeBreakPoints[i].breakPoints[j].entropy += HL * numLTotalBreakPoint / (numLTotalBreakPoint + numRTotalBreakPoint);
                        instancesBreakPoint.attributeBreakPoints[i].breakPoints[j].entropy += HR * numRTotalBreakPoint / (numLTotalBreakPoint + numRTotalBreakPoint);
                    }
                    if (minEntropy > instancesBreakPoint.attributeBreakPoints[i].breakPoints[j].entropy) {
                        minEntropy = instancesBreakPoint.attributeBreakPoints[i].breakPoints[j].entropy;
                        minPosI = i;
                        minPosJ = j;
                    }
                }
            }
            if (minPosI == -1 || minPosJ == -1) {
                System.err.println("The original decision table is conflicting!");
                return null;
            }
            instancesBreakPoint.attributeBreakPoints[minPosI].breakPoints[minPosJ].isSelect = true;
            numAttributeAvailableBreakPoints[minPosI]++;
            if (numAttributeAvailableBreakPoints[minPosI] == 1) {
                numNewAttributeBreakPoints++;
            }
            indexAttribute = minPosI;
            minBreakPointValue = instancesBreakPoint.attributeBreakPoints[minPosI].breakPoints[minPosJ].value;

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
        for (int i = 0; i < instancesBreakPoint.numAttributeBreakPoints; i++) {
            if (numAttributeAvailableBreakPoints[i] > 0) {
                indexBreakPoint = 0;
                newInstancesBreakPoint.attributeBreakPoints[indexAttribute] = new AttributeBreakPoint(numAttributeAvailableBreakPoints[i], instancesBreakPoint.attributeBreakPoints[i].indexAttribute);

                for (int j = 0; j < instancesBreakPoint.attributeBreakPoints[i].numBreakPoints; j++) {
                    if (instancesBreakPoint.attributeBreakPoints[i].breakPoints[j].isSelect == true) {
                        newInstancesBreakPoint.attributeBreakPoints[indexAttribute].breakPoints[indexBreakPoint] = new BreakPoint();
                        newInstancesBreakPoint.attributeBreakPoints[indexAttribute].breakPoints[indexBreakPoint].value = instancesBreakPoint.attributeBreakPoints[i].breakPoints[j].value;
                        newInstancesBreakPoint.attributeBreakPoints[indexAttribute].breakPoints[indexBreakPoint].isSelect = instancesBreakPoint.attributeBreakPoints[i].breakPoints[j].isSelect;
                        newInstancesBreakPoint.attributeBreakPoints[indexAttribute].breakPoints[indexBreakPoint].entropy = instancesBreakPoint.attributeBreakPoints[i].breakPoints[j].entropy;
                        newInstancesBreakPoint.attributeBreakPoints[indexAttribute].breakPoints[indexBreakPoint].importance = instancesBreakPoint.attributeBreakPoints[i].breakPoints[j].importance;
                        indexBreakPoint++;
                    }
                }
                indexAttribute++;
            }
        }
        return newInstancesBreakPoint;
    }
}
