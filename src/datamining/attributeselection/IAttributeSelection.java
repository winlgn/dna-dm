package datamining.attributeselection;

import datamining.core.Instances;

/**
 * AttributeSelection接口.
 *
 * @author LiuGuining
 */
public interface IAttributeSelection {

    /**
     * 取得权值较优的前K个属性.
     *
     * @param instances 数据集
     * @param K 取得属性个数
     * @return 数据集中权值较优的前K个属性索引
     */
    int[] getFirstKAttributes(Instances instances, int K);
}
