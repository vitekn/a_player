package com.example.test2;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import android.util.Log;


public class ChannelsConfig {

	class GenItem implements OnHttpRequestComplete{

		private String _icon_url;
		private int _id=-1;
		private byte[] _icon=new byte[0];
		private ArrayList<OnChannelDataChanged> _listeners=new ArrayList<OnChannelDataChanged>();	
		private String _name;
		
		public GenItem(String name,String icon_url,int id)
		{
			_name=name;
			_icon_url=icon_url;
			_id=id;
		}
		public void uploadIcon(){
			if (_icon.length==0 && _icon_url.startsWith("http"))
			{
				new SendHttpRequest(null,this).execute(_icon_url,"","1","GET");
			}
		}
		public int getId(){return _id;}
		public String getName(){return _name;}

		public String getIconUrl(){return _icon_url;}
		public byte[] getIconBytes(){		//	Log.d("CHANNEL","getIconBytes" + _icon + _icon.length);
			return _icon;}
		@Override
		public void onHttpRequestComplete(byte[] res,int id) {
				Log.d("CHANNEL","ICON LOADED");
				_icon=res;
				if (_icon==null)
					_icon=new byte[0];
				else
					notifyAllDataChanged();
		}
		public void addDataChangeListener(OnChannelDataChanged l){_listeners.add(l);}
		public void removeDataChangeListener(OnChannelDataChanged l){_listeners.remove(l);}
		
		public void notifyAllDataChanged(){ for (OnChannelDataChanged l : _listeners){l.onChannelDataChanged(this);}}
	}
	
	class Channel extends GenItem {
		private String _mrl;
		private ArrayList<EPGData> _epg=new ArrayList<EPGData>();	
		private boolean _epg_present=true;
		private String _timeshift_url;
		private int _timeshift_duration;
		
		public Channel(String name,String mrl,String icon_url,int id,String tm_url,int tm_dur){
			super(name,icon_url,id);
			_mrl=mrl;
			_timeshift_url=tm_url;
			_timeshift_duration=tm_dur;
		}
		public String getMrl(){
			Log.d("CHANNEL","getMrl");
			return _mrl;}
		
		public String getTimeShiftUrlForProgramm(EPGData epg)
		{
			String url="";
			Date now=new Date();
			long dur=(now.getTime()-epg.getStop().getTime())/1000;
			if (!_timeshift_url.isEmpty() && dur>60 && (_timeshift_duration)>dur)
			{
				url=_timeshift_url+"/timeshift_abs/"+((long)(epg.getStop().getTime()/1000));
			}
			return url;
		}
		public boolean canUploadEpg(){ return _epg_present;}
		public void epgFail() {_epg_present=false;}
		public void epgClear(){_epg.clear();}
		public void addEpgData(EPGData epg){ 
			int i;
			do
			{
				i=getEpgIndexByStop(epg.getStop());
				if (i<_epg.size())
				{
					if (_epg.get(i).getStart().getTime()<=epg.getStart().getTime())
					{
						Log.d("CHANNEL","rem epg "+ epg.getTitle() +" t "+epg.getStartTime()+" at "+i);
						_epg.remove(i);
					}
					else
						break;
				}
			} while (i<_epg.size());

//			Log.d("CHANNEL","add epg "+ epg.getTitle() +" t "+epg.getStartTime()+" at "+i);
			epg.setUrl(getTimeShiftUrlForProgramm(epg));
			_epg.add(i,epg);
			_epg_present=true;
			
		}
		private int getEpgIndexByStop(Date d)
		{
			int i=0;
			for (;i<_epg.size();++i)
			{
				EPGData ed=_epg.get(i);
				if (ed.getStop().getTime()>=d.getTime())
					break;
			}
			return i;
		}
		public EPGData getCurrentEpgData()
		{
			EPGData epg=null;
			Date now=new Date();
			for (EPGData e:_epg)
			{
				if (e.getStart().before(now) && e.getStop().after(now))
				{
					epg=e;
					break;
				}
			}
			return epg;
		}
		public ArrayList<EPGData> getAllEpgData()
		{
			return _epg;
		}
		public ArrayList<EPGData> getEpgData(Date from)
		{
			ArrayList<EPGData> epg=new ArrayList<EPGData>();
			for (EPGData e:_epg)
			{
				if (e.getStop().after(from))
				{
					epg.add(e);
				}
			}
			
			return epg;
		}
		public ArrayList<EPGData> getEpgData(Date from,Date till)
		{
			ArrayList<EPGData> epg=new ArrayList<EPGData>();
			for (EPGData e:_epg)
			{
				if (e.getStart().after(till))
					break;
				if (e.getStop().after(from))
				{
					epg.add(e);
				}
			}
			return epg;
		}
	}
	class Topic extends GenItem{
		private ArrayList<Channel> _channels=new ArrayList<Channel>();
		public Topic(String name,String icon_url,int id)
		{
			super(name,icon_url,id);
		}
		public ArrayList<Channel> getChannels(){return _channels;}
		public void addChannel(String name,String mrl,String icon_url,int id,String tm_url,int tm_dur)
		{
			_channels.add(new Channel(name,mrl,icon_url,id,tm_url,tm_dur));
		}
	}
	
	private ArrayList<Topic> _topics=new ArrayList<Topic>();

	public ArrayList<Topic> getTopics(){return _topics;}
	public void addTopic(String tn,String icon_url,int id){_topics.add(new Topic(tn,icon_url,id));}
	public void addCommonTopic(String tn){
		Topic all=new Topic(tn,"",-1);
		for (Topic t: _topics)
		{
			all.getChannels().addAll(t.getChannels());
		}		
		_topics.add(0, all);
	}
	public Topic findTopic(String name){
		Topic res=null;
		for (Topic t: _topics)
		{
			if (t.getName()==name)
			{
				res=t;
				break;
			}
		}
		return res;
	}
	public Topic findTopic(int id){
		Topic res=null;
		for (Topic t: _topics)
		{
			if (t.getId()==id)
			{
				res=t;
				break;
			}
		}
		return res;
	}
	public void removeEmptyTopics()
	{
		Iterator<Topic> i=_topics.iterator();
		while (i.hasNext()){
			if (i.next().getChannels().size()==0)
				i.remove();
		}
		
	}
	
}
