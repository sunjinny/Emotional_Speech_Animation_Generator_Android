package vml.com.vm.utils;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.util.Log;

import vml.com.vm.avatar.VMAvatar;
import vml.com.vm.avatar.VMAvatarLoader;
import vml.com.vm.blend.VMBOModel;

/**
 * Helper class of static methods for loading different type of text files.
 * 
 * @author Bang SeungBae
 *
 */
public class ReadMarker
{
	/**debuggin tag*/
	private static String TAG = "ReadMarker";
	
	/**
	 * Loads the marker template.
	 * @param ctx Application context
	 * @param numMarker	Number of markers to read
	 * @param fixedIdx Index of the origin marker
	 * @param templateFile File path of the marker template file
	 * @return The list of template indices
	 */
	public static int[] readTemplate(Context ctx, int[] numMarker, int[] fixedIdx, String templateFile){
		try
		{
			ArrayList<Integer> markerIdxList = new ArrayList<Integer>();
			
			BufferedReader Idxreader = new BufferedReader(new InputStreamReader(ctx.getAssets().open(templateFile)));
			
			String line="";
			int num=0;
			while(true){
				
				line= Idxreader.readLine();	
				if(num==0){
					String value = line.trim();
					numMarker[0] = Integer.parseInt(value);
					num++;
					continue;
				}
				if(num==1){
					String value = line.trim();
					fixedIdx[0] = Integer.parseInt(value);
					num++;
					continue;
				}
		    	if(line==null) 
		    		break;
		    	markerIdxList.add(Integer.parseInt(line));
			}
			int[] markerIdx = new int[markerIdxList.size()];
		    for(int i=0; i<markerIdxList.size(); i++){
		    	markerIdx[i] = markerIdxList.get(i).intValue();
		    	//Log.v("READMARKER", "index: "+markerIdxList.get(i).intValue());
		    }
		    markerIdxList.clear();
		    return markerIdx;
		}
		catch(IOException ex){
			Log.e(TAG,"cannot read file ");
			ex.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Loads a sequence of marker positions. The markers are stored frame by frame. 
	 * @param ctx Application context
	 * @param numMarker Number of markers to read
	 * @param markerIdx template index list
	 * @param filePath file path of the sequence
	 * @return
	 */
	public static float[] loadMarker(Context ctx, int numMarker, int[] markerIdx, String filePath){
		try
		{

		    File readFile = new File(filePath);
			FileInputStream fIn = new FileInputStream(readFile);
		    BufferedReader Posreader = new BufferedReader(new InputStreamReader(fIn));

		    int num=0;
		    String line="";
		    
		    Set<Integer> IdxSet = new HashSet<Integer>();
		    for(int i=0; i<markerIdx.length; i++){
		    	IdxSet.add(markerIdx[i]);
		    	//Log.d("RETARGET", "idx: "+i+": "+markerIdx[i]);
		    }

		    ArrayList<Float> markerPosList = new ArrayList<Float>();
		    while(true)
		    {
		    	line= Posreader.readLine();	
		    	if(line==null) 
		    		break;
		    	if(line.isEmpty())
		    		continue;
		    	String[] values = line.split("\t");

		    	if(IdxSet.contains(num%numMarker)){
			    	markerPosList.add(Float.parseFloat(values[0]));
			    	markerPosList.add(Float.parseFloat(values[1]));
			    	markerPosList.add(Float.parseFloat(values[2]));
		    	}
		    	num++;
		    }
		    Posreader.close();
		    float[] markerPos = new float[markerPosList.size()];
		    for(int i=0; i<markerPosList.size(); i++){
		    	Float f = markerPosList.get(i);
		    	markerPos[i] = (f != null ? f : Float.NaN);
		    }
		    return markerPos;
		}
		catch(IOException ex){
			Log.e(TAG,"cannot read file ");
			ex.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Loads the correspondences positions
	 * @param ctx Application context
	 * @param numBS number of Blendshapes
	 * @param idxPath Path to the correspondence file
	 * @param xmlpath path to the avatar file
	 * @return returns the marker positions on the blendshapes
	 */
	public static float[] readCorrPos(Context ctx, int[] numBS, String idxPath, String xmlpath){
		try
		{
		    File readFile = new File(idxPath);
			FileInputStream fIn = new FileInputStream(readFile);
		    BufferedReader Idxreader = new BufferedReader(new InputStreamReader(fIn));
			
		    ArrayList<Integer> markerIdxList = new ArrayList<Integer>();
			String line="";
			while(true){
				line= Idxreader.readLine();	
		    	if(line==null) 
		    		break;
		    	markerIdxList.add(Integer.parseInt(line));
			}
			int numMarker = markerIdxList.size();
			int[] markerIdx = new int[numMarker];
		    for(int i=0; i<markerIdxList.size(); i++){
		    	markerIdx[i] = markerIdxList.get(i).intValue();
		    }
		    markerIdxList.clear();
			
		    
		    VMAvatar model = VMAvatarLoader.loadAvatar(ctx, xmlpath);
			//VMBOModel model= VMBOLoader.loadModel(ctx,mdPath);
			//model.initRenderScript(ctx);
		    
		    VMBOModel headModel = model.getFaceModel();//model.mHead.faceModel;
		    
			numBS[0] = (int)headModel.nBS;
			float[] vertFloat = new float[3*headModel.nVtx];
			headModel.mVerticesBuffer.get(vertFloat);
			int numBlendVtx = headModel.nBlendVtx;
			int numFixed = headModel.nVtx - numBlendVtx;
			Log.v("CORR","nVtx: "+headModel.nVtx);
			Log.v("CORR","vertFloat.length: "+vertFloat.length);
			Log.v("CORR","numFixed: "+numFixed);
			Log.v("CORR","model.nBlendVtx length: "+headModel.nBlendVtx);
			Log.v("CORR","model.NVertices.length: "+headModel.NVertices.length);
			Log.v("CORR","model.BSVertices.length: "+headModel.BSVertices.length);
			
			float[] markerPos = new float[3*(headModel.nBS+1)*numMarker];
			for(int i=0; i<numMarker; i++){
					markerPos[3*i]   = vertFloat[3*markerIdx[i]];
					markerPos[3*i+1] = vertFloat[3*markerIdx[i]+1];
					markerPos[3*i+2] = vertFloat[3*markerIdx[i]+2];

			}
			for(int j=0; j<headModel.nBS; j++){
				for(int i=0; i<numMarker; i++){
					if(markerIdx[i]-numBlendVtx>0){  
						markerPos[3*(j+1)*numMarker+3*i]   = vertFloat[3*markerIdx[i]];
						markerPos[3*(j+1)*numMarker+3*i+1] = vertFloat[3*markerIdx[i]+1];
						markerPos[3*(j+1)*numMarker+3*i+2] = vertFloat[3*markerIdx[i]+2];
					}
					else{
						markerPos[3*(j+1)*numMarker+3*i]   = vertFloat[3*markerIdx[i]] + headModel.BSVertices[3*j*numBlendVtx+3*markerIdx[i]];
						markerPos[3*(j+1)*numMarker+3*i+1] = vertFloat[3*markerIdx[i]+1] + headModel.BSVertices[3*j*numBlendVtx+3*markerIdx[i]+1];
						markerPos[3*(j+1)*numMarker+3*i+2] = vertFloat[3*markerIdx[i]+2] + headModel.BSVertices[3*j*numBlendVtx+3*markerIdx[i]+2];
					}
				}
			}
			
			Idxreader.close();
		    return markerPos;

		}
		catch(IOException ex){
			Log.e(TAG,"cannot read file ");
			ex.printStackTrace();
			return null;
		}
	}
	
}
