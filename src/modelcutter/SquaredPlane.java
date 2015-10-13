/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelcutter;

import javax.vecmath.Point2f;
import javax.vecmath.Point3f;

/**
 *
 * @author MICHAL
 */
public class SquaredPlane extends GeneralPlane implements Plane {
    
    private float width;

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }
    
    public SquaredPlane(Point3f point, Point3f normal, float width){
        super(point, normal);
        this.width = width;
    }

    @Override
    public boolean isIntersecting(Point3f coord1, Point3f coord2) {
        if(!super.isIntersecting(coord1, coord2)){
            return false;
        } 
        Point3f intersectPoint = super.getIntersectionPoint(coord1, coord2);
        Point2f intersectPoint2D = super.getCenteredProjectionPoint(intersectPoint);
        
        return this.isPointInSquare(intersectPoint2D, this.width);
    }

    @Override
    public Boolean isPointBelongToPlane(Point3f coord) {
        if(super.isPointBelongToPlane(coord)){
            return false;
        } 
        Point2f point2D = super.getCenteredProjectionPoint(coord);
        
        return this.isPointInSquare(point2D, this.width);
    }
    
    private Boolean isPointInSquare(Point2f point, float width){
        if(point.x > width || point.x < -width){
            return false;
        }
        
        if(point.y > width || point.y < -width){
            return false;
        }
        
        return true;
    }
    
}
