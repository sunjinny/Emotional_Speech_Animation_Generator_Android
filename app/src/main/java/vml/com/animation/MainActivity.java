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


public class MainActivity extends AppCompatActivity { //for test
    //TODO: Create AvatarAnimation instance
    private AvatarAnimation avatarAnimation;
    private MediaPlayer mediaPlayer; //for test

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //TODO: Get Fragment Manager
        AvatarFragment fragment = (AvatarFragment) getFragmentManager().findFragmentById(R.id.fragment);
        avatarAnimation = new AvatarAnimation(fragment);

        mediaPlayer = MediaPlayer.create(this, R.raw.surprise); //for test


        Button button = (Button) findViewById(R.id.button); //for test
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AssetManager assetManager = getResources().getAssets(); //for test
                try{
                    //TODO: setAnimation with the animation_data from the server (to InputStream)
                    avatarAnimation.setAnimation(assetManager.open("EmoSpeech_surprise_1.0.xml"));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                mediaPlayer.start();
                while(true){ //This is just for testing the audio with animation.
                    if(!mediaPlayer.isPlaying()) {
                        //TODO: PlayIdleMotion when the audio ends
                        avatarAnimation.playIdleMotion();
                        break;
                    }
                    //TODO: Update animation by getting the current audio timing
                    avatarAnimation.updateAnimation(mediaPlayer.getCurrentPosition());
                }
            }
        });

        Button button2 = (Button) findViewById(R.id.button2); //for test
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AssetManager assetManager = getResources().getAssets(); //for test
                try{
                    //TODO: setAnimation with the animation_data from the server (to InputStream)
                    avatarAnimation.setAnimation(assetManager.open("ManAnimation/surprise.xml"));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                mediaPlayer.start();
                while(true){ //This is just for testing the audio with animation.
                    if(!mediaPlayer.isPlaying()) {
                        //TODO: PlayIdleMotion when the audio ends
                        avatarAnimation.playIdleMotion();
                        break;
                    }
                    //TODO: Update animation by getting the current audio timing
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