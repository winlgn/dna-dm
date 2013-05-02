/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package datamining.rough;

import java.util.Arrays;

/**
 * @属性断点、实例断点
 *
 * @author lenovo
 */
class BreakPoint implements Comparable<BreakPoint> {

    double value;
    boolean isSelect;
    double importance;
    double entropy;

    @Override
    public int compareTo(BreakPoint arg0) {

        if (this.value > arg0.value) {
            return 1;
        } else if (this.value < arg0.value) {
            return -1;
        } else {
            return 0;
        }
    }
}

class AttributeBreakPoint implements Comparable<AttributeBreakPoint> {

    BreakPoint[] breakPoints;
    int numBreakPoints;
    int indexAttribute;

    public AttributeBreakPoint(int numBreakPoints, int indexAttribute) {
        this.indexAttribute = indexAttribute;
        breakPoints = new BreakPoint[numBreakPoints];
        this.numBreakPoints = numBreakPoints;
    }

    public void SortBreakPoint() {
        Arrays.sort(breakPoints);
    }

    @Override
    public int compareTo(AttributeBreakPoint arg0) {
        return this.numBreakPoints - arg0.numBreakPoints;
    }

    public int GetDiscreteValue(double value) {
        int index = 0;
        for (int i = 0; i < numBreakPoints; i++) {
            if (value < breakPoints[i].value) {
                break;
            }
            index++;
        }
        return index;
    }

    public void ShowBreakPoint() {

        System.out.println("Attribute " + indexAttribute);
        for (int i = 0; i < numBreakPoints; i++) {
            System.out.print(Double.toString(breakPoints[i].value) + " ");
        }
        System.out.println();
    }
}

class InstancesBreakPoint {

    AttributeBreakPoint[] attributeBreakPoints;
    int numAttributeBreakPoints;

    public InstancesBreakPoint(int numAttributes) {
        attributeBreakPoints = new AttributeBreakPoint[numAttributes];
        numAttributeBreakPoints = numAttributes;
    }

    public void SortAttribute() {
        Arrays.sort(attributeBreakPoints);
    }

    public int[] GetIndexAttributes() {
        int[] indexAttributs = new int[numAttributeBreakPoints];

        for (int i = 0; i < indexAttributs.length; i++) {
            indexAttributs[i] = attributeBreakPoints[i].indexAttribute;
        }

        return indexAttributs;
    }

    public void ShowBreakPoint() {
        System.out.println("Break Point:");
        for (int i = 0; i < numAttributeBreakPoints; i++) {

            attributeBreakPoints[i].ShowBreakPoint();

        }
        System.out.println();
    }
}
