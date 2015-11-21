/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelcutter;

import java.util.Comparator;
import javax.vecmath.Point2f;

/**
 *
 * @author MICHAL
 */
public class EdgeWithHelperComparator implements Comparator<EdgeWithHelper> {

    @Override
    public int compare(EdgeWithHelper o1, EdgeWithHelper o2) {
        Point2f o1Point = o1.getIntersectPoint();
        Point2f o2Point = o2.getIntersectPoint();
        
        if(o1Point.x > o2Point.x){
            return 1;
        }
        if(o1Point.x < o2Point.x){
            return -1;
        }
        
        return 0;
    }
    
}
