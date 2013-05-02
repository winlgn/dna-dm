/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package datamining.layout;

import java.util.Properties;

/**
 *
 * @author LiuGuining
 */
public interface ISettingDialog {

    public Properties getProperties();

    public void setProperties(Properties prop);
    
    public void setModal(boolean modal);
    
    public void setVisible(boolean b);
}
