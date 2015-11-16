/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelcutter;

import javax.vecmath.Point2f;

/**
 *
 * @author MICHAL
 */
public class HEVertex {
    private final long id;
    private Point2f vertex;
    private long leavingHalfEdgeID;
    
    public HEVertex(long id, Point2f vertex, long leavingHalfedgeID){
        this.id = id;
        this.vertex = vertex;
        this.leavingHalfEdgeID = leavingHalfedgeID;
    }

    public long getId() {
        return id;
    }
    
    public Point2f getVertex() {
        return vertex;
    }

    public void setVertex(Point2f vertex) {
        this.vertex = vertex;
    }

    public long getLeavingHalfEdgeID() {
        return leavingHalfEdgeID;
    }

    public void setLeavingHalfEdgeID(long leavingHalfEdgeID) {
        this.leavingHalfEdgeID = leavingHalfEdgeID;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + (int) (this.id ^ (this.id >>> 32));
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
        final HEVertex other = (HEVertex) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }
    
}
