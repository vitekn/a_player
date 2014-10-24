package com.example.test2;

public class ReqDataTopics implements MiddlewareProto.ReqData{

	@Override
	public Request getType() {
		return Request.TOPICS;
	}

}
