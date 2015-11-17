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
public class HEVertexPositionComparator implements Comparator<HEVertex> {

    @Override
    public int compare(HEVertex o1, HEVertex o2) {
        Point2f o1Point = o1.getVertex();
        Point2f o2Point = o2.getVertex();
        
        if(o1Point.y < o2Point.y){
            return 1;
        }
        if(o1Point.y > o2Point.y){
            return -1;
        }
        if(o1Point.x > o2Point.x){
            return 1;
        }
        if(o1Point.x < o2Point.x){
            return -1;
        }
        
        return 0;
    }
    
}
