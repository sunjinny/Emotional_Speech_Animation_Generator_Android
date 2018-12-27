package vml.com.animation;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.app.FragmentActivity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.widget.Button;
import android.widget.Spinner;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // load fragment

        Button playButton = (Button) findViewById(R.id.play_btn);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Spinner spinnerAnim = (Spinner) findViewById(R.id.spinner);
                String animName = String.valueOf(spinnerAnim.getSelectedItem());
                Log.i("CLICK", animName);

                //TODO
                AvatarFragment fragment = (AvatarFragment) getFragmentManager().findFragmentById(R.id.fragment);
                AvatarAnimation avatarAnimation = new AvatarAnimation(fragment);

                avatarAnimation.setAnimation(animName);

                //fragment.mGLView.mRenderer.mAvatar.setAnimation(animName);
                //fragment.mGLView.mRenderer.mAvatar.startAnimation();

                //((TouchSurfaceView)mGLView).mRenderer.mAvatar.setAnimation(animName);
                //((TouchSurfaceView)mGLView).mRenderer.mAvatar.startAnimation();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
