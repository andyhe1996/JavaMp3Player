import javax.swing.*;
import java.io.*;
import java.awt.event.*;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Color;
import java.util.*;
import javazoom.jl.player.*;

public class MP3Player{

	private static final int DEFAULT_WIDTH = 800;
	private static final int DEFAULT_HEIGHT = 600;

	private static Player playMP3;
	private static FileInputStream fis;
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

	private static JList<String> showList;

	public static void main(String[] args){

		//load initial stuff
		loading();

		//set up the panels
		PanelInit();

		//set up the play buttons
		buttonsInit();

		//set up the music list
		listInit();
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);


	}

	public static void listInit(){
		
		showList = new JList<String>(nameList);

		listPanel.add(showList);
	}

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
		lowerMainPanel.setLayout(new BorderLayout());
		lowerMainPanel.add(lowerTempPanel, BorderLayout.CENTER);
		lowerMainPanel.add(buttonsPanel, BorderLayout.WEST);

		frame.add(upperMainPanel, BorderLayout.CENTER);
		frame.add(lowerMainPanel, BorderLayout.SOUTH);
	}

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
				if(playMP3 == null)
					startMusic();
				else{
					if(player.isPause()){
						player.resume();
					}
					else{
						player.pause();
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

	public static void playNextMusic(){
		preSongIndex = index++;
		if(index >= playList.size()){
				index = 0;
		}
		startMusic();
	}

	public static void playPreMusic(){
		preSongIndex = index--;
		if(index < 0){
				index = playList.size() - 1;
		}
		startMusic();
	}

	private static void startMusic(){
		try{
			stopMusic();

			//give now playing mark
			int nowPlayingIndex = nameList[preSongIndex].lastIndexOf('<') - 1;
			if(nowPlayingIndex >= 0){
				nameList[preSongIndex] = nameList[preSongIndex].substring(0, nowPlayingIndex);
			}
			nameList[index] = nameList[index] + " <-playing";
			showList.setListData(nameList);

			String musicPath = musicFolder.getName() + "/" + playList.get(index);
			fis = new FileInputStream(musicPath);
			playMP3 = new Player(fis);
			player = new MusicPlayer(playMP3);
			curPlay = new Thread(player);
			curPlay.start();

			
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	public static void stopMusic(){
		try{

			if(playMP3 != null){
				player.stop();
				playMP3.close();
				playMP3 = null;
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
}