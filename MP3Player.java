import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.sound.sampled.*;
import java.io.*;
import java.awt.event.*;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Color;
import java.util.*;
//import javazoom.spi.mpeg.sampled.file.*;
import org.tritonus.share.sampled.file.*;
import org.tritonus.share.sampled.TAudioFormat;
//import javazoom.jl.player.*;
//import javazoom.jl.player.advanced.*;

public class MP3Player{

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

	private static JFrame frame;
	
	private static JPanel upperMainPanel;
	private static JPanel lowerMainPanel;
	private static JPanel listPanel;
	private static JPanel upperTempPanel;
	private static JPanel buttonsPanel;
	private static JPanel lowerTempPanel;

	private static JButton playPauseButton;
	private static JButton playNextButton;
	private static JButton playPreButton;
	private static JButton stopButton;

	private static JScrollPane scrollBar;
	private static JList<String> showList;

	private static MusicBar bar;
	private static JLabel timerDisplay;

	public static void main(String[] args){

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
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);


	}

	//initialize the music progress bar, with timers
	public static void musicBarInit(){

		double barWidth = lowerTempPanel.getPreferredSize().getWidth() - 50.0;
		double barHeight = lowerTempPanel.getPreferredSize().getHeight();

		//MusicBar(width, height, thickness)
		bar = new MusicBar(barWidth, barHeight, 2);

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

		timerDisplay = new JLabel("00:00  ");


		lowerTempPanel.add(timerDisplay, BorderLayout.EAST);
		lowerTempPanel.add(bar, BorderLayout.CENTER);
	}

	//initialize the music list
	public static void listInit(){
		
		scrollBar = new JScrollPane();
		showList = new JList<String>(nameList);

		int listWidth = (int)listPanel.getPreferredSize().getWidth() * 9 / 10;
		int listHeight = (int)listPanel.getPreferredSize().getHeight() * 9 / 10;
		scrollBar.setPreferredSize(new Dimension(listWidth, listHeight));
		showList.setFixedCellHeight(listHeight / 20);

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
		upperMainPanel = new JPanel();
		lowerMainPanel = new JPanel();
		listPanel = new JPanel();
		upperTempPanel = new JPanel();
		buttonsPanel = new JPanel();
		lowerTempPanel = new JPanel();

		upperMainPanel.setPreferredSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT * 4 / 5));
		lowerMainPanel.setPreferredSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT / 5));
		
		listPanel.setPreferredSize(new Dimension(DEFAULT_WIDTH * 3 / 8, DEFAULT_HEIGHT * 4 / 5));

		lowerTempPanel.setPreferredSize(new Dimension(DEFAULT_WIDTH * 3 / 5, DEFAULT_HEIGHT / 5));

		//temp code for clearify upper panel
		upperTempPanel.setBackground(Color.BLACK);
		upperMainPanel.setLayout(new BorderLayout());
		upperMainPanel.add(listPanel, BorderLayout.WEST);
		upperMainPanel.add(upperTempPanel, BorderLayout.CENTER);

		//lowerMainPanel stuff
		lowerTempPanel.setBackground(Color.WHITE);
		lowerTempPanel.setLayout(new BorderLayout());
		lowerMainPanel.setLayout(new BorderLayout());
		lowerMainPanel.add(lowerTempPanel, BorderLayout.CENTER);
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
	public static void loading(){
		playList = new ArrayList<String>();
		index = 0;
		preSongIndex = 0;
		isPause = false;

		frame = new JFrame("MyMP3Player");
		frame.setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
		frame.setLayout(new BorderLayout());

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
			player = new MusicPlayer(musicPath, duration);
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

		try{
			stopMusic();

			tagPlaced();

			String musicPath = musicFolder.getName() + "/" + playList.get(index);

			//get the duration of the music
			int duration = (int)getDuration(new File(musicPath));
			int startMS = bar.calMS(startPos, duration);
			//System.out.println(startMS + " " + duration);

			player = new MusicPlayer(musicPath, duration, startMS);
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
			System.out.println("time = " + min + ":" + sec);
		} else {
			throw new UnsupportedAudioFileException();
		}

		return microseconds / 1000;

	}
}