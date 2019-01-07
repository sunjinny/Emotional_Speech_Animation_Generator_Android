package vml.com.animation;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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

    public void playIdleMotion(){
        mFragment.mGLView.mRenderer.mAvatar.setNeutralFace();
    }

    public void updateAnimation(int time){
        //time in milliseconds
        mFragment.mGLView.mRenderer.mAvatar.doBlinking(false);
        mFragment.mGLView.mRenderer.mAvatar.updateAudioTiming(time);
    }

    public void setAnimation(InputStream animation){
        try{
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(animation);
            Element element = doc.getDocumentElement();
            element.normalize();

            Element animEl = doc.getDocumentElement();
            animName = element.getAttributes().getNamedItem("name").getNodeValue();
            List<KeyFrame> keys = new ArrayList<KeyFrame>();

            Element keyFrameNodes = (Element) animEl.getElementsByTagName("keyframeList").item(0);
            if (keyFrameNodes.hasChildNodes()) {
                NodeList keyList = keyFrameNodes.getElementsByTagName("key");
                for (int k = 0; k < keyList.getLength(); k++) {
                    Element curEl = (Element) keyList.item(k);

                    String keyTime = curEl.getAttribute("t");
                    Element faceWNode = (Element) curEl.getElementsByTagName("faceWeights").item(0);
                    Text faceWText = (Text) faceWNode.getFirstChild();
                    String stringFaceWeights = faceWText.getNodeValue();
                    String[] partsFW = stringFaceWeights.split(" ");

                    float[] fw = new float[partsFW.length];
                    for (int j = 0; j < partsFW.length; j++) {
                        fw[j] = Float.parseFloat(partsFW[j]);
                    }

                    KeyFrame key = new KeyFrame();
                    key.time = Integer.parseInt(keyTime);
                    key.pose = new FacePose(fw);
                    keys.add(key);
                }
            }
            mFragment.mGLView.mRenderer.mAvatar.addAnimation(animName, keys);
            animation.close();
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