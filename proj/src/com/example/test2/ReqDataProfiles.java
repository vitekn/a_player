package com.example.test2;


public class ReqDataProfiles implements MiddlewareProto.ReqData{

	@Override
	public Request getType() {
		return Request.PROFILES;
	}
}
