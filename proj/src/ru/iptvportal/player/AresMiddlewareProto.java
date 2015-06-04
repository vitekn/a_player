package ru.iptvportal.player;


import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Stack;
import java.text.SimpleDateFormat;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import ru.iptvportal.player.ProfilesData.Profile;
import android.content.Context;
import android.util.Log;


public class AresMiddlewareProto  implements MiddlewareProto,OnHttpRequestComplete{

	
	private Context _ctx;
	private String _hw="";
	private String _host_url="";
	private String _cur_user_name="";
	private boolean _anon_reg=false;
	private ArrayList<ReqRespPair> _req_pairs=new ArrayList<ReqRespPair>();
	final String _ssl_sert_name="server.crt";
	
	public AresMiddlewareProto (Context ctx){_ctx=ctx;}

	public String getCurUserName(){return _cur_user_name;}
	
	@Override
	public MiddlewareProto clone(){ 
		return new AresMiddlewareProto(_ctx);
	}

	@Override
	public void authRequest(String id, String pin, String hw,MiddlewareProto.ProtoEvents clb) {
		_hw=hw;
		_anon_reg=(id.length()==0 && pin.length()==0);
		if (_anon_reg)
		{
			clb.onLogin(true);
			return;
		}
		//Log.d("PROTO", "AR");
		String req="{\"jsonrpc\":\"2.0\",\"id\":123,\"method\":\"register_terminal\""
					+",\"params\"  : "
					+ "{\"macaddr\": \""+hw+"\",\"username\" : \""+id+"\",\"password\" : \""+pin+"\"}}";

		//Log.d("PROTO", req);
		ReqRespPair rrp=new ReqRespPair();
		_req_pairs.add(rrp);
		rrp.req_data=new ReqDataAuth();
		rrp.clb=clb;
		rrp.resp= (SendHttpRequest) new SendHttpRequest((MiddlewareProto)this,(OnHttpRequestComplete)this).execute(_host_url+"/ca/",req,Integer.toString(rrp.getId()));
		
	}

	@Override
	public void channelsRequest(String hw,MiddlewareProto.ProtoEvents clb) {
		_hw=hw;
		//Log.d("PROTO", "CR");
		String anon="";
		if (_anon_reg)
			anon= " \"anonymous\": true,";
		//else
//			anon="\"require_username\": true,";
		
		String req="{\"jsonrpc\":\"2.0\",\"id\":123,\"method\":\"login\""
					+",\"params\"  : { \"require_username\": true," + anon
					+ "\"macaddr\": \""+hw+"\"}}";
		
		//Log.d("PROTO", req);
		ReqRespPair rrp=new ReqRespPair();
		_req_pairs.add(rrp);
		rrp.req_data=new ReqDataLogin();
		rrp.clb=clb;
		rrp.resp= (SendHttpRequest) new SendHttpRequest((MiddlewareProto)this,(OnHttpRequestComplete)this).execute(_host_url+"/ca/",req,Integer.toString(rrp.getId()));

	}
	@Override
	public void profilesRequest(String hw,MiddlewareProto.ProtoEvents clb)
	{
		_hw=hw;
		//Log.d("PROTO", "CR");
		String req="{\"jsonrpc\":\"2.0\",\"id\":123,\"method\":\"get_profiles\""
					+",\"params\"  : {"
					+ "\"macaddr\": \""+hw+"\"}}";

		//Log.d("PROTO", req);
		ReqRespPair rrp=new ReqRespPair();
		_req_pairs.add(rrp);
		rrp.req_data=new ReqDataProfiles();
		rrp.clb=clb;
		rrp.resp= (SendHttpRequest) new SendHttpRequest((MiddlewareProto)this,(OnHttpRequestComplete)this).execute(_host_url+"/ca/",req,Integer.toString(rrp.getId()));
		
	}
	@Override
	public void updateProfile(String hw, Profile p, ProtoEvents clb) {
		_hw=hw;
		//Log.d("PROTO", "CR");
		String req="{\"jsonrpc\":\"2.0\",\"id\":123,\"method\":\"update_profile\""
					+",\"params\"  : {"
					+ "\"macaddr\": \""+hw+"\",\"profile_id\":"+p.getId()+",\"profile_password\":\""+p.getPassword()+"\"}}";

		//Log.d("PROTO", req);
		ReqRespPair rrp=new ReqRespPair();
		_req_pairs.add(rrp);
		rrp.req_data=new ReqUpdateProfiles();
		rrp.clb=clb;
		rrp.resp= (SendHttpRequest) new SendHttpRequest((MiddlewareProto)this,(OnHttpRequestComplete)this).execute(_host_url+"/ca/",req,Integer.toString(rrp.getId()));
	}

	@Override
	public void terminalSettingsRequest(String hw,MiddlewareProto.ProtoEvents clb)
	{
		_hw=hw;
		//Log.d("PROTO", "CR");
		String req="{\"jsonrpc\":\"2.0\",\"id\":123,\"method\":\"get_terminal_params\""
					+",\"params\"  : {"
					+ "\"macaddr\": \""+hw+"\"}}";

		//Log.d("PROTO", req);
		ReqRespPair rrp=new ReqRespPair();
		_req_pairs.add(rrp);
		rrp.req_data=new ReqDataTParams();
		rrp.clb=clb;
		rrp.resp= (SendHttpRequest) new SendHttpRequest((MiddlewareProto)this,(OnHttpRequestComplete)this).execute("https://demo.iptvportal.ru/ca/",req,Integer.toString(rrp.getId()));
		
	}


	@Override
	public void setTerminalSettings(String hw, TerminalSettings ts,
			ProtoEvents clb) {
		_hw=hw;
		//Log.d("PROTO", "CR");
		String req="{\"jsonrpc\":\"2.0\",\"id\":123,\"method\":\"set_terminal_params\""
					+",\"params\"  : {"
					+ "\"macaddr\": \""+hw+"\",\"profile_id\":"+ts.getCurrentProfileId()+",\"use_mcast_proxy\":\""+ts.isUdpProxyUsed()+"\",\"mcast_proxy_url\":\""+ts.getUdpProxy()+"\"}}";

		//Log.d("PROTO", req);
		ReqRespPair rrp=new ReqRespPair();
		_req_pairs.add(rrp);
		rrp.req_data=new ReqUpdateProfiles();
		rrp.clb=clb;
		rrp.resp= (SendHttpRequest) new SendHttpRequest((MiddlewareProto)this,(OnHttpRequestComplete)this).execute(_host_url+"/ca/",req,Integer.toString(rrp.getId()));
		
	}
	
	private void playlistRequest(MiddlewareProto.ProtoEvents clb)
	{
		//Log.d("PROTO", "CR");
		String req="{\"jsonrpc\":\"2.0\",\"id\":123,\"method\":\"get_playlists\""
					+",\"params\"  : {"
					+ "\"macaddr\": \""+_hw+"\"}}";

		//Log.d("PROTO", req);
		ReqRespPair rrp=new ReqRespPair();
		_req_pairs.add(rrp);
		rrp.req_data=new ReqDataConfig();
		rrp.clb=clb;
		rrp.resp= (SendHttpRequest) new SendHttpRequest((MiddlewareProto)this,(OnHttpRequestComplete)this).execute(_host_url+"/jsonrpc/",req,Integer.toString(rrp.getId()));
		
	}
	@Override
	public void epgRequest(ChannelsConfig.Channel ch,Date from,Date to,MiddlewareProto.ProtoEvents clb)
	{
		//Log.d("PROTO", "ER");
		String req="{\"jsonrpc\":\"2.0\",\"id\":123,\"method\":\"get_epg\""
					+",\"params\"  : {"
				   +"\"channel_id\" : "+ ch.getId() +",";
		if (from==null || to==null)
			req=req+"\"limit\"      : 20";
		else
		{
			SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			req=req+"\"start\":\""+df.format(from)+"\", \"stop\":\""+df.format(to)+"\"";
		}
				    
							req=req+ "}}";

		//Log.d("PROTO", req);
		ReqRespPair rrp=new ReqRespPair();
		_req_pairs.add(rrp);
		rrp.req_data=new ReqDataEPG(ch);
		rrp.clb=clb;
		ch.epgLoading();
		rrp.resp= (SendHttpRequest)	new SendHttpRequest((MiddlewareProto)this,(OnHttpRequestComplete)this).execute(_host_url+"/jsonrpc/",req,Integer.toString(rrp.getId()));
		
	}

	
	
/*	@Override
	public ChannelsConfig getCannelsConfig() {
		return _ch_c;
	}*/

	@Override
	public Context getContext() {
		return _ctx;
	}

	private ReqRespPair findRRP(int id){
		ReqRespPair res=null;
		for (ReqRespPair it:_req_pairs)
		{
			if (it.getId()==id)
			{
				res=it;
				break;
			}
		}
		return res;
	}
	
	@Override
	public void onHttpRequestComplete(byte[] res,int rid) {
		
		String result="";
		if (res!=null ) result=new String(res);
		
		//result=new Scanner(result).useDelimiter("\\A").next();
		ReqRespPair rrp=findRRP(rid);
		//Log.d("PROTO","RESPONSE= "+result+" =END " +rrp );
		if (rrp!=null)
		{
			MiddlewareProto.ProtoEvents pre=rrp.clb;
			////Log.d("PROTO","RESP= "+result);
			switch (rrp.req_data.getType())
			{
			case UPDATE_PROFILE:
			case SET_TERMINAL:
				pre.onSetRequest(true);
				break;
			case TERMINAL_PARAMS:
				TerminalSettings ts=null;
				try{
					JSONObject object = (JSONObject) new JSONTokener(result).nextValue();
					object = object.getJSONObject("result");
					String udp_p="";
					try{
						udp_p=object.getString("mcast_proxy_url");
						if (udp_p.compareTo("null")==0)
							udp_p="";
					}
					catch(JSONException e){}
					boolean use_p=false;
					try{
						use_p=object.getBoolean("use_mcast_proxy");
					}
					catch(JSONException e){}
					ts=new TerminalSettings(udp_p,use_p,object.getInt("profile_id"));
				}				
				catch(JSONException e)
				{}
				pre.onTerminalSettingsLoaded(ts);
				break;
			case PROFILES:
				
				ProfilesData pd=new ProfilesData();
				try{
					JSONObject object = (JSONObject) new JSONTokener(result).nextValue();
					JSONArray prfs = object.getJSONArray("result");
					for (int i=0;i<prfs.length();++i)
					{
						JSONObject to=prfs.getJSONObject(i);
						String pass=null;
						try{pass=to.getString("password");}
						catch(JSONException e){}
						boolean rp=false;
						try{rp=to.getBoolean("require_password");}
						catch(JSONException e){}
						//Log.d("PROTO","profiles pass" + pass);
						pd.addProfile(to.getInt("id"),to.getString("name"),to.getString("title"), rp, to.getInt("age_limit"),pass);
					}
				}
				catch(JSONException e)
				{
					//Log.d("PROTO","profiles except");
				}
				pre.onProfilesLoaded(pd);
				
				break;
			case AUTH:
				try {
					JSONObject object;
					object = (JSONObject) new JSONTokener(result).nextValue();
					boolean re = object.getBoolean("result");
					pre.onLogin(re);
				} catch (JSONException e) {
					pre.onLogin(false);
				}
				break;
			case LOGIN:
			//	Log.d("PROTO","result: "+result);
				try {
					JSONObject object;
					object = (JSONObject) new JSONTokener(result).nextValue();
					boolean re=false;
					try{
						re = object.getBoolean("result");
					}
					catch(JSONException e)
					{
						try{
							JSONObject ro=object.getJSONObject("result");
							if (ro.isNull("username"))
								_anon_reg=true;
							_cur_user_name=object.getJSONObject("result").getString("username");
							re=true;
						}
						catch(JSONException e1)
						{}
					}
					if (_anon_reg)
						_cur_user_name="anonymous";
					if (re)
						playlistRequest(pre);
					else
						pre.onChannels(null);
					
				} catch (JSONException e) {
					pre.onChannels(null);
				}
				
				break;
			case CHANNELS:
				try {
					//Log.d("PROTO", "parsing channels");
					JSONObject object;
					object = (JSONObject) new JSONTokener(result).nextValue();
					object = object.getJSONObject("result");
					JSONArray ch=object.getJSONArray("medialist");
					JSONArray t=object.getJSONArray("playlists");
					
					ChannelsConfig chc=new ChannelsConfig();
					for (int i=0;i<t.length();++i)
					{
						JSONObject to=t.getJSONObject(i);
						String tname=to.getString("name");
						String ticon=to.getString("logo");
						int id=to.getInt("id");
						JSONArray ji=to.getJSONArray("items");
						//Log.d("PROTO", "parsing one topic "+tname);
						
						chc.addTopic(tname,ticon,id);
						fillTopic(ch,chc.getTopics().get(chc.getTopics().size()-1),ji);
					}

					chc.removeEmptyTopics();
			//		chc.addCommonTopic("Все");

					pre.onChannels(chc);
					
				} catch (JSONException e) {
					pre.onChannels(null);
				}
				
				break;
			case EPG:
				ReqDataEPG epgrd=(ReqDataEPG)rrp.req_data;
				try{
					//Log.d("PROTO", "parsing channels");
					JSONObject object;
					object = (JSONObject) new JSONTokener(result).nextValue();
					object = object.getJSONObject("result");
					
					JSONArray prgs=object.getJSONArray("programs");
					if (prgs.length()==0)
						epgrd.getChannel().epgFail();
					else
					{
						if (epgrd.getChannel().getAllEpgData().size()>200)
							epgrd.getChannel().epgClear();
						SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						for (int i=0; i<prgs.length();++i)
						{
							JSONObject pr=prgs.getJSONObject(i);
							String start=pr.getString("start"); 
							String stop=pr.getString("stop");
							String title=pr.getString("title");
							
							Date st=df.parse(start);
							Date sp=df.parse(stop);
							EPGData epg=new EPGData(st,sp,title);
							epgrd.getChannel().addEpgData(epg);
							
						}
						epgrd.getChannel().epgUploaded();
						pre.onEPGUploaded(epgrd.getChannel());
					}
				}catch (Exception e)
				{
					epgrd.getChannel().epgFail();
					
				}
				break;
			default:
				break;
			}
			_req_pairs.remove(rrp);
		}
	}

	private void fillTopic(JSONArray ml,ChannelsConfig.Topic t,JSONArray items)
	{
		try{
			//Log.d("PROTO", "fillTopic");
			for (int i=0;i<items.length();++i)
			{
				int ch_id=items.getInt(i);
				
				for (int j=0;j<ml.length();++j)
				{
					JSONObject ch=ml.getJSONObject(j);
				//	Log.d("PROTO","CHANNEL= "+ch.toString());
					if (ch.getInt("id")==ch_id)
					{
						
						String n=ch.getString("name");
						String m=ch.getString("mrl");
						String u=ch.getString("logo");
						String tm_url="";
						int tm_dur=0;
						try{
							tm_url=ch.getString("timeshift_url");
							tm_dur=ch.getInt("timeshift_archive_length");
						}catch(Exception e){}

						int age_r=0;
						int ind=0;
						try{
							age_r=ch.getInt("age_limit");
							ind=ch.getInt("index");
						}catch(Exception e){}
//						Log.d("PROTO","ind="+ind);
						int id=ch.getInt("channel_id");
						t.addChannel(n,m,u,id,tm_url,tm_dur,age_r,ind);
						break;
					}
				}
			}
		}
		catch(Exception e)
		{
			//Log.d("PROTO","exc="+e.toString());
		}
//		t.sortByIndex();
	}

	@Override
	public String getSSLSertName() {

		return _ssl_sert_name;
	}

	@Override
	public void setUrl(String url) {
		_host_url=url;
		
	}

	
	
	

}
