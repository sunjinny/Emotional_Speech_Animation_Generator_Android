package vml.com.vm.avatar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import vml.com.animation.R;
import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Debug;
import android.os.SystemClock;
import android.util.Log;

import vml.com.vm.blend.VMBOLoader;
import vml.com.vm.blend.VMBOModel;
import vml.com.vm.utils.*;

/**
 * Class VMAvatar is the main class for managing, animating and rendering a virtual avatar.
 * <p>
 * The Avatar is composed of several parts arranged in hierarchical order:
 * <ul>
 * <li> Extra(Scene): Objects in the scene only subject to global transforms
 * <li> Head: Objects attached to the head transformations (face, mouth, hair, hats...)
 * <li> Eyes: left and right eyes
 * </ul>
 * <p>
 * The core parts of the avatar are the Face, VMBOModel (VML Blendshape Object Model)
 * <p>
 * Face models are composed by a neutral face and a set of Blendshapes which control its deformation.
 * <p>
 * A FacePose is a set of blendshape weights creating a facial expression.
 * <p>
 * The VMAvatar provides also with procedural idle animation consisting in randomized blinking.
 *
 * 	 
 * @author Roger Blanco i Ribera, Sunjin Jung
 *
 *@see FacePose
 *
 */
public class VMAvatar
{
	Context mContext;
	private static String TAG="VMAvatar";

	/** name of the Avatar. "Nobody" by default*/	
	public String name="nobody"; 
	/** Head object */
	public AvatarHead mHead;

	/** List of emotions
	 *  @see FacePose */
	private Map<String,FacePose> emotions   = new LinkedHashMap <String,FacePose>();
	/** List of visemes
	 *  @see FacePose */
	private Map<String,FacePose> visemes    = new LinkedHashMap <String,FacePose>();
	/** List of keyframed animations 
	 * @see KeyFrame 
	 * @see KeyFramedAnimation */
	private Map<String,List<KeyFrame> > animations    =new LinkedHashMap <String,List<KeyFrame> >();

	/** index of blendshapes affecting mouth region*/
	public int[] mouthRegionBlends;

	/**	index of blendshape for left eye blink*/	
	private int blinkLID;
	/**	index of blendshape for right eye blink*/
	private int blinkRID;
	
	/**list of models attached to the scene
	 *  @see VMBOModel*/
	private List<VMBOModel> extraModels	= new ArrayList<VMBOModel>();
	/** list of materials of the scene
	 *  @see VMMaterial	 */
	private List<VMMaterial> extraMaterials= new ArrayList<VMMaterial>();
	/** list of face/mouth blendshape connections
	 *  @see #addMouthLink(int, int)*/
	private Map<Integer,Integer> mouthLinks = new LinkedHashMap <Integer,Integer>();
	/** animation manager.
	 *  @see Animator*/
	private Animator animator;
	
	//Transforms	///////////////////////////////////////////////////////
	/**Scene Model View Matrix */
	private float[] mmatModelView = 	new float[16];
	/**Scene Projection Matrix */
	private float[] mmatViewProjection = new float[16];
	/**Inverse Projection Matrix */
	private float[] mmatViewProjection_inv = new float[16];
	/** Transposed inverse ViewProjection Matrix*/
	private float[] mmatViewProjection_inv_t = new float[16];
	/** Light position vector*/
	//private float[] mfvLightPosition = {-0.5f,0,-1};
	//private float[] mfvLightPosition = {-0.15f,-0.15f,1.2f};
    private float[] mfvLightPosition = {
            100.0f, 100.0f, -150.0f,
            -100.0f, -30.0f, -50.0f
    };
    /**Camera position vector*/
	//private float[] mfvEyePosition = {0,0,-5};
	private float[] mfvEyePosition = {0,0,-50};

	//Shader variables	///////////////////////////////////////////////////
	/** compiled shader program handle*/
	private int mProgram;	
	/*vertex array handle*/
	private int mrm_VertexHandle;
	/*Normal array handle*/
	private int mrm_NormalHandle;
	/*UV coordinates array handle*/
	private int mrm_TexCoord0Handle;	
	private int miTexturedHandle;
	private int mmatViewProjectionHandle;
	private int mmatViewProjectionInverseTransposeHandle;
	private int mfvLightPositionHandle;    
	private int mfvEyePositionHandle;
	private int mfvEyeRotationHandle;
	private int mfvAmbientHandle;
	private int mfvDiffuseHandle;
	private int mfvSpecularHandle;
	private int mfSpecularPowerHandle;
	private int mfTestTimeHandle;
    private int mfvEyeTranslationHandle;
    private int miIndexHandle;
	private int mfHeadNoddingAngleHandle;
	///////////////////////////////////////////////////////////////////////
	
    /**
	 *
	 * @param context
	 */
	public VMAvatar(Context context, 	String faceBobFile,  String faceBobMatFile,
										String teethBobFile, String teethMatFile,
										String tongueBobFile, String tongueMatFile,
										String eyeBobFile,	 String eyeMatFile)
	{	
		mContext=context;
		blinkLID=0; blinkRID=0;
		 try 
		 {
			 mHead= new AvatarHead( faceBobFile, faceBobMatFile, teethBobFile, teethMatFile, tongueBobFile, tongueMatFile, eyeBobFile, eyeMatFile);
			 if((mHead.faceModel.nBS!=mHead.teethModel.nBS)||(mHead.faceModel.nBS!=mHead.tongueModel.nBS))
			     Log.e("error","Different blendshape number: Face, Teeth, Tongue !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
             for(int idx=0; idx < mHead.faceModel.nBS; idx++)
                addMouthLink(idx, idx);
			 animator=new Animator();			 
			 animator.start();
		}
		 catch (IOException e) 
		{		
			e.printStackTrace();
		}
	}

	/**
	 *
	 * @param context
	 * Not considering mouth model
	 */
	public VMAvatar(Context context, String faceBobFile,  String faceBobMatFile, String eyeBobFile,	 String eyeMatFile)
	{
		mContext=context;
		blinkLID=0; blinkRID=0;
		try
		{
			mHead= new AvatarHead(faceBobFile, faceBobMatFile,  eyeBobFile,	 eyeMatFile);
			animator=new Animator();
			animator.start();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Initiate the setup for using RenderScript.
	 * It must be called before any call to Render(). 
	 * Typically it can be called in the Renderer constructor of your activity
	 */
	public void initRenderScript()
	{
		mHead.faceModel.initRenderScript(mContext);
		mHead.teethModel.initRenderScript(mContext);
		mHead.tongueModel.initRenderScript(mContext);
	}
	
	/**
	 * get list of animations
	 */
	public String[] getAnimationList()
	{
	   String[] keys = new String[animations.size()];                            	   
 	   animations.keySet().toArray(keys); 	    
		return keys;		
	}
	
	/**
	 * get list of emotions
	 */
	public String[] getEmotionList()
	{
	   String[] keys = new String[emotions.size()];                            	   
 	   emotions.keySet().toArray(keys); 	    
		return keys;		
	}

	/**
	 *get list of visemes
	 */
	public String[] getVisemeList()
	{
	   String[] keys = new String[visemes.size()];                            	   
 	   visemes.keySet().toArray(keys); 	    
		return keys;		
	}
	
	/**
	 * Returns the VMBOModel of the face
	 * @return faceModel VMBOModel 
	 */	
	public VMBOModel getFaceModel()
	{
		return mHead.getFaceModel();
	}
	
	/**
	 * Returns the VMBOModel of the mouth
	 * @return mouthModel VMBOModel 
	 */	
	public VMBOModel getTeethModel()
	{
		return mHead.getTeethModel();
	}
	public VMBOModel getTongueModel()
	{
		return mHead.getTongueModel();
	}
	
	/**
	 * Clears any existing animation from the animation engine
	 * ie. removes all existing keyframes
	 */
	public void clearAnimation(){	animator.animEngine.clearKeys();	}
	
	/**
	 * Adds a keyFrame to the animation engine of an existing emotion in VMAvatar.emotions list) at the specified time
	 * @param keyTime  time (ms) 
	 * @param emotionKey name of the emotion
	 */
	public void setEmotionKeyFrame(int keyTime, String emotionKey){		animator.animEngine.addKey(keyTime, emotions.get(emotionKey));	}
	
	/**
	 * Adds a key frame to the animation engine from a FacePose
	 * @param keyTime
	 * @param keyPose
	 */
	
	public void setKeyFrame(int keyTime, FacePose keyPose)
	{
		animator.animEngine.addKey(keyTime, keyPose);
	}
	
	/**
	 * Sets up the animation engine to the chosen animation.
	 * ie. sets all the key frames for the animation choice if it exists in VMAvatar.animations
	 * @param AnimChoice name of the animation to be played 
	 */
	public void setAnimation(String AnimChoice)
	{
		//remove any existing animation
		clearAnimation();
		if (animations.containsKey(AnimChoice))			
			animator.animEngine.setKeys(animations.get(AnimChoice));
	}
	
	/**
	 * Starts the animation engine to play the current setup animation
	 */
	public void startAnimation()
	{		
		animator.animEngine.start();
	}

	/**
	 * Updates the current audio timing in the animation engine
	 */
	public void updateAudioTiming(int time)
	{
		animator.animEngine.setAudioTiming(time);
	}

    /**
     * Disable, enable the blinking motion
     */
    public void doBlinking(boolean doBlink)
    {
        animator.doBlinking(doBlink);
    }

    /**
	 * Stops the animation engine to play the current setup animation
	 */
	public void stopAnimation()
	{		
		animator.animEngine.stop();
	}
	
	/**
	 * pauses or resumes the current animation
	 */
	public void tooglePauseAnimation()
	{		
		animator.animEngine.togglePause();
	}
	
	/**
	 * Adds an animation to the existing animation list VMAvatar.animations.
	 * 
	 * @param animName name of the animation
	 * @param keyList	: list of key frames
	 * @see KeyFrame
	 */
	public void addAnimation(String animName,List<KeyFrame> keyList)
	{
		animations.put(animName, keyList);
		return;
	}

	private float GlobalHeadNoddingValue;
	public void headNod(float inputHeadNoddingValue)
	{
        GlobalHeadNoddingValue = inputHeadNoddingValue;
	}

	
	/**
	 * enables/disable the randomized blinking 
	 * @param enable enable if true, disable if false
	 */
	public void enableBlinking(boolean enable)
	{
		animator.doBlinking(enable);
	}

	/**
	 * enables/disables the procedural headMotion
	 * @param enable enable if true
	 */
	public void enableHeadMotion(boolean enable)
	{
		animator.doHeadMotion(enable);
	}
	
	/**
	 * sets the range in degrees for the procedural head motion. 
	 * A value of 5 means that it will generate motions from -5 degrees to 5 degrees in the chosen axis
	 * 
	 * @param pitch range of pitch motion in degrees
	 * @param yaw range of yaw motion in degrees
	 * @param roll range of roll motion in degrees
	 */
	public void setHeadMotionRange(float pitch, float yaw, float roll)
	{
		animator.setHeadMotionRange(pitch, yaw, roll);	
	}
	
	/**
	 * sets the range in degrees for the procedural head motion. 
	 * A value of 5 means that it will generate motions from -5 degrees to 5 degrees in the chosen axis
	 * 
	 * @param pitch intensity of pitch motion
	 * @param yaw intensity of yaw motion
	 * @param roll intensity of roll motion
	 */
	public void setHeadMotionSpeed(float pitch, float yaw, float roll)
	{
		animator.setHeadMotionSpeed(pitch, yaw, roll);	
	}

	/**
	 * Sets the position of each eye with respect to the head coordinate frame.
	 * Mainly for loading/ setup purposes
	 * @param eyeTransL: float[] 3d vector of left eye translation x,y,z 
	 * @param eyeTransR: float[] 3d vector of left eye translation x,y,z
	 */
	public void setEyePos(float[] eyeTransL, float[] eyeTransR )
	{
		mHead.setEyeTranslation(eyeTransL, eyeTransR);
	}
	
	/**
	 * Sets the viewing direction of the eyes.
	 * front is 0,0,0
	 * 
	 * @param eyeRot float[] 3d roation vector rx,ry,rz
	 */
	public void setEyeRotation(float[] eyeRot)
	{
		mHead.setEyeRotation(eyeRot);
	}
	
	/**
	 * Sets the orientation direction of the head
	 * front is 0,0,0
	 * 
	 * @param headRot float[] 3d roation vector rx,ry,rz
	 */	
	public void setHeadRotation(float[] headRot)
	{
		mHead.setRotation(headRot);
	}

	/**
	 * Modifies the current weights of the avatar blendshapes to the viseme Expression.
	 * The emotion has to exist within the VMAvatar.visemes list
	 * 
	 * @param visemeName name of the phoneme to apply
	 * @param emotiveWeight  100% in 0 to 1 range of blending between current facial pose and the viseme
	 */
	public void setViseme(String visemeName, float emotiveWeight)
	{
		FacePose visPose = visemes.get(visemeName);
		if (visPose != null) 
		{
			for(int i=0; i< mHead.faceModel.BSWeights.length; i++)
			{
				if(visPose.faceWeights[i]>0.01)
					mHead.faceModel.BSWeights[i]=emotiveWeight*(mHead.faceModel.BSWeights[i]-visPose.faceWeights[i])+visPose.faceWeights[i];  //a*F + (1-a)V--> aF +V -aV --> a*(F-V)+V
			}
			
			for(int i=0; i< mHead.teethModel.BSWeights.length; i++)
			{
				if(visPose.mouthWeights[i]>0.01)
					mHead.teethModel.BSWeights[i]=emotiveWeight*(mHead.teethModel.BSWeights[i]-visPose.mouthWeights[i])+visPose.mouthWeights[i];  //a*F + (1-a)V--> aF +V -aV --> a*(F-V)+V
					mHead.tongueModel.BSWeights[i]=emotiveWeight*(mHead.tongueModel.BSWeights[i]-visPose.mouthWeights[i])+visPose.mouthWeights[i];  //a*F + (1-a)V--> aF +V -aV --> a*(F-V)+V
			}			
		} 
		else 
		{
		    // No such key so vacation time...
		}
	}
	
	/**
	 * blends between a viseme and a emotion expression
	 * 
	 * @param visemeName name of the viseme
	 * @param emotionName name of the emotion
	 * @param weight  	100% in 0 to 1 range of blending between the emotion and the viseme (1 is full emotion)
	 */	
	public void blendVisemeEmotion(String visemeName,String emotionName, float weight)
	{
		FacePose visPose = visemes.get(visemeName);
		FacePose emoPose = emotions.get(emotionName);
		
		//Log.d(TAG,"blend weight: "+weight+" "+emotionName +" "+ visemeName);
		
		if (visPose != null&&emoPose!=null) 
		{
			this.setEmotion(emotionName);
			
			for(int i=0; i<mouthRegionBlends.length; i++)
			{
				int idx=mouthRegionBlends[i];
				mHead.faceModel.BSWeights[idx]= weight*emoPose.faceWeights[idx] + (1-weight)*visPose.faceWeights[idx];
			}
		}
		else 
		{
		    // No such key so vacation time...
		}
		this.doMouthLinks();
	}

	/**
	 * sets the current blendshape weights to the desired emotion.
	 * the emotion has to exist within the VMAvatar.emotions list
	 * 
	 * @param emotionName 	: name of the emotion to apply	 
	 */
	public void setEmotion(String emotionName)
	{
		FacePose EmotiPose = emotions.get(emotionName);
		if (EmotiPose != null) 
		{
		   mHead.faceModel.BSWeights=EmotiPose.faceWeights.clone();
		   mHead.teethModel.BSWeights=EmotiPose.mouthWeights.clone();
		   mHead.tongueModel.BSWeights=EmotiPose.mouthWeights.clone();
		} else 
		{
		    // No such key so vacation time...
		}
	}
	
	/**
	 * sets the current blendshape weights to the given face expression
	 *  
	 * @param pose 	  facial expression	 
	 * @see FacePose
	 */
	public void setFacePose(FacePose pose)
	{
//		if(	pose.faceWeights.length==mHead.faceModel.BSWeights.length &&
//			pose.mouthWeights.length==mHead.mouthModel.BSWeights.length)
//		{
//			mHead.faceModel.BSWeights = pose.faceWeights;
//			mHead.mouthModel.BSWeights= pose.mouthWeights;
//		}
		if(	pose.faceWeights.length==mHead.faceModel.BSWeights.length)
		{
			mHead.faceModel.BSWeights = pose.faceWeights;
			mHead.teethModel.BSWeights= pose.faceWeights;
			mHead.tongueModel.BSWeights= pose.faceWeights;
		}
		else
			Log.e("xml","Different Size of Blendshape Weights");

	}
	
	/**
	 * Modifies the specified face blendshape weight
	 * @param blendID  index of the blendshape to be modified
	 * @param weight  desired weight
	 */
	public void setFaceBlendshape(int blendID, float weight)
	{
		if(blendID>0&&blendID<mHead.faceModel.BSWeights.length)
			mHead.faceModel.BSWeights[blendID]=weight;
	}
	
	/**
	 * Modifies the whole face blendshape weights 
	 * @param weights weights to be applied
	 */
	public void setFaceBlendshape(float[] weights)
	{
		if(weights.length==mHead.faceModel.BSWeights.length)
			mHead.faceModel.BSWeights=weights;
	}

	/**
	 * Modifies the whole face blendshape weights to 0.0
	 */
	public void setNeutralFace()
	{
		animator.animEngine.blend();
	}
	
	/**
	 * Modifies the specified mouth blendshape weight
	 * @param blendID index of the mouth blendshape to be modified
	 * @param weight desired weight value
	 */
	public void setMouthBlendshape(int blendID, float weight)
	{
		if(blendID>0&&blendID<mHead.teethModel.BSWeights.length)
			mHead.teethModel.BSWeights[blendID]=weight;
		if(blendID>0&&blendID<mHead.tongueModel.BSWeights.length)
			mHead.tongueModel.BSWeights[blendID]=weight;
	}
	
	/**
	 * Modifies the whole mouth blendshape weights 
	 * @param weights weights to be applied
	 */
	public void setMouthBlendshape(float[] weights)
	{
		if(weights.length==mHead.teethModel.BSWeights.length)
			mHead.teethModel.BSWeights=weights;
		if(weights.length==mHead.tongueModel.BSWeights.length)
			mHead.tongueModel.BSWeights=weights;
	}
	
	/**
	 * Sets the blendshapes ID that controls the left and right eye blinking 
	 * @param leftBlink index of the left eye blink blendshape
	 * @param rightBlink index of the right eye blink blendshape
	 */	
	public void setBlinkIDs(int leftBlink, int rightBlink)
	{
		blinkLID=leftBlink;
		blinkRID=rightBlink;
	}
	
	/**
	 *Attaches the given model to the Extra Models group from the specified file
	 *That model will only undergo global scene transforms and will not be subject 
	 *to the heads orientation	 *
	 * 
	 * @param modfile			bob model file
	 * @param matfile			material file
	 * @throws IOException loading file problem 
	 */
	public void addExtraModel(String modfile, String matfile) throws IOException
	{
		try
		{
			extraModels.add(VMBOLoader.loadModel(mContext, modfile));
			
			if(matfile==""|| matfile=="default"||matfile=="Default")
			{
				extraMaterials.add(new VMMaterial());
			}
			else
			{
				extraMaterials.add(VMBOLoader.loadMaterials(mContext,matfile));
			}
			
		} catch (IOException e) 
		{
			Log.e(TAG,"Error: Could not add "+modfile+" to head");
			//e.printStackTrace();
		}
	}

	/**
	 *Attaches the given model to the Head Models group from the specified file in the SD card.
	 *The model will be subject to head transformations (ie. it will follow the head)
	 * 
	 * @param modfile			bob model file
	 * @param matfile			material file
	 * @throws IOException  Loading problem
	 */
	public void addExtraModelToHead(String modfile, String matfile) throws IOException
	{
		try
		{
			if(matfile==""|| matfile=="default"||matfile=="Default")
			{
				mHead.addExtraModel(VMBOLoader.loadModel(mContext,modfile), new VMMaterial());
			}
			else
			{
				mHead.addExtraModel(VMBOLoader.loadModel(mContext,modfile),VMBOLoader.loadMaterials(mContext,matfile));
			}
			
		} catch (IOException e) 
		{
			Log.e(TAG,"Error: Could not add "+modfile+" to head");
			//e.printStackTrace();
		}
	}

	/**
	 * Loads the necessary textures for each material.
	 * needs to be called before any Render call.
	 * needs to be called after a opengl context is set up.
	 * tipically called in the Renderer onSurfaceCreated()
	 */
	public void loadTextures()
	{
		final String vertexShader = VMShaderUtil.readShaderFromRawResource(mContext, R.raw.colormap_vert);   		
 		final String fragmentShader = VMShaderUtil.readShaderFromRawResource(mContext, R.raw.colormap_frag);
 				
		final int vertexShaderHandle = VMShaderUtil.compileShader(GLES20.GL_VERTEX_SHADER, vertexShader);	
		final int fragmentShaderHandle = VMShaderUtil.compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader);
		
		mProgram = VMShaderUtil.createAndLinkProgram(vertexShaderHandle,fragmentShaderHandle, null);

		if (mProgram == 0) 
		{
			throw new RuntimeException("Error compiling the shader programs");
		}

		mHead.loadTextures();

		for(int i=0; i<extraMaterials.size();i++)
		{	
			if (extraMaterials.get(i).textureID == -1)	extraMaterials.get(i).loadTexture();				
		}
	}

	/**
	 * Binds the attributes and uniforms
	 */
    private void useShader()
    {
        //Log.i(TAG,"useShader!");

        GLES20.glUseProgram(mProgram);
        VMShaderUtil.checkGlError("glUseProgram");

        // get handles for the vertex attributes
        mrm_VertexHandle = GLES20.glGetAttribLocation(mProgram, "rm_Vertex");
        VMShaderUtil.checkGlError("glGetAttribLocation rm_Vertex");
        if (mrm_VertexHandle == -1) {
            throw new RuntimeException("Could not get attrib location for rm_Vertex");
        }
        mrm_NormalHandle = GLES20.glGetAttribLocation(mProgram, "rm_Normal");
        VMShaderUtil.checkGlError("glGetAttribLocation rm_Normal");
        if (mrm_NormalHandle == -1) {
            throw new RuntimeException("Could not get attrib location for rm_Normal");
        }
        mrm_TexCoord0Handle = GLES20.glGetAttribLocation(mProgram, "rm_TexCoord0");
        VMShaderUtil.checkGlError("glGetAttribLocation rm_TexCoord0");
        if (mrm_TexCoord0Handle == -1) {
            throw new RuntimeException("Could not get attrib location for rm_TexCoord0");
        }

        // get handles for the light and eye positions
        mfvLightPositionHandle = GLES20.glGetUniformLocation(mProgram, "fvLightPosition");
        VMShaderUtil.checkGlError("glGetUniformLocation fvLightPosition");
        if (mfvLightPositionHandle == -1) {
            throw new RuntimeException("Could not get uniform location for fvLightPosition");
        }
        mfvEyePositionHandle = GLES20.glGetUniformLocation(mProgram, "fvEyePosition");
        VMShaderUtil.checkGlError("glGetUniformLocation fvEyePosition");
        if (mfvEyePositionHandle == -1) {
            throw new RuntimeException("Could not get uniform location for fvEyePosition");
        }

        // get handles for the transform matrices
        mmatViewProjectionHandle = GLES20.glGetUniformLocation(mProgram, "matViewProjection");
        VMShaderUtil.checkGlError("glGetUniformLocation matViewProjection");
        if (mmatViewProjectionHandle == -1) {
            throw new RuntimeException("Could not get uniform location for matViewProjection");
        }
        mmatViewProjectionInverseTransposeHandle = GLES20.glGetUniformLocation(mProgram, "matViewProjectionInverseTranspose");
        VMShaderUtil.checkGlError("glGetUniformLocation matViewProjectionInverseTranspose");
        if (mmatViewProjectionInverseTransposeHandle == -1) {
            //throw new RuntimeException("Could not get uniform location for matViewProjectionInverseTranspose");
        }

        // get handles for the material properties

        mfvAmbientHandle = GLES20.glGetUniformLocation(mProgram, "fvAmbient");
        VMShaderUtil.checkGlError("glGetUniformLocation fvAmbient");
        if (mfvAmbientHandle == -1) {
            //throw new RuntimeException("Could not get uniform location for fvAmbient");
        }
        mfvDiffuseHandle = GLES20.glGetUniformLocation(mProgram, "fvDiffuse");
        VMShaderUtil.checkGlError("glGetUniformLocation fvDiffuse");
        if (mfvDiffuseHandle == -1) {
            //throw new RuntimeException("Could not get uniform location for fvDiffuse");
        }
        mfvSpecularHandle = GLES20.glGetUniformLocation(mProgram, "fvSpecular");
        VMShaderUtil.checkGlError("glGetUniformLocation fvSpecular");
        if (mfvSpecularHandle == -1) {
            //throw new RuntimeException("Could not get uniform location for fvSpecular");
        }
        mfSpecularPowerHandle = GLES20.glGetUniformLocation(mProgram, "fSpecularPower");
        VMShaderUtil.checkGlError("glGetUniformLocation fSpecularPower");
        if (mfSpecularPowerHandle == -1) {
            //throw new RuntimeException("Could not get uniform location for fSpecularPower");
        }
        miTexturedHandle = GLES20.glGetUniformLocation(mProgram, "iTextured");
        VMShaderUtil.checkGlError("glGetUniformLocation iTextured");
        if (miTexturedHandle == -1) {
            //throw new RuntimeException("Could not get uniform location for iTextured");
        }

        mfTestTimeHandle = GLES20.glGetUniformLocation(mProgram, "iTime");
        mfvEyeTranslationHandle = GLES20.glGetUniformLocation(mProgram, "fvEyeTranslation");
        miIndexHandle = GLES20.glGetUniformLocation(mProgram, "iIndex");
		mfHeadNoddingAngleHandle = GLES20.glGetUniformLocation(mProgram, "fHeadNoddingAngle");
		mfvEyeRotationHandle = GLES20.glGetUniformLocation(mProgram, "fvEyeRotation");
    }

	/**
	 * render the models in the Extra Models group
	 */
	private void renderExtraModels()
	{
		//load extra textures
		for(int i=0; i<extraModels.size();i++)
		{
			//bind buffers
	    	// vertex array
	        GLES20.glVertexAttribPointer(mrm_VertexHandle, 3, GLES20.GL_FLOAT, false,0, extraModels.get(i).mVerticesBuffer);		VMShaderUtil.checkGlError("glVertexAttribPointer mrm_VertexHandle");
	        GLES20.glEnableVertexAttribArray(mrm_VertexHandle);																			VMShaderUtil.checkGlError("glEnableVertexAttribArray mrm_VertexHandle");
	        // normal array
	        GLES20.glVertexAttribPointer(mrm_NormalHandle, 3, GLES20.GL_FLOAT, false, 0,  extraModels.get(i).mNormalsBuffer);		VMShaderUtil.checkGlError("glVertexAttribPointer mrm_NormalHandle");
	        GLES20.glEnableVertexAttribArray(mrm_NormalHandle);																			VMShaderUtil.checkGlError("glEnableVertexAttribArray mrm_NormalHandle");
	        // UV array
	        GLES20.glVertexAttribPointer(mrm_TexCoord0Handle, 2, GLES20.GL_FLOAT, false, 0,  extraModels.get(i).mTexCoordsBuffer);	VMShaderUtil.checkGlError("glVertexAttribPointer mrm_TexCoord0Handle");
			GLES20.glEnableVertexAttribArray(mrm_TexCoord0Handle);

			//bind material, textures
			GLES20.glUniform4f(mfvAmbientHandle,	 extraMaterials.get(i).matrix[0], extraMaterials.get(i).matrix[1], extraMaterials.get(i).matrix[2],  extraMaterials.get(i).matrix[3]);		VMShaderUtil.checkGlError("glUniform4f mfvAmbientHandle");
			GLES20.glUniform4f(mfvDiffuseHandle,	 extraMaterials.get(i).matrix[4], extraMaterials.get(i).matrix[5], extraMaterials.get(i).matrix[6],  extraMaterials.get(i).matrix[7]);		VMShaderUtil.checkGlError("glUniform4f mfvDiffuseHandle");
			GLES20.glUniform4f(mfvSpecularHandle,	 extraMaterials.get(i).matrix[8], extraMaterials.get(i).matrix[9], extraMaterials.get(i).matrix[10],  extraMaterials.get(i).matrix[11]);	VMShaderUtil.checkGlError("glUniform4f mfvSpecularHandle");
			GLES20.glUniform1f(mfSpecularPowerHandle,extraMaterials.get(i).matrix[12]);																														VMShaderUtil.checkGlError("glUniform1f mfSpecularPowerHandle");

			// turn on the texture if there is one
			if (extraMaterials.get(i).textureID != -1 )
			{

				GLES20.glEnableVertexAttribArray(mrm_TexCoord0Handle);
				GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
				GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, extraMaterials.get(i).textureID);
				GLES20.glUniform1i(miTexturedHandle, 1);

			}
			else
				GLES20.glUniform1i(miTexturedHandle, 0);

			if (extraMaterials.get(i).bumpID != -1 )
			{

				GLES20.glEnableVertexAttribArray(mrm_TexCoord0Handle);
				GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
				GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, extraMaterials.get(i).bumpID);

			}

			GLES20.glDrawElements(GLES20.GL_TRIANGLES,extraModels.get(i).mIndexBuffer.capacity(), GLES20.GL_UNSIGNED_SHORT, extraModels.get(i).mIndexBuffer);		VMShaderUtil.checkGlError("glDrawElements");
		}
	}

	/**
	 * Associates a face blendshape to a mouth blendshape
	 * The face blendshape will control the weight of its associated blendshape.
	 * Needed for actions that need to be done together.
	 * For example, jaw opening(face) and mouth opening (mouth)
	 *
	 * @param faceBlendshape index of the face blendshape
	 * @param mouthBlendshape: index of the mouth blendshape
	 */
	public void addMouthLink(int faceBlendshape, int mouthBlendshape)
	{
		mouthLinks.put(faceBlendshape, mouthBlendshape);
	}

	/**
	 * Enforces all of the existing  mouth links.
	 * Applies the face blendshape weight to its associated mouth blendshape
	 * for all existing mouthlinks
	 */
	private void doMouthLinks()
	{
		//iterate over the keys
//		for (Integer key : mouthLinks.keySet())
//		{
//			if(key!=null)
        for(int key=0; key<mHead.faceModel.nBS; key++){
            //Log.d("check", "key: "+key+", face: "+mHead.faceModel.nBS+", tongue: "+mHead.tongueModel.nBS);
            mHead.teethModel.BSWeights[mouthLinks.get(key)]=mHead.faceModel.BSWeights[key];
            mHead.tongueModel.BSWeights[mouthLinks.get(key)]=mHead.faceModel.BSWeights[key];
		}

	}

	/**
	 * Renders the whole Avatar
	 *  
	 * @param M Model matrix from the Renderer set the global transform of the avatar 
	 * @param V View matrix
	 * @param P Projection matrix
	 */	
	public void Render(float[] M, float[] V, float P[] )
	{
    	//apply the blendshapes to the neutral face
    	mHead.faceModel.applyBlendShapes();

    	//ensure the connected blendshapes between face and mouth
    	doMouthLinks();
    	mHead.teethModel.applyBlendShapes();
		mHead.tongueModel.applyBlendShapes();

		useShader();

		//bind global Attributes
		//bind the light and eye positions
        GLES20.glUniform3f(GLES20.glGetUniformLocation(mProgram, "fvLightPosition[0]"), mfvLightPosition[0], mfvLightPosition[1], mfvLightPosition[2]); VMShaderUtil.checkGlError("glUniform3f mfvLightPositionHandle");
        GLES20.glUniform3f(GLES20.glGetUniformLocation(mProgram, "fvLightPosition[1]"), mfvLightPosition[3], mfvLightPosition[4], mfvLightPosition[5]); VMShaderUtil.checkGlError("glUniform3f mfvLightPositionHandle");
		GLES20.glUniform3f(mfvEyePositionHandle,mfvEyePosition[0], mfvEyePosition[1], mfvEyePosition[2]);			VMShaderUtil.checkGlError("glUniform3f mfvEyePositionHandle");


        Matrix.multiplyMM(mmatModelView, 0, V, 0, M, 0);
        Matrix.multiplyMM(mmatViewProjection, 0, P, 0, mmatModelView, 0);

        //bind matrices
        Matrix.invertM(mmatViewProjection_inv, 0, mmatViewProjection, 0);
        Matrix.transposeM(mmatViewProjection_inv_t, 0, mmatViewProjection_inv, 0);

        // bind the transform matrices
        GLES20.glUniformMatrix4fv(mmatViewProjectionHandle, 1, false, mmatViewProjection, 0);							VMShaderUtil.checkGlError("glUniformMatrix4fv mmatViewProjectionHandle");
        GLES20.glUniformMatrix4fv(mmatViewProjectionInverseTransposeHandle, 1, false, mmatViewProjection_inv_t, 0);	VMShaderUtil.checkGlError("glUniformMatrix4fv mmatViewProjectionInverseTransposeHandle");

        //renderExtraModels();
        mHead.render(mmatViewProjection);

	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Avatar Head Class.
	 * 	 * 
	 * @author Roger Blanco i Ribera
	 *
	 */
	public class AvatarHead
	{
		private float[] translation= {0.0f,0.0f,0.0f};
		public float[] rotation= {0.0f,0.0f,0.0f};
		public float[] rotationOffset= {0.0f,0.0f,0.0f};

		private VMBOModel faceModel;
		private VMMaterial faceMaterial;

		private VMBOModel teethModel;
		private VMMaterial teethMaterial;

		private VMBOModel tongueModel;
		private VMMaterial tongueMaterial;

		private List<VMBOModel> extraHeadModels	= new ArrayList<VMBOModel>();
		private List<VMMaterial> extraHeadMaterials= new ArrayList<VMMaterial>();

		private boolean followEyes=false;

		//matrices
		private float[] MVP_inv = new float[16];
	    private float[] MVP_inv_t = new float[16];

	    private AvatarEye mEye;

		public AvatarHead(String faceBobFile,  String faceBobMatFile,
							String teethBobFile, String teethMatFile,
						    String tongueBobFile, String tongueMatFile,
							String eyeBobFile,	 String eyeMatFile) throws IOException
		{
			faceModel=VMBOLoader.loadModel(mContext,faceBobFile);
			faceMaterial = VMBOLoader.loadMaterials(mContext, faceBobMatFile);

			teethModel=VMBOLoader.loadModel(mContext,teethBobFile);
			teethMaterial = VMBOLoader.loadMaterials(mContext, teethMatFile);

			tongueModel=VMBOLoader.loadModel(mContext,tongueBobFile);
			tongueMaterial = VMBOLoader.loadMaterials(mContext, tongueMatFile);

			mEye= new AvatarEye(eyeBobFile,eyeMatFile);
		}

		/**
		 * New version not considering mouth model
		 */
		public AvatarHead(String faceBobFile,  String faceBobMatFile, String eyeBobFile, String eyeMatFile) throws IOException
		{
			faceModel = VMBOLoader.loadModel(mContext, faceBobFile);
			faceMaterial = VMBOLoader.loadMaterials(mContext, faceBobMatFile);

			mEye= new AvatarEye(eyeBobFile,eyeMatFile);
		}

		/**
		 * Returns the VMBOModel of the face
		 * @return faceModel VMBOModel
		 */
		public VMBOModel getFaceModel()
		{
			return faceModel;
		}

		/**
		 * Returns the VMBOModel of the mouth
		 * @return mouthModel VMBOModel
		 */
		public VMBOModel getTeethModel()
		{
			return teethModel;
		}
		public VMBOModel getTongueModel()
		{
			return tongueModel;
		}


		/**
		 *Set the translation of the head with respect to the world coordinate frame
		 *
		 * @param trans float[] 3D orientation vector for the eyes rx,ry,rz
		 *
		 */
		public void setTranslation(float[] trans)
		{
			translation=trans;
		}

		/**
		 *Sets the orientation of the head
		 *
		 * @param rot float[] 3D orientation vector rx,ry,rz
		 *
		 */
		public void setRotation(float[] rot)
		{
			rotation=rot;
			for (int i=0; i<3; i++)
			{
				if(rotation[i]>45.0f) rotation[i]=45.0f;
				if(rotation[i]<-45.0f) rotation[i]=-45.0f;
			}
		}

		/**
		 * Sets the eyes translation with respect of the head coordinate system
		 *
		 * @param eyeTransL: left eye translation
		 * @param eyeTransR: right eye translation
		 */
		public void setEyeTranslation(float[] eyeTransL, float[] eyeTransR )
		{
			mEye.setTranslation(eyeTransL, eyeTransR);
		}

		/**
		 *Sets the orientation of the eyes
		 *
		 * @param eyeRot float[] 3D orientation vector for the eyes rx,ry,rz
		 *
		 */
		public void setEyeRotation(float[] eyeRot)
		{
			mEye.setRotation(eyeRot);
		}

		private float tempElapsedTime = 0f;
		private long elapsedRealtime = 0;
        private float interval = 3.8f;
        private float[] desiredEyeRotation = new float[3];
		private float[] currentEyeRotation = new float[3];
		private float maxAngle = 10;
		private void setEyeRandomRotation()
		{
			elapsedRealtime = SystemClock.elapsedRealtime() - elapsedRealtime;
			tempElapsedTime += elapsedRealtime * 0.001;

			for(int i = 0; i < 2; i++){
				currentEyeRotation[i] += (desiredEyeRotation[i] - currentEyeRotation[i]) * 0.4f;
			}
			//mEye.setRotation(currentEyeRotation);
			if(tempElapsedTime > interval) {
				tempElapsedTime = 0f;
				for(int i = 0; i < 2; i++){
					desiredEyeRotation[i] = (float)Math.random() * maxAngle - maxAngle * 0.5f;
				}
			}
			elapsedRealtime = SystemClock.elapsedRealtime();
		}



		/**
		 * Adds a model to the extra model group
		 * @param mod: loaded Model
		 * @param mat: loaded Material
		 */
		public void addExtraModel(VMBOModel mod, VMMaterial mat) //throws IOException
		{
			extraHeadModels.add(mod);
			extraHeadMaterials.add(mat);

			//Log.i(TAG,"added material succesfully"+ extraHeadModels.size()+" "+extraHeadMaterials.size());
		}
		/**
		 * loads the textures associated with the head group model
		 */
		public void loadTextures()
		{
			//load face model textures
			if (faceMaterial.textureID == -1)	faceMaterial.loadTexture();
			if (teethMaterial.textureID == -1)	teethMaterial.loadTexture();
			if (tongueMaterial.textureID == -1)	tongueMaterial.loadTexture();
			//if (mouthMaterial.textureID == -1)	mouthMaterial.loadTexture();

			//load eye texture
			mEye.loadTextures();

			//Log.e(TAG, "this works");

			//load extra textures
			for(int i=0; i<extraHeadMaterials.size();i++)
			{
				if (extraHeadMaterials.get(i).textureID == -1)	extraHeadMaterials.get(i).loadTexture();
			}

			//Log.e(TAG, "this works too");
		}
		/**
		 * Renders the Face model

		 */

		float testTime = 0;

		private void renderFace()
		{
			testTime++;
			GLES20.glUniform1f(mfTestTimeHandle, testTime);


			//bind buffers
	    	// vertex array
	        GLES20.glVertexAttribPointer(mrm_VertexHandle, 3, GLES20.GL_FLOAT, false,0, faceModel.mVerticesBuffer);
	        VMShaderUtil.checkGlError("glVertexAttribPointer mrm_VertexHandle");
	        GLES20.glEnableVertexAttribArray(mrm_VertexHandle);
	        VMShaderUtil.checkGlError("glEnableVertexAttribArray mrm_VertexHandle");
	        // normal array
	        GLES20.glVertexAttribPointer(mrm_NormalHandle, 3, GLES20.GL_FLOAT, false, 0, faceModel.mNormalsBuffer);
	        VMShaderUtil.checkGlError("glVertexAttribPointer mrm_NormalHandle");
	        GLES20.glEnableVertexAttribArray(mrm_NormalHandle);
	        VMShaderUtil.checkGlError("glEnableVertexAttribArray mrm_NormalHandle");
	        // UV array
	        GLES20.glVertexAttribPointer(mrm_TexCoord0Handle, 2, GLES20.GL_FLOAT, false, 0, faceModel.mTexCoordsBuffer);
	        VMShaderUtil.checkGlError("glVertexAttribPointer mrm_TexCoord0Handle");
			GLES20.glEnableVertexAttribArray(mrm_TexCoord0Handle);
			VMShaderUtil.checkGlError("glEnableVertexAttribArray mrm_TexCoord0Handle");

	        //bind material, textures
			GLES20.glUniform4f(mfvAmbientHandle,faceMaterial.matrix[0], faceMaterial.matrix[1], faceMaterial.matrix[2],  faceMaterial.matrix[3]);		VMShaderUtil.checkGlError("glUniform4f mfvAmbientHandle");
			GLES20.glUniform4f(mfvDiffuseHandle,faceMaterial.matrix[4], faceMaterial.matrix[5], faceMaterial.matrix[6],  faceMaterial.matrix[7]);		VMShaderUtil.checkGlError("glUniform4f mfvDiffuseHandle");
			GLES20.glUniform4f(mfvSpecularHandle,faceMaterial.matrix[8], faceMaterial.matrix[9], faceMaterial.matrix[10],  faceMaterial.matrix[11]);	VMShaderUtil.checkGlError("glUniform4f mfvSpecularHandle");
			GLES20.glUniform1f(mfSpecularPowerHandle, faceMaterial.matrix[12]);																			VMShaderUtil.checkGlError("glUniform1f mfSpecularPowerHandle");

			GLES20.glUniform3f(mfvEyeTranslationHandle, 0.0f, 0.0f, 0.0f);

			// turn on the texture if there is one
			if (faceMaterial.textureID != -1)
			{
				int mTextureUniformHandle = GLES20.glGetUniformLocation(mProgram, "baseMap");
				GLES20.glEnableVertexAttribArray(mrm_TexCoord0Handle);
				GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
				GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, faceMaterial.textureID);
				GLES20.glUniform1i(mTextureUniformHandle, 0);
				GLES20.glUniform1i(miTexturedHandle, 1);
			}
			else
				GLES20.glUniform1i(miTexturedHandle, 0);

			if (faceMaterial.bumpID != -1)
			{
				int mTextureUniformHandle = GLES20.glGetUniformLocation(mProgram, "bumpMap");
				GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
				GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, faceMaterial.bumpID);
				GLES20.glUniform1i(mTextureUniformHandle, 1);
			}

			//draw models
			GLES20.glDrawElements(GLES20.GL_TRIANGLES,faceModel.mIndexBuffer.capacity(), GLES20.GL_UNSIGNED_SHORT, faceModel.mIndexBuffer);		VMShaderUtil.checkGlError("glDrawElements");
		}
		/**
		 * Renders the Mouth model
		 */
		private void renderTeeth()
		{
			//bind buffers
	    	// vertex array
	        GLES20.glVertexAttribPointer(mrm_VertexHandle, 3, GLES20.GL_FLOAT, false,0, teethModel.mVerticesBuffer);		VMShaderUtil.checkGlError("glVertexAttribPointer mrm_VertexHandle");
	        GLES20.glEnableVertexAttribArray(mrm_VertexHandle);															VMShaderUtil.checkGlError("glEnableVertexAttribArray mrm_VertexHandle");
	        // normal array
	        GLES20.glVertexAttribPointer(mrm_NormalHandle, 3, GLES20.GL_FLOAT, false, 0, teethModel.mNormalsBuffer);		VMShaderUtil.checkGlError("glVertexAttribPointer mrm_NormalHandle");
	        GLES20.glEnableVertexAttribArray(mrm_NormalHandle);															VMShaderUtil.checkGlError("glEnableVertexAttribArray mrm_NormalHandle");
	        // UV array
	        GLES20.glVertexAttribPointer(mrm_TexCoord0Handle, 2, GLES20.GL_FLOAT, false, 0, teethModel.mTexCoordsBuffer);	VMShaderUtil.checkGlError("glVertexAttribPointer mrm_TexCoord0Handle");
			GLES20.glEnableVertexAttribArray(mrm_TexCoord0Handle);														VMShaderUtil.checkGlError("glEnableVertexAttribArray mrm_TexCoord0Handle");

	        //bind material, textures
			GLES20.glUniform4f(mfvAmbientHandle,teethMaterial.matrix[0], teethMaterial.matrix[1], teethMaterial.matrix[2],  teethMaterial.matrix[3]);		VMShaderUtil.checkGlError("glUniform4f mfvAmbientHandle");
			GLES20.glUniform4f(mfvDiffuseHandle,teethMaterial.matrix[4], teethMaterial.matrix[5], teethMaterial.matrix[6],  teethMaterial.matrix[7]);		VMShaderUtil.checkGlError("glUniform4f mfvDiffuseHandle");
			GLES20.glUniform4f(mfvSpecularHandle,teethMaterial.matrix[8], teethMaterial.matrix[9], teethMaterial.matrix[10],  teethMaterial.matrix[11]);	VMShaderUtil.checkGlError("glUniform4f mfvSpecularHandle");
			GLES20.glUniform1f(mfSpecularPowerHandle, teethMaterial.matrix[12]);																			VMShaderUtil.checkGlError("glUniform1f mfSpecularPowerHandle");

			// turn on the texture if there is one
			if (teethMaterial.textureID != -1)
			{
				GLES20.glEnableVertexAttribArray(mrm_TexCoord0Handle);
				GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
				GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, teethMaterial.textureID);
				GLES20.glUniform1i(miTexturedHandle, 1);

			}
			else
				GLES20.glUniform1i(miTexturedHandle, 0);

			if (teethMaterial.bumpID != -1)
			{
				int mTextureUniformHandle = GLES20.glGetUniformLocation(mProgram, "bumpMap");
				GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
				GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, teethMaterial.bumpID);
				GLES20.glUniform1i(mTextureUniformHandle, 1);
			}

			//draw models
			GLES20.glDrawElements(GLES20.GL_TRIANGLES,teethModel.mIndexBuffer.capacity(), GLES20.GL_UNSIGNED_SHORT, teethModel.mIndexBuffer);		VMShaderUtil.checkGlError("glDrawElements");

		}

		private void renderTongue()
		{
			//bind buffers
			// vertex array
			GLES20.glVertexAttribPointer(mrm_VertexHandle, 3, GLES20.GL_FLOAT, false,0, tongueModel.mVerticesBuffer);		VMShaderUtil.checkGlError("glVertexAttribPointer mrm_VertexHandle");
			GLES20.glEnableVertexAttribArray(mrm_VertexHandle);															VMShaderUtil.checkGlError("glEnableVertexAttribArray mrm_VertexHandle");
			// normal array
			GLES20.glVertexAttribPointer(mrm_NormalHandle, 3, GLES20.GL_FLOAT, false, 0, tongueModel.mNormalsBuffer);		VMShaderUtil.checkGlError("glVertexAttribPointer mrm_NormalHandle");
			GLES20.glEnableVertexAttribArray(mrm_NormalHandle);															VMShaderUtil.checkGlError("glEnableVertexAttribArray mrm_NormalHandle");
			// UV array
			GLES20.glVertexAttribPointer(mrm_TexCoord0Handle, 2, GLES20.GL_FLOAT, false, 0, tongueModel.mTexCoordsBuffer);	VMShaderUtil.checkGlError("glVertexAttribPointer mrm_TexCoord0Handle");
			GLES20.glEnableVertexAttribArray(mrm_TexCoord0Handle);														VMShaderUtil.checkGlError("glEnableVertexAttribArray mrm_TexCoord0Handle");

			//bind material, textures
			GLES20.glUniform4f(mfvAmbientHandle,tongueMaterial.matrix[0], tongueMaterial.matrix[1], tongueMaterial.matrix[2],  tongueMaterial.matrix[3]);		VMShaderUtil.checkGlError("glUniform4f mfvAmbientHandle");
			GLES20.glUniform4f(mfvDiffuseHandle,tongueMaterial.matrix[4], tongueMaterial.matrix[5], tongueMaterial.matrix[6],  tongueMaterial.matrix[7]);		VMShaderUtil.checkGlError("glUniform4f mfvDiffuseHandle");
			GLES20.glUniform4f(mfvSpecularHandle,tongueMaterial.matrix[8], tongueMaterial.matrix[9], tongueMaterial.matrix[10],  tongueMaterial.matrix[11]);	VMShaderUtil.checkGlError("glUniform4f mfvSpecularHandle");
			GLES20.glUniform1f(mfSpecularPowerHandle, tongueMaterial.matrix[12]);																			VMShaderUtil.checkGlError("glUniform1f mfSpecularPowerHandle");


			// turn on the texture if there is one
			if (tongueMaterial.textureID != -1)
			{
				GLES20.glEnableVertexAttribArray(mrm_TexCoord0Handle);
				GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
				GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tongueMaterial.textureID);
				GLES20.glUniform1i(miTexturedHandle, 1);

			}
			else
				GLES20.glUniform1i(miTexturedHandle, 0);
			//draw models
			GLES20.glDrawElements(GLES20.GL_TRIANGLES,tongueModel.mIndexBuffer.capacity(), GLES20.GL_UNSIGNED_SHORT, tongueModel.mIndexBuffer);		VMShaderUtil.checkGlError("glDrawElements");

		}

		/**
		 * Renders the models from the extra models group
		 */
		private void renderExtraModels()
		{
			//load extra textures
			for(int i=0; i<extraHeadModels.size();i++)
			{
				//bind buffers
		    	// vertex array
		        GLES20.glVertexAttribPointer(mrm_VertexHandle, 3, GLES20.GL_FLOAT, false,0, extraHeadModels.get(i).mVerticesBuffer);		VMShaderUtil.checkGlError("glVertexAttribPointer mrm_VertexHandle");
		        GLES20.glEnableVertexAttribArray(mrm_VertexHandle);																			VMShaderUtil.checkGlError("glEnableVertexAttribArray mrm_VertexHandle");
		        // normal array
		        GLES20.glVertexAttribPointer(mrm_NormalHandle, 3, GLES20.GL_FLOAT, false, 0,  extraHeadModels.get(i).mNormalsBuffer);		VMShaderUtil.checkGlError("glVertexAttribPointer mrm_NormalHandle");
		        GLES20.glEnableVertexAttribArray(mrm_NormalHandle);																			VMShaderUtil.checkGlError("glEnableVertexAttribArray mrm_NormalHandle");
		        // UV array
		        GLES20.glVertexAttribPointer(mrm_TexCoord0Handle, 2, GLES20.GL_FLOAT, false, 0,  extraHeadModels.get(i).mTexCoordsBuffer);	VMShaderUtil.checkGlError("glVertexAttribPointer mrm_TexCoord0Handle");
				GLES20.glEnableVertexAttribArray(mrm_TexCoord0Handle);

				//bind material, textures
				GLES20.glUniform4f(mfvAmbientHandle,	 extraHeadMaterials.get(i).matrix[0], extraHeadMaterials.get(i).matrix[1], extraHeadMaterials.get(i).matrix[2],  extraHeadMaterials.get(i).matrix[3]);		VMShaderUtil.checkGlError("glUniform4f mfvAmbientHandle");
				GLES20.glUniform4f(mfvDiffuseHandle,	 extraHeadMaterials.get(i).matrix[4], extraHeadMaterials.get(i).matrix[5], extraHeadMaterials.get(i).matrix[6],  extraHeadMaterials.get(i).matrix[7]);		VMShaderUtil.checkGlError("glUniform4f mfvDiffuseHandle");
				GLES20.glUniform4f(mfvSpecularHandle,	 extraHeadMaterials.get(i).matrix[8], extraHeadMaterials.get(i).matrix[9], extraHeadMaterials.get(i).matrix[10],  extraHeadMaterials.get(i).matrix[11]);	VMShaderUtil.checkGlError("glUniform4f mfvSpecularHandle");
				GLES20.glUniform1f(mfSpecularPowerHandle,extraHeadMaterials.get(i).matrix[12]);																														VMShaderUtil.checkGlError("glUniform1f mfSpecularPowerHandle");

				GLES20.glUniform3f(mfvEyeTranslationHandle, 0.0f, 0.0f, 0.0f);

				// turn on the texture if there is one
				if (extraHeadMaterials.get(i).textureID != -1 )
				{

					GLES20.glEnableVertexAttribArray(mrm_TexCoord0Handle);
					GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
					GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, extraHeadMaterials.get(i).textureID);
					GLES20.glUniform1i(miTexturedHandle, 1);

				}
				else
					GLES20.glUniform1i(miTexturedHandle, 0);

				if (extraHeadMaterials.get(i).bumpID != -1)
				{
					int mTextureUniformHandle = GLES20.glGetUniformLocation(mProgram, "bumpMap");
					GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
					GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, extraHeadMaterials.get(i).bumpID);
					GLES20.glUniform1i(mTextureUniformHandle, 1);
				}
				GLES20.glDrawElements(GLES20.GL_TRIANGLES,extraHeadModels.get(i).mIndexBuffer.capacity(), GLES20.GL_UNSIGNED_SHORT, extraHeadModels.get(i).mIndexBuffer);		VMShaderUtil.checkGlError("glDrawElements");
			}
		}
		/**
		 * Renders the models of the Head group
		 * @param MVP: Model View Projection transform matrix for the head group models
		 */
		public void render(float[] MVP) //receive the global transform
		{
			//apply local transform
			Matrix.translateM(MVP, 0, translation[0], translation[1], translation[2]);
			Matrix.rotateM(MVP, 0, rotationOffset[0]+rotation[0], 1, 0, 0);
			Matrix.rotateM(MVP, 0, rotationOffset[1]+rotation[1], 0, 1, 0);
			Matrix.rotateM(MVP, 0, rotationOffset[2]+rotation[2], 0, 0, 1);
	        //Matrix.scaleM(MVP,0, 0.1f, 0.1f, 0.1f);

			//bind matrices
	        Matrix.invertM(MVP_inv, 0, MVP, 0);
	        Matrix.transposeM(MVP_inv_t, 0, MVP_inv, 0);

	        // bind the transform matrices
	        GLES20.glUniformMatrix4fv(mmatViewProjectionHandle, 1, false, MVP, 0);							VMShaderUtil.checkGlError("glUniformMatrix4fv mmatViewProjectionHandle");
	        GLES20.glUniformMatrix4fv(mmatViewProjectionInverseTransposeHandle, 1, false, MVP_inv_t, 0);	VMShaderUtil.checkGlError("glUniformMatrix4fv mmatViewProjectionInverseTransposeHandle");
	        GLES20.glUniform1f(mfHeadNoddingAngleHandle, GlobalHeadNoddingValue);

            //draw Face
			GLES20.glUniform1i(miIndexHandle, 0);
			GLES20.glUniform3f(mfvEyeRotationHandle, 0, 0, 0);			VMShaderUtil.checkGlError("glUniform3f mfvEyePositionHandle");
	        renderFace();

	        //draw the mouth
	        renderTeeth();
			renderTongue();

	        //draw extraModels
	        renderExtraModels();

			setEyeRandomRotation();
	        //mEye.rotation[0]=rotation[0]+rotationOffset[0];
			//mEye.rotation[1]=rotation[1]+rotationOffset[1];
	        if(followEyes)
			{
	        	mEye.rotation[0]*=-1;
				mEye.rotation[1]*=-1;
			}
			//draw Eye
			GLES20.glUniform3f(mfvEyeRotationHandle, currentEyeRotation[0], currentEyeRotation[1], currentEyeRotation[2]);			VMShaderUtil.checkGlError("glUniform3f mfvEyePositionHandle");
			GLES20.glUniform1i(miIndexHandle, 1);
			mEye.render(MVP);

		}

	}
	
	/**
	 * Class Avatar Eye
	 *
	 * @author Roger Blanco i Ribera, Sunjin Jung
	 *
	 */
	class AvatarEye
	{
		private VMBOModel eyeModel;
		private VMMaterial  eyeMaterial;
		
		private float[] translation_left= {0.0f,0.0f,0.0f};
		private float[] translation_right= {0.0f,0.0f,0.0f};
		private float[] rotation= {0.0f,0.0f,0.0f};
		
		//render matrices
		private float[] MVP = new float[16];
		private float[] MVP_inv = new float[16];
	    private float[] MVP_inv_t = new float[16];
	    
		public AvatarEye(String bobFile, String MatFile) throws IOException
		{
			eyeModel = VMBOLoader.loadModel(mContext, bobFile);
			eyeMaterial = VMBOLoader.loadMaterials(mContext, MatFile);
		}
		/**
		 * Eye translation with respect of the head coordinate system 
		 * 
		 * @param transL: left eye translation
		 * @param transR: right eye translation
		 */
		public void setTranslation(float[] transL, float[] transR)
		{
			translation_left  = transL;
			translation_right = transR;
		}
		
		/**
		 *Sets the orientation of the eyes 
		 * 
		 * @param rot float[] 3D orientation vector for the eyes rx,ry,rz 
		 */
		public void setRotation(float[] rot)
		{
			rotation=rot;
			
			for (int i=0; i<3; i++)
			{
				if(rotation[i]>45.0f) rotation[i]=45.0f;
				if(rotation[i]<-45.0f) rotation[i]=-45.0f;
			}
		}
		/**
		 * Load textures associated with the eyes
		 */
		public void loadTextures()
		{
			if (eyeMaterial.textureID == -1)	eyeMaterial.loadTexture();
		}
		
		/**
		 *Renders the left and right eyes  
		 * @param ModelViewProj: Model View Projection Transform matrix for the eye coordiante system
		 */


		public void render(float[] ModelViewProj) //receive the global transform
		{
			MVP= ModelViewProj.clone(); //clone since we need to restore it for the other eye

			//bind buffers
	    	// vertex array
	        GLES20.glVertexAttribPointer(mrm_VertexHandle, 3, GLES20.GL_FLOAT, false,0, eyeModel.mVerticesBuffer);		VMShaderUtil.checkGlError("glVertexAttribPointer mrm_VertexHandle");
	        GLES20.glEnableVertexAttribArray(mrm_VertexHandle);															VMShaderUtil.checkGlError("glEnableVertexAttribArray mrm_VertexHandle");	        
	        // normal array
	        GLES20.glVertexAttribPointer(mrm_NormalHandle, 3, GLES20.GL_FLOAT, false, 0, eyeModel.mNormalsBuffer);		VMShaderUtil.checkGlError("glVertexAttribPointer mrm_NormalHandle");
	        GLES20.glEnableVertexAttribArray(mrm_NormalHandle);															VMShaderUtil.checkGlError("glEnableVertexAttribArray mrm_NormalHandle");
	        // UV array
	        GLES20.glVertexAttribPointer(mrm_TexCoord0Handle, 2, GLES20.GL_FLOAT, false, 0, eyeModel.mTexCoordsBuffer);	VMShaderUtil.checkGlError("glVertexAttribPointer mrm_TexCoord0Handle");
			GLES20.glEnableVertexAttribArray(mrm_TexCoord0Handle);														VMShaderUtil.checkGlError("glEnableVertexAttribArray mrm_TexCoord0Handle");

	        //bind material, textures
			GLES20.glUniform4f(mfvAmbientHandle,eyeMaterial.matrix[0], eyeMaterial.matrix[1], eyeMaterial.matrix[2],  eyeMaterial.matrix[3]);		VMShaderUtil.checkGlError("glUniform4f mfvAmbientHandle");		
			GLES20.glUniform4f(mfvDiffuseHandle,eyeMaterial.matrix[4], eyeMaterial.matrix[5], eyeMaterial.matrix[6],  eyeMaterial.matrix[7]);		VMShaderUtil.checkGlError("glUniform4f mfvDiffuseHandle");		
			GLES20.glUniform4f(mfvSpecularHandle,eyeMaterial.matrix[8], eyeMaterial.matrix[9], eyeMaterial.matrix[10],  eyeMaterial.matrix[11]);	VMShaderUtil.checkGlError("glUniform4f mfvSpecularHandle");		
			GLES20.glUniform1f(mfSpecularPowerHandle, eyeMaterial.matrix[12]);																			VMShaderUtil.checkGlError("glUniform1f mfSpecularPowerHandle");
			
			// turn on the texture if there is one
			if (eyeMaterial.textureID != -1) 
			{
				GLES20.glEnableVertexAttribArray(mrm_TexCoord0Handle);
				GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
				GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, eyeMaterial.textureID);
				GLES20.glUniform1i(miTexturedHandle, 1);
				
			}
			else
				GLES20.glUniform1i(miTexturedHandle, 0);

			if (eyeMaterial.bumpID != -1)
			{
				int mTextureUniformHandle = GLES20.glGetUniformLocation(mProgram, "bumpMap");
				GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
				GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, eyeMaterial.bumpID);
				GLES20.glUniform1i(mTextureUniformHandle, 1);
			}



			//LEFT EYE///////////////////////////////////////////////////////////////////////////////////////
			//apply local transform
			Matrix.translateM(MVP, 0, translation_left[0], translation_left[1], translation_left[2]);
			Matrix.rotateM(MVP, 0, rotation[0], 1, 0, 0);
			Matrix.rotateM(MVP, 0, rotation[1], 0, 1, 0);

			//bind matrices
	        Matrix.invertM(MVP_inv, 0, MVP, 0);	        
	        Matrix.transposeM(MVP_inv_t, 0, MVP_inv, 0);
	        
	        // bind the transform matrices
	        GLES20.glUniformMatrix4fv(mmatViewProjectionHandle, 1, false, MVP, 0);							VMShaderUtil.checkGlError("glUniformMatrix4fv mmatViewProjectionHandle");
	        GLES20.glUniformMatrix4fv(mmatViewProjectionInverseTransposeHandle, 1, false, MVP_inv_t, 0);	VMShaderUtil.checkGlError("glUniformMatrix4fv mmatViewProjectionInverseTransposeHandle");

			GLES20.glUniform3f(mfvEyeTranslationHandle, translation_left[0], translation_left[1], translation_left[2]);

			//draw models
			GLES20.glDrawElements(GLES20.GL_TRIANGLES,eyeModel.mIndexBuffer.capacity(), GLES20.GL_UNSIGNED_SHORT, eyeModel.mIndexBuffer);		VMShaderUtil.checkGlError("glDrawElements");
			
			//RIGHT EYE//////////////////////////////////////////////////////////////////////////////////////
			//restore parents array
			MVP= ModelViewProj.clone(); 
			//apply local transform
			Matrix.translateM(MVP, 0, translation_right[0], translation_right[1], translation_right[2]);
			Matrix.rotateM(MVP, 0, rotation[0], 1, 0, 0);
			Matrix.rotateM(MVP, 0, rotation[1], 0, 1, 0);
	        //Matrix.scaleM(MVP,0, 0.1f, 0.1f, 0.1f);		
	        
			//bind matrices
	        Matrix.invertM(MVP_inv, 0, MVP, 0);	        
	        Matrix.transposeM(MVP_inv_t, 0, MVP_inv, 0);
	        
	        // bind the transform matrices
	        GLES20.glUniformMatrix4fv(mmatViewProjectionHandle, 1, false, MVP, 0);							VMShaderUtil.checkGlError("glUniformMatrix4fv mmatViewProjectionHandle");
	        GLES20.glUniformMatrix4fv(mmatViewProjectionInverseTransposeHandle, 1, false, MVP_inv_t, 0);		VMShaderUtil.checkGlError("glUniformMatrix4fv mmatViewProjectionInverseTransposeHandle");

			GLES20.glUniform3f(mfvEyeTranslationHandle, translation_right[0], translation_right[1], translation_right[2]);

			//draw right eye
			GLES20.glDrawElements(GLES20.GL_TRIANGLES,eyeModel.mIndexBuffer.capacity(), GLES20.GL_UNSIGNED_SHORT, eyeModel.mIndexBuffer);		VMShaderUtil.checkGlError("glDrawElements");
		}
		
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Class Animator Thread
	 * Manages the animation of the avatar whether is a key framed animation 
	 * or the random blinking
	 *   
	 * @author Roger Blanco i Ribera, Sunjin Jung
	 *
	 */
	class Animator extends Thread 
	{	/** Target Frames Per Second*/
		//private final static int 	MAX_FPS = 70;				// desired fps
		private final static int 	MAX_FPS = 60;				// desired fps
		//** maximum number of allowed frames to skip*/
		private final static int	MAX_FRAME_SKIPS = 5;			// maximum number of frames to be skipped
		//**target time for a single frame*/
		private final static int	FRAME_PERIOD = 1000 / MAX_FPS;	// the frame period

//		/** range in degrees for the procedural head motion */
//		float[] headMotionMultiplier= new float[]{5,10,3};
//
//		/** speed of the procedural head motion*/
//		float[] headMotionSpeeds= new float[]{10000.0f, 9000.0f, 11000.0f};
		
		/**FPS counter*/
		private FPSCounter sfps;
		
		/**animation play state*/
		private boolean running;		// flag to hold game state
		
		/** procedural head motion state	 */
		private boolean headMotion=false;
		
		/**keyframe animation manager.
		 * @see KeyFramedAnimation*/
		public KeyFramedAnimation animEngine = new KeyFramedAnimation();

		/**Procedural blinking engine
		 * @see Blinker*/
		private Blinker blinkEngine=new Blinker();
		
		/**
		 * enables the random blinking 
		 * 
		 * @param doBlink enable if true else disable
		 */
		public void doBlinking(boolean doBlink)
		{

			if(doBlink)
				blinkEngine.start();
			else
				blinkEngine.stop();
		}
		/**
		 * enables/disables the procedural headMotion
		 * @param doHead enable if true
		 */
		public void doHeadMotion(boolean doHead)
		{
			//headMotion=doHead;
		}
		
		/**
		 * sets the procedural head motion parameters
		 * 
		 * @param pitch range of pitch motion
		 * @param yaw range of yaw motion
		 * @param roll range of roll motion
		 */
		public void setHeadMotionRange(float pitch, float yaw, float roll)
		{
//			headMotionMultiplier[0]=pitch;
//			headMotionMultiplier[1]=yaw;
//			headMotionMultiplier[2]=roll;
		}
		/**
		 * sets the procedural head motion parameters
		 * 
		 * @param pitch intensity of pitch motion
		 * @param yaw intensity of yaw motion
		 * @param roll intensity of roll motion
		 */
		public void setHeadMotionSpeed(float pitch, float yaw, float roll)
		{
//			if(pitch >1.0f) pitch=1.0f;
//			if(yaw >1.0f) yaw=1.0f;
//			if(roll >1.0f) roll=1.0f;
//
//			if(pitch <0.0f) pitch=0.0f;
//			if(yaw <0.0f) yaw=0.0f;
//			if(roll <0.0f) roll=0.0f;
//
//			headMotionSpeeds[0]=(1.0f-pitch)*19000+1000;
//			headMotionSpeeds[1]=(1.0f-yaw)*19000+1000;
//			headMotionSpeeds[2]=(1.0f-roll)*19000+1000;
		}
		
		public void setRunning(boolean running) 
		{
			this.running = running;
		}
	
		public Animator() 
		{
			super();
			sfps= new FPSCounter();
		}
	
		@Override
		public void run() 
		{
			//Canvas canvas;
			running=true;
			
			long timeline;
			long beginTime;		// the time when the cycle begun
			long timeDiff;		// the time it took for the cycle to execute
			int sleepTime;		// ms to sleep (<0 if we're behind)
			int framesSkipped;	// number of frames being skipped 
	
			sleepTime = 0;
			
			blinkEngine.start();
			
			timeline=0;
			while (running) 
			{
				beginTime = System.currentTimeMillis();
				framesSkipped = 0;	// resetting the frames skipped
				
				timeline+=FRAME_PERIOD;

				animEngine.update(FRAME_PERIOD);
				blinkEngine.update(FRAME_PERIOD);

				timeDiff = System.currentTimeMillis() - beginTime;
				
				// calculate sleep time
				sleepTime = (int)(FRAME_PERIOD - timeDiff);
				
				if (sleepTime > 0)
				{
					// if sleepTime > 0 we're OK
					try {
						// send the thread to sleep for a short period
						Thread.sleep(sleepTime);	
					} catch (InterruptedException e) {}
				}
				
				while (sleepTime < 0 && framesSkipped < MAX_FRAME_SKIPS) 
				{
					// we need to catch up
					sleepTime += FRAME_PERIOD;	// add frame period to check if in next frame
					framesSkipped++;
				}
			}
		}
		
		/**
		 * Class Blinker
		 * creates randomized blinking for the avatar
		 * 
		 * @author Roger Blanco i Ribera, Sunjin Jung
		 *
		 */
		class Blinker
		{
			/** current time within the animation*/
			int timeline;
			/** current blinking weight */
			int currentIdx;			
			/**blinking base timings*/
			//private int blinkTiming[]= {0,200,250,400};
			private int blinkTiming[]= {200,400,450,600};
			/**weights for the blink blendshapes*/
			private int blinkWeights[]= {0,1,1,0};
			/**animation state*/
			boolean run=false;
			/**blinking state: closing/opening eyes*/
			private int state=1;
			/**starting open eyes*/
			private float startWeight[] ={0,0};
			/**waiting time until next blink*/
			int timeToNextBlink;
			
			public void start()
			{
				timeline=0;
				currentIdx=0;
				run = true;
				timeToNextBlink=0;
			}
			public void stop()
			{
				run=false;
			}
			
			void update(float dt)
			{
				if(run)
				{	
					if(timeToNextBlink<=0)
					{
						if(timeline==0)
						{
							startWeight[0]=0.0f;
							startWeight[1]=0.0f;
//							startWeight[0]=mHead.faceModel.BSWeights[blinkLID];
//							startWeight[1]=mHead.faceModel.BSWeights[blinkRID];
						}
						timeline+=dt;

						if(timeline>200) {
							state = blinkWeights[currentIdx + 1] - blinkWeights[currentIdx];

							float tweight = (float) (timeline - blinkTiming[currentIdx]) / (float) (blinkTiming[currentIdx + 1] - blinkTiming[currentIdx]);

							float weight = 1.0f;

							if (state > 0) weight = tweight;
							else if (state < 0) weight = 1 - tweight;

							weight = Math.min(1.0f, Math.max(0.0f, weight));

							//applyBlink here
							mHead.faceModel.BSWeights[blinkLID] = weight + (1 - weight) * startWeight[0];
							mHead.faceModel.BSWeights[blinkRID] = weight + (1 - weight) * startWeight[1];

							if (timeline >= blinkTiming[currentIdx + 1] && currentIdx != blinkTiming.length - 2) {
								currentIdx++;
							}

							if (timeline >= blinkTiming[blinkTiming.length - 1]) {
								mHead.faceModel.BSWeights[blinkLID] = startWeight[0];
								mHead.faceModel.BSWeights[blinkRID] = startWeight[1];

								timeToNextBlink = 4800 * 2 / 3 + (int) (Math.random() * 1000 - 500);

								//restart
								currentIdx = 0;
								timeline = 0;
							}
						}
					}
					else
					{
						timeToNextBlink-=dt;
					}
				}
			}
		}
		
	}
	
	/**
	 * Class Key Framed Animation
	 * @author  Roger Blanco i Ribera, Sunjin Jung
	 *
	 */
	class KeyFramedAnimation
	{
		/**list of keyframes in the animation*/
		private List<KeyFrame> keyframes	= new ArrayList<KeyFrame>();
		/**current position of the animation*/
		private int timeline;
		/**The last frame time of the animation (update when keyFrames are added)*/
		private int lastFrame=0;		//
		/**Animation state*/
		private boolean run_speech=false;	//
		private boolean run_blend=false;	//
		/**current facePose*/
		private FacePose facePose;
		/**current keyframe*/
		private int currentIdx = -1;
		/**given animation time step*/
		private int xmlTimeStep = 16;
		/** update audio timing (ms) */
		private int audioTiming = 0;
		
		/**Empty constructor. Animation needs to be setup by adding keys*/
		public KeyFramedAnimation(){	}
		
		/**
		 * Clears the animation.
		 * Removes all the keys.
		 */
		public void clearKeys()
		{
			lastFrame=0;
			keyframes.clear();
		}		
		
		/**
		 * Adds a key frame to the existing animation at the given time.
		 * 
		 * @param time time of the key frame
		 * @param keyPose expression 
		 */
		public void addKey(int time, FacePose keyPose)
		{
			KeyFrame key = new KeyFrame();
			key.time = time;
			key.pose = keyPose;
			keyframes.add(key);
			Collections.sort(keyframes, new KeyFrameComparator() ); //sort with respect to time			
			if(lastFrame < time) lastFrame=time;			
		}
		/**
		 * Sets animation from the given list of keys
		 * @param animKeys List of keyframes
		 */
		public void setKeys(List<KeyFrame> animKeys)
		{
			lastFrame=0;
			keyframes.clear();
			for(int i=0; i<animKeys.size(); i++)
			{
				KeyFrame key = new KeyFrame();
				key.time = animKeys.get(i).time;
				key.pose = animKeys.get(i).pose;
				key.noddingValue = animKeys.get(i).noddingValue;
				keyframes.add(key);
				Collections.sort(keyframes, new KeyFrameComparator() ); //sort with respect to time			
				if(lastFrame < key.time) lastFrame=key.time;
			}
		}
		/**
		 * Starts the animation from the beginning
		 */
		public void start()
		{
			timeline = 0;
			currentIdx = -1;
			xmlTimeStep = keyframes.get(1).time -  keyframes.get(0).time;
			if(!keyframes.isEmpty()) run_speech=true;
		}
		/**
		 * Stops and rewinds the animation.
		 * The current animation is reset to its starting position
		 */
		public void stop()
		{
			timeline=0;
			currentIdx = -1;
			run_speech=false;
		}
		/**
		 * Blends animation from stop position to neutral position
		 */
		public void blend()
		{
			stop();
			run_blend = true;
		}
		/**
		 * Pauses/ unpauses the animation
		 * the current animation position is maintained
		 */
		public void togglePause()
		{
			if(!keyframes.isEmpty()) //if there is an animation ready for play
			{
				if(timeline < lastFrame && timeline >0) //we can only pause/resume in the middle of the sequence 
					run_speech= !run_speech;
			}
		}

		public void setAudioTiming(int _audioTiming){
			//run_speech=true;
			audioTiming = _audioTiming;
		}

		public int getCurrentIdxFromAudioTiming(){
			return audioTiming/xmlTimeStep;
		}

		/**
		 * update the animation for the given passed time
		 * @param dt update time in ms
		 */
		public void update(int dt)
		{
			if(run_speech)
			{
				currentIdx = getCurrentIdxFromAudioTiming();

				if(keyframes.size()<=currentIdx+1) return;

				float weight= (float)(audioTiming - keyframes.get(currentIdx).time)/(float)( keyframes.get(currentIdx+1).time- keyframes.get(currentIdx).time);
				facePose = smoothStep(  keyframes.get(currentIdx).pose, keyframes.get(currentIdx+1).pose, weight );
                float headNoddingValue = smoothStep(  keyframes.get(currentIdx).noddingValue, keyframes.get(currentIdx+1).noddingValue, weight );
//                Log.d("asddsadsad", headNoddingValue + "  no1");
//                Log.d("asddsadsad", keyframes.get(1).noddingValue + "   no2");
				setFacePose(facePose);
                headNod(headNoddingValue);
			}

			if(run_blend)
			{
				timeline += dt;

				float weight= (float)timeline/500.0f;
				FacePose tmpPose = smoothStep(facePose, 0.0f, weight);
				setFacePose(tmpPose);

				if(timeline >= 500){
					run_blend = false;
					doBlinking(true);
					facePose = tmpPose;
				}
			}
		}
		
		/**
		 * Interpolations the face expression between current key pose and next key pose
		 * 
		 * @param curPose  face pose of the current key frame
		 * @param nextPose face pose of the next key frame
		 * @param weight interpolation step
		 * @return FacePose resulting face expression
		 */

		private FacePose smoothStep(FacePose curPose, FacePose nextPose, float weight)
		{
			//FacePose interPose= new FacePose(nextPose.faceWeights,nextPose.mouthWeights);
			FacePose interPose= new FacePose(nextPose.faceWeights);

			for(int i=0; i<nextPose.faceWeights.length; i++)
			{
				interPose.faceWeights[i]=weight*interPose.faceWeights[i]+(1-weight)*curPose.faceWeights[i];
			}
//			for(int i=0; i<nextPose.mouthWeights.length; i++)
//			{
//				interPose.mouthWeights[i]=weight*interPose.mouthWeights[i]+(1-weight)*curPose.mouthWeights[i];
//			}
			
			return interPose;
		}

        private float smoothStep(float a, float b, float weight)
        {
            return weight * a + (1 - weight) * b;
        }

		private FacePose smoothStep(FacePose curPose, float allWeights, float weight)
		{
			float[] weights = new float[mHead.faceModel.BSWeights.length];
			for(int i=0; i<weights.length; i++) weights[i] = allWeights;
			FacePose interPose= new FacePose(weights);

			if((curPose != null) && ( curPose.faceWeights.length==mHead.faceModel.BSWeights.length))
			{
				for(int i=0; i<curPose.faceWeights.length; i++)
					interPose.faceWeights[i]=weight*interPose.faceWeights[i]+(1-weight)*curPose.faceWeights[i];
			}

			return interPose;
		}
	}
}