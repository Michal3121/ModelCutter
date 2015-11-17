/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelcutter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author MICHAL
 */
public class HEFace {
    private final long id;
    private List<Long> holesHalfEdgeIDs;
    private long innerHalfEdgeID;
    
    public HEFace(long id, long innerHalfEdgeID){
        this.id = id;
        this.innerHalfEdgeID = innerHalfEdgeID;
        this.holesHalfEdgeIDs = new ArrayList<>();
    }

    public long getId() {
        return id;
    }

    public long getInnerHalfEdgeID() {
        return innerHalfEdgeID;
    }

    public void setInnerHalfEdgeID(long innerHalfEdgeID) {
        this.innerHalfEdgeID = innerHalfEdgeID;
    }
    
    public void addHoleHalfEdgeID(long holeHalfEdgeID){
        this.holesHalfEdgeIDs.add(holeHalfEdgeID);
    }
    
    public Collection<Long> getHoleHalfEdges(){
        return Collections.unmodifiableCollection(holesHalfEdgeIDs);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 43 * hash + (int) (this.id ^ (this.id >>> 32));
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
        final HEFace other = (HEFace) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }
    
}
