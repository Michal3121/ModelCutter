/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelcutter;

import javax.vecmath.Point3f;

/**
 *
 * @author MICHAL
 */
public interface Plane {
    public Point3f getCenterPoint();
    
    public Point3f getNormal();
    
    public boolean isIntersecting(Point3f coord1, Point3f coord2);
    
    public Point3f getIntersectionPoint(Point3f coord1, Point3f coord2);
    
    public Boolean isPointBelongToPlane(Point3f coord);
}
