/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelcutter;

import com.vividsolutions.jts.geom.Coordinate;

/**
 *
 * @author MICHAL
 */
public class Intersections {
    private Coordinate intersection1;
    private Coordinate intersection2;
    
    public Intersections(Coordinate inter1, Coordinate inter2){
        this.intersection1 = inter1;
        this.intersection2 = inter2;
    }
    
    public Coordinate getIntersection1(){
        return intersection1;
    }
    
    public Coordinate getIntersection2(){
        return intersection2;
    }
    
    public void setIntersection1(Coordinate c){
        this.intersection1 = c;
    }
    
    public void setIntersection2(Coordinate c){
        this.intersection2 = c;
    }
    
    @Override
    public String toString(){
        return "The first intersection: " + intersection1.toString() + ", the second: " + intersection2.toString();
    }
    
    @Override
    public boolean equals(Object o){ 
        if(o == this){
            return true;
        }
        
        if(!(o instanceof Intersections)){
            return false;
        }
        
        Intersections intersect  = (Intersections) o;
        return intersection1.equals3D(intersect.intersection1) && intersection2.equals3D(intersect.intersection2);
    }
    
    @Override
    public int hashCode(){
        int hash = 17;
        hash = hash*37 + intersection1.hashCode();
        hash = hash*37 + intersection2.hashCode();
        return hash;
    }
    
    
}
