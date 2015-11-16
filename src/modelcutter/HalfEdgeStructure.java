/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelcutter;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author MICHAL
 */
public class HalfEdgeStructure {
    private Map<Long, HEVertex> vertices;
    private Map<Long, HalfEdge> halfEdges;
    private Map<Long, HEFace> faces;
    private int areaOfPolygon;
    
    public HalfEdgeStructure(){
        this.vertices = new HashMap<>();
        this.halfEdges = new HashMap<>();
        this.faces = new HashMap<>();
    }
    
    public void addVertex(HEVertex vertex){
        this.vertices.put(vertex.getId(), vertex);
    }
    
    public void addHalfEdge(HalfEdge halfEdge){
        this.halfEdges.put(halfEdge.getId(), halfEdge);
    }
    
    public void addFace(HEFace face){
        this.faces.put(face.getId(), face);
    }
    
    public HEVertex getHEVertex(long id){
        return this.vertices.get(id);
    }
    
    public HalfEdge getHalfEdge(long id){
        return this.halfEdges.get(id);
    }
    
    public HEFace getHEFace(long id){
        return this.faces.get(id);
    }
    
    public Collection<HEVertex> getHEVertices(){
        return Collections.unmodifiableCollection(this.vertices.values());
    }
    
    public Collection<HalfEdge> getHalfEdges(){
        return Collections.unmodifiableCollection(this.halfEdges.values());
    }
    
    public Collection<HEFace> getFaces(){
        return Collections.unmodifiableCollection(this.faces.values());
    }

    public int getAreaOfPolygon() {
        return areaOfPolygon;
    }

    public void setAreaOfPolygon(int areaOfPolygon) {
        this.areaOfPolygon = areaOfPolygon;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 47 * hash + Objects.hashCode(this.vertices);
        hash = 47 * hash + Objects.hashCode(this.halfEdges);
        hash = 47 * hash + Objects.hashCode(this.faces);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final HalfEdgeStructure other = (HalfEdgeStructure) obj;
        if (!Objects.equals(this.vertices, other.vertices)) {
            return false;
        }
        if (!Objects.equals(this.halfEdges, other.halfEdges)) {
            return false;
        }
        if (!Objects.equals(this.faces, other.faces)) {
            return false;
        }
        return true;
    }
    
}
