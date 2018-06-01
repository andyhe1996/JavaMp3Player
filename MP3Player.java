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

	private static File musicFolder;
	private static ArrayList<String> playList;
	private static int index;

	private static JFrame frame;

	public static void main(String[] args){

		loading();
		JPanel buttonsPanel = new JPanel();
		JButton playButton = new JButton("play");
		JButton stopButton = new JButton("stop");
		frame.setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
		buttonsPanel.setSize(DEFAULT_WIDTH - 200, DEFAULT_HEIGHT);

		int buttonsizeX = 100;
		int buttonsizeY = 50;
		playButton.setPreferredSize(new Dimension(buttonsizeX, buttonsizeY));
		stopButton.setPreferredSize(new Dimension(buttonsizeX, buttonsizeY));

		class PlayListener implements ActionListener{
			public void actionPerformed(ActionEvent e){
				playMusic();
			}
		}
		PlayListener playListener = new PlayListener();
		playButton.addActionListener(playListener);

		class StopListener implements ActionListener{
			public void actionPerformed(ActionEvent e){
				stopMusic();
			}
		}
		StopListener stopListener = new StopListener();
		stopButton.addActionListener(stopListener);

		buttonsPanel.add(playButton);
		buttonsPanel.add(stopButton);

		frame.setLayout(new BorderLayout());
		frame.add(buttonsPanel, BorderLayout.NORTH);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);


	}

	public static void loading(){
		playList = new ArrayList<String>();
		index = 0;
		frame = new JFrame("MyMP3Player");

		musicFolder = new File("Music");
		for (File fileEntry : musicFolder.listFiles()) {
			if(fileEntry.getName().charAt(0) != '.'){
            	System.out.println(fileEntry.getName());
            	playList.add(fileEntry.getName());
        	}
    	}
		
	}

	public static void playMusic(){
		try{
			stopMusic();

			String musicPath = musicFolder.getName() + "/" + playList.get(index++);
			fis = new FileInputStream(musicPath);
			playMP3 = new Player(fis);
			MusicPlayer player = new MusicPlayer(playMP3);
			new Thread(player).start();

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
				playMP3.close();
				playMP3 = null;
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
}