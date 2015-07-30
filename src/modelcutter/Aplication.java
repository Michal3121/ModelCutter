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
    
    public Collection<Long> getAllIntersectionTriangles(Model model, Plane plane){
        Map<Long, MTriangle> triangleMesh = model.getTriangleMesh();
        Map<Long, MVertex> triangleVertices = model.getVertices(); 
        Set<Point3f> ring = new HashSet<>(); 
        Set<Long> intersectionTriangles = new HashSet<>();
        int pocetPretnuti = 0;
        int pocetPrechodov = 0;
        
        for(long triangleID: triangleMesh.keySet()){
            long[] verticesIDs = triangleMesh.get(triangleID).getTriangleVertices();
            
            for(int k = 0; k < 3; k++){
                Point3f vertex1 = triangleVertices.get(verticesIDs[k]).getVertex();
                Point3f vertex2 = triangleVertices.get(verticesIDs[(k+1) % 3]).getVertex();
                
                pocetPrechodov++;
                if(plane.isIntersecting(vertex1, vertex2)){
                    ring.add(vertex1);
                    ring.add(vertex2);
                    triangleMesh.get(triangleID).setIntersecting(true);  
                    intersectionTriangles.add(triangleID);
                    pocetPretnuti++;
                }
            }
        }  
        return Collections.unmodifiableCollection(intersectionTriangles);
    }
    
    public List<List<Long>> getListsOfParts(Set<Long> allIntersectingTriangles, Model model){
        List<Long> triangleList = new ArrayList<>(allIntersectingTriangles);
        Map<Long, MTriangle> triangleMesh = model.getTriangleMesh();
        long nextTriangleID = -1;
        long firstTriangleInCurrentRing = -1;
        List<List<Long>> allParts = new ArrayList<>(); // list vsetkych ringov 
        List<Long> onePart = new ArrayList<>(); 
        boolean hasAdjacentTriangle = false;
        
        while(!triangleList.isEmpty())
        {
            /*
            if(firstTriangleInCurrentRing == nextTriangleID && firstTriangleInCurrentRing != -1){
                allParts.add(onePart);
                onePart = new ArrayList<>();
                firstTriangleInCurrentRing = -1;
            }*/
           
            if(firstTriangleInCurrentRing == -1){ 
                nextTriangleID = triangleList.get(0);
                firstTriangleInCurrentRing = nextTriangleID;
                onePart.add(nextTriangleID); 
                triangleList.remove(nextTriangleID);
            }
            
            //for(int j = 0; j < allIntersectingTriangles.size(); j++){
            do{
                
                long[] adjacentTriangleIDs = triangleMesh.get(nextTriangleID).getAdjacentTriangles(); // vyberieme vsetky susedne trojuholnikz
                hasAdjacentTriangle = false;
                
                
                for(int i = 0; i < 3; i++){ // najdeme susedny trojuholnik v ringu
                    if (triangleList.contains(adjacentTriangleIDs[i])){
                        nextTriangleID = adjacentTriangleIDs[i];
                        onePart.add(nextTriangleID);
                        triangleList.remove(nextTriangleID);
                        hasAdjacentTriangle = true;
                        break;
                    } 
                }
                if(!hasAdjacentTriangle){
                    for(int i = 0; i < 3; i++){
                        if(firstTriangleInCurrentRing == adjacentTriangleIDs[i]){
                                allParts.add(new ArrayList<>(onePart));
                                onePart = new ArrayList<>();
                                nextTriangleID = firstTriangleInCurrentRing;
                                //firstTriangleInCurrentRing = -1;
                        }
                    }
                }
                
                
                
            }while(nextTriangleID != firstTriangleInCurrentRing) ;   
            
            firstTriangleInCurrentRing = -1;
        }  
        
        return allParts;
    }
    
    public void cutModel(List<List<Long>> listsOfParts,  Model model, Plane plane){
        Map<Long, MTriangle> triangleMesh = model.getTriangleMesh();
        Map<Long, MVertex> triangleVertices = model.getVertices(); 
        
        for(List<Long> item : listsOfParts)
        {
            for(int i = 0; i < item.size(); i++)
            {
                long[] verticesIDs = triangleMesh.get(item.get(i)).getTriangleVertices();
                List<Point3f> triangleVertWithIntersections = new ArrayList<>();
                
                for(int k = 0; k < 3; k++)
                {
                    Point3f vertex1 = triangleVertices.get(verticesIDs[k]).getVertex();
                    Point3f vertex2 = triangleVertices.get(verticesIDs[(k+1) % 3]).getVertex();
                    
                    triangleVertWithIntersections.add(vertex1);
                    if(plane.isIntersecting(vertex1, vertex2))
                    {
                        triangleVertWithIntersections.add(plane.getIntersectionPoint(vertex1, vertex2));
                    }
                }
                
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
    
    private boolean IsPointInPolygon(Point3f point, List<Long> polygon, Model model)
    {
        boolean inside = false;
        Map<Long, MTriangle> triangleMesh = model.getTriangleMesh();
        
        int j = polygon.size() - 1;
        for(int i = 0; i < polygon.size(); i++){
            
        }
        
        
        return false;
    }
    
    
}
