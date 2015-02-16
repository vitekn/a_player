package ru.iptvportal.player;

public class ReqDataTopics implements MiddlewareProto.ReqData{

	@Override
	public Request getType() {
		return Request.TOPICS;
	}

}
