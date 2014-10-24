package com.example.test2;

public class ReqDataLogin implements MiddlewareProto.ReqData{

	@Override
	public Request getType() {
		return Request.LOGIN;
	}

}
