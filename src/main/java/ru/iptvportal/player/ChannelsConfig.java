package ru.iptvportal.player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;

import org.videolan.libvlc.VlcPlayer;

import android.net.Uri;
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
		public byte[] getIconBytes(){		//	//Log.d("CHANNEL","getIconBytes" + _icon + _icon.length);
			return _icon;}
		@Override
		public void onHttpRequestComplete(byte[] res,int id) {
				//Log.d("CHANNEL","ICON LOADED");
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
	
	class ChannelIndexComparator implements Comparator<Channel>{
		@Override
		public int compare(Channel arg0, Channel arg1) {
			return arg0.index-arg1.index;
		}
	}
	
	class Channel extends GenItem {
		private String _mrl;
		private ArrayList<EPGData> _epg=new ArrayList<EPGData>();	
		private boolean _epg_present=true;
		private String _timeshift_url;
		private int _timeshift_duration;
		private boolean _epg_loading=false;
		private boolean _pl_arch=false;
		private int _position=0;
		private long _ts_pstart=0;
		private long _ts_pend=0;
		private long _tshift=0;
		private int _age_rating=0;
		private int index=0;
		
		public Channel(String name,String mrl,String icon_url,int id,String tm_url,int tm_dur, int age_r,int index){
			super(name,icon_url,id);
			_mrl=mrl;
			_timeshift_url=tm_url;
			_timeshift_duration=tm_dur;
			_age_rating=age_r;
		}
		public boolean isEpgLoading(){return _epg_loading;}
		public void epgLoading(){_epg_loading=true;}
		public void epgUploaded(){_epg_loading=false;}
		public boolean playsArchive(){return _pl_arch;}
		public int getPosition(VlcPlayer pl){
			
			if (_pl_arch)
				return -1;
			Date now=new Date();
			long p=now.getTime()-_tshift-pl.getPauseTime();
			if (p>=_ts_pend || _ts_pstart==0)
			{
				EPGData ed=getCurrentEpgData();
				if (ed!=null)
				{
					_ts_pstart=ed.getStart().getTime();
					_ts_pend=ed.getStop().getTime();
				}
				
			}
			if (_ts_pend!=_ts_pstart)
			{
				_position=(int)(((p-_ts_pstart)*1000/(_ts_pend-_ts_pstart)));
			}
			return _position;
			}
		public void setPosition(VlcPlayer p, int pos)
		{//set position here!
			if (p.isSeekable() && _pl_arch)
			{
				p.setPosition(((float)pos)/1000);
			}
			else
			{
				if (_ts_pstart!=0 && _ts_pstart!=_ts_pend)
				{
					Date now=new Date();
					int rp=(int)((now.getTime()-_ts_pstart)*1000/(_ts_pend-_ts_pstart));
					if (pos<rp)
					{
//						_position=pos;
						now.setTime(_ts_pstart+((_ts_pend-_ts_pstart)*pos/1000));
						startPlay(p,now,"");
					}
				}
			}
		};
		
		public boolean startPlay(VlcPlayer p,Date start,ProfilesData.Profile up,TerminalSettings ts){
			//Log.d("CHANNEL","startPlay" + up.getAgeLimit() + " "+ _age_rating);
			if (up.getAgeLimit()<_age_rating)
			{
				return false;
			}
			String pref="";
			if (ts.isUdpProxyUsed())
			{
				pref=ts.getUdpProxy();
				if (!pref.endsWith("/"))
					pref=pref+"/";
			}
			
			return startPlay(p,start,pref);
		}
		private boolean startPlay(VlcPlayer p,Date start,String prefix){
			Date now=new Date();
			String u="";
			_pl_arch=false;
			_ts_pstart=0;
			_ts_pend=0;
			_tshift=0;
			_position=0;
			if ((start.getTime()+10000)>=now.getTime())
			{///use live
				u=getMrl();
				if (u.startsWith("udp://")) {
					if (!prefix.isEmpty())
						u=u.replace("://@","/");
					u = prefix + u;
				}
			}
			else
			if (!_epg.isEmpty() && _timeshift_duration>0)
			{
				int i=getEpgIndexIn(start);
				if (i!=_epg.size())
				{
					EPGData ed=_epg.get(i);
					if (ed.getStop().after(new Date()))
					{///use timeshift
						_ts_pstart=ed.getStart().getTime();
						_ts_pend=ed.getStop().getTime();
						if (_ts_pend!=_ts_pstart)
							_position=(int)((start.getTime()-ed.getStart().getTime())*1000/(_ts_pend-_ts_pstart));
						else
							_position=0;
						u=getTimeShiftUrlForProgramm((now.getTime()-start.getTime())/1000);

						_tshift=now.getTime()-start.getTime();
						
					}
					else
					{///use archive
						_pl_arch=true;
						u=getArchiveUrlForProgramm(ed);
					}
				
				}
				else
					return false;
			}
			if (!u.isEmpty())
			{
				p.setVideoURI(Uri.parse(u));
				p.start();
			}
			
			return !u.isEmpty();
		}
		
		public String getMrl(){
			//Log.d("CHANNEL","getMrl");
			return _mrl;}
		
		private String getTimeShiftUrlForProgramm(long secs_ago)
		{
			return _timeshift_url+"/timeshift_rel-"+secs_ago+".m3u8";
		}
		private String getArchiveUrlForProgramm(EPGData epg)
		{
			String url="";
			Date now=new Date();
			long dur=(now.getTime()-epg.getStart().getTime())/1000;
			if (!_timeshift_url.isEmpty() && dur>60 && (_timeshift_duration)>dur && epg.getStop().before(now))
			{
				url=_timeshift_url+"/index-"+((long)(epg.getStart().getTime()/1000))+"-"+(((long)(epg.getStop().getTime()/1000))-((long)(epg.getStart().getTime()/1000)))+".m3u8";
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
						//Log.d("CHANNEL","rem epg "+ epg.getTitle() +" t "+epg.getStartTime()+" at "+i);
						_epg.remove(i);
					}
					else
						break;
				}
			} while (i<_epg.size());

//			//Log.d("CHANNEL","add epg "+ epg.getTitle() +" t "+epg.getStartTime()+" at "+i);
		//	epg.setUrl(getTimeShiftUrlForProgramm(epg));
			epg.setTimeShift(!getArchiveUrlForProgramm(epg).isEmpty());
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
		private int getEpgIndexIn(Date d)
		{
			int i=0;
			for (;i<_epg.size();++i)
			{
				EPGData ed=_epg.get(i);
				if (ed.isAtTime(d))
					break;
//				if (ed.getStart().getTime()<=d.getTime() && d.getTime()<ed.getStop().getTime())
	//				break;
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
		public void addChannel(String name,String mrl,String icon_url,int id,String tm_url,int tm_dur, int age_r,int index)
		{
			_channels.add(new Channel(name,mrl,icon_url,id,tm_url,tm_dur,age_r,index));
		}
		void sortByIndex(){
			Collections.sort(_channels, new ChannelIndexComparator());
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
			Topic t=i.next();
			if (t.getChannels().size()==0)
			{
				i.remove();
			}
		}
	}
	
}
