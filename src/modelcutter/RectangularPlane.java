/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelcutter;

import javax.vecmath.Point2f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

/**
 *
 * @author MICHAL
 */
public class RectangularPlane extends GeneralPlane implements Plane {
    
    private float width;
    private float length;

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getLength() {
        return length;
    }

    public void setLength(float length) {
        this.length = length;
    }
    
    public RectangularPlane(Point3f point, Vector3f normal, float width, float length){
        super(point, normal);
        this.width = width;
        this.length = length;
    }

    @Override
    public boolean isIntersecting(Point3f coord1, Point3f coord2) {
        if(!super.isIntersecting(coord1, coord2)){
            return false;
        }
        Point3f intersectPoint = super.getIntersectionPoint(coord1, coord2);
        Point2f intersectPoint2D = super.getCenteredProjectionPoint(intersectPoint);
        
        return this.isPointInRectangle(intersectPoint2D, this.width, this.length);
    }

    @Override
    public Boolean isPointBelongToPlane(Point3f coord) {
        if(!super.isPointBelongToPlane(coord)){
            return false;
        }
        Point2f point2D = super.getCenteredProjectionPoint(coord);
        
        return this.isPointInRectangle(point2D, this.width, this.length);
    }
    
    private Boolean isPointInRectangle(Point2f point, float width, float length){
        if(point.x > width / 2 || point.x < -width / 2){
            return false;
        }
        
        if(point.y > length / 2 || point.y < -length / 2){
            return false;
        }
        
        return true;
    }
}
