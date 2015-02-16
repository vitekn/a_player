package ru.iptvportal.player;

public class ReqDataAuth implements MiddlewareProto.ReqData{

	@Override
	public Request getType() {
		return Request.AUTH;
	}
}
