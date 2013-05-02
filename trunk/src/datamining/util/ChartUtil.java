/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package datamining.util;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYBarDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 *
 * @author ZhangYucong
 */
public class ChartUtil {

    private double[] xdataset;
    private double[] ydataset;
    private int datasetnum;
    private static JFreeChart chart;

    /**
     * @param datasetnum 数据种类
     * @param xdataset[] x轴数据
     * @param ydataset[] y轴数据
     */
    public ChartUtil(int datasetnum, double[] xdataset, double[] ydataset) {
        this.datasetnum = datasetnum;
        this.xdataset = xdataset;
        this.ydataset = ydataset;
        drawChart();
    }

    private void drawChart() {
        IntervalXYDataset dataset = setDataSet();
        chart = ChartFactory.createXYBarChart(
                "DataInfo", // 图表标题
                "Range", // 目录轴的显示标签                
                false,
                "Number", // 数值轴的显示标签
                dataset, // 数据集
                PlotOrientation.VERTICAL, // 图表方向：水平、垂直
                false, // 是否显示图例(对于简单的柱状图必须是false)
                false, // 是否生成工具
                false // 是否生成URL链接
                );
        chart.setBackgroundPaint(Color.white);
        XYPlot xyplot = (XYPlot) chart.getPlot();
        xyplot.setBackgroundPaint(Color.lightGray);
        xyplot.setDomainGridlinePaint(Color.white);
        xyplot.setRangeGridlinePaint(Color.white);
        XYBarRenderer xybarrenderer = (XYBarRenderer) xyplot.getRenderer();
        xybarrenderer.setDrawBarOutline(false);

        try {
            File file = new File("E://DataInfo.png");
            ChartUtilities.saveChartAsPNG(file, chart, 480, 320);//把报表保存为文件
        } catch (Exception e) {
            String s = e.getLocalizedMessage();
            s = e.getMessage();
            s = e.toString();
        }
    }

    /**
     * 数据集生成
     *
     * @return new XYBatdataset
     */
    private IntervalXYDataset setDataSet() {
        List<XYSeries> xylist = new ArrayList<>();
        XYSeries xyseries = new XYSeries("A");
        XYSeries xyseries1 = new XYSeries("B");
        XYSeries xyseries2 = new XYSeries("C");
        XYSeries xyseries3 = new XYSeries("D");
        XYSeries xyseries4 = new XYSeries("E");
        XYSeries xyseries5 = new XYSeries("F");
        XYSeries xyseries6 = new XYSeries("G");
        XYSeries xyseries7 = new XYSeries("H");
        XYSeries xyseries8 = new XYSeries("I");
        XYSeries xyseries9 = new XYSeries("J");
        xylist.add(0, xyseries);
        xylist.add(1, xyseries1);
        xylist.add(2, xyseries2);
        xylist.add(3, xyseries3);
        xylist.add(4, xyseries4);
        xylist.add(5, xyseries5);
        xylist.add(6, xyseries6);
        xylist.add(7, xyseries7);
        xylist.add(8, xyseries8);
        xylist.add(9, xyseries9);
        for (int i = 0; i <= xdataset.length - 1; i++) {
            for (int j = 0; j <= ydataset.length - 1; j = j + datasetnum) {
                for (int k = 0; k <= datasetnum - 1; k++) {
                    xylist.get(k).add(xdataset[i], ydataset[j]);
                    j++;
                }
            }
        }

        XYSeriesCollection xyserisecollection = new XYSeriesCollection();
        for (int i = 0; i <= datasetnum - 1; i++) {
            xyserisecollection.addSeries(xylist.get(i));
        }
        return new XYBarDataset(xyserisecollection, 0.90000000000000002D);
    }
}
