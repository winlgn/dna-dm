package datamining.util;

import java.util.HashMap;

/**
 * 序列统计.
 *
 * @author LiuGuining
 */
public class SequenceUtil {

    private String key[] = null;
    private String data[] = null;

    /**
     * 初始化SequenceUtil.
     *
     * @param data data集
     * @param key data集对应的key值
     */
    public SequenceUtil(String[] data, String[] key) {
        this.data = data;
        this.key = key;
    }

    /**
     * 统计data集.
     *
     * @return data集各元素的分布情况
     */
    public HashMap<String, Integer> getStat() {
        HashMap<String, Integer> map = new HashMap<>();
        for (String string : key) {
            map.put(string, 0);
        }
        for (String string : data) {
            int count = map.get(string);
            count++;
            map.put(string, count);
        }
        return map;
    }
}
