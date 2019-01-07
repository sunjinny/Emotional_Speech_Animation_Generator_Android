package vml.com.animation;

import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import java.io.IOException;


public class MainActivity extends AppCompatActivity {
    //TODO: Create AvatarAnimation instance
    private AvatarAnimation avatarAnimation;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //TODO: Get Fragment Manager
        AvatarFragment fragment = (AvatarFragment) getFragmentManager().findFragmentById(R.id.fragment);
        avatarAnimation = new AvatarAnimation(fragment);
        mediaPlayer = MediaPlayer.create(this, R.raw.joy);

        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AssetManager assetManager = getResources().getAssets();
                try{
                    //TODO: setAnimation with the InputStream data (.xml file from server output)
                    avatarAnimation.setAnimation(assetManager.open("joy_anim.xml"));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                mediaPlayer.start();
                while(true){
                    if(!mediaPlayer.isPlaying()) {
                        //TODO: PlayIdleMotion when the audio ends
                        avatarAnimation.playIdleMotion();
                        break;
                    }
                    //TODO: Audio Play and get the timing, pass it to the parameter
                    avatarAnimation.updateAnimation(mediaPlayer.getCurrentPosition());
                }
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