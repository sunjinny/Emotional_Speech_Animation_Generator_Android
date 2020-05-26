package vml.com.vm.avatar;


import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;
import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import vml.com.vm.utils.FacePose;
import vml.com.vm.utils.KeyFrame;


/**
 * Loader class for the VMAvatar Class
 * It loads an avatar XML file from the assets.
 * 
 * @author Roger Blanco i Ribera, Sunjin Jung
 *
 */
public class VMAvatarLoader 
{
	private static String TAG = "AvatarLoader";
	static final String outputEncoding = "UTF-8";

	/**
	 * load Avatar from a XML file on the Assets
	 * @param ctx					Activity context
	 * @param avatarFileName		complete path to the avatar XML file
	 * @return 					returns the loaded avatar
	 */		
	public static VMAvatar loadAvatar(Context ctx, String avatarFileName)
	{
		AssetManager assetManager = ctx.getResources().getAssets();
		try
		{
			String sAvatarName="";
			String sfaceModel,sfaceMaterial;
			String steethModel,steethMaterial;
			String stongueModel,stongueMaterial;
			String seyeModel,seyeMaterial;


			InputStream is = assetManager.open(avatarFileName);
			
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();  
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();  
			Document doc = dBuilder.parse(is);  
			
			Element element = doc.getDocumentElement();
			element.normalize();

			/////////////////////////////////////////////////////////////////////////////////////////
			Element avatarEl = doc.getDocumentElement(); //parent node
			sAvatarName=element.getAttributes().getNamedItem("name").getNodeValue();

			//Log.i("Avatar name", sAvatarName);
			
			Element headNode = (Element) avatarEl.getElementsByTagName( "head" ).item(0);			
			
			Element faceNode = (Element) headNode.getElementsByTagName("face").item(0);
			sfaceModel	 = faceNode.getAttributes().getNamedItem("model").getNodeValue();			
			sfaceMaterial= faceNode.getAttributes().getNamedItem("material").getNodeValue();

			Element teethNode = (Element) headNode.getElementsByTagName("teeth").item(0);
			steethModel	 = teethNode.getAttributes().getNamedItem("model").getNodeValue();
			steethMaterial= teethNode.getAttributes().getNamedItem("material").getNodeValue();

			Element tongueNode = (Element) headNode.getElementsByTagName("tongue").item(0);
			stongueModel	 = tongueNode.getAttributes().getNamedItem("model").getNodeValue();
			stongueMaterial= tongueNode.getAttributes().getNamedItem("material").getNodeValue();

			Element eyesNode = (Element) headNode.getElementsByTagName("eyes").item(0);
			Element eyeNode = (Element) eyesNode.getElementsByTagName("eye").item(0);
			seyeModel	 = eyeNode.getAttribute("model");
			seyeMaterial = eyeNode.getAttribute("material");

			VMAvatar  avatar = new VMAvatar( ctx,	sfaceModel, sfaceMaterial, steethModel, steethMaterial, stongueModel, stongueMaterial, seyeModel, seyeMaterial);

			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////

			//Translations
			float[] eyeleftTrans= new float[3];
			float[] eyerightTrans= new float[3];
			Element leftTransNode = (Element) eyesNode.getElementsByTagName("leftPos").item(0);
			eyeleftTrans[0]=Float.parseFloat(leftTransNode.getAttribute("tx"));
			eyeleftTrans[1]=Float.parseFloat(leftTransNode.getAttribute("ty"));
			eyeleftTrans[2]=Float.parseFloat(leftTransNode.getAttribute("tz"));

			Element rightTransNode = (Element) eyesNode.getElementsByTagName("rightPos").item(0);
			eyerightTrans[0]=Float.parseFloat(rightTransNode.getAttribute("tx"));
			eyerightTrans[1]=Float.parseFloat(rightTransNode.getAttribute("ty"));
			eyerightTrans[2]=Float.parseFloat(rightTransNode.getAttribute("tz"));

			avatar.setEyePos(eyeleftTrans,eyerightTrans);

			//get Blink
			Element blinkNode=(Element) headNode.getElementsByTagName("blink").item(0);
			int leftBlink = Integer.parseInt(blinkNode.getAttribute("left"));
			int rightBlink = Integer.parseInt(blinkNode.getAttribute("right"));

			avatar.setBlinkIDs(leftBlink, rightBlink);

			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			//ExtraNodes--HEAD !!!!
			Element extraHeadNodes = (Element) headNode.getElementsByTagName("extraHeadModels").item(0);
			if(extraHeadNodes.hasChildNodes())
			{
				NodeList exModList = extraHeadNodes.getElementsByTagName("model");

				for(int i=0; i<exModList.getLength(); i++)
				{
					Element curEl=(Element) exModList.item(i);

					String cExtMod = curEl.getAttributes().getNamedItem("model").getNodeValue();
					String cExtMat = curEl.getAttributes().getNamedItem("material").getNodeValue();

					avatar.addExtraModelToHead( cExtMod, cExtMat);
				}
			}

			//ExtraNodes--GLOBAL !!!!
			Element extraGlobalNodes = (Element) avatarEl.getElementsByTagName("extraGlobalModels").item(0);
			if(extraGlobalNodes.hasChildNodes())
			{
				NodeList exModList = extraGlobalNodes.getElementsByTagName("model");

				for(int i=0; i<exModList.getLength(); i++)
				{
					Element curEl=(Element) exModList.item(i);

					String cExtMod = curEl.getAttributes().getNamedItem("model").getNodeValue();
					String cExtMat = curEl.getAttributes().getNamedItem("material").getNodeValue();

					avatar.addExtraModel( cExtMod, cExtMat);
				}
			}

			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			//Animations
			List<String> animFiles= new ArrayList<String>();

			NodeList animListNodes=avatarEl.getElementsByTagName("animationList");
			if(animListNodes.getLength()==1)
			{
				Element animationNodes= (Element) animListNodes.item(0);
				if(animationNodes.hasChildNodes())
				{
					//Log.i("AniXML","anim chld");
					NodeList animList = animationNodes.getElementsByTagName("animation");
					for(int i=0; i<animList.getLength(); i++)
					{
						Element curEl=(Element) animList.item(i);
						String animName= curEl.getAttribute("filename");

						animFiles.add(animName);
						//Log.i("AniXML",animName);
					}
				}
			}

			//navigate through animation files
			if(!animFiles.isEmpty())
			{
				for(int i=0; i<animFiles.size(); i++)
				{
					//open file
					is = assetManager.open(animFiles.get(i));

					dbFactory = DocumentBuilderFactory.newInstance();
					dBuilder = dbFactory.newDocumentBuilder();
					doc = dBuilder.parse(is);
					element = doc.getDocumentElement();
					element.normalize();

					Element animEl = doc.getDocumentElement(); //parent node
					String animName=element.getAttributes().getNamedItem("name").getNodeValue();
					List<KeyFrame> keys=new ArrayList<KeyFrame>();

					Element keyFrameNodes = (Element) animEl.getElementsByTagName("keyframeList").item(0);
					if(keyFrameNodes.hasChildNodes())
					{
						NodeList keyList = keyFrameNodes.getElementsByTagName("key");
						for(int k=0; k<keyList.getLength(); k++)
						{
							Element curEl=(Element) keyList.item(k);

							String keyTime= curEl.getAttribute("t");
							Element faceWNode = (Element) curEl.getElementsByTagName("faceWeights").item(0);
							Text faceWText = (Text) faceWNode.getFirstChild();
							String stringFaceWeights=faceWText.getNodeValue();

							String[] partsFW = stringFaceWeights.split(" ");

							float[] fw= new float[partsFW.length];
							for( int j=0; j< partsFW.length;j++)
							{
								fw[j]= Float.parseFloat(partsFW[j]);
							}

							KeyFrame key = new KeyFrame();
							key.time= Integer.parseInt(keyTime);
							key.pose= new FacePose(fw);
							keys.add(key);
						}
					}
					avatar.addAnimation(animName, keys);

					//close file
					is.close();
				}
			}
			return avatar;
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
