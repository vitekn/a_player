package ru.iptvportal.player;



import java.util.Date;

import android.content.Context;

public interface MiddlewareProto {
	public interface ProtoEvents{
		void onLogin(boolean r);
		void onChannels(ChannelsConfig ch_conf);
		void onEPGUploaded(ChannelsConfig.Channel ch);
		void onProfilesLoaded(ProfilesData pd);
		void onTerminalSettingsLoaded(TerminalSettings ts);
		void onSetRequest(boolean success);
	}
	public interface ReqData{
		public enum Request{NONE,AUTH,LOGIN,CHANNELS,EPG,TOPICS,PROFILES,TERMINAL_PARAMS,UPDATE_PROFILE,SET_TERMINAL}

		Request getType();
	}
	public class ReqRespPair{
		private static int _id=0;
		private int _rrid;
		public SendHttpRequest resp;
		public ReqData req_data;
		public ProtoEvents clb;
		public ReqRespPair(){_rrid=_id; ++_id;}
		public int getId(){return _rrid;}
	} 
	public MiddlewareProto clone(); 
	public void authRequest(String id,String pin,String hw,MiddlewareProto.ProtoEvents clb);
	public void channelsRequest(String hw,MiddlewareProto.ProtoEvents clb);
	public void epgRequest(ChannelsConfig.Channel ch,Date from,Date to,MiddlewareProto.ProtoEvents clb);
	public void profilesRequest(String hw,MiddlewareProto.ProtoEvents clb);
	public void terminalSettingsRequest(String hw,MiddlewareProto.ProtoEvents clb);
	public void updateProfile(String hw,ProfilesData.Profile p,MiddlewareProto.ProtoEvents clb);
	public void setTerminalSettings(String hw,TerminalSettings ts,MiddlewareProto.ProtoEvents clb);
	String getSSLSertName();
	public void setUrl(String url);
//	ChannelsConfig getCannelsConfig();
	Context getContext();
	
	
}
