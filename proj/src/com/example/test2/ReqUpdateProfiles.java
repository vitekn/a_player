package com.example.test2;

import com.example.test2.MiddlewareProto.ReqData;

public class ReqUpdateProfiles implements ReqData {

	@Override
	public Request getType() {
		return Request.UPDATE_PROFILE;
	}

}
