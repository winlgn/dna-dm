/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package datamining.rough;

import datamining.core.Instance;
import datamining.core.Instances;
import java.util.Arrays;
import java.util.Comparator;

/**
 *
 * @author lenovo
 */
class PositiveRegion {

    private OSet[] oSets;

    class LableClass {

        int startIndex;
        double classVale;
    }

    class InstanceIndex {

        int index;
    }
    private Instances instances;
    private int[] attributeOrders;
    private boolean[] isInstanceInPositives;
    private int numPositiveInstance;

    public boolean[] CalcPostiveRegionByOrdinarySort(Instances instances) {
        this.instances = instances;
        attributeOrders = new int[instances.numAttributes() - 1];
        for (int i = 0; i < attributeOrders.length; i++) {
            attributeOrders[i] = i;
        }
        GetPositiveByOrdinarySort();
        return isInstanceInPositives;
    }

    class InstanceComparator implements Comparator<InstanceIndex> {

        private Instances instances;

        private int comperaInst(Instance o1, Instance o2) {
            for (int i = 0; i < attributeOrders.length; i++) {
                int index = attributeOrders[i];
                if (o1.value(index) == o2.value(index)) {
                    continue;
                } else if (o1.value(index) > o2.value(index)) {
                    return 1;
                } else {
                    return -1;
                }
            }
            return 0;
        }

        public InstanceComparator(Instances instances) {
            this.instances = instances;
        }

        public int compare(InstanceIndex o1, InstanceIndex o2) {
            // TODO Auto-generated method stub
            Instance arg1 = instances.instance(o1.index);
            Instance arg2 = instances.instance(o2.index);
            return comperaInst(arg1, arg2);
        }
    }

    private void GetPositiveByOrdinarySort() {
        int numPositive = 0;
        boolean[] isInPositive = new boolean[instances.numInstances()];
        Arrays.fill(isInPositive, false);

        InstanceComparator instanceComparator = new InstanceComparator(
                instances);

        InstanceIndex[] instanceIndex = new InstanceIndex[instances.numInstances()];
        for (int i = 0; i < instanceIndex.length; i++) {
            instanceIndex[i] = new InstanceIndex();
            instanceIndex[i].index = i;
        }

        Arrays.sort(instanceIndex, instanceComparator);

        int index1, index2;
        int numLables;

        LableClass[] lableClass = new LableClass[instances.numInstances() + 1];// ���ܻ��һ��

        lableClass[0] = new LableClass();
        lableClass[0].startIndex = 0;
        lableClass[0].classVale = instances.instance(instanceIndex[0].index).classValue();
        numLables = 1;

        for (int i = 1; i < instances.numInstances(); i++) {

            index1 = instanceIndex[i].index;
            index2 = instanceIndex[i - 1].index;

            boolean IsSame = true;

            for (int j = 0; j < instances.numAttributes() - 1; j++) {

                if (instances.instance(index1).value(j) == instances.instance(
                        index2).value(j)) {
                    continue;
                } else {
                    IsSame = false;
                    break;
                }
            }

            if (IsSame != true) {
                lableClass[numLables] = new LableClass();
                lableClass[numLables].startIndex = i;
                lableClass[numLables].classVale = instances.instance(
                        instanceIndex[i].index).classValue();
                numLables++;
            } else {
                if (instances.instance(index1).classValue() != instances.instance(index2).classValue()) {

                    lableClass[numLables - 1].classVale = -1;

                }
            }
        }

        int startIndex, endIndex;
        numPositive = 0;

        for (int i = 0; i < numLables - 1; i++) {

            startIndex = lableClass[i].startIndex;
            endIndex = lableClass[i + 1].startIndex;

            if (lableClass[i].classVale == -1) {
                for (int j = startIndex; j < endIndex; j++) {
                    isInPositive[instanceIndex[j].index] = false;
                }
            } else {
                for (int j = startIndex; j < endIndex; j++) {
                    isInPositive[instanceIndex[j].index] = true;
                    numPositive++;
                }
            }
        }

        startIndex = lableClass[numLables - 1].startIndex;
        endIndex = instances.numInstances();

        if (lableClass[numLables - 1].classVale == -1) {
            for (int j = startIndex; j < endIndex; j++) {
                isInPositive[instanceIndex[j].index] = false;
            }
        } else {
            for (int j = startIndex; j < endIndex; j++) {
                isInPositive[instanceIndex[j].index] = true;
                numPositive++;
            }
        }
        numPositiveInstance = numPositive;
        isInstanceInPositives = isInPositive;
    }

    public boolean[] GetPositiveInstanceLabels() {
        return isInstanceInPositives;
    }

    public boolean[] CalcPostiveRegionByDivideAndConquer(Instances instances) {
        this.instances = instances;
        attributeOrders = new int[instances.numAttributes() - 1];
        for (int i = 0; i < attributeOrders.length; i++) {
            attributeOrders[i] = i;
        }
        GetPositiveByDivideAndConquer();
        return isInstanceInPositives;
    }

    private void GetPositiveByDivideAndConquer() {
        if (oSets == null) {
            oSets = new OSet[instances.numInstances()];
            for (int i = 0; i < oSets.length; i++) {
                oSets[i] = new OSet();
            }
        }

        if (oSets.length != instances.numInstances()) {
            oSets = new OSet[instances.numInstances()];
            for (int i = 0; i < oSets.length; i++) {
                oSets[i] = new OSet();
            }
        }
        for (int i = 0; i < oSets.length - 1; i++) {
            oSets[i].index = i;
            oSets[i].nextDomain = null;
            oSets[i].nextEntry = oSets[i + 1];
        }
        oSets[instances.numInstances() - 1].index = instances.numInstances() - 1;
        oSets[instances.numInstances() - 1].nextDomain = null;
        oSets[instances.numInstances() - 1].nextEntry = null;

        if (isInstanceInPositives == null) {
            isInstanceInPositives = new boolean[instances.numInstances()];
            Arrays.fill(isInstanceInPositives, true);
        }

        if (isInstanceInPositives.length != instances.numInstances()) {
            isInstanceInPositives = new boolean[instances.numInstances()];
            Arrays.fill(isInstanceInPositives, true);
        }

        numPositiveInstance = 0;
        GetPositiveByDivideAndConquerEx(0, oSets[0]);

        for (int i = 0; i < isInstanceInPositives.length; i++) {
            if (isInstanceInPositives[i] == true) {
                numPositiveInstance++;
            }
        }
    }

    private void GetPositiveByDivideAndConquerEx(int index, OSet pDomainStart) {
        OSet pLinkSet = pDomainStart;
        double decValue = instances.instance(pDomainStart.index).classValue();
        //递归结束 判断每个等价类是不是觉有相同的决策属性
        if (index == attributeOrders.length) {
            boolean isAllDecSame = true;
            int countInstance = 0;
            while (pLinkSet != null) {
                countInstance++;
                if (instances.instance(pLinkSet.index).classValue() != decValue) {
                    isAllDecSame = false;
                }
                pLinkSet = pLinkSet.nextEntry;
            }
            if (isAllDecSame == true) {
                return;
            }

            pLinkSet = pDomainStart;
            while (pLinkSet != null) {
                isInstanceInPositives[pLinkSet.index] = false;
                pLinkSet = pLinkSet.nextEntry;
            }
            return;
        }

        int indexAttribute = attributeOrders[index];
        pLinkSet = pDomainStart;
        OSet pDomainLink;
        OSet pNextLink;

        pDomainStart = null;

        OSet pMissSet = null;

        while (pLinkSet != null) {

            //需要下记录下一个对象在什么位置，存放到pNextLink中去
            pNextLink = pLinkSet.nextEntry;
            pDomainLink = pDomainStart;

            //如果这个对象的值是空的，则把它放到pMissSet中去，以备后面与其他等价类组合成新的等价类
            if (instances.instance(pLinkSet.index).isMissing(indexAttribute) == true) {
                pLinkSet.nextEntry = pMissSet;
                pMissSet = pLinkSet;
            } else {
                //将pLinkSet指向的对象链接到相应的等价类集合中去，如果没有找到，那么它自己会重新够建一个等价类
                while (pDomainLink != null && instances.instance(pDomainLink.index).value(indexAttribute)
                        != instances.instance(pLinkSet.index).value(indexAttribute)) {
                    pDomainLink = pDomainLink.nextDomain;
                }

                //没有找到的时候，pLinkSet自己构建一个等价类
                if (pDomainLink == null) {
                    pLinkSet.nextDomain = pDomainStart;
                    pLinkSet.nextEntry = null;
                    pDomainStart = pLinkSet;
                } //找到了，则直接将pLinkSet链接到相应的等价类
                else {
                    pLinkSet.nextEntry = pDomainLink.nextEntry;
                    pDomainLink.nextEntry = pLinkSet;
                }
            }

            pLinkSet = pNextLink;
        }

        pDomainLink = pDomainStart;

        //如果全部都是MissVaue，那么需要特殊处理，直接递归到下一层
        if (pDomainLink == null) {
            GetPositiveByDivideAndConquerEx(index + 1, pMissSet);
            return;
        }

        //否则，将MissValue构成的等价集合链接到其他等价类中去，这是必须的
        while (pDomainLink != null) {
            pNextLink = pDomainLink.nextDomain;
            GetPositiveByDivideAndConquerEx(index + 1,
                    MergeOSet(pMissSet, pDomainLink));
            pDomainLink = pNextLink;
        }
    }

    private OSet MergeOSet(OSet pMissSet, OSet pDomainStart) {
        if (pMissSet == null) {
            return pDomainStart;
        }

        OSet pLinkSet = pMissSet;
        OSet pNewSetStart = pDomainStart;
        OSet pNewLinkSet;

        while (pLinkSet != null) {
            pNewLinkSet = pLinkSet.Clone();
            pNewLinkSet.nextEntry = pNewSetStart;
            pNewSetStart = pNewLinkSet;

            pLinkSet = pLinkSet.nextEntry;
        }
        return pNewSetStart;
    }

    public int GetPositiveInstanceNumber() {
        return numPositiveInstance;
    }
}
