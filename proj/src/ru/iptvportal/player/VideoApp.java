package ru.iptvportal.player;


import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;

import org.videolan.libvlc.VlcPlayer;

import ru.iptvportal.player.ChannelsConfig.Channel;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;
import android.util.Log;
import android.view.animation.Animation;

public class VideoApp extends Application {

	class AppService implements MiddlewareProto.ProtoEvents{
		private MiddlewareProto _prot;
		private boolean _logged_in=true;
		private Activity _r_act;
		private String _portal_url;
		boolean _config_loading=false;
		
		public AppService(MiddlewareProto proto){_prot=proto;
//			setPortalUrl("https://demo.iptvportal.ru");
			_prot.setUrl(_portal_url);
		}
		public MiddlewareProto getMiddlewareProtoInstance(){
			return _prot;
		}
		public boolean isLoggedIn(){return _logged_in;}
		public String getCurrentUserName(){ return _prot.getCurUserName();}
		public void login(Activity r_act, String id,String pin){
			String hw=getHW();
			//Log.d("HW","mac="+hw);
			_r_act=r_act;
			_prot.authRequest(id, pin, hw,this);
			//Log.d("AppService","logging in");
		}
		public void loadConfig(Activity r_act)
		{
			_config_loading=true;
			_r_act=r_act;
			_prot.channelsRequest(getHW(),this);
		}
		private void loadProfiles(Activity r_act)
		{
			_config_loading=true;
			_r_act=r_act;
			_prot.profilesRequest(getHW(),this);
			
		}
		private void loadTerminalSettings(Activity r_act)
		{
			_config_loading=true;
			_r_act=r_act;
			_prot.terminalSettingsRequest(getHW(),this);
			
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
			/*WifiManager manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
			if (manager!=null)
			{
				WifiInfo info = manager.getConnectionInfo();
				//return "00:11:22:33:44:53";
				return info.getMacAddress();
			}*/
		    try {
		        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) 
		        {
		            NetworkInterface intf = en.nextElement();
		            if (intf.isUp() && !intf.isLoopback())
		            {
		            	byte[] hw=intf.getHardwareAddress();
		            	if (hw!=null)
			            	return bytesToHex(hw);
		            }
		        }
		    } catch (SocketException ex) {

		    }
			return "";
		}
		@Override
		public void onLogin(boolean r) {
			_logged_in=r;
			resetConfig();
			_r_act.recreate();
		}
		@Override
		public void onProfilesLoaded(ProfilesData pd) {
			
			_logged_in=(pd.getProfile(0)!=null);
			if (_logged_in)
			{
				getAppConfig().getUserProfiles().setProfiles(pd,getAppConfig().getTerminalSettings());
			//	loadConfig(_r_act);
			}

			_config_loading=false;
			_r_act.recreate();
			
		}
		@Override 
		public void onTerminalSettingsLoaded(TerminalSettings ts) {
			
			_logged_in=ts!=null;
			if (_logged_in)
			{
				getAppConfig().setTerminalSettings(ts);
				loadProfiles(_r_act);
			}
			else
			{
				_config_loading=false;
				_r_act.recreate();
			}
				
		}
		
		@Override
		public void onChannels(ChannelsConfig ch_conf) {
			//Log.d("APPSERV","CC " + ch_conf);
			_logged_in=(ch_conf!=null && ch_conf.getTopics().size()>0);
			if (_logged_in)
			{
				//Log.d("APPSERV","SZ "+ch_conf.getTopics().size());
				AppConfig ac=getAppConfig();
				ac.setChannelsConfig(ch_conf);
				ac.setCurTopic(ch_conf.getTopics().get(0));
				ac.setCurChannel(ac.getCurTopic().getChannels().get(0));
				loadTerminalSettings(_r_act);
				
			}
			else
			{
				_config_loading=false;
				_r_act.recreate();
			}
			
		}
		@Override
		public void onEPGUploaded(Channel ch) {
			//Log.d("APP","epg uploaded");
		}
		public boolean isConfigLoading(){return _config_loading;}
		public void saveProfile(int pid) {
			_prot.updateProfile(getHW(), getAppConfig().getUserProfiles().getProfile(pid), this);
		}
		public void saveTerminalSettings() {
			_prot.setTerminalSettings(getHW(), getAppConfig().getTerminalSettings(), this);
		}
		@Override
		public void onSetRequest(boolean success) {
			
		}
		public String getPortalUrl() {
			return _portal_url;
		}
		public void setPortalUrl(String _portal_url) {
			this._portal_url = _portal_url;
			_prot.setUrl(_portal_url);
	        if (_video_player!=null)
	        	_video_player.setCasPortal(_portal_url);
		}
	}
	
	class AppConfig {
		class UserProfiles
		{
			private ProfilesData _pd=new ProfilesData();
			private int _cur_prof=0;
			public void setProfiles(ProfilesData pd,TerminalSettings ts)
			{
				_pd=pd;
				int i=0;
				for (ProfilesData.Profile p:_pd.getProfiles())
				{
					if (p.getId()==ts.getCurrentProfileId())
						break;
					++i;
				}
				if (i==_pd.getProfiles().size())
					i=0;
				_cur_prof=i;
			}
			public String[] getProfilesNames()
			{
				
				String a[]=new String[_pd.getProfiles().size()];
				int i=0;
				for ( ProfilesData.Profile p: _pd.getProfiles())
				{
					a[i]=p.getTitle();
					++i;
				}
				return a;
			}
			public boolean isPassNeeded(int pid)
			{
				return _pd.getProfile(pid).isPassReq();
			}
			public boolean isPassCorrect(int pid,String pass)
			{
				String p= _pd.getProfile(pid).getPassword();
				if (p==null)
					return false;
				return pass.compareTo(p)==0;
			}
			public void setCurrentProfile(int pid)
			{
				_ts.setCurrentProfileId(_pd.getProfile(pid).getId());
				_cur_prof=pid;
			}
			public ProfilesData.Profile getProfile(int num){
				return _pd.getProfile(num);
			}
			public ProfilesData.Profile getCurrentProfile(){
				return _pd.getProfile(_cur_prof);
			}
			public int getCurrentProfileNum(){
				return _cur_prof;
			}
			public boolean passIsSet(int selected_profile) {
				
				return _pd.getProfile(selected_profile).getPassword()!=null;
			}
			public void setProfilePass(int selected_profile,String pass) {
				_pd.getProfile(selected_profile).setPassword(pass);
			}
		};
		private ChannelsConfig _ch_conf=null;
		private ChannelsConfig.Topic _cur_topic;
		private ChannelsConfig.Channel _cur_channel;
		private UserProfiles _user_prof= new UserProfiles();
		private TerminalSettings _ts;
		
		public AppConfig(){
			reset();
		}
		protected void reset() {
			
			_ch_conf=null;
			_user_prof= new UserProfiles();
			_ts=new TerminalSettings();
			
		}
		
		
		public void setTerminalSettings(TerminalSettings ts){
			_ts=ts;
		}
		public TerminalSettings getTerminalSettings(){return _ts;}
		public ChannelsConfig getChannelsConfig() {
			return _ch_conf;
		}
		public void setChannelsConfig(ChannelsConfig _ch_conf) {
			this._ch_conf = _ch_conf;
		}
		public ChannelsConfig.Topic getCurTopic() {
			return _cur_topic;
		}
		public UserProfiles getUserProfiles()
		{
			return _user_prof;
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
	private VlcPlayer _video_player=null;
	//public String getSomeString(){return "123";}
	//public void setCurrentMedia(String m){_current_media=m;}
	//public String getCurrentMedia(){return _current_media;}
	
	public AppService getAppService(){return _app_serv;}
	public AppConfig  getAppConfig(){return _app_conf;}
	public void setViewManager (ViewManager vm){_vmng=vm;}
	public ViewManager.AppViewState getViewState(){return _app_vs;}
	public void setNextView(Animation a){setViewState(_app_vs.getNext(),a);}
	public void setPrevView(Animation a){setViewState(_app_vs.getPrev(),a);}
	public void setViewState(ViewManager.AppViewState s,Animation a){
		ViewManager.AppViewState old=_app_vs;
		_app_vs=s;
		if (_vmng!=null)
			switch (_app_vs)
			{
				case INTERFACE:
					_vmng.onViewInterface(old,a);
					break;
				case EPG:
					_vmng.onViewEpg(old,a);
					break;
				case VIDEO:
					_vmng.onViewVideo(old,a);
					break;
				case LOGIN:
					_vmng.onViewLogin(old, a);
					break;
			}
		}
	public VlcPlayer getVideoPlayer() {
		return _video_player;
	}
	public void setVideoPlayer(VlcPlayer _video_player) {
		this._video_player = _video_player;
        if (_video_player!=null)
        	_video_player.setCasPortal(getAppService()._portal_url);
	}
	protected void resetConfig(){_app_conf.reset();}

	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
	public static String bytesToHex(byte[] bytes) {
	    char[] hexChars = new char[bytes.length * 3];
	    for ( int j = 0; j < bytes.length; j++ ) {
	        int v = bytes[j] & 0xFF;
	        hexChars[j * 3] = hexArray[v >>> 4];
	        hexChars[j * 3 + 1] = hexArray[v & 0x0F];
	        hexChars[j * 3 + 2] = ':';
	    }
	    String res=new String(hexChars);
	    return res.substring(0,res.length()-1);
	}
	
	
}
