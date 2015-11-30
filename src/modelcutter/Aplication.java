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
import javax.vecmath.Point2f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;
import poly2Tri.Polygon;
import static poly2Tri.Triangulation.triangulate;



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
                
                if(plane.isPointBelongToPlane(vertex1) || plane.isPointBelongToPlane(vertex2) 
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
                    
                    if(plane.isPointBelongToPlane(vertex1.getVertex()) && plane.isPointBelongToPlane(vertex2.getVertex()))
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
                if(!plane.isPointBelongToPlane(vertex1.getVertex()) && !plane.isPointBelongToPlane(vertex2.getVertex()) 
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
                    if(!plane.isPointBelongToPlane(vertex1.getVertex()) || !plane.isPointBelongToPlane(vertex2.getVertex())){
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
    
    private Collection<Long> getBoundaryTrianglesIDs(Model model, Plane plane){
        Map<Long, MTriangle> triangleMesh = model.getTriangleMesh();
        Map<Long, MVertex> triangleVertices = model.getVertices(); 
        Set<Long> boundaryTriangles = new HashSet<>(); // boundary under the plane
         
        for(long triangleID: triangleMesh.keySet()){
            MTriangle currentTriangle = triangleMesh.get(triangleID);
            List<Long> adjacentTriangles = currentTriangle.getAdjacentTriangles();
            
            if(adjacentTriangles.size() < 3){
                long[] verticesIDs = currentTriangle.getTriangleVertices();
                
                for(long vertexID : verticesIDs){
                    MVertex currentVertex = triangleVertices.get(vertexID);
                    
                    if(plane.isPointUnderPlane(currentVertex.getVertex())){
                        boundaryTriangles.add(triangleID);
                    }
                }
            }
        }
        
        return Collections.unmodifiableCollection(boundaryTriangles);
    }
    
    private List<List<MVertex>> getRingsOfBoundaryVertices(Collection<Long> triangles, Model model, Plane plane){
        Map<Long, MTriangle> triangleMesh = model.getTriangleMesh();
        Map<Long, MVertex> triangleVertices = model.getVertices();
        
        List<Long> boundaryTriangles = new ArrayList<>(triangles);
        List<List<MVertex>> listOfRings = new ArrayList<>();
        List<MVertex> ringOfVertices = new ArrayList<>();
        
        MVertex firstVertexInCurrRing = null;
        MVertex nextVertex = null;
        
        while(!boundaryTriangles.isEmpty()){
            
            if(firstVertexInCurrRing == null){
                long triangleID = boundaryTriangles.get(0);
                MTriangle firstTriangleInCurrRing = triangleMesh.get(triangleID);
                long[] verticesIDs = firstTriangleInCurrRing.getTriangleVertices();
                
                for(int k = 0; k < 3; k++){
                    MVertex vertex = triangleVertices.get(verticesIDs[k]);
                    Point3f point = vertex.getVertex(); 
                   
                    if(plane.isPointBelongToPlane(point)){
                       firstVertexInCurrRing = vertex;
                       /*Set<Long> vertexAux = new HashSet<>(firstVertexInCurrRing.getAdjacentTriangles());
                       Set<Long> allTrianglesAux = new HashSet<>(triangleMesh.keySet());
                       
                       vertexAux.retainAll(allTrianglesAux);
                       vertexAux.remove(firstTriangleInCurrRing.getTriangleID());*/
                       break; 
                    }
                }
                
                nextVertex = firstVertexInCurrRing;
                ringOfVertices = new ArrayList<>();
                //boundaryTriangles.remove(firstTriangleInCurrRing.getTriangleID());
            }
            
            do{
                MVertex currVertex = nextVertex;
                ringOfVertices.add(nextVertex);
                
                Set<Long> vertexAux = new HashSet<>(currVertex.getAdjacentTriangles());
                Set<Long> allTrianglesAux = new HashSet<>(boundaryTriangles);
                
                vertexAux.retainAll(allTrianglesAux); // zostava vzdy iba jeden trojuholnik, predchadzajuci mazeme
                long triangleID = vertexAux.iterator().next();
                MTriangle currTriangle = triangleMesh.get(triangleID);
                long[] verticesIDs = currTriangle.getTriangleVertices();
                
                for(int k = 0; k < 3; k++){
                    MVertex vertex = triangleVertices.get(verticesIDs[k]);
                    Point3f point = vertex.getVertex(); 
                   
                    if(plane.isPointBelongToPlane(point) && !vertex.equals(currVertex)){
                        nextVertex = vertex;
                    }
                }
                
                boundaryTriangles.remove(currTriangle.getTriangleID());
                
            }while(!nextVertex.equals(firstVertexInCurrRing));
            
            listOfRings.add(ringOfVertices);
            firstVertexInCurrRing = null;
        }
        return listOfRings;
    }
    
    private List<HalfEdgeStructure> makeHalfEdgeStructure(List<List<MVertex>> allRings, Plane plane){
        List<HalfEdgeStructure> allHEStructures = new ArrayList<>();
        long halfEdgeID = 0;
        long faceID = 0;
        
        for(List<MVertex> ring : allRings){
            HalfEdgeStructure halfEdgeStruct = new HalfEdgeStructure();
            List<HalfEdge> auxList = new ArrayList<>();

            MVertex mVertex = ring.get(ring.size() - 1);
            HalfEdge prevHalfEdge = new HalfEdge(halfEdgeID, mVertex.getVertexID());
            HalfEdge firstHalfEdge = prevHalfEdge;
            HEFace heFace = new HEFace(faceID, halfEdgeID);
            halfEdgeID++;

            for(int i = 0; i < ring.size(); i++){
                mVertex = ring.get(i);
                Point3f vertex3D = mVertex.getVertex();
                Point2f vertex2D = plane.getCenteredProjectionPoint(vertex3D);
                HalfEdge currHalfEdge;
                HEVertex heVertex;

                if(i == ring.size() - 1){
                    currHalfEdge = firstHalfEdge;
                    heVertex = new HEVertex(mVertex.getVertexID(), vertex2D, /*firstHalfEdge.getId()*/ -1);  
                }else{
                    currHalfEdge = new HalfEdge(halfEdgeID, mVertex.getVertexID());
                    heVertex = new HEVertex(mVertex.getVertexID(), vertex2D, /*halfEdgeID*/ -1);

                    halfEdgeID++;    
                }
                currHalfEdge.setFace(faceID);
                currHalfEdge.setPrev(prevHalfEdge.getId());
                prevHalfEdge = currHalfEdge;
                halfEdgeStruct.addVertex(heVertex);
                auxList.add(currHalfEdge);
            }

            for(int i = 0; i < auxList.size(); i++){
                HalfEdge currHalfEdge = auxList.get(i);
                HalfEdge nextHalfEdge = auxList.get((i + 1) % auxList.size());

                currHalfEdge.setNext(nextHalfEdge.getId());
                long currVertexID = currHalfEdge.getTargetVertex();
                HEVertex currVertex = halfEdgeStruct.getHEVertex(currVertexID);
                currVertex.setLeavingHalfEdgeID(nextHalfEdge.getId());
                
                halfEdgeStruct.addHalfEdge(currHalfEdge);
            }
            halfEdgeStruct.addFace(heFace);
            faceID++;
            allHEStructures.add(halfEdgeStruct);
        }
        
        return allHEStructures;
    }
    
    private List<List<MVertex>> fillTestList(){
        List<List<MVertex>> allRings = new ArrayList<>();
        List<MVertex> splitPolygon = new ArrayList<>();
        List<MVertex> mergePolygon = new ArrayList<>();
        List<MVertex> nonMonotonePolygon = new ArrayList<>();
        
        splitPolygon.add(new MVertex(0, 0, new Point3f(4,0,10)));
        splitPolygon.add(new MVertex(1, 0, new Point3f(6,0,2)));
        splitPolygon.add(new MVertex(2, 0, new Point3f(9,0,6)));
        splitPolygon.add(new MVertex(3, 0, new Point3f(12,0,1)));
        splitPolygon.add(new MVertex(4, 0, new Point3f(14,0,8)));
        
        mergePolygon.add(new MVertex(0, 0, new Point3f(4,0,12)));
        mergePolygon.add(new MVertex(1, 0, new Point3f(6,0,2)));
        mergePolygon.add(new MVertex(2, 0, new Point3f(9,0,2)));
        mergePolygon.add(new MVertex(3, 0, new Point3f(12,0,2)));
        mergePolygon.add(new MVertex(4, 0, new Point3f(12,0,10)));
        mergePolygon.add(new MVertex(5, 0, new Point3f(8,0,8)));
        
        nonMonotonePolygon.add(new MVertex(0, 0, new Point3f(6,0,12)));
        nonMonotonePolygon.add(new MVertex(1, 0, new Point3f(3,0,5)));
        nonMonotonePolygon.add(new MVertex(2, 0, new Point3f(6,0,2)));
        nonMonotonePolygon.add(new MVertex(3, 0, new Point3f(7,0,6)));
        nonMonotonePolygon.add(new MVertex(4, 0, new Point3f(10,0,6)));
        
        //allRings.add(nonMonotonePolygon);
        //allRings.add(splitPolygon);
        allRings.add(mergePolygon);
        return allRings;
    }
    
    public List<HalfEdgeStructure> findBoundaryPoplygonsFromCut(Model model, Plane plane){
        List<Long> boundaryTriangles = new ArrayList<>(this.getBoundaryTrianglesIDs(model, plane));
        List<List<MVertex>> allRings = this.getRingsOfBoundaryVertices(boundaryTriangles, model, plane);
        //List<List<MVertex>> allRings = this.fillTestList();
        List<HalfEdgeStructure> allPolygons = new ArrayList<>(this.makeHalfEdgeStructure(allRings, plane));
        
        HalfEdgeManagerImpl halfEdgeManager = new HalfEdgeManagerImpl();
        List<HalfEdgeStructure> polygonsWithHoles = halfEdgeManager.findAllPolygons(allPolygons);
        
        //halfEdgeManager.divideIntoMonotonePieces(polygonsWithHoles.get(0));
        
        int[] numContures1 = {5,4,3}; 
        //double[][] vertices = {{0, 0}, {7, 0}, {4, 4}, {2, 2}, {2, 3}, {3, 3}};
        //double[][] vertices2 = {{0,0},{2,0},{2,2},{0,2}};
        double[][] vertices3 = {{2,2},{10,2},{12,7},{10,14},{2,14},{5,7},{5,10},{8,10},{8,7},{8,3},{5,3},{6,5}};
        List list = new ArrayList();
        list = triangulate(3, numContures1, vertices3);
        
        return polygonsWithHoles;
    }
    
    public List<Model> divideModel(Model cutModel, List<HalfEdgeStructure> listOfPolygons, Plane plane){
        List<Model> listOfModels = new ArrayList<>();
        List<MTriangle> allNewTriangles = this.triangulatePolygons(listOfPolygons);
        
        this.addNewTrianglesToModel(cutModel, allNewTriangles, plane);
        
        ModelManagerImpl modelmanager = new ModelManagerImpl();
        Model updatedModel = modelmanager.updateAdjacentTriangles(cutModel);
        
        this.setComponent(cutModel);
        return listOfModels;
    }
    
    private List<MTriangle> triangulatePolygons(List<HalfEdgeStructure> listOfPolygons){
        HalfEdgeManagerImpl halfEdgeManager = new HalfEdgeManagerImpl();
        List<MTriangle> allNewTriangles = new ArrayList<>();
        
        for(HalfEdgeStructure currPolygon : listOfPolygons){
            List<HEVertex> outherRing = halfEdgeManager.findOutherRing(currPolygon);
            List<List<HEVertex>> holes = halfEdgeManager.findAllHolesRings(currPolygon);
            
            int numContures = 1;
            int numVerticesInContures[] = new int[1 + holes.size()];
            numVerticesInContures[0] = outherRing.size();
            List<HEVertex> allVertices = new ArrayList<>();
            allVertices.addAll(outherRing);
            int i = 1;
            
            for(List<HEVertex> currHole : holes){
                numVerticesInContures[i] = currHole.size();
                allVertices.addAll(currHole);
                numContures++;
                i++;
            }
            
            double[][] verticesInArray = this.transformVerticesListToArray(allVertices);
            
            List<List<Integer>> triangles = triangulate(numContures, numVerticesInContures, verticesInArray);
            
            allNewTriangles.addAll(this.transformTriangleFromTriangulationToListOfMTriangles(triangles, allVertices));
        }
        
        return allNewTriangles;
    }
    
    private double[][] transformVerticesListToArray(List<HEVertex> allVertices){
        int size = allVertices.size();
        double[][] verticesInArray = new double[size][2];
        
        for(int i = 0; i < allVertices.size(); i++){
            HEVertex currVertex = allVertices.get(i);
            Point2f currPoint = currVertex.getVertex();
            
            double x = currPoint.x;
            double y = currPoint.y;
            
            verticesInArray[i][0] = x;
            verticesInArray[i][1] = y;
        }
        
        return verticesInArray;
    }
    
    private List<MTriangle> transformTriangleFromTriangulationToListOfMTriangles
                            (List<List<Integer>> triangleFromTriangulation, 
                             List<HEVertex> allHEVertices)
    {
        List<MTriangle> listOfMVertices = new ArrayList<>();
        
        for(List<Integer> currTriangle : triangleFromTriangulation){
            long[] triangleVerticesIDs = new long[3];
            
            for(int i = 0; i < 3; i++){
                int vertexIndex = currTriangle.get(i);
                HEVertex heVertex = allHEVertices.get(vertexIndex);
                triangleVerticesIDs[i] = heVertex.getId();
            }
            
            MTriangle newTriangle = new MTriangle(-1, -1, new Point3f(1,0,0), triangleVerticesIDs);
            listOfMVertices.add(newTriangle);
        }
        
        return listOfMVertices;
    }
                            
    private Model addNewTrianglesToModel(Model cutModel, List<MTriangle> newTriangles, Plane plane){
        List<Long> boundaryTrianglesIDs = new ArrayList<>();
        boundaryTrianglesIDs.addAll(this.getBoundaryTrianglesIDs(cutModel, plane));
        
        List<Long> trianglesBelowPlane = this.findAllTrianglesBelowPlane(cutModel, boundaryTrianglesIDs, plane);
        List<Long> trianglesAbovePlane = new ArrayList<>(boundaryTrianglesIDs);
        trianglesAbovePlane.removeAll(trianglesBelowPlane);
        
        this.addTrianglesBelowPlane(cutModel, trianglesBelowPlane, newTriangles, plane);
        this.addVerticesAbovePlane(cutModel, trianglesAbovePlane, newTriangles, plane);
        
        return cutModel;
    }
    
    private List<Long> findAllTrianglesBelowPlane(Model cutModel, List<Long> allBoundaryTriangles, Plane plane){
        Map<Long, MTriangle> triangleMesh = cutModel.getTriangleMesh();
        Map<Long, MVertex> triangleVertices = cutModel.getVertices(); 
        List<Long> trianglesBelowPlane = new ArrayList<>();
        
        for(long triangleID : allBoundaryTriangles){
            MTriangle currTriangle = triangleMesh.get(triangleID);
            long[] verticesIDs = currTriangle.getTriangleVertices();
            
            for(int k = 0; k < 3; k++){
                Point3f vertex = triangleVertices.get(verticesIDs[k]).getVertex();
                
                if(plane.isPointUnderPlane(vertex)){
                    trianglesBelowPlane.add(triangleID);
                    break;
                }
            }
        }
        
        return trianglesBelowPlane;
    }
    
    private Model addTrianglesBelowPlane(Model cutModel, List<Long> trianglesBelowPlane, 
                                         List<MTriangle> newTriangles, Plane plane)
    {
        Map<Long, MTriangle> triangleMesh = cutModel.getTriangleMesh();
        Map<Long, MVertex> triangleVertices = cutModel.getVertices();
        
        long vertexID = modelManager.findMaxVertexID(cutModel) + 1;
        long triangleID = modelManager.findMaxTriangleID(cutModel) + 1;
        
        Map<Point3f, Long> auxVerticesMap = new HashMap<>();
        Point3f planeNorm = plane.getNormal();
        Point3f newTriangleNorm = new Point3f(-planeNorm.x, -planeNorm.y, -planeNorm.z);
        
        for(MTriangle currNewTriangle : newTriangles){
            long[] verticesIDs = currNewTriangle.getTriangleVertices();
            long[] newVerticesIDs = new long[3];
            
            for(int i = 0; i < 3; i++){
                MVertex currVertex = triangleVertices.get(verticesIDs[i]);
                Point3f currPoint = currVertex.getVertex();
                
                if(plane.isPointBelongToPlane(currPoint)){
                    
                    if(!auxVerticesMap.containsKey(currPoint)){
                        List<Long> adjacentTriangles = currVertex.getAdjacentTriangles();

                        List<Long> adjacentTrianglesBelowPlane = this.findAllTrianglesBelowPlane(cutModel, 
                                                                                                 adjacentTriangles, 
                                                                                                 plane);

                        List<Long> newAdjacentTriangles = new ArrayList<>(adjacentTriangles);
                        newAdjacentTriangles.removeAll(adjacentTrianglesBelowPlane);
                        currVertex.setAdjacentTriangles(newAdjacentTriangles); // vymazeme vsetkych dolnych susedov v povodnom trojuholniku 
                        
                        for(long currTriangleID : adjacentTrianglesBelowPlane){ // vymena starych bodov v trojuholnikoch pod rovinou za nove
                            MTriangle currTriangle = triangleMesh.get(currTriangleID);
                            long[] triVertices = currTriangle.getTriangleVertices();
                            
                            for(int j = 0; j < 3; j++){
                                if(triVertices[j] == currVertex.getVertexID()){
                                    triVertices[j] = vertexID;
                                }
                            }
                        }
                        
                        MVertex newVertex = new MVertex(vertexID, currVertex.getObjectID(), currVertex.getVertex());
                        newVertex.setAdjacentTriangles(adjacentTrianglesBelowPlane);
                        newVertex.addAdjacentTriangles(triangleID);
                        newVerticesIDs[i] = vertexID;
                        auxVerticesMap.put(currPoint, vertexID);
                        triangleVertices.put(vertexID, newVertex);
                        
                        vertexID++;
                    }else{
                        long auxIndex = auxVerticesMap.get(currPoint);
                        MVertex processedVertex = triangleVertices.get(auxIndex);
                        processedVertex.addAdjacentTriangles(triangleID);
                        newVerticesIDs[i] = auxIndex;
                    }
                }
            }
            MTriangle newTriangle = new MTriangle(triangleID, 0, newTriangleNorm, newVerticesIDs);
            triangleMesh.put(triangleID, newTriangle);
            triangleID++;
        }
        
        return cutModel;
    }
    
    private Model addVerticesAbovePlane(Model cutModel, List<Long> trianglesAbovePlane, 
                                         List<MTriangle> newTriangles, Plane plane)
    {
        Map<Long, MTriangle> triangleMesh = cutModel.getTriangleMesh();
        Map<Long, MVertex> triangleVertices = cutModel.getVertices();
        
        long triangleID = modelManager.findMaxTriangleID(cutModel) + 1;
        
        Point3f newTriangleNorm = plane.getNormal();
        for(MTriangle currNewTriangle : newTriangles){
            long[] verticesIDs = currNewTriangle.getTriangleVertices();
            
            for(int i = 0; i < 3; i++){
                MVertex triangleVertex = triangleVertices.get(verticesIDs[i]);
                triangleVertex.addAdjacentTriangles(triangleID);
            }
            
            MTriangle newTriangle = new MTriangle(triangleID, 0, newTriangleNorm, verticesIDs);
            triangleMesh.put(triangleID, newTriangle);
            triangleID++;
        }
        return cutModel;
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
