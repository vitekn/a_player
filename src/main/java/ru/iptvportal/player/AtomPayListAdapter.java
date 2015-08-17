package ru.iptvportal.player;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

public class AtomPayListAdapter extends /*ArrayAdapter<LVComItem>*/BaseAdapter implements OnChannelDataChanged{

	protected static final String LOG_TAG = AtomPayListAdapter.class.getSimpleName();
	
	private Handler _change_delay=new Handler(new Handler.Callback(){

		@Override
		public boolean handleMessage(Message msg) {
			_timer_on=false;
			notifyDataSetChanged();
			return false;
		}
		
	});	
	private boolean _timer_on=false;
	private ArrayList<LVComItem> items;
	private ArrayList<LVComItem> items_orig;
	private String _filter="";
	LayoutInflater _inflater;
	

	public AtomPayListAdapter(Context context) {
		this.items = new ArrayList<LVComItem>();
		this.items_orig = new ArrayList<LVComItem>();
		_inflater = ((Activity) context).getLayoutInflater();
	}

	public void setFilter(String f)
	{
		_filter=f;
		if (f==null || f.isEmpty())
		{
			items.clear();
			items.addAll(items_orig);
			
		}
		else
		{
			items.clear();
			for (LVComItem i:items_orig)
			{
				addFiltered(i);
			}
		}
		
		notifyDataSetChanged();
	}
	
	private void addFiltered(LVComItem i)
	{
		//Log.d("LISTTEXT","filter |" + _filter+"|");
		if (_filter==null || _filter.isEmpty() || i.getName().toLowerCase().startsWith(_filter.toLowerCase()))
		{
			items.add(i);
		}
		
	}
	
	@Override 
	public int getViewTypeCount() {
		return LVComItem.TYPES_COUNT;
	}
	
	@Override
    public int getItemViewType(int position) {
        return items.get(position).getType();
    }
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		LVComItem ci=items.get(position);
		ci.setDataChangeListener(this);
		return ci.makeView(row,parent,_inflater);

	}
	
	@Override
	public void onChannelDataChanged(ChannelsConfig.GenItem gi) {
		if (_timer_on)
			return;
		_timer_on=true;
		_change_delay.sendEmptyMessageDelayed(0, 1500);
	}
	
	//@Override 
	public void clear()
	{
		for (LVComItem p: this.items)
		{
			p.cleanup();
		}
		items.clear();
		items_orig.clear();
//		super.clear();
	}

	public void add (LVComItem i)
	{
		items_orig.add(i);
		addFiltered(i);
		
	}
	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public Object getItem(int position) {
		
		return items.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
}
