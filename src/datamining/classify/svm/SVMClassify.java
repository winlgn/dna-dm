/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package datamining.classify.svm;

import datamining.attributeselection.IAttributeSelection;
import datamining.classify.Classify;
import datamining.core.Attribute;
import datamining.core.Instance;
import datamining.core.Instances;
import datamining.rough.Util;
import datamining.util.ArffFileReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author lenovo
 */
class SVMModel {

    private Parameter parameter;
    private Problem problem;
    private Model model;
    double threshold;
    int selectedNumber;
    int[] selectedAttributes;
    private Properties prop = null;

    Properties getProperties() {
        return prop;
    }

    void setProperties(Properties prop) {
        this.prop = prop;
    }
    private boolean flag = false;

    public SVMModel() {
        if (prop == null) {
            InputStream input = SVMClassify.class.getResourceAsStream("svm_prop.properties");
            prop = new Properties();
            try {
                prop.load(input);
            } catch (IOException ex) {
                Logger.getLogger(SVMModel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private Parameter getParameter(Instances instances) {
        Parameter tmpParameter = new Parameter();
        tmpParameter.svm_type = Parameter.C_SVC;
        tmpParameter.kernel_type = Parameter.RBF;
        tmpParameter.degree = 3;
        tmpParameter.gamma = 1.0 / (selectedNumber + 1);
        tmpParameter.coef0 = 0;
        tmpParameter.nu = 0.5;
        tmpParameter.cache_size = 500;
        tmpParameter.C = 100;
        tmpParameter.eps = 1e-3;
        tmpParameter.p = 0.1;

        tmpParameter.shrinking = 1;
        tmpParameter.probability = 0;
        tmpParameter.nr_weight = 0;
        tmpParameter.weight_label = new int[0];
        tmpParameter.weight = new double[0];
        return tmpParameter;
    }

    private Parameter getParameter() throws
            NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        if (!flag) {
            Parameter tmpParameter = new Parameter();
            Class<?> c = tmpParameter.getClass();

            tmpParameter.svm_type = c.getDeclaredField(prop.getProperty("svm_type")).getInt(c);
            tmpParameter.kernel_type = c.getDeclaredField(prop.getProperty("kernel_type")).getInt(c);
            tmpParameter.degree = Integer.parseInt(prop.getProperty("degree"));

            if (Double.compare(0, Double.parseDouble(prop.getProperty("gamma"))) == 0) {
                tmpParameter.gamma = 1.0 / (selectedNumber + 1);
            } else {
                tmpParameter.gamma = Double.parseDouble(prop.getProperty("gamma"));
            }

            tmpParameter.coef0 = Double.parseDouble(prop.getProperty("coef0"));
            tmpParameter.nu = Double.parseDouble(prop.getProperty("nu"));
            tmpParameter.cache_size = Double.parseDouble(prop.getProperty("cache_size"));
            tmpParameter.C = Double.parseDouble(prop.getProperty("C"));
            tmpParameter.eps = Double.parseDouble(prop.getProperty("eps"));
            tmpParameter.p = Double.parseDouble(prop.getProperty("p"));

            tmpParameter.shrinking = Integer.parseInt(prop.getProperty("shrinking"));
            tmpParameter.probability = Integer.parseInt(prop.getProperty("probability"));
            tmpParameter.nr_weight = Integer.parseInt(prop.getProperty("nr_weight"));

            if (prop.getProperty("weight_label").trim().length() == 0) {
                tmpParameter.weight_label = new int[0];
            } else {
                String[] s_tmp_wl = prop.getProperty("weight_label").trim().split("\\s+");
                int[] tmp_wl = new int[s_tmp_wl.length];
                for (int i = 0; i < s_tmp_wl.length; i++) {
                    String string = s_tmp_wl[i];
                    tmp_wl[i] = Integer.parseInt(string);
                }
                tmpParameter.weight_label = tmp_wl;
            }
            if (prop.getProperty("weight").trim().length() == 0) {
                tmpParameter.weight = new double[0];
            } else {
                String[] s_tmp_w = prop.getProperty("weight_label").trim().split("\\s+");
                double[] tmp_w = new double[s_tmp_w.length];
                for (int i = 0; i < s_tmp_w.length; i++) {
                    String string = s_tmp_w[i];
                    tmp_w[i] = Double.parseDouble(string);
                }
                tmpParameter.weight = tmp_w;
            }
            parameter = tmpParameter;
            flag = true;
        }

        return parameter;
    }

    private Problem getProblem(Instances instances) {
        int numInstances = instances.numInstances();
        int numAttributes = selectedNumber;
        Problem tmpProblem = new Problem();
        tmpProblem.l = numInstances;
        tmpProblem.x = new Node[numInstances][];
        for (int i = 0; i < numInstances; i++) {
            tmpProblem.x[i] = new Node[numAttributes];
            for (int j = 0; j < numAttributes; j++) {
                tmpProblem.x[i][j] = new Node();
                tmpProblem.x[i][j].index = j + 1;
                tmpProblem.x[i][j].value = instances.instance(i).value(selectedAttributes[j]);
            }
        }
        tmpProblem.y = new double[numInstances];
        for (int i = 0; i < numInstances; i++) {
            tmpProblem.y[i] = instances.instance(i).classValue();
        }
        return tmpProblem;
    }

    public void buildClassifier(Instances instances) throws Exception {
        selectedNumber = instances.numAttributes() - 1;
        selectedAttributes = new int[selectedNumber];
        for (int i = 0; i < selectedNumber; i++) {
            selectedAttributes[i] = i;
        }
        problem = getProblem(instances);
        parameter = getParameter();
        model = SVM.svm_train(problem, parameter);
    }

    public double classifyInstance(Instance instance) throws Exception {
//        selectedNumber = instances.numAttributes() - 1;
//        selectedAttributes = new int[selectedNumber];
//        for (int i = 0; i < selectedNumber; i++) {
//            selectedAttributes[i] = i;
//        }
        Node[] x = new Node[selectedAttributes.length];
        for (int j = 0; j < selectedAttributes.length; j++) {
            x[j] = new Node();
            x[j].index = j + 1;
            x[j].value = instance.value(selectedAttributes[j]);
        }
        return SVM.svm_predict(model, x);
    }
}

public class SVMClassify implements Classify {

    private Parameter param;
    private Properties prop = new Properties();
    SVMModel run;

    public SVMClassify() {
        run = new SVMModel();
    }

    @Override
    public double crossTest(Instances instances, int fold, int times) throws Exception {
        Random random = new Random();
        int crossV = fold;
        int numInstances = instances.numInstances();
//        FileWriter fileWriter = new FileWriter("E:\\test\\data\\DecEntropy\\SVM_" + instances.relationName() + ".txt");

        List<Attribute> afterAttr = new ArrayList<>();
        afterAttr = instances.getAttributes();



//        FastVector afterAttr = new FastVector();
//        for (int j = 0; j < instances.numAttributes(); j++) {
//            afterAttr.addElement(instances.classAttribute(j));
//        }

        int[] rightCount = new int[instances.numClasses()];
        int[] classAmount = new int[instances.numClasses()];
        for (int k = 0; k < instances.numClasses(); k++) {
            rightCount[k] = 0;
            classAmount[k] = 0;
        }

        int[] classes = new int[crossV];
        for (int j = 0; j < crossV; j++) {
            classes[j] = instances.numInstances() / crossV * j;
            if (instances.numInstances() % crossV > j) {
                classes[j] += j;
            } else {
                classes[j] += instances.numInstances() % crossV;
            }
        }

        for (; times > 0; times--) {
            int[] index = new int[instances.numInstances()];
            for (int j = 0; j < instances.numInstances(); j++) {
                index[j] = j;
            }
            for (int j = 0; j < numInstances; j++) {
                int seed = j + random.nextInt(numInstances - j);
                int tmp = index[j];
                index[j] = index[seed];
                index[seed] = tmp;
            }
            for (int j = 0; j < crossV; j++) {
                Instances trainInstances, testInstances;

                int end = classes[(j - 1 + crossV) % crossV];

                trainInstances = new Instances("train", afterAttr, end - classes[j]);
                trainInstances.setClassIndex(instances.numAttributes() - 1);

                for (int k = classes[j]; k < end; k++) {
                    //  System.out.println("before " + instances.numClasses());   //                     
                    int tmpk = index[k % instances.numInstances()];
                    // System.out.println("after " + instances.numClasses());                  //     
                    trainInstances.addInstance(instances.instance(tmpk));
                }

                classes[j] += instances.numInstances();
                testInstances = new Instances("test", afterAttr, classes[j] - end);
                testInstances.setClassIndex(instances.numAttributes() - 1);
                for (int k = end; k < classes[j]; k++) {
                    int tmpk = index[k % instances.numInstances()];

                    testInstances.addInstance(instances.instance(tmpk));

                }


                //               int[] indexs = DecisionEntropy.getFirstKAttributes(trainInstances, instances.numAttributes() - 1);
//                int[] indexs = new int[instances.numAttributes() - 1];
//                for (int i = 0; i < indexs.length; i++) {
//                    indexs[i] = i;
//                    
//                }
//                
//                trainInstances = Util.reduceInstancesButClassAttribute(trainInstances, indexs);
//                testInstances = Util.reduceInstancesButClassAttribute(testInstances, indexs);
//			
                run.buildClassifier(trainInstances);


                double aa = 0;
                double bb = 0;
                for (int k = 0; k < testInstances.numInstances(); k++) {
                    int rightClass = (int) testInstances.instance(k).classValue();
                    classAmount[rightClass]++;
                    int resultClass = (int) (run.classifyInstance(testInstances.instance(k)));

//                    System.out.println("r" + "\t" + resultClass);
                    if (rightClass == resultClass) {
                        rightCount[rightClass]++;
                        aa++;
                    }
                    bb++;
                }
//                fileWriter.write(aa / bb + "\n");

            }
        }
//        fileWriter.close();
        int tmpright = 0;
        int tmptotal = 0;
//        System.out.println("3 " + instances.numClasses());
//        System.out.println("4 " + instances.numInstances());
//        for (int j = 0; j < instances.numInstances(); j++) {
        for (int j = 0; j < instances.numClasses(); j++) {
            tmpright += rightCount[j];
            tmptotal += classAmount[j];
        }
        return tmpright * 1.0 / tmptotal;
        //  throw new UnsupportedOperationException("Not supported yet.");
    }

    public static void main(String[] args) {
        File f = new File("D:" + File.separator + "colon.arff");
        ArffFileReader reader = new ArffFileReader(f);
        Instances inst = reader.getDataSet();
        SVMClassify sv = new SVMClassify();
        try {
            System.out.println(sv.crossTest(inst, 10, 2));
            System.out.println("result=" + sv.evalScore(inst));
        } catch (Exception ex) {
            Logger.getLogger(SVMClassify.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public double crossTest(Instances instances, int fold, int times, IAttributeSelection attributeSelector) throws Exception {
        Random random = new Random();
        int crossV = fold;
        int numInstances = instances.numInstances();

        List<Attribute> afterAttr = new ArrayList<>();
        afterAttr = instances.getAttributes();

        int[] rightCount = new int[instances.numClasses()];
        int[] classAmount = new int[instances.numClasses()];
        for (int k = 0; k < instances.numClasses(); k++) {
            rightCount[k] = 0;
            classAmount[k] = 0;
        }

        int[] classes = new int[crossV];
        for (int j = 0; j < crossV; j++) {
            classes[j] = instances.numInstances() / crossV * j;
            if (instances.numInstances() % crossV > j) {
                classes[j] += j;
            } else {
                classes[j] += instances.numInstances() % crossV;
            }
        }

        for (; times > 0; times--) {
            int[] index = new int[instances.numInstances()];
            for (int j = 0; j < instances.numInstances(); j++) {
                index[j] = j;
            }
            for (int j = 0; j < numInstances; j++) {
                int seed = j + random.nextInt(numInstances - j);
                int tmp = index[j];
                index[j] = index[seed];
                index[seed] = tmp;
            }
            for (int j = 0; j < crossV; j++) {
                Instances trainInstances, testInstances;

                int end = classes[(j - 1 + crossV) % crossV];

                trainInstances = new Instances("train", afterAttr, end - classes[j]);
                trainInstances.setClassIndex(instances.numAttributes() - 1);

                for (int k = classes[j]; k < end; k++) {
                    int tmpk = index[k % instances.numInstances()];
                    trainInstances.addInstance(instances.instance(tmpk));
                }

                classes[j] += instances.numInstances();
                testInstances = new Instances("test", afterAttr, classes[j] - end);
                testInstances.setClassIndex(instances.numAttributes() - 1);
                for (int k = end; k < classes[j]; k++) {
                    int tmpk = index[k % instances.numInstances()];
                    testInstances.addInstance(instances.instance(tmpk));
                }

                int[] indexs = attributeSelector.getFirstKAttributes(trainInstances, instances.numAttributes() - 1);

                trainInstances = Util.reduceInstancesButClassAttribute(trainInstances, indexs);
                testInstances = Util.reduceInstancesButClassAttribute(testInstances, indexs);
                run.buildClassifier(trainInstances);

                double aa = 0;
                double bb = 0;
                for (int k = 0; k < testInstances.numInstances(); k++) {
                    int rightClass = (int) testInstances.instance(k).classValue();
                    classAmount[rightClass]++;
                    int resultClass = (int) (run.classifyInstance(testInstances.instance(k)));

                    if (rightClass == resultClass) {
                        rightCount[rightClass]++;
                        aa++;
                    }
                    bb++;
                }

            }

        }
        int tmpright = 0;
        int tmptotal = 0;
//        System.out.println("4 " + instances.numInstances());
        for (int j = 0; j < instances.numClasses(); j++) {
            tmpright += rightCount[j];
            tmptotal += classAmount[j];
        }
        return tmpright * 1.0 / tmptotal;
    }

//    private double classifyInstance(Instance instance) throws Exception {
//        return run.classifyInstance(instance);
//    }
//    @Override
//    public double[] classifyInstances(Instances instances) {
//        double[] result = null;
//        for (int i = 0; i < instances.numInstances(); i++) {
//            try {
//                result[i] = run.classifyInstance(instances.instance(i));
//            } catch (Exception ex) {
//                Logger.getLogger(SVMClassify.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
//        return result;
//    }
    @Override
    public Properties getprProperties() {
        return run.getProperties();
    }

    @Override
    public void setProperties(Properties prop) {
        run.setProperties(prop);
    }

    @Override
    public double evalScore(Instances instances) {
        double[] score = new double[instances.numClasses()];
        double[] num = new double[instances.numClasses()];
        Arrays.fill(score, 0);
        Arrays.fill(num, 0);
        for (int i = 0; i < instances.numInstances(); i++) {
            try {
                int tmp = (int) instances.instance(i).classValue();
                num[tmp]++;

                if (instances.instance(i).classValue() == run.classifyInstance(instances.instance(i))) {
                    score[tmp]++;
                }
            } catch (Exception ex) {
                Logger.getLogger(SVMClassify.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        double res = 0;
        for (int i = 0; i < num.length; i++) {
            res += score[i] / num[i];
        }

        return res / instances.numClasses();
    }

    @Override
    public boolean hasProperties() {
        return true;
    }

//    @Override
//    public double crossTest(Instances instances, int fold, int times, IAttributeSelection attributeSelector, int K) throws Exception {
//        if (K <= 0 || K >= instances.numInstances()) {
//            throw new Exception("K值不规范");
//        }
//        Random random = new Random();
//        int crossV = fold;
//        int numInstances = instances.numInstances();
//
//        List<Attribute> afterAttr = new ArrayList<>();
//        afterAttr = instances.getAttributes();
//
//        int[] rightCount = new int[instances.numClasses()];
//        int[] classAmount = new int[instances.numClasses()];
//        for (int k = 0; k < instances.numClasses(); k++) {
//            rightCount[k] = 0;
//            classAmount[k] = 0;
//        }
//
//        int[] classes = new int[crossV];
//        for (int j = 0; j < crossV; j++) {
//            classes[j] = instances.numInstances() / crossV * j;
//            if (instances.numInstances() % crossV > j) {
//                classes[j] += j;
//            } else {
//                classes[j] += instances.numInstances() % crossV;
//            }
//        }
//
//        for (; times > 0; times--) {
//            int[] index = new int[instances.numInstances()];
//            for (int j = 0; j < instances.numInstances(); j++) {
//                index[j] = j;
//            }
//            for (int j = 0; j < numInstances; j++) {
//                int seed = j + random.nextInt(numInstances - j);
//                int tmp = index[j];
//                index[j] = index[seed];
//                index[seed] = tmp;
//            }
//            for (int j = 0; j < crossV; j++) {
//                Instances trainInstances, testInstances;
//
//                int end = classes[(j - 1 + crossV) % crossV];
//
//                trainInstances = new Instances("train", afterAttr, end - classes[j]);
//                trainInstances.setClassIndex(instances.numAttributes() - 1);
//
//                for (int k = classes[j]; k < end; k++) {
//                    int tmpk = index[k % instances.numInstances()];
//                    trainInstances.addInstance(instances.instance(tmpk));
//                }
//
//                classes[j] += instances.numInstances();
//                testInstances = new Instances("test", afterAttr, classes[j] - end);
//                testInstances.setClassIndex(instances.numAttributes() - 1);
//                for (int k = end; k < classes[j]; k++) {
//                    int tmpk = index[k % instances.numInstances()];
//                    testInstances.addInstance(instances.instance(tmpk));
//                }
//
//                int[] indexs = attributeSelector.getFirstKAttributes(trainInstances, instances.numAttributes() - 1);
//
//                trainInstances = Util.reduceInstancesButClassAttribute(trainInstances, indexs);
//                testInstances = Util.reduceInstancesButClassAttribute(testInstances, indexs);
//                run.buildClassifier(trainInstances);
//
//                double aa = 0;
//                double bb = 0;
//                for (int k = 0; k < testInstances.numInstances(); k++) {
//                    int rightClass = (int) testInstances.instance(k).classValue();
//                    classAmount[rightClass]++;
//                    int resultClass = (int) (run.classifyInstance(testInstances.instance(k)));
//
//                    if (rightClass == resultClass) {
//                        rightCount[rightClass]++;
//                        aa++;
//                    }
//                    bb++;
//                }
//
//            }
//
//        }
//        int tmpright = 0;
//        int tmptotal = 0;
////        System.out.println("4 " + instances.numInstances());
//        for (int j = 0; j < instances.numClasses(); j++) {
//            tmpright += rightCount[j];
//            tmptotal += classAmount[j];
//        }
//        return tmpright * 1.0 / tmptotal;
//    }
}
