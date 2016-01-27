package delaunayVoronoi;

import java.awt.Point;

public class Operators {

	// GEOMETRIC OPERATORS //
	public static double det3(Point a, Point b, Point c){
		/* Computes following determinant:
		 *   |a.x  a.y  1|
		 *   |b.x  b.y  1|
		 *   |c.x  c.y  1|
		 */
		return	(b.getX() - a.getX())*(c.getY() - a.getY()) -
				(c.getX() - a.getX())*(b.getY() - a.getY());
	}
	
	public static boolean inCircle(Point a, Point b, Point c, Point d){
		/* Returns true if d is inside of a circle defined by points a, b and
		 * c. Computed by determinant:
		 *   |a.x  a.y  a.x^2+a.y^2  1|
		 *   |b.x  b.y  b.x^2+b.y^2  1|  > 0
		 *   |c.x  c.y  c.x^2+c.y^2  1|
		 *   |d.x  d.y  d.x^2+d.y^2  1|
		 * by Laplace expansion of the third column.
		 * http://en.wikipedia.org/wiki/Delaunay_triangulation#Algorithms
		 */
		double det = (a.getX()*a.getX() + a.getY()*a.getY()) * det3(b, c, d);
		det -= (b.getX()*b.getX() + b.getY()*b.getY()) * det3(a, c, d);
		det += (c.getX()*c.getX() + c.getY()*c.getY()) * det3(a, b, d);
		det -= (d.getX()*d.getX() + d.getY()*d.getY()) * det3(a, b, c);
		return det > 0;
	}
	
	public static boolean rightOf(Point x, Edge e){
		/* Standard orientation function - returns true if x is strictly on the
		 * right side of an oriented line defined by edge e.
		 */
		return det3(e.origin(), e.dest(), x) < 0;
	}
	
	// EDGE OPERATORS //
	public static Edge newEdge(){
		// Make new  edge within a QuadEdge
		QuadEdge qe = new QuadEdge();
		// Edges 0 and 2 are connecting sites
		qe.edges[0].oNextEdge = qe.edges[0];
		qe.edges[2].oNextEdge = qe.edges[2];
		// Edges 1 and 3 are connecting faces (dual edges)
		qe.edges[1].oNextEdge = qe.edges[3];
		qe.edges[3].oNextEdge = qe.edges[1];
		return qe.edges[0];
	}
	
	public static Edge newEdge(DataPoint origin, DataPoint dest){
		// Make new edge and set its end points
		Edge e = newEdge();
		e.setEnds(origin, dest);
		return e;
	}
	
	public static void splice(Edge a, Edge b){
		/* Operator that modifies edges, affecting two tuples of edge rings:
		 * [a.origin and b.origin] and [a.left and b.left]. If the two rings in
		 * a tuple are different, splice will merge them, and if they are same,
		 * splice will split them. (Guibas and Stolfi, 1985, pages 96 - 102)
		 */
		Edge alpha = a.oNext().rot();
		Edge beta = b.oNext().rot();
		// Swap values of a.oNext <-> b.oNext and alpha.oNext <-> beta.oNext
		Edge temp = a.oNextEdge;
		a.oNextEdge = b.oNextEdge;
		b.oNextEdge = temp;
		
		temp = alpha.oNextEdge;
		alpha.oNextEdge = beta.oNextEdge;
		beta.oNextEdge = temp;
	}
	
	public static Edge connect(Edge a, Edge b){
		/* Connects a.dest and b.orig by newly created edge, returns this new
		 * connection edge.
		 */
		Edge e = newEdge();
		e.setEnds(a.dest(), b.origin());
		// Connect the rings
		splice(e, a.lNext());	// connect e.origin <--> a.dest==a.lNext.origin
		splice(e.sym(), b);		// connect e.dest==e.sym.origin <--> b.origin
		return e;
	}
	
	public static void delete(Edge e){
		splice(e, e.oPrev());				// disconnect e.origin from vertex
		splice(e.sym(), e.sym().oPrev());	// disconnect e.dest from vertex
	}

	public static void flip(Edge e){
		// Flips the edge to the other diagonal by turning it CCW.
		Edge botR = e.oPrev();
		Edge topR = e.dNext();
		Edge botL = e.lPrev();
		Edge topL = e.sym().oPrev();
		e.setEnds(topR.origin(), botL.origin());
		// Disconnect from sturcture
		splice(e, botR);
		splice(e.sym(), topL);
		// Connect to right place
		splice(e, topR);
		splice(e.sym(), botL);
	}
	
}