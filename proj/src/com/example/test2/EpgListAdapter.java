package com.example.test2;

import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class EpgListAdapter extends ArrayAdapter<EPGData> {

	private List<EPGData> _items;
	private int _layoutResourceId;
	private Context _context;
	private OnListBoundReached _clb;
	
	public EpgListAdapter(Context context, int textViewResourceId,	List<EPGData> items,OnListBoundReached clb) {
		super(context, textViewResourceId,R.id.prog_title , items);
		_items=items;
		_context=context;
		_layoutResourceId=textViewResourceId;
		_clb=clb;
	}
	public void setItems(List<EPGData> items)
	{
		_items=items;
		notifyDataSetChanged();
		
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
			View row = convertView;
			LayoutInflater inflater = ((Activity) _context).getLayoutInflater();
			row = inflater.inflate(_layoutResourceId, parent, false);
			EPGData ei=_items.get(position);
			TextView tv=(TextView)row.findViewById(R.id.prog_start);
			tv.setText(ei.getStartDayTime());
			if (!ei.getUrl().isEmpty())
				tv.setTextColor(0xFFC0FFC0);
			else
				if (ei.getStart().before(new Date()))
					tv.setTextColor(0xFFD0D0D0);
			tv.setTag(ei);
				
			tv=(TextView)row.findViewById(R.id.prog_title);
			tv.setText(ei.getTitle());
			tv.setTag(ei);
			row.setTag(ei);
			if (position==0)
				_clb.upperBound();
			else if (position==(_items.size()-1))
				_clb.lowerBound();
			return row;
		}

	

}
