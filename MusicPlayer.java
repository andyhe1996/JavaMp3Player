import java.lang.Runnable;

import javazoom.jl.player.*;


public class MusicPlayer implements Runnable{

	private static Player player;
	private static Object pauseLock;
	private boolean pause;
	private boolean playing;

	public MusicPlayer(Player player){
		this.player = player;
		pause = false;
		playing = true;

	}

	public void run(){
		try{
			while(playing && !player.isComplete()){
				if(!pause)
					player.play(1);
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
	}

	public void stop(){
		playing = false;
	}

	public boolean isPause(){
		return pause;
	}
	
}