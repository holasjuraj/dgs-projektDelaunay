package delaunayVoronoi;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Checkbox;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Random;

public class ProgramApplet extends Applet {
	private static final long serialVersionUID = -1484119501224210012L;
	
	private static final int INIT_WIDTH = 700;
	private static final int INIT_HEIGHT = 500;
	
	// GUI components
	Checkbox cbDelaunay = new Checkbox("Show Delaunay triangulation");
	Checkbox cbVoronoi = new Checkbox("Show Voronoi diagram");
	Checkbox cbCircles = new Checkbox("Show circles");
	private Button btnAddRandom = new Button("Add 10 random sites");
	private Button btnClearPointInCell = new Button("Clear point in cell");
	private Button btnClearAll = new Button("Clear all");
	private DrawPanel pnlDraw;
	
	// Structures
	Triangulation DT;
	Random rand = new Random();

	public static void main(String[] args) {
		ProgramApplet applet = new ProgramApplet();
		applet.init();
	}
	
	public void init(){
		resize(INIT_WIDTH, INIT_HEIGHT);
		setLayout(new BorderLayout());
		
		// Controls panel
		Panel pnlContrContainer = new Panel();
		pnlContrContainer.setBackground(new Color(239, 239, 239));
		add("West", pnlContrContainer);
		Panel pnlControls = new Panel();
		pnlControls.setLayout(new GridLayout(6, 1));
		pnlContrContainer.add(pnlControls);
		pnlControls.add(cbDelaunay);
		pnlControls.add(cbVoronoi);
		pnlControls.add(cbCircles);
		pnlControls.add(btnAddRandom);
		pnlControls.add(btnClearPointInCell);
		pnlControls.add(btnClearAll);
		cbDelaunay.setState(true);
		
		// Draw panel
		pnlDraw = new DrawPanel(this);
		add("Center", pnlDraw);
		initDT();
		
		// Listeners
		btnAddRandom.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				for(int i = 0; i < 10; i++){
					DT.addSite(new DataPoint(DataPoint.SITE,
							rand.nextInt(pnlDraw.getWidth()),
							rand.nextInt(pnlDraw.getHeight())));
				}
				pnlDraw.repaint();
			}
		});
		btnClearPointInCell.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				pnlDraw.pointInCell = null;
				pnlDraw.repaint();
			}
		});
		btnClearAll.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				initDT();
			}
		});
		
		// Repaint when selection is changed
		final DrawPanel finPnlDrw = pnlDraw;
		MouseAdapter repaintOnChange = new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				finPnlDrw.repaint();
			}
		};
		cbDelaunay.addMouseListener(repaintOnChange);
		cbVoronoi.addMouseListener(repaintOnChange);
		cbCircles.addMouseListener(repaintOnChange);
	}
	
	public void initDT(){
		DT = new Triangulation();
		pnlDraw.pointInCell = null;
		pnlDraw.repaint();
	}

}