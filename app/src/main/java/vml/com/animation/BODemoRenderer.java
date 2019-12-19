package vml.com.animation;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import vml.com.vm.avatar.VMAvatar;
import vml.com.vm.avatar.VMAvatarLoader;
import vml.com.vm.utils.FPSCounter;
import vml.com.vm.utils.VMShaderUtil;


public class BODemoRenderer implements GLSurfaceView.Renderer 
{
	private float[] worldRotation= {0.0f,0.0f,0.0f};
    private float[] mmatModel = 		new float[16];
    private float[] mmatView = 		new float[16];
    private float[] mmatProjection = 	new float[16];
    //private float[] mfvEyePosition = {0,0,-40}; // 3rd year
	private float[] mfvEyePosition = {0,3,-37};

  	//public float mScale = 0.1f;
	public float mScale = 1.0f;
    public float mAngleX = 0;
    public float mAngleY = 0;
    public float weight=0;
    public float step=0.1f;
    public int curBlend=0;
    public int curVis=0;
    float w=0.0f;

    public int frameCount=0;

    private Context mContext;

    VMAvatar mAvatar_1;
	VMAvatar mAvatar_2;
	VMAvatar mAvatarMan;
	public boolean isMan = false;
	public int hair_model = 0;

    public String currentEmotion="Neutral";
    public String currentViseme ="Neutral";
    public boolean loopVisemes=false;
    public float currentWeight=0;
    public boolean controlHead=false;
    public boolean touchInterface=true;
    public boolean followEyes=true;

    private static FPSCounter fps;
    private static String TAG = "BODemoRend";

    public BODemoRenderer(Context context)
    {
		fps= new FPSCounter();
		mContext = context;

		mAvatar_1=VMAvatarLoader.loadAvatar(context, "Girl_1.xml");
		mAvatar_2=VMAvatarLoader.loadAvatar(context, "Girl_2.xml");
		mAvatarMan=VMAvatarLoader.loadAvatar(context, "Man.xml");

		mAvatar_1.initRenderScript();
		mAvatar_2.initRenderScript();
		mAvatarMan.initRenderScript();

		mAvatar_1.enableBlinking(true);
		mAvatar_2.enableBlinking(true);
		mAvatarMan.enableBlinking(true);

		//mAvatar.enableHeadMotion(true);

		new Thread() {
	           @Override
	           public void run() {
	                  try {
						  final Handler handler = new Handler(Looper.getMainLooper());
						  handler.post(new Runnable() {
						  //((MainActivity) mContext).runOnUiThread(new Runnable() {
							  @Override
							  public void run()
							  {
								   // code runs in a UI(main) thread
								   String[] keys= mAvatar_1.getAnimationList();
							   }
						  });
	                  } catch (final Exception ex){  /*TODO del with exceptions*/  }
	           }
	    }.start();
    }

	@Override
	public void onDrawFrame(GL10 gl)
	{



		GLES20.glClear( GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

		//Rotate world + look at screen
		Matrix.translateM(mmatModel, 0, 0, 1.0f, 0);
		Matrix.setRotateM(mmatModel, 0, 180,0, 1.0f, 0);


		if(!controlHead)
		{
			worldRotation[0]=mAngleY;
			worldRotation[1]=mAngleX;
		}

		if(touchInterface) {
			Matrix.rotateM(mmatModel, 0, worldRotation[0], 1.0f, 0, 0);
			Matrix.rotateM(mmatModel, 0, worldRotation[1], 0, 1.0f, 0);
			Matrix.scaleM(mmatModel, 0, mScale, mScale, mScale);//0.2f*(mScale-1.0f), 0.2f*(mScale-1.0f),  0.2f*(mScale-1.0f));

			if (controlHead) {
				mAvatar_1.setHeadRotation(new float[]{mAngleY, mAngleX, 0.0f});
				mAvatar_2.setHeadRotation(new float[]{mAngleY, mAngleX, 0.0f});
				mAvatarMan.setHeadRotation(new float[]{mAngleY, mAngleX, 0.0f});
			}
		}

		if(isMan)
			mAvatarMan.Render(mmatModel, mmatView, mmatProjection);
		else if(hair_model==0)
			mAvatar_1.Render(mmatModel, mmatView, mmatProjection);
		else if(hair_model==1)
			mAvatar_2.Render(mmatModel, mmatView, mmatProjection);

        //fps.logFrame("FPS");
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height)
	{
		//Log.i(TAG,"onSurfaceChanged");
		GLES20.glViewport(0, 0, width, height);
		float ratio = (float) width / height;
		Matrix.perspectiveM(mmatProjection, 0, 70.0f, ratio, 1, 100);
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config)
	{
		GLES20.glClearColor(0.9f, 0.9f, 0.9f, 1.0f);		VMShaderUtil.checkGlError("glClearColor");
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);				VMShaderUtil.checkGlError("GL_DEPTH_TEST");

		Matrix.setLookAtM(mmatView, 0, mfvEyePosition[0], mfvEyePosition[1], mfvEyePosition[2], 0f, 3.0f, 0f, 0f, 1.0f, 0.0f);

		mAvatar_1.loadTextures(); //loads textures in OpenGL and sets up the shader
		mAvatar_2.loadTextures(); //loads textures in OpenGL and sets up the shader
		mAvatarMan.loadTextures(); //loads textures in OpenGL and sets up the shader
	}



}

