/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelcutter;

import javax.vecmath.GMatrix;
import javax.vecmath.Matrix3d;
import javax.vecmath.Point2f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;

/**
 *
 * @author MICHAL
 */
public class GeneralPlane {
    
    private Point3f centerPoint;
    private Point3f normal;
    private Matrix3d transformationMatrix;

    public GeneralPlane(Point3f point, Point3f normal){
        this.centerPoint = point;
        this.normal = normal;
        this.transformationMatrix = this.computeTransformationMatrix();
    }

    public GeneralPlane(Point3f centerPoint) {
        this.centerPoint = centerPoint;
        //this.normal = new Point3f(1.0f, -1.0f, 0.0f);
        this.normal = new Point3f(0.0f, 1.0f, 0.0f);
        //this.normal = new Point3f(0.0f, 0.0f, 1.0f);
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
    
    public Matrix3d getTransformationMatrix() {
        return transformationMatrix;
    }

    public void setTransformationMatrix(Matrix3d transformationMatrix) {
        this.transformationMatrix = transformationMatrix;
    }
    
    /**
     * This method return true if a line segment connecting a point coord1 
     * and a point coord2 intersecting a plane in a one new point. 
     * 
     * @param coord1 the first end point of a line whose intersection 
     *               with a plane is to be tested
     * @param coord2 the second end point of a line whose intersection 
     *               with a plane is to be tested
     * @return true if a line segment intersects a plane in a one new point
     */
    public boolean isIntersecting(Point3f coord1, Point3f coord2)
    {    
        Vector3d normalVec = new Vector3d(this.normal.x, this.normal.y, this.normal.z);
        Vector3d centerVec = new Vector3d(this.centerPoint.x, this.centerPoint.y, this.centerPoint.z);
        Vector3d coord1Vec = new Vector3d(coord1.x, coord1.y, coord1.z);
        Vector3d coord2Vec = new Vector3d(coord2.x, coord2.y, coord2.z);
        
        //double distance = Math.abs(normalVec.dot(centerVec));
        double distance = normalVec.dot(centerVec);
        double sgn1 = Math.signum(normalVec.dot(coord1Vec) - distance);
        double sgn2 = Math.signum(normalVec.dot(coord2Vec) - distance);
        
        return sgn1 != sgn2;
    }
    
    public boolean isPointUnderPlane(Point3f coord){
        Vector3d normalVec = new Vector3d(this.normal.x, this.normal.y, this.normal.z);
        Vector3d centerVec = new Vector3d(this.centerPoint.x, this.centerPoint.y, this.centerPoint.z);
        Vector3d coordVec = new Vector3d(coord.x, coord.y, coord.z);
        
        double distance = normalVec.dot(centerVec);
        double ret = normalVec.dot(coordVec) - distance;
        ret = (double) Math.round(ret * 1000) / 1000;
        
        return ret < 0;
    }
    
    /**
     * This method return a new point of a Point3f value which lies 
     * on a line segment connecting a point coord1 and point coord2.
     * 
     * @param coord1 the first end point of a line whose intersection
     *               with a plane to gain a new point is to be tested
     * @param coord2 the second end point of a line whose intersection
     *               with a plane to gain a new point is to be tested
     * @return a new point of a Point3f value which lies on a line segment
     */
    public Point3f getIntersectionPoint(Point3f coord1, Point3f coord2)
    {
        Vector3d normalVec = new Vector3d(this.normal.x, this.normal.y, this.normal.z);
        Vector3d centerVec = new Vector3d(this.centerPoint.x, this.centerPoint.y, this.centerPoint.z);
        Vector3d coord1Vec = new Vector3d(coord1.x, coord1.y, coord1.z);
        Vector3d coord2Vec = new Vector3d(coord2.x, coord2.y, coord2.z);
        
        double distance = normalVec.dot(centerVec);
        double dotProduct1 = normalVec.dot(coord1Vec);
        double dotProduct2 = normalVec.dot(coord2Vec);
        
        double fraction1 =  ((dotProduct1 - distance) / (dotProduct2 - dotProduct1));
        double fraction2 =  ((dotProduct2 - distance) / (dotProduct2 - dotProduct1));
        
        coord1Vec.scale(fraction2);
        coord2Vec.scale(fraction1);
        coord1Vec.sub(coord2Vec);
        
        return new Point3f((float) coord1Vec.x, (float) coord1Vec.y, (float) coord1Vec.z);
    }
    
    /**
     * This method return true if a point coord is lying on a plane.
     * 
     * @param coord point whose presence in a plane is to be tested
     * @return true if a point coord is lying on a plane
     */
    public Boolean isPointBelongToPlane(Point3f coord) // isPointLyingOnPlane
    {
        Vector3d normalVec = new Vector3d(this.normal.x, this.normal.y, this.normal.z);
        Vector3d centerVec = new Vector3d(this.centerPoint.x, this.centerPoint.y, this.centerPoint.z);
        Vector3d coordVec = new Vector3d(coord.x, coord.y, coord.z);
        
        double distance = normalVec.dot(centerVec);
        double ret = normalVec.dot(coordVec) - distance; 
        
       //return ((double) Math.round(ret * 10000) / 10000) == 0.0; 
       return ((double) Math.round(ret * 1000) / 1000) == 0.0;
       //return ((double) Math.round(ret * 100) / 100) == 0.0;
       
        //double sgn = Math.signum(normalVec.dot(coordVec) - distance); // povodne testovane
        //return sgn == 0;
    }
    
    private Vector3d getPerpendicularVector(Vector3d inputVec){
        if(inputVec.length() == 0.0){
            System.out.println("Chyba ///////");
        }
        if(inputVec.x == 0.0){
            return new Vector3d(1, 0, 0);
        }
        if(inputVec.y == 0.0){
            return new Vector3d(0, 1, 0);
        }
        if(inputVec.z == 0.0){
            return new Vector3d(0, 0, 1);
        }
        
        double z = (inputVec.x + inputVec.y) / inputVec.z;
        return new Vector3d(1, 1, -z);
    }
    
    private Matrix3d computeTransformationMatrix(){
        Vector3d normalVec = new Vector3d(this.normal.x, this.normal.y, this.normal.z);
        normalVec.normalize();
        Vector3d perpendicVec1 = this.getPerpendicularVector(normalVec);
        perpendicVec1.normalize();
        Vector3d perpendicVec2 = new Vector3d(0, 0, 0);
        perpendicVec2.cross(normalVec, perpendicVec1);
        perpendicVec2.normalize();
        
        Matrix3d transformMatrix = new Matrix3d(perpendicVec1.x, perpendicVec2.x, normalVec.x,
                                                perpendicVec1.y, perpendicVec2.y, normalVec.y,
                                                perpendicVec1.z, perpendicVec2.z, normalVec.z);
        transformMatrix.invert();
        return transformMatrix;
    }
    
    private Point2f getProjectionPoint(Point3f point3D){
        GMatrix vector = new GMatrix(3, 1, new double[] {(double) point3D.x, 
                                                         (double) point3D.y, 
                                                         (double) point3D.z});
        GMatrix matrix = new GMatrix(3, 3);
        matrix.set(this.transformationMatrix);
        
        GMatrix result = new GMatrix(3, 1); 
        result.mul(matrix, vector);
        
        if(Math.round(result.getElement(2, 0) * 1000) / 1000 != 0){
            System.out.println("Chyba////////");
        }
        
        return new Point2f((float) result.getElement(0, 0), (float) result.getElement(1, 0));
    }
    
    public Point2f getCenteredProjectionPoint(Point3f point3D){
        Point2f projectPoint = this.getProjectionPoint(point3D);
        Point2f projectCenter = this.getProjectionPoint(this.centerPoint);
        
        return new Point2f(projectPoint.x - projectCenter.x, projectPoint.y - projectCenter.y);
    }
    
    /*
    public Point3f getCorner(){
        Vector3d normalVec = new Vector3d(normal.x, normal.y, normal.z);
        
    }*/
}
