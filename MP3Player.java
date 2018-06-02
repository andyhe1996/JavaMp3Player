import javax.swing.*;
import java.io.*;
import java.awt.event.*;
import java.awt.BorderLayout;
import java.awt.Dimension;
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

	private static JFrame frame;

	private static JPanel buttonsPanel;
	private static JButton playPauseButton;
	private static JButton playNextButton;
	private static JButton stopButton;

	public static void main(String[] args){

		loading();

		buttonsPanelInit();

		class PlayPauseListener implements ActionListener{
			public void actionPerformed(ActionEvent e){
				if(playMP3 == null)
					playNextMusic();
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
		
		class PlayNextListener implements ActionListener{
			public void actionPerformed(ActionEvent e){
				playNextMusic();
			}
		}
		PlayNextListener playNextListener = new PlayNextListener();
		playNextButton.addActionListener(playNextListener);

		class StopListener implements ActionListener{
			public void actionPerformed(ActionEvent e){
				stopMusic();
			}
		}
		StopListener stopListener = new StopListener();
		stopButton.addActionListener(stopListener);

		buttonsPanel.add(playPauseButton);
		buttonsPanel.add(playNextButton);
		buttonsPanel.add(stopButton);

		frame.setLayout(new BorderLayout());
		frame.add(buttonsPanel, BorderLayout.NORTH);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);


	}

	public static void buttonsPanelInit(){
		buttonsPanel = new JPanel();
		playPauseButton = new JButton("play/pause");
		playNextButton = new JButton("play next");
		stopButton = new JButton("stop");
		buttonsPanel.setSize(DEFAULT_WIDTH - 200, DEFAULT_HEIGHT);

		int buttonsizeX = 100;
		int buttonsizeY = 50;
		playPauseButton.setPreferredSize(new Dimension(buttonsizeX, buttonsizeY));
		playNextButton.setPreferredSize(new Dimension(buttonsizeX, buttonsizeY));
		stopButton.setPreferredSize(new Dimension(buttonsizeX, buttonsizeY));

	}

	public static void loading(){
		playList = new ArrayList<String>();
		index = 0;
		isPause = false;

		frame = new JFrame("MyMP3Player");
		frame.setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);

		musicFolder = new File("Music");
		for (File fileEntry : musicFolder.listFiles()) {
			if(fileEntry.getName().charAt(0) != '.'){
            	System.out.println(fileEntry.getName());
            	playList.add(fileEntry.getName());
        	}
    	}
		
	}

	public static void playNextMusic(){
		try{
			stopMusic();

			String musicPath = musicFolder.getName() + "/" + playList.get(index++);
			fis = new FileInputStream(musicPath);
			playMP3 = new Player(fis);
			player = new MusicPlayer(playMP3);
			curPlay = new Thread(player);
			curPlay.start();

			if(index >= playList.size()){
				index = 0;
			}
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