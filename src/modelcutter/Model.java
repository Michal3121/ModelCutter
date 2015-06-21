/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelcutter;

import com.vividsolutions.jts.geom.Coordinate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.vecmath.Point3f;

/**
 *
 * @author MICHAL
 */
public class Model {
    
    private List<ModelFacet> mesh = new ArrayList<>();
    private Map<Long, MVertex> vertices = new HashMap<>();
    private Map<Long, MTriangle> triangleMesh = new HashMap<>();
    private double sizeXmax;
    private double sizeXmin;
    private double sizeYmax;
    private double sizeYmin;
    private double sizeZmax;
    private double sizeZmin;
    
    public Model(Collection<ModelFacet> newModel){
        this.mesh = new ArrayList<>(newModel);
        //this.initMeasurements2();
    }
    
    public Model(Map<Long, MVertex> vertices, Map<Long, MTriangle> triangles){
        this.vertices = vertices;
        this.triangleMesh = triangles;
        this.initMeasurements();
    }
    
    private void initMeasurements(){
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
    
    public int getNumberOfFacet(){
        return mesh.size();
    }
    
    public ModelFacet getFacet(int i){
        return mesh.get(i);
    }
    
    public Collection<ModelFacet> getAllFacet(){
       return  Collections.unmodifiableCollection(mesh);
    }
    
    public Map<Long, MVertex> getVertices() {
        return vertices;
    }

    public Map<Long, MTriangle> getTriangleMesh() {
        return triangleMesh;
    }
    
    /*
    private void initMeasurements2(){
        
        if(mesh.isEmpty()){
            return;
        }
        
        sizeXmax = mesh.get(0).getTriangleCoord(0).x;
        sizeXmin = mesh.get(0).getTriangleCoord(0).x;
        sizeYmax = mesh.get(0).getTriangleCoord(0).y;
        sizeYmin = mesh.get(0).getTriangleCoord(0).y;
        sizeZmax = mesh.get(0).getTriangleCoord(0).z;
        sizeZmin = mesh.get(0).getTriangleCoord(0).z;
       
        for (ModelFacet mesh1 : mesh) {
            for (int j = 0; j < 3; j++) {
                
                double x = mesh1.getTriangleCoord(j).x;
                double y = mesh1.getTriangleCoord(j).y;
                double z = mesh1.getTriangleCoord(j).z;
                
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
    } */
    
    /*
    public Coordinate getModelCenter(){
        
        double maxX = mesh.get(0).getTriangleCoord(0).x;
        double minX = mesh.get(0).getTriangleCoord(0).x;
        double maxY = mesh.get(0).getTriangleCoord(0).y;
        double minY = mesh.get(0).getTriangleCoord(0).y;
        double maxZ = mesh.get(0).getTriangleCoord(0).z;
        double minZ = mesh.get(0).getTriangleCoord(0).z;
       
        for (ModelFacet mesh1 : mesh) {
            for (int j = 0; j < 3; j++) {
                
                double x = mesh1.getTriangleCoord(j).x;
                double y = mesh1.getTriangleCoord(j).y;
                double z = mesh1.getTriangleCoord(j).z;
                
                if(maxX < x){
                    maxX = x;
                }
                if(minX > x){
                    minX = x;
                }
                if(maxY < y){
                    maxY = y;
                }
                if(minY > y){
                    minY = y;
                }
                if(maxZ < z){
                    maxZ = z;
                }
                if(minZ > z){
                    minZ = z;
                }   
            }
        }
        
        return new Coordinate((maxX + minX) / 2, (maxY + minY) / 2, (maxZ + minZ) / 2);
    }
    */

    
}
