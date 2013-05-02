/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package datamining.rough;

import datamining.core.Instances;

/**
 *
 * @author lenovo
 */
public class ValueReductionByDivide implements ValueReduction {

    public boolean[] isValueRemoveables;
    public OSet[] oSets;
    public Instances instances;
    public int[] attributeOrders;
    public boolean[] isInstanceInPositives;
    //值约简的位置
    private int indexValueReduction;

    public void SetInformation(Instances instances, int[] attributeOrders, boolean[] isInstanceInPositives) {
        this.instances = instances;
        this.attributeOrders = attributeOrders;
        this.isInstanceInPositives = isInstanceInPositives;
    }

    public Instances ValueReductionByDivide() {

        OSet pDomainStart = null;

        OSet[] oSets = new OSet[instances.numInstances()];

        for (int i = 0; i < oSets.length; i++) {
            oSets[i] = new OSet();
            oSets[i].index = i;
        }

        isValueRemoveables = new boolean[instances.numInstances()];

        for (int i = 0; i < attributeOrders.length; i++) {

            for (int j = 0; j < oSets.length - 1; j++) {
                oSets[j].nextEntry = oSets[j + 1];
            }
            oSets[instances.numInstances() - 1].nextEntry = null;
            pDomainStart = oSets[0];


            //从后往前约简
            indexValueReduction = attributeOrders.length - i - 1;

            //初始化indexValueReduction属性上的值全部可以约简
            for (int j = 0; j < isValueRemoveables.length; j++) {
                isValueRemoveables[j] = true;
            }

            //递归值约简
            ValueReductionByDivideEx(0, pDomainStart);

            //将约简的设置成Miss
            for (int j = 0; j < isValueRemoveables.length; j++) {
                //值可以约简并且对象在正区域中
                if (isValueRemoveables[j] == true && isInstanceInPositives[j] == true) {
                    instances.instance(j).setMissing(attributeOrders[indexValueReduction]);
                }
            }
        }
        return instances;
    }

    private void ValueReductionByDivideEx(int index, OSet pDomainStart) {

        //如果是约简的位置，则跳过
        if (indexValueReduction == index) {
            index++;
        }

        boolean isAllOutPositive = true;
        boolean isAllDecSame = true;

        OSet pLinkSet = pDomainStart;
        double decValue = instances.instance(pDomainStart.index).classValue();

        //递归结束 判断约简属性是不是可以在该等价类上进行值约简
        if (index == attributeOrders.length) {

            int countInstance = 0;

            while (pLinkSet != null) {
                countInstance++;
                if (instances.instance(pLinkSet.index).classValue() != decValue) {
                    isAllDecSame = false;
                }

                if (isInstanceInPositives[pLinkSet.index] == true) {
                    isAllOutPositive = false;
                }
                pLinkSet = pLinkSet.nextEntry;
            }


            //如果只有一个对象 那么该对象肯定是属性正区域的 
            if (countInstance == 1) {
                return;
            }

            //去掉indexValueReduction属性后 该等价类上所有对象的决策值都相同 即正区域没有发生变化 则可以进行值约简
            if (isAllDecSame == true) {
                return;
            }

            //所有对象都不在正区域
            if (isAllOutPositive == true) {
                return;
            }

            pLinkSet = pDomainStart;
            while (pLinkSet != null) {
                isValueRemoveables[pLinkSet.index] = false;
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

            //如果这个对象的值是被约简的，则把它放到pMissSet中去，以备后面与其他等价类组合成新的等价类
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

        //如果全部都是MissVaue，即都是被值约简的，那么需要特殊处理，直接递归到下一层
        if (pDomainLink == null) {
            ValueReductionByDivideEx(index + 1, pMissSet);
            return;
        }
        //否则，将MissValue构成的等价集合链接到其他等价类中去，这是必须的
        while (pDomainLink != null) {
            pNextLink = pDomainLink.nextDomain;
            ValueReductionByDivideEx(index + 1,
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

    @Override
    public Instances GetReducedInstances(Instances instances) {
        PositiveRegion positiveRegion = new PositiveRegion();
        positiveRegion.CalcPostiveRegionByOrdinarySort(instances);

        int[] attributeOrders = new int[instances.numAttributes() - 1];
        for (int i = 0; i < instances.numAttributes() - 1; i++) {
            attributeOrders[i] = i;
        }
        boolean[] isInstanceInPositives = positiveRegion.GetPositiveInstanceLabels();

        ValueReductionByDivide valueReductionByDivide = new ValueReductionByDivide();
        valueReductionByDivide.SetInformation(instances, attributeOrders, isInstanceInPositives);
        Instances tmpInstances = valueReductionByDivide.ValueReductionByDivide();
        return tmpInstances;
    }
}
