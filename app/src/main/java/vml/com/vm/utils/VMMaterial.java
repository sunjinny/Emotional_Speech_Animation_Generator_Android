package vml.com.vm.utils;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

/**
 * Material Class
 * 
 * 
 * @author Roger Blanco i Ribera
 *
 */
public class VMMaterial 
{
	/**material name*/
	public String name;
	/**Material information
	 * Contains transparency, ambient, diffuse and specular data
	 *<p>
	 *constructor code:<br>
	 *matrix = new float[] <br>
	 *{<br>
	 *ambient_r, ambient_g, ambient_b, alpha,<br>
	 *diffuse_r, diffuse_g, diffuse_b, alpha,<br>
	 *specular_r,specular_g,specular_b,alpha,<br>
	 *shininess,  0,        0,         0,<br>
	 * };<br>
	 * */
	public float[] matrix = null;
	/**OpenGL textured ID */
	public int textureID = -1;
	/**OpenGL textured ID for the bump map */
	public int bumpID = -1;
	/**OpenGL textured ID for the normal map */
	public int normalID = -1;

	/** vertex array starting index to apply the material */
	public int vertexIndexStart = -1;
	/**last vertex array index to apply the material*/
	public int vertexIndexCount = -1;
	/**texture data*/
	private Bitmap mTexture = null;
	//**bump map data*/
	private Bitmap mBump = null;
	//**normal map data*/
	private Bitmap mNorm = null;

	
	/**
	 * Construct a material with the given data
	 * @param matName material name
	 * @param alpha transparency value
	 * @param ambient ambient lighting color [r,g,b] in range [0,1]
	 * @param diffuse diffuse color [r,g,b] in range [0,1]
	 * @param specular specular color [r,g,b] in range [0,1]
	 * @param shininess specular power
	 * @param texture loaded texture 
	 * @param bump loaded bump map
	 */
	public VMMaterial(String matName, float alpha, float[] ambient, float[] diffuse,
		float[] specular, float shininess, Bitmap texture, Bitmap bump, Bitmap norm)
	{
		name=matName;
		
		matrix = new float[] 
		{
				ambient[0], ambient[1], ambient[2], alpha,
				diffuse[0], diffuse[1], diffuse[2], alpha,
				specular[0], specular[1], specular[2], alpha,
				shininess, 0.0f, 0.0f, 0.0f,
		};
		
		mTexture=texture;
		mBump=bump;
		mNorm = norm;
	}
	/**
	 * Construct a default greyish material
	 */
	public VMMaterial() //create default material 
	{
		name = "default";
		float alpha = 1.0f;
		float[] ambient = new float[] {0.2f, 0.2f, 0.2f};
		float[]  diffuse = new float[] {0.6f, 0.6f, 0.6f};
		float[] specular = new float[] {1.0f, 1.0f, 1.0f};
		float shininess = 10.0f;
		mTexture = null;
		mBump = null;
		mNorm = null;

		matrix = new float[] 
				{
					ambient[0], ambient[1], ambient[2], alpha,
					diffuse[0], diffuse[1], diffuse[2], alpha,
					specular[0], specular[1], specular[2], alpha,
					shininess, 0.0f, 0.0f, 0.0f,
				};
	}
	/*
	 * sets up the textures in OpenGL context
	 */
	public void loadTexture() {
		if ((mTexture == null) && (mBump == null))
			return;
		
		if (mTexture != null) {
			int[] textures = new int[1];
			GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
			GLES20.glGenTextures(1, textures, 0);
			textureID = textures[0];
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureID);

			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
					GLES20.GL_NEAREST);
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
					GLES20.GL_LINEAR);

			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
					GLES20.GL_REPEAT);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
					GLES20.GL_REPEAT);

			GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mTexture, 0);
			mTexture.recycle();
			mTexture = null;
		}


		if (mBump != null) {
			int[] textures = new int[1];
			GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
			GLES20.glGenTextures(1, textures, 0);
			bumpID = textures[0];
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, bumpID);

			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
					GLES20.GL_NEAREST);
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
					GLES20.GL_LINEAR);

			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
					GLES20.GL_REPEAT);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
					GLES20.GL_REPEAT);

			GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mBump, 0);
			mBump.recycle();
			mBump = null;
		}

		if (mNorm != null) {
			int[] textures = new int[1];
			GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
			GLES20.glGenTextures(1, textures, 0);
			normalID = textures[0];
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, normalID);

			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
					GLES20.GL_NEAREST);
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
					GLES20.GL_LINEAR);

			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
					GLES20.GL_REPEAT);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
					GLES20.GL_REPEAT);

			GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mNorm, 0);
			mNorm.recycle();
			mNorm = null;
		}

	}
	
}
