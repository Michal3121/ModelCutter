/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelcutter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.vecmath.Point3f;

/**
 *
 * @author MICHAL
 */
public class Model {
    
    private Map<Long, MVertex> vertices = new HashMap<>();
    private Map<Long, MTriangle> triangleMesh = new HashMap<>();
    private double sizeXmax;
    private double sizeXmin;
    private double sizeYmax;
    private double sizeYmin;
    private double sizeZmax;
    private double sizeZmin;
    
    public Model(Map<Long, MVertex> vertices, Map<Long, MTriangle> triangles){
        this.vertices = vertices;
        this.triangleMesh = triangles;
        this.initMeasurements();
    }
    
    private void initMeasurements()
    {
        if(triangleMesh.isEmpty()){
            return;
        }
        
        this.sizeXmax = Float.NEGATIVE_INFINITY;
        this.sizeXmin = Float.POSITIVE_INFINITY;
        this.sizeYmax = Float.NEGATIVE_INFINITY;
        this.sizeYmin = Float.POSITIVE_INFINITY;
        this.sizeZmax = Float.NEGATIVE_INFINITY;
        this.sizeZmin = Float.POSITIVE_INFINITY;
        
        for(Long i : vertices.keySet()){
                        
            double x = vertices.get(i).getVertex().x;
            double y = vertices.get(i).getVertex().y;
            double z = vertices.get(i).getVertex().z;
                
                if(sizeXmax < x){
                    sizeXmax = x;
                }
                if(sizeXmin > x){
                    sizeXmin = x;
                }
                if(sizeYmax < y){
                    sizeYmax = y;
                }
                if(sizeYmin > y){
                    sizeYmin = y;
                }
                if(sizeZmax < z){
                    sizeZmax = z;
                }
                if(sizeZmin > z){
                    sizeZmin = z;
                }
        }
    }
    
    public void deleteTriangleAndUpdateModel(long triangleToDeleteID)
    {
        MTriangle triangleToDelete = this.triangleMesh.get(triangleToDeleteID);
        long[] triangleVerticesID = triangleToDelete.getTriangleVertices();
        List<Long> adjacentTriangleList = triangleToDelete.getAdjacentTriangles();
        
        for(int i = 0; i < adjacentTriangleList.size(); i++){
            this.triangleMesh.get(adjacentTriangleList.get(i)).deleteAdjacentTriangleID(triangleToDeleteID);
        }
        
        for(int i = 0; i < 3; i++){
            this.vertices.get(triangleVerticesID[i]).deleteAdjacentTriangleID(triangleToDeleteID);
        }
        
        this.triangleMesh.remove(triangleToDeleteID);
    }
    
    public double getSizeX(){
        return this.sizeXmax - this.sizeXmin;
    }
    
    public double getSizeY(){
        return this.sizeYmax - this.sizeYmin;
    }
    
    public double getSizeZ(){
        return this.sizeZmax - this.sizeZmin;
    }
    
    
    public Point3f getModelCenter(){
        return new Point3f((float) (this.sizeXmax + this.sizeXmin) / 2,(float) (this.sizeYmax + this.sizeYmin) / 2, 
                (float) (this.sizeZmax + this.sizeZmin) / 2);
    }
    
    public Map<Long, MVertex> getVertices() {
        return vertices;
    }

    public Map<Long, MTriangle> getTriangleMesh() {
        return triangleMesh;
    }
    
}
