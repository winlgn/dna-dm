/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package datamining.rough;

import datamining.core.Instances;

/**
 * 基于属性序的属性约简算法
 *
 * @author lenovo
 */
public class AttributeReduction {

    private int[] attributeOrders;
    private boolean[] isInstaiceInPositives;
    public int numPositiveInstance;
    public int[] attrbuteRedcutions;

    public int[] reductionByAttributeOrder(Instances instances) {
        attributeOrders = new int[instances.numAttributes() - 1];
        for (int i = 0; i < instances.numAttributes() - 1; i++) {
            attributeOrders[i] = i;
        }
        return ReductionByAttributeOrder(instances, attributeOrders);
    }

    public int[] ReductionByAttributeOrder(Instances instances, int[] attributeOrders) {
        //获得正区域信息
        PositiveRegion positiveRegion = new PositiveRegion();
        positiveRegion.CalcPostiveRegionByDivideAndConquer(instances);
        isInstaiceInPositives = positiveRegion.GetPositiveInstanceLabels();
        numPositiveInstance = positiveRegion.GetPositiveInstanceNumber();

        NonEmptyLableAttribute nonEmptyLableAttribute = new NonEmptyLableAttribute();
        int[] oldAttributeOrders = null;
        int[] newAttrbuteOrders = null;


        //首先计算一次非空标签
        oldAttributeOrders = attributeOrders;
        nonEmptyLableAttribute.SetInformations(instances, oldAttributeOrders, isInstaiceInPositives, numPositiveInstance);;
        newAttrbuteOrders = nonEmptyLableAttribute.GetNonEmptyAttributes();

        //迭代次数
        int numIteration = newAttrbuteOrders.length - 1;

        while (true) {
            //如果只有一个非空标签属性
            if (newAttrbuteOrders.length == 1) {
                break;
            }

            //将后面一个标签大的属性放到前面去
            oldAttributeOrders = new int[newAttrbuteOrders.length];
            for (int i = oldAttributeOrders.length - 1; i >= 1; i--) {
                oldAttributeOrders[i] = newAttrbuteOrders[i - 1];
            }
            oldAttributeOrders[0] = newAttrbuteOrders[newAttrbuteOrders.length - 1];

            //计算调换顺序后的非空标签属性
            nonEmptyLableAttribute.SetInformations(instances, oldAttributeOrders, isInstaiceInPositives, numPositiveInstance);;
            newAttrbuteOrders = nonEmptyLableAttribute.GetNonEmptyAttributes();

            //如果有属性被删除，那么迭代次数会相应减少
            numIteration = numIteration - (oldAttributeOrders.length - newAttrbuteOrders.length);

            numIteration--;

            if (numIteration <= 0) {
                break;
            }
        }

        attrbuteRedcutions = newAttrbuteOrders;

        return attrbuteRedcutions;
    }

    public int[] getReducedAttributeByAttributeOrder(Instances instances) {
        AttributeReduction attributeReduction = new AttributeReduction();
        int[] indexs = attributeReduction.reductionByAttributeOrder(instances);
        return indexs;
    }
}
