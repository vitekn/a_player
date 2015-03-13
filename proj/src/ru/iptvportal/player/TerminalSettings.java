package ru.iptvportal.player;

public class TerminalSettings {
	private String _udp_proxy="";
	private boolean _use_udp_proxy=false;
	private int _current_profile=0;
	public TerminalSettings(){}
	public TerminalSettings(String udp_p,boolean use_udp_p,int cur_prof)
	{
		_udp_proxy=udp_p;
		_use_udp_proxy=use_udp_p;
		_current_profile=cur_prof;
	}
	public String getUdpProxy(){return _udp_proxy;}
	public boolean isUdpProxyUsed(){return _use_udp_proxy;}
	public int getCurrentProfileId(){return _current_profile;}
	public void setUdpProxy(String up){_udp_proxy=up;}
	public void setUdpProxyUsed(boolean u){_use_udp_proxy=u;}
	public void setCurrentProfileId(int id){_current_profile=id;}
	
}
