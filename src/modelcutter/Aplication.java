/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelcutter;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Triangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.vecmath.Point3f;


/**
 *
 * @author MICHAL
 */
public class Aplication {
    
    
    public Collection<Long> getTriangleRings(Model model, Plane plane){
        Map<Long, MTriangle> triangleMesh = model.getTriangleMesh();
        Map<Long, MVertex> triangleVertices = model.getVertices(); 
        Set<Point3f> ring = new HashSet<>(); 
        Set<Long> intersectionTriangles = new HashSet<>();
        int aux = 0;
        int pocetPrechodov = 0;
        
        for(long triangleID: triangleMesh.keySet()){
            long[] verticesIDs = triangleMesh.get(triangleID).getTriangleVertices();
            
            for(int k = 0; k < 3; k++){
                Point3f vertex1 = triangleVertices.get(verticesIDs[k]).getVertex();
                Point3f vertex2 = triangleVertices.get(verticesIDs[(k+1) % 3]).getVertex();
                
                //System.out.println("krok " + k + " vertex1 " + vertex1.toString());
                //System.out.println("krok " + (k+1) % 3 + " vertex2 " + vertex2.toString());
                
                pocetPrechodov++;
                if(plane.isIntersecting(vertex1, vertex2)){
                    ring.add(vertex1);
                    ring.add(vertex2);
                    intersectionTriangles.add(triangleID);
                    aux++;
                    
                    //System.out.println("Pretla");
                    //System.out.println("Vrchol 1" + vertex1.toString());
                    //System.out.println("Vrchol 2" + vertex2.toString());
                }
            }
            //System.out.println("------------------------------------------------------");
        }  
        
        System.out.println("Rovina pretla model: " + aux);
        System.out.println("Pocet prechodov: " + pocetPrechodov);
        System.out.println("Pocet trojuholnikov: " + intersectionTriangles.size());
        return Collections.unmodifiableCollection(intersectionTriangles);
    }
    
    public void getListsOfParts(Set<Long> triangleRing, Model model){
        List<Long> triangleList = new ArrayList<>(triangleRing);
        Map<Long, MTriangle> triangleMesh = model.getTriangleMesh();
        Map<Long, MVertex> triangleVertices = model.getVertices(); 
        long nextTriangle;
        List<List<Long>> parts = new ArrayList<>();
        
        if(!triangleRing.isEmpty()){
            nextTriangle = triangleList.get(0);
            parts.add(new ArrayList<Long>());
            parts.get(0).add(nextTriangle);
            triangleRing.remove(nextTriangle);
        }else{
            return;
        }
        
        while(!triangleRing.isEmpty()){
            for(int j = 1; j < triangleRing.size(); j++){
                long[] adjacentTriangleIDs = triangleMesh.get(nextTriangle).getTriangleVertices();

                for(int i = 0; i < 3; i++){
                    if (triangleRing.contains(adjacentTriangleIDs[i])){
                        nextTriangle = adjacentTriangleIDs[i];
                        //part.add(nextTriangle);
                        triangleRing.remove(nextTriangle);
                        return;
                    } 
                }
            }
        }
        
        List<Long> currentPart;
        
        while(!triangleRing.isEmpty()){
            
            boolean inOnePart = true;
            while(inOnePart){
                inOnePart = false;
                long[] adjacentTriangleIDs = triangleMesh.get(nextTriangle).getTriangleVertices();

                for(int i = 0; i < 3; i++){
                    if (triangleRing.contains(adjacentTriangleIDs[i])){
                        nextTriangle = adjacentTriangleIDs[i];
                        currentPart = parts.get(parts.size()-1); // vyberieme posledny zoznam
                        currentPart.add(nextTriangle);
                        triangleRing.remove(nextTriangle);
                        inOnePart = true;
                    }
                }
            }
            
            if(inOnePart == false && !triangleRing.isEmpty()){
                parts.add(new ArrayList<Long>());
                nextTriangle = triangleRing.iterator().next();
            }      
        }
        
    }
    
    
    
    //----//
    public void getRing(Model model, Plane plane){
        
        for(int i = 0; i < model.getNumberOfFacet(); i++)
        {
            ModelFacet actualFacet = model.getFacet(i);
            Point3f[] triangleCoords = new Point3f[3];
            for(int j = 0; j < 3; j++){
               double x = actualFacet.getTriangleCoord(j).x;
               double y = actualFacet.getTriangleCoord(j).y;
               double z = actualFacet.getTriangleCoord(j).z;
               
               triangleCoords[j].x = (float) actualFacet.getTriangleCoord(j).x;
               triangleCoords[j].y = (float) actualFacet.getTriangleCoord(j).y;
               triangleCoords[j].z = (float) actualFacet.getTriangleCoord(j).z;      
            }
            
            for(int k = 0; k < 2; k++){
                if(plane.isIntersecting(triangleCoords[k], triangleCoords[(k+1) % 3])){
                    System.out.println("pretina sa");
                }
            }
            
            
        }
    }
    
    
}
