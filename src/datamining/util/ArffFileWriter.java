package datamining.util;

import datamining.core.Attribute;
import datamining.core.Instance;
import datamining.core.Instances;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * 负责Arff文件写入工作.<br/>把数据及写入Arff文件.
 *
 * @author LiuGuining
 */
public class ArffFileWriter {

    private static final String ATTRIBUTE_FLAG = "@attribute";
    private static final String DATA_FLAG = "@data";
    private static final String RELATION_FLAG = "@relation";
    private static final String NUMERIC = "numeric";
    private File file;
    private Instances dataSet;
    private BufferedWriter writer = null;

    /**
     * 初始化ArffFileWriter.
     *
     * @param file 欲写入的文件
     */
    public ArffFileWriter(File file) {
        this.file = file;
    }

    /**
     * 初始化ArffFileWriter.
     *
     * @param filename 欲写入的文件名
     */
    public ArffFileWriter(String filename) {
        this.file = new File(filename);
    }

    /**
     * 将数据集写入文件.
     *
     * @param dataSet 欲写入的数据集合
     * @throws IOException
     */
    public void write(Instances dataSet) throws IOException {
        write(dataSet, dataSet.getOffset());
    }

    /**
     * 将数据集写入文件.
     *
     * @param dataSet 欲写入的数据集合
     * @param offset 数据集和Arff文件中的Decision属性的偏差值
     * @throws IOException
     */
    public void write(Instances dataSet, int offset) throws IOException {
        writer = new BufferedWriter(new FileWriter(file));
        //输出 Relation
        writer.append(new StringBuffer(RELATION_FLAG).append(" ").append(dataSet.getRelationName()));
        writer.newLine();
        //输出Attribute
        List<Attribute> attrs = dataSet.getAttributes();
        for (Attribute attribute : attrs) {
            StringBuffer sb = new StringBuffer(ATTRIBUTE_FLAG);
            sb.append(" ");
            sb.append(attribute.getAttributeName());
            sb.append(" ");
            if (attribute.getType() == Attribute.NUMERIC) {
                sb.append(NUMERIC);
            } else {

                sb.append("{ ");
                int[] decision = attribute.getDecision();
                for (int i : decision) {
                    sb.append((i + offset));
                    sb.append(" ");
                }
                sb.append("}");

            }
            writer.append(sb);
            writer.newLine();
        }
        //输出data
        writer.append(DATA_FLAG);
        writer.newLine();
        List<Instance> insts = dataSet.getInstances();
        for (Instance instance : insts) {
            StringBuffer sb = new StringBuffer();
            double[] attrValues = instance.getAttrValues();
            for (int i = 0; i < attrValues.length - 1; i++) {
                sb.append(attrValues[i]);
                sb.append(",");
            }
            sb.append((int) attrValues[attrValues.length - 1] + offset);
            writer.append(sb);
            writer.newLine();
        }
        flush();
        close();
    }

    /**
     * 刷新Writer.
     *
     * @throws IOException
     */
    public void flush() throws IOException {
        if (writer != null) {
            writer.flush();
        }
    }

    /**
     * 关闭Writer.
     *
     * @throws IOException
     */
    public void close() throws IOException {
        if (writer != null) {
            writer.close();
        }
    }
}
