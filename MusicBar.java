import javax.swing.JComponent;
import java.awt.*;
import java.awt.geom.*;

public class MusicBar extends JComponent{

	public static final int SPACE_WIDTH = 10;

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
		progressLine = new Line2D.Double(0.0, height / 2.0, width, height / 2.0);
		progressBall = new Ellipse2D.Double(0.0 - (dimeter / 2.0), (height / 2.0) - (dimeter / 2.0), dimeter, dimeter);
		
		//init is the bar being drag
		dragging = false;

		setPreferredSize(new Dimension((int) width, (int) height));
	}

	public void paintComponent(Graphics g){
		Graphics2D g2 = (Graphics2D) g;
		
		//color = light grey, transparent = 70%, range (0-255)
		Color progressColor = new Color(50, 50, 50, 255 * 7 / 10);
		g2.setColor(progressColor);
		g2.setStroke(new BasicStroke(thickness));
		g2.draw(progressLine);
		g2.fill(progressBall);
	}

	//set the position to the mouse position on the progress bar when press/drag
	public void updateProgressByDrag(double x){
		if(x < 0){
			x = 0;
		}
		else if(x > width){
			x = width;
		}
		progressBall.setFrame(x - (dimeter / 2.0), (height / 2.0) - (dimeter / 2.0), dimeter, dimeter);
		repaint();
	}

	//current milli second
	public void updateProgress(int curMS, int totalMS){
		//only run when the bar is not dragging
		if(!dragging){
			//find the current position by looking at the percentage complete
			double curPos = ((double)curMS / (double)totalMS * width);
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

		if(x < 0){
			x = 0;
		}
		else if(x > width){
			x = width;
		}

		double ms = (x / width) * (double)totalMS;
		return (int)ms;
	}

}