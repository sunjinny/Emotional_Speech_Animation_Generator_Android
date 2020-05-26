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
import android.opengl.GLES20;
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
    public Bitmap norm;

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
        this.norm = null;
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
			float[] specular, float shininess, Bitmap texture, Bitmap bump, Bitmap norm) {
		super();
		this.name = name;
		this.alpha = alpha;
		this.ambient = ambient;
		this.diffuse = diffuse;
		this.specular = specular;
		this.shininess = shininess;
		this.texture = texture;
		this.bump = bump;
        this.norm = norm;
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
						BitmapFactory.Options opts = new BitmapFactory.Options();
						opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
						currentMaterial.texture = BitmapFactory.decodeStream( assetManager.open("Data/"+tokens[1]), null, opts);
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
				} else if (tokens[0].equals("norm"))
				{
                    if (currentMaterial.norm == null)
                    {
                        currentMaterial.norm = BitmapFactory.decodeStream( assetManager.open("Data/"+tokens[1]));
                        if (currentMaterial.norm == null)
                            throw new RuntimeException("Unable to load texture file "+ "Data/"+tokens[1] );
                    }
				} else {
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
										currentMaterial.texture, currentMaterial.bump, currentMaterial.norm);
		return vmMat;
	}

	private static float[] subtract(float[] arr1, float[] arr2)
	{
		int len = arr1.length;
		float[] val = new float[len];
		for(int i = 0; i < len; i++) {
			val[i] = arr1[i] - arr2[i];
		}
		return val;
	}

	private static float[] normalize(float[] arr){
		int len = arr.length;
		float[] val = new float[len];
		float sum = 0.0f;
		for(int i = 0; i < len; i++) {
			sum += (arr[i] * arr[i]);
		}
		sum = (float)Math.sqrt(sum);
		for(int i = 0; i < len; i++) {
			val[i] = arr[i] / sum;
		}
		return val;
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
				if(i < model.NVertices.length) {
					model.NVertices[i] = val;
				}
				
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

			//Tangents Buffer//////////////////////////////////////
			ByteBuffer tanbb = ByteBuffer.allocateDirect(model.nVtx * 3 * BytesPerFloat); // nVertex * 3 dimension * 4 (size of Float)
			tanbb.order(ByteOrder.nativeOrder());
			model.mTangentsBuffer = tanbb.asFloatBuffer();

			//Indices Buffer
			ByteBuffer ibb = ByteBuffer.allocateDirect(model.nFcs*3*BytesPerShort);
			ibb.order(ByteOrder.nativeOrder());
			model.mIndexBuffer = ibb.asShortBuffer();
			for (int i = 0; i < 3*model.nFcs; i ++)
			{
				short val= (short) reader.readInt();
				model.mIndexBuffer.put(val);			
			}

			Log.d("size of array: ", model.nFcs + "," + model.nVtx);


			// Index 순서대로 vertex position으로 저장한거.
			float[] orderedX = new float[model.nVtx];
			float[] orderedY = new float[model.nVtx];
			float[] orderedZ = new float[model.nVtx];

			float[] orderedTanX = new float[model.nVtx];
			float[] orderedTanY = new float[model.nVtx];
			float[] orderedTanZ = new float[model.nVtx];

/*
			for(int face_i = 0; face_i < 3 * model.nFcs; face_i++)
			{
				int idx = model.mIndexBuffer.get(face_i);
				orderedX[idx] = 1;
				orderedY[idx] = 1;
				orderedZ[idx] = 1;
			}
*/

			for(int face_i = 0; face_i < 3 * model.nFcs; face_i += 3)
			{
				int idx0 = model.mIndexBuffer.get(face_i + 0); // pointer to the vertex position X,Y,Z
				int idx1 = model.mIndexBuffer.get(face_i + 1);
				int idx2 = model.mIndexBuffer.get(face_i + 2);

				float[] v0 = new float[3];
				v0[0] = model.mVerticesBuffer.get(3 * idx0 + 0);
				v0[1] = model.mVerticesBuffer.get(3 * idx0 + 1);
				v0[2] = model.mVerticesBuffer.get(3 * idx0 + 2);
				orderedX[idx0] = v0[0];
				orderedY[idx0] = v0[1];
				orderedZ[idx0] = v0[2];

				float[] v1 = new float[3];
				v1[0] = model.mVerticesBuffer.get(3 * idx1 + 0);
				v1[1] = model.mVerticesBuffer.get(3 * idx1 + 1);
				v1[2] = model.mVerticesBuffer.get(3 * idx1 + 2);
				orderedX[idx1] = v1[0];
				orderedY[idx1] = v1[1];
				orderedZ[idx1] = v1[2];

				float[] v2 = new float[3];
				v2[0] = model.mVerticesBuffer.get(3 * idx2 + 0);
				v2[1] = model.mVerticesBuffer.get(3 * idx2 + 1);
				v2[2] = model.mVerticesBuffer.get(3 * idx2 + 2);
				orderedX[idx2] = v2[0];
				orderedY[idx2] = v2[1];
				orderedZ[idx2] = v2[2];

				// idx0
				float[] uv0 = new float[2];
				uv0[0] = model.mTexCoordsBuffer.get(2 * idx0 + 0);
				uv0[1] = model.mTexCoordsBuffer.get(2 * idx0 + 1);

				// idx1
				float[] uv1 = new float[2];
				uv1[0] = model.mTexCoordsBuffer.get(2 * idx1 + 0);
				uv1[1] = model.mTexCoordsBuffer.get(2 * idx1 + 1);

				// idx2
				float[] uv2 = new float[2];
				uv2[0] = model.mTexCoordsBuffer.get(2 * idx2 + 0);
				uv2[1] = model.mTexCoordsBuffer.get(2 * idx2 + 1);

				float[] edge1 = subtract(v1, v0);
				float[] edge2 = subtract(v2, v0);
				float[] deltaUV1 = subtract(uv1, uv0);
				float[] deltaUV2 = subtract(uv2, uv0);

				float f = 1.0f / (deltaUV1[0] * deltaUV2[1] - deltaUV2[0] * deltaUV1[1]);

				float[] tangent = new float[3];
				tangent[0] = f * (deltaUV2[1] * edge1[0] - deltaUV1[1] * edge2[0]);
				tangent[1] = f * (deltaUV2[1] * edge1[1] - deltaUV1[1] * edge2[1]);
				tangent[2] = f * (deltaUV2[1] * edge1[2] - deltaUV1[1] * edge2[2]);

				tangent = normalize(tangent);

				orderedTanX[idx0] = (orderedTanX[idx0] + tangent[0]) * 0.5f;
				orderedTanY[idx0] = (orderedTanY[idx0] + tangent[1]) * 0.5f;
				orderedTanZ[idx0] = (orderedTanZ[idx0] + tangent[2]) * 0.5f;

				orderedTanX[idx1] = (orderedTanX[idx1] + tangent[0]) * 0.5f;
				orderedTanY[idx1] = (orderedTanY[idx1] + tangent[1]) * 0.5f;
				orderedTanZ[idx1] = (orderedTanZ[idx1] + tangent[2]) * 0.5f;

				orderedTanX[idx2] = (orderedTanX[idx2] + tangent[0]) * 0.5f;
				orderedTanY[idx2] = (orderedTanY[idx2] + tangent[1]) * 0.5f;
				orderedTanZ[idx2] = (orderedTanZ[idx2] + tangent[2]) * 0.5f;

				// COMPUTE TANGENT //
				// v0, v1, v2, uv
			}

			for (int i = 0 ; i < model.nVtx; i++)
			{
				model.mTangentsBuffer.put(orderedTanX[i]);
				model.mTangentsBuffer.put(orderedTanY[i]);
				model.mTangentsBuffer.put(orderedTanZ[i]);
			}

/*
			for (int i = 0; i < model.nVtx - 2; )
			{
				int uv_idx_0_u = i * 2 + 0;
				int uv_idx_0_v = i * 2 + 1;
				int uv_idx_1_u = i * 2 + 2; // (i +1) *2 + 0
				int uv_idx_1_v = i * 2 + 3;
				int uv_idx_2_u = i * 2 + 4;
				int uv_idx_2_v = i * 2 + 5;

				int vtx_idx_0_x = i * 3 + 0;
				int vtx_idx_0_y = i * 3 + 1;
				int vtx_idx_0_z = i * 3 + 2;
				int vtx_idx_1_x = i * 3 + 3;
				int vtx_idx_1_y = i * 3 + 4;
				int vtx_idx_1_z = i * 3 + 5;
				int vtx_idx_2_x = i * 3 + 6;
				int vtx_idx_2_y = i * 3 + 7;
				int vtx_idx_2_z = i * 3 + 8;

				float[] v0 = new float[3];
				v0[0] = model.mVerticesBuffer.get(vtx_idx_0_x);
				v0[1] = model.mVerticesBuffer.get(vtx_idx_0_y);
				v0[2] = model.mVerticesBuffer.get(vtx_idx_0_z);

				float[] v1 = new float[3];
				v1[0] = model.mVerticesBuffer.get(vtx_idx_1_x);
				v1[1] = model.mVerticesBuffer.get(vtx_idx_1_y);
				v1[2] = model.mVerticesBuffer.get(vtx_idx_1_z);

				float[] v2 = new float[3];
				v2[0] = model.mVerticesBuffer.get(vtx_idx_2_x);
				v2[1] = model.mVerticesBuffer.get(vtx_idx_2_y);
				v2[2] = model.mVerticesBuffer.get(vtx_idx_2_z);

				float[] uv0 = new float[2];
				uv0[0] = model.mTexCoordsBuffer.get(uv_idx_0_u);
				uv0[1] = model.mTexCoordsBuffer.get(uv_idx_0_v);
				float[] uv1 = new float[2];
				uv1[0] = model.mTexCoordsBuffer.get(uv_idx_1_u);
				uv1[1] = model.mTexCoordsBuffer.get(uv_idx_1_v);
				float[] uv2 = new float[2];
				uv2[0] = model.mTexCoordsBuffer.get(uv_idx_2_u);
				uv2[1] = model.mTexCoordsBuffer.get(uv_idx_2_v);

				float[] edge1 = subtract(v1, v0);
				float[] edge2 = subtract(v2, v0);
				float[] deltaUV1 = subtract(uv1, uv0);
				float[] deltaUV2 = subtract(uv2, uv0);

				float f = 1.0f / (deltaUV1[0] * deltaUV2[1] - deltaUV2[0] * deltaUV1[1]);
				float[] tangent1 = new float[3];
				tangent1[0] = f * (deltaUV2[1] * edge1[0] - deltaUV1[1] * edge2[0]);
				tangent1[1] = f * (deltaUV2[1] * edge1[1] - deltaUV1[1] * edge2[1]);
				tangent1[2] = f * (deltaUV2[1] * edge1[2] - deltaUV1[1] * edge2[2]);

				tangent1 = normalize(tangent1);

				model.mTangentsBuffer.put(v1[0]);
				model.mTangentsBuffer.put(v1[1]);
				model.mTangentsBuffer.put(v1[2]);
				i++;

				model.mTangentsBuffer.put(v1[0]);
				model.mTangentsBuffer.put(v1[1]);
				model.mTangentsBuffer.put(v1[2]);
				i++;

				model.mTangentsBuffer.put(v1[0]);
				model.mTangentsBuffer.put(v1[1]);
				model.mTangentsBuffer.put(v1[2]);
				i++;
			}
*/
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
			model.mTangentsBuffer.position(0);
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
