package com.example.test2;



import java.util.Date;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.test2.ChannelsConfig.Channel;


public class LVChannelItem extends LVComItem implements MiddlewareProto.ProtoEvents{

	private static final long serialVersionUID =-5435670920302756946L;
	//private String _name = "";
	private ChannelsConfig.Channel _my_channel=null;
	private ChannelsConfig.Topic _my_topic=null;
	private VideoApp _app;
	boolean _epg_uploading=false;
	
	public LVChannelItem(Channel ch,ChannelsConfig.Topic from_topic,int l_id,OnClickListener cll,VideoApp app) {
		super(l_id,cll,CHANNEL_TYPE,ch);
		_app=app;
		_my_channel=ch;
//		this.setName(ch.getName());
		_my_topic=from_topic;
		

	}
	
	public ChannelsConfig.Topic getTopic(){return _my_topic;}
	
	

	
	public ChannelsConfig.Channel getChannel(){return _my_channel;}

	/*
	public String getName() {
		return _name;
	}

	public void setName(String name) {
		this._name = name;
	}*/
	

	@Override
	public View makeView(View v, ViewGroup parent,LayoutInflater inf) {
		if (v==null)
		{
			v = inf.inflate(_layoutResourceId, parent, false);
		}
		View bgv=v.findViewById(R.id.channel_list_item);
		if (_app.getAppConfig().getCurChannel()==_my_channel)
			bgv.setBackgroundResource(R.drawable.button_hl_grad);
		else
			bgv.setBackgroundResource(R.drawable.button_nhl_grad);
		bgv.setOnClickListener(_cl_l);
		bgv.setTag(this);
		uploadIcon(_my_channel);
		EPGData ed=_my_channel.getCurrentEpgData();
		TextView epg=(TextView)v.findViewById(R.id.channel_epg);
		epg.setTag(this);
		epg.setOnClickListener(_cl_l);

		ProgressBar bar=(ProgressBar)v.findViewById(R.id.progressBar1);
		if (ed!=null)
		{
			epg.setText(ed.getTitle());
			
			int secs=(int)((ed.getStop().getTime()- ed.getStart().getTime())/1000);
			bar.setVisibility(View.VISIBLE);
			bar.setMax(secs);
			bar.setProgress((int)((new Date().getTime() - ed.getStart().getTime())/1000));
		}
		else
		{
			if (_my_channel.canUploadEpg() && !_epg_uploading)
			{
				_epg_uploading=true;
				MiddlewareProto mp=_app.getAppService().getMiddlewareProtoInstance();
				mp.epgRequest(_my_channel, null, null,this);
			}
			bar.setVisibility(View.INVISIBLE);
			epg.setText("");
		}
		ImageButton but= (ImageButton)v.findViewById(R.id.atomPay_removePay1);
		but.setTag(this);
		but.setImageBitmap(getIcon());
		but.setOnClickListener(_cl_l);
		TextView name=(TextView)v.findViewById(R.id.atomPay_name1);
		name.setText(_my_channel.getName());
		name.setOnClickListener(_cl_l);
		name.setTag(this);
		return v;
	}
	
	
	@Override
	public void onLogin(boolean r) {
	}

	@Override
	public void onChannels(ChannelsConfig ch_conf) {
	}

	@Override
	public void onEPGUploaded(Channel ch) {
		_epg_uploading=false;
		callChangedListener();
		
	}

	@Override
	public void onProfilesLoaded(ProfilesData pd) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTerminalSettingsLoaded(TerminalSettings ts) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSetRequest(boolean success) {
		// TODO Auto-generated method stub
		
	}


}
