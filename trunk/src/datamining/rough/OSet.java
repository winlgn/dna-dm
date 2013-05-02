/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package datamining.rough;

/**
 *
 * @author lenovo
 */
class OSet {

    public int index;
    public OSet nextDomain;
    public OSet nextEntry;

    public OSet() {
        nextDomain = nextEntry = null;
    }

    public OSet Clone() {
        OSet set = new OSet();
        set.index = this.index;
        return set;
    }
}
