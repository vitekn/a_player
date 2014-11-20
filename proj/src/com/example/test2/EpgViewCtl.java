package com.example.test2;

import java.util.ArrayList;
import java.util.Date;

import com.example.test2.ChannelsConfig.Channel;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;

public class EpgViewCtl implements OnItemSelectedListener , OnListBoundReached,MiddlewareProto.ProtoEvents{
	private ArrayAdapter<String> _t_adapter;
	private EpgListAdapter _list_adapter;
	private VideoApp _app;
	private ArrayList<ChannelsConfig.Channel> _all_ch;
	private ChannelsConfig.Channel _cur_chan=null;
	private Date _item_to_select=new Date();
	private ListView _epg_l;
	private Spinner _sp;
	private ProgressBar _epg_prog;
	 class EpgListClick implements OnItemClickListener{

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
			EPGData epg=(EPGData)arg1.getTag();
			if (_cur_chan!=null)
			{
				String ts=_cur_chan.getTimeShiftUrlForProgramm(epg);
				if (!ts.isEmpty())
				{
					_app.getVideoPlayer().setVideoURI(Uri.parse(ts));
					_app.getVideoPlayer().start();
				}
			}
		}
	 }

	public EpgViewCtl(VideoApp a,Context ctx,Spinner s,ListView l,ProgressBar ep){
		
		_app= a;
		_sp =s;
		_epg_l=l;
		_epg_prog=ep;
		_epg_prog.setVisibility(View.INVISIBLE);
		_epg_l.setOnItemClickListener(new EpgListClick());
		Log.d("epg","la1");
		_t_adapter=new ArrayAdapter<String>(ctx,R.layout.epg_topic,new ArrayList<String>());
		_sp.setAdapter(_t_adapter);
		
		ChannelsConfig.Topic t=_app.getAppConfig().getChannelsConfig().getTopics().get(0);
		_all_ch=t.getChannels();
		syncChannel();
		
		Log.d("epg","la2");
		_list_adapter = new EpgListAdapter(ctx, R.layout.epg_list_item, new ArrayList<EPGData>(),this);
		Log.d("epg","la2 ok");
		
		_epg_l.setAdapter(_list_adapter);		
		
	}

	public void syncChannel() {
		int i=0;
		int si=0;
		String ccn=_app.getAppConfig().getCurChannel().getName();
		for (ChannelsConfig.Channel c:_all_ch)
		{
			_t_adapter.add(c.getName());
			if (ccn.equalsIgnoreCase(c.getName()))
				si=i;
			++i;
		}
		_sp.setOnItemSelectedListener(this);
		_sp.setSelection(si);
	}
	
	public void selectEpgListItem(Date at){
		for (int i=0;i<_list_adapter.getCount();++i)
		{
			EPGData ed=_list_adapter.getItem(i);
			if (ed.getStart().getTime()<=at.getTime() && ed.getStop().getTime()>=at.getTime())
			{
				Log.d("EPGACT","sel ch=" + ed.getStartTime() + "-" + ed.getStopTime()+ " "+i);
				
				_epg_l.setSelectionAfterHeaderView();
				_epg_l.setSelection(i);
				//lv.getSelectedView().setSelected(true);
				break;
			}
		}
	}
	
	public void refreshList(ChannelsConfig.Channel ch)
	{
		
		Log.d("EPGACT","refre itp= "+_sp.getSelectedItemPosition()+" " +ch.getName()+" "+_t_adapter.getItem(_sp.getSelectedItemPosition()));
		
		if (ch.getName().equalsIgnoreCase(_t_adapter.getItem(_sp.getSelectedItemPosition())));
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
		
		if (_cur_chan!=null && !_cur_chan.isEpgLoading())
		{
			_epg_prog.setVisibility(View.VISIBLE);
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
		if (_cur_chan!=null && !_cur_chan.isEpgLoading())
		{
			_epg_prog.setVisibility(View.VISIBLE);
			
			ArrayList<EPGData> el= _cur_chan.getAllEpgData();
			if (el.size()>0)
			{
				int p=_epg_l.getFirstVisiblePosition();
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

	@Override
	public void onLogin(boolean r) {
	}

	@Override
	public void onChannels(ChannelsConfig ch_conf) {
	}

	@Override
	public void onEPGUploaded(Channel ch) {
		_epg_prog.setVisibility(View.INVISIBLE);
		refreshList(ch);
	}


}
