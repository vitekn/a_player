package com.example.test2;

public class ReqDataConfig implements MiddlewareProto.ReqData{

	@Override
	public Request getType() {
		return Request.CHANNELS;
	}
}
