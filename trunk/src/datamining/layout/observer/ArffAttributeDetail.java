/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package datamining.layout.observer;

import java.util.Observable;

/**
 *
 * @author LiuGuining
 */
public class ArffAttributeDetail extends Observable {

    public ArffAttributeDetail() {
    }

    public void set(int line) {
        super.setChanged();
        super.notifyObservers(line);

    }
}
