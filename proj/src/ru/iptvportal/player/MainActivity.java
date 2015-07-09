package ru.iptvportal.player;

import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.videolan.libvlc.VlcPlayer;

import ru.iptvportal.player.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
//import android.view.ViewGroup.LayoutParams;
//import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;
//import android.widget.VideoView;


public class MainActivity extends Activity implements OnItemSelectedListener,OnClickListener,OnScrollListener,ViewManager {

	 private AtomPayListAdapter adapter;
	 private AtomPayListAdapter _t_adapter;
	 private VideoApp app;
	 private EpgViewCtl _epg_vc;
	 private OnSwipeTouchListener _swipe_detector;
	 private SeekBar _player_pb;
	 private Timer _hide_tm=new Timer();
	 private Animation _leftout_anim;
	 private Animation _rightout_anim;
	 private Animation _fadein_anim;
	 private int _selected_profile=-1;
	 private boolean _profile_pass_ok=false;
	 boolean _data_ok=false;
	 boolean _select_topic=true;
	 VlcPlayer vv;
	 ListView _ch_lv;
	 ListView _t_lv;
	 boolean _ignore_scroll=false;
	 int _first_vis_ch=-1;
//	 Spinner _t_sp;
	 TopicClick _top_click;
	 
	 class AnimationChain implements AnimationListener{
		 private int _id_to_show;
		 private int _id_to_hide;
		 AnimationChain(int id_to_hide,int id_to_show){_id_to_show=id_to_show; _id_to_hide=id_to_hide;}
		@Override
		public void onAnimationEnd(Animation animation) {
			if (_id_to_hide!=0)
				findViewById(_id_to_hide).setVisibility(View.GONE);
			if (_id_to_show!=0 && (_id_to_show!=R.id.player_ctl_l || vv.isSeekable()))
			{
				findViewById(_id_to_show).setVisibility(View.VISIBLE);
				findViewById(_id_to_show).startAnimation(_fadein_anim);
			}
		}
		@Override
		public void onAnimationRepeat(Animation animation) {}
		@Override
		public void onAnimationStart(Animation animation) {}
	 }
	 
	 class TopicClick implements OnClickListener
	 {
		@Override
		public void onClick(View v) {
			
			//Log.d("MAINACT","top click");
			LVTopicItem t=(LVTopicItem)v.getTag();
			for (int i=0;i<adapter.getCount();++i)
			{
				//Log.d("MAINACT","top click t="+t.getTopic().getName());

				LVComItem ci=(LVComItem)adapter.getItem(i);
				if (ci.getType()==LVComItem.TOPIC_TYPE && t.getTopic().getName().equalsIgnoreCase(((LVTopicItem)ci).getTopic().getName()))
				{
					//Log.d("MAINACT","top click found t="+t.getTopic().getName());
					_ignore_scroll=true;
					refreshTopicSelecton(i,_ch_lv.getLastVisiblePosition()-_ch_lv.getFirstVisiblePosition());
					_ch_lv.setSelection(i);
					//_ch_lv.invalidate();
					break;
				}
			}
		
		}
	 }
	 
	 @Override
	 public boolean dispatchTouchEvent (MotionEvent ev) {
		 
		 boolean sw=_swipe_detector.onTouch(null, ev);
		 
		 return sw || super.dispatchTouchEvent(ev) ;
		 
	 }
	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_main);

		_swipe_detector=new OnSwipeTouchListener(this){
			@Override
			public void onSwipeLeft() {
				app.setNextView(_leftout_anim);
			}
			@Override
			public void onSwipeRight() {
				app.setPrevView(_rightout_anim);
				
			}
			@Override
			public void onLongTouch() {
				Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
				
				v.vibrate(300);
				app.setViewState(AppViewState.LOGIN, null);
//				openOptionsMenu();
			}
		};
		_leftout_anim=AnimationUtils.loadAnimation(getApplicationContext(), R.animator.leftout_anim);
		_rightout_anim=AnimationUtils.loadAnimation(getApplicationContext(), R.animator.rightout_anim);
		_fadein_anim=AnimationUtils.loadAnimation(getApplicationContext(), R.animator.fadein_anim);
		
		app= (VideoApp)this.getApplication();

		SurfaceView sv= (SurfaceView) findViewById(R.id.videoView1);
		vv=new VlcPlayer(sv,this);
		app.setVideoPlayer(vv);
		
		app.getAppService().setPortalUrl(PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getString("url_portal", "https://go.iptvportal.ru"));
		app.setViewManager(this);
		//Log.d("MAINACT","START" + app.getAppConfig().getChannelsConfig());
		adapter = new AtomPayListAdapter(MainActivity.this);
		_ch_lv = (ListView)findViewById(R.id.listView1);
		_ch_lv.setAdapter(adapter);	
		_ch_lv.setOnScrollListener(this);
		
		_t_lv =(ListView) findViewById(R.id.topicList);
		_top_click=new TopicClick();
		_t_adapter=new AtomPayListAdapter(MainActivity.this);
		_t_lv.setAdapter(_t_adapter);

		//findViewById(R.id.ma_rel_l).getViewTreeObserver().addOnGlobalLayoutListener(new ReLayout());

		ImageButton plb=(ImageButton)findViewById(R.id.player_play);
		plb.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				ImageButton b=(ImageButton) arg0;
				vv.togglePause();
				if (vv.isPaused())
					b.setImageResource(R.drawable.play_icon);
				else
					b.setImageResource(R.drawable.pause_icon);
			}
		});
		_player_pb=(SeekBar)findViewById(R.id.player_bar);
		_player_pb.setMax(1000);
		_player_pb.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){

			@Override
			public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
				if (arg2)
					app.getAppConfig().getCurChannel().setPosition(vv, arg1);
			}
			@Override
			public void onStartTrackingTouch(SeekBar arg0) {}
			@Override
			public void onStopTrackingTouch(SeekBar arg0) {
			}
			
		});
/*		vv.setPlayerEventsListener(new VlcPlayer.PlayerEventsListener(){
			@Override
			public void onPositionChanged() {
				int pos=(int)(vv.getPosition()*1000);
				if (pos>=0)
					_player_pb.setProgress(pos);
			}
		});*/
		//Log.d("MAINACT","1");
		ListView elv=(ListView)findViewById(R.id.epg_listView);
		//Log.d("MAINACT","2");
		hideView(AppViewState.EPG,null);
		hideView(AppViewState.VIDEO,null);
		hideView(AppViewState.INTERFACE,null);
		hideView(AppViewState.LOGIN,null);
		findViewById(R.id.loginprogressBarEPG).setVisibility(View.INVISIBLE);

		//Log.d("MAINACT","4");
		sv.setOnTouchListener(new SurfaceView.OnTouchListener(){
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				if (app.getViewState()==AppViewState.VIDEO)
					onViewVideo(AppViewState.VIDEO,null);
				return false;
			}}); 

		
		SearchView tv = (SearchView)findViewById(R.id.channelSearch);
		
		int id = tv.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
		TextView textView = (TextView) tv.findViewById(id);
		if (textView!=null)
			textView.setTextColor(0xfff0f0f0);
		
		tv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			
			@Override
			public boolean onQueryTextSubmit(String query) {
				return false;
			}
			
			@Override
			public boolean onQueryTextChange(String newText) {
				  adapter.setFilter(newText);
				return false;
			}
		});
		
		if (getIntent().getBooleanExtra("EXIT", false)) {
			//Log.d("MAINACT","START exit");
		    finish();
		    return;
		}

		if (!app.getAppService().isLoggedIn())
		{
			View l=getView(AppViewState.LOGIN);
			l.setVisibility(View.VISIBLE);
			TextView t=(TextView)l.findViewById(R.id.loginTitle);
			t.setText("Вход не выполнен");

			//Log.d("APP","LOGIN" );
		//	Intent ni=new Intent(this,LoginActivity.class);
		// startActivity(ni);
			return;
		}
		
		getView(AppViewState.INTERFACE).setVisibility(View.VISIBLE);
		app.setViewState(AppViewState.INTERFACE, null);
		
		if (checkConfig())
		{
			_selected_profile=app.getAppConfig().getUserProfiles().getCurrentProfileNum();
			initData();
			Spinner esp=(Spinner)findViewById(R.id.epg_spinner);
			//Log.d("MAINACT","5");
			_epg_vc=new EpgViewCtl(app,this,esp,elv,(ProgressBar)findViewById(R.id.progressEpgLoading));
			//Log.d("MAINACT","6");

			final Handler hnd=new Handler();
			_hide_tm=new Timer();
			_hide_tm.schedule(new TimerTask(){
				@Override
				public void run() {
					hnd.post(new Runnable(){
						public void run(){
							ChannelsConfig.Channel ch= app.getAppConfig().getCurChannel();
							if (ch!=null)
							{
								if (vv.isSeekable() && ch.playsArchive())
									_player_pb.setProgress((int)(vv.getPosition()*1000));
								else
								{ 	
									int pos=ch.getPosition(vv);
									if (pos>=0)
										_player_pb.setProgress(pos);
								}
							}
						}
					});
				}
			}, 1000,1000);
			
		}
	}
	
	public void onSubmitLogin(View v)
	{
		EditText l=(EditText) findViewById(R.id.logineditText1);
		EditText p=(EditText) findViewById(R.id.logineditText2);
	
		app.getAppService().login(this,l.getText().toString(),p.getText().toString());
		ProgressBar pb=(ProgressBar) findViewById(R.id.loginprogressBarEPG);
		pb.setVisibility(View.VISIBLE);
		
	}
	
	public void onShowSettings (View v)
	{
		showSettingsDialog();
	}
	
	@Override 
	public boolean onCreateOptionsMenu(Menu menu)
	{
//		if (app.getAppService().isLoggedIn())
	//	{
			MenuInflater mi=getMenuInflater();
			mi.inflate(R.menu.main, menu);
			return true;
		//}
		//return false;
	}

	public Activity getActivity(){return this;}

	private AlertDialog createCustomDialog(int title_id,int ok_id,int lid,DialogInterface.OnClickListener cl)
	{
		AlertDialog.Builder b2 = new AlertDialog.Builder(getActivity());
		LayoutInflater li= getActivity().getLayoutInflater();
		b2.setTitle(title_id)
			.setView(li.inflate(lid,null))
			.setNegativeButton(R.string.cancel_button_title, null)
			.setPositiveButton(ok_id, cl);
		return b2.create();
		
	}
	
	public void createProfilePass(int title_id)
	{
		createCustomDialog(title_id,R.string.ok_button_title,R.layout.profile_create_pass,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						AlertDialog ad=(AlertDialog)dialog;
						EditText p=(EditText) ad.findViewById(R.id.profile_password);
						EditText pa=(EditText) ad.findViewById(R.id.profile_password_again);
						_profile_pass_ok=(p.getText().toString().compareTo(pa.getText().toString())==0);
						if (_profile_pass_ok)
						{
							app.getAppConfig().getUserProfiles().setProfilePass(_selected_profile,p.getText().toString());
							app.getAppService().saveProfile(_selected_profile);
							changeProfile();
						}
						else
							createProfilePass(R.string.profile_retry_pass_dialog_title);
					}
				}).show();
		
	}
	private void checkProfilePass()
	{
		createCustomDialog(R.string.profile_pass_dialog_title,R.string.ok_button_title,R.layout.profile_pass,
			new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					AlertDialog ad=(AlertDialog)dialog;
					TextView p=(TextView) ad.findViewById(R.id.profile_password);
					_profile_pass_ok=app.getAppConfig().getUserProfiles().isPassCorrect(_selected_profile, p.getText().toString());
					changeProfile();
				}
			}).show();
	}

	private void changeProfile()
	{
		if (!_profile_pass_ok && app.getAppConfig().getUserProfiles().isPassNeeded(_selected_profile))
		{
			if (app.getAppConfig().getUserProfiles().passIsSet(_selected_profile))
				checkProfilePass();
			else
				createProfilePass(R.string.profile_create_pass_dialog_title);
				
		}
		else
		{
			app.getAppConfig().getUserProfiles().setCurrentProfile(_selected_profile);
			app.getAppService().saveTerminalSettings();
		}
		
	}
	
	@Override 
	public boolean onOptionsItemSelected(MenuItem mi)
	{
		switch(mi.getItemId())
		{
			case R.id.item1:
				app.setViewState(AppViewState.LOGIN, null);
				break;
			case R.id.item2:
				_profile_pass_ok=false;
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(R.string.profile_dialog_title).setSingleChoiceItems(app.getAppConfig().getUserProfiles().getProfilesNames(),app.getAppConfig().getUserProfiles().getCurrentProfileNum(),
						new DialogInterface.OnClickListener(){
							@Override
							public void onClick(DialogInterface dialog,int which) {
								_selected_profile=which;
							}}
						).setPositiveButton(R.string.select_button_title, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								changeProfile();
							}
						}).setNegativeButton(R.string.close_button_title, null);
				builder.create().show();

				break;
			case R.id.item3:
					showSettingsDialog();
				break;
			default:
				break;
		}
		return true;
	}
	public void applySettings(View button)
	{
		_applySettings(button.getRootView());
	}
	void _applySettings(View host)
	{
		Spinner psp=(Spinner) host.findViewById(R.id.user_profile_selector);
		if (psp!=null)
		{
			int sp=psp.getSelectedItemPosition();
			if (sp!=Spinner.INVALID_POSITION && _selected_profile!=sp)
			{
				_selected_profile=sp;
				changeProfile();
			}
		}
		String p="http://"+((EditText) host.findViewById(R.id.udp_proxy)).getText().toString();
		String p_url="https://"+((EditText) host.findViewById(R.id.portal_url)).getText().toString();
		boolean u=((Switch) host.findViewById(R.id.use_proxy)).isChecked();
		if (p_url.compareTo(app.getAppService().getPortalUrl())!=0 || p.compareTo(app.getAppConfig().getTerminalSettings().getUdpProxy())!=0 || u!=app.getAppConfig().getTerminalSettings().isUdpProxyUsed())
		{
			app.getAppConfig().getTerminalSettings().setUdpProxy(p);
			app.getAppConfig().getTerminalSettings().setUdpProxyUsed(u);
			app.getAppService().setPortalUrl(p_url);
			app.getAppService().saveTerminalSettings();
			SharedPreferences sp= PreferenceManager.getDefaultSharedPreferences(getBaseContext());
			SharedPreferences.Editor ped=sp.edit();
			ped.putString("url_portal", p_url);
			boolean cr=ped.commit();
		}
		
	}
	
	void fillSettingsView(View parent)
	{
		Spinner psp=(Spinner) parent.findViewById(R.id.user_profile_selector);
		if (psp!=null)
				psp.setSelection(_selected_profile);
		((Switch)parent.findViewById(R.id.use_proxy)).setChecked(app.getAppConfig().getTerminalSettings().isUdpProxyUsed());
		((EditText)parent.findViewById(R.id.udp_proxy)).setText(app.getAppConfig().getTerminalSettings().getUdpProxy().replace("http://", ""));
		((EditText)parent.findViewById(R.id.portal_url)).setText(app.getAppService().getPortalUrl().replace("https://", ""));
	}
	
	void showSettingsDialog()
	{
		AlertDialog ad=createCustomDialog(R.string.udp_proxy_dialog_title,R.string.ok_button_title,R.layout.terminal_settings,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						_applySettings(((AlertDialog)dialog).findViewById(R.id.terminal_settings_l));						
					}
				});
		ad.show();
		fillSettingsView(ad.findViewById(R.id.terminal_settings_l));

	}	
	private boolean checkConfig()
	{
		if (app.getAppConfig().getChannelsConfig()==null)
		{
			if (!app.getAppService().isConfigLoading())
				app.getAppService().loadConfig(this);
			return false;
		}
		return true;
	}
	
	@Override
	public void onBackPressed()
	{		
		vv.release();
		super.onBackPressed();	
	}
		
	private void initData()
	{
		if (!_data_ok)
		{
			//Log.d("MAINACT","initData");
			refreshChannelsList();
			ArrayList<ChannelsConfig.Topic> ts=app.getAppConfig().getChannelsConfig().getTopics();
			_t_adapter.clear();
			for (int i=0;i<ts.size();++i)
			{
				_t_adapter.add(new LVTopicItem(ts.get(i),R.layout.topic_item,_top_click));//, 0);
			}
			
			_data_ok=true;
			setCurrentChannelName();
			/*
			if (app.getAppConfig().getCurChannel()!=null)
			{
				refreshEPGDisplay(app.getAppConfig().getCurChannel());
			}*/
		}
	}

	private void setCurrentChannelName()
	{
		TextView tv = (TextView)findViewById(R.id.currentChannel);
		
		tv.setText(app.getAppConfig().getCurChannel().getName());
		
	}
	private void refreshChannelsList(){
		//Log.d("MAINACT","refreshChannelsList");
		adapter.clear();
		ChannelsConfig.Topic ct=app.getAppConfig().getCurTopic();
		ArrayList<ChannelsConfig.Topic> allt=app.getAppConfig().getChannelsConfig().getTopics();
		//if (ct.equals(allt.get(0)))
		//{/// if current topic is first topic (all channels topic)
			for (int i=0;i<allt.size();++i)
			{
				adapter.add(new LVTopicItem(allt.get(i),R.layout.topic_list_item,null));
				addTopicChannelsToList(allt.get(i));
			}
		//}
		//else
			//addTopicChannelsToList(ct);
		
		//Log.d("MAINACT","refreshChannelsList ok");
		
	}
	private void addTopicChannelsToList(ChannelsConfig.Topic ct)
	{
		ArrayList<ChannelsConfig.Channel> chl=ct.getChannels();
		for (int i=0;i<chl.size();++i)
		{
			//Log.d("MAINACT","refreshChannelsList " + i);
			adapter.add(new LVChannelItem(chl.get(i),ct,R.layout.atom_pay_list_item,this,app));
			//adapter.insert(new AtomPayment(chl.get(i)),0);//.get(i).getMrl(), 0,chl.get(i).getIconBytes()), 0);
		}
		
	}
	@Override
	protected void onDestroy(){
		_player_pb.setOnSeekBarChangeListener(null);
		super.onDestroy();
	}
	
	@Override
	protected void onResume()
	{
		//Log.d("MAINACT","onresume ");
		super.onResume();
		if (app.getAppService().isLoggedIn() && checkConfig())
		{
			//Log.d("MAINACT","onresume initData");
			initData();
			if (!app.getAppConfig().getCurChannel().startPlay(vv,
														new Date(),
														app.getAppConfig().getUserProfiles().getCurrentProfile(),
														app.getAppConfig().getTerminalSettings()))
			{
				Toast toast = Toast.makeText(this, 
						   "Ограничение 18+. Включите профиль 18+", Toast.LENGTH_SHORT); 
						toast.show(); 
			}
		//	String v=app.getAppConfig().getCurChannel().getMrl();//"http://192.168.101.29/hls/TNT/TNT.m3u8");
			////Log.d("MAINACT","initData "+v);
//			vv.setVideoURI(Uri.parse(v));
	//		vv.start();
		}
		//Log.d("MAINACT","onresume ok");
	}
	@Override
	public void onClick(View v)
	{
		LVComItem ci=(LVComItem) v.getTag();
		if (ci instanceof LVChannelItem)
		{
			ChannelsConfig.Channel cch=((LVChannelItem)ci).getChannel();
			app.getAppConfig().setCurChannel(cch);
			setCurrentChannelName();
			hideView(AppViewState.VIDEO,null);
			
			if (!cch.startPlay(vv, new Date(),app.getAppConfig().getUserProfiles().getCurrentProfile(),
															app.getAppConfig().getTerminalSettings()))
			{
				Toast toast = Toast.makeText(this, 
						   "Ограничение 18+. Включите профиль 18+", Toast.LENGTH_SHORT); 
						toast.show(); 
			}
			adapter.notifyDataSetChanged();
		}	
	}

	/*
	public void refreshEPGDisplay(ChannelsConfig.Channel ch){
		//Log.d("MAINACT","refreshEPGDisplay");
		EPGData epg=ch.getCurrentEpgData();
		if (epg!=null)
		{
			//Log.d("MAINACT","refreshEPGDisplay epg ok");
			TextView tv=(TextView)findViewById(R.id.textViewEPG);
			
			//Log.d("MAINACT","refreshEPGDisplay epg ok" + tv.getText() + tv);
			tv.setText(epg.getTitle());
			tv.invalidate();

			tv=(TextView)findViewById(R.id.textViewTStart);
			tv.setText(epg.getStartTime());
			tv=(TextView)findViewById(R.id.textViewTStop);
			tv.setText(epg.getStopTime());
			ProgressBar bar=(ProgressBar)findViewById(R.id.progressBarEPG);
			int secs=(int)((epg.getStop().getTime()- epg.getStart().getTime())/1000);
			bar.setMax(secs);
			bar.setProgress((int)((new Date().getTime() - epg.getStart().getTime())/1000));
			//Log.d("MAINACT","refreshEPGDisplay " + epg.getStop().getTime() +" "+epg.getStart().getTime() +" " +new Date().getTime());   
			//Log.d("MAINACT","refreshEPGDisplay epg text set " + tv.getText() + " " + ch.getName() + " "+
			bar.getMax()+"="+bar.getProgress());
		
		}
		else
		{
			//Log.d("MAINACT","refreshEPGDisplay epg upload");
			app.getAppService().loadEPG(this,ch);
		}
		
	}
	*/
	public void onVideoClicked(View v)
	{
		//Log.d("VIDEO","CLICKED" );
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		//Log.d("MAINACT", "topsel#");
		if (checkConfig() && _select_topic)
		{
			LVTopicItem t=(LVTopicItem)_t_adapter.getItem(position);
			for (int i=0;i<adapter.getCount();++i)
			{
				LVComItem ci=(LVComItem)adapter.getItem(i);
				if (ci.getType()==LVComItem.TOPIC_TYPE && t.getTopic().getName().equalsIgnoreCase(((LVTopicItem)ci).getTopic().getName()))
				{
					_ch_lv.setSelection(i);
					_ch_lv.invalidate();
					break;
				}
			}
		
			
			
			/*
			String tn=_t_adapter.getItem(position);
			ChannelsConfig.Topic t=app.getAppConfig().getChannelsConfig().findTopic(tn);
			if (t!=null)
			{
				app.getAppConfig().setCurTopic(t);
				if (_data_ok)
					refreshChannelsList();
				else
					initData();*/
			//}
			//Log.d("MAINACT", "topsel");
			
		}
		_select_topic=true;
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) 
	{
//		//Log.d("MAINACT","onscroll "+ firstVisibleItem + " of "+visibleItemCount);
		if (_ignore_scroll==false &&_first_vis_ch!=firstVisibleItem)
		{
			int tp=refreshTopicSelecton(firstVisibleItem,visibleItemCount);
	//		//Log.d("MAINACT","!!!onscroll");
			if(tp>=0)
			{
				_t_lv.setSelection(tp);
			}
		}
		_first_vis_ch=firstVisibleItem;
		_ignore_scroll=false;
		
		
	}
	
	private int refreshTopicSelecton(int firstVisibleItem,int visibleItemCount){
		int tp=0;
		if ( visibleItemCount>0)
		{
			LVComItem itm=(LVComItem) adapter.getItem(firstVisibleItem);
		//	//Log.d("MAINACT","onscroll itm "+ itm);
			if (itm!=null)
			{
				int i;
				int topics=0;
				int till=firstVisibleItem+visibleItemCount;
				till=till> adapter.getCount()? adapter.getCount():till;
				for (i=firstVisibleItem;i<till;++i)
				{
					LVComItem cm=(LVComItem)adapter.getItem(i);
					if(cm.getType()==LVComItem.TOPIC_TYPE)
						++topics;
				}
	//			//Log.d("MAINACT","onscroll vt "+ topics);
				
				ChannelsConfig.Topic t=null;
				if (itm.getType()==LVComItem.CHANNEL_TYPE)
				{
					t=((LVChannelItem)itm).getTopic();
					++topics;
				}
				else
				if (itm.getType()==LVComItem.TOPIC_TYPE)
				{
					t=((LVTopicItem)itm).getTopic();
				}
//				//Log.d("MAINACT","onscroll cvt "+ topics);
				
				if(t!=null)
				{
					i=0;
					LVTopicItem ti=null;
					for (;i<_t_adapter.getCount();++i)
					{
						ti=(LVTopicItem)_t_adapter.getItem(i);
				//		//Log.d("MAINACT","onscroll ft "+ ti.getTopic().getName());
						if (ti.getTopic().getName().equalsIgnoreCase(t.getName()))
						{
							break;
						}
						ti.setSelected(false);
					}
					tp=i;
			//		//Log.d("MAINACT","onscroll fi "+ tp);
					for (;i<_t_adapter.getCount() && topics>0;++i,--topics)
					{
		//				//Log.d("MAINACT","onscroll ts "+ i);
						((LVTopicItem)_t_adapter.getItem(i)).setSelected(true);
					}						
					for (;i<_t_adapter.getCount();++i)
					{
	//					//Log.d("MAINACT","onscroll tail "+ i);
						((LVTopicItem)_t_adapter.getItem(i)).setSelected(false);
					}

//					//Log.d("MAINACT","onscroll "+_t_adapter.getCount()+" t "+ t +" "+i + " sel " +_t_lv.getSelectedItemPosition());
					_t_adapter.notifyDataSetChanged();
//					if (i<_t_adapter.getCount() && _t_lv.getSelectedItemPosition()!=i)
//					{
						
	//				}
				}
			}
			return tp;
		}
		return -1;
		
		
	}
	
	public void toggleInterface(View v)
	{
		app.setViewState(AppViewState.VIDEO,null);
	}
	public void toggleEpg(View v)
	{
//		Intent ni=new Intent(v.getContext(),EpgActivity.class);
	//	startActivity(ni);
		app.setViewState(AppViewState.EPG,null);
		return;
		
	}
	
	private View getView(AppViewState v)
	{
		View view=null;
		switch (v)
		{
			case INTERFACE:
				view=findViewById(R.id.interface_l);
			break;
			case EPG:
				view=findViewById(R.id.epg_l);
			break;
			case VIDEO:
				view=findViewById(R.id.player_ctl_l);
			break;
			case LOGIN:
				view=findViewById(R.id.login_l);
			break;
				
		}
		return view;
		
	}	
	public void hideView(AppViewState v,Animation a)
	{
		if (v==AppViewState.VIDEO)
		{
			try{
			_hide_tm.cancel();
			_hide_tm.purge();
			}catch(Exception e){}
		}
		View view=getView(v);
	
		if (view!=null)
		{
			if (a!=null)
				view.startAnimation(a);
			else
				view.setVisibility(View.GONE);
		}
			
	}
	
	private boolean animateTransaction(int id,AppViewState from,Animation a)
	{
		if(a!=null)
		{
			View view=getView(from);
			if (from==AppViewState.VIDEO)
				view.setVisibility(View.VISIBLE);
			a.setAnimationListener(new AnimationChain(getView(from).getId(),id));
		}
		hideView(from,a);
		
		return a!=null;
		
	}
	
	@Override
	public void onViewInterface(AppViewState from,Animation a) {

		if (!animateTransaction(R.id.interface_l,from,a))
			findViewById(R.id.interface_l).setVisibility(View.VISIBLE);
	}

	@Override
	public void onViewEpg(AppViewState from,Animation a) {
		_epg_vc.syncChannel();
		if (!animateTransaction(R.id.epg_l,from,a))
			findViewById(R.id.epg_l).setVisibility(View.VISIBLE);
		
	}

	@Override
	public void onViewVideo(AppViewState from,Animation a) {
		boolean animated=animateTransaction(R.id.player_ctl_l,from,a);
			
		//if (vv.isSeekable())
		//{
			final Handler hnd=new Handler();
			_hide_tm=new Timer();
			_hide_tm.schedule(new TimerTask(){
				@Override
				public void run() {
					hnd.post(new Runnable(){
						public void run(){hideView(AppViewState.VIDEO,null);}
					});
				}
			}, 5000);
			if (!animated)
				findViewById(R.id.player_ctl_l).setVisibility(View.VISIBLE);

		//}
	}
	@Override
	public void onViewLogin(AppViewState from,Animation a)
	{
		animateTransaction(R.id.login_l,from,null);
		View l=findViewById(R.id.login_l);
		TextView t=(TextView)l.findViewById(R.id.loginTitle);
		if (app.getAppService().isLoggedIn())
			t.setText("Вход выполнен под именем "+app.getAppService().getCurrentUserName());
		else
			t.setText("Вход не выполнен.");
		fillSettingsView(l);
		l.setVisibility(View.VISIBLE);
	
	}
	
}
