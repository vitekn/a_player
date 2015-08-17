package ru.iptvportal.player;

public class ReqDataEPG implements MiddlewareProto.ReqData{

	private ChannelsConfig.Channel _ch;
	
	public ReqDataEPG(ChannelsConfig.Channel ch){_ch=ch;}
	public ChannelsConfig.Channel getChannel(){return _ch;}
	@Override
	public Request getType() {
		return Request.EPG;
	}
	
}
