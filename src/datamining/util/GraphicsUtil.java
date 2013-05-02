package datamining.util;

import java.awt.Color;
import java.awt.Graphics;

/**
 * 绘图操作集合.
 *
 * @author LiuGuining
 */
public class GraphicsUtil {

    public static final int TOP = 0;
    public static final int BOTTOM = 1;
    public static final int LEFT = 2;
    public static final int RIGHT = 3;
    public static final int TOP_LEFT = 4;
    public static final int TOP_RIGHT = 5;
    public static final int BOTTOM_LEFT = 6;
    public static final int BOTTOM_RIGHT = 7;
    public static final int CENTER = 8;
    public static final Color[] COLOR = {
        Color.BLACK, Color.BLUE, Color.CYAN,
        Color.YELLOW, Color.GREEN, Color.PINK,
        Color.RED, Color.orange, Color.MAGENTA};

    public static void drawString(Graphics g, String str, int x, int y) {
        g.drawString(str, x, y);
    }

    public static void drawString(Graphics g, String str, int x, int y, int pos) {
        int width = g.getFontMetrics().stringWidth(str);
        int height = g.getFontMetrics().getHeight();
        switch (pos) {
            case TOP:
                g.drawString(str, x - width / 2, y + height);
                break;
            case BOTTOM:
                g.drawString(str, x - width / 2, y);
                break;
            case LEFT:
                g.drawString(str, x, y + height / 2);
                break;
            case RIGHT:
                g.drawString(str, x - width, y + height / 2);
                break;
            case TOP_LEFT:
                g.drawString(str, x, y + height);
                break;
            case TOP_RIGHT:
                g.drawString(str, x - width, y + height);
                break;
            case BOTTOM_LEFT:
                g.drawString(str, x, y);
                break;
            case BOTTOM_RIGHT:
                g.drawString(str, x - width, y);
                break;
            case CENTER:
                g.drawString(str, x - width / 2, y + height / 2);
                break;
        }

    }

    public static void fillRect(Graphics g, int x, int y, int width, int height) {
        g.fillRect(x, y, width, height);
    }

    public static void fillRect(Graphics g, int x, int y, int width, int height, int pos) {
        switch (pos) {
            case TOP:
                g.fillRect(x - width / 2, y, width, height);
                break;
            case BOTTOM:
                g.fillRect(x - width / 2, y - height, width, height);
                break;
            case LEFT:
                g.fillRect(x, y - height / 2, width, height);
                break;
            case RIGHT:
                g.fillRect(x - width, y - height / 2, width, height);
                break;
            case TOP_LEFT:
                g.fillRect(x, y, width, height);
                break;
            case TOP_RIGHT:
                g.fillRect(x - width, y, width, height);
                break;
            case BOTTOM_LEFT:
                g.fillRect(x, y - height, width, height);
                break;
            case BOTTOM_RIGHT:
                g.fillRect(x - width, y - height, width, height);
                break;
            case CENTER:
                g.fillRect(x - width / 2, y - width / 2, width, height);
                break;
        }
    }
}
