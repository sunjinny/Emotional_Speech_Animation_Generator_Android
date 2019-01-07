package vml.com.animation;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

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
    //private float[] mfvEyePosition = {0,0,-5};
	private float[] mfvEyePosition = {0,0,-40};

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

    VMAvatar mAvatar;

    public String currentEmotion="Neutral";
    public String currentViseme ="Neutral";
    public boolean loopVisemes=false;
    public float currentWeight=0;
    public boolean controlHead=true;
    public boolean followEyes=true;

    private static FPSCounter fps;
    private static String TAG = "BODemoRend";

    public BODemoRenderer(Context context)
    {
		fps= new FPSCounter();

		mContext = context;


		String root = Environment.getExternalStorageDirectory().getAbsolutePath();
		//mAvatar=VMAvatarLoader.loadAvatar(context, root+"/VML_DEMO/Models/Animation/Girl/Girl.xml");
		mAvatar=VMAvatarLoader.loadAvatar(context, "Girl.xml");



		//file:///android_asset/Bonnie.xml

//		PackageManager m = context.getPackageManager();
//		String s = context.getPackageName();
//		try {
//			PackageInfo p = m.getPackageInfo(s, 0);
//			s = p.applicationInfo.dataDir;
//		} catch (PackageManager.NameNotFoundException e) {
//			Log.w("yourtag", "Error Package name not found ", e);
//		}
//		mAvatar=VMAvatarLoader.loadAvatar(context, s+"/model/Animation/Bonnie/Bonnie.xml");

		//mAvatar=VMAvatarLoader.loadAvatar(context, Environment.getExternalStorageDirectory().getAbsolutePath()+ getString(R.string.application_root);"/../../../../main/model/Animation/Bonnie/Bonnie.xml");

		mAvatar.initRenderScript();

		mAvatar.enableBlinking(true);
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
								   String[] keys= mAvatar.getAnimationList();
								   // Selection of the spinner
								   Spinner spinner = (Spinner) ((AppCompatActivity)mContext).findViewById(R.id.spinner);
								   // Application of the Array to the Spinner
								   ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(mContext,android.R.layout.simple_spinner_item,keys);
								   spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
								   spinner.setAdapter(spinnerArrayAdapter);
							   }
						  });
	                  } catch (final Exception ex){  /*TODO del with exceptions*/  }
	           }
	    }.start();
    }

    boolean animating=false;

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

		Matrix.rotateM(mmatModel, 0, worldRotation[0], 1.0f, 0, 0);
		Matrix.rotateM(mmatModel, 0, worldRotation[1], 0, 1.0f, 0);
		Matrix.scaleM(mmatModel, 0, mScale,mScale,mScale);//0.2f*(mScale-1.0f), 0.2f*(mScale-1.0f),  0.2f*(mScale-1.0f));

		//limit ui rotations
//        if(mAngleY>30.0f) mAngleY=30.0f;
//        if(mAngleY<-30.0f) mAngleY=-30.0f;
//
//        if(mAngleX>30.0f) mAngleX=30.0f;
//        if(mAngleX<-30.0f) mAngleX=-30.0f;

        if(controlHead)
			mAvatar.setHeadRotation(new float[]{mAngleY, mAngleX,0.0f});

        mAvatar.Render(mmatModel, mmatView, mmatProjection);

        fps.logFrame("FPS");
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height)
	{
		Log.i(TAG,"onSurfaceChanged");
		GLES20.glViewport(0, 0, width, height);
		float ratio = (float) width / height;
		Matrix.perspectiveM(mmatProjection, 0, 60.0f, ratio, 1, 100);
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config)
	{
		GLES20.glClearColor(0.9f, 0.9f, 0.9f, 1.0f);		VMShaderUtil.checkGlError("glClearColor");
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);				VMShaderUtil.checkGlError("GL_DEPTH_TEST");

		Matrix.setLookAtM(mmatView, 0, mfvEyePosition[0], mfvEyePosition[1], mfvEyePosition[2], 0f, 0f, 0f, 0f, 1.0f, 0.0f);

		mAvatar.loadTextures(); //loads textures in OpenGL and sets up the shader
	}

}

