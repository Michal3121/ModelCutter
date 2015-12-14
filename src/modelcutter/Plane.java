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
public interface Plane {
    public Point3f getCenterPoint();
    
    public Vector3f getNormal();
    
    public Point2f getCenteredProjectionPoint(Point3f point3D);
    
    public boolean isIntersecting(Point3f coord1, Point3f coord2);
    
    public boolean isPointUnderPlane(Point3f coord);
    
    public Point3f getIntersectionPoint(Point3f coord1, Point3f coord2);
    
    public Boolean isPointBelongToPlane(Point3f coord);
}
