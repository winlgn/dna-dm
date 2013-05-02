package datamining.util;

import java.util.Arrays;

/**
 * 数学工具集.<br/>负责数组基本统计.
 *
 * @author LiuGuining
 */
public class MathUtil {

    private double data[];
    private double original_data[];

    /**
     * 初始化MathUtil.
     *
     * @param data 欲统计的double数组
     */
    public MathUtil(double[] data) {
        this.original_data = data;
        this.data = data.clone();
        Arrays.sort(this.data);
    }

    /**
     * 取得数组最大值.
     *
     * @return 数组最大值
     */
    public double getMax() {
        return data[data.length - 1];
    }

    /**
     * 取得数组最小值.
     *
     * @return 数组最小值
     */
    public double getMin() {
        return data[0];
    }

    /**
     * 取得数组平均值.
     *
     * @return 数组平均值
     */
    public double getMean() {
        double mean = 0;
        for (double d : data) {
            mean += d;
        }
        return mean / data.length;
    }

    /**
     * 取得数组标准差.
     *
     * @return 数组标准差
     */
    public double getStdDev() {
        double mean = this.getMean();
        double sum = 0;
        for (double d : data) {
            sum += (d - mean) * (d - mean);
        }
        return Math.sqrt(sum / data.length);
    }

    /**
     * 取得数组非重复值个数.
     *
     * @return 数组非重复值个数
     */
    public int getUnique() {
        if (data == null || data.length == 0) {
            return -1;
        }
        if (data.length == 1) {
            return 1;
        }//数组长度为1时，直接返回1
        int count = 1;
        double tmp = data[0];
        boolean flag = true;
        for (int i = 1; i < data.length; i++) {
            if (Double.compare(tmp, data[i]) != 0) {
                count++;
                flag = true;
            } else {
                if (flag) {
                    count--;
                    flag = false;
                }
            }
            tmp = data[i];
        }
        return count;
    }

    /**
     * 返回数组长度.
     *
     * @return 数组长度
     */
    public int length() {
        return data.length;
    }

    /**
     * 取得标准化后的数组.
     *
     * @param new_max 新数组的最大值
     * @param new_min 新数组的最小值
     * @return 标准化后的数组
     */
    public double[] normalize(double new_max, double new_min) {
        double result[] = new double[original_data.length];
        double min = this.getMin();
        double max = this.getMax();
        double tmp;
        tmp = (new_max - new_min) / (max - min);
        for (int i = 0; i < original_data.length; i++) {
            result[i] = (original_data[i] - min) * tmp + new_min;
        }
        return result;
    }
}
