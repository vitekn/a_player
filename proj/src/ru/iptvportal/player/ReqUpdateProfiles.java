package ru.iptvportal.player;

import ru.iptvportal.player.MiddlewareProto.ReqData;

public class ReqUpdateProfiles implements ReqData {

	@Override
	public Request getType() {
		return Request.UPDATE_PROFILE;
	}

}
