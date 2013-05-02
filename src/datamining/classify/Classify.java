/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package datamining.classify;

import datamining.attributeselection.IAttributeSelection;
import datamining.core.Instances;
import java.util.Properties;

/**
 * Classify接口.
 *
 * @author lenovo
 */
public interface Classify {

    /**
     * 交叉验证.
     *
     * @param instances 数据集
     * @param fold 交叉次数
     * @param times 重叠次数
     * @return 根据部分数据预测结果相较于原始结果的正确率
     * @throws Exception
     */
    public double crossTest(Instances instances, int fold, int times) throws Exception;

    /**
     * 取得数据集的平均预测结果.
     *
     * @param instances　数据集
     * @return 数据集的平均预测结果
     */
    public double evalScore(Instances instances);

    /**
     * 取得分类器参数设置.
     *
     * @return 分类器参数集合
     */
    public Properties getprProperties();

    /**
     * 判断分类器是否可设置参数.
     *
     * @return 是否可设置参数
     */
    public boolean hasProperties();

    /**
     * 设置分类器参数.
     *
     * @param prop 分类器参数集合
     */
    public void setProperties(Properties prop);

    /**
     * 交叉验证
     *
     * @param instances 数据集
     * @param fold 交叉次数
     * @param times 重叠次数
     * @param attributeSelector 特征选择方法
     * @return 根据部分数据预测结果相较于原始结果的正确率
     * @throws Exception
     */
    public double crossTest(Instances instances, int fold, int times, IAttributeSelection attributeSelector) throws Exception;
}
