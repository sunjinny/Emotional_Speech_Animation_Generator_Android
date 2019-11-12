package vml.com.animation;

import android.util.Log;

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

import vml.com.vm.avatar.VMAvatar;
import vml.com.vm.utils.FacePose;
import vml.com.vm.utils.KeyFrame;

/**
 *  @author Sunjin Jung
 */

public class AvatarAnimation {
    private AvatarFragment mFragment;
    private String animName;
    private String gender;
    private VMAvatar renderedAvatar;

    public AvatarAnimation(AvatarFragment fragment){
        mFragment = fragment;
    }

    public void playIdleMotion(){
        renderedAvatar.setNeutralFace();
    }

    public void updateAnimation(int time){
        //time in milliseconds
        renderedAvatar.doBlinking(false);
        renderedAvatar.updateAudioTiming(time);
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

            Element genderEl = (Element) animEl.getElementsByTagName("gender").item(0);
            gender = genderEl.getAttribute("gender");

            if(Integer.parseInt(gender) == 30001) {
                mFragment.mGLView.mRenderer.isMan = true;
                renderedAvatar = mFragment.mGLView.mRenderer.mAvatarMan;
            }
            else {
                mFragment.mGLView.mRenderer.isMan = false;
                renderedAvatar = mFragment.mGLView.mRenderer.mAvatar;
            }

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


                    //START seonghyeon
                    Element headNoddingNode = (Element) curEl.getElementsByTagName("headNodding").item(0);
                    Text headNoddingText = (Text) headNoddingNode.getFirstChild();
                    String stringHeadNodding = headNoddingText.getNodeValue();
                    String[] partsHN = stringHeadNodding.split(" ");
                    //END   seonghyeon

                    KeyFrame key = new KeyFrame();
                    key.time = Integer.parseInt(keyTime);
                    key.pose = new FacePose(fw);
                    //START seonghyeon
                    key.noddingValue = Float.parseFloat(partsHN[0]);
                    //END   seonghyeon
                    keys.add(key);
                }
            }
            renderedAvatar.addAnimation(animName, keys);
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
        renderedAvatar.setAnimation(animName);
        renderedAvatar.startAnimation();
    }
}