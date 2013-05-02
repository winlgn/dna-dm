/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package datamining.rough;

import datamining.core.Instances;
import java.util.Arrays;

/**
 *
 * @author lenovo
 */
class NonEmptyLableAttribute {

    public Instances instances;
    OSet[] oSets;
    public int numAttribute;
    public int numInstance;
    public int numClass;
    public boolean[] isInstanceInPositives;
    public int numPositiveInstance;
    public int[] attributeOrders;
    public boolean[] isNonEmptyAttributes;
    public int[] nonEmptyAttributes;

    public NonEmptyLableAttribute() {
    }

    public void SetInformations(Instances instances, int[] attributeOrders, boolean[] isInPositives, int numPositiveInstance) {
        this.instances = instances;
        this.attributeOrders = attributeOrders;
        this.isInstanceInPositives = isInPositives;
        this.numPositiveInstance = numPositiveInstance;
    }

    public void CalcNonEmptyLabelAttributes() {

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


        isNonEmptyAttributes = new boolean[attributeOrders.length];
        Arrays.fill(isNonEmptyAttributes, false);

        CalcNonEmptyLabelAttributesEx(0, oSets[0]);

        CalcNonEmptyAttributesIndex();
    }

    /**
     * @Name: CalcNonEmptyLabelAttributesEx @Description: TODO (Here is your
     * comment) 递归计算非空标签属性
     *
     * @param index
     * @param pDomainStart
     * @throws @date 2011-5-22
     */
    private void CalcNonEmptyLabelAttributesEx(int index, OSet pDomainStart) {
        //已经到最后一个属性，直接返回
        if (index == attributeOrders.length) {
            return;
        }

//      不同于属性核的计算 需要再次检验的		
//		if (isNonEmptyAttributes[index] == true) {
//			return;
//		}

        int indexAttribute = attributeOrders[index];//实际的属性标签
        OSet pLinkSet = pDomainStart;
        double decValue = instances.instance(pDomainStart.index).classValue();//第一个对象的决策值
        double attValue = instances.instance(pDomainStart.index).value(indexAttribute);//第一个对象的属性值
        boolean isAllDecSame = true;
        boolean isAllOutPositive = true;
        boolean isAllAttSame = true;

        //扫描一次当前的决策表
        while (pLinkSet != null) {
            if (decValue != instances.instance(pLinkSet.index).classValue()) {
                isAllDecSame = false;
            }

            if (attValue != instances.instance(pLinkSet.index).value(indexAttribute)) {
                isAllAttSame = false;
            }

            if (isInstanceInPositives[pLinkSet.index] == true) {
                isAllOutPositive = false;
            }
            pLinkSet = pLinkSet.nextEntry;
        }

        //所有对象的决策属性相同，返回
        if (isAllDecSame == true) {
            return;
        }

        //所有对象都不在正区域中，返回
        if (isAllOutPositive == true) {
            return;
        }

        //所有对象在该属性上的值相同，该属性不会是非空标签属性
        if (isAllAttSame == true) {
            CalcNonEmptyLabelAttributesEx(index + 1, pDomainStart);
            return;
        }

        //否则后面肯定存在非空标签属性
        isNonEmptyAttributes[index] = true;

        pLinkSet = pDomainStart;
        OSet pDomainLink;
        OSet pNextLink;

        pDomainStart = null;


        //while循环用于划分等级类
        while (pLinkSet != null) {

            //需要下记录下一个对象在什么位置，存放到pNextLink中去
            pNextLink = pLinkSet.nextEntry;
            pDomainLink = pDomainStart;

            // 将pLinkSet指向的对象链接到相应的等价类集合中去，如果没有找到，那么它自己会重新够建一个等价类
            while ((pDomainLink != null)
                    && (instances.instance(pDomainLink.index).value(indexAttribute)
                    != instances.instance(pLinkSet.index).value(indexAttribute))) {
                pDomainLink = pDomainLink.nextDomain;
            }

            // 没有找到的时候，pLinkSet自己构建一个等价类
            if (pDomainLink == null) {
                pLinkSet.nextDomain = pDomainStart;
                pLinkSet.nextEntry = null;
                pDomainStart = pLinkSet;
            } // 找到了，则直接将pLinkSet链接到相应的等价类
            else {
                pLinkSet.nextEntry = pDomainLink.nextEntry;
                pDomainLink.nextEntry = pLinkSet;
            }
            pLinkSet = pNextLink;
        }

        pDomainLink = pDomainStart;

        //对划分后的等价类递归操作
        while (pDomainLink != null) {
            pNextLink = pDomainLink.nextDomain;
            CalcNonEmptyLabelAttributesEx(index + 1, pDomainLink);
            pDomainLink = pNextLink;
        }
    }

    /**
     * @Name: CalcNonEmptyAttributesIndex @Description: TODO (Here is your
     * comment) 计算非空标签属性 存放在NonEmptyAttributesIndex中
     *
     * @throws @date 2011-5-22
     */
    private void CalcNonEmptyAttributesIndex() {
        int numNonEmptyAttributes = 0;
        for (int i = 0; i < isNonEmptyAttributes.length; i++) {

            if (isNonEmptyAttributes[i] == true) {
                numNonEmptyAttributes++;
            }
        }

        nonEmptyAttributes = new int[numNonEmptyAttributes];
        numNonEmptyAttributes = 0;

        for (int i = 0; i < isNonEmptyAttributes.length; i++) {
            if (isNonEmptyAttributes[i] == true) {
                nonEmptyAttributes[numNonEmptyAttributes++] = attributeOrders[i];
            }
        }
    }

    public int[] GetNonEmptyAttributes() {

        CalcNonEmptyLabelAttributes();

        return nonEmptyAttributes;
    }
}
