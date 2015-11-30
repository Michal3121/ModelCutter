/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelcutter;

import com.sun.org.apache.xml.internal.security.utils.HelperNodeList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.vecmath.Point2f;

/**
 *
 * @author MICHAL
 */
public class HalfEdgeManagerImpl {
    private List<HalfEdgeStructure> findBoundaryRings(List<HalfEdgeStructure> listOfRings){
        List<HalfEdgeStructure> boundaryRings = new ArrayList<>();
        int intersectCount = 0;
        
        for(HalfEdgeStructure testRing : listOfRings){
            List<HEFace> faces = new ArrayList<>(testRing.getFaces());
            HEFace face = faces.get(0);
            long firstHalfEdgeID = face.getInnerHalfEdgeID();
            HalfEdge firstHalfEdge = testRing.getHalfEdge(firstHalfEdgeID);
            HEVertex testVertex = testRing.getHEVertex(firstHalfEdge.getTargetVertex());
            
            List<HalfEdgeStructure> allRingAux = new ArrayList<>(listOfRings);
            allRingAux.remove(testRing);
            for(HalfEdgeStructure currRing : allRingAux){
                if(this.isPointInPolygon(testVertex, currRing)){
                    intersectCount++;
                }
            }
            
            if(intersectCount % 2 == 0){
                boundaryRings.add(testRing);
            }
            intersectCount = 0;
            
        }
        
        listOfRings.removeAll(boundaryRings);
        return boundaryRings;
    }
    
    private boolean isPointInPolygon(HEVertex testVertex, HalfEdgeStructure testRing){
        List<HEFace> faces = new ArrayList<>(testRing.getFaces());
        HEFace face = faces.get(0);
        long firstHalfEdgeID = face.getInnerHalfEdgeID();
        HalfEdge firstHalfEdge = testRing.getHalfEdge(firstHalfEdgeID); 
        HalfEdge currHalfEdge = firstHalfEdge;
        HalfEdge nextHalfEdge = testRing.getHalfEdge(currHalfEdge.getNext());
        boolean ret = false;
        
        do{
            HEVertex currVertex = testRing.getHEVertex(currHalfEdge.getTargetVertex());
            HEVertex nextVertex = testRing.getHEVertex(nextHalfEdge.getTargetVertex());
            
            if(this.isPointLeftToLine(testVertex.getVertex(), 
                                      currVertex.getVertex(), 
                                      nextVertex.getVertex())){
                ret = !ret;
            }
            
            currHalfEdge = nextHalfEdge;
            nextHalfEdge = testRing.getHalfEdge(currHalfEdge.getNext());
        }while(currHalfEdge != firstHalfEdge); 
        return ret;
    }
    
    private boolean isPointLeftToLine(Point2f testPoint, Point2f startPoint, Point2f endPoint){
        if(startPoint.y > testPoint.y == endPoint.y > testPoint.y){
            return false;
        } 
        
        float slopeOfLine = (startPoint.x - endPoint.x)/(startPoint.y - endPoint.y);
        float pointLyingOnLine = slopeOfLine * (testPoint.y - endPoint.y) + endPoint.x; // x coord of the point
        
        return testPoint.x < pointLyingOnLine;
    }
    
    private boolean isRingClockwise(HalfEdgeStructure testRing){
        List<HEFace> faces = new ArrayList<>(testRing.getFaces());
        HEFace face = faces.get(0);
        long firstHalfEdgeID = face.getInnerHalfEdgeID();
        HalfEdge firstHalfEdge = testRing.getHalfEdge(firstHalfEdgeID);
        HalfEdge currHalfEdge = firstHalfEdge;
        HalfEdge nextHalfEdge = testRing.getHalfEdge(currHalfEdge.getNext());
        double areaSum = 0;
            
        do{
            HEVertex currVertex = testRing.getHEVertex(currHalfEdge.getTargetVertex());
            HEVertex nextVertex = testRing.getHEVertex(nextHalfEdge.getTargetVertex());
            Point2f currPoint = currVertex.getVertex();
            Point2f nextPoint = nextVertex.getVertex();
            
            areaSum+= currPoint.x * nextPoint.y - currPoint.y * nextPoint.x;
            
            currHalfEdge = nextHalfEdge;
            nextHalfEdge = testRing.getHalfEdge(currHalfEdge.getNext());
        }while(currHalfEdge != firstHalfEdge);
        
        areaSum = areaSum / 2.0;
        
        if(this.isRingConvex(testRing)){
            return areaSum < 0;
        }
        return areaSum < 0; // > 0
    }
    
    private boolean isRingConvex(HalfEdgeStructure testRing){
        List<HEFace> faces = new ArrayList<>(testRing.getFaces());
        HEFace face = faces.get(0);
        long firstHalfEdgeID = face.getInnerHalfEdgeID();
        HalfEdge firstHalfEdge = testRing.getHalfEdge(firstHalfEdgeID);
        HalfEdge currHalfEdge = firstHalfEdge;
        HalfEdge prevHalfEdge = testRing.getHalfEdge(currHalfEdge.getPrev());
        HalfEdge nextHalfEdge = testRing.getHalfEdge(currHalfEdge.getNext());
        boolean firstIteration = true;
        boolean firstCrossProduct = false;
        
        do{
            HEVertex prevVertex = testRing.getHEVertex(prevHalfEdge.getTargetVertex());
            HEVertex currVertex = testRing.getHEVertex(currHalfEdge.getTargetVertex());
            HEVertex nextVertex = testRing.getHEVertex(nextHalfEdge.getTargetVertex());
            Point2f prevPoint = prevVertex.getVertex();
            Point2f currPoint = currVertex.getVertex();
            Point2f nextPoint = nextVertex.getVertex();
            
            if(firstIteration){
                firstCrossProduct = this.isInteriorAngleLessThanPI(currPoint, prevPoint, nextPoint);
                firstIteration = false;
                //break;
            }
            
            if(this.isInteriorAngleLessThanPI(currPoint, prevPoint, nextPoint) != firstCrossProduct){
                return false;
            }
            
            prevHalfEdge = currHalfEdge;
            currHalfEdge = nextHalfEdge;
            nextHalfEdge = testRing.getHalfEdge(currHalfEdge.getNext());
        }while(currHalfEdge != firstHalfEdge);
        
        return true;
    }
    
    private int computeAreaOfPolygon(HalfEdgeStructure testRing){
        long firstHalfEdgeID = this.getFirstHalfEdge(testRing);
        HalfEdge firstHalfEdge = testRing.getHalfEdge(firstHalfEdgeID);
        HalfEdge currHalfEdge = firstHalfEdge;
        HalfEdge nextHalfEdge = testRing.getHalfEdge(currHalfEdge.getNext());
        int areaSum = 0;
            
        do{
            HEVertex currVertex = testRing.getHEVertex(currHalfEdge.getTargetVertex());
            HEVertex nextVertex = testRing.getHEVertex(nextHalfEdge.getTargetVertex());
            Point2f currPoint = currVertex.getVertex();
            Point2f nextPoint = nextVertex.getVertex();
            
            areaSum+= currPoint.x * nextPoint.y - currPoint.y * nextPoint.x;
            
            currHalfEdge = nextHalfEdge;
            nextHalfEdge = testRing.getHalfEdge(currHalfEdge.getNext());
        }while(currHalfEdge != firstHalfEdge);
        
        return Math.abs(areaSum / 2);   
    }
    
    private long getFirstHalfEdge(HalfEdgeStructure testRing){
        List<HEFace> faces = new ArrayList<>(testRing.getFaces());
        HEFace face = faces.get(0);
        return face.getInnerHalfEdgeID();
    }
    
    private List<HalfEdgeStructure> createPolygonsWithHoles(List<HalfEdgeStructure> polygons, List<HalfEdgeStructure> holes){
        List<HalfEdgeStructure> listOfPolygons = new ArrayList<>(polygons);
        List<HalfEdgeStructure> listOfHoles = new ArrayList<>(holes);
        
        for(HalfEdgeStructure polygon : listOfPolygons){
            int area = this.computeAreaOfPolygon(polygon);
            polygon.setAreaOfPolygon(area);
        }
        
        for(HalfEdgeStructure hole : listOfHoles){
            int area = this.computeAreaOfPolygon(hole);
            hole.setAreaOfPolygon(area);
        }
        
        Collections.sort(listOfPolygons, new PolygonAreaComparator());
        Collections.sort(listOfHoles, new PolygonAreaComparator());
        
        Iterator<HalfEdgeStructure> holesIter = listOfHoles.iterator();
        while(holesIter.hasNext()){
            HalfEdgeStructure currHole = holesIter.next();
            
            for(HalfEdgeStructure polygon : listOfPolygons){
                long halfEdgeInHoleID = this.getFirstHalfEdge(currHole);
                HalfEdge halfEdgeInHole = currHole.getHalfEdge(halfEdgeInHoleID);
                HEVertex vertexInHole = currHole.getHEVertex(halfEdgeInHole.getTargetVertex());
                
                if(currHole.getAreaOfPolygon() < polygon.getAreaOfPolygon() && 
                   this.isPointInPolygon(vertexInHole, polygon)){
                    this.addHoleToPolygon(polygon, currHole);
                }
            }
        }
        
        return listOfPolygons;
    }
    
    private HalfEdgeStructure addHoleToPolygon(HalfEdgeStructure polygon, HalfEdgeStructure hole){
        long firstHoleHalfEdgeID = this.getFirstHalfEdge(hole);
        List<HEFace> faces = new ArrayList<>(polygon.getFaces());
        HEFace polygonFace = faces.get(0);
        
        polygonFace.addHoleHalfEdgeID(firstHoleHalfEdgeID);
        
        for(HEVertex vertex : hole.getHEVertices()){
            polygon.addVertex(vertex);
        }
        
        for(HalfEdge halfEdge : hole.getHalfEdges()){
            polygon.addHalfEdge(halfEdge);
        }
        
        return polygon;
    }
    
    private HalfEdgeStructure changeOrientationOfPolygon(HalfEdgeStructure polygon){
        List<HEFace> faces = new ArrayList<>(polygon.getFaces());
        HEFace face = faces.get(0);
        long firstHalfEdgeID = face.getInnerHalfEdgeID();
        HalfEdge firstHalfEdge = polygon.getHalfEdge(firstHalfEdgeID);
        HalfEdge currHalfEdge = firstHalfEdge;
        HalfEdge nextHalfEdge = polygon.getHalfEdge(currHalfEdge.getNext());
        
        HalfEdgeStructure oppositeOrientationPolygon = new HalfEdgeStructure();
        
        do{
            long prevHalfEdgeID = currHalfEdge.getPrev();
            HalfEdge prevHalfEdge = polygon.getHalfEdge(prevHalfEdgeID);
            HalfEdge oppositeHalfEdge = new HalfEdge(currHalfEdge.getId(), prevHalfEdge.getTargetVertex());
            oppositeHalfEdge.setPrev(currHalfEdge.getNext());
            oppositeHalfEdge.setNext(currHalfEdge.getPrev());
            oppositeHalfEdge.setFace(currHalfEdge.getFace());
            
            HEVertex currVertex = polygon.getHEVertex(currHalfEdge.getTargetVertex());
            HEVertex oppositeHEVertex = new HEVertex(currVertex.getId(), 
                                                     currVertex.getVertex(), 
                                                     currHalfEdge.getId());
            
            
            oppositeOrientationPolygon.addHalfEdge(oppositeHalfEdge);
            oppositeOrientationPolygon.addVertex(oppositeHEVertex);
            oppositeOrientationPolygon.addFace(face);
            
            currHalfEdge = nextHalfEdge;
            nextHalfEdge = polygon.getHalfEdge(currHalfEdge.getNext());
        }while(currHalfEdge != firstHalfEdge);
        
        return oppositeOrientationPolygon;
    }
    
    //musi obsahovat iba jednu face
    private List<HalfEdge> getOuterBoundaryOfSimplePolygon(HalfEdgeStructure polygon){
        long firstHalfEdgeID = this.getFirstHalfEdge(polygon);
        HalfEdge firstHalfEdge = polygon.getHalfEdge(firstHalfEdgeID);
        HalfEdge currHalfEdge = firstHalfEdge;
        HalfEdge nextHalfEdge = polygon.getHalfEdge(currHalfEdge.getNext());
        List<HalfEdge> halfEdgeList = new ArrayList<>();
        
        do{
            halfEdgeList.add(currHalfEdge);
            
            currHalfEdge = nextHalfEdge;
            nextHalfEdge = polygon.getHalfEdge(currHalfEdge.getNext());
        }while(nextHalfEdge != firstHalfEdge);
        
        return halfEdgeList;
    }
    
    public List<HalfEdgeStructure> findAllPolygons(List<HalfEdgeStructure> rings){
        List<HalfEdgeStructure> outerRings = this.findBoundaryRings(rings);
        List<HalfEdgeStructure> outerRingsCounterClockWise = new ArrayList<>();
        List<HalfEdgeStructure> holesClockWise = new ArrayList<>();
        
        for(HalfEdgeStructure currRing : outerRings){
            HalfEdgeStructure ringAux = currRing;
            if(this.isRingClockwise(currRing)){
                ringAux = this.changeOrientationOfPolygon(currRing);
            }
            outerRingsCounterClockWise.add(ringAux);
        }
        
        for(HalfEdgeStructure currHole : rings){
            HalfEdgeStructure holeAux = currHole;
            if(!this.isRingClockwise(currHole)){
                holeAux = this.changeOrientationOfPolygon(currHole);
            }
            holesClockWise.add(holeAux);
        }
        
        List<HalfEdgeStructure> polygonsWithHoles = this.createPolygonsWithHoles(outerRingsCounterClockWise, holesClockWise);
        return polygonsWithHoles;
    }
    
    public List<HalfEdgeStructure> divideIntoMonotonePieces(HalfEdgeStructure polygon){
        List<HalfEdgeStructure> monotonePieces = new ArrayList<>();
        
        SortedSet<HEVertex> sortedVertices = new TreeSet<>(new HEVertexPositionComparator());
        sortedVertices.addAll(polygon.getHEVertices());
        SortedSet<HEVertex> allSortedVertices = new TreeSet<>(new HEVertexPositionComparator());
        allSortedVertices.addAll(sortedVertices);
        SortedSet<EdgeWithHelper> edgeTree = new TreeSet<>(new EdgeWithHelperComparator());
        
        while(!sortedVertices.isEmpty()){
            HEVertex sweepLineVertex = sortedVertices.first();
            
            if(this.isStartVertex(sweepLineVertex, polygon)){
                long leavingHalfEdgeID = sweepLineVertex.getLeavingHalfEdgeID();
                HEVertex startVertex = sweepLineVertex;
                HEVertex endVertex = this.nextHEVertex(startVertex, polygon);
                EdgeWithHelper edgeWithHelper = new EdgeWithHelper(leavingHalfEdgeID, 
                                                                   startVertex, 
                                                                   endVertex);
                edgeWithHelper.setHelper(startVertex);
                edgeWithHelper.setIntersectPoint(startVertex.getVertex());
                edgeTree.add(edgeWithHelper);
                sortedVertices.remove(sweepLineVertex);
                continue;
            }
            if(this.isEndVertex(sweepLineVertex, polygon)){
                List<HalfEdge> allIntersectEdges = this.findAllIntersectingEdges(sweepLineVertex, polygon);
                HalfEdge prevHalfEdge = this.prevHalfEdge(sweepLineVertex, polygon);
                allIntersectEdges.add(prevHalfEdge);
                
                SortedSet<EdgeWithHelper> currTree = this.updateTree(allSortedVertices, 
                                                                     sweepLineVertex, 
                                                                     allIntersectEdges, 
                                                                     polygon);
                
                EdgeWithHelper prevEdgeWithHelper = this.findEdgeInTreeByID(prevHalfEdge, currTree);
                HEVertex helper = prevEdgeWithHelper.getHelper();
                
                if(this.isMergeVertex(helper, polygon)){
                    // insert diagonal
                }
                
                sortedVertices.remove(sweepLineVertex);
                continue;
            }
            if(this.isSplitVertex(sweepLineVertex, polygon)){
                List<HalfEdge> allIntersectEdges = this.findAllIntersectingEdges(sweepLineVertex, polygon);
                SortedSet<EdgeWithHelper> currTree = this.updateTree(allSortedVertices, 
                                                                     sweepLineVertex, 
                                                                     allIntersectEdges, 
                                                                     polygon);
                EdgeWithHelper edgeLeftToVertex = this.findEdgeLeftToVertex(sweepLineVertex, currTree);
                
                // insert diagonal
                
                /*edgeLeftToVertex.setHelper(sweepLineVertex);
                HEVertex startVertex = sweepLineVertex;
                HEVertex endVertex = this.nextHEVertex(startVertex, polygon);
                EdgeWithHelper newEdge = new EdgeWithHelper(startVertex.getId(), 
                                                            startVertex, 
                                                            endVertex);*/
                sortedVertices.remove(sweepLineVertex);
                //currTree.add(newEdge);
                continue;
            }
            if(this.isMergeVertex(sweepLineVertex, polygon)){
                List<HalfEdge> allIntersectEdges = this.findAllIntersectingEdges(sweepLineVertex, polygon);
                HalfEdge prevHalfEdge = this.prevHalfEdge(sweepLineVertex, polygon);
                allIntersectEdges.add(prevHalfEdge);
                
                SortedSet<EdgeWithHelper> currTree = this.updateTree(allSortedVertices, 
                                                                     sweepLineVertex, 
                                                                     allIntersectEdges, 
                                                                     polygon);
                
                EdgeWithHelper prevEdgeWithHelper = this.findEdgeInTreeByID(prevHalfEdge, currTree);
                HEVertex helper = prevEdgeWithHelper.getHelper();
                
                if(this.isMergeVertex(helper, polygon)){
                    // insert diagonal
                }
                
                currTree.remove(prevEdgeWithHelper);
                EdgeWithHelper edgeLeftToVertex = this.findEdgeLeftToVertex(sweepLineVertex, currTree);
                helper = edgeLeftToVertex.getHelper();
                
                if(this.isMergeVertex(helper, polygon)){
                    // insert diagonal
                }
                
                edgeLeftToVertex.setHelper(sweepLineVertex);
                sortedVertices.remove(sweepLineVertex);
                continue;
            }
            if(this.isRegularVertex(sweepLineVertex, polygon)){
                List<HalfEdge> allIntersectEdges = this.findAllIntersectingEdges(sweepLineVertex, polygon);
                HalfEdge prevHalfEdge = this.prevHalfEdge(sweepLineVertex, polygon);
                allIntersectEdges.add(prevHalfEdge);

                SortedSet<EdgeWithHelper> currTree = this.updateTree(allSortedVertices, 
                                                                     sweepLineVertex, 
                                                                     allIntersectEdges, 
                                                                     polygon);

                if(this.isInteriorToRightOfVertex(sweepLineVertex, polygon)){
                    EdgeWithHelper prevEdgeWithHelper = this.findEdgeInTreeByID(prevHalfEdge, currTree);
                    HEVertex helper = prevEdgeWithHelper.getHelper();
                    
                    if(this.isMergeVertex(helper, polygon)){
                        // insert diagonal
                    }
                    currTree.remove(prevEdgeWithHelper);
                    HEVertex startVertex = sweepLineVertex;
                    HEVertex endVertex = this.nextHEVertex(startVertex, polygon);
                    EdgeWithHelper newEdge = new EdgeWithHelper(startVertex.getId(), 
                                                                startVertex, 
                                                                endVertex);
                    newEdge.setHelper(helper);
                }else{
                    EdgeWithHelper edgeLeftToVertex = this.findEdgeLeftToVertex(sweepLineVertex, currTree);
                    HEVertex helper = edgeLeftToVertex.getHelper();
                
                    if(this.isMergeVertex(helper, polygon)){
                        // insert diagonal
                    }
                    edgeLeftToVertex.setHelper(helper);
                }
                sortedVertices.remove(sweepLineVertex);
            }
            
        }
        
        return monotonePieces;
    }
    
    private boolean isStartVertex(HEVertex testVertex, HalfEdgeStructure polygon){
        HEVertex prevVertex = this.prevHEVertex(testVertex, polygon);
        HEVertex nextVertex = this.nextHEVertex(testVertex, polygon);
        
        Point2f prevPoint = prevVertex.getVertex();
        Point2f nextPoint = nextVertex.getVertex();
        Point2f testPoint = testVertex.getVertex();
        
        return this.isInteriorAngleLessThanPI(testPoint, prevPoint, nextPoint) &&
               this.isBothNeighborsBelowVertex(testPoint, prevPoint, nextPoint);
    }
    
    private boolean isEndVertex(HEVertex testVertex, HalfEdgeStructure polygon){
        HEVertex prevVertex = this.prevHEVertex(testVertex, polygon);
        HEVertex nextVertex = this.nextHEVertex(testVertex, polygon);
        
        Point2f prevPoint = prevVertex.getVertex();
        Point2f nextPoint = nextVertex.getVertex();
        Point2f testPoint = testVertex.getVertex();
        
        return this.isInteriorAngleLessThanPI(testPoint, prevPoint, nextPoint) &&
               this.isBothNeighborsAboveVertex(testPoint, prevPoint, nextPoint);
    }
    
    private boolean isSplitVertex(HEVertex testVertex, HalfEdgeStructure polygon){
        HEVertex prevVertex = this.prevHEVertex(testVertex, polygon);
        HEVertex nextVertex = this.nextHEVertex(testVertex, polygon);
        
        Point2f prevPoint = prevVertex.getVertex();
        Point2f nextPoint = nextVertex.getVertex();
        Point2f testPoint = testVertex.getVertex();
        
        return !this.isInteriorAngleLessThanPI(testPoint, prevPoint, nextPoint) &&
               this.isBothNeighborsBelowVertex(testPoint, prevPoint, nextPoint);
    }
    
    private boolean isMergeVertex(HEVertex testVertex, HalfEdgeStructure polygon){
        HEVertex prevVertex = this.prevHEVertex(testVertex, polygon);
        HEVertex nextVertex = this.nextHEVertex(testVertex, polygon);
        
        Point2f prevPoint = prevVertex.getVertex();
        Point2f nextPoint = nextVertex.getVertex();
        Point2f testPoint = testVertex.getVertex();
        
        return !this.isInteriorAngleLessThanPI(testPoint, prevPoint, nextPoint) &&
               this.isBothNeighborsAboveVertex(testPoint, prevPoint, nextPoint);
    }
    
    private boolean isRegularVertex(HEVertex testVertex, HalfEdgeStructure polygon){
        HEVertex prevVertex = this.prevHEVertex(testVertex, polygon);
        HEVertex nextVertex = this.nextHEVertex(testVertex, polygon);
        
        Point2f prevPoint = prevVertex.getVertex();
        Point2f nextPoint = nextVertex.getVertex();
        Point2f testPoint = testVertex.getVertex();
        
        return this.isVertexABelowVertexB(testPoint, prevPoint) == 
               this.isVertexABelowVertexB(nextPoint, testPoint);
    }
    
    private boolean isInteriorToRightOfVertex(HEVertex testVertex, HalfEdgeStructure polygon){
        HEVertex prevVertex = this.prevHEVertex(testVertex, polygon);
        HEVertex nextVertex = this.nextHEVertex(testVertex, polygon);
        
        Point2f prevPoint = prevVertex.getVertex();
        Point2f nextPoint = nextVertex.getVertex();
        Point2f testPoint = testVertex.getVertex();
        
        return this.isVertexABelowVertexB(testPoint, prevPoint) && 
               this.isVertexABelowVertexB(nextPoint, testPoint);
    }
    
    private boolean isInteriorAngleLessThanPI(Point2f testPoint, Point2f prevPoint, Point2f nextPoint){
        float prevVectorX = testPoint.x - prevPoint.x;
        float prevVectorY = testPoint.y - prevPoint.y;        
        
        float nextVectorX = nextPoint.x - testPoint.x;
        float nextVectorY = nextPoint.y - testPoint.y;
        
        float ret = (prevVectorX * nextVectorY) - (prevVectorY * nextVectorX); // vzpocitame cross product pre 2D vector
        
        return ret > 0; 
    }
    
    private boolean isBothNeighborsBelowVertex(Point2f testPoint, Point2f prevPoint, Point2f nextPoint){
        //return testPoint.y > prevPoint.y && testPoint.y > nextPoint.y;
        return this.isVertexABelowVertexB(prevPoint, testPoint) &&
               this.isVertexABelowVertexB(nextPoint, testPoint);
    }
    
    private boolean isBothNeighborsAboveVertex(Point2f testPoint, Point2f prevPoint, Point2f nextPoint){
        //return testPoint.y < prevPoint.y && testPoint.y < nextPoint.y;
        return this.isVertexABelowVertexB(testPoint, prevPoint) &&
               this.isVertexABelowVertexB(testPoint, nextPoint);
    }
    
    private boolean isVertexABelowVertexB(Point2f pointA, Point2f pointB){
        if(pointA.y < pointB.y){
            return true;
        }
        return pointA.y == pointB.y && pointA.x > pointB.x;
    }
    
    private HEVertex nextHEVertex(HEVertex vertex, HalfEdgeStructure polygon){
        long leavingHalfEdgeID = vertex.getLeavingHalfEdgeID();
        HalfEdge leavingHalfEdge = polygon.getHalfEdge(leavingHalfEdgeID);
        
        long nextHEVertexID = leavingHalfEdge.getTargetVertex();
        
        return polygon.getHEVertex(nextHEVertexID);
    }
    
    private HEVertex prevHEVertex(HEVertex vertex, HalfEdgeStructure polygon){
        long leavingHalfEdgeID = vertex.getLeavingHalfEdgeID();
        HalfEdge leavingHalfEdge = polygon.getHalfEdge(leavingHalfEdgeID);
        
        HalfEdge currHalfEdge = polygon.getHalfEdge(leavingHalfEdge.getPrev());
        HalfEdge prevHalfEdge = polygon.getHalfEdge(currHalfEdge.getPrev());
        
        long prevHEVertexID = prevHalfEdge.getTargetVertex();
        
        return polygon.getHEVertex(prevHEVertexID);
    }
    
    private HalfEdge prevHalfEdge(HEVertex vertex, HalfEdgeStructure polygon){
        long leavingHalfEdgeID = vertex.getLeavingHalfEdgeID();
        HalfEdge leavingHalfEdge = polygon.getHalfEdge(leavingHalfEdgeID);
        
        HalfEdge prevHalfEdge = polygon.getHalfEdge(leavingHalfEdge.getPrev());
        
        return prevHalfEdge;
    }
    
    private EdgeWithHelper findEdgeInTreeByID(HalfEdge halfEdge, SortedSet<EdgeWithHelper> tree){
        
        for(EdgeWithHelper currEdge : tree){
            if(halfEdge.getId() == currEdge.getEdgeID()){
                return currEdge;
            }
        }
        
        return new EdgeWithHelper(-1, null, null);
    }
    
    private List<HEVertex> findSortedVertices(HalfEdgeStructure polygon){
        List<HEVertex> vertices = new ArrayList<>(polygon.getHEVertices());
        
        Collections.sort(vertices, new HEVertexPositionComparator());
        
        return vertices;
    }
    
    private HEVertex findHelper(HEVertex vertex, HalfEdgeStructure polygon){
        
        return vertex;
    }
    
    private EdgeWithHelper findEdgeLeftToVertex(HEVertex vertex, SortedSet<EdgeWithHelper> tree){
        Point2f point = vertex.getVertex();
        EdgeWithHelper edgeLeftToVertex = tree.first();
        
        return edgeLeftToVertex;
    }
    
    private SortedSet<EdgeWithHelper> updateTree(SortedSet<HEVertex> allSortedVertices,
                                                 HEVertex sweepLineVertex, 
                                                 List<HalfEdge> allIntersectingEdges, 
                                                 HalfEdgeStructure polygon)
    {
        //List<HalfEdge> allIntersectingEdges = this.findAllIntersectingEdges(sweepLineVertex, polygon);
        List<HalfEdge> allEdgesOnPolygonLeft = this.findAllEdgesOnPolygonLeft(sweepLineVertex, 
                                              allIntersectingEdges, polygon);
        
        List<EdgeWithHelper> edgesWithHelper = this.convertToEdgesWithHelper(allEdgesOnPolygonLeft, polygon, sweepLineVertex);
        SortedSet<EdgeWithHelper> newTree = new TreeSet<>(new EdgeWithHelperComparator());
        
        for(EdgeWithHelper edge : edgesWithHelper){
            SortedSet<HEVertex> subset = allSortedVertices.subSet(edge.getStartEdgePoint(), sweepLineVertex); // poradie v strome je stupajuce
            subset.remove(sweepLineVertex);
            HEVertex helper = new HEVertex(-1, null, -1);
            
            for(HEVertex testVertex : subset){
                
                if(this.isVertexEdgeHelper(testVertex, edge, polygon)){
                    helper = testVertex;
                }
            }
            
            if(helper.getId() == -1){
                helper = edge.getStartEdgePoint();     
            }
            edge.setHelper(helper);
            newTree.add(edge);
        }
        
        return newTree;
    }
    
    private boolean isVertexEdgeHelper(HEVertex testVertex, 
                                       EdgeWithHelper edge, 
                                       HalfEdgeStructure polygon)
    {
        List<HalfEdge> allHalfEdge = new ArrayList<>(polygon.getHalfEdges());
        Point2f testPoint = testVertex.getVertex();
        
        HEVertex edgeWithHelperStart = edge.getStartEdgePoint();
        HEVertex edgeWithHelperEnd = edge.getEndEdgePoint();
        Point2f edgeWithHelperStartPoint = edgeWithHelperStart.getVertex();
        Point2f edgeWithHelperEndPoint = edgeWithHelperEnd.getVertex();

        Point2f pointLyingOnEdge = this.findEdgeAndLineOfVertexIntersection(testPoint, 
                                                                            edgeWithHelperStartPoint, 
                                                                            edgeWithHelperEndPoint);
        float leftBoarder = pointLyingOnEdge.x;
        float rightBoarder = testPoint.x;
        
        for(HalfEdge testHalfEdge : allHalfEdge){
            long targetVertexID = testHalfEdge.getTargetVertex();
            
            HEVertex endVertex = polygon.getHEVertex(targetVertexID);
            HEVertex startVertex = this.prevHEVertex(endVertex, polygon);
            Point2f endPoint = endVertex.getVertex();
            Point2f startPoint = startVertex.getVertex();
            
            if(startPoint.y > testPoint.y != endPoint.y > testPoint.y){ // testovana usecka lezi v rozmedzi y
                Point2f intersectPoint = this.findEdgeAndLineOfVertexIntersection(testPoint, startPoint, endPoint);
                if(intersectPoint.x > leftBoarder && intersectPoint.x < rightBoarder){
                    return false;
                }
            }
        }
        
        return true;
    }
    
    private List<EdgeWithHelper> convertToEdgesWithHelper(List<HalfEdge> allEdgesOnPolygonLeft, 
                                                          HalfEdgeStructure polygon,
                                                          HEVertex sweepLineVertex){
        List<EdgeWithHelper> edgesWithHelperList = new ArrayList<>();
        
        for(HalfEdge currHalfEdge : allEdgesOnPolygonLeft){
            long targetVertexID = currHalfEdge.getTargetVertex();
            HEVertex targetVertex = polygon.getHEVertex(targetVertexID);
            Point2f endPoint = targetVertex.getVertex();
            
            long prevHalfEdgeID = currHalfEdge.getPrev();
            HalfEdge prevHalfEdge = polygon.getHalfEdge(prevHalfEdgeID);
            long startVertexID = prevHalfEdge.getTargetVertex();
            HEVertex startVertex = polygon.getHEVertex(startVertexID);
            Point2f startPoint = startVertex.getVertex();
            
            EdgeWithHelper edgeWithHelper = new EdgeWithHelper(currHalfEdge.getId(), startVertex, targetVertex);
            Point2f intersectPoint = this.findEdgeAndLineOfVertexIntersection(sweepLineVertex.getVertex(), 
                                                                           startPoint, endPoint);
            edgeWithHelper.setIntersectPoint(intersectPoint);
            edgesWithHelperList.add(edgeWithHelper);
        }
        
        return edgesWithHelperList;
    }
    
    private Point2f findEdgeAndLineOfVertexIntersection(Point2f sweepLinePoint, Point2f startPoint, Point2f endPoint){
        float slopeOfLine = (startPoint.x - endPoint.x)/(startPoint.y - endPoint.y);
        float pointLyingOnLine = slopeOfLine * (sweepLinePoint.y - endPoint.y) + endPoint.x; // x coord of the point
        
        return new Point2f(pointLyingOnLine, sweepLinePoint.y);
    }
    
    private List<HalfEdge> findAllEdgesOnPolygonLeft(HEVertex sweepLineVertex, 
                                                     List<HalfEdge> allIntersectEdges, 
                                                     HalfEdgeStructure polygon){
        Point2f sweepLinePoint = sweepLineVertex.getVertex();
        List<HalfEdge> edgesLeftToPolygon = new ArrayList<>();
        
        for(HalfEdge currHalfEdge : allIntersectEdges){
            long targetVertexID = currHalfEdge.getTargetVertex();
            HEVertex targetVertex = polygon.getHEVertex(targetVertexID);
            Point2f targetPoint = targetVertex.getVertex();
            HEVertex startVertex = this.prevHEVertex(targetVertex, polygon);
            Point2f startPoint = startVertex.getVertex();
            
            if(targetPoint.y < sweepLinePoint.y || 
              (sweepLineVertex.equals(targetVertex) && startPoint.y >= sweepLinePoint.y)) // Merge a Regular
            { 
                edgesLeftToPolygon.add(currHalfEdge);
            }
        }
        return edgesLeftToPolygon;
    }
    
    private Collection<HalfEdge> findAllHalfEdgesFromHoles(HalfEdgeStructure polygon){
        List<HEFace> allFaces = new ArrayList<>(polygon.getFaces()); 
        HEFace polygonFace = allFaces.get(0);
        List<Long> startHolesHalfEdgeIDs = new ArrayList<>(polygonFace.getHoleHalfEdges());
        List<HalfEdge> halfEdgesInHoles = new ArrayList<>();
        
        for(Long HalfEdgeID : startHolesHalfEdgeIDs){
            HalfEdge firstHalfEdge = polygon.getHalfEdge(HalfEdgeID); 
            HalfEdge currHalfEdge = firstHalfEdge;
            HalfEdge nextHalfEdge = polygon.getHalfEdge(currHalfEdge.getNext()); 
            
            do{
                halfEdgesInHoles.add(currHalfEdge);
                
                currHalfEdge = nextHalfEdge;
                nextHalfEdge = polygon.getHalfEdge(currHalfEdge.getNext());
            }while(currHalfEdge != firstHalfEdge); 
        }
        
        return halfEdgesInHoles;
    }
    
    private List<HalfEdge> findAllIntersectingEdges(HEVertex testPoint, HalfEdgeStructure polygon){
        List<HalfEdge> intersectingEdges = new ArrayList<>();
        long firstHalfEdgeID = this.getFirstHalfEdge(polygon);
        HalfEdge firstHalfEdge = polygon.getHalfEdge(firstHalfEdgeID);
        HalfEdge currHalfEdge = firstHalfEdge;
        HalfEdge nextHalfEdge = polygon.getHalfEdge(currHalfEdge.getNext());
        
        do{
            HEVertex currVertex = polygon.getHEVertex(currHalfEdge.getTargetVertex());
            HEVertex nextVertex = polygon.getHEVertex(nextHalfEdge.getTargetVertex());
            
            Point2f currPoint = currVertex.getVertex();
            Point2f nextPoint = nextVertex.getVertex();
            
            if(this.isIntersecting(testPoint.getVertex(), currVertex.getVertex(), nextVertex.getVertex())){
                intersectingEdges.add(nextHalfEdge);
            }
            currHalfEdge = nextHalfEdge;
            nextHalfEdge = polygon.getHalfEdge(currHalfEdge.getNext());
        }while(currHalfEdge != firstHalfEdge);
        
        return intersectingEdges;
    }
    
    private boolean isIntersecting(Point2f testPoint, Point2f startPoint, Point2f endPoint){
        return startPoint.y > testPoint.y != endPoint.y > testPoint.y;
    }
    
    public List<HEVertex> findOutherRing(HalfEdgeStructure polygon){
        List<HEVertex> outherVertices = new ArrayList<>();
        long firstHalfEdgeID = this.getFirstHalfEdge(polygon);
        HalfEdge firstHalfEdge = polygon.getHalfEdge(firstHalfEdgeID);
        HalfEdge currHalfEdge = firstHalfEdge;
        HalfEdge nextHalfEdge = polygon.getHalfEdge(currHalfEdge.getNext());
        
        do{
            HEVertex currVertex = polygon.getHEVertex(currHalfEdge.getTargetVertex());
            HEVertex nextVertex = polygon.getHEVertex(nextHalfEdge.getTargetVertex());
            
            outherVertices.add(currVertex);
            
            currHalfEdge = nextHalfEdge;
            nextHalfEdge = polygon.getHalfEdge(currHalfEdge.getNext());
        }while(currHalfEdge != firstHalfEdge);
        
        return outherVertices;
    }
    
    public List<List<HEVertex>> findAllHolesRings(HalfEdgeStructure polygon){
        List<List<HEVertex>> allHoles = new ArrayList<>();
        List<HEFace> faces = new ArrayList<>(polygon.getFaces());
        HEFace face = faces.get(0);
        
        List<Long> holesHalfEdges = new ArrayList<>(face.getHoleHalfEdges());
        
        for(long currHalfEdgeID : holesHalfEdges){
            List<HEVertex> currHole = this.findRingContainHalfEdge(currHalfEdgeID, polygon);
            allHoles.add(currHole);
        }
        
        return allHoles;
    }
    
    private List<HEVertex> findRingContainHalfEdge(long firstHalfEdgeID, HalfEdgeStructure polygon){
        List<HEVertex> ringOfVertices = new ArrayList<>();
        HalfEdge firstHalfEdge = polygon.getHalfEdge(firstHalfEdgeID);
        HalfEdge currHalfEdge = firstHalfEdge;
        HalfEdge nextHalfEdge = polygon.getHalfEdge(currHalfEdge.getNext());
        
        do{
            HEVertex currVertex = polygon.getHEVertex(currHalfEdge.getTargetVertex());
            //HEVertex nextVertex = polygon.getHEVertex(nextHalfEdge.getTargetVertex());
            
            ringOfVertices.add(currVertex);
            
            currHalfEdge = polygon.getHalfEdge(currHalfEdge.getNext());
            //currHalfEdge = nextHalfEdge;
            //nextHalfEdge = polygon.getHalfEdge(currHalfEdge.getNext());
        }while(currHalfEdge != firstHalfEdge);
        
        return ringOfVertices;
    }
}
