package datamining.util;

import datamining.core.Attribute;
import datamining.core.Instance;
import datamining.core.Instances;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 负责Arff文件读取工作. <br/> 读取Arff文件，并将文件内容转换成可供其他类使用的数据集合.
 *
 * @author LiuGuining
 */
public class ArffFileReader {

    private static final String ATTRIBUTE_FLAG = "@attribute";
    private static final String DATA_FLAG = "@data";
    private static final String RELATION_FLAG = "@relation";
    private static final String DECISION = "decision";
    private File file;
    private String relationName = null;
    private List<Attribute> attributes = null;
    private List<Instance> instances = null;
    private Instances dataSet = null;
    private int offset;

    /**
     * 初始化ArffFileReader.
     *
     * @param file 欲读取的Arff文件
     */
    public ArffFileReader(File file) {
        this.file = file;
    }

    /**
     * 初始化ArffFileReader.
     *
     * @param filename 欲读取的Arff文件名
     */
    public ArffFileReader(String filename) {
        this.file = new File(filename);
    }

    /**
     * 取得数据集Relation名称.
     *
     * @return 数据集Relation名称
     */
    public String getRelationName() {
        if (relationName == null) {
            relationName = getRelationName(file);
        }
        return relationName;
    }

    /**
     * 取得数据集属性集合.
     *
     * @return 数据集属性集合
     */
    public List<Attribute> getAttributes() {
        if (attributes == null) {
            attributes = getAttributes(file);
        }
        return attributes;
    }

    /**
     * 取得数据集实例集合.
     *
     * @return 数据集实例集合
     */
    public List<Instance> getInstances() {
        if (instances == null) {
            instances = getInstances(file);
        }
        return instances;
    }

    /**
     * 取得数据集
     *
     * @return 数据集
     */
    public Instances getDataSet() {
        if (dataSet == null) {
            dataSet = new Instances(getRelationName(), getAttributes(), getInstances());
            dataSet.setOffset(offset);
        }

        return dataSet;
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
     * 取得取得数据集Relation名称.
     *
     * @param f 要读取的Arff文件
     * @return 取得数据集Relation名称
     */
    private String getRelationName(File f) {
        String tmp;
        String relation = null;
        int start = -1;
        int end = -1;
        try {
            try (BufferedReader reader = new BufferedReader(new FileReader(f))) {
                tmp = reader.readLine();
                while (tmp != null) {
                    if (tmp.toLowerCase().startsWith(RELATION_FLAG)) {
                        tmp = tmp.replaceAll("^@\\w+\\s++", "");
                        start = tmp.indexOf('\'');
                        end = tmp.indexOf('\'', start + 1);
                        if (start >= 0) {
                            relation = tmp.substring(start + 1, end).trim();
                        } else {
                            relation = tmp.trim();
                        }
                        break;
                    }
                    tmp = reader.readLine();
                }
            }
            System.gc();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex1) {
            ex1.printStackTrace();
        }
        return relation;
    }

    /**
     * 初始化属性集合.
     *
     * @param f 读取的Arff文件
     * @return 完成初始化的属性集合
     */
    private List<Attribute> getAttributes(File f) {
        List<Attribute> attrs = new ArrayList<>();
        String tmp;
        int index = 0;
        try {
            try (BufferedReader reader = new BufferedReader(new FileReader(f))) {
                while ((tmp = reader.readLine()) != null) {
                    String attr = null;
                    if (tmp.toLowerCase().startsWith(ATTRIBUTE_FLAG)) {
                        //获取attribute值
                        if (tmp.matches("^@\\w+\\s+'.*")) {
                            int flag_s = tmp.indexOf('\'');
                            int flag_e = tmp.indexOf('\'', flag_s + 1);
                            if (flag_s >= 0) {
                                attr = tmp.substring(flag_s + 1, flag_e).trim();
                            }
                        } else {
                            String[] attr_tmp = tmp.trim().split("\\s+");
                            attr = attr_tmp[1];
                        }
                        Attribute a = new Attribute(attr);
                        a.setIndex(index++);
                        //读取decision内容
                        if (DECISION.equalsIgnoreCase(attr)) {
                            int flag_s = tmp.indexOf('{');
                            int flag_e = tmp.indexOf('}', flag_s + 1);
                            String decision = tmp.substring(flag_s + 1, flag_e).trim();
                            String[] desc_tmp;
                            if (decision.contains(",")) {
                                desc_tmp = decision.split(",");
                            } else {
                                desc_tmp = decision.split("\\s+");
                            }

                            int[] desc = new int[desc_tmp.length];
                            for (int i = 0; i < desc.length; i++) {
                                desc[i] = Integer.parseInt(desc_tmp[i].trim());
                            }
                            Arrays.sort(desc);
                            offset = desc[0];
                            if (offset != 0) {
                                for (int i = 0; i < desc.length; i++) {
                                    desc[i] -= offset;

                                }
                            }
                            a.setType(Attribute.DECISION);
                            a.setDecision(desc);
                        }
                        attrs.add(a);
                    } else if (tmp.toUpperCase().startsWith(DATA_FLAG)) {
                        break;
                    }
                }
            }
            System.gc();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex1) {
            ex1.printStackTrace();
        }
        return attrs;
    }

    /**
     * 初始化实例集合.
     *
     * @param f 要读取的Arff文件
     * @return 完成初始化的实例集合
     */
    private List<Instance> getInstances(File f) {
        List<Instance> insts = new ArrayList<>();
        String tmp;
        int index = 0;
        try {
            try (BufferedReader reader = new BufferedReader(new FileReader(f))) {
                tmp = reader.readLine();
                while (tmp != null) {
                    if (tmp.toLowerCase().startsWith(DATA_FLAG)) {
                        break;
                    }
                    tmp = reader.readLine();
                }
                tmp = reader.readLine();
                while (tmp != null) {
                    if (tmp.matches("\\s*%.*|\\s*")) {
                        tmp = reader.readLine();
                        continue;
                    }
                    String[] data_tmp;
                    if (tmp.contains(",")) {
                        data_tmp = tmp.split(",");
                    } else {
                        data_tmp = tmp.split("\\s+");
                    }
                    double[] data = new double[data_tmp.length];
                    for (int i = 0; i < data_tmp.length - 1; i++) {
                        data[i] = Double.parseDouble(data_tmp[i].trim());
                    }
                    data[data_tmp.length - 1] =
                            Double.parseDouble(data_tmp[data_tmp.length - 1].trim()) - offset;
                    Instance inst = new Instance(0, data);
                    insts.add(inst);

                    tmp = reader.readLine();
                }
            }
            System.gc();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex1) {
            ex1.printStackTrace();
        }
        return insts;
    }
}
