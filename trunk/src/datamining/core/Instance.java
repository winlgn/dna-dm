package datamining.core;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 数据集实例.
 *
 * @author LiuGuining
 */
public class Instance implements Cloneable, Serializable {

    private static final long serialVersionUID = 7719864626444161277L;
    private Instances dataSet;//数据集
    private double[] attrValues;//属性值
    private double weight;//权重
    private int classIndex = -1;

    /**
     * 初始化实例.
     */
    public Instance() {
    }

    /**
     * 初始化实例.
     *
     * @param instance 实例
     */
    public Instance(Instance instance) {
        this.attrValues = instance.getAttrValues();
        this.weight = instance.getWeight();
        dataSet = null;
    }

    /**
     * 初始化实例.
     *
     * @param weight 权重
     * @param attrValues 实例数据
     */
    public Instance(double weight, double[] attrValues) {
        this.attrValues = attrValues;
        this.weight = weight;
        dataSet = null;
    }

    /**
     * 设置属性数据.
     *
     * @param attIndex 数据索引
     * @param value 值
     */
    public void setValue(int attIndex, double value) {
        attrValues[attIndex] = value;
    }

    /**
     * 设置实例数据.
     *
     * @param attrValues 实例数据
     */
    public void setAttrValues(double[] attrValues) {
        this.attrValues = attrValues;
    }

    /**
     * 取得实例数据.
     *
     * @return 实例数据
     */
    public double[] getAttrValues() {
        return attrValues;
    }

    /**
     * 取得实例权重.
     *
     * @return 实例权重
     */
    public double getWeight() {
        return weight;
    }

    /**
     * 设置分类索引.
     *
     * @param classIndex 分类索引
     */
    public void setClassIndex(int classIndex) {
        this.classIndex = classIndex;
    }

    /**
     * 判断指定数据有无丢失.
     *
     * @param attIndex 数据索引
     * @return 指定数据有无丢失
     */
    public boolean isMissing(int attIndex) {
        if (Double.isNaN(attrValues[attIndex])) {
            return true;
        }
        return false;
    }

    /**
     * 设置指定数据为丢失.
     *
     * @param attIndex 数据索引
     */
    public void setMissing(int attIndex) {
        setValue(attIndex, Double.NaN);
    }

    /**
     * 取得实例包含属性总数.
     *
     * @return 实例包含属性总数
     */
    public int numAttributes() {
        return attrValues.length;
    }

    /**
     * 返回指定编号的值.
     *
     * @param Index　指定编号
     * @return 指定编号的值
     */
    public double value(int Index) {
        if (Index < 0) {
            return attrValues[attrValues.length - 1];
        }
        return attrValues[Index];
    }

    /**
     * 取得类标签.
     *
     * @return 分类索引
     */
    public double classValue() {
        return value(classIndex());
    }

    /**
     * 取得类标签索引.
     *
     * @return 分类索引
     */
    public int classIndex() {
        return classIndex;
    }

    /**
     * 取得实例所属的数据集.
     *
     * @param dataSet 实例所属的数据集
     */
    public void setDataSet(Instances dataSet) {
        this.dataSet = dataSet;
    }

    /**
     * 取得克隆的实例.
     *
     * @return 克隆的实例
     */
    @Override
    public Instance clone() {
        Instance newInstance = null;
        try {
            newInstance = (Instance) cloneObject(this);
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(Instance.class.getName()).log(Level.SEVERE, null, ex);
        }
        return newInstance;
    }

    private Object cloneObject(Object obj) throws IOException, ClassNotFoundException {
        ObjectOutputStream out;
        ByteArrayInputStream byteIn;
        ObjectInputStream in;
        Object object;

        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        out = new ObjectOutputStream(byteOut);
        out.writeObject(obj);
        byteIn = new ByteArrayInputStream(byteOut.toByteArray());
        in = new ObjectInputStream(byteIn);
        object = in.readObject();
        out.close();
        in.close();
        return object;
    }
}
