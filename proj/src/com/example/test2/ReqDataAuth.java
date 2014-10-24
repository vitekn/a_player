package com.example.test2;

public class ReqDataAuth implements MiddlewareProto.ReqData{

	@Override
	public Request getType() {
		return Request.AUTH;
	}
}
