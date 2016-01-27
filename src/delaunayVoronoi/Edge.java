package delaunayVoronoi;

public class Edge {
	private QuadEdge parentQE;
	private int numInQE;
	Edge oNextEdge;
	DataPoint data;
	
	public Edge(QuadEdge parentQE, int numInQE){
		this.parentQE = parentQE;
		this.numInQE = numInQE;
	}

	// Edge algebra within QuadEdge
	public Edge rot(){    return parentQE.edges[(numInQE + 1) % 4]; }
	public Edge sym(){    return parentQE.edges[(numInQE + 2) % 4]; }
	public Edge rotInv(){ return parentQE.edges[(numInQE + 3) % 4]; }
	
	// Edge algebra - next and previous edges
	public Edge oNext(){ return oNextEdge; }
	public Edge dNext(){ return sym().oNext().sym(); }
	public Edge lNext(){ return rotInv().oNext().rot(); }
	public Edge rNext(){ return rot().oNext().rotInv(); }
	public Edge oPrev(){ return rNext().sym(); }
	public Edge dPrev(){ return lNext().sym(); }
	public Edge lPrev(){ return oNext().sym(); }
	public Edge rPrev(){ return dNext().sym(); }

	// End data
	public DataPoint origin(){ return data; }
	public DataPoint dest(){ return sym().data; }
	public QuadEdge getQE(){ return parentQE; }
	
	public void setEnds(DataPoint newOrigin, DataPoint newDest){
		data = newOrigin;
		sym().data = newDest;
	}	
	
	public String toString(){
		return "edge No."+numInQE+" "+origin()+" -> "+dest();
	}

}