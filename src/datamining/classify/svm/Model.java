/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package datamining.classify.svm;

/**
 *
 * @author lenovo
 */
public class Model {

    Parameter param;	// parameter
    int nr_class;		// number of classes, = 2 in regression/one class svm
    int l;			// total #SV
    Node[][] SV;	// SVs (SV[l])
    double[][] sv_coef;	// coefficients for SVs in decision functions (sv_coef[k-1][l])
    double[] rho;		// constants in decision functions (rho[k*(k-1)/2])
    double[] probA;         // pariwise probability information
    double[] probB;
    // for classification only
    int[] label;		// label of each class (label[k])
    int[] nSV;		// number of SVs for each class (nSV[k])
    // nSV[0] + nSV[1] + ... + nSV[k-1] = l
}
