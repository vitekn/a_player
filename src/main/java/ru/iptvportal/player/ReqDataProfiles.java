package ru.iptvportal.player;


public class ReqDataProfiles implements MiddlewareProto.ReqData{

	@Override
	public Request getType() {
		return Request.PROFILES;
	}
}
