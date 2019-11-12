package vml.com.vm.utils;


import vml.com.vm.utils.FacePose;

/*
 * KeyFrame data Structure
 */
public class KeyFrame
{
	/**keyframe time in ms*/
	public int time;
	/** Face pose for the key
	 * @see FacePose
	 */
	public FacePose pose;

	public float noddingValue;
}

