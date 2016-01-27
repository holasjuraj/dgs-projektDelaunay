package delaunayVoronoi;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Panel;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;

public class DrawPanel extends Panel {
	private static final long serialVersionUID = -7559497381715935304L;
	// Colors
	private static final Color CLR_DELAUNAY_SITE = Color.BLACK;
	private static final Color CLR_DELAUNAY_EDGE = Color.BLUE;
	private static final Color CLR_VORONOI_VERTEX = new Color(128, 0, 0);
	private static final Color CLR_VORONOI_EDGE = Color.RED;
	private static final Color CLR_CIRCLE = Color.LIGHT_GRAY;
	private static final Color CLR_POINT_IN_CELL = Color.RED;
	private static final Color CLR_POINT_IN_CELL_POLY = new Color(255, 224, 192);
	
	private ProgramApplet applet;
	Point pointInCell = null;

	public DrawPanel(ProgramApplet applet) {
		this.applet = applet;
		final DrawPanel thisObj = this;
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				int but = e.getButton();
				if(but == MouseEvent.BUTTON1){
					// Left click
					thisObj.applet.DT.addSite(new DataPoint(DataPoint.SITE, e.getX(), e.getY()));
				}
				else if(but == MouseEvent.BUTTON2 || but == MouseEvent.BUTTON3){
					// Right click
					pointInCell = new Point(e.getX(), e.getY());
				}
				thisObj.repaint();
			}
		});
	}

	// Main paint method
	public void paint(final Graphics g) {
		// Clear
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, getWidth(), getHeight());
		// Paint extras
		if(pointInCell != null){
			paintPointInCell(g);
		}
		if(applet.cbVoronoi.getState()){
			paintVoronoi(g);
		}
		if(applet.cbDelaunay.getState()){
			paintDelaunay(g);
		}
		if(applet.cbCircles.getState()){
			paintCircles(g);
		}
		// Paint sites
		traverse(
			TRAVERSE_VERTEXES,
			applet.DT.getFirstEdge(),
			new EdgeProcessor() {
				@Override
				public void process(Edge e) {
					paintPoint(g, e.origin(), CLR_DELAUNAY_SITE, 3);
				}
		});
		// Bug fix for first site
		if(applet.DT.size() == 1){
			paintPoint(g, applet.DT.getFirstEdge().lNext().dest(), CLR_DELAUNAY_SITE, 3);
		}
	}
	
	// Helper paint methods
	private void paintDelaunay(final Graphics g){
		traverse(
			TRAVERSE_EDGES,
			applet.DT.getFirstEdge(),
			new EdgeProcessor() {
				@Override
				public void process(Edge e) {
					//# DEBUG if(controller.DT.edgeToBoundary(e)){ return; }
					paintLine(g, e.origin(), e.dest(), CLR_DELAUNAY_EDGE);
				}
		});
	}
	
	private void paintVoronoi(final Graphics g){
		traverse(
			TRAVERSE_EDGES,
			applet.DT.getFirstEdge().rot(),
			new EdgeProcessor() {
				@Override
				public void process(Edge e) {
					if(e.origin().type == DataPoint.FACE){
						paintPoint(g, e.origin(), CLR_VORONOI_VERTEX, 2);
						if(e.dest().type == DataPoint.FACE){
							paintLine(g, e.origin(), e.dest(), CLR_VORONOI_EDGE);
						}
					}
				}
		});
	}
	
	private void paintCircles(final Graphics g){
		traverse(
			TRAVERSE_VERTEXES,
			applet.DT.getFirstEdge().rot(),
			new EdgeProcessor() {
				@Override
				public void process(Edge e) {
					if(e.origin().type == DataPoint.FACE){
						// Don`t paint circles with boundary
						Edge temp = e.rot().lNext();
						while(!temp.equals(e.rot())){
							if(applet.DT.edgeToFromBoundary(temp)){
								return;
							}
							temp = temp.lNext();
						}
						paintCircle(g, e.origin(), e.rot().origin(), CLR_CIRCLE);
					}
				}
		});
	}
	
	private void paintPointInCell(final Graphics g){
		try{
			// Bug fix for 0 or 1 sites
			if(applet.DT.size() < 2){
				g.setColor(CLR_POINT_IN_CELL_POLY);
				g.fillRect(0, 0, getWidth(), getHeight());
				paintPoint(g, pointInCell, CLR_POINT_IN_CELL, 3);
			}
			
			class ClosestPoint{
				public Edge leavingEdge = null;
				public double dist = Double.MAX_VALUE;
			}
			final ClosestPoint closest = new ClosestPoint();
			final DrawPanel thisObj = this;
			// Find the closest site
			traverse(
				TRAVERSE_VERTEXES,
				thisObj.applet.DT.getFirstEdge(),
				new EdgeProcessor() {
					@Override
					public void process(Edge e) {
						double	dx = pointInCell.getX() - e.origin().getX(),
								dy = pointInCell.getY() - e.origin().getY(),
								dist = Math.sqrt(dx*dx + dy*dy);
						if(dist < closest.dist){
							closest.leavingEdge = e;
							closest.dist = dist;
						}
					}
			});
			// Traverse point of Voronoi polygon
			Edge e = closest.leavingEdge.rot();
			Polygon poly = new Polygon();
			do{
				poly.addPoint(e.origin().x, e.origin().y);
				e = e.lNext();
			} while(!e.equals(closest.leavingEdge.rot()));
			g.setColor(CLR_POINT_IN_CELL_POLY);
			g.fillPolygon(poly);
			paintPoint(g, pointInCell, CLR_POINT_IN_CELL, 3);
			paintPoint(g, closest.leavingEdge.origin(), CLR_DELAUNAY_EDGE, 6);
		}
		catch(NullPointerException e){ /* Should never happen, but, you know... */ }
	}
	
	private void paintPoint(Graphics g, Point p, Color color, int radius){
		g.setColor(color);
		g.fillOval(p.x - radius, p.y - radius, radius * 2, radius * 2);
	}

	private void paintLine(Graphics g, Point a, Point b, Color color){
		g.setColor(color);
		g.drawLine(a.x, a.y, b.x, b.y);
	}
	
	private void paintCircle(Graphics g, Point s, Point a, Color color){
		// Draws a circle with center s and through point a
		g.setColor(color);
		double	dx = s.getX() - a.getX(),
				dy = s.getY() - a.getY();
		double radius = Math.sqrt(dx*dx + dy*dy);
		g.drawOval(	(int)Math.round(s.getX() - radius),
					(int)Math.round(s.getY() - radius),
					(int)Math.round(radius * 2),
					(int)Math.round(radius * 2));
	}
	
	// Graph traversal
	private static final int TRAVERSE_EDGES = 1;
	private static final int TRAVERSE_VERTEXES = 2;
	
	private static abstract class EdgeProcessor{
		public abstract void process(Edge e);
	}
	
	private void traverse(int mode, Edge firstEdge, EdgeProcessor proc){
		traverseRec(mode, firstEdge, new HashSet<Edge>(), new HashSet<DataPoint>(), proc);
	}
	
	private void traverseRec(int mode, Edge e, HashSet<Edge> visitedE, HashSet<DataPoint> processedV, EdgeProcessor proc){
		if(!visitedE.add(e)){
			return;
		}
		// Ignore boundary edges/sites/faces
		if(	!applet.DT.edgeToFromBoundary(e) &&
			!applet.DT.edgeToFromBoundary(e.rot())
		){
			// Process all edges or only those with unprocessed origin
			if(mode == TRAVERSE_EDGES || processedV.add(e.origin())){
				proc.process(e);
			}
		}
		traverseRec(mode, e.sym(), visitedE, processedV, proc);
		traverseRec(mode, e.oNext(), visitedE, processedV, proc);
		traverseRec(mode, e.lNext(), visitedE, processedV, proc);
	};
	
}