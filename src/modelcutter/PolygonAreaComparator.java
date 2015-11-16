/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelcutter;

import java.util.Comparator;

/**
 *
 * @author MICHAL
 */
public class PolygonAreaComparator implements Comparator<HalfEdgeStructure> {

    @Override
    public int compare(HalfEdgeStructure o1, HalfEdgeStructure o2) {
        int area1 = o1.getAreaOfPolygon();
        int area2 = o2.getAreaOfPolygon();
        
        if(area1 > area2){
            return 1;
        }
        
        return -1;
    }
    
}
