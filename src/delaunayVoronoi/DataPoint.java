package delaunayVoronoi;

import java.awt.Point;

public class DataPoint extends Point {
	private static final long serialVersionUID = 6636520486044399750L;
	public static final int NONE = 0;
	public static final int SITE = 1;
	public static final int FACE = 2;
	
	public int type = NONE;

	public DataPoint(int type, int x, int y) {
		super(x, y);
		this.type = type;
	}

	public DataPoint(Point p) {
		super(p);
	}
	
	@Override
	public boolean equals(Object obj) {
		try{
			DataPoint p = (DataPoint)obj;
			return x == p.x && y == p.y && type == p.type;
		}
		catch(Exception e){
			return false;
		}
	}

	public String toString(){
		String result = "?";
		if(type == SITE){ result = "site"; }
		else if(type == FACE){ result = "face"; }
		return result + " ["+x+", "+y+"]";
	}

}