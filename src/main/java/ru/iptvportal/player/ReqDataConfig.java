package ru.iptvportal.player;

public class ReqDataConfig implements MiddlewareProto.ReqData{

	@Override
	public Request getType() {
		return Request.CHANNELS;
	}
}
