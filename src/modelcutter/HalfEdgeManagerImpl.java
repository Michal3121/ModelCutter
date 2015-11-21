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
        return areaSum > 0; 
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
        
        List<HEVertex> sortedVertices = this.findSortedVertices(polygon);
        SortedSet<HEVertex> sortedVertices2 = new TreeSet<>(new HEVertexPositionComparator());
        sortedVertices2.addAll(polygon.getHEVertices());
        SortedSet<EdgeWithHelper> edgeTree = new TreeSet<>(new EdgeWithHelperComparator());
        boolean startVertex = true;
        
        while(!sortedVertices2.isEmpty()){
            HEVertex highestPriorVertex = sortedVertices2.first();
            HEVertex prevVertex = this.prevHEVertex(highestPriorVertex, polygon);
            HEVertex nextVertex = this.nextHEVertex(highestPriorVertex, polygon);

            Point2f currPoint = highestPriorVertex.getVertex();
            Point2f prevPoint = prevVertex.getVertex();
            Point2f nextPoint = nextVertex.getVertex();
            
            if(this.isStartVertex(currPoint, prevPoint, nextPoint)){
                long leavingHalfEdgeID = highestPriorVertex.getLeavingHalfEdgeID();
                HalfEdge edge = polygon.getHalfEdge(leavingHalfEdgeID);
                EdgeWithHelper edgeWithHelper = new EdgeWithHelper(leavingHalfEdgeID, 
                                                                   highestPriorVertex, 
                                                                   null);
                edgeTree.add(edgeWithHelper);
                sortedVertices2.remove(highestPriorVertex);
                startVertex = false;
            }
            if(this.isEndVertex(nextPoint, prevPoint, nextPoint)){
                
            }
            if(this.isSplitVertex(nextPoint, prevPoint, nextPoint)){
                
            }
            if(this.isMergeVertex(nextPoint, prevPoint, nextPoint)){
                
            }
            
            
        }
        
        return monotonePieces;
    }
    
    private boolean isStartVertex(Point2f testVertex, Point2f prevPoint, Point2f nextPoint){
        return this.isInteriorAngleLessThanPI(testVertex, prevPoint, nextPoint) &&
               this.isBothNeighborsBelowVertex(testVertex, prevPoint, nextPoint);
    }
    
    private boolean isEndVertex(Point2f testVertex, Point2f prevPoint, Point2f nextPoint){
        return this.isInteriorAngleLessThanPI(testVertex, prevPoint, nextPoint) &&
               !this.isBothNeighborsBelowVertex(testVertex, prevPoint, nextPoint);
    }
    
    private boolean isSplitVertex(Point2f testVertex, Point2f prevPoint, Point2f nextPoint){
        return !this.isInteriorAngleLessThanPI(testVertex, prevPoint, nextPoint) &&
               this.isBothNeighborsBelowVertex(testVertex, prevPoint, nextPoint);
    }
    
    private boolean isMergeVertex(Point2f testVertex, Point2f prevPoint, Point2f nextPoint){
        return !this.isInteriorAngleLessThanPI(testVertex, prevPoint, nextPoint) &&
               !this.isBothNeighborsBelowVertex(testVertex, prevPoint, nextPoint);
    }
    
    private boolean isInteriorAngleLessThanPI(Point2f testPoint, Point2f prevPoint, Point2f nextPoint){
        float prevVectorX = testPoint.x - prevPoint.x;
        float prevVectorY = testPoint.y - prevPoint.y;        
        
        float nextVectorX = nextPoint.x - testPoint.x;
        float nextVectorY = nextPoint.y - testPoint.y;
        
        float ret = (prevVectorX * nextVectorX) - (prevVectorY * nextVectorY); // vzpocitame cross product pre 2D vector
        
        return ret < 0; 
    }
    
    private boolean isBothNeighborsBelowVertex(Point2f testPoint, Point2f prevPoint, Point2f nextPoint){
        return testPoint.y > prevPoint.y && testPoint.y > nextPoint.y;
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
        
        long prevHEVertexID = currHalfEdge.getTargetVertex();
        
        return polygon.getHEVertex(prevHEVertexID);
    }
    
    private List<HEVertex> findSortedVertices(HalfEdgeStructure polygon){
        List<HEVertex> vertices = new ArrayList<>(polygon.getHEVertices());
        
        Collections.sort(vertices, new HEVertexPositionComparator());
        
        return vertices;
    }
    
    private HEVertex findHelper(HEVertex vertex, HalfEdgeStructure polygon){
        
        return vertex;
    }
    
    /*private HalfEdge findEdgeLeftToVertex(HEVertex vertex, HalfEdgeStructure polygon){
        
    }*/
    
    private SortedSet<EdgeWithHelper> updateTree(SortedSet<HEVertex> sortedVertices,
                                                 HEVertex sweepLineVertex, 
                                                 SortedSet<HEVertex> tree, 
                                                 HalfEdgeStructure polygon){
        List<HalfEdge> allIntersectingEdges = this.findAllIntersectingEdges(sweepLineVertex, polygon);
        List<HalfEdge> allEdgesOnPolygonLeft = this.findAllEdgesOnPolygonLeft(sweepLineVertex, 
                                              allIntersectingEdges, polygon);
        
        List<EdgeWithHelper> edgesWithHelper = this.convertToEdgesWithHelper(allEdgesOnPolygonLeft, polygon);
        SortedSet<EdgeWithHelper> newTree = new TreeSet<>(new EdgeWithHelperComparator());
        
        for(EdgeWithHelper edge : edgesWithHelper){
            SortedSet<HEVertex> subset = sortedVertices.subSet(sweepLineVertex, edge.getStartEdgePoint());
            subset.remove(sweepLineVertex);
            
            for(HEVertex currVertex : subset){
                Point2f currPoint = currVertex.getVertex();
                Point2f sweepPoint = sweepLineVertex.getVertex();
                
                if(this.isVertexEdgeHelper(currVertex, edge, allIntersectingEdges, polygon)){
                    
                }
            }
        }
        
        return newTree;
    }
    
    private boolean isVertexEdgeHelper(HEVertex testVertex, 
                                       EdgeWithHelper edge, 
                                       List<HalfEdge> allIntersectingEdges,
                                       HalfEdgeStructure polygon){
        return true;
    }
    
    private List<EdgeWithHelper> convertToEdgesWithHelper(List<HalfEdge> allEdgesOnPolygonLeft, 
                                                        HalfEdgeStructure polygon){
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
            edgesWithHelperList.add(edgeWithHelper);
        }
        
        return edgesWithHelperList;
        
    }
    
    private List<HalfEdge> findAllEdgesOnPolygonLeft(HEVertex sweepLineVertex, 
                                                         List<HalfEdge> allIntersectEdges, 
                                                         HalfEdgeStructure polygon){
        Set<HalfEdge> halfEdgesInHoles = new HashSet<>(allIntersectEdges);
        Point2f sweepLinePoint = sweepLineVertex.getVertex();
        List<HalfEdge> edgesLeftToPolygon = new ArrayList<>();
        
        for(HalfEdge currHalfEdge : allIntersectEdges){
            long targetVertexID = currHalfEdge.getTargetVertex();
            HEVertex targetVertex = polygon.getHEVertex(targetVertexID);
            Point2f targetPoint = targetVertex.getVertex();
            
            if(halfEdgesInHoles.contains(currHalfEdge)){
                if(targetPoint.y > sweepLinePoint.y){
                    edgesLeftToPolygon.add(currHalfEdge);
                }
            }else{
                if(targetPoint.y < sweepLinePoint.y){
                    edgesLeftToPolygon.add(currHalfEdge);
                }
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
}
