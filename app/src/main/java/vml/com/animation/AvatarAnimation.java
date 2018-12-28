package vml.com.animation;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import vml.com.vm.utils.FacePose;
import vml.com.vm.utils.KeyFrame;

/**
 *  @author Sunjin Jung
 */

public class AvatarAnimation {
    private AvatarFragment mFragment;
    private String animName;

    public AvatarAnimation(AvatarFragment fragment){
        mFragment = fragment;
    }

    public void updateAnimation(float time){

    }

    //TODO: For TEST!!!
    public void setAnimationTest(String animationName) {
        mFragment.mGLView.mRenderer.mAvatar.setAnimation(animationName);
        mFragment.mGLView.mRenderer.mAvatar.startAnimation();
    }

    public void setAnimation(String animationName){
        //mFragment.mGLView.mRenderer.mAvatar.setAnimation(animationName);
        //mFragment.mGLView.mRenderer.mAvatar.startAnimation();

        try {
            //open file
            FileInputStream is = new FileInputStream(new File(animationName));
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(is);
            Element element = doc.getDocumentElement();
            element.normalize();

            /////////////////////////////////////////////////////////////////////////////////////////
            //Requirements!
            Element animEl = doc.getDocumentElement(); //parent node
            animName = element.getAttributes().getNamedItem("name").getNodeValue();
            List<KeyFrame> keys = new ArrayList<KeyFrame>();

            //Log.i("AnimXML2", "name "+animName);

            Element keyFrameNodes = (Element) animEl.getElementsByTagName("keyframeList").item(0);
            if (keyFrameNodes.hasChildNodes()) {


                NodeList keyList = keyFrameNodes.getElementsByTagName("key");
                for (int k = 0; k < keyList.getLength(); k++) {
                    Element curEl = (Element) keyList.item(k);

                    String keyTime = curEl.getAttribute("t");
                    Element faceWNode = (Element) curEl.getElementsByTagName("faceWeights").item(0);
                    Text faceWText = (Text) faceWNode.getFirstChild();
                    String stringFaceWeights = faceWText.getNodeValue();

//							Element mouthWNode = (Element) curEl.getElementsByTagName("mouthWeights").item(0);
//							Text mouthWText = (Text) mouthWNode.getFirstChild();
//							String stringmouthWeights=mouthWText.getNodeValue();

                    String[] partsFW = stringFaceWeights.split(" ");
//							String[] partsMW = stringmouthWeights.split(" ");


                    //Log.i("Vise", "face n:"+partsMW.length+" "+ stringFaceWeights );
                    //Log.i("Vise", " mouth n:"+partsFW.length+" "+ stringmouthWeights );

                    float[] fw = new float[partsFW.length];
                    for (int j = 0; j < partsFW.length; j++) {
                        fw[j] = Float.parseFloat(partsFW[j]);
                    }
//							float[] mw= new float[partsMW.length];
//							for( int j=0; j< partsMW.length;j++)
//							{
//								mw[j]= Float.parseFloat(partsMW[j]);
//							}

                    KeyFrame key = new KeyFrame();
                    key.time = Integer.parseInt(keyTime);

                    //Log.i("AnimXML", "t ="+key.time);
//							key.pose= new FacePose(fw,mw);
                    key.pose = new FacePose(fw);

                    keys.add(key);
                    ///////////////////////////////////////////////////////////////////////

                }
            }
            mFragment.mGLView.mRenderer.mAvatar.addAnimation(animName, keys);

            //close file
            is.close(); //close anim file

        } catch (SAXException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mFragment.mGLView.mRenderer.mAvatar.setAnimation(animName);
        mFragment.mGLView.mRenderer.mAvatar.startAnimation();
    }
}
