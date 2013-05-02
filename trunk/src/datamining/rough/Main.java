/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package datamining.rough;

import datamining.core.Instances;
import datamining.util.ArffFileReader;
import java.io.File;

/**
 *
 * @author lenovo
 */
public class Main {

    public static void main(String[] args) throws Exception {

        System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");

        File f = new File("D:" + File.separator + "colon.arff");
        ArffFileReader reader = new ArffFileReader(f);
        Instances inst = reader.getDataSet();
//
//        TreeClassify tc = new TreeClassify();
        RuleClassify rc = new RuleClassify();
//
        InstancesBreakPoint instancesBreakPoint = null;

        DiscreteBasedOnEntropy de = new DiscreteBasedOnEntropy();
        instancesBreakPoint = de.discrete(inst);

        inst = de.getDiscretizationInstances(inst, instancesBreakPoint);
        for (int i = 0; i < inst.numInstances(); i++) {
            for (int j = 0; j < inst.numAttributes(); j++) {
                System.out.print(inst.instance(i).value(j) + "\t");
            }
            System.out.print("\n");
        }
        System.out.println(inst.numAttributes());
//        for (int i = 0; i < inst.numInstances(); i++) {
//            for (int j = 0; j < inst.numAttributes(); j++) {
//                System.out.print(inst.instance(i).value(j) + "\t");
//            }
//            System.out.print("\n");
//        }
//属性约简
        //基于属性序的属性约简算法
        AttributeReduction ar = new AttributeReduction();
        int[] indexs = ar.getReducedAttributeByAttributeOrder(inst);



        //获得新的决策表对象
        inst = Util.reduceInstancesButClassAttribute(inst, indexs);
        for (int i = 0; i < inst.numInstances(); i++) {
            for (int j = 0; j < inst.numAttributes(); j++) {
                System.out.print(inst.instance(i).value(j) + "\t");
            }
            System.out.print("\n");
        }
//        double res = rc.evalScore(inst);
        double res = rc.crossTest(inst, 10, 2);
        System.out.print(res);
    }
}
