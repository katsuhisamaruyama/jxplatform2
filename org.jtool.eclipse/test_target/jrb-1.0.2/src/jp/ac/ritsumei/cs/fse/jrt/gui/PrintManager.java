/*
 *     PrintManager.java  Nov 5, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.gui;
import javax.swing.*;
import java.awt.*;
import java.awt.print.*;
import java.util.*;

public class PrintManager {
    private JFrame frame;
    private FontMetrics fontMetrics;
    private Font font;
    private int fontHeight;
    private int fontAscent;

    public PrintManager(JFrame frame) {
        this.frame = frame;

        font = new Font("Monospaced", Font.PLAIN, 10);
        fontMetrics = frame.getFontMetrics(font);
        fontHeight = fontMetrics.getHeight();
        fontAscent = fontMetrics.getAscent();
    }

    public void print(String text) {
        Properties properties = new Properties();
        PrintJob pj = Toolkit.getDefaultToolkit().getPrintJob(frame, "Printer", properties);

        if (pj == null) {
            return;
        }

        int screenResolution = Toolkit.getDefaultToolkit().getScreenResolution();
        int topMargin = screenResolution;
        int leftMargin = screenResolution / 2;
        Dimension pageSize = new Dimension(pj.getPageDimension());
        pageSize.width = pageSize.width - leftMargin * 2;
        pageSize.height = pageSize.height - topMargin * 2;

        Graphics g = null;
        int line = fontAscent;
        int column = 0;
        int maxLine = pageSize.height - fontHeight;

        try {
            String content = translatePrintText(text);
            StringTokenizer st = new StringTokenizer(content, "\n");

            while (st.hasMoreTokens()) {
                String oneLineContent = (String)st.nextToken();
                StringTokenizer ost = new StringTokenizer(oneLineContent, " ", true);

                while (ost.hasMoreTokens()) {
                    String token = (String)ost.nextToken();

                    int stringWidth = fontMetrics.stringWidth(token);
                    if (column + stringWidth > pageSize.width) {
                        line = line + fontHeight;
                        column = 0;

                        if (line > maxLine) {
                            g.dispose();
                            g = null;
                        }
                    }
                    
                    if (g == null) {
                        g = pj.getGraphics();
                        g.translate(leftMargin, topMargin);
                        g.clipRect(0, 0, pageSize.width, pageSize.height);
                        g.setFont(font);
                        line = fontAscent;
                        column = 0;
                    }
                    g.drawString(token, column, line);
                    column = column + stringWidth;
                }
                column = pageSize.width;
            }

            if (g != null) {
                g.dispose();
            }

        } finally {
            pj.end();
        }
    }

    private String translatePrintText(String text) {
        boolean isCR = false;
        StringBuffer buf = new StringBuffer();

        StringTokenizer st = new StringTokenizer(text, "\t\n", true);
        while (st.hasMoreTokens()) {
            String token = (String)st.nextToken();
            if (token.equals("\t")) {
                buf.append("    ");
            } else if (token.equals("\n")) {
                if (isCR) {
                    buf.append(" \n");
                }
                isCR = true;
            } else {
                buf.append(token);
                buf.append("\n");
                isCR = false;
            }
        }
        return buf.toString();
    }
}
