/*
 * To change this temp

 @Override
 public int getSize() {
 throw new UnsupportedOperationException("Not supported yet.");
 }

 @Override
 public Object getElementAt(int index) {
 throw new UnsupportedOperationException("Not supported yet.");
 }

 @Override
 public void addListDataListener(ListDataListener l) {
 throw new UnsupportedOperationException("Not supported yet.");
 }

 @Override
 public void removeListDataListener(ListDataListener l) {
 throw new UnsupportedOperationException("Not supported yet.");
 }
 }ate, choose Tools | Templates
 * and open the template in the editor.
 */
package datamining.layout;

import datamining.attributeselection.IAttributeSelection;
import datamining.classify.Classify;
import datamining.core.Instances;
import datamining.rough.AttributeReduction;
import datamining.rough.Discretization;
import datamining.rough.Util;
import datamining.rough.ValueReduction;
import datamining.util.ArffFileReader;
import datamining.util.PropertiesUtil;
import java.io.File;
import java.util.Timer;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

/**
 *
 * @author LiuGuining
 */
public class RecommandWizard extends javax.swing.JFrame {

    private Instances dataSet;
    private static final int N = 3;
    private static final int THRESHOLD = 5000;
    private boolean customFlag = false;

    /**
     * Creates new form RecommandWizard
     */
    public RecommandWizard(Instances dataSet) {
        this.dataSet = dataSet;
        initComponents();
        initLists();
        setTabSelection(0);
    }

    private class TestResult implements Comparable<TestResult> {

        int fold;
        int times;
        int K;
        String classifierName;
        String seletorName;
        String discretizationName;
        String attributeReductionName;
        String valueReductionName;
        double crossTestResult;
        boolean flag;

        public TestResult(String classifierName, String seletorName, double crossTestResult) {
            this.classifierName = classifierName;
            this.seletorName = seletorName;
            this.crossTestResult = crossTestResult;
            flag = true;
        }

        public TestResult(String discretizationName, String attributeReductionName,
                String valueReductionName, String classifierName, double crossTestResult) {
            this.discretizationName = discretizationName;
            this.attributeReductionName = attributeReductionName;
            this.valueReductionName = valueReductionName;
            this.classifierName = classifierName;
            this.crossTestResult = crossTestResult;
            flag = false;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            if (flag) {
                builder.append("Classifier:\t").append(classifierName).append("\n");
                builder.append("Selector:\t").append(seletorName).append("\n");
                builder.append("Result:\t\t").append(crossTestResult).append("\n");
            } else {
                builder.append("Discretization:\t").append(discretizationName).append("\n");
                builder.append("AttrReduction:\t").append(attributeReductionName).append("\n");
                builder.append("ValueReduction:\t").append(valueReductionName).append("\n");
                builder.append("Classifier:\t").append(classifierName).append("\n");
                builder.append("Result:\t\t").append(crossTestResult).append("\n");
            }
            return builder.toString();
        }

        @Override
        public int compareTo(TestResult o) {
            int compare = Double.compare(crossTestResult, o.crossTestResult);
            if (compare == 0) {
                return 0;
            } else if (compare > 0) {
                return -1;
            } else {
                return 1;
            }
        }
    }

    private class FastTest extends Thread {

        Instances inst;
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {

            int i = 0;

            @Override
            public void run() {
                switch (i % 3) {
                    case 0:
                        statusLabel.setText("处理中.");
                        break;
                    case 1:
                        statusLabel.setText("处理中..");
                        break;
                    case 2:
                        statusLabel.setText("处理中...");
                        break;
                }
                i++;
            }
        };
        int fold;
        int times;
        JTextArea textArea;
        JProgressBar progressBar;
        JLabel label;
        JButton button;
        SortedSet<TestResult> results = new TreeSet<>();

        public FastTest(Instances inst, JTextArea textArea,
                JProgressBar progressBar, JLabel label, JButton button) {
            this(inst, 10, 3, textArea, progressBar, label, button);
        }

        public FastTest(Instances inst, int fold, int times, JTextArea textArea,
                JProgressBar progressBar, JLabel label, JButton button) {
            this.inst = inst;
            this.fold = fold;
            this.times = times;
            this.textArea = textArea;
            this.progressBar = progressBar;
            this.label = label;
            this.button = button;

        }

        @Override
        public void run() {
            if (inst.numAttributes() > THRESHOLD) {
                Vector<String> classifierNames;
                Vector<String> selectorNames;
                if (!customFlag) {
                    classifierNames = PropertiesUtil.getPropItems(PropertiesUtil.CLASSIFIER);
                    selectorNames = PropertiesUtil.getPropItems(PropertiesUtil.ATTIBUTE_SELECTION);
                } else {
                    classifierNames = listData(jList1_1);
                    selectorNames = listData(jList2_1);
                }
                //初始化ProgressBar
                progressBar.setMinimum(0);
                progressBar.setMaximum(classifierNames.size() * (selectorNames.size() + 1));

                int count = 0;
                progressBar.setValue(count);
                //开始计算
                button.setEnabled(false);
                timer.schedule(task, 1000, 1000);
                for (int i = 0; i < classifierNames.size(); i++) {
                    Classify classifier = PropertiesUtil.getClassifier(classifierNames.get(i));
                    for (int j = -1; j < selectorNames.size(); j++) {
                        TestResult testResult;
                        double crossTest = 0.0;
                        if (j >= 0) {
                            try {
                                IAttributeSelection selector = PropertiesUtil.getAttributeSelection(selectorNames.get(j));
                                crossTest = classifier.crossTest(inst, fold, times, selector);

                            } catch (Exception ex) {
                                Logger.getLogger(RecommandWizard.class.getName()).log(Level.SEVERE, null, ex);
                                timer.cancel();
                            }
                            testResult = new TestResult(classifierNames.get(i), selectorNames.get(j), crossTest);
                        } else {
                            try {
                                crossTest = classifier.crossTest(inst, fold, times);
                            } catch (Exception ex) {
                                Logger.getLogger(RecommandWizard.class.getName()).log(Level.SEVERE, null, ex);
                                timer.cancel();
                            }
                            testResult = new TestResult(classifierNames.get(i), "N/A", crossTest);
                        }
                        textArea.append("\n====#" + (++count) + "====\n");
                        textArea.append(testResult.toString());
                        textArea.paintImmediately(textArea.getBounds());
                        textArea.setCaretPosition(textArea.getText().length());
                        results.add(testResult);
                        progressBar.setValue(count);
                    }
                }
                label.setText(getBestResult(3));
            } else {
                Vector<String> discretizationNames = PropertiesUtil.getPropItems(PropertiesUtil.DISCRETIZATION);
                Vector<String> valueReductionNames = PropertiesUtil.getPropItems(PropertiesUtil.VALUEREDUCTION);
                Vector<String> tmpClassiferNames = PropertiesUtil.getPropItems(PropertiesUtil.CLASSIFIER);
                //对classifer进行筛选
                Vector<String> classiferNames = new Vector<>();
                for (String string : tmpClassiferNames) {
                    if (string.contains("Rough")) {
                        classiferNames.add(string);
                    }
                }
                progressBar.setMinimum(0);
                progressBar.setMaximum(discretizationNames.size() * 1 * valueReductionNames.size() * classiferNames.size());
                int count = 0;
                progressBar.setValue(count);
                button.setEnabled(false);
                timer.schedule(task, 1000, 1000);
                for (int i = 0; i < discretizationNames.size(); i++) {
                    Discretization discretization = PropertiesUtil.getDiscretization(discretizationNames.get(i));
                    AttributeReduction reduction = new AttributeReduction();
                    for (int j = 0; j < valueReductionNames.size(); j++) {
                        ValueReduction valueReduction = PropertiesUtil.getValueReduction(valueReductionNames.get(j));
                        for (int k = 0; k < classiferNames.size(); k++) {
                            try {
                                Classify classifer = PropertiesUtil.getClassifier(classiferNames.get(k));
                                Instances newInst = inst.clone();
                                double crossTest = 0.0;
                                newInst = discretization.getDiscretizationInstances(newInst, discretization.discrete(newInst));
                                int[] index = reduction.reductionByAttributeOrder(newInst);
                                newInst = Util.ReduceInstances(newInst, index);
                                newInst = valueReduction.GetReducedInstances(newInst);
                                //分类器
                                crossTest = classifer.crossTest(newInst, fold, times);
                                TestResult testResult = new TestResult(discretizationNames.get(i),
                                        "AttributeReduction", valueReductionNames.get(j), classiferNames.get(k), crossTest);
                                textArea.append("\n====#" + (++count) + "====\n");
                                textArea.append(testResult.toString());
                                textArea.paintImmediately(textArea.getBounds());
                                textArea.setCaretPosition(textArea.getText().length());
                                results.add(testResult);
                                progressBar.setValue(count);
                            } catch (Exception ex) {
                                Logger.getLogger(RecommandWizard.class.getName()).log(Level.SEVERE, null, ex);
                                timer.cancel();
                            }
                        }

                    }

                }
                label.setText(getBestResult(2));
            }
            //运算结束
            timer.cancel();
            statusLabel.setText("已完成。");
            button.setEnabled(true);
        }

        private String getBestResult(int n) {

            if (results == null || results.isEmpty()) {
                return null;
            }
            StringBuilder sb = new StringBuilder("<html>");
            sb.append("<div>显示得分最高的").append("<b>&nbsp;");
            sb.append(results.size() > n ? n : results.size());
            sb.append("</b>&nbsp;条结果：</div><br/>");
            sb.append("<div>(fold = ").append(fold).append(",times = ").append(times).append(")</div><br/>");
            Iterator<TestResult> iterator = results.iterator();
            for (int i = 0; i < n; i++) {
                if (iterator.hasNext()) {
                    if (i == 0) {
                        sb.append("<div style=\"color:red;font-weight:bold\">");
                    } else {
                        sb.append("<div>");
                    }
                    TestResult next = iterator.next();
                    sb.append((i + 1)).append('.');
                    sb.append(next.toString().replaceAll("\n", "<br/>&nbsp;"));
                    sb.append("<br/></div>");
                }
            }
            sb.append("</html>");
            return sb.toString();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        testAllRadioButton = new javax.swing.JRadioButton();
        customRadioButton = new javax.swing.JRadioButton();
        jPanel3 = new javax.swing.JPanel();
        jTabbedPane2 = new javax.swing.JTabbedPane();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jList1_1 = new javax.swing.JList();
        jScrollPane3 = new javax.swing.JScrollPane();
        jList1_2 = new javax.swing.JList();
        moveToRightButton_1 = new javax.swing.JButton();
        moveToLeftButton_1 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jPanel8 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        jList2_1 = new javax.swing.JList();
        jScrollPane5 = new javax.swing.JScrollPane();
        jList2_2 = new javax.swing.JList();
        moveToRightButton_2 = new javax.swing.JButton();
        moveToLeftButton_2 = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        statusLabel = new javax.swing.JLabel();
        jProgressBar1 = new javax.swing.JProgressBar();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        recomputeButton = new javax.swing.JButton();
        resultLabel = new javax.swing.JLabel();
        previousButton = new javax.swing.JButton();
        nextButton = new javax.swing.JButton();
        exitButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("推荐向导");
        setResizable(false);

        jTabbedPane1.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);
        jTabbedPane1.setTabPlacement(javax.swing.JTabbedPane.LEFT);

        buttonGroup1.add(testAllRadioButton);
        testAllRadioButton.setSelected(true);
        testAllRadioButton.setText("测试全部");
        testAllRadioButton.setActionCommand("quick");

        buttonGroup1.add(customRadioButton);
        customRadioButton.setText("自定义");
        customRadioButton.setActionCommand("custom");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(130, 130, 130)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(customRadioButton)
                    .addComponent(testAllRadioButton))
                .addContainerGap(282, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(78, 78, 78)
                .addComponent(testAllRadioButton)
                .addGap(39, 39, 39)
                .addComponent(customRadioButton)
                .addContainerGap(155, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("开始", jPanel1);

        jScrollPane2.setViewportView(jList1_1);

        jScrollPane3.setViewportView(jList1_2);

        moveToRightButton_1.setText(">");
        moveToRightButton_1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveToRightButton_1ActionPerformed(evt);
            }
        });

        moveToLeftButton_1.setText("<");
        moveToLeftButton_1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveToLeftButton_1ActionPerformed(evt);
            }
        });

        jLabel1.setText("<html><b>已激活：</b></html>");

        jLabel2.setText("可用：");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 188, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(moveToRightButton_1)
                            .addComponent(moveToLeftButton_1)))
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 201, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(102, 102, 102)
                        .addComponent(moveToRightButton_1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(moveToLeftButton_1)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane2)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 229, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE)))))
                .addContainerGap())
        );

        jTabbedPane2.addTab("Classifer", jPanel4);

        jScrollPane4.setViewportView(jList2_1);

        jScrollPane5.setViewportView(jList2_2);

        moveToRightButton_2.setText(">");
        moveToRightButton_2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveToRightButton_2ActionPerformed(evt);
            }
        });

        moveToLeftButton_2.setText("<");
        moveToLeftButton_2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveToLeftButton_2ActionPerformed(evt);
            }
        });

        jLabel3.setText("<html><b>已激活：</b></html>");

        jLabel4.setText("可用：");

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 188, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(moveToRightButton_2)
                            .addComponent(moveToLeftButton_2)))
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 201, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGap(102, 102, 102)
                        .addComponent(moveToRightButton_2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(moveToLeftButton_2)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane4)
                            .addGroup(jPanel8Layout.createSequentialGroup()
                                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 229, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE)))))
                .addContainerGap())
        );

        jTabbedPane2.addTab("Selector", jPanel8);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane2)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 298, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("参数设定", jPanel3);

        statusLabel.setText("运算中...");

        jProgressBar1.setStringPainted(true);

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane1.setViewportView(jTextArea1);

        recomputeButton.setText("重新计算");
        recomputeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                recomputeButtonActionPerformed(evt);
            }
        });

        resultLabel.setText("运算结果：");
        resultLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(statusLabel)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 229, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 229, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(recomputeButton)
                            .addComponent(resultLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 226, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(statusLabel)
                    .addComponent(recomputeButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 249, Short.MAX_VALUE))
                    .addComponent(resultLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        jTabbedPane1.addTab("推荐结果", jPanel2);

        previousButton.setMnemonic('p');
        previousButton.setText("上一步(P)");
        previousButton.setToolTipText("");
        previousButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                previousButtonActionPerformed(evt);
            }
        });

        nextButton.setMnemonic('n');
        nextButton.setText("下一步(N)");
        nextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextButtonActionPerformed(evt);
            }
        });

        exitButton.setText("退出");
        exitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 554, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(previousButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(nextButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(exitButton)))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {exitButton, nextButton, previousButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(previousButton)
                    .addComponent(nextButton)
                    .addComponent(exitButton))
                .addContainerGap())
        );

        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width-590)/2, (screenSize.height-414)/2, 590, 414);
    }// </editor-fold>//GEN-END:initComponents

    private void recomputeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_recomputeButtonActionPerformed
        new FastTest(dataSet, jTextArea1, jProgressBar1, resultLabel, recomputeButton).start();
    }//GEN-LAST:event_recomputeButtonActionPerformed

    private void previousButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_previousButtonActionPerformed
        int index = jTabbedPane1.getSelectedIndex();
        switch (index) {
            case 1:
                setTabSelection(0);
                break;
            case 2:
                if (customFlag) {
                    setTabSelection(1);
                } else {
                    setTabSelection(0);
                }
                break;
        }
    }//GEN-LAST:event_previousButtonActionPerformed

    private void nextButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextButtonActionPerformed
        int index = jTabbedPane1.getSelectedIndex();
        switch (index) {
            case 0:
                switch (buttonGroup1.getSelection().getActionCommand()) {
                    case "quick":
                        setTabSelection(2);
                        customFlag = false;
                        recomputeButtonActionPerformed(null);
                        break;
                    case "custom":
                        customFlag = true;
                        setTabSelection(1);
                        break;
                }
                break;
            case 1:
                if (listData(jList1_1).isEmpty()) {
                    JOptionPane.showMessageDialog(rootPane, "警告", "至少选择一种Classifer。", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                customFlag = true;
                recomputeButtonActionPerformed(null);
                setTabSelection(2);
                //TODO custom推荐执行
                break;
        }
    }//GEN-LAST:event_nextButtonActionPerformed

    private void exitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitButtonActionPerformed
        this.dispose();
    }//GEN-LAST:event_exitButtonActionPerformed

    private void moveToRightButton_1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveToRightButton_1ActionPerformed
        changeList(jList1_1, jList1_2);
    }//GEN-LAST:event_moveToRightButton_1ActionPerformed

    private void moveToLeftButton_1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveToLeftButton_1ActionPerformed
        changeList(jList1_2, jList1_1);
    }//GEN-LAST:event_moveToLeftButton_1ActionPerformed

    private void moveToRightButton_2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveToRightButton_2ActionPerformed
        changeList(jList2_1, jList2_2);
    }//GEN-LAST:event_moveToRightButton_2ActionPerformed

    private void moveToLeftButton_2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveToLeftButton_2ActionPerformed
        changeList(jList2_2, jList2_1);
    }//GEN-LAST:event_moveToLeftButton_2ActionPerformed
    private void initLists() {
        //Classifier
        jList1_1.setListData(PropertiesUtil.getPropItems(PropertiesUtil.CLASSIFIER));
        //Selector
        jList2_1.setListData(PropertiesUtil.getPropItems(PropertiesUtil.ATTIBUTE_SELECTION));
    }

    private void setTabSelection(int index) {
        if (index < 0 || index > jTabbedPane1.getTabCount() - 1) {
            return;
        }
        jTabbedPane1.setSelectedIndex(index);
        for (int i = 0; i < jTabbedPane1.getTabCount(); i++) {
            jTabbedPane1.setEnabledAt(i, false);
        }
        jTabbedPane1.setEnabledAt(index, true);
        previousButton.setEnabled(true);
        nextButton.setEnabled(true);
        if (index == 0) {
            previousButton.setEnabled(false);
        } else if (index == jTabbedPane1.getTabCount() - 1) {
            nextButton.setEnabled(false);
        }
    }

    private Vector<String> listData(JList list) {
        int size = list.getModel().getSize();
        Vector<String> data = new Vector<>();
        for (int i = 0; i < size; i++) {
            data.add(list.getModel().getElementAt(i).toString());
        }
        return data;
    }

    private void changeList(JList list1, JList list2) {
        //设置list2选项
        List selectedList = list1.getSelectedValuesList();
        int jListSize2 = list2.getModel().getSize();
        String[] listData2 = new String[jListSize2 + selectedList.size()];

        for (int i = 0; i < jListSize2; i++) {
            listData2[i] = list2.getModel().getElementAt(i).toString();
        }
        for (int i = 0; i < selectedList.size(); i++) {
            listData2[i + jListSize2] = selectedList.get(i).toString();
        }
        //设置list1选项
        int jListSize1 = list1.getModel().getSize();
        String[] listData1 = new String[jListSize1 - selectedList.size()];
        Vector<String> tmp = new Vector<>();
        for (int i = 0; i < jListSize1; i++) {
            tmp.add(list1.getModel().getElementAt(i).toString());
        }
        for (Object o : selectedList) {
            tmp.remove(o.toString());
        }
        tmp.toArray(listData1);
        Arrays.sort(listData2);
        Arrays.sort(listData1);
        list2.setListData(listData2);
        list1.setListData(listData1);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /*
         * Set the Nimbus look and feel
         */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /*
         * If Nimbus (introduced in Java SE 6) is not available, stay with the
         * default look and feel. For details see
         * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    break;


                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(RecommandWizard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(RecommandWizard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(RecommandWizard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(RecommandWizard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /*
         * Create and display the form
         */
        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                Instances instances = new ArffFileReader("D:" + File.separator + "colon.arff").getDataSet();
                new RecommandWizard(instances).setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JRadioButton customRadioButton;
    private javax.swing.JButton exitButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JList jList1_1;
    private javax.swing.JList jList1_2;
    private javax.swing.JList jList2_1;
    private javax.swing.JList jList2_2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTabbedPane jTabbedPane2;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JButton moveToLeftButton_1;
    private javax.swing.JButton moveToLeftButton_2;
    private javax.swing.JButton moveToRightButton_1;
    private javax.swing.JButton moveToRightButton_2;
    private javax.swing.JButton nextButton;
    private javax.swing.JButton previousButton;
    private javax.swing.JButton recomputeButton;
    private javax.swing.JLabel resultLabel;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JRadioButton testAllRadioButton;
    // End of variables declaration//GEN-END:variables
}
