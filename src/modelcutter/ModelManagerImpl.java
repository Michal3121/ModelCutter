/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelcutter;


import eu.printingin3d.javascad.coords.Coords3d;
import eu.printingin3d.javascad.coords.Triangle3d;
import eu.printingin3d.javascad.vrl.export.StlBinaryFile;
import eu.printingin3d.javascad.vrl.Facet;
import eu.printingin3d.javascad.vrl.export.StlTextFile;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;
import org.j3d.loaders.InvalidFormatException;
import org.j3d.loaders.stl.STLFileReader;

/**
 *
 * @author MICHAL
 */
public class ModelManagerImpl implements ModelManager {
  
    @Override
    public Model loadModel(File path){
        
        Map<Long, MTriangle> triangleMap = new HashMap<>();
        Map<Long, MVertex> verticesMap = new HashMap<>();
        try {
            STLFileReader reader = new STLFileReader(path);
            long vertexID = 0;
            long triangleID = 0;
            Map<Point3f, Long> auxVerticesMap = new HashMap<>();
            double[] normal = new double[3];
            double[][] vertex = new double[3][3];
        
            while(reader.getNextFacet(normal, vertex)){
                
                Vector3f triangleNorm = new Vector3f((float) normal[0], (float) normal[1], (float) normal[2]);
                Point3f[] triangleVertices = this.transformVerticesToPoint3f(vertex);
                long[] triangleVerticesID = new long[3];
                
                for(int i = 0; i < 3; i++){
                    
                    if(!auxVerticesMap.containsKey(triangleVertices[i])){
                        verticesMap.put(vertexID, new MVertex(vertexID, 0, triangleVertices[i], triangleID));
                        //this.verticesMap.get(vertexID).addAdjacentTriangles(triangleID); // !! spoliehame sa na to, ze ziadny vrchol nie je sam
                        auxVerticesMap.put(triangleVertices[i], vertexID);
                        triangleVerticesID[i] = vertexID;
                        vertexID++;
                    }else{
                        long auxIndex = auxVerticesMap.get(triangleVertices[i]);
                        triangleVerticesID[i] = auxIndex; // index trojuholnikov 
                        verticesMap.get(auxIndex).addAdjacentTriangles(triangleID);
                    }
                }
                
                triangleMap.put(triangleID, new MTriangle(triangleID, 0, triangleNorm, triangleVerticesID));
                triangleID++;
            }
        } catch (InvalidFormatException | IOException ex) {
            Logger.getLogger(Model.class.getName()).log(Level.SEVERE, null, ex);
        }
           
        System.out.println("Trojuholniky = " + triangleMap.keySet().size());
        System.out.println("Vrcholy = " + verticesMap.keySet().size());
        String nameOfModel = this.modelName(path);
        return this.updateAdjacentTriangles(new Model(nameOfModel, true, verticesMap, triangleMap));
    }
    
    public Model updateAdjacentTriangles(Model model){
        Map<Long, MTriangle> triangleMesh = model.getTriangleMesh();
        Map<Long, MVertex> triangleVertices = model.getVertices();
        
        for(long triangleID : triangleMesh.keySet()){
            MTriangle currentTriangle = triangleMesh.get(triangleID);
            long[] verticesIDs = currentTriangle.getTriangleVertices();
            List<Long> adjacentTriangles = new ArrayList<>();
            
            for(int k = 0; k < 3; k++)
            {
                MVertex vertex1 = triangleVertices.get(verticesIDs[k]);
                MVertex vertex2 = triangleVertices.get(verticesIDs[(k+1) % 3]);
        
                Set<Long> vertex1aux = new HashSet<>(vertex1.getAdjacentTriangles()); // prilahle trojuholniky dame do pomocnej mnoziny
                Set<Long> vertex2aux = new HashSet<>(vertex2.getAdjacentTriangles());
                
                vertex1aux.retainAll(vertex2aux); // vo vertex1aux zostanu len spolocne prvky s vertex2aux (dva trojuholniky)
                vertex1aux.remove(triangleID); // odstranime trojuholnik, ktory prave prehladavame
                
                if(vertex1aux.size() == 1){
                    adjacentTriangles.add(vertex1aux.iterator().next()); //pridame prilahly trojuholnik
                }else{
                    System.out.println("Chyba///////////////////////////////");
                }
            }
            triangleMesh.get(triangleID).setAdjacentTriangles(adjacentTriangles);      
        }
        return model;
    }       
    
    private Point3f[] transformVerticesToPoint3f(double[][] vertices)
    {
        Point3f[] triangleVertices = new Point3f[3];
        
        for(int i = 0; i < 3; i++){
            triangleVertices[i] = new Point3f((float) vertices[i][0], (float) vertices[i][1], (float) vertices[i][2]);
        }
            
        return triangleVertices;
    }
    
    @Override
    public Void exportModel(File path, List<Model> models) {
        
        StlTextFile writer = new StlTextFile(path);
        
        try {
            writer.writeToFile(this.facetTransformerToSCAD(models));
        } catch (IOException ex) {
            Logger.getLogger(ModelManagerImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null; // docasne riesenie pouzit lambdu z Javy 8
    }
    
    public void exportModelBinary(File path, List<Model> models){
        
        StlBinaryFile writer = new StlBinaryFile(path);
        
        try {
            writer.writeToFile(this.facetTransformerToSCAD(models));
        } catch (IOException ex) {
            Logger.getLogger(ModelManagerImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    private List<Facet> facetTransformerToSCAD(List<Model> models){
        List<Facet> listOfFacetsSCAD = new ArrayList<>();
        
        for(Model currModel : models){
            Map<Long, MTriangle> triangleMap = currModel.getTriangleMesh();
            Map<Long, MVertex> verticesMap = currModel.getVertices(); 


            for(long key : triangleMap.keySet()){
                MTriangle currentTriangle = triangleMap.get(key);
                long[] verticesKeys = currentTriangle.getTriangleVertices();
                Vector3f triangleNormal = currentTriangle.getTriangleNormal();

                Point3f[] triangleVertices = new Point3f[3];
                Coords3d[] coordsSCAD = new Coords3d[3];

                for(int i = 0; i < 3; i++){
                    triangleVertices[i] = verticesMap.get(verticesKeys[i]).getVertex();
                    coordsSCAD[i] = new Coords3d((double)triangleVertices[i].x, (double) triangleVertices[i].y, (double) triangleVertices[i].z);
                }

                Coords3d normalSCAD = new Coords3d((double) triangleNormal.x,(double) triangleNormal.y,(double) triangleNormal.z);
                Triangle3d triangleSCAD = new Triangle3d(coordsSCAD[0], coordsSCAD[1], coordsSCAD[2]); 
                listOfFacetsSCAD.add(new Facet(triangleSCAD, normalSCAD, Color.lightGray));   
            }
            System.out.println("Ukladanie Trojuholniky = " + triangleMap.keySet().size());
            System.out.println("Ukladanie Vrcholy = " + verticesMap.keySet().size());
        }
        System.out.println("Ukladanie SCAD List : " + listOfFacetsSCAD.size());
        return listOfFacetsSCAD;
    }
    
    /*
    @Override
    public void updateModel(Model model) {
        
    }
    */
    
    public long findMaxVertexID(Model model){
        Map<Long, MVertex> verticesMap = model.getVertices();
        long maxID = -1;
        
        for(long currVertexID : verticesMap.keySet()){
            
            if(maxID < currVertexID){
                maxID = currVertexID;
            }
        }
        
        return maxID;
    }
    
    public long findMaxTriangleID(Model model){
        Map<Long, MTriangle> triangleMap = model.getTriangleMesh();
        long maxID = -1;
        
        for(long currTriangleID : triangleMap.keySet()){
            
            if(maxID < currTriangleID){
                maxID = currTriangleID;
            }
        }
        
        return maxID;
    }
    
    public Point3f findCenterOfModels(List<Model> allModels){ 
        List<Double> xDimensions = new ArrayList<>();
        List<Double> yDimensions = new ArrayList<>();
        List<Double> zDimensions = new ArrayList<>();
        
        for(Model currModel : allModels){
            xDimensions.add(currModel.getSizeXmin());
            xDimensions.add(currModel.getSizeXmax());
            
            yDimensions.add(currModel.getSizeYmin());
            yDimensions.add(currModel.getSizeYmax());
            
            zDimensions.add(currModel.getSizeZmin());
            zDimensions.add(currModel.getSizeZmax());
        }
        
        double sizeXmin = Collections.min(xDimensions);
        double sizeXmax = Collections.max(xDimensions);
        double sizeYmin = Collections.min(yDimensions);
        double sizeYmax = Collections.max(yDimensions);
        double sizeZmin = Collections.min(zDimensions);
        double sizeZmax = Collections.max(zDimensions);
        
        return new Point3f((float) (sizeXmax + sizeXmin) / 2,
                           (float) (sizeYmax + sizeYmin) / 2, 
                           (float) (sizeZmax + sizeZmin) / 2);
    }
    
    public double findLargestDimension(Model model){
        List<Double> dimensions = new ArrayList<>();
        
        dimensions.add(model.getSizeX());
        dimensions.add(model.getSizeY());
        dimensions.add(model.getSizeZ());
        
        return Collections.max(dimensions);
    }
    
    private String modelName(File path){
        String nameOfModel = path.getName();
        
        return nameOfModel.replaceFirst("[.][^.]+$", "");
    }
}
