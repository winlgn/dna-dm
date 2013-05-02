package datamining.core;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 数据集属性.
 *
 * @author LiuGuining
 */
public class Attribute implements Cloneable, Serializable {

    private static final long serialVersionUID = -8749052559742121869L;
    private String AttributeName;//属性名称
    private int[] decision;//decision
    private int index;//属性索引值
    private int type;//类型
    /**
     * 属性类型:数值型.
     */
    public static final int NUMERIC = 1;
    /**
     * 属性类型:决策索引型.
     */
    public static final int DECISION = 2;

    /**
     * 初始化属性.
     */
    public Attribute() {
        this.index = -1;
        this.type = NUMERIC;
    }

    /**
     * 初始化属性.
     *
     * @param AttributeName 属性名称
     */
    public Attribute(String AttributeName) {
        this();
        this.AttributeName = AttributeName;
    }

    /**
     * 初始化属性.
     *
     * @param AttributeName 属性名称
     * @param type 属性类型
     */
    public Attribute(String AttributeName, int type) {
        this(AttributeName);
        this.type = type;
    }

    /**
     * 取得属性index
     *
     * @return 属性index
     */
    public int getIndex() {
        return index;
    }

    /**
     * 设置属性index
     *
     * @param index 属性index
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * 取得属性名称.
     *
     * @return 属性名称
     */
    public String getAttributeName() {
        return AttributeName;
    }

    /**
     * 设置属性名称.
     *
     * @param AttributeName 属性名称
     */
    public void setAttributeName(String AttributeName) {
        this.AttributeName = AttributeName;
    }

    /**
     * 取得属性类型.
     *
     * @return 属性类型
     */
    public int getType() {
        return type;
    }

    /**
     * 设置属性类型.
     *
     * @param type 属性类型
     */
    public void setType(int type) {
        this.type = type;
    }

    /**
     * 取得决策索引.
     *
     * @return 决策索引
     */
    public int[] getDecision() {
        return decision;
    }

    /**
     * 设置决策索引.
     *
     * @param decision 决策索引
     */
    public void setDecision(int[] decision) {
        this.decision = decision;
    }

    /**
     * 取得克隆的属性.
     *
     * @return 克隆的属性
     */
    @Override
    public Object clone() {
        Object o = null;
        try {
            o = (Attribute) super.clone();
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(Attribute.class.getName()).log(Level.SEVERE, null, ex);
        }
        return o;
    }
}
