/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelcutter;

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
    
    private ModelManagerImpl modelManager;
    
    public Aplication(){
        this.modelManager = new ModelManagerImpl();
    }
    
    /**
     * This method return a collection of all triangles IDs from a model which 
     * are intersecting with a plane. Intersecting means, that triangle 
     * has two intersecting points, one triangle vertex belong to the plane
     * or two vertices belong to the plane. If the model doesn´t 
     * intersecting with the plane, methods return empty collection.
     * 
     * @param model model which is testing to intersect
     * @param plane plane which is tested
     * @return a collection of all triangles IDs from a model which 
     *         are intersecting with a plane or an empty collection 
     *         if the model doesn´t intersecting with the plane
     */
    public Collection<Long> getAllIntersectionTriangles(Model model, Plane plane){
        Map<Long, MTriangle> triangleMesh = model.getTriangleMesh();
        Map<Long, MVertex> triangleVertices = model.getVertices(); 
        Set<Long> intersectionTriangles = new HashSet<>();
         
        for(long triangleID: triangleMesh.keySet()){
            long[] verticesIDs = triangleMesh.get(triangleID).getTriangleVertices();
            
            for(int k = 0; k < 3; k++){
                Point3f vertex1 = triangleVertices.get(verticesIDs[k]).getVertex();
                Point3f vertex2 = triangleVertices.get(verticesIDs[(k+1) % 3]).getVertex();
                
                if(plane.belongToPlane(vertex1) || plane.belongToPlane(vertex2) 
                    || plane.isIntersecting(vertex1, vertex2) ){
                    triangleMesh.get(triangleID).setIntersecting(true);  
                    intersectionTriangles.add(triangleID);
                    break;
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
    /**
     * This method traverse all intersecting triangles. Every triangle 
     * which is intersecting with a plane with one or two new point is
     * divided into smaller triangles and added to model (original triangle
     * is removed). Model does not update. All new triangles does not have
     * initialized adjacent vertices.
     * 
     * @param intersectionTriangles a collection of triangleIDs which are intersecting
     * @param model a model 
     * @param plane a plane which is tested to intersecting
     * @return a model with new smaller triangles, if some triangle is intersecting 
     *         with plane or the same model if does not 
     */
    public Model divideIntersectingTriangles(Collection<Long> intersectionTriangles, Model model, Plane plane){ // POZOR na nazov intersection
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
                }
            }
            
            if(verticesToTriangulation.size() > 3){
                newTriangleList = this.earClipping(verticesToTriangulation, originalTriangle);
                isTrianglesModified = true;
                
                for(MTriangle triangle : newTriangleList){
                    long[] triangleVerticesID = triangle.getTriangleVertices();
                                        
                    for(long vertexID : triangleVerticesID)
                    {
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
            //this.modelManager.updateAdjacentTriangles(model);
            model = this.updateAndDivideModel(model, plane);
            //model = this.updateAndDivideModel(model, plane);
        //}
        
        return  model;
    }
    
    /**
     * This method returns a model which is divided by a plane. All triangles
     * pair from the model which their common side lies on the plane (two 
     * triangle vertices belong to the plane) are separated (deleted their
     * triangle ID in adjacentTriangleList each other). Every triangle which
     * has set isIntersecting() to true is set to false.
     * //of triangle lying on the other side of plane//  
     * 
     * @param model model which is dividing
     * @param plane plane which is tested
     * @return a model which is consist of two or more part 
     *         which are separated from each other and also has
     *         no intersection triangles 
     */
    public Model updateAndDivideModel(Model model, Plane plane){
        Map<Long, MTriangle> triangleMesh = model.getTriangleMesh();
        Map<Long, MVertex> triangleVertices = model.getVertices();
        
        for(long triangleID : triangleMesh.keySet()){
            MTriangle currentTriangle = triangleMesh.get(triangleID);
            
            if(currentTriangle.isIntersecting())
            {
                currentTriangle.setIntersecting(false);
            } 
            
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
                    System.out.println("Chyba //////////////////// ");
                }
            }
            currentTriangle.setAdjacentTriangles(adjacentTrianglesTemp);
        }
        return model;
    }
    
    /**
     * This method return a list of new smaller triangles (MTriangle objects), 
     * which replace an original triangle specified by verticesList. There is 
     * added -1 as triangleID and objectID, as well as normal from original 
     * triangle to each new triangle in the list. It uses a slightly modified 
     * Ear Clipping algorithm.
     * 
     * @param verticesList ring of vertices which are triangulated
     * @param triangle original triangle
     * @return list of new tringles (MTriangle objects)
     */
    private List<MTriangle> earClipping(List<MVertex> verticesList, MTriangle triangle)
    {
        double smalestAngle = 181;
        double currentAngle;
        int smalestAngleIndex = 0;
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
                currentAngle = this.getAngle(verticesList.get(j-1).getVertex(), 
                                             verticesList.get(j % listSize).getVertex(), 
                                             verticesList.get((j+1)% listSize).getVertex());
                
                if(oneIntersection && (verticesList.get(j-1).getObjectID() == -1 
                   || verticesList.get((j+1)% listSize).getObjectID() == -1))
                { 
                   smalestAngleIndex = j;
                   break;
                }
                
                if(verticesList.get(j-1).getObjectID() == -1 
                   && verticesList.get((j+1)% listSize).getObjectID() == -1)
                { 
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
    
    /**
     * This method finds and marks by objectID all strongly connected component 
     * in a model. Strongly connected means, that every triangle is reachable 
     * from every other triangle. One component is represented by a group 
     * of triangles with common objectID. 
     * 
     * @param model model in which is set strongly connected component
     * @return number of component
     */
    public int setComponent(Model model)
    {
        Map<Long, MTriangle> triangleMesh = model.getTriangleMesh();
        
        int counter = 1;
        for(long triangleID : triangleMesh.keySet()){
            MTriangle currentTriangle = triangleMesh.get(triangleID);
            if(currentTriangle.getObjectID() == 0){
                counter++;
                this.doIterativeDFS(model, triangleID, counter);
            }
        }
        System.out.println("Number of parts: " + (counter - 1));
        
        return counter;
        
    }
    
    /**
     * This method do an Iterative Depth-first search algorithm. It traverse 
     * all adjacent triangles of triangle specified by triangleID in a model.  
     * Each of them set objectID specified by counter to find all strongly 
     * connected componet in the model. 
     * 
     * @param model model to traversing
     * @param triangleID triangleID which specified triangle in which 
     *                   adjacent triangles are traversing
     * @param counter it is used to mark objectID in each traversed triangle
     */
    private void doIterativeDFS(Model model, long triangleID, int counter){
        Map<Long, MTriangle> triangleMesh = model.getTriangleMesh();
        List<Long> stack = new ArrayList<>();
        stack.add(triangleID);
        
        while(!stack.isEmpty())
        {
            long currentID = stack.get(0); // take first 
            stack.remove(0);
            MTriangle currentTriangle = triangleMesh.get(currentID);
            if(currentTriangle.getObjectID() == 0)
            {
                currentTriangle.setObjectID(counter); // label as discover
                for(long adjacentID : currentTriangle.getAdjacentTriangles())
                {
                    stack.add(adjacentID);
                }
            }
        }
    }
    
    /**
     * This method returns size of an angle specified by 3 vertices 
     * in degrees. It returns smaller angle.
     * 
     * @param vertex0 vertex0 is outer vertex
     * @param vertex1 vertex1 is middle vertex
     * @param vertex2 vertex2 is outher vertex
     * @return size of an angle in degrees
     */
    private double getAngle(Point3f vertex0, Point3f vertex1, Point3f vertex2)
    {
        Vector3d vector1 = new Vector3d(vertex0.x - vertex1.x, vertex0.y - vertex1.y, vertex0.z - vertex1.z);
        Vector3d vector2 = new Vector3d(vertex2.x - vertex1.x, vertex2.y - vertex1.y, vertex2.z - vertex1.z);
        
        vector1.normalize();
        vector2.normalize();
        
        return Math.toDegrees(Math.acos(vector1.dot(vector2))) ;
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
