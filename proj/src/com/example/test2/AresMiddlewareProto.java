package com.example.test2;


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

import android.content.Context;
import android.util.Log;


public class AresMiddlewareProto  implements MiddlewareProto,OnHttpRequestComplete{

	
	private Context _ctx;
	private String _hw="";
	private ArrayList<ReqRespPair> _req_pairs=new ArrayList<ReqRespPair>();
	final String _ssl_sert_name="server.crt";
	
	public AresMiddlewareProto (Context ctx){_ctx=ctx;}

	@Override
	public MiddlewareProto clone(){ 
		return new AresMiddlewareProto(_ctx);
	}

	@Override
	public void authRequest(String id, String pin, String hw,MiddlewareProto.ProtoEvents clb) {
		_hw=hw;
		Log.d("PROTO", "AR");
		String req="{\"jsonrpc\":\"2.0\",\"id\":123,\"method\":\"register_terminal\""
					+",\"params\"  : "
					+ "{\"macaddr\": \""+hw+"\",\"username\" : \""+id+"\",\"password\" : \""+pin+"\"}}";

		Log.d("PROTO", req);
		ReqRespPair rrp=new ReqRespPair();
		_req_pairs.add(rrp);
		rrp.req_data=new ReqDataAuth();
		rrp.clb=clb;
		rrp.resp= (SendHttpRequest) new SendHttpRequest((MiddlewareProto)this,(OnHttpRequestComplete)this).execute("https://demo.iptvportal.ru/ca/",req,Integer.toString(rrp.getId()));
		
	}

	@Override
	public void channelsRequest(String hw,MiddlewareProto.ProtoEvents clb) {
		_hw=hw;
		Log.d("PROTO", "CR");
		String req="{\"jsonrpc\":\"2.0\",\"id\":123,\"method\":\"login\""
					+",\"params\"  : {"
					+ "\"macaddr\": \""+hw+"\"}}";

		Log.d("PROTO", req);
		ReqRespPair rrp=new ReqRespPair();
		_req_pairs.add(rrp);
		rrp.req_data=new ReqDataLogin();
		rrp.clb=clb;
		rrp.resp= (SendHttpRequest) new SendHttpRequest((MiddlewareProto)this,(OnHttpRequestComplete)this).execute("https://demo.iptvportal.ru/ca/",req,Integer.toString(rrp.getId()));

	}

	private void playlistRequest(MiddlewareProto.ProtoEvents clb)
	{
		Log.d("PROTO", "CR");
		String req="{\"jsonrpc\":\"2.0\",\"id\":123,\"method\":\"get_playlists\""
					+",\"params\"  : {"
					+ "\"macaddr\": \""+_hw+"\"}}";

		Log.d("PROTO", req);
		ReqRespPair rrp=new ReqRespPair();
		_req_pairs.add(rrp);
		rrp.req_data=new ReqDataConfig();
		rrp.clb=clb;
		rrp.resp= (SendHttpRequest) new SendHttpRequest((MiddlewareProto)this,(OnHttpRequestComplete)this).execute("https://demo.iptvportal.ru/jsonrpc/",req,Integer.toString(rrp.getId()));
		
	}
	@Override
	public void epgRequest(ChannelsConfig.Channel ch,Date from,Date to,MiddlewareProto.ProtoEvents clb)
	{
		Log.d("PROTO", "ER");
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

		Log.d("PROTO", req);
		ReqRespPair rrp=new ReqRespPair();
		_req_pairs.add(rrp);
		rrp.req_data=new ReqDataEPG(ch);
		rrp.clb=clb;
		rrp.resp= (SendHttpRequest)	new SendHttpRequest((MiddlewareProto)this,(OnHttpRequestComplete)this).execute("https://demo.iptvportal.ru/jsonrpc/",req,Integer.toString(rrp.getId()));
		
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
		String result=new String(res);
		//result=new Scanner(result).useDelimiter("\\A").next();
		ReqRespPair rrp=findRRP(rid);
		if (rrp!=null)
		{
			MiddlewareProto.ProtoEvents pre=rrp.clb;
			//Log.d("PROTO","RESP= "+result);
			switch (rrp.req_data.getType())
			{
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
				try {
					JSONObject object;
					object = (JSONObject) new JSONTokener(result).nextValue();
					boolean re = object.getBoolean("result");
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
					Log.d("PROTO", "parsing channels");
					JSONObject object;
					object = (JSONObject) new JSONTokener(result).nextValue();
					object = object.getJSONObject("result");
					JSONArray ch=object.getJSONArray("medialist");
					JSONArray t=object.getJSONArray("playlists");
					
					ChannelsConfig chc=new ChannelsConfig();
					for (int i=0;i<t.length();++i)
					{
						Log.d("PROTO", "parsing one topic");
						JSONObject to=t.getJSONObject(i);
						String tname=to.getString("name");
						String ticon=to.getString("logo");
						int id=to.getInt("id");
						JSONArray ji=to.getJSONArray("items");
						
						chc.addTopic(tname,ticon,id);
						fillTopic(ch,chc.getTopics().get(chc.getTopics().size()-1),ji);
					}

					chc.removeEmptyTopics();
					chc.addCommonTopic("Все");

					pre.onChannels(chc);
					
				} catch (JSONException e) {
					pre.onChannels(null);
				}
				
				break;
			case EPG:
				ReqDataEPG epgrd=(ReqDataEPG)rrp.req_data;
				try{
					Log.d("PROTO", "parsing channels");
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
			Log.d("PROTO", "fillTopic");
			for (int i=0;i<items.length();++i)
			{
				int ch_id=items.getInt(i);
				
				for (int j=0;j<ml.length();++j)
				{
					JSONObject ch=ml.getJSONObject(j);
					if (ch.getInt("id")==ch_id)
					{
						String n=ch.getString("name");
						String m=ch.getString("mrl");
						String u=ch.getString("logo");
						String tm_url=ch.getString("timeshift_url");
						int tm_dur=ch.getInt("timeshift_archive_length");
						int id=ch.getInt("channel_id");
						t.addChannel(n,m,u,id,tm_url,tm_dur);
						break;
					}
				}
			}
		}
		catch(Exception e)
		{
			Log.d("PROTO","exc="+e.toString());
			return;
		}
	}

	@Override
	public String getSSLSertName() {

		return _ssl_sert_name;
	}
	
	
	

}
