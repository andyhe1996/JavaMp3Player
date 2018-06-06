import javax.swing.JComponent;
import java.awt.*;
import java.awt.geom.*;

public class MusicBar extends JComponent{

	private static final double SPACE_WIDTH = 10.0;

	private double width;
	private double height;
	private int thickness;
	private double dimeter;

	private Line2D.Double progressLine;
	private Ellipse2D.Double progressBall;

	public MusicBar(){
		this(100.0, 50.0, 1);
	}

	public MusicBar(double width, double height, int thickness){
		this.width = width;
		this.height = height;
		this.thickness = thickness;
		dimeter = thickness * 4.0;

		//set up the geometry
		progressLine = new Line2D.Double(SPACE_WIDTH, height / 2.0, SPACE_WIDTH + width, height / 2.0);
		progressBall = new Ellipse2D.Double(SPACE_WIDTH - (dimeter / 2.0), height / 2.0 - (dimeter / 2.0), dimeter, dimeter);

		setPreferredSize(new Dimension((int) width, (int) height));

	}

	public void paintComponent(Graphics g){
		Graphics2D g2 = (Graphics2D) g;
		
		g2.setColor(Color.BLACK);
		g2.setStroke(new BasicStroke(thickness));
		g2.draw(progressLine);
		g2.fill(progressBall);

	}

	//current milli second
	public void updateProgress(int curMS, int totalMS){
		//find the current position by looking at the percentage complete
		double curPos = SPACE_WIDTH + ((double)curMS / (double)totalMS * width);
		progressBall.setFrame(curPos - (dimeter / 2.0), height / 2.0 - (dimeter / 2.0), dimeter, dimeter);
		repaint();
	}
}