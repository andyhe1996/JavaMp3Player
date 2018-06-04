import java.lang.Runnable;

import javazoom.jl.player.*;


public class MusicPlayer implements Runnable{

	private static Player player;
	private static Object pauseLock;
	private boolean pause;

	public MusicPlayer(Player player){
		this.player = player;
		pauseLock = new Object();
		pause = false;
		playing = true;

	}

	public void run(){
		try{
			while(player.play(1) && !player.isComplete()){
				if(pause){
					synchronized(pauseLock){
					pauseLock.wait();
					}
				}
					//player.play(1);
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

	}

	public boolean isPause(){
		return pause;
	}
	
}