package com.example.test2;

import com.example.test2.MiddlewareProto.ReqData;

public class ReqDataTParams implements ReqData {

	@Override
	public Request getType() {
		// TODO Auto-generated method stub
		return Request.TERMINAL_PARAMS;
	}

}
