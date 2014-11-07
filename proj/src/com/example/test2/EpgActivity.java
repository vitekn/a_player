package com.example.test2;

import java.util.ArrayList;
import java.util.Date;

import org.videolan.libvlc.VlcPlayer;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.VideoView;
import android.widget.AdapterView.OnItemSelectedListener;

public class EpgActivity extends Activity implements OnItemSelectedListener , OnListBoundReached{
	private ArrayAdapter<String> _t_adapter;
	private EpgListAdapter _list_adapter;
	private VideoApp _app;
	private ArrayList<ChannelsConfig.Channel> _all_ch;
	private ChannelsConfig.Channel _cur_chan=null;
	private Date _item_to_select=new Date();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_epg);
		
		_app= (VideoApp)this.getApplication();
		Spinner sp =(Spinner) findViewById(R.id.spinner1);
		Log.d("epg","la1");
		_t_adapter=new ArrayAdapter<String>(EpgActivity.this,R.layout.epg_topic,new ArrayList<String>());
		sp.setAdapter(_t_adapter);
		
		ChannelsConfig.Topic t=_app.getAppConfig().getChannelsConfig().getTopics().get(0);
		_all_ch=t.getChannels();
		int i=0;
		int s=0;
		String ccn=_app.getAppConfig().getCurChannel().getName();
		for (ChannelsConfig.Channel c:_all_ch)
		{
			_t_adapter.add(c.getName());
			if (ccn.equalsIgnoreCase(c.getName()))
				s=i;
			++i;
		}
		sp.setOnItemSelectedListener(this);
		sp.setSelection(s);
		
		Log.d("epg","la2");
		_list_adapter = new EpgListAdapter(EpgActivity.this, R.layout.epg_list_item, new ArrayList<EPGData>(),this);
		Log.d("epg","la2 ok");
		ListView lv = (ListView)findViewById(R.id.listView1);
		lv.setAdapter(_list_adapter);		
		
		lv.setOnTouchListener( new OnSwipeTouchListener(this.getBaseContext()){
			@Override
			public void onSwipeLeft() {
				finish();
			}
			@Override
			public void onSwipeRight() {
				finish();
			}
			
		});
		
		VlcPlayer vv;
		SurfaceView sv= (SurfaceView) findViewById(R.id.videoView1);
		vv=new VlcPlayer(sv,this);

//		VideoView vv=(VideoView) findViewById(R.id.videoView1);
		vv.setVideoURI(Uri.parse(_app.getAppConfig().getCurChannel().getMrl()));
		vv.start();
/*		if (_all_ch.size()>0)
		{
			_list_adapter.clear();
		}*/
		
	}
	
	public void selectEpgListItem(Date at){
		for (int i=0;i<_list_adapter.getCount();++i)
		{
			EPGData ed=_list_adapter.getItem(i);
			if (ed.getStart().getTime()<=at.getTime() && ed.getStop().getTime()>=at.getTime())
			{
				Log.d("EPGACT","sel ch=" + ed.getStartTime() + "-" + ed.getStopTime()+ " "+i);
				ListView lv = (ListView)findViewById(R.id.listView1);
				lv.setSelectionAfterHeaderView();
				lv.setSelection(i);
				//lv.getSelectedView().setSelected(true);
				break;
			}
		}
	}
	
	public void refreshList(ChannelsConfig.Channel ch)
	{
		Spinner sp =(Spinner) findViewById(R.id.spinner1);
		Log.d("EPGACT","refre itp= "+sp.getSelectedItemPosition()+" " +ch.getName()+" "+_t_adapter.getItem(sp.getSelectedItemPosition()));
		
		if (ch.getName().equalsIgnoreCase(_t_adapter.getItem(sp.getSelectedItemPosition())));
		{
			ArrayList<EPGData> epgl=ch.getAllEpgData();
			_list_adapter.clear();
			_list_adapter.addAll(epgl);
			if (epgl.size()==0)
				_app.getAppService().loadEPG(this, ch);
			_list_adapter.notifyDataSetChanged();
		}	
		
		selectEpgListItem(_item_to_select);
	}

	private void setCurrentChannel(String name)
	{
		_cur_chan=null;
		for (ChannelsConfig.Channel c:_all_ch)
		{
			if (c.getName().equalsIgnoreCase(name))
			{
				_cur_chan=c;
				break;
			}
		}
		Log.d("EPGACT","selected f= "+_cur_chan.getName() );
		if (_cur_chan!=null)
		{
			_list_adapter.clear();
			refreshList(_cur_chan);
			_item_to_select=new Date();
		}
	}
	
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		String s=_t_adapter.getItem(position);
		setCurrentChannel(s);
	
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		
	}

	@Override
	public void upperBound() {
		Log.d("EPGACT","upb");
		
		if (_cur_chan!=null)
		{
//			upb=false;
			ArrayList<EPGData> el= _cur_chan.getAllEpgData();
			if (el.size()>0)
			{
				if (_list_adapter.getCount()>0)
				{
					EPGData sed=_list_adapter.getItem(0);
					_item_to_select=sed.getStart();
				}					
				else
					_item_to_select=new Date();
			
				_app.getAppService().loadEPG(this,_cur_chan,new Date (el.get(0).getStart().getTime()-1000*60*60*24),el.get(0).getStart());
			}
			else
			{
				_item_to_select=new Date();
				_app.getAppService().loadEPG(this, _cur_chan);
			}

		}
	}

	@Override
	public void lowerBound() {
		if (_cur_chan!=null)
		{
			ArrayList<EPGData> el= _cur_chan.getAllEpgData();
			if (el.size()>0)
			{
				ListView lv = (ListView)findViewById(R.id.listView1);
				int p=lv.getFirstVisiblePosition();
				if (_list_adapter.getCount()>0 && p<_list_adapter.getCount())
				{
					EPGData sed=_list_adapter.getItem(p);
					_item_to_select=sed.getStart();
				}					
				else
					_item_to_select=new Date();
				
				_app.getAppService().loadEPG(this,_cur_chan,el.get(el.size()-1).getStop(),new Date (el.get(el.size()-1).getStop().getTime()+1000*60*60*24));
			}
			else
			{
				_item_to_select=new Date();
				_app.getAppService().loadEPG(this, _cur_chan);
			}

			
		}
		
	}

}
