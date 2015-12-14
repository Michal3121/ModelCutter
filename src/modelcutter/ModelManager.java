/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelcutter;

import java.io.File;
import java.util.List;

/**
 *
 * @author MICHAL
 */
public interface ModelManager {
    
    Model loadModel(File path);
    
    Void exportModel(File path, List<Model> model);
    
    /*
    void updateModel(Model model);
   */
}
