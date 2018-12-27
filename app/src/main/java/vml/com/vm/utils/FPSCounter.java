package vml.com.vm.utils;

import android.util.Log;

/**
 * FPSCounter is an util to compute the frames per second
 * @author Roger Blanco i Ribera
 *
 */
public class FPSCounter 
{
	long startTime=0;
	int frames = 0;
	/**
	 * initializes the counter 
	 */
	public FPSCounter()
	{
		startTime = System.nanoTime();
		frames = 0;
	}
	/**
	 * logs the current frames per second in the logCat
	 * @param TAG logCat tag
	 */
	public  void logFrame(String TAG) 
	{
	    frames++;
	    if(System.nanoTime() - startTime >= 3e9) //for 3
	    {
	        Log.d(TAG, "fps: " + (float)frames/3.0f);
	        frames = 0;
	        startTime = System.nanoTime();
	    }
	}
}