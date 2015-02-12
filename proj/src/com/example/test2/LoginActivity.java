package com.example.test2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ProgressBar;

public class LoginActivity extends Activity {
	private VideoApp app;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//Log.d("LoginActivity","LOGIN IN" );
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_login);
		app= (VideoApp)this.getApplication();
		if (app.getAppService().isLoggedIn())
			finish();
		ProgressBar pb=(ProgressBar) findViewById(R.id.progressBarEPG);
		pb.setVisibility(View.INVISIBLE);
	}
	
	public void onSubmitLogin(View v)
	{
		//Log.d("LoginActivity","LOGIN submit" );
		EditText l=(EditText) findViewById(R.id.editText1);
		EditText p=(EditText) findViewById(R.id.editText2);
		
		app.getAppService().login(this,l.getText().toString(),p.getText().toString());
		ProgressBar pb=(ProgressBar) findViewById(R.id.progressBarEPG);
		pb.setVisibility(View.VISIBLE);
	}
	
	@Override
	public void onBackPressed()
	{
		Intent intent = new Intent(getApplicationContext(), MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra("EXIT", true);
		startActivity(intent);
		
	}
}
