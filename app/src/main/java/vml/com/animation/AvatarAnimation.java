package vml.com.animation;

import android.app.FragmentManager;

/**
 *  @author Sunjin Jung
 */

public class AvatarAnimation {
    private AvatarFragment mFragment;
    //private

    public AvatarAnimation(AvatarFragment fragment){
        mFragment = fragment;
    }

    public void setAnimation(String animationName){
        mFragment.mGLView.mRenderer.mAvatar.setAnimation(animationName);
        mFragment.mGLView.mRenderer.mAvatar.startAnimation();

    }
}
