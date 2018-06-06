import javax.swing.JComponent;
import java.awt.*;
import java.awt.geom.*;

public class MusicBar extends JComponent{

	private static final double SPACE_WIDTH = 10.0;

	private double width;
	private double height;
	private double dimeter;
	private int thickness;

	private Line2D.Double progressLine;
	private Ellipse2D.Double progressBall;

	private boolean dragging;

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
		progressBall = new Ellipse2D.Double((SPACE_WIDTH) - (dimeter / 2.0), (height / 2.0) - (dimeter / 2.0), dimeter, dimeter);
		
		//init is the bar being drag
		dragging = false;

		setPreferredSize(new Dimension((int) width, (int) height));
	}

	public void paintComponent(Graphics g){
		Graphics2D g2 = (Graphics2D) g;
		
		g2.setColor(Color.BLACK);
		g2.setStroke(new BasicStroke(thickness));
		g2.draw(progressLine);
		g2.fill(progressBall);
	}

	//set the position to the mouse position on the progress bar when press/drag
	public void updateProgressByDrag(double x){
		if(x < SPACE_WIDTH){
			x = SPACE_WIDTH;
		}
		else if(x > SPACE_WIDTH + width){
			x = SPACE_WIDTH + width;
		}
		progressBall.setFrame(x - (dimeter / 2.0), (height / 2.0) - (dimeter / 2.0), dimeter, dimeter);
		repaint();
	}

	//current milli second
	public void updateProgress(int curMS, int totalMS){
		//only run when the bar is not dragging
		if(!dragging){
			//find the current position by looking at the percentage complete
			double curPos = SPACE_WIDTH + ((double)curMS / (double)totalMS * width);
			progressBall.setFrame(curPos - (dimeter / 2.0), (height / 2.0) - (dimeter / 2.0), dimeter, dimeter);
			repaint();
		}
	}

	//start drag
	public void startDrag(){
		dragging = true;
	}

	//finish drag
	public void finishDrag(){
		dragging = false;
	}

	//calculate the current time in millisecond given the current position
	public int calMS(int position, int totalMS){
		double x = position;

		if(x < SPACE_WIDTH){
			x = SPACE_WIDTH;
		}
		else if(x > SPACE_WIDTH + width){
			x = SPACE_WIDTH + width;
		}

		double ms = ((x - SPACE_WIDTH) / width) * (double)totalMS;
		return (int)ms;
	}

}