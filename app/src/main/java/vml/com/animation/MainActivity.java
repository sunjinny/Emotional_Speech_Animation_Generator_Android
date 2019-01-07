package vml.com.animation;

import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Spinner;
import java.io.IOException;


public class MainActivity extends AppCompatActivity {
    //TODO: AvatarAnimation variable
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
                //TODO: 아크릴이 서버로부터 xml 파일을 inputstream으로 받아서 넘겨준다고 가정.
                //String root = Environment.getExternalStorageDirectory().getAbsolutePath();
                //avatarAnimation.setAnimation(root+"/VML_DEMO/Models/Animation/Girl/joy_anim.xml");
                AssetManager assetManager = getResources().getAssets();
                try{
                    avatarAnimation.setAnimation(assetManager.open("joy_anim.xml"));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //TODO: Audio Play and get the timing, pass it to the parameter
                mediaPlayer.start();
                while(true){
                    if(!mediaPlayer.isPlaying()) {
                        avatarAnimation.playIdleMotion();
                        break;
                    }
                    avatarAnimation.updateAnimation(mediaPlayer.getCurrentPosition());
                }
            }
        });


        //TODO: For TEST!!!
        Button playButton = (Button) findViewById(R.id.play_btn);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Spinner spinnerAnim = (Spinner) findViewById(R.id.spinner);
                String animName = String.valueOf(spinnerAnim.getSelectedItem());
                avatarAnimation.setAnimationTest(animName);
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