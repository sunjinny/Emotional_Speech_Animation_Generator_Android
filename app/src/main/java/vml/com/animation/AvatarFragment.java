package vml.com.animation;

import android.content.Context;
import android.opengl.GLSurfaceView;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.MotionEventCompat;
import android.view.LayoutInflater;
import android.app.Fragment;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 *  @author Sunjin Jung
 */

public class AvatarFragment extends Fragment
{
    public VMSurfaceView mGLView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mGLView = new VMSurfaceView(this.getActivity());
        mGLView.setEGLContextClientVersion(2);
        mGLView.setRenderer(new BODemoRenderer(this.getActivity()));
        return mGLView;
    }
}

class VMSurfaceView extends GLSurfaceView
{
    public BODemoRenderer mRenderer;
    //Touch Event
    private static final int INVALID_POINTER_ID = 0;
    private int mActivePointerId = INVALID_POINTER_ID;
    private final float TOUCH_SCALE_FACTOR = 180.0f / 640;
    private float mPreviousX;
    private float mPreviousY;
    private float mScaling;
    //private ScaleGestureDetector mScaleDetector;


    public VMSurfaceView(Context context) {
        super(context);
        //mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

    @Override
    public void setRenderer(Renderer renderer)
    {
        super.setRenderer(renderer);
        mRenderer = (BODemoRenderer) renderer;
    }


    // Touch Event
    public boolean onTouchEvent(MotionEvent ev) {

        //Let the ScaleGestureDetector inspect all events.
        //mScaleDetector.onTouchEvent(ev);

        final int action = MotionEventCompat.getActionMasked(ev);

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                final int pointerIndex = MotionEventCompat.getActionIndex(ev);
                final float x = MotionEventCompat.getX(ev, pointerIndex);
                final float y = MotionEventCompat.getY(ev, pointerIndex);

                // Remember where we started (for dragging)
                mPreviousX = x;
                mPreviousY = y;
                // Save the ID of this pointer (for dragging)
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                break;
            }

            case MotionEvent.ACTION_MOVE:
            {
                // Find the index of the active pointer and fetch its position
                final int pointerIndex =
                        MotionEventCompat.findPointerIndex(ev, mActivePointerId);


                final float x = MotionEventCompat.getX(ev, pointerIndex);
                final float y = MotionEventCompat.getY(ev, pointerIndex);

                // Calculate the distance moved
                final float dx = x - mPreviousX;
                final float dy = y - mPreviousY;

                if (ev.getPointerCount()==1)
                {
                    mRenderer.mAngleX += dx * TOUCH_SCALE_FACTOR;
                    mRenderer.mAngleY += dy * TOUCH_SCALE_FACTOR;
                }
                // Remember this touch position for the next move event
                mPreviousX = x;
                mPreviousY = y;

                break;
            }

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
            {
                mActivePointerId = INVALID_POINTER_ID;
                break;
            }

            case MotionEvent.ACTION_POINTER_UP:
            {

                final int pointerIndex = MotionEventCompat.getActionIndex(ev);
                final int pointerId = MotionEventCompat.getPointerId(ev, pointerIndex);

                if (pointerId == mActivePointerId) {
                    // This was our active pointer going up. Choose a new
                    // active pointer and adjust accordingly.
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    mPreviousX = MotionEventCompat.getX(ev, newPointerIndex);
                    mPreviousY = MotionEventCompat.getY(ev, newPointerIndex);
                    mActivePointerId = MotionEventCompat.getPointerId(ev, newPointerIndex);
                }
                break;
            }
        }
        return true;
    }
}