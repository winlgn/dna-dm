/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package datamining.layout.observer;

import datamining.core.Instances;
import datamining.util.ArffFileReader;
import java.io.File;
import java.util.Observable;

/**
 *
 * @author LiuGuining
 */
public class ArffFile extends Observable {

    private File file;
    private Instances dataSet;

    public ArffFile(File file) {
        this.setFile(file);
    }

    public ArffFile() {
    }

    public File getFile() {
        return file;
    }

    public Instances getDataSet() {
        return dataSet;
    }

    public void setDataSet(Instances dataSet) {
        this.dataSet = dataSet;
        super.setChanged();
        super.notifyObservers(dataSet);
    }

    public void setFile(File file) {

        this.file = file;
        ArffFileReader reader = new ArffFileReader(file);
        dataSet = reader.getDataSet();
        super.setChanged();
        super.notifyObservers(file);
        super.setChanged();
        super.notifyObservers(dataSet);

    }
}
