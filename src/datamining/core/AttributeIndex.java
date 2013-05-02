package datamining.core;

/**
 * 数据集属性索引.
 *
 * @author lenovo
 */
public class AttributeIndex implements Comparable<AttributeIndex> {

    public int index;
    public double value;
    public double noise = 0;
    public boolean[] attrNoise = null;
    public static int order = 0;

    /**
     * 取得数据集属性索引.
     *
     * @return 数据集属性索引
     * 获取噪音数据？
     */
    public boolean[] getAttrNoise() {

        return attrNoise;
    }

    /**
     * 设置数据集属性索引.
     *
     * @param attrNoise 数据集属性索引
     */
    public void setAttrNoise(boolean[] attrNoise) {
        this.attrNoise = new boolean[attrNoise.length];
        System.arraycopy(attrNoise, 0, this.attrNoise, 0, attrNoise.length);
    }

    @Override
    public int compareTo(AttributeIndex arg0) {
        if (AttributeIndex.order == 0) {
            if (this.value > arg0.value) {
                return 1;
            } else if (this.value < arg0.value) {
                return -1;
            } else {
                if (this.noise > arg0.noise) {
                    return 1;
                } else if (this.noise < arg0.noise) {
                    return -1;
                } else {
                    return 0;
                }
            }
        } else {
            if (this.value > arg0.value) {
                return -1;
            } else if (this.value < arg0.value) {
                return 1;
            } else {
                if (this.noise > arg0.noise) {
                    return -1;
                } else if (this.noise < arg0.noise) {
                    return 1;
                } else {
                    return 0;
                }
            }
        }

    }
}
