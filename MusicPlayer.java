import java.lang.Runnable;
import java.io.*;

import javazoom.jl.player.*;



public class MusicPlayer implements Runnable{

	private static Player player;
	//private static String musicPath;
	private static Object pauseLock;
	private boolean pause;

	public MusicPlayer(String path){
		//musicPath = path;
		try{
			player = new Player(new FileInputStream(path));
			pauseLock = new Object();
			pause = false;
		}
		catch(Exception e){
			e.printStackTrace();
		}

	}

	public void run(){
		try{
			while(player.play(1)){
				if(pause){
					synchronized(pauseLock){
					pauseLock.wait();
					}
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
		synchronized(pauseLock){
			pauseLock.notify();
		}
	}

	public boolean isPause(){
		return pause;
	}
	
}