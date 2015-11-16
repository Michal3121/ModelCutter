/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelcutter;

/**
 *
 * @author MICHAL
 */
public class HalfEdge {
    private final long id;
    private long targetVertex; // vertex na konci half edge
    private long twin;
    private long face;
    private long next;
    private long prev;
    
    public HalfEdge(long id, long targetVertID){
        this.id = id;
        this.targetVertex = targetVertID;
    }

    public long getId() {
        return id;
    }

    public long getTargetVertex() {
        return targetVertex;
    }

    public void setTargetVertex(long targetVertex) {
        this.targetVertex = targetVertex;
    }

    public long getTwin() {
        return twin;
    }

    public void setTwin(long twin) {
        this.twin = twin;
    }

    public long getFace() {
        return face;
    }

    public void setFace(long face) {
        this.face = face;
    }

    public long getNext() {
        return next;
    }

    public void setNext(long next) {
        this.next = next;
    }

    public long getPrev() {
        return prev;
    }

    public void setPrev(long prev) {
        this.prev = prev;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + (int) (this.id ^ (this.id >>> 32));
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
        final HalfEdge other = (HalfEdge) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }
    
}
