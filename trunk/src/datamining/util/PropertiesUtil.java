package datamining.util;

import datamining.attributeselection.IAttributeSelection;
import datamining.classify.Classify;
import datamining.rough.Discretization;
import datamining.rough.ValueReduction;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 属性文件读取集合.<br/>从属性文件中读取相应的信息.
 *
 * @author LiuGuining
 */
public class PropertiesUtil {

    public static final int CLASSIFIER = 0;
    public static final int ATTIBUTE_SELECTION = 1;
    public static final int DISCRETIZATION = 2;
    public static final int VALUEREDUCTION = 3;
    private static final String[] PROPFILE = {"classifier.properties",
        "attribute_selection.properties", "rough_discretization.properties",
        "rough_valuereduction.properties"};

    /**
     * 根据AttributeSelection名称取得实例.
     *
     * @param attributeSelectionName AttributeSelection名称
     * @return 对应的AttributeSelection实例
     */
    public static IAttributeSelection getAttributeSelection(String attributeSelectionName) {
        IAttributeSelection attributeSelection = null;
        try {
            attributeSelection = (IAttributeSelection) getInstance(attributeSelectionName, ATTIBUTE_SELECTION);
        } catch (IOException | InstantiationException | ClassNotFoundException | IllegalAccessException ex) {
            Logger.getLogger(PropertiesUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return attributeSelection;
    }

    /**
     * 根据classifier名称取得实例.
     *
     * @param classifierName classifier名称
     * @return 对应的classifier实例
     */
    public static Classify getClassifier(String classifierName) {
        Classify classifer = null;
        try {
            classifer = (Classify) getInstance(classifierName, CLASSIFIER);
        } catch (IOException | InstantiationException | ClassNotFoundException | IllegalAccessException ex) {
            Logger.getLogger(PropertiesUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return classifer;
    }

    /**
     * 根据Discretization名称取得实例.
     *
     * @param discretizationName Discretization名称
     * @return 对应的Discretization实例
     */
    public static Discretization getDiscretization(String discretizationName) {
        Discretization discrete = null;
        try {
            discrete = (Discretization) getInstance(discretizationName, DISCRETIZATION);
        } catch (IOException | InstantiationException | ClassNotFoundException | IllegalAccessException ex) {
            Logger.getLogger(PropertiesUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return discrete;
    }

    /**
     * 根据valueReduction名称取得实例.
     *
     * @param valueReductionName valueReduction名称
     * @return 对应的valueReduction实例
     */
    public static ValueReduction getValueReduction(String valueReductionName) {
        ValueReduction value = null;
        try {
            value = (ValueReduction) getInstance(valueReductionName, VALUEREDUCTION);
        } catch (IOException | InstantiationException | ClassNotFoundException | IllegalAccessException ex) {
            Logger.getLogger(PropertiesUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return value;
    }

    /**
     * 根据实力强名称取得对应实例.
     *
     * @param instancesName 实力派名称
     * @param index 实例类型索引
     * @return 对应的实例
     * @throws IOException
     * @throws InstantiationException
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     */
    private static Object getInstance(String instancesName, int index)
            throws IOException, InstantiationException, ClassNotFoundException, IllegalAccessException {
        InputStream input = PropertiesUtil.class.getResourceAsStream(PROPFILE[index]);
        Properties prop = new Properties();
        prop.load(input);
        String className = prop.getProperty(instancesName);
        return Class.forName(className).newInstance();
    }

    /**
     * 取得索引对饮类型的所有实例名称
     *
     * @param index 实例索引
     * @return 索引对饮类型的所有实例名称
     */
    public static Vector<String> getPropItems(int index) {
        Vector<String> items = new Vector<>();
        try {
            InputStream input = PropertiesUtil.class.getResourceAsStream(PROPFILE[index]);
            Properties prop = new Properties();
            prop.load(input);
            Iterator<Entry<Object, Object>> iter = prop.entrySet().iterator();
            while (iter.hasNext()) {
                Entry<Object, Object> entry = iter.next();
                items.add((String) entry.getKey());
            }
        } catch (IOException ex) {
            Logger.getLogger(PropertiesUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return items;
    }
}
