package com.example.test2;


import java.util.Date;

import org.videolan.libvlc.VlcPlayer;

import com.example.test2.ChannelsConfig.Channel;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class VideoApp extends Application {

	class AppService implements MiddlewareProto.ProtoEvents{
		private MiddlewareProto _prot;
		private boolean _logged_in=true;
		private Activity _r_act;
		boolean _config_loading=false;
		
		public AppService(MiddlewareProto proto){_prot=proto;
		}
		public MiddlewareProto getMiddlewareProtoInstance(){
			return _prot;
		}
		public boolean isLoggedIn(){return _logged_in;}
		public void login(Activity r_act, String id,String pin){
			String hw=getHW();
			Log.d("HW","mac="+hw);
			_r_act=r_act;
			_prot.authRequest(id, pin, hw,this);
			Log.d("AppService","logging in");
		}
		public void loadConfig(Activity r_act)
		{
			_config_loading=true;
			_r_act=r_act;
			String hw=getHW();
			_prot.channelsRequest(hw,this);
		}
		public void loadEPG(MiddlewareProto.ProtoEvents pre,ChannelsConfig.Channel ch){
			if (pre==null)
				pre=this;
			ch.epgClear();
			_prot.epgRequest(ch,null,null,pre);
		}		
		public void loadEPG(MiddlewareProto.ProtoEvents pre,ChannelsConfig.Channel ch,Date from,Date to){
			if (pre==null)
				pre=this;
			_prot.epgRequest(ch,from,to,pre);
		}		
		private String getHW()
		{
			WifiManager manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
			if (manager!=null)
			{
				WifiInfo info = manager.getConnectionInfo();
				//return "00:11:22:33:44:53";
				return info.getMacAddress();
			}
			return "";
		}
		@Override
		public void onLogin(boolean r) {
			_logged_in=r;
			_r_act.recreate();
		}
		@Override
		public void onChannels(ChannelsConfig ch_conf) {
			Log.d("APPSERV","CC " + ch_conf);
			if (ch_conf!=null && ch_conf.getTopics().size()>0)
			{
				Log.d("APPSERV","SZ "+ch_conf.getTopics().size());
				AppConfig ac=getAppConfig();
				ac.setChannelsConfig(ch_conf);
				ac.setCurTopic(ch_conf.getTopics().get(0));
				ac.setCurChannel(ac.getCurTopic().getChannels().get(0));
				_logged_in=true;
			}
			else
				_logged_in=false;
			_config_loading=false;
			_r_act.recreate();
		}
		@Override
		public void onEPGUploaded(Channel ch) {
			Log.d("APP","epg uploaded");
		}
		public boolean isConfigLoading(){return _config_loading;}
	}
	
	class AppConfig {
		private ChannelsConfig _ch_conf=null;
		private ChannelsConfig.Topic _cur_topic;
		private ChannelsConfig.Channel _cur_channel;
		
		public ChannelsConfig getChannelsConfig() {
			return _ch_conf;
		}
		public void setChannelsConfig(ChannelsConfig _ch_conf) {
			this._ch_conf = _ch_conf;
		}
		public ChannelsConfig.Topic getCurTopic() {
			return _cur_topic;
		}
		public void setCurTopic(ChannelsConfig.Topic _cur_topic) {
			this._cur_topic = _cur_topic;
		//	this._cur_topic.uploadIcon();
		}
		public ChannelsConfig.Channel getCurChannel() {
			return _cur_channel;
		}
		public void setCurChannel(ChannelsConfig.Channel _cur_channel) {
			this._cur_channel = _cur_channel;
		}
	}
	
	
	
	private ViewManager.AppViewState _app_vs=ViewManager.AppViewState.INTERFACE;
	private ViewManager _vmng=null;
	private AppService _app_serv = new AppService(new AresMiddlewareProto(this));
	private AppConfig _app_conf=new AppConfig(); 
	private VlcPlayer _video_player;
	//public String getSomeString(){return "123";}
	//public void setCurrentMedia(String m){_current_media=m;}
	//public String getCurrentMedia(){return _current_media;}
	
	public AppService getAppService(){return _app_serv;}
	public AppConfig  getAppConfig(){return _app_conf;}
	public void setViewManager (ViewManager vm){_vmng=vm;}
	public ViewManager.AppViewState getViewState(){return _app_vs;}
	public void setNextView(){setViewState(_app_vs.getNext());}
	public void setPrevView(){setViewState(_app_vs.getPrev());}
	public void setViewState(ViewManager.AppViewState s){
		ViewManager.AppViewState old=_app_vs;
		_app_vs=s;
		if (_vmng!=null)
			switch (_app_vs)
			{
				case INTERFACE:
					_vmng.onViewInterface(old);
					break;
				case EPG:
					_vmng.onViewEpg(old);
					break;
				case VIDEO:
					_vmng.onViewVideo(old);
					break;
			}
		}
	public VlcPlayer getVideoPlayer() {
		return _video_player;
	}
	public void setVideoPlayer(VlcPlayer _video_player) {
		this._video_player = _video_player;
	}
	
	
}
