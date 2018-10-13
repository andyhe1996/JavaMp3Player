import java.lang.Runnable;
import javax.swing.JLabel;
import java.io.*;



public class MusicPlayer implements Runnable{

	public static final int REPLAY = 0; 
	public static final int LOOP = 1;
	public static final int RANDOM = 2;

	private static CustomPlayer player;
	private static Object pauseLock;
	//in milli second
	private static int duration;
	private int startMS;
	private int status;
	private JLabel timer;
	private boolean pause;
	private MusicBar panelMusicBar;
	private float ms_per_frame;

	//for the animation
	private ParticleApp upperPanel;

	public MusicPlayer(String path, int duration, int status, ParticleApp upperPanel){
		try{
			player = new CustomPlayer(new FileInputStream(path));
			ms_per_frame = player.ms_per_frame();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		this.duration = duration;
		pauseLock = new Object();
		pause = false;
		startMS = 0;
		this.status = status;
		this.upperPanel = upperPanel;
	}

	public MusicPlayer(String path, int duration, int status, int startMS, ParticleApp upperPanel){
		this(path, duration, status, upperPanel);
		this.startMS = startMS;

		boolean ret = true;
		try{
			if(ms_per_frame >= 0){
				int offset = (int)(startMS / ms_per_frame) - 1;
				System.out.println("offset:" + offset + " ms_per_frame:" + ms_per_frame);
				System.out.println("curMS:" + player.getPosition() + " startMS:" + startMS);
				while (offset-- > 0 && ret)
					ret = player.skipFrame();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public void run(){
		try{
			int animationDur = (int)(ms_per_frame * 100.0);
			upperPanel.setFrame(animationDur);
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
				System.out.println("song complete");
				if(status == RANDOM)
					MP3Player.playRandomMusic();
				else if(status == REPLAY)
					MP3Player.replayMusic();
				else
					MP3Player.playNextMusic();
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		upperPanel.frameStop();
		System.out.println("previous session finish");
	}

	public void pause(){
		pause = true;
		upperPanel.framePause();
	}

	public void resume(){
		pause = false;
		upperPanel.frameResume();
		synchronized(pauseLock){
			pauseLock.notify();
		}
	}

	public void stop(){
		player.close();
		startMS = 0;
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

	public void setMusicBar(MusicBar mB){
		panelMusicBar = mB;
	}

	public void setStatus(int status){
		this.status = status;
	}

	//int millisecond
	public void UpdateCurrentTime(){
		if(timer != null){
			int millisec = player.getPosition() + startMS;
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

		if(panelMusicBar != null){
			panelMusicBar.updateProgress(player.getPosition() + startMS, duration);
		}
	}
}