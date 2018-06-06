import java.lang.Runnable;
import javax.swing.JLabel;
import java.io.*;

import javazoom.jl.player.*;



public class MusicPlayer implements Runnable{

	private static Player player;
	//private static String musicPath;
	private static Object pauseLock;
	//in milli second
	private static int duration;
	private JLabel timer;
	private boolean pause;

	public MusicPlayer(String path, int duration){
		//musicPath = path;
		try{
			player = new Player(new FileInputStream(path));
		}
		catch(Exception e){
			e.printStackTrace();
		}
		this.duration = duration;
		pauseLock = new Object();
		pause = false;
	}

	public void run(){
		try{
			while(player.play(1)){
				if(pause){
					synchronized(pauseLock){
					pauseLock.wait();
					}
				}
				else{
					UpdateCurrentTime();
				}
			}
			if(player.isComplete()){
				System.out.println("complete");
				MP3Player.playNextMusic();
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		System.out.println("finish");
	}

	public void pause(){
		pause = true;
	}

	public void resume(){
		pause = false;
		synchronized(pauseLock){
			pauseLock.notify();
		}
	}

	public void stop(){
		player.close();
		UpdateCurrentTime();
		synchronized(pauseLock){
			pauseLock.notify();
		}
	}

	public boolean isPause(){
		return pause;
	}
	
	public void setTimer(JLabel timer){
		this.timer = timer;
	}

	//int millisecond
	public void UpdateCurrentTime(){
		if(timer != null){
			int millisec = player.getPosition();
			int sec = millisec / 1000;
			int min = sec / 60;
			sec = sec % 60;

			String curTime = "";
			if(min < 10){
				curTime += "0";
			}
			curTime += min + ":";
			if(sec < 10){
				curTime += "0";
			}
			curTime += sec + "  ";
			timer.setText(curTime);
		}
	}
}