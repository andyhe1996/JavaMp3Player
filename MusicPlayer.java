import java.lang.Runnable;

import javazoom.jl.player.*;


public class MusicPlayer implements Runnable{

	private static Player player;
	public MusicPlayer(Player player){
		this.player = player;

	}

	public void run(){
		try{
			player.play();
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
	
}