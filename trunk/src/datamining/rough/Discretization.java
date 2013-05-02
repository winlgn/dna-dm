/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package datamining.rough;

import datamining.core.Instances;

/**
 *
 * @author lenovo
 */
public interface Discretization {

    public InstancesBreakPoint discrete(Instances instances);

    public Instances getDiscretizationInstances(Instances instances, InstancesBreakPoint instancesBreakPoint);
}
