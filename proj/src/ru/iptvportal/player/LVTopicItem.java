package ru.iptvportal.player;


import com.example.test2.R;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

public class LVTopicItem extends LVComItem {

	private static final long serialVersionUID =-5435670920302756947L;
	private ChannelsConfig.Topic _my_topic;
	private boolean _selected;
	
	public LVTopicItem( ChannelsConfig.Topic t,int l_id,OnClickListener cll) {
		super(l_id, cll, LVComItem.TOPIC_TYPE,t);
		_my_topic=t;
	}

	public ChannelsConfig.Topic getTopic() {return _my_topic;}
	@Override
	public View makeView(View v, ViewGroup parent, LayoutInflater inf) {

	//	//Log.d("LVTopic","makeView "+ _my_topic.getName()+" "+_selected);
		if (v==null)
		{
			v = inf.inflate(_layoutResourceId, parent, false);
		}
		
		TextView name=(TextView)v.findViewById(R.id.textView1);
		if (name!=null)
		{
			name.setText(_my_topic.getName());
			name.setTag(this);
			if (_cl_l!=null)
				name.setOnClickListener(_cl_l);
		}
		ImageButton but=(ImageButton)v.findViewById(R.id.topicButton);
		if (but!=null)
		{
			uploadIcon(_my_topic);
			but.setTag(this);
			but.setImageBitmap(getIcon());
			if (_selected)
				but.setBackgroundResource(R.drawable.button_hl_grad);
			else
				but.setBackgroundResource(R.drawable.button_nhl_grad);
			
			if (_cl_l!=null)
				but.setOnClickListener(_cl_l);
		}
		
		return v;
	}

	public void setSelected(boolean s){_selected=s;}
	public boolean getSelected(){return _selected;}
}
