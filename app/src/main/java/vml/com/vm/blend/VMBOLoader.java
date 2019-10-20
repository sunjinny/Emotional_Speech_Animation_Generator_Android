package vml.com.vm.blend;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.regex.Pattern;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import vml.com.vm.utils.VMMaterial;

/**
 *Data Class ObjMaterial
 *Contains render material information in the style of OBJ format (.mat) 
 * @author Roger Blanco i Ribera, Sunjin Jung
 *
 */
class ObjMaterial 
{
	/** material name*/
	public String name;
	/** transparency value*/
	public float alpha;
	/** ambient lighting color*/
	public float[] ambient;
	/** diffuse surface color*/
	public float[] diffuse;
	/**specular reflection color*/
	public float[] specular;
	/** specular power*/
	public float shininess;
	/** color texture map*/
	public Bitmap texture;
	/** bump map*/
	public Bitmap bump;
	
	/**
	 * Creates a default material
	 * highly specular greyish material 
	 */
	public ObjMaterial() {
		super();
		this.name = "default";
		this.alpha = 1.0f;
		this.ambient = new float[] {0.2f, 0.2f, 0.2f, this.alpha};
		this.diffuse = new float[] {0.6f, 0.6f, 0.6f, this.alpha};
		this.specular = new float[] {1.0f, 1.0f, 1.0f, this.alpha};
		this.shininess = 10.0f;
		this.texture = null;
		this.bump = null;
	}
	/**
	 * Creates a material from the given data
	 * 
	 * @param name material name
	 * @param alpha transparecy
	 * @param ambient ambient lighitng color [r,g,b] in [0,1] range
	 * @param diffuse diffuse color [r,g,b] in [0,1] range
	 * @param specular specular color [r,g,b] in [0,1] range
	 * @param shininess specular power 
	 * @param texture loaded color texture
	 * @param bump load loaded bump map
	 */
	public ObjMaterial(String name, float alpha, float[] ambient, float[] diffuse,
			float[] specular, float shininess, Bitmap texture, Bitmap bump) {
		super();
		this.name = name;
		this.alpha = alpha;
		this.ambient = ambient;
		this.diffuse = diffuse;
		this.specular = specular;
		this.shininess = shininess;
		this.texture = texture;
		this.bump = bump;
	}

}
/**
 * Loading class for BO models (.bob files)
 * 
 * @author Roger Blanco i Ribera, Sunjin Jung
 *
 */
public class VMBOLoader 
{
	private static String TAG = "BOLoader";
	private static final int BytesPerFloat = 4;
	private static final int BytesPerShort = 2;
	private static Pattern whitespacePattern = Pattern.compile("\\s+");

	/**
	 * load material file
	 * @param ctx application context
	 * @param mtlFilePath material file path
	 * @return returns the loaded material
	 */
	public static VMMaterial loadMaterials(Context ctx, String mtlFilePath)
	{
		HashMap<String,ObjMaterial> materials = new HashMap<String,ObjMaterial>();
        AssetManager assetManager = ctx.getResources().getAssets();
        InputStream is;
		try {
            is = assetManager.open(mtlFilePath);
		} catch (IOException ex)
        {
            Log.e(TAG,"Cannot read the material file "+mtlFilePath);
            ex.printStackTrace();
            return null;
        }

		BufferedReader reader = new BufferedReader(new InputStreamReader(is));

		String line;
		ObjMaterial currentMaterial = null;
		
		try {
			while ((line = reader.readLine()) != null)
			{
				String[] tokens;
				
				if (line.equals("") || line.startsWith("#"))
					continue;
				
				tokens = whitespacePattern.split(line);
				if (tokens[0].equals("newmtl")) {
					if (currentMaterial != null)
						materials.put(currentMaterial.name, currentMaterial);
					currentMaterial = new ObjMaterial();
					currentMaterial.name = tokens[1];
				} else if (tokens[0].equals("Ka")) {
					float[] temp = new float[4];
					for (int i=0; i<3; i++) {
						temp[i] = Float.parseFloat(tokens[i+1]);
					}
					temp[3] = currentMaterial.alpha;
					currentMaterial.ambient = temp;
				} else if (tokens[0].equals("Kd")) {
					float[] temp = new float[4];
					for (int i=0; i<3; i++) {
						temp[i] = Float.parseFloat(tokens[i+1]);
					}
					temp[3] = currentMaterial.alpha;
					currentMaterial.diffuse = temp;
				} else if (tokens[0].equals("Ks")) 
				{
					float[] temp = new float[4];
					for (int i=0; i<3; i++) 
					{
						temp[i] = Float.parseFloat(tokens[i+1]);
					}
					temp[3] = currentMaterial.alpha;
					currentMaterial.specular = temp;
				}
				else if (tokens[0].equals("Ns")) 
				{
					currentMaterial.shininess = Float.parseFloat(tokens[1]);
				}
				else if (tokens[0].equals("d") || tokens[0].equals("tr")) 
				{
					currentMaterial.alpha = Float.parseFloat(tokens[1]);
					currentMaterial.ambient[3] = currentMaterial.alpha;
					currentMaterial.diffuse[3] = currentMaterial.alpha;
					currentMaterial.specular[3] = currentMaterial.alpha;
				}
				else if (tokens[0].equals("map_Ka") || tokens[0].equals("map_Kd") ) 
				{
					// XXX - TODO - implement multitexturing
					if (currentMaterial.texture == null)
					{
						currentMaterial.texture = BitmapFactory.decodeStream( assetManager.open("Data/"+tokens[1]));
						if (currentMaterial.texture == null)
							throw new RuntimeException("Unable to load texture file "+ "Data/"+tokens[1]);
					}
				} else if (tokens[0].equals("bump")) 
				{
					if (currentMaterial.bump == null)
					{
						currentMaterial.bump = BitmapFactory.decodeStream( assetManager.open("Data/"+tokens[1]));
						if (currentMaterial.bump == null)
							throw new RuntimeException("Unable to load texture file "+ "Data/"+tokens[1] );
					}
				} else 
				{
					//TODO - we don't support this yet
				}
			}
		} 
		catch (IOException e) 
		{
			Log.e(TAG,"error occurred while reading the material file !");
			e.printStackTrace();
			return null;
		}
		
		
		if (currentMaterial != null)
			materials.put(currentMaterial.name, currentMaterial);
		
		 
		VMMaterial vmMat = new VMMaterial("material", currentMaterial.alpha,
										currentMaterial.ambient, currentMaterial.diffuse, 
										currentMaterial.specular,currentMaterial.shininess,
										currentMaterial.texture, currentMaterial.bump);
		return vmMat;
	}
	
	/**
	 * Loads a BO model from a (.bob) file
	 * @param ctx application context
	 * @param bobFilePath path to the model file in the SD card
	 * @return returns the loaded model
	 * @throws IOException Loading problem
	 */
	public static VMBOModel loadModel(Context ctx, String bobFilePath) throws IOException
	{
		//XXX TODO XXX : Deal with the case when there is no Normals or no UVS

        AssetManager assetManager = ctx.getResources().getAssets();
		VMBOModel model = new VMBOModel();
		DataInputStream reader;

		//opening the file
		try  
		{
			reader = new DataInputStream(assetManager.open(bobFilePath));

			//Parsing header
			model.nBlendVtx =	reader.readInt();
			model.nVtx =		reader.readInt();		
			model.nFcs =		reader.readInt();
			model.uNorm=		reader.readBoolean();
			model.uUV  =		reader.readBoolean();
			model.nBS  =(short)	reader.readInt();
			//Log.i(TAG,"verts : "+model.nBlendVtx+" / "+model.nVtx + "   faces : "+model.nFcs+ "   blends : "+model.nBS+ "   use N/B : "+model.uNorm+" "+model.uUV);
			model.BSNames= new String[model.nBS];
			
			//read blend names
			for(int i=0; i<model.nBS; ++i)
			{				
				int curStrSize=reader.readInt();
				byte[] curStr= new byte[curStrSize];
				
				reader.read(curStr);				
				model.BSNames[i]=new String(curStr,"UTF_8");
				
				//Log.i(TAG,"blendshape "+i+" : "+curStrSize+"  "+model.BSNames[i]);
			}

			//Parsing Body
			model.NVertices = new float[3*model.nBlendVtx];		
			model.BSVertices= new float[3*model.nBS*model.nBlendVtx];
			model.BSNormals = new float[3*model.nBS*model.nBlendVtx];
			model.BSWeights = new float[model.nBS];

			//1--Vertices
			//Vertices Buffer//////////////////////////////////////
			ByteBuffer vbb = ByteBuffer.allocateDirect(model.nVtx*3*BytesPerFloat); // nVertex * 3 dimension * 4 (size of Float)
			vbb.order(ByteOrder.nativeOrder());
			model.mVerticesBuffer = vbb.asFloatBuffer();
			
			for (int i = 0; i <3*model.nVtx; i ++)
			{
				float val= reader.readFloat();
				model.mVerticesBuffer.put(val);
				//Neutral Face blending vertices ( up to nBlendVertices x 3)
				if(i<model.NVertices.length)
					model.NVertices[i]=val;
				
			}

			//Normals Buffer
			ByteBuffer nbb = ByteBuffer.allocateDirect(model.nVtx*3*BytesPerFloat);
			nbb.order(ByteOrder.nativeOrder());
			model.mNormalsBuffer = nbb.asFloatBuffer();
			for (int i = 0; i < 3*model.nVtx; i ++)
			{
				float val= reader.readFloat();
				model.mNormalsBuffer.put(val);			
			}

			//Tex Coord Buffer
			ByteBuffer tbb = ByteBuffer.allocateDirect(model.nVtx*2*BytesPerFloat);
			tbb.order(ByteOrder.nativeOrder());
			model.mTexCoordsBuffer = tbb.asFloatBuffer();
			for (int i = 0; i < 2*model.nVtx; i ++)
			{
				float val= reader.readFloat();
				model.mTexCoordsBuffer.put(val);			
			}

			//Indices Buffer
			ByteBuffer ibb = ByteBuffer.allocateDirect(model.nFcs*3*BytesPerShort);
			ibb.order(ByteOrder.nativeOrder());
			model.mIndexBuffer = ibb.asShortBuffer();
			for (int i = 0; i < 3*model.nFcs; i ++)
			{
				short val= (short) reader.readInt();
				model.mIndexBuffer.put(val);			
			}

			//BlendShapes
			//BSverts
			for (int i = 0; i < model.BSVertices.length; i ++)
			{
				float val= reader.readFloat();
				model.BSVertices[i]=val;
			}

			//BSNormals
			for (int i = 0; i < model.BSVertices.length; i ++)
			{
				float val= reader.readFloat();
				model.BSNormals[i]=val;
			}
			model.mVerticesBuffer.position(0);
			model.mNormalsBuffer.position(0);
			model.mTexCoordsBuffer.position(0);
			model.mIndexBuffer.position(0);
			
			reader.close();
		}
		catch (FileNotFoundException ex1)
		{
			Log.e(TAG,bobFilePath + " BOB file not found");
			ex1.printStackTrace();
			return null;
		}
		catch (IOException ex)
		{
			Log.e(TAG,"cannot read the BOB file ");
			ex.printStackTrace();
			return null;
		}
		
		return model;
	}	
	
}
