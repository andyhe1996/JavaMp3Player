import javax.swing.JComponent;
import java.awt.*;
import java.awt.geom.*;

public class MusicBar extends JComponent{

	private double length;
	private double thickness;

	public MusicBar(){
		this(100.0, 1.0);
	}

	public MusicBar(double length, double thickness){
		this.length = length;
		this.thickness = thickness;

		setPreferredSize(new Dimension((int)length + 30, 40));

	}

	public void paintComponent(Graphics g){
		Graphics2D g2 = (Graphics2D) g;
		g2.setColor(Color.BLACK);

		//Line2D(xStart, yStart, xEnd, yEnd)
		Line2D.Double progressLine = new Line2D.Double(0.0, 20.0, length, 20.0);

		g2.draw(progressLine);

	}
}