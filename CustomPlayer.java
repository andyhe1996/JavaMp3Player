import java.io.InputStream;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.decoder.SampleBuffer;
import javazoom.jl.player.*;
	
 //This is my own version of Player
 //extended from the JLayer Player

// REVIEW: the audio device should not be opened until the
// first MPEG audio frame has been decoded. 
public class CustomPlayer{	  	
	// /**
	//  * The current frame number. 
	//  */
	private int frame = 0;
	
	// /**
	//  * The MPEG audio bitstream. 
	//  */
	// javac blank final bug. 
	/*final*/ private Bitstream bitstream;
	
	// /**
	//  * The MPEG audio decoder. 
	//  */
	/*final*/ private Decoder decoder; 
	
	// /**
	//  * The AudioDevice the audio samples are written to. 
	//  */
	private AudioDevice	audio;
	
	/**
	 * Has the player been closed?
	 */
	private boolean	closed = false;
	
	// /**
	//  * Has the player played back all frames from the stream?
	//  */
	private boolean	complete = false;

	private int	lastPosition = 0;

	private short[] frameContent;
	
	// /**
	//  * Creates a new <code>Player</code> instance. 
	//  */
	public CustomPlayer(InputStream stream) throws JavaLayerException
	{
		this(stream, null);	
	}
	
	public CustomPlayer(InputStream stream, AudioDevice device) throws JavaLayerException
	{
		bitstream = new Bitstream(stream);		
		decoder = new Decoder();
				
		if (device!=null)
		{		
			audio = device;
		}
		else
		{			
			FactoryRegistry r = FactoryRegistry.systemRegistry();
			audio = r.createAudioDevice();
		}
		audio.open(decoder);
	}
	
	public void play() throws JavaLayerException
	{
		play(Integer.MAX_VALUE);
	}
	
	// /**
	//  * Plays a number of MPEG audio frames. 
	//  * 
	//  * @param frames	The number of frames to play. 
	//  * @return	true if the last frame was played, or false if there are
	//  *			more frames. 
	//  */
	public boolean play(int frames) throws JavaLayerException
	{
		boolean ret = true;
			
		while (frames-- > 0 && ret)
		{
			ret = decodeFrame();			
		}
		
		if (!ret)
		{
			// last frame, ensure all data flushed to the audio device. 
			AudioDevice out = audio;
			if (out!=null)
			{				
				out.flush();
				synchronized (this)
				{
					complete = (!closed);
					close();
				}				
			}
		}
		return ret;
	}
		
	// /**
	//  * Cloases this player. Any audio currently playing is stopped
	//  * immediately. 
	//  */
	public synchronized void close()
	{		
		AudioDevice out = audio;
		if (out!=null)
		{ 
			closed = true;
			audio = null;	
			// this may fail, so ensure object state is set up before
			// calling this method. 
			out.close();
			lastPosition = out.getPosition();
			try
			{
				bitstream.close();
			}
			catch (BitstreamException ex)
			{
			}
		}
	}
	
	// /**
	//  * Returns the completed status of this player.
	//  * 
	//  * @return	true if all available MPEG audio frames have been
	//  *			decoded, or false otherwise. 
	//  */
	public synchronized boolean isComplete()
	{
		return complete;	
	}
				
	// /**
	//  * Retrieves the position in milliseconds of the current audio
	//  * sample being played. This method delegates to the <code>
	//  * AudioDevice</code> that is used by this player to sound
	//  * the decoded audio samples. 
	//  */
	public int getPosition()
	{
		int position = lastPosition;
		
		AudioDevice out = audio;		
		if (out!=null)
		{
			position = out.getPosition();	
		}
		return position;
	}		
	
	// /**
	//  * Decodes a single frame.
	//  * 
	//  * @return true if there are no more frames to decode, false otherwise.
	//  */
	protected boolean decodeFrame() throws JavaLayerException
	{		
		try
		{
			AudioDevice out = audio;
			if (out==null)
				return false;

			Header h = bitstream.readFrame();	
			
			if (h==null)
				return false;
				
			// sample buffer set when decoder constructed
			SampleBuffer output = (SampleBuffer)decoder.decodeFrame(h, bitstream);

			frameContent = output.getBuffer();
																																					
			synchronized (this)
			{
				out = audio;
				if (out!=null)
				{					
					out.write(output.getBuffer(), 0, output.getBufferLength());
				}				
			}
																			
			bitstream.closeFrame();
		}		
		catch (RuntimeException ex)
		{
			throw new JavaLayerException("Exception decoding audio frame", ex);
		}
/*
		catch (IOException ex)
		{
			System.out.println("exception decoding audio frame: "+ex);
			return false;	
		}
		catch (BitstreamException bitex)
		{
			System.out.println("exception decoding audio frame: "+bitex);
			return false;	
		}
		catch (DecoderException decex)
		{
			System.out.println("exception decoding audio frame: "+decex);
			return false;				
		}
*/		
		return true;
	}

	public short[] getContent(){
		return frameContent;
	}

	public boolean play(int start, int frame) throws JavaLayerException{
		boolean ret = true;
		int offset = start;
		while (offset-- > 0 && ret) ret = skipFrame();
		return play(frame);
	}

	
	// skips over a single frame
	// return false	if there are no more frames to decode, true otherwise.
	public boolean skipFrame() throws JavaLayerException
	{
		Header h = bitstream.readFrame();
		if (h == null) return false;
		bitstream.closeFrame();
		return true;
	}

	//get the ms per frame
	public float ms_per_frame() throws JavaLayerException{
		Header h = bitstream.readFrame();
		if(h == null)
			return (float) -1.0;
		float ms = h.ms_per_frame();
		bitstream.closeFrame();
		return ms;
	}
}