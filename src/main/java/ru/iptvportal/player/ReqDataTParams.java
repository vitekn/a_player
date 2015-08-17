package ru.iptvportal.player;

import ru.iptvportal.player.MiddlewareProto.ReqData;

public class ReqDataTParams implements ReqData {

	@Override
	public Request getType() {
		// TODO Auto-generated method stub
		return Request.TERMINAL_PARAMS;
	}

}
