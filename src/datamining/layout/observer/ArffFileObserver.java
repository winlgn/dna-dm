/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package datamining.layout.observer;

import datamining.layout.Chart;
import java.io.File;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;
import javax.swing.JLabel;

/**
 *
 * @author LiuGuining
 */
public class ArffFileObserver implements Observer {

    private JLabel pathLabel;
    private JLabel sizeLabel;
    private JLabel lastModifyLabel;

    public ArffFileObserver(JLabel pathLabel, JLabel sizeLabel, JLabel lastModifyLabel) {
        this.pathLabel = pathLabel;
        this.sizeLabel = sizeLabel;
        this.lastModifyLabel = lastModifyLabel;
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof File) {
            File f = (File) arg;
            pathLabel.setText(f.getAbsolutePath());
            NumberFormat nf = NumberFormat.getInstance();
            sizeLabel.setText(nf.format(f.length()) + " Byte");
            lastModifyLabel.setText((new Date(f.lastModified())).toString());
        }
    }
}
