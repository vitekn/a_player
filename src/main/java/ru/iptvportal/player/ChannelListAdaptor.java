package ru.iptvportal.player;

import java.util.ArrayList;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Button;

public class ChannelListAdaptor extends ArrayAdapter<Button> {
	Context context;
	int layoutResourceId;
	ArrayList<Button> data = new ArrayList<Button>();


	public ChannelListAdaptor(Context context, int layoutResourceId,
			ArrayList<Button> data ) {
		super(context, layoutResourceId, data);
		 this.layoutResourceId = layoutResourceId;
		 this.context = context;
		 this.data = data;
	}

}
