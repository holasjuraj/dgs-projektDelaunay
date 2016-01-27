package delaunayVoronoi;

import java.awt.Point;
import java.util.HashSet;

public class Triangulation {
	private static final int BOUNDARY_SIZE = 10000;
	private static final double EPSILON = 0.001;
	private Edge firstEdge;
	private DataPoint boundA, boundB, boundC;
	private int siteCount = 0;
	
	public Triangulation(){
		// Make bounding triangle
		boundA = new DataPoint(DataPoint.SITE, 0, -BOUNDARY_SIZE);
		boundB = new DataPoint(DataPoint.SITE, BOUNDARY_SIZE, BOUNDARY_SIZE);
		boundC = new DataPoint(DataPoint.SITE, -BOUNDARY_SIZE, BOUNDARY_SIZE);
		Edge	e1 = Operators.newEdge(boundA, boundB),
				e2 = Operators.newEdge(boundB, boundC),
				e3 = Operators.newEdge(boundC, boundA);
		// Connect edges
		Operators.splice(e1.sym(), e2);
		Operators.splice(e2.sym(), e3);
		Operators.splice(e3.sym(), e1);
		// Save first edge for traversal
		firstEdge = e1;
		// Compute Voronoi diagram
		computeVoronoi();
	}
	
	// Adding sites to triangulation
	public void addSite(DataPoint x){
		// Locate triangle
		Edge triSide = locate(x);
		if(triSide.origin().equals(x) || triSide.dest().equals(x)){
			// Site is already in the triangulation
			return;
		}
		if(onEdge(x, triSide)){
			// Three colinear points - delete original edge
			Edge temp = triSide.oPrev();
			Operators.delete(triSide);
			triSide = temp;
		}
		
		// Connect x to sites within its triangle
		DataPoint firstSite = triSide.origin();
		Edge starEdge = Operators.newEdge(firstSite, x);
		Operators.splice(starEdge, triSide);	// Connect to first point
		while(true){
			starEdge = Operators.connect(triSide, starEdge.sym());
			triSide = starEdge.oPrev();				// Move to next triangle side
			if(triSide.dest().equals(firstSite)){	// Connected to all sites
				break;
			}
		}

		// Inspecting suspect edges and swapping
		Edge suspectEdge = starEdge.oPrev();
		while(true){
			Edge suspectTri = suspectEdge.oPrev();
			if(	Operators.rightOf(suspectTri.dest(), suspectEdge) &&			// We`re inside of bounding triangle
				Operators.inCircle(suspectEdge.origin(), suspectTri.dest(), suspectEdge.dest(), x)	// In circle test
			){
				// Bad edge - flip
				Operators.flip(suspectEdge);
				// Move to new suspect edge (one CW). The other suspect edge (one CCW) will be examined in next step by turning twice CCW.
				suspectEdge = suspectEdge.oPrev();
			}
			else if(suspectEdge.origin().equals(firstSite)){
				// We returned to start no more suspect edges. Recompute face circumCenters for Voroni diagram and quit. 
				computeVoronoi();
				siteCount++;
				return;
			}
			else{
				// Move to next suspect edge (skip one newly added edge in between).
				suspectEdge = suspectEdge.oNext().oNext().sym();
			}
		}
	}
	
	private Edge locate(Point x){
		/* Jump-and-Walk algorithm. Returns any edge of a triangle containing
		 * given point x (x is on left of result edge or on it).
		 */
		Edge e = getFirstEdge();
		while(true){
			// x is on edge of triangle
			if(onEdge(x, e)){ return e; }
			else if(onEdge(x, e.lNext())){ return e.lNext(); }
			else if(onEdge(x, e.lPrev())){ return e.lPrev(); }
			// x is elsewhere
			if(Operators.rightOf(x, e)){
				e = e.sym();
			}
			else if(Operators.rightOf(x, e.lNext())){
				// Move towards lNext triangle
				e = e.lNext().sym();
			}
			else if(Operators.rightOf(x, e.lPrev())){
				// Move towards lPrev triangle
				e = e.lPrev().sym();
			}
			else{
				// x is on left of all sides of triangle => is in triangle
				return e;
			}
		}
	}
	
	private boolean onEdge(Point x, Edge e){
		// Vectors e.orig -> e.dest and e.orig -> x :
		double	ex = e.dest().getX() - e.origin().getX(),
				ey = e.dest().getY() - e.origin().getY(),
				xx = x.getX() - e.origin().getX(),
				xy = x.getY() - e.origin().getY();
		// Parameter of formula x = e.orig + t*(e.dest - e.orig)
		double	tx = xx / ex,
				ty = xy / ey;
		if(Math.abs(tx - ty) > EPSILON){
			// x is not on line defined by e
			return false;
		}
		if(tx < 0 || tx > 1 || ty < 0 || ty > 1){
			// x is on the line, but not within the segment of e
			return false;
		}
		return true;
	}
	
	// Computing vertexes (face circumCenters) of Voronoi diagram
	public void computeVoronoi(){
		traverse(firstEdge.rot(), new HashSet<Edge>(), new HashSet<DataPoint>());
	}
	
	private void traverse(Edge e, HashSet<Edge> visitedE, HashSet<DataPoint> computedF){
		if(!visitedE.add(e)){
			return;
		}
		if(!computedF.contains(e.origin())){
			// Get face sites
			boolean isBoundingTri = true;
			
			Edge triSide = e.rot();
			isBoundingTri &= edgeFromBoundary(triSide);
			DataPoint a = triSide.origin();
			
			triSide = triSide.lNext();
			isBoundingTri &= edgeFromBoundary(triSide);
			DataPoint b = triSide.origin();
			
			triSide = triSide.lNext();
			isBoundingTri &= edgeFromBoundary(triSide);
			DataPoint c = triSide.origin();

			if(!isBoundingTri){
				// Compute circumCenter of face
				e.data = circumCenter(a, b, c);
			}
			else{
				// Point in infinity
				e.data = new DataPoint(DataPoint.NONE, 0, 0);
			}
			computedF.add(e.origin());
		}
		traverse(e.sym(), visitedE, computedF);
		traverse(e.oNext(), visitedE, computedF);
		traverse(e.lNext(), visitedE, computedF);
	}
	
	private DataPoint circumCenter(DataPoint a, DataPoint b, DataPoint c){
		// Formula from http://en.wikipedia.org/wiki/Circumscribed_circle#Cartesian_coordinates
		Point	bb = new Point(b.x - a.x, b.y - a.y),
				cc = new Point(c.x - a.x, c.y - a.y);
		double d = 2*(bb.getX() * cc.getY()  -  bb.getY() * cc.getX());
		double	bXsqr = bb.getX()*bb.getX(),
				bYsqr = bb.getY()*bb.getY(),
				cXsqr = cc.getX()*cc.getX(),
				cYsqr = cc.getY()*cc.getY();
		// circumCenter of triangle [0,0], bb, cc =
		int centerX = (int)Math.round(  (cc.getY() * (bXsqr + bYsqr)  -  bb.getY() * (cXsqr + cYsqr)) / d  );
		int centerY = (int)Math.round(  (bb.getX() * (cXsqr + cYsqr)  -  cc.getX() * (bXsqr + bYsqr)) / d  );
		// circumCenter of triangle a, b, c =
		centerX += a.x;
		centerY += a.y;
		return new DataPoint(DataPoint.FACE, centerX, centerY);
	}
	
	// Public helper methods
	public Edge getFirstEdge(){
		return firstEdge;
	}
	
	public int size(){
		return siteCount;
	}
	
	public boolean edgeFromBoundary(Edge e){
		// Returns true if edge goes from a boundary point.
		return	boundA.equals(e.origin()) ||
				boundB.equals(e.origin()) ||
				boundC.equals(e.origin());
	}
	
	public boolean edgeToFromBoundary(Edge e){
		// Returns true if edge goes to or from a boundary point.
		return edgeFromBoundary(e) || edgeFromBoundary(e.sym());
	}

}