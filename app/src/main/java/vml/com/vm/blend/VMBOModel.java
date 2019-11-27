package vml.com.vm.blend;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import android.content.Context;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.util.Log;

import vml.com.animation.R;

/**
 * Blendshape Object Core class
 * 
 * @author Roger Blanco i Ribera, Sunjin Jung
 *
 */
public class VMBOModel
{
	//data containers
	/**Positions for rendering*/
	public FloatBuffer mVerticesBuffer	= null;		
	/**Normals for rendering*/
	public FloatBuffer mNormalsBuffer 	= null;
	/**UV texture coordinates for rendering*/
	public FloatBuffer mTexCoordsBuffer= null;		
	/**Face indices for rendering*/
	public ShortBuffer mIndexBuffer    = null;		
	
	/**Face positions for computation*/
	public float[] NVertices  = null;		
	/**BS position deltas for computation*/
	public float[] BSVertices = null;		
	/**BS position deltas for computation*/
	public float[] BSNormals  = null;		
			
	/**current blendshape weights*/
	public float[] BSWeights  = null;		
	
	/**number of blending vertices*/
	public int 	 nBlendVtx	= 0;		 
	/**total number of vertices*/
	public int	 	 nVtx		= 0;		
	/**number of Blendshapes*/
	public short 	 nBS		= 0;		 
	/**number of faces*/
	public int 	 nFcs		= 0;		 
	/**model has normals*/
	public boolean uNorm		= false;	 
	/**model has a UV mapping*/
	public boolean uUV			= false;	 
	/**blendshapes names*/
	public String[] BSNames		= null;		
	
	/**for scale normalization*/
	public float mLongestAxisLength = 1.0f;  
	
	/////////////////////////////////////////////////////////////////////////
	//RenderScript Variable 
	
	/**RenderScript handle*/
	private RenderScript mRS;
	/**allocation of A matrix */
	private Allocation allocationA; 
	/**allocation of B matrix */
	private Allocation allocationB; 
	/** allocation of C matrix */
	private Allocation allocationC;
	/**allocation of C matrix */
	private Allocation allocationN; 
	/**allocation of Row Size of Matrix A*/
	private Allocation allocationKSize; 
	/**allocation of Col Size of Matrix B;*/
	private Allocation allocationNSize; 
	/**allocation row positions*/
	private Allocation allocationPosRow;
	
	/**compiled script*/
	private ScriptC_blend mScript;
	
	/**output data from RenderScript*/
	private float[] outMatrix;
	/**input row indices*/
	private int[] pos_row;
	/**input number of columns of B*/
	private int[] nSize;
	/**input number of columns of A*/
	private int[] kSize;
		
	/**logcat Tag*/
	private static String TAG = "BO Model";	
	/**
	 * initializes the renderscript to perform the Blendshape interpolation
	 * 
	 * @param context application context
	 */
	public void initRenderScript(Context context)
	{
		mRS = RenderScript.create(context);
		
		int A_m = 1;
		int A_k = nBS;
		int B_n = NVertices.length;
				
		int sizeA = A_m * A_k;
		int sizeB = A_k * B_n;
		int sizeC = A_m * B_n;
		
		initMatrix(A_m, A_k, B_n);		
		
		//Log.d(TAG,"RS - Created");
		// memory allocation part 
		allocationA = Allocation.createSized(mRS, Element.F32(mRS),sizeA);
		Log.i(String.valueOf(sizeA),"size a");
		allocationB = Allocation.createSized(mRS, Element.F32(mRS),sizeB);
		allocationC = Allocation.createSized(mRS, Element.F32(mRS),sizeC);
		allocationN = Allocation.createSized(mRS, Element.F32(mRS),sizeC);
		
		allocationNSize = Allocation.createSized(mRS, Element.I32(mRS), 1);
		allocationKSize = Allocation.createSized(mRS, Element.I32(mRS), 1);
		allocationPosRow = Allocation.createSized(mRS, Element.I32(mRS), A_m);
		
		//Log.d(TAG,"RS - allocated");
		
		allocationN.copyFrom(NVertices);
		allocationB.copyFrom(BSVertices);
		allocationA.copyFrom(BSWeights);
		allocationPosRow.copyFrom(pos_row);
		allocationNSize.copyFrom(nSize);
		allocationKSize.copyFrom(kSize);
				
		//Log.d(TAG,"RS - copied");

		mScript = new ScriptC_blend(mRS, context.getResources(), R.raw.blend);
		
		mScript.bind_matN(allocationN);
		mScript.bind_matA(allocationA);
		mScript.bind_matB(allocationB);
		mScript.bind_outMatrix(allocationC);
		
		mScript.bind_kSize(allocationKSize);
		mScript.bind_nSize(allocationNSize);
	}
	/**
	 * Applies the Blendshape interpolation for the current set of weights
	 */
	public void applyBlendShapes()
	{
		allocationA.copyFrom(BSWeights);
		mScript.forEach_root(allocationPosRow, allocationPosRow);
		allocationC.copyTo(outMatrix);
		mVerticesBuffer.put(outMatrix);
		mVerticesBuffer.rewind();
	}
	/**
	 * Allocates a Matrix
	 * @param m number of rows
	 * @param n number of columns
	 * @return return the allocated matrix
	 */
	private float[] init_matrix(int m, int n)
	{
		int size = m*n;
		float[] r_matrix = new float[size];
		return r_matrix;
	}	
	/**
	 * Computes the idx of each row to use in the renderScript
	 * 
	 * @param m number of rows
	 * @return row index vector
	 */
	private int[] generate_row_idx(int m)
	{
		int[] r_idx = new int[m];
		for(int i=0; i<m; i++) { r_idx[i] = i; }
		return r_idx;
	}
	
	/**
	 * Initialize the data for Renderscript
	 * 
	 * @param A_m matrix A # of rows
	 * @param A_k matrix A # of columns (Matrix B # of rows)
	 * @param B_n matrix B # of columns
	 */
	private void initMatrix(int A_m, int A_k, int B_n)
	{		
		outMatrix = init_matrix(A_m, B_n);
		pos_row = generate_row_idx(A_m);
		
		kSize = new int[1];
		nSize = new int[1];
		
		kSize[0] = A_k;
		nSize[0] = B_n;
	}
}
