import javax.swing.JComponent;
import java.awt.*;
import java.awt.geom.*;

public class MusicBar extends JComponent{

	private double width;
	private double height;
	private int thickness;

	public MusicBar(){
		this(100.0, 50.0, 1);
	}

	public MusicBar(double width, double height, int thickness){
		this.width = width;
		this.height = height;
		this.thickness = thickness;

		setPreferredSize(new Dimension((int) width, (int) height));

	}

	public void paintComponent(Graphics g){
		Graphics2D g2 = (Graphics2D) g;
		
		g2.setColor(Color.BLACK);
		g2.setStroke(new BasicStroke(thickness));
		Line2D.Double progressLine = new Line2D.Double(10.0, height / 2.0, 10.0 + width, height / 2.0);

		g2.draw(progressLine);

	}
}