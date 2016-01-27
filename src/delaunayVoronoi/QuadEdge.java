package delaunayVoronoi;

public class QuadEdge {
	Edge[] edges = new Edge[4];
	
	public QuadEdge(){
		for(int i = 0; i < 4; i++){
			edges[i] = new Edge(this, i);
		}
	}

}