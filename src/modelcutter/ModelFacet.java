/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelcutter;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Triangle;

/**
 *
 * @author MICHAL
 */
public class ModelFacet {
    
    private Triangle triangle;
    private Coordinate normal;
    
    public ModelFacet(Triangle t, Coordinate n){
        this.triangle = t;
        this.normal = n; 
    }
    
    public Triangle getTriangle(){
        return this.triangle;
    }
    
    public Coordinate getNormal(){
        return normal;
    }
    
    public void setTriangle(Triangle t){
        this.triangle = t;
    }
    
    public void setNormal(Coordinate c){
        this.normal = c;
    }
    
    public Coordinate getTriangleCoord0(){
        return this.triangle.p0;
    }
    
    public Coordinate getTriangleCoord1(){
        return this.triangle.p1;
    }
    
    public Coordinate getTriangleCoord2(){
        return this.triangle.p2;
    }
    
    public Coordinate getTriangleCoord(int numberOfVertex){
        
        Coordinate coord = new Coordinate(0,0,0);
        
        switch(numberOfVertex) {
            case 0: coord = triangle.p0;
                    break;
            case 1: coord = triangle.p1;
                    break;
            case 2: coord = triangle.p2;
                    break;    
        }
        return coord;
    }
    
    @Override
    public String toString(){
        return "Triangle coordinate: " + getTriangleCoord0() + "; " + getTriangleCoord1() +
                "; " + getTriangleCoord2() + ",\nits normal: " + normal.toString();
    }
    
    /*
    @Override
    public boolean equals(Object o){
        
        if(!(o instanceof Facet)){
            return false;
        }
        
        if(o == this){
            return true;
        }
        
        Facet facet = (Facet) o;
        return triangle.equalsExact(facet.triangle) && normal.equals3D(normal);
        
    }
    */ 
}
