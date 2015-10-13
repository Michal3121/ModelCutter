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
public class CircularPlane extends GeneralPlane implements Plane {
    
    private float radius;

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }
    
    public CircularPlane(Point3f point, Point3f normal, float radius){
        super(point, normal);
        this.radius = radius;
    }

    @Override
    public boolean isIntersecting(Point3f coord1, Point3f coord2) {
        if(!super.isIntersecting(coord1, coord2)){
            return false;
        }
        Point3f intersectionPoint = super.getIntersectionPoint(coord1, coord2);
        Point2f intersectionPoint2D = super.getCenteredProjectionPoint(intersectionPoint);
        Point2f centerPoint2D = super.getCenteredProjectionPoint(super.getCenterPoint());
        
        return centerPoint2D.distance(intersectionPoint2D) <= this.radius;
    }

    @Override
    public Boolean isPointBelongToPlane(Point3f coord) {
        if(!super.isPointBelongToPlane(coord)){
            return false; 
        }
        Point2f point2D = super.getCenteredProjectionPoint(coord);
        Point2f centerPoint2D = super.getCenteredProjectionPoint(super.getCenterPoint());
        
        return centerPoint2D.distance(point2D) <= this.radius;
    }
}
