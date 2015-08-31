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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;


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
                if(plane.belongToPlane(vertex1) || plane.belongToPlane(vertex2) 
                    || plane.isIntersecting(vertex1, vertex2) ){
                    triangleMesh.get(triangleID).setIntersecting(true);  
                    intersectionTriangles.add(triangleID);
                    pocetPretnuti++;
                    break;
                }  
            }
            /*
            int aux = 0;
            for(int k = 0; k < 3; k++)
            {
                Point3f vertex = triangleVertices.get(verticesIDs[k]).getVertex();
                int currentVertex = this.getPointType(vertex, plane);
                aux += currentVertex;
                
                if(currentVertex == 1){ // Pozor ak bod patri rovine
                    triangleVertices.get(verticesIDs[k]).setObjectID(1);
                }
                
                if(currentVertex == -1){
                    triangleVertices.get(verticesIDs[k]).setObjectID(2);
                }     
            }
      
            if(aux == 3){
                triangleMesh.get(triangleID).setObjectID(1);
            }
            else if(aux == -3){
                triangleMesh.get(triangleID).setObjectID(2);
            }
            else{
                triangleMesh.get(triangleID).setIntersecting(true);  
                intersectionTriangles.add(triangleID);
                pocetPretnuti++;
            }
            */
        }  
        return Collections.unmodifiableCollection(intersectionTriangles);
    }
    /*
    private int getTriangleType(Point3f[] vertices, Plane plane)
    {
        Vector3d normalVec = new Vector3d(plane.getNormal().x, plane.getNormal().y, plane.getNormal().z);
        Vector3d centerVec = new Vector3d(plane.getCenterPoint().x, plane.getCenterPoint().y, plane.getCenterPoint().z);
        
        double distance = normalVec.dot(centerVec); // d
        
        
        for(int k = 0; k < 3; k++){
            aux += this.getPointType(normalVec, new Vector3d(vertices[k].x, vertices[k].y, vertices[k].z), distance);
        }
        
         
    }*/
    
    private int getPointType(Point3f point, Plane plane)
    {
        Vector3d normalVec = new Vector3d(plane.getNormal().x, plane.getNormal().y, plane.getNormal().z);
        Vector3d centerVec = new Vector3d(plane.getCenterPoint().x, plane.getCenterPoint().y, plane.getCenterPoint().z);
        double distance = normalVec.dot(centerVec); // d
        
        Vector3d vertex = new Vector3d(point.x, point.y, point.z);
        double dotProduct = normalVec.dot(vertex);
        double equation = dotProduct - distance;
        if(equation < 0)
        {
            return 1;
        }
        else
        {
            return -1;
        }
        
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
                
                List<Long> adjacentTriangleIDs = triangleMesh.get(nextTriangleID).getAdjacentTriangles(); // vyberieme vsetky susedne trojuholnikz
                hasAdjacentTriangle = false;
                
                
                for(int i = 0; i < 3; i++){ // najdeme susedny trojuholnik v ringu
                    if (triangleList.contains(adjacentTriangleIDs.get(i))){
                        nextTriangleID = adjacentTriangleIDs.get(i);
                        onePart.add(nextTriangleID);
                        triangleList.remove(nextTriangleID);
                        hasAdjacentTriangle = true;
                        break;
                    } 
                }
                if(!hasAdjacentTriangle){
                    for(int i = 0; i < 3; i++){
                        if(firstTriangleInCurrentRing == adjacentTriangleIDs.get(i)){
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
    
    public Model getDividedTriangleFromRing(List<List<Long>> listsOfParts,  Model model, Plane plane){
        Map<Long, MTriangle> triangleMesh = model.getTriangleMesh();
        Map<Long, MVertex> triangleVertices = model.getVertices();
        long verticesID = triangleVertices.size();
        long triangleID = triangleMesh.size();
        Map<Point3f, Long> auxVerticesMap = new HashMap<>();
        
        List<MVertex> triangleVertWithIntersections;
        List<MTriangle> newTriangleList;
        
        for(List<Long> item : listsOfParts)
        {
            for(int i = 0; i < item.size(); i++)
            {
                MTriangle originalTriangle = triangleMesh.get(item.get(i));
                long[] verticesIDs = originalTriangle.getTriangleVertices();
                triangleVertWithIntersections = new ArrayList<>(); 
                
                for(int k = 0; k < 3; k++)
                {
                    MVertex vertex1 = triangleVertices.get(verticesIDs[k]);
                    MVertex vertex2 = triangleVertices.get(verticesIDs[(k+1) % 3]);
                    
                    if(plane.isPointLyingOnPlane(vertex1.getVertex()) && plane.isPointLyingOnPlane(vertex2.getVertex()))
                    {
                        Set<Long> vertex1auxSet = new HashSet<>(vertex1.getAdjacentTriangles());
                        Set<Long> vertex2auxSet = new HashSet<>(vertex2.getAdjacentTriangles());
                        
                        vertex1auxSet.retainAll(vertex2auxSet); 
                        vertex1auxSet.remove(originalTriangle.getTriangleID());
                        
                        if(vertex1auxSet.size() == 1){
                            long commonTriangleID = vertex1auxSet.iterator().next(); // POZOR mozno premenovat common
                            originalTriangle.deleteAdjacentTriangleID(commonTriangleID); // vymazeme iba susedny trojuhonik, nie vrcholy
                        }else{
                            System.out.println("Chyba///////////////////////////////");
                        }
                        break;
                    }
                    
                    triangleVertWithIntersections.add(vertex1);
                    if(plane.isIntersecting(vertex1.getVertex(), vertex2.getVertex()))
                    {
                        Point3f intersectingPoint = plane.getIntersectionPoint(vertex1.getVertex(), vertex2.getVertex());
                        if(!auxVerticesMap.containsKey(intersectingPoint)){
                            verticesID++;
                            auxVerticesMap.put(intersectingPoint, verticesID);
                            MVertex intersectingVertex = new MVertex(verticesID, (long) -1, intersectingPoint);
                            triangleVertWithIntersections.add(intersectingVertex);
                            triangleVertices.put(verticesID, intersectingVertex);
                        }else{
                            long auxVerticesID = auxVerticesMap.get(intersectingPoint);
                            triangleVertWithIntersections.add(triangleVertices.get(auxVerticesID));
                        }
                    }   
                }
                
                newTriangleList = this.earClipping(triangleVertWithIntersections, originalTriangle);
                
                
                for(MTriangle triangle : newTriangleList){
                    long[] triangleVerticesID = triangle.getTriangleVertices();
                    
                    for(long vertexID : triangleVerticesID)
                    {
                        triangleVertices.get(vertexID).addAdjacentTriangles(triangleID);
                    }
                    triangleID++;
                    triangleMesh.put(triangleID, new MTriangle(triangleID, (long) -1, triangle.getTriangleNormal(), triangle.getTriangleVertices())); 
                }
                model.deleteTriangleAndUpdateModel(originalTriangle.getTriangleID()); 
            }
        }
        
        return model;
    }
    
    public Model divideTriangles(Collection<Long> intersectionTriangles, Model model, Plane plane){ // POZOR na nazov intersection
        Map<Long, MTriangle> triangleMesh = model.getTriangleMesh();
        Map<Long, MVertex> triangleVertices = model.getVertices();
        long verticesID = triangleVertices.size();
        long triangleID = triangleMesh.size();
        boolean isTrianglesModified = false;
        Map<Point3f, Long> auxVerticesMap = new HashMap<>();
        
        List<MVertex> verticesToTriangulation;
        List<MTriangle> newTriangleList;
        
        for(long currentTriangleID : intersectionTriangles){
            MTriangle originalTriangle = triangleMesh.get(currentTriangleID);
            long[] verticesIDs = originalTriangle.getTriangleVertices();
            verticesToTriangulation = new ArrayList<>(); 
            
            for(int k = 0; k < 3; k++){
                MVertex vertex1 = triangleVertices.get(verticesIDs[k]);
                MVertex vertex2 = triangleVertices.get(verticesIDs[(k+1) % 3]);
                
                verticesToTriangulation.add(vertex1);
                if(!plane.belongToPlane(vertex1.getVertex()) && !plane.belongToPlane(vertex2.getVertex()) 
                   && plane.isIntersecting(vertex1.getVertex(), vertex2.getVertex()))
                {
                    Point3f intersectingPoint = plane.getIntersectionPoint(vertex1.getVertex(), vertex2.getVertex());
                    
                    int x = (int) intersectingPoint.x;
                    int y = (int) intersectingPoint.y;
                    int z = (int) intersectingPoint.z;
                    
                    if(x == -3 && y == -3 && z == 38){
                        System.out.println("hladany trojuholnik");
                    }
                    
                    
                    if(!auxVerticesMap.containsKey(intersectingPoint)){
                        auxVerticesMap.put(intersectingPoint, verticesID);
                        MVertex intersectingVertex = new MVertex(verticesID, (long) -1, intersectingPoint);
                        verticesToTriangulation.add(intersectingVertex);
                        triangleVertices.put(verticesID, intersectingVertex);
                        verticesID++;
                    }else{
                        long auxVerticesID = auxVerticesMap.get(intersectingPoint);
                        verticesToTriangulation.add(triangleVertices.get(auxVerticesID));
                    }
                }/*
                else if(plane.belongToPlane(vertex1.getVertex()) && plane.belongToPlane(vertex2.getVertex())){
                    Set<Long> vertex1aux = new HashSet<>(vertex1.getAdjacentTriangles()); // dame do pomocnej mnoziny
                    Set<Long> vertex2aux = new HashSet<>(vertex2.getAdjacentTriangles());

                    vertex1aux.retainAll(vertex2aux); // vo vertex1aux zostanu len spolocne prvky s vertex2aux (dva trojuholniky)
                    vertex1aux.remove(triangleID); // odstranime trojuholnik, ktory prave prehladavame
                    if(vertex1aux.size() == 1){
                       //current 
                    }else{
                        System.out.println("Vertex1 " + vertex1.toString());
                        System.out.println("Vertex2 " + vertex2.toString());
                        System.out.println("Common" + vertex1aux.toString());
                        System.out.println("");
                    }
                    
                }*/
            }
            
            if(verticesToTriangulation.size() > 3){ // > 4
                newTriangleList = this.earClipping(verticesToTriangulation, originalTriangle);
                isTrianglesModified = true;
                
                for(MTriangle triangle : newTriangleList){
                    long[] triangleVerticesID = triangle.getTriangleVertices();
                                        
                    //triangleID++;
                    for(long vertexID : triangleVerticesID)
                    {
                        if(vertexID == 6328)
                        {
                            System.out.println("hhh");
                        }
                        triangleVertices.get(vertexID).addAdjacentTriangles(triangleID); // updatovanie vertexov novych trojuholnikov                     
                    }
                    //triangleMesh.put(triangleID, new MTriangle(triangleID, (long) -1, triangle.getTriangleNormal(), triangle.getTriangleVertices())); 
                    triangleMesh.put(triangleID, new MTriangle(triangleID, 0, triangle.getTriangleNormal(), triangle.getTriangleVertices())); 
                    
                    triangleID++;
                }
                model.deleteTriangleAndUpdateModel(originalTriangle.getTriangleID());
            }
        }
        
        //if(isTrianglesModified){
            model = this.updateAndDivideModel(model, plane);
        //}
        
        
        return model;
    }
    
    //nastavi adjacent vertices v kazdom trojuholniku 
    private Model updateAndDivideModel(Model model, Plane plane){
        Map<Long, MTriangle> triangleMesh = model.getTriangleMesh();
        Map<Long, MVertex> triangleVertices = model.getVertices();
        
        for(long triangleID : triangleMesh.keySet()){
            MTriangle currentTriangle = triangleMesh.get(triangleID);
            
            //if(currentTriangle.getAdjacentTriangles().size() < 3){
                long[] verticesIDs = currentTriangle.getTriangleVertices();
                List<Long> adjacentTrianglesTemp = new ArrayList<>();

                for(int k = 0; k < 3; k++)
                {
                    MVertex vertex1 = triangleVertices.get(verticesIDs[k]);
                    MVertex vertex2 = triangleVertices.get(verticesIDs[(k+1) % 3]);
                            
                    Set<Long> vertex1aux = new HashSet<>(vertex1.getAdjacentTriangles()); // dame do pomocnej mnoziny
                    Set<Long> vertex2aux = new HashSet<>(vertex2.getAdjacentTriangles());

                    vertex1aux.retainAll(vertex2aux); // vo vertex1aux zostanu len spolocne prvky s vertex2aux (dva trojuholniky)
                    vertex1aux.remove(triangleID); // odstranime trojuholnik, ktory prave prehladavame

                    if(vertex1aux.size() == 1){
                        if(!plane.belongToPlane(vertex1.getVertex()) || !plane.belongToPlane(vertex2.getVertex())){
                            adjacentTrianglesTemp.add(vertex1aux.iterator().next()); //pridame prilahly trojuholnik
                        }
                    }else{
                        System.out.println("Triangle ID " + currentTriangle.getTriangleID());
                        System.out.println("Vertex1 " + vertex1.toString());
                        System.out.println("Vertex2 " + vertex2.toString());
                        System.out.println("Common" + vertex1aux.toString());
                        System.out.println("");
                    }
                }
                currentTriangle.setAdjacentTriangles(adjacentTrianglesTemp);
            //}
        }
        return model;
    }
    
    private List<MTriangle> earClipping(List<MVertex> verticesList, MTriangle triangle)
    {
        double smalestAngle = 181;
        double currentAngle;
        int smalestAngleIndex = 0;
        int i = 1;
        int listSize;
        long[] triangleVertices = new long[3];
        List<MTriangle> triangleList = new ArrayList<>();
        boolean oneIntersection = false;
        
        if(verticesList.size() == 4){
            oneIntersection = true;
        }
               
        while(verticesList.size() >= 3){
            
            listSize = verticesList.size();
            
            for(int j = 1; j <= listSize; j++){
                currentAngle = this.getAngle(verticesList.get(j-1).getVertex() , verticesList.get(j % listSize).getVertex() , verticesList.get((j+1)% listSize).getVertex());
                
                if(oneIntersection && (verticesList.get(j-1).getObjectID() == -1 
                   || verticesList.get((j+1)% listSize).getObjectID() == -1)){ 
                   smalestAngleIndex = j;
                    break;
                }
                
                if(verticesList.get(j-1).getObjectID() == -1 && verticesList.get((j+1)% listSize).getObjectID() == -1){ 
                    smalestAngleIndex = j;
                    break;
                }
                
                if(currentAngle <= smalestAngle)
                {
                    smalestAngle = currentAngle;
                    smalestAngleIndex = j;
                }
            }
            
            triangleVertices = new long[3];
            triangleVertices[0] = verticesList.get((smalestAngleIndex + listSize - 1) % listSize).getVertexID();
            triangleVertices[1] = verticesList.get(smalestAngleIndex % listSize).getVertexID();
            triangleVertices[2] = verticesList.get((smalestAngleIndex + 1) % listSize).getVertexID();
            
            triangleList.add(new MTriangle((long)-1, (long) -1, triangle.getTriangleNormal(), triangleVertices));
            
            verticesList.remove(smalestAngleIndex % listSize);
            smalestAngle = 181;
        }
        
        return triangleList;
    }
    
    public int setComponent(Model model)
    {
        Map<Long, MTriangle> triangleMesh = model.getTriangleMesh();
        
        int counter = 1;
        for(long triangleID : triangleMesh.keySet()){
            MTriangle currentTriangle = triangleMesh.get(triangleID);
            if(currentTriangle.getObjectID() == 0){
                counter++;
                //this.doDFS(model, currentTriangle, counter);
                this.doIterativeDFS(model, triangleID, counter);
            }
        }
        System.out.println("Number of parts: " + (counter - 1));
        
        return counter;
        
    }
    
    private void doIterativeDFS(Model model, long triangleID, int counter){
        Map<Long, MTriangle> triangleMesh = model.getTriangleMesh();
        List<Long> stack = new ArrayList<>();
        stack.add(triangleID);
        
        while(!stack.isEmpty()){
            long currentID = stack.get(0);
           stack.remove(0);
            MTriangle currentTriangle = triangleMesh.get(currentID);
            if(currentTriangle.getObjectID() == 0){
                currentTriangle.setObjectID(counter); // label as discover
                for(long adjacentID : currentTriangle.getAdjacentTriangles())
                {
                    stack.add(adjacentID);
                }
            }
        }
        
    }
    
    private void doDFS(Model model, MTriangle triangle, int counter){
        
        Map<Long, MTriangle> triangleMesh = model.getTriangleMesh();
        triangle.setObjectID(-1);
        
        for(long triangleID : triangle.getAdjacentTriangles()){
            if(triangleMesh.get(triangleID).getObjectID() == 0){
                this.doDFS(model, triangleMesh.get(triangleID), counter);
            }
        }
        triangle.setObjectID(counter);
    }
    
    private double getAngle(Point3f vertex0, Point3f vertex1, Point3f vertex2)
    {
        Vector3d vector1 = new Vector3d(vertex0.x - vertex1.x, vertex0.y - vertex1.y, vertex0.z - vertex1.z);
        Vector3d vector2 = new Vector3d(vertex2.x - vertex1.x, vertex2.y - vertex1.y, vertex2.z - vertex1.z);
        
        vector1.normalize();
        vector2.normalize();
        
        return Math.toDegrees(Math.acos(vector1.dot(vector2))) ;
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
