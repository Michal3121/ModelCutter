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
public class EdgeWithHelper {
    private final long edgeID;
    private HEVertex startEdgePoint;
    private HEVertex endEdgePoint;
    private Point2f intersectPoint;
    private long helperID;
    private HEVertex helper;
    
    public EdgeWithHelper(long halfEdgeID, HEVertex startPoint, HEVertex endPoint){
        this.edgeID = halfEdgeID;
        this.startEdgePoint = startPoint;
        this.endEdgePoint = endPoint;
    }

    public long getEdgeID() {
        return edgeID;
    }

    public HEVertex getStartEdgePoint() {
        return startEdgePoint;
    }

    public void setStartEdgePoint(HEVertex startEdgePoint) {
        this.startEdgePoint = startEdgePoint;
    }

    public HEVertex getEndEdgePoint() {
        return endEdgePoint;
    }

    public void setEndEdgePoint(HEVertex endEdgePoint) {
        this.endEdgePoint = endEdgePoint;
    }
    
    public Point2f getIntersectPoint() {
        return intersectPoint;
    }

    public void setIntersectPoint(Point2f intersectPoint) {
        this.intersectPoint = intersectPoint;
    }

    public HEVertex getHelper() {
        return helper;
    }

    public void setHelper(HEVertex helper) {
        this.helper = helper;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + (int) (this.edgeID ^ (this.edgeID >>> 32));
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
        final EdgeWithHelper other = (EdgeWithHelper) obj;
        if (this.edgeID != other.edgeID) {
            return false;
        }
        return true;
    }
    
}
