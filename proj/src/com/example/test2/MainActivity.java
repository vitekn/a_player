package com.example.test2;

import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.videolan.libvlc.VlcPlayer;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
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
			
			Log.d("MAINACT","top click");
			LVTopicItem t=(LVTopicItem)v.getTag();
			for (int i=0;i<adapter.getCount();++i)
			{
				Log.d("MAINACT","top click t="+t.getTopic().getName());

				LVComItem ci=(LVComItem)adapter.getItem(i);
				if (ci.getType()==LVComItem.TOPIC_TYPE && t.getTopic().getName().equalsIgnoreCase(((LVTopicItem)ci).getTopic().getName()))
				{
					Log.d("MAINACT","top click found t="+t.getTopic().getName());
					_ignore_scroll=true;
					refreshTopicSelecton(i,_ch_lv.getLastVisiblePosition()-_ch_lv.getFirstVisiblePosition());
					_ch_lv.setSelection(i);
					//_ch_lv.invalidate();
					break;
				}
			}
		
			/*for (int i=0;i<_t_adapter.getCount();++i)
			{
				LVTopicItem ti=(LVTopicItem)_t_adapter.getItem(i);
				if (t.equals(ti))
				{
					_t_lv.setSelection(i);
					_t_lv.invalidate();
				}
			}*/
//			v.setSelected(true);
	//		t.setSelected(true);
			Log.d("MAINACT","top click ok");
		
		}
	 }
	 /*
	 class ReLayout implements OnGlobalLayoutListener {
		 @Override
		  public void onGlobalLayout() {
			 View lo=findViewById(R.id.ma_rel_l);
		   int width = lo.getWidth();
		   int height = lo.getHeight();
		   Log.d("MAINACT","dims" + width + " " + height);
		   LayoutParams lp=vv.getLayoutParams();
		   lp.width=width;
		   lp.height=lp.width*9/16;
		   lp.width=width;
				
		   vv.setLayoutParams(lp);
		   vv.requestLayout();

		   lo.getViewTreeObserver().removeGlobalOnLayoutListener(this);
		 }
		 }*/
	 
	 @Override
	 public boolean dispatchTouchEvent (MotionEvent ev) {
		 
		 boolean sw=_swipe_detector.onTouch(null, ev);
		 
		 return sw || super.dispatchTouchEvent(ev) ;
		 
	 }
	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
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
		};
		_leftout_anim=AnimationUtils.loadAnimation(getApplicationContext(), R.animator.leftout_anim);
		_rightout_anim=AnimationUtils.loadAnimation(getApplicationContext(), R.animator.rightout_anim);
		_fadein_anim=AnimationUtils.loadAnimation(getApplicationContext(), R.animator.fadein_anim);
		
		app= (VideoApp)this.getApplication();
		app.setViewManager(this);
		Log.d("MAINACT","START" + app.getAppConfig().getChannelsConfig());
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
				vv.togglePause();
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
		SurfaceView sv= (SurfaceView) findViewById(R.id.videoView1);
		vv=new VlcPlayer(sv,this);
		app.setVideoPlayer(vv);
/*		vv.setPlayerEventsListener(new VlcPlayer.PlayerEventsListener(){
			@Override
			public void onPositionChanged() {
				int pos=(int)(vv.getPosition()*1000);
				if (pos>=0)
					_player_pb.setProgress(pos);
			}
		});*/
		Log.d("MAINACT","1");
		ListView elv=(ListView)findViewById(R.id.epg_listView);
		Log.d("MAINACT","2");
		hideView(AppViewState.EPG,null);
		hideView(AppViewState.VIDEO,null);
		Log.d("MAINACT","4");
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
			Log.d("MAINACT","START exit");
		    finish();
		    return;
		}

		if (!app.getAppService().isLoggedIn())
		{
			Log.d("APP","LOGIN" );
			Intent ni=new Intent(this,LoginActivity.class);
			startActivity(ni);
			return;
		}
		if (checkConfig())
		{
			initData();
			Spinner esp=(Spinner)findViewById(R.id.epg_spinner);
			Log.d("MAINACT","5");
			_epg_vc=new EpgViewCtl(app,this,esp,elv,(ProgressBar)findViewById(R.id.progressEpgLoading));
			Log.d("MAINACT","6");

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
			Log.d("MAINACT","initData");
			refreshChannelsList();
			ArrayList<ChannelsConfig.Topic> ts=app.getAppConfig().getChannelsConfig().getTopics();
			_t_adapter.clear();
			for (int i=1;i<ts.size();++i)
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
		Log.d("MAINACT","refreshChannelsList");
		adapter.clear();
		ChannelsConfig.Topic ct=app.getAppConfig().getCurTopic();
		ArrayList<ChannelsConfig.Topic> allt=app.getAppConfig().getChannelsConfig().getTopics();
		if (ct.equals(allt.get(0)))
		{/// if current topic is first topic (all channels topic)
			for (int i=1;i<allt.size();++i)
			{
				adapter.add(new LVTopicItem(allt.get(i),R.layout.topic_list_item,null));
				addTopicChannelsToList(allt.get(i));
			}
		}
		else
			addTopicChannelsToList(ct);
		
		Log.d("MAINACT","refreshChannelsList ok");
		
	}
	private void addTopicChannelsToList(ChannelsConfig.Topic ct)
	{
		ArrayList<ChannelsConfig.Channel> chl=ct.getChannels();
		for (int i=0;i<chl.size();++i)
		{
			Log.d("MAINACT","refreshChannelsList " + i);
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
		Log.d("MAINACT","onresume ");
		super.onResume();
		if (app.getAppService().isLoggedIn() && checkConfig())
		{
			Log.d("MAINACT","onresume initData");
			initData();
			app.getAppConfig().getCurChannel().startPlay(vv,new Date());
		//	String v=app.getAppConfig().getCurChannel().getMrl();//"http://192.168.101.29/hls/TNT/TNT.m3u8");
			//Log.d("MAINACT","initData "+v);
//			vv.setVideoURI(Uri.parse(v));
	//		vv.start();
		}
		Log.d("MAINACT","onresume ok");
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
			cch.startPlay(vv, new Date());
			adapter.notifyDataSetChanged();
		}	
	}

	/*
	public void refreshEPGDisplay(ChannelsConfig.Channel ch){
		Log.d("MAINACT","refreshEPGDisplay");
		EPGData epg=ch.getCurrentEpgData();
		if (epg!=null)
		{
			Log.d("MAINACT","refreshEPGDisplay epg ok");
			TextView tv=(TextView)findViewById(R.id.textViewEPG);
			
			Log.d("MAINACT","refreshEPGDisplay epg ok" + tv.getText() + tv);
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
			Log.d("MAINACT","refreshEPGDisplay " + epg.getStop().getTime() +" "+epg.getStart().getTime() +" " +new Date().getTime());   
			Log.d("MAINACT","refreshEPGDisplay epg text set " + tv.getText() + " " + ch.getName() + " "+
			bar.getMax()+"="+bar.getProgress());
		
		}
		else
		{
			Log.d("MAINACT","refreshEPGDisplay epg upload");
			app.getAppService().loadEPG(this,ch);
		}
		
	}
	*/
	public void onVideoClicked(View v)
	{
		Log.d("VIDEO","CLICKED" );
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		Log.d("MAINACT", "topsel#");
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
			Log.d("MAINACT", "topsel");
			
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
//		Log.d("MAINACT","onscroll "+ firstVisibleItem + " of "+visibleItemCount);
		if (_ignore_scroll==false &&_first_vis_ch!=firstVisibleItem)
		{
			int tp=refreshTopicSelecton(firstVisibleItem,visibleItemCount);
	//		Log.d("MAINACT","!!!onscroll");
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
		//	Log.d("MAINACT","onscroll itm "+ itm);
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
	//			Log.d("MAINACT","onscroll vt "+ topics);
				
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
//				Log.d("MAINACT","onscroll cvt "+ topics);
				
				if(t!=null)
				{
					i=0;
					LVTopicItem ti=null;
					for (;i<_t_adapter.getCount();++i)
					{
						ti=(LVTopicItem)_t_adapter.getItem(i);
				//		Log.d("MAINACT","onscroll ft "+ ti.getTopic().getName());
						if (ti.getTopic().getName().equalsIgnoreCase(t.getName()))
						{
							break;
						}
						ti.setSelected(false);
					}
					tp=i;
			//		Log.d("MAINACT","onscroll fi "+ tp);
					for (;i<_t_adapter.getCount() && topics>0;++i,--topics)
					{
		//				Log.d("MAINACT","onscroll ts "+ i);
						((LVTopicItem)_t_adapter.getItem(i)).setSelected(true);
					}						
					for (;i<_t_adapter.getCount();++i)
					{
	//					Log.d("MAINACT","onscroll tail "+ i);
						((LVTopicItem)_t_adapter.getItem(i)).setSelected(false);
					}

//					Log.d("MAINACT","onscroll "+_t_adapter.getCount()+" t "+ t +" "+i + " sel " +_t_lv.getSelectedItemPosition());
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
	
}
