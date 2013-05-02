/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package datamining.rough;

import datamining.core.Attribute;
import datamining.core.Instance;
import datamining.core.Instances;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author lenovo
 */
public class Util {

    public static Instances reduceInstancesButClassAttribute(Instances instances, int index[]) throws Exception {
        int numAttributes = index.length;

        if (index.length >= instances.numAttributes()) {
            throw new Exception("Error Util reduceInstances line 24: Can't reduceInstances");
        }

        List<Attribute> afterAttr = new ArrayList<>();
        afterAttr = instances.getAttributes();
        Instances AfterInstances = new Instances("ReducedInstances", afterAttr, instances.numInstances());
        AfterInstances.setClassIndex(numAttributes);


        for (int i = 0; i < instances.numInstances(); i++) {
            double[] attrValue = new double[numAttributes + 1];
            for (int j = 0; j < numAttributes; j++) {
                attrValue[j] = instances.instance(i).value(index[j]);
            }
            attrValue[numAttributes] = instances.instance(i).classValue();
            Instance instanceValue = new Instance(1, attrValue);
//            instanceValue.setDataset(AfterInstances);
            AfterInstances.addInstance(instanceValue);
        }
        return AfterInstances;
    }

    public static Instances NormalizationInstances(Instances instances) {
        double minValue, maxValue;
        for (int i = 0; i < instances.numAttributes() - 1; i++) {
            minValue = Double.MAX_VALUE;
            maxValue = -Double.MAX_VALUE;

            for (int j = 0; j < instances.numInstances(); j++) {
                minValue = Math.min(minValue, instances.instance(j).value(i));
                maxValue = Math.max(maxValue, instances.instance(j).value(i));
            }

            for (int j = 0; j < instances.numInstances(); j++) {
                instances.instance(j).setValue(i, (instances.instance(j).value(i) - minValue) / (maxValue - minValue));
            }

        }
        return instances;
    }

    public static Instances ReduceInstances(Instances instances, int indexs[]) throws Exception {
        int numAttributes = indexs.length;

        if (indexs.length >= instances.numAttributes()) {
            throw new Exception("Error Util reduceInstances line 24: Can't reduceInstances");
        }

        List<Attribute> afterAttr = new ArrayList<>();
        afterAttr = instances.getAttributes();
        Instances AfterInstances = new Instances("ReducedInstances", afterAttr, instances.numInstances());
        AfterInstances.setClassIndex(numAttributes);


        for (int i = 0; i < instances.numInstances(); i++) {
            double[] attrValue = new double[numAttributes + 1];
            for (int j = 0; j < numAttributes; j++) {
                attrValue[j] = instances.instance(i).value(indexs[j]);
            }
            attrValue[numAttributes] = instances.instance(i).classValue();
            Instance instanceValue = new Instance(1, attrValue);
            instanceValue.setDataSet(AfterInstances);
            AfterInstances.addInstance(instanceValue);
        }
        return AfterInstances;
    }
}
