/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelcutter;

import java.util.ArrayList;
import java.util.List;
import javax.vecmath.Point3f;

/**
 *
 * @author MICHAL
 */
public class MVertex {
    private final long vertexID;
    private long objectID;
    private Point3f vertex;
    private List<Long> adjacentTriangles = new ArrayList<>();
    
    public MVertex(long id, long objectID, Point3f vertex, Long triangleID){
        this.vertexID = id;
        this.objectID = objectID;
        this.vertex = vertex;
        this.adjacentTriangles.add(triangleID);
    }
    
    public MVertex(long id, long objectID, Point3f vertex){
        this.vertexID = id;
        this.objectID = objectID;
        this.vertex = vertex;
    }
    
    public long getVertexID() {
        return vertexID;
    }

    public long getObjectID() {
        return objectID;
    }

    public void setObjectID(long objectID) {
        this.objectID = objectID;
    }

    public Point3f getVertex() {
        return vertex;
    }

    public void setVertex(Point3f vertex) {
        this.vertex = vertex;
    }

    public List<Long> getAdjacentTriangles() {
        return adjacentTriangles;
    }

    public void setAdjacentTriangles(List<Long> adjacentTriangles) {
        this.adjacentTriangles = adjacentTriangles;
    }
    
    public void addAdjacentTriangles(long triangleID){
        this.adjacentTriangles.add(triangleID);
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
        return String.format("Vertex ID: %d; x = %f, y = %f, z = %f; adjacentTriangles ID: %s ", 
                            this.vertexID, this.vertex.x, this.vertex.y, 
                            this.vertex.z, this.adjacentTriangles.toString());
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 43 * hash + (int) (this.vertexID ^ (this.vertexID >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MVertex other = (MVertex) obj;
        if (this.vertexID != other.vertexID) {
            return false;
        }
        return true;
    }
      
}
