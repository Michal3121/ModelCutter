/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelcutter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.vecmath.Point3f;

/**
 *
 * @author MICHAL
 */
public class MTriangle {
    private final long triangleID;
    private long objectID;
    private Point3f triangleNormal;
    private long[] triangleVertices = new long[3];
    private List<Long> adjacentTriangles;
    private boolean intersecting;

    public MTriangle(long triangleID, long objectID, Point3f norm, long[] triangleVertices) {
        this.triangleID = triangleID;
        this.objectID = objectID;
        this.triangleNormal = norm;
        this.triangleVertices = triangleVertices;
        this.intersecting = false;
        this.adjacentTriangles = new ArrayList<>();
    }

    public long getTriangleID() {
        return triangleID;
    }

    public long getObjectID() {
        return objectID;
    }

    public void setObjectID(long objectID) {
        this.objectID = objectID;
    }

    public Point3f getTriangleNormal() {
        return triangleNormal;
    }

    public void setTriangleNormal(Point3f triangleNormal) {
        this.triangleNormal = triangleNormal;
    }

    public long[] getTriangleVertices() {
        return triangleVertices;
    }

    public void setTriangleVertices(long[] triangleVertices) {
        this.triangleVertices = triangleVertices;
    }

    public List<Long> getAdjacentTriangles() {
        return adjacentTriangles;
    }

    public void setAdjacentTriangles(List<Long> adjacentTriangles) { //POZOR na spravne vlozenie
        this.adjacentTriangles = adjacentTriangles;
    }

    public boolean isIntersecting() {
        return intersecting;
    }

    public void setIntersecting(boolean isIntersecting) {
        this.intersecting = isIntersecting;
    }
    
    public Boolean deleteAdjacentTriangleID(long id){
        
        if(this.adjacentTriangles.contains(id)){ // Ceknut remove
            this.adjacentTriangles.remove(id);
            return true;
        }
        return false;   
    }
    
    @Override
    public String toString() {
        return String.format("Triangle ID: %d; vertices IDs: %s; adjacent triangles: %s ", 
                            this.triangleID, Arrays.toString(this.triangleVertices), 
                            this.adjacentTriangles.toString());
    }
}
