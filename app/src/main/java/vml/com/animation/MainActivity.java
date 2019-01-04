package vml.com.animation;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Spinner;


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
        mediaPlayer = MediaPlayer.create(this, R.raw.sad);


        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: 아크릴이 서버로부터 받아온 xml을 저장소에 저장하였다고 가정하고 /(저장하지않고 바로 input stream 줄 것 같지만)/ 여기서 경로 알려주면 xml 파일을 받아옴
                //TODO: 아크릴에서 서버로부터 받아올 때, 우리에게 넘겨주는 함수 인자로 input stream을 주는지, string을 주는지 확인이 필요.
                String root = Environment.getExternalStorageDirectory().getAbsolutePath();
                avatarAnimation.setAnimation(root+"/VML_DEMO/Models/Animation/Girl/sad.xml");

                Log.i("TEST","-------------------------------------------------------Audio play");

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