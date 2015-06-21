/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelcutter;

import com.vividsolutions.jts.geom.Coordinate;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;

/**
 *
 * @author MICHAL
 */
public class Plane {
    
    private Point3f centerPoint;
    private Point3f normal;
    private double sizeX;
    private double sizeY;
    
    public Plane(Point3f point, Point3f normal){
        this.centerPoint = point;
        this.normal = normal;
    }

    public Plane(Point3f centerPoint) {
        this.centerPoint = centerPoint;
        this.normal = new Point3f(0.0f, 1.0f, 0.0f);
    }

    public Point3f getCenterPoint() {
        return centerPoint;
    }

    public void setCenterPoint(Point3f centerPoint) {
        this.centerPoint = centerPoint;
    }

    public Point3f getNormal() {
        return normal;
    }

    public void setNormal(Point3f normal) {
        this.normal = normal;
    }
    
    public boolean isIntersecting(Point3f coord1, Point3f coord2){
         
        Vector3d normalVec = new Vector3d(this.normal.x, this.normal.y, this.normal.z);
        Vector3d centerVec = new Vector3d(this.centerPoint.x, this.centerPoint.y, this.centerPoint.z);
        Vector3d coord1Vec = new Vector3d(coord1.x, coord1.y, coord1.z);
        Vector3d coord2Vec = new Vector3d(coord2.x, coord2.y, coord2.z);
        
        //System.out.println("centerVec = " + centerVec.toString() );
        //System.out.println("normalVec = " + normalVec.toString() );
        //double distance = Math.abs(normalVec.dot(centerVec));
        double distance = normalVec.dot(centerVec);
        //System.out.println("distance =" + distance);
        
        
        //double sgn1 = Math.signum(normalVec.dot(coord1Vec) - centerPoint.distance(new Point3f(0.0f, 0.0f, 0.0f)));
        //double sgn2 = Math.signum(normalVec.dot(coord2Vec) - centerPoint.distance(new Point3f(0.0f, 0.0f, 0.0f)));
        
        double sgn1 = Math.signum(normalVec.dot(coord1Vec) - distance);
        double sgn2 = Math.signum(normalVec.dot(coord2Vec) - distance);
        
        //System.out.println("sgn1" + sgn1);
        //System.out.println("sgn2" + sgn2);
        return sgn1 != sgn2;
    }
    
    /*
    public Point3f getCorner(){
        Vector3d normalVec = new Vector3d(normal.x, normal.y, normal.z);
        
    }*/
    
    
}
