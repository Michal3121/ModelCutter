/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelcutter;

import java.util.Arrays;
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
    private long[] adjacentTriangles = new long[3];
    private boolean intersecting;

    public MTriangle(long triangleID, long objectID, Point3f norm, long[] triangleVertices) {
        this.triangleID = triangleID;
        this.objectID = objectID;
        this.triangleNormal = norm;
        this.triangleVertices = triangleVertices;
        this.intersecting = false;
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

    public long[] getAdjacentTriangles() {
        return adjacentTriangles;
    }

    public void setAdjacentTriangles(long[] adjacentTriangles) {
        this.adjacentTriangles = adjacentTriangles;
    }

    public boolean isIntersecting() {
        return intersecting;
    }

    public void setIntersecting(boolean isIntersecting) {
        this.intersecting = isIntersecting;
    }
    
    @Override
    public String toString() {
        return String.format("Triangle ID: %d; vertices IDs: %s; adjacent triangles: %s ", 
                            this.triangleID, Arrays.toString(this.triangleVertices), 
                            Arrays.toString(this.adjacentTriangles));
    }
}
