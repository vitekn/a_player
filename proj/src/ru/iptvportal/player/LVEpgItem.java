package ru.iptvportal.player;

import java.util.Date;

import ru.iptvportal.player.ChannelsConfig.GenItem;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.test2.R;

public class LVEpgItem extends LVComItem {

	private static final long serialVersionUID = -5435670920302756945L;
	private EPGData _epg;
	
	public LVEpgItem(EPGData epg, int l_id, OnClickListener cll, int type, GenItem gi) {
		super(l_id, cll, type, gi);
		_epg=epg;
	}
	public EPGData getEPGData(){return _epg;}
	
	@Override
	public View makeView(View v, ViewGroup parent, LayoutInflater inf) {

		if (v==null)
		{
			v = inf.inflate(_layoutResourceId, parent, false);
		}
		TextView epg=(TextView)v.findViewById(R.id.channel_epg);
		epg.setTag(this);
		epg.setOnClickListener(_cl_l);
		TextView name=(TextView)v.findViewById(R.id.atomPay_name1);
		name.setOnClickListener(_cl_l);
		name.setTag(this);
		return v;
	}

}
