/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelcutter;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.vecmath.Point3f;

/**
 *
 * @author MICHAL
 */
public class Model {
    
    private String modelName;
    private Color color;
    private boolean visible;
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
    
    public Model(String name, boolean visible, Map<Long, MVertex> vertices, Map<Long, MTriangle> triangles){
        this.modelName = name;
        this.visible = visible;
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

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
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

    public double getSizeXmax() {
        return sizeXmax;
    }

    public double getSizeXmin() {
        return sizeXmin;
    }

    public double getSizeYmax() {
        return sizeYmax;
    }

    public double getSizeYmin() {
        return sizeYmin;
    }

    public double getSizeZmax() {
        return sizeZmax;
    }

    public double getSizeZmin() {
        return sizeZmin;
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

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + Objects.hashCode(this.modelName);
        hash = 67 * hash + Objects.hashCode(this.vertices);
        hash = 67 * hash + Objects.hashCode(this.triangleMesh);
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
        final Model other = (Model) obj;
        if (!Objects.equals(this.modelName, other.modelName)) {
            return false;
        }
        if (!Objects.equals(this.vertices, other.vertices)) {
            return false;
        }
        if (!Objects.equals(this.triangleMesh, other.triangleMesh)) {
            return false;
        }
        return true;
    }
    
}
