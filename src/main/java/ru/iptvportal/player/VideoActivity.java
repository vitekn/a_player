package ru.iptvportal.player;

import ru.iptvportal.player.R;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.VideoView;

public class VideoActivity extends Activity implements OnTouchListener{
	
	private VideoApp app;
	VideoView vv;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.onlyvideo);
		app= (VideoApp)this.getApplication();
		vv=(VideoView) findViewById(R.id.videoView1);
		vv.setOnTouchListener(this);
		
		String v=app.getAppConfig().getCurChannel().getMrl();//"http://192.168.101.29/hls/TNT/TNT.m3u8");
		vv.setVideoURI(Uri.parse(v));
		vv.start();
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		//Log.d("VIDEO","touched" );
		finish();
		// TODO Auto-generated method stub
		return false;
	}
}
