package datamining.core;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 数据集.
 *
 * @author ASUS
 */
public class Instances implements Serializable {

    private static final long serialVersionUID = 7042078639060079767L;
    private String relationName;
    private List<Attribute> attributes;
    private List<Instance> instances;
    private Map<Integer, Integer> sortMap;
    protected int classIndex;
    private int offset;

    /**
     * 初始化数据集.
     *
     * @param dataSet 数据集
     */
    public Instances(Instances dataSet) {
        this.relationName = dataSet.getRelationName();
        this.attributes = dataSet.getAttributes();
        this.instances = dataSet.getInstances();
        this.classIndex = this.attributes.size() - 1;
        this.offset = offset;
        sortMap = new HashMap<>();
        for (int i = 0; i < instances.size(); i++) {
            sortMap.put(i, i);
        }
    }

    /**
     * 初始化数据集.
     *
     * @param relationName　关系名
     * @param attribute 属性集合
     * @param instances 实例集合
     */
    public Instances(String relationName, List<Attribute> attributes, List<Instance> instances) {
        this.relationName = relationName;
        this.attributes = attributes;
        this.instances = instances;
        this.offset = 0;
        sortMap = new HashMap<>();
        this.classIndex = this.attributes.size() - 1;
        for (int i = 0; i < instances.size(); i++) {
            instances.get(i).setDataSet(this);
            sortMap.put(i, i);
        }
    }

    /**
     * 初始化数据集.
     *
     * @param name 关系名
     * @param attInfo 属性集合
     * @param capacity 初始容量
     */
    public Instances(String name, List<Attribute> attInfo, int capacity) {
        this.relationName = name;
        this.classIndex = -1;
        this.offset = 0;
        this.attributes = new ArrayList<>();
        for (int i = 0; i < attInfo.size(); i++) {
            attributes.add((Attribute) attInfo.get(i).clone());
        }
        this.classIndex = attributes.size() - 1;
        instances = new ArrayList<>();
        for (int i = 0; i < attributes.size(); i++) {
            attributes.get(i).setIndex(i);
        }
        sortMap = new HashMap<>();
        for (int i = 0; i < capacity; i++) {
            sortMap.put(i, i);
        }
        instances = new ArrayList<>(capacity);
    }

    /**
     * 增加新实例.
     *
     * @param inst 新实例
     */
    public void addInstance(Instance inst) {
        Instance newInst = (Instance) inst.clone();
        newInst.setDataSet(this);
        instances.add(inst);
    }

    /**
     * 设置属性集合.
     *
     * @param attributes 属性集合
     */
    public void setAttributes(List<Attribute> attributes) {
        this.attributes = attributes;
    }

    /**
     * 设置实例集合.
     *
     * @param instances 实例集合
     */
    public void setInstances(List<Instance> instances) {
        this.instances = instances;
    }

    /**
     * 取得属性集合.
     *
     * @return 属性集合
     */
    public List<Attribute> getAttributes() {
        return attributes;
    }

    /**
     * 取得实例集合.
     *
     * @return 实例集合
     */
    public List<Instance> getInstances() {
        return instances;
    }

    /**
     * 取得Relation名称.
     *
     * @return Relation名称
     */
    public String getRelationName() {
        return relationName;
    }

    /**
     * 返回类标号.
     *
     * @return 类标号
     */
    public int classIndex() {
        return classIndex;
    }

    /**
     * 返回属性数量.
     *
     * @return 属性数量
     */
    public int numAttributes() {
        return attributes.size();
    }

    /**
     * 返回实例数量.
     *
     * @return 实例数量
     */
    public int numInstances() {
        return instances.size();
    }

    /**
     * 返回属性值数量.
     *
     * @return 属性值数量
     */
    public int numClasses() {
        return attributes.get(numAttributes() - 1).getDecision().length;
    }

    /**
     * 取得分类属性.
     *
     * @return 分类属性
     */
    public Attribute classAttribute() {

        return attributes.get(classIndex);
    }

    /**
     * 取得对应Index的实例.
     *
     * @param index 实例Index
     * @return 对应Index的实例
     */
    public Instance instance(int index) {
        if (sortMap.get(index) == null) {
            return instances.get(index);
        }
        return instances.get(sortMap.get(index));
    }

    /**
     * 设置classIndex.
     *
     * @param classIndex 指定的classIndex
     */
    public void setClassIndex(int classIndex) {
        this.classIndex = classIndex;
        for (int i = 0; i < instances.size(); i++) {
            instances.get(i).setClassIndex(classIndex);
        }
    }

    /**
     * 根据ClassIndex对应的属性对实例集合排序.
     */
    public void sort() {
        sort(classIndex());
    }

    /**
     * 根据attIndex对应的属性对实例集合排序.
     *
     * @param attIndex 指定的属性索引
     */
    public void sort(int attIndex) {
        for (int i = 0; i < instances.size(); i++) {
            sortMap.put(i, i);
        }
        setClassIndex(classIndex);
        SortValues[] attrValues = new SortValues[instances.size()];
        for (int i = 0; i < attrValues.length; i++) {
            attrValues[i] = new SortValues();
            attrValues[i].index = i;
            attrValues[i].value = (instances.get(i).getAttrValues())[attIndex];
        }
        Arrays.sort(attrValues);
        for (int i = 0; i < attrValues.length; i++) {
            sortMap.put(i, attrValues[i].index);
        }
    }

    /**
     * 根据attIndex对应的属性对实例集合排序.
     *
     * @param att 要排序的属性
     */
    public void sort(Attribute att) {
        sort(att.getIndex());
    }

    private class SortValues implements Comparable<SortValues>, Serializable {

        private static final long serialVersionUID = 9054894629421451791L;
        int index;
        double value;

        @Override
        public int compareTo(SortValues o) {
            return Double.compare(value, o.value);
        }
    }

    /**
     * 取得数据集和Arff文件中的Decision属性的偏差值.
     *
     * @return 数据集和Arff文件中的Decision属性的偏差值
     */
    public int getOffset() {
        return offset;
    }

    /**
     * 设置数据集和Arff文件中的Decision属性的偏差值.
     *
     * @param offset 数据集和Arff文件中的Decision属性的偏差值
     */
    public void setOffset(int offset) {
        this.offset = offset;
    }

    /**
     * 克隆数据集.
     *
     * @return 克隆后的新数据集
     */
    @Override
    public Instances clone() {
        Instances newInst = null;
        try {
            newInst = (Instances) cloneObject(this);
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(Instances.class.getName()).log(Level.SEVERE, null, ex);
        }
        return newInst;
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
