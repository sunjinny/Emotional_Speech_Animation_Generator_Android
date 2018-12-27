package vml.com.vm.utils;

import java.util.Comparator;

/**
 * Key frame comparator class for rearranging key frames according to its key time
 * @author Roger Blanco i Ribera
 *
 */
public class KeyFrameComparator implements Comparator<KeyFrame> 
{
	/**
	 * 	Compares to given key frames based on their timing
	 * @param key1 input keyframe
	 * @param key2 input keyframe
	 */
    public int compare(KeyFrame key1, KeyFrame key2)
    {
        return key1.time - key2.time;
    }
}
