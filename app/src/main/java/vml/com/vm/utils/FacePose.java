package vml.com.vm.utils;

/** class FacePose : 
* Data container for facial expressions
* it contains the weights associated with the face blendshapes and 
* the mouth blendshapes
* 
* @author Roger Blanco i Ribera
* 
*/

public class FacePose
{
	/** FaceWeights keeps the weights of the blendshapes related to the face */
	public float[] faceWeights;
	/** mouthWeights keeps the weights of the blendshapes related to the mouth */
	public float[] mouthWeights;
	
	/**Class constructor 
	 * @param faceW 		Input face blendshapes weights
	 * @param mouthW		Input mouth blendshapes weights
	 * 
	 * */	
	public FacePose(float[] faceW, float[] mouthW)
	{
		faceWeights= faceW.clone();
		mouthWeights= mouthW.clone();
	}

	public FacePose(float[] faceW)
	{
		faceWeights= faceW.clone();
	}
	
}