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
public class ValueReductionByDecisionMatrix implements ValueReduction {

    @Override
    public Instances GetReducedInstances(Instances instances) {
        PositiveRegion positiveRegion = new PositiveRegion();
        positiveRegion.CalcPostiveRegionByOrdinarySort(instances);

        int[] attributeOrders = new int[instances.numAttributes() - 1];
        for (int i = 0; i < instances.numAttributes() - 1; i++) {
            attributeOrders[i] = i;
        }
        boolean[] isInstanceInPositives = positiveRegion.GetPositiveInstanceLabels();

        ValueReductionByDivide valueReductionByDivide = new ValueReductionByDivide();
        valueReductionByDivide.SetInformation(instances, attributeOrders, isInstanceInPositives);
        Instances tmpInstances = valueReductionByDivide.ValueReductionByDivide();

        return tmpInstances;
    }
}
