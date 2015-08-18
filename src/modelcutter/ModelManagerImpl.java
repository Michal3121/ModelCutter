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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.vecmath.Point3f;
import org.j3d.loaders.InvalidFormatException;
import org.j3d.loaders.stl.STLFileReader;

/**
 *
 * @author MICHAL
 */
public class ModelManagerImpl implements ModelManager {

    private STLFileReader reader;
    private final double[] normal; // kvoli vynimkam atribut
    private final double[][] vertex; // kvoli vynimkam atribut
    //private final List<ModelFacet> mesh;
    private Map<Long, MTriangle> triangleMap; 
    private Map<Long, MVertex> verticesMap; 
    
    public ModelManagerImpl(){
        normal = new double[3];
        vertex = new double[3][3];
        //mesh = new ArrayList<>();
        triangleMap = new HashMap<>();
        verticesMap = new HashMap<>();
    }
      
    @Override
    public Model loadModel(File path){
        try {
            reader = new STLFileReader(path);
        } catch (InvalidFormatException | IOException ex) {
            Logger.getLogger(Model.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        try {
            long vertexID = 0;
            long triangleID = 0;
            Map<Point3f, Long> auxVerticesMap = new HashMap<>();
            
            while(reader.getNextFacet(normal, vertex)){
                
                Point3f triangleNorm = new Point3f((float) normal[0], (float) normal[1], (float) normal[2]);
                Point3f[] triangleVertices = this.transformVerticesToPoint3f(vertex);
                long[] triangleVerticesID = new long[3];
                
                for(int i = 0; i < 3; i++){
                    
                    if(!auxVerticesMap.containsKey(triangleVertices[i])){
                        this.verticesMap.put(vertexID, new MVertex(vertexID, 0, triangleVertices[i], triangleID));
                        //this.verticesMap.get(vertexID).addAdjacentTriangles(triangleID); // !! spoliehame sa na to, ze ziadny vrchol nie je sam
                        auxVerticesMap.put(triangleVertices[i], vertexID);
                        triangleVerticesID[i] = vertexID;
                        vertexID++;
                    }else{
                        long auxIndex = auxVerticesMap.get(triangleVertices[i]);
                        triangleVerticesID[i] = auxIndex; // index trojuholnikov 
                        this.verticesMap.get(auxIndex).addAdjacentTriangles(triangleID);
                    }
                }
                
                triangleMap.put(triangleID, new MTriangle(triangleID, 0, triangleNorm, triangleVerticesID));
                triangleID++;
            }
            
        } catch (InvalidFormatException | IOException ex) {
            Logger.getLogger(ModelManagerImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        this.initAdjacentTriangles();
        System.out.println("Trojuholniky = " + this.triangleMap.keySet().size());
        System.out.println("Vrcholy = " + this.verticesMap.keySet().size());
        System.out.println(this.triangleMap.get(2l).toString());
        System.out.println(this.verticesMap.get(2l).toString());
        return new Model(verticesMap, triangleMap);
    }
    
    private void initAdjacentTriangles(){
        int aux = 0;
        for(long triangleID : this.triangleMap.keySet()){
            MTriangle currentTriangle = this.triangleMap.get(triangleID);
            long[] verticesIDs = currentTriangle.getTriangleVertices();
            List<Long> adjacentTriangles = new ArrayList<>();
            
            for(int k = 0; k < 3; k++){
                List<Long> vertex1 = this.verticesMap.get(verticesIDs[k]).getAdjacentTriangles(); // ziskame prilahle trojuholniky
                List<Long> vertex2 = this.verticesMap.get(verticesIDs[(k+1) % 3]).getAdjacentTriangles();
                
                Set<Long> vertex1aux = new HashSet<>(vertex1); // dame do pomocnej mnoziny
                Set<Long> vertex2aux = new HashSet<>(vertex2);
                
                vertex1aux.retainAll(vertex2aux); // vo vertex1aux zostanu len spolocne prvky s vertex2aux (dva trojuholniky)
                vertex1aux.remove(triangleID); // odstranime trojuholnik, ktory prave prehladavame
                
                if(vertex1aux.size() == 1){
                    //System.out.println("Adjacent pridavam");
                    adjacentTriangles.add(vertex1aux.iterator().next()); //pridame prilahlz trojuholnik
                }else{
                    System.out.println("Chyba///////////////////////////////");
                    aux++;
                }
            }
            
            this.triangleMap.get(triangleID).setAdjacentTriangles(adjacentTriangles);      
        }
        System.out.println("Pocet chybnych " + aux);
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
    public void exportModel(File path, Model model) {
        
        StlTextFile writer = new StlTextFile(path);
        
        try {
            writer.writeToFile(this.facetTransformerToSCAD());
        } catch (IOException ex) {
            Logger.getLogger(ModelManagerImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    public void exportModelBinary(File path, Model model){
        
        StlBinaryFile writer = new StlBinaryFile(path);
        
        try {
            writer.writeToFile(this.facetTransformerToSCAD());
        } catch (IOException ex) {
            Logger.getLogger(ModelManagerImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    private List<Facet> facetTransformerToSCAD(){
        List<Facet> listOfFacetsSCAD = new ArrayList<>();  
        
        for(long key : this.triangleMap.keySet()){
            MTriangle currentTriangle = this.triangleMap.get(key);
            long[] verticesKeys = currentTriangle.getTriangleVertices();
            Point3f triangleNormal = currentTriangle.getTriangleNormal();
            
            Point3f[] triangleVertices = new Point3f[3];
            Coords3d[] coordsSCAD = new Coords3d[3];
            
            for(int i = 0; i < 3; i++){
                triangleVertices[i] = this.verticesMap.get(verticesKeys[i]).getVertex();
                coordsSCAD[i] = new Coords3d((double)triangleVertices[i].x, (double) triangleVertices[i].y, (double) triangleVertices[i].z);
            }
            
            Coords3d normalSCAD = new Coords3d((double) triangleNormal.x,(double) triangleNormal.y,(double) triangleNormal.z);
            Triangle3d triangleSCAD = new Triangle3d(coordsSCAD[0], coordsSCAD[1], coordsSCAD[2]); 
            listOfFacetsSCAD.add(new Facet(triangleSCAD, normalSCAD, Color.lightGray));   
        }
        System.out.println("Ukladanie Trojuholniky = " + this.triangleMap.keySet().size());
        System.out.println("Ukladanie Vrcholy = " + this.verticesMap.keySet().size());
        System.out.println("Ukladanie SCAD List : " + listOfFacetsSCAD.size());
        return listOfFacetsSCAD;
    }
    
    
    /*
    @Override
    public void updateModel(Model model) {
        
    }
    */
    /*
    public Model loadModel2(File path) {
        
        try {
            reader = new STLFileReader(path);
        } catch (InvalidFormatException | IOException ex) {
            Logger.getLogger(Model.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        try {
            while(reader.getNextFacet(normal, vertex)){
                Coordinate triangleNorm = new Coordinate(normal[0], normal[1], normal[2]); 
                Coordinate triangleCoord0 = new Coordinate(vertex[0][0], vertex[0][1], vertex[0][2]);
                Coordinate triangleCoord1 = new Coordinate(vertex[1][0], vertex[1][1], vertex[1][2]);
                Coordinate triangleCoord2 = new Coordinate(vertex[2][0], vertex[2][1], vertex[2][2]);
                
                Triangle triangle = new Triangle(triangleCoord0, triangleCoord1, triangleCoord2);
                mesh.add(new ModelFacet(triangle, triangleNorm));
            }
        } catch (InvalidFormatException | IOException ex) {
            Logger.getLogger(Model.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        System.out.println("load list size" + mesh.size());
        
        return new Model(mesh);
    }
    */
    /*
    private List<Facet> facetTransformer(Model model){
        List<Facet> listOfFacetsSCAD = new ArrayList<>();  
        
        for(int i = 0; i < model.getNumberOfFacet(); i++)
        {
            ModelFacet triangle = model.getFacet(i);
                
            Coords3d coordSCAD_0 = new Coords3d(triangle.getTriangleCoord0().x, triangle.getTriangleCoord0().y, triangle.getTriangleCoord0().z);
            Coords3d coordSCAD_1 = new Coords3d(triangle.getTriangleCoord1().x, triangle.getTriangleCoord1().y, triangle.getTriangleCoord1().z);
            Coords3d coordSCAD_2 = new Coords3d(triangle.getTriangleCoord2().x, triangle.getTriangleCoord2().y, triangle.getTriangleCoord2().z);
                
            Coords3d normalSCAD = new Coords3d(triangle.getNormal().x, triangle.getNormal().y, triangle.getNormal().z);
            
            Triangle3d triangleSCAD = new Triangle3d(coordSCAD_0, coordSCAD_1, coordSCAD_2); 
            
            Facet facetSCAD = new Facet(triangleSCAD, normalSCAD, Color.lightGray); 
            
            listOfFacetsSCAD.add(facetSCAD);
        }
        
        return listOfFacetsSCAD;
    }
    */
}
