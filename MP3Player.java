import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.sound.sampled.*;
import javax.imageio.ImageIO;
import java.io.*;
import java.awt.event.*;
import java.awt.*;
import java.util.*;
import java.time.Clock;
//import javazoom.spi.mpeg.sampled.file.*;
import org.tritonus.share.sampled.file.*;
import org.tritonus.share.sampled.TAudioFormat;
//import javazoom.jl.player.*;
//import javazoom.jl.player.advanced.*;

public class MP3Player{

	//interal helper var
	private static final int DEFAULT_WIDTH = 800;
	private static final int DEFAULT_HEIGHT = 600;

	//private static Player playMP3;
	//private static FileInputStream fis;
	private static MusicPlayer player;
	private static Thread curPlay;

	public static boolean isPause;

	private static File musicFolder;
	private static ArrayList<String> playList;
	private static int index;
	private static int preSongIndex;
	private static String[] nameList;
	private static Random rand;
	private static int loopingStatus;

	//interface var
	private static JFrame frame;
	
	private static JPanel upperMainPanel;
	private static JPanel lowerMainPanel;
	private static JPanel listPanel;
	private static JPanel upperTempPanel;
	private static JPanel buttonsPanel;
	private static JPanel musicBarPanel;

	private static Image musicBarBackground;
	private static Image musicListBackground;
	private static Image buttonBackground;

	private static JButton playPauseButton;
	private static JButton playNextButton;
	private static JButton playPreButton;
	private static JButton stopButton;

	private static JScrollPane scrollBar;
	private static JList<String> showList;

	private static MusicBar bar;
	private static JLabel timerDisplay;
	private static JButton replayButton;
	private static JButton shuffleButton;
	private static JButton playLoopButton;
	private static Image[] barButtonIcons;

	public static void main(String[] args){

		try{
			//load initial stuff
			loading();

			//set up the panels
			PanelInit();

			//set up the play buttons
			buttonsInit();

			//set up the music list
			listInit();

			//set up the music duration bar
			musicBarInit();
		}catch(IOException e){
			e.printStackTrace();
		}
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);


	}

	//initialize the music progress bar, with timers
	public static void musicBarInit(){

		int barPanelWidth = (int)musicBarPanel.getPreferredSize().getWidth();
		int barPanelHeight = (int)musicBarPanel.getPreferredSize().getHeight();

		int timerWidth = 50;
		int progressBarWidth = barPanelWidth - timerWidth;
		int progressBarHeight = 10;

		//MusicBar(width, height, thickness)
		bar = new MusicBar((double)progressBarWidth, (double)progressBarHeight, 2);
		bar.setBounds(MusicBar.SPACE_WIDTH, barPanelHeight / 2, progressBarWidth, progressBarHeight);

		timerDisplay = new JLabel("00:00  ");
		timerDisplay.setBounds(2 * MusicBar.SPACE_WIDTH + progressBarWidth, barPanelHeight / 2, timerWidth, progressBarHeight);

		//set up drag listener for draging
		class DragBarListener extends MouseAdapter{
			public void mousePressed(MouseEvent e){
				//System.out.println(e.getX());
				bar.startDrag();
				bar.updateProgressByDrag((double) e.getX());
			}
			public void mouseReleased(MouseEvent e){
				bar.updateProgressByDrag((double) e.getX());
				bar.finishDrag();
				startMusicAtPos(e.getX());
			}
			public void mouseDragged(MouseEvent e){
				bar.updateProgressByDrag((double) e.getX());
			}

		}

		MouseListener dragBarListener = new DragBarListener();
		bar.addMouseListener(dragBarListener);
		bar.addMouseMotionListener((MouseMotionListener)dragBarListener);

		barButtonInit();

		musicBarPanel.add(timerDisplay);
		musicBarPanel.add(bar);
	}

	public static void barButtonInit(){

		int buttonSize = 25;

		Image iconLoad = resizeImage(barButtonIcons[0], buttonSize, buttonSize);
		replayButton = new JButton(new ImageIcon(iconLoad));

		iconLoad = resizeImage(barButtonIcons[1], buttonSize, buttonSize);
		playLoopButton = new JButton(new ImageIcon(iconLoad));

		iconLoad = resizeImage(barButtonIcons[2], buttonSize, buttonSize);
		shuffleButton = new JButton(new ImageIcon(iconLoad));

		
		replayButton.setPreferredSize(new Dimension(buttonSize, buttonSize));
		playLoopButton.setPreferredSize(new Dimension(buttonSize, buttonSize));
		shuffleButton.setPreferredSize(new Dimension(buttonSize, buttonSize));

		int posx = (int)musicBarPanel.getPreferredSize().getWidth() * 2 / 5;
		int posy = (int)musicBarPanel.getPreferredSize().getHeight() * 7 / 10;

		replayButton.setBounds(posx, posy, buttonSize, buttonSize);
		posx += (int)(buttonSize * 1.5);
		playLoopButton.setBounds(posx, posy, buttonSize, buttonSize);
		posx += (int)(buttonSize * 1.5);
		shuffleButton.setBounds(posx, posy, buttonSize, buttonSize);

		//testing transparent buttons
		replayButton.setOpaque(false);
		replayButton.setContentAreaFilled(false);
		replayButton.setBorderPainted(false);

		playLoopButton.setOpaque(false);
		playLoopButton.setContentAreaFilled(false);
		playLoopButton.setBorderPainted(false);

		shuffleButton.setOpaque(false);
		shuffleButton.setContentAreaFilled(false);
		shuffleButton.setBorderPainted(false);

		//add listeners
		//replay after finish
		class ReplayListener implements ActionListener{
			public void actionPerformed(ActionEvent e){
				loopingStatus = MusicPlayer.REPLAY;
				if(player != null)
					player.setStatus(MusicPlayer.REPLAY);

				//replay active: image index 3
				Image iconLoad = resizeImage(barButtonIcons[3], buttonSize, buttonSize);
				replayButton.setIcon(new ImageIcon(iconLoad));

				iconLoad = resizeImage(barButtonIcons[1], buttonSize, buttonSize);
				playLoopButton.setIcon(new ImageIcon(iconLoad));

				iconLoad = resizeImage(barButtonIcons[2], buttonSize, buttonSize);
				shuffleButton.setIcon(new ImageIcon(iconLoad));
			}
		}
		ActionListener replayListener = new ReplayListener();
		replayButton.addActionListener(replayListener);

		class PlayLoopListener implements ActionListener{
			public void actionPerformed(ActionEvent e){
				loopingStatus = MusicPlayer.LOOP;
				if(player != null)
					player.setStatus(MusicPlayer.LOOP);

				Image iconLoad = resizeImage(barButtonIcons[0], buttonSize, buttonSize);
				replayButton.setIcon(new ImageIcon(iconLoad));

				//loop active: image index 4
				iconLoad = resizeImage(barButtonIcons[4], buttonSize, buttonSize);
				playLoopButton.setIcon(new ImageIcon(iconLoad));

				iconLoad = resizeImage(barButtonIcons[2], buttonSize, buttonSize);
				shuffleButton.setIcon(new ImageIcon(iconLoad));
			}
		}
		ActionListener loopListener = new PlayLoopListener();
		playLoopButton.addActionListener(loopListener);

		class ShuffleListener implements ActionListener{
			public void actionPerformed(ActionEvent e){
				loopingStatus = MusicPlayer.RANDOM;
				if(player != null)
					player.setStatus(MusicPlayer.RANDOM);

				Image iconLoad = resizeImage(barButtonIcons[0], buttonSize, buttonSize);
				replayButton.setIcon(new ImageIcon(iconLoad));

				iconLoad = resizeImage(barButtonIcons[1], buttonSize, buttonSize);
				playLoopButton.setIcon(new ImageIcon(iconLoad));

				//shuffle active: image index 5
				iconLoad = resizeImage(barButtonIcons[5], buttonSize, buttonSize);
				shuffleButton.setIcon(new ImageIcon(iconLoad));
			}
		}
		ActionListener shuffleListener = new ShuffleListener();
		shuffleButton.addActionListener(shuffleListener);

		musicBarPanel.add(replayButton);
		musicBarPanel.add(playLoopButton);
		musicBarPanel.add(shuffleButton);
	}

	//resize Image helper function
	private static Image resizeImage(Image image, int resizedWidth, int resizedHeight) { 
    	Image resizedImage = image.getScaledInstance(resizedWidth, resizedHeight,  java.awt.Image.SCALE_SMOOTH);  
    	return resizedImage;
	}

	//initialize the music list
	public static void listInit(){

		//selectColor = white
		Color selectColor = new Color(255, 255, 255);
		
		scrollBar = new JScrollPane();
		showList = new JList<String>(nameList);

		int listWidth = (int)listPanel.getPreferredSize().getWidth() * 9 / 10;
		int listHeight = (int)listPanel.getPreferredSize().getHeight() * 9 / 10;
		scrollBar.setPreferredSize(new Dimension(listWidth, listHeight));
		showList.setFixedCellHeight(listHeight / 20);

		//try to set the color of the selected cell
		//does not work right now
		showList.setSelectionForeground(selectColor);
		//scrollBar.setBackground(listColor);

		//making Jlist transparent
		class TransparentListCellRenderer extends DefaultListCellRenderer {

			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				setForeground(Color.BLACK);
				setOpaque(isSelected);
				return this;
	        }
    	}
    	showList.setCellRenderer(new TransparentListCellRenderer());
        showList.setOpaque(false);
        scrollBar.setOpaque(false);
		scrollBar.getViewport().setOpaque(false);


		//add listener
		// class MusicListListener implements ListSelectionListener{
		// 	public void valueChanged(ListSelectionEvent e){
		// 		preSongIndex = index;
		// 		index = showList.getSelectedIndex();
		// 		//startMusic();
		// 		System.out.println(e.getFirstIndex() + " " + e.getLastIndex() + " " + index);
		// 	}
		// }
		// MusicListListener musicListListener = new MusicListListener();
		// showList.addListSelectionListener(musicListListener);

		class DoubleClickListener extends MouseAdapter{
			public void mouseClicked(MouseEvent evt) {
	        	JList list = (JList)evt.getSource();

    	    	if (evt.getClickCount() == 2) {
            	// Double-click detected
    	    	preSongIndex = index;
            	index = list.locationToIndex(evt.getPoint());
            	System.out.println("selecting index: " + index);
            	startMusic();
        		} 
        		// else if (evt.getClickCount() == 3) {
				// Triple-click detected
        		// int i = list.locationToIndex(evt.getPoint());
        		// System.out.println(i);
       			// }
       		}
		}
		MouseListener doubleClick = new DoubleClickListener();
		showList.addMouseListener(doubleClick);


		scrollBar.setViewportView(showList);
		listPanel.add(scrollBar);
	}

	//initialize the panels, the layout of the interface
	public static void PanelInit(){
		try{
			musicListBackground = ImageIO.read(new File("Background/Blue_flower_bg_vertical.jpg"));
			musicBarBackground = ImageIO.read(new File("Background/Blue_flower_bg_horizontal.jpg"));
			buttonBackground = ImageIO.read(new File("Background/Blue_flower_bg_horizontal_flip.jpg"));
		}catch(IOException e){
			e.printStackTrace();
		}
		
		upperMainPanel = new JPanel();
		lowerMainPanel = new BackgroundPanel(musicBarBackground);	
		listPanel = new BackgroundPanel(musicListBackground);
		upperTempPanel = new JPanel();
		buttonsPanel = new BackgroundPanel(buttonBackground);
		musicBarPanel = new BackgroundPanel(musicBarBackground);
		

		upperMainPanel.setPreferredSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT * 4 / 5));
		lowerMainPanel.setPreferredSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT / 5));
		
		listPanel.setPreferredSize(new Dimension(DEFAULT_WIDTH * 3 / 8, DEFAULT_HEIGHT * 4 / 5));

		musicBarPanel.setPreferredSize(new Dimension(DEFAULT_WIDTH * 3 / 5, DEFAULT_HEIGHT / 5));

		//temp code for clearify upper panel
		upperTempPanel.setBackground(Color.BLACK);
		upperMainPanel.setLayout(new BorderLayout());
		upperMainPanel.add(listPanel, BorderLayout.WEST);
		upperMainPanel.add(upperTempPanel, BorderLayout.CENTER);

		//lowerMainPanel stuff
		musicBarPanel.setBackground(Color.WHITE);
		musicBarPanel.setLayout(null);
		lowerMainPanel.setLayout(new BorderLayout());
		lowerMainPanel.add(musicBarPanel, BorderLayout.CENTER);
		lowerMainPanel.add(buttonsPanel, BorderLayout.WEST);

		frame.add(upperMainPanel, BorderLayout.CENTER);
		frame.add(lowerMainPanel, BorderLayout.SOUTH);
	}

	//initialize the buttons with its listener
	public static void buttonsInit(){

		playPauseButton = new JButton("play/pause");
		playNextButton = new JButton("play next");
		playPreButton = new JButton("play previous");
		stopButton = new JButton("stop");
		
		int buttonsizeX = 100;
		int buttonsizeY = 60;
		playPauseButton.setPreferredSize(new Dimension(buttonsizeX, buttonsizeY));
		playNextButton.setPreferredSize(new Dimension(buttonsizeX, buttonsizeY));
		playPreButton.setPreferredSize(new Dimension(buttonsizeX, buttonsizeY));
		stopButton.setPreferredSize(new Dimension(buttonsizeX, buttonsizeY));

		//play pause button function
		class PlayPauseListener implements ActionListener{
			public void actionPerformed(ActionEvent e){
				if(player == null)
					startMusic();
				else{
					if(player.isPause()){
						player.resume();
					}
					else{
						player.pause();
						Thread.currentThread().yield();
					}
				}
			}
		}
		PlayPauseListener playPauseListener = new PlayPauseListener();
		playPauseButton.addActionListener(playPauseListener);
		
		//play next button function
		class PlayNextListener implements ActionListener{
			public void actionPerformed(ActionEvent e){
				playNextMusic();
			}
		}
		PlayNextListener playNextListener = new PlayNextListener();
		playNextButton.addActionListener(playNextListener);

		//play previous button function
		class PlayPreListener implements ActionListener{
			public void actionPerformed(ActionEvent e){
				playPreMusic();
			}
		}
		PlayPreListener playPreListener = new PlayPreListener();
		playPreButton.addActionListener(playPreListener);

		//stop button function
		class StopListener implements ActionListener{
			public void actionPerformed(ActionEvent e){
				preSongIndex = index;
				stopMusic();
			}
		}
		StopListener stopListener = new StopListener();
		stopButton.addActionListener(stopListener);

		buttonsPanel.setLayout(new BorderLayout());
		buttonsPanel.add(playPreButton, BorderLayout.WEST);
		buttonsPanel.add(playPauseButton, BorderLayout.SOUTH);
		buttonsPanel.add(playNextButton, BorderLayout.EAST);
		buttonsPanel.add(stopButton, BorderLayout.CENTER);
	}

	//create the basic Jframe and load the music into the program
	public static void loading() throws IOException{
		playList = new ArrayList<String>();
		index = 0;
		preSongIndex = 0;
		isPause = false;
		rand = new Random();
		loopingStatus = MusicPlayer.LOOP;

		//create the frame
		frame = new JFrame("MyMP3Player");
		frame.setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
		frame.setLayout(new BorderLayout());

		//load the image of bar buttons
		//3 bar buttons and 2 status for each button
		//index 0,1,2 is normal status
		//index 3,4,5 is active status
		int size = 6;
		barButtonIcons = new Image[size];
		barButtonIcons[0] = ImageIO.read(new File("Icon/replay.png"));
		barButtonIcons[1] = ImageIO.read(new File("Icon/loop.png"));
		barButtonIcons[2] = ImageIO.read(new File("Icon/shuffle.png"));
		barButtonIcons[3] = ImageIO.read(new File("Icon/replay_active.png"));
		barButtonIcons[4] = ImageIO.read(new File("Icon/loop_active.png"));
		barButtonIcons[5] = ImageIO.read(new File("Icon/shuffle_active.png"));

		//create the playlist
		musicFolder = new File("Music");
		for (File fileEntry : musicFolder.listFiles()) {
			if(fileEntry.getName().charAt(0) != '.'){
            	System.out.println(fileEntry.getName());
            	playList.add(fileEntry.getName());
        	}
    	}

    	//create the music name list
    	nameList = new String[playList.size()];
		char dash = '-';
		char dot = '.';
		for(int i = 0; i < playList.size(); i++){
			String fullName = playList.get(i);
			int beginIndex = fullName.indexOf(dash);
			if(beginIndex < 0){
				beginIndex = 0;
			}
			else{
				beginIndex++;
			}
			int endIndex = fullName.lastIndexOf(dot);
			nameList[i] = fullName.substring(beginIndex, endIndex);
		}
		
	}

	//play the next music on the music list
	public static void playNextMusic(){
		preSongIndex = index++;
		if(index >= playList.size()){
				index = 0;
		}
		startMusic();
	}

	//play the previous music on the music list
	public static void playPreMusic(){
		preSongIndex = index--;
		if(index < 0){
				index = playList.size() - 1;
		}
		startMusic();
	}

	//play a random music
	public static void playRandomMusic(){
		rand.setSeed(Clock.systemUTC().millis());
		int curIndex = index;
		while(index == preSongIndex || index == curIndex){
			index = rand.nextInt(playList.size());
		}
		preSongIndex = curIndex;
		startMusic();
	}

	//replay the music
	public static void replayMusic(){
		preSongIndex = index;
		startMusic();
	}

	//start the music
	private static void startMusic(){
		try{
			stopMusic();

			//give now playing mark
			tagPlaced();

			String musicPath = musicFolder.getName() + "/" + playList.get(index);

			//get the duration of the music
			int duration = (int)getDuration(new File(musicPath));

			//play the music
			player = new MusicPlayer(musicPath, duration, loopingStatus);
			player.setTimer(timerDisplay);
			player.setMusicBar(bar);
			curPlay = new Thread(player);
			curPlay.start();
		
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	//stop the music
	public static void stopMusic(){
		try{

			if(player != null){
				player.stop();
				//playMP3.close();
				player = null;
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	//start music from certain position
	//the position as millisecond
	public static void startMusicAtPos(int startPos){

		preSongIndex = index;

		try{
			stopMusic();

			tagPlaced();

			String musicPath = musicFolder.getName() + "/" + playList.get(index);

			//get the duration of the music
			int duration = (int)getDuration(new File(musicPath));
			int startMS = bar.calMS(startPos, duration);
			//System.out.println(startMS + " " + duration);

			player = new MusicPlayer(musicPath, duration, loopingStatus, startMS);
			player.setTimer(timerDisplay);
			player.setMusicBar(bar);
			curPlay = new Thread(player);
			curPlay.start();

		}catch(Exception e){
			e.printStackTrace();
		}
	}

	//place the now playing tag in the list
	public static void tagPlaced(){
		int tagStartIndex = nameList[preSongIndex].lastIndexOf('<') - 1;
		if(tagStartIndex >= 0){
			nameList[preSongIndex] = nameList[preSongIndex].substring(0, tagStartIndex);
		}
		nameList[index] = nameList[index] + " <-playing";
		showList.setListData(nameList);
	}

	//getting the duration of the song
	//return millisecond
	private static long getDuration(File file) throws UnsupportedAudioFileException, IOException {

		long microseconds = 0;

		AudioFileFormat fileFormat = AudioSystem.getAudioFileFormat(file);
		if(fileFormat instanceof TAudioFileFormat) {
			Map<?, ?> properties = ((TAudioFileFormat) fileFormat).properties();
			String key = "duration";
			microseconds = (Long) properties.get(key);
			int mili = (int) (microseconds / 1000);
			int sec = (mili / 1000) % 60;
			int min = (mili / 1000) / 60;
			System.out.println("duration of song = " + min + ":" + sec);
		} else {
			throw new UnsupportedAudioFileException();
		}

		return microseconds / 1000;

	}
}