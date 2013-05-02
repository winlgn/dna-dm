/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package datamining.layout;

import datamining.core.Instances;
import datamining.util.ChartDataUtil;
import datamining.util.GraphicsUtil;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.text.DecimalFormat;

/**
 *
 * @author LiuGuining
 */
public class Chart extends Canvas {

    public static final int HEIGHT_1 = 20;
    public static final int HEIGHT_2 = 15;
    public static final int MARGIN = 5;
    private int index;
    private double max;
    private int[][] data;
    private ChartDataUtil dataUtil;
    private boolean flag = false;
    private Instances dataSet;

    public Chart() {
        this.index = -1;
    }

    public Chart(Instances dataSet) {
        this.dataSet = dataSet;
        index = dataSet.classIndex();
        dataUtil = new ChartDataUtil(dataSet, index, 5);
        data = dataUtil.getResult();
    }

    public Chart(Instances dataSet, int index) {
        this.dataSet = dataSet;
        this.index = index;
        dataUtil = new ChartDataUtil(dataSet, index, 5);
        data = dataUtil.getResult();
    }

    public void setDataSet(Instances dataSet) {
        this.dataSet = dataSet;
        //      index = dataSet.classIndex();
        index = -1;
        dataUtil = new ChartDataUtil(dataSet, dataSet.classIndex(), 5);
        data = dataUtil.getResult();
        flag = false;
        repaint();
    }

    public void setIndex(int index) {
        this.index = index;
        dataUtil.setIndex(index);
        data = dataUtil.getResult();
        flag = false;
        repaint();
    }

    @Override
    public void paint(Graphics g) {
        if (index >= 0) {
            if (index != dataSet.classIndex()) {
                drawAxis(g, dataUtil.getMin(), dataUtil.getMax());
                int len = data[0].length;
                boolean flag_1 = true;
                for (int i = 0; i < len; i++) {
                    g.setColor(GraphicsUtil.COLOR[len - 1 - i]);
                    drawHistogram(g, len - i, flag_1);
                    if (i == 0) {
                        flag_1 = false;
                    }
                }
            } else {
                drawHistogram(g, GraphicsUtil.COLOR);
            }
        }

    }

    /**
     * 绘制坐标轴
     *
     * @param g
     * @param min
     * @param max
     */
    private void drawAxis(Graphics g, double min, double max) {
        DecimalFormat format = new DecimalFormat("#.##");
        g.drawLine(MARGIN, getHeight() - HEIGHT_1, getWidth() - MARGIN, getHeight() - HEIGHT_1);
        g.drawLine(MARGIN, getHeight() - HEIGHT_1, MARGIN, getHeight() - HEIGHT_2);
        g.drawLine(getWidth() / 2, getHeight() - HEIGHT_1, getWidth() / 2, getHeight() - HEIGHT_2);
        g.drawLine(getWidth() - MARGIN, getHeight() - HEIGHT_1, getWidth() - MARGIN, getHeight() - HEIGHT_2);

        GraphicsUtil.drawString(g, format.format(min), MARGIN, getHeight() - HEIGHT_2, GraphicsUtil.TOP_LEFT);
        GraphicsUtil.drawString(g, format.format((max - min) / 2 + min), getWidth() / 2, getHeight() - HEIGHT_2, GraphicsUtil.TOP);
        GraphicsUtil.drawString(g, format.format(max), getWidth() - MARGIN, getHeight() - HEIGHT_2, GraphicsUtil.TOP_RIGHT);
    }

    /**
     * 绘制直方图
     *
     * @param g
     * @param level
     * @param showText
     */
    private void drawHistogram(Graphics g, int level, boolean showText) {
        int[] points = new int[dataUtil.getParts() + 1];
        points[0] = MARGIN;
        points[points.length - 1] = getWidth() - MARGIN;
        int freqWidth = (points[points.length - 1] - points[0]) / dataUtil.getParts();
        int rem = (points[points.length - 1] - points[0]) % dataUtil.getParts();
        for (int i = 1; i < points.length; i++) {
            points[i] = points[i - 1] + freqWidth;
            if (i <= rem) {
                points[i]++;
            }
        }
        //柱状高度
        int[] heights = new int[data.length];
        int freqMax = 0;
        for (int i = 0; i < heights.length; i++) {
            for (int j = 0; j < level; j++) {
                heights[i] = heights[i] + data[i][j];
            }
            if (!flag) {
                if (i == 0) {
                    freqMax = heights[i];
                } else {
                    if (heights[i] > freqMax) {
                        freqMax = heights[i];
                    }
                }
            }
        }
        if (!flag) {
            max = freqMax;
            flag = true;
        }
        int maxHeight = getHeight() - HEIGHT_1 - HEIGHT_2 - 1;


        for (int i = 0; i < points.length - 1; i++) {

            GraphicsUtil.fillRect(g,
                    points[i],
                    getHeight() - HEIGHT_1 - 1,
                    points[i + 1] - points[i],
                    (int) ((double) maxHeight / max * heights[i]),
                    GraphicsUtil.BOTTOM_LEFT);
            Color tmp = g.getColor();
            g.setColor(Color.BLACK);
            if (showText) {
                GraphicsUtil.drawString(g, String.valueOf(heights[i]), points[i] + 2,
                        getHeight() - HEIGHT_1 - (int) ((double) maxHeight / max * heights[i]) - 2);
            }
            g.setColor(tmp);
        }
    }

    private void drawHistogram(Graphics g, Color[] c) {
        if (data == null) {
            return;
        }
        int[] points = new int[data.length];
        int[] realWidth = new int[data.length];
        points[0] = 15;
        int freqWidth = (getWidth() - (data.length + 1) * 15) / data.length;
        int rem = (getWidth() - (data.length + 1) * 15) % data.length;
        for (int i = 0; i < realWidth.length; i++) {
            realWidth[i] = freqWidth;
            if (i < rem) {
                realWidth[i]++;
            }

        }
        for (int i = 1; i < points.length; i++) {
            points[i] = points[i - 1] + freqWidth + 15;
        }
        int freqMax = 0;
        for (int i = 0; i < data.length; i++) {
            if (data[i][0] > freqMax) {
                freqMax = data[i][0];
            }

        }
        int maxHeight = getHeight() - HEIGHT_1 - 1;
        for (int i = 0; i < points.length; i++) {
            g.setColor(c[i]);
            GraphicsUtil.fillRect(g,
                    points[i],
                    getHeight() - 1, realWidth[i],
                    (int) ((double) maxHeight / freqMax * data[i][0]),
                    GraphicsUtil.BOTTOM_LEFT);
            g.setColor(Color.BLACK);
            GraphicsUtil.drawString(g,
                    String.valueOf(data[i][0]),
                    points[i] + 2,
                    getHeight() - (int) ((double) maxHeight / freqMax * data[i][0]) - 2);
        }
    }
}
