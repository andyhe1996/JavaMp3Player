import javax.swing.JPanel;
import java.awt.*;

public class BackgroundPanel extends JPanel{

	private Image backgroundImage;

	public BackgroundPanel(Image bgI){
		super();
		backgroundImage = bgI;
	}

	public void paintComponent(Graphics g){
		super.paintComponent(g);
		g.drawImage(backgroundImage, 0, 0, this.getWidth(), this.getHeight(), null);
	}
}