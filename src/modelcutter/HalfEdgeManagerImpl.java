/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelcutter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
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
        
        return monotonePieces;
    }
    
    private List<HEVertex> findSortedVertices(HalfEdgeStructure polygon){
        List<HEVertex> vertices = new ArrayList<>(polygon.getHEVertices());
        
        Collections.sort(vertices, new HEVertexPositionComparator());
        
        return vertices;
    }
}
