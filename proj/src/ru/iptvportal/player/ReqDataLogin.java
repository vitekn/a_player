package ru.iptvportal.player;

public class ReqDataLogin implements MiddlewareProto.ReqData{

	@Override
	public Request getType() {
		return Request.LOGIN;
	}

}
