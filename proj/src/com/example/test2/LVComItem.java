package com.example.test2;

import java.io.Serializable;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

public abstract class LVComItem implements Serializable,OnChannelDataChanged {
	class AsyncImageDecoder  extends AsyncTask<byte[],Integer,Bitmap>
	{

		@Override
		protected Bitmap doInBackground(byte[]... params) {
			//Log.d("LISTITEM AID","param l= " + params.length);
			if (params.length>0)
			{
				byte[] d=params[0];
			//	Log.d("LISTITEM AID","decoding= " + d.length);
				return BitmapFactory.decodeByteArray(d, 0, d.length);
				
			}
			return null;
		}
		@Override 
		public void onPostExecute(Bitmap res)
		{
			_icon=res;
			callChangedListener();
		}
	}
	
	public final static int CHANNEL_TYPE=0;
	public final static int TOPIC_TYPE=1;
	public final static int TYPES_COUNT=2;
	
	
	private Bitmap _icon=null;
	private OnChannelDataChanged _listener=null;
	private static final long serialVersionUID = -5435670920302756945L;
	protected int _layoutResourceId;
	protected OnClickListener _cl_l;
	private ChannelsConfig.GenItem _gi;
	private int _type;
	
	public Bitmap getIcon(){return _icon;}	
	public int getType(){return _type;}

	public LVComItem(int l_id,OnClickListener cll,int type,ChannelsConfig.GenItem gi) {
		_layoutResourceId=l_id;
		_cl_l=cll;
		_type=type;
		gi.addDataChangeListener(this);
		_gi=gi;
	
	}
		
	public String getName() {return _gi.getName();}
	public void cleanup(){
		_gi.removeDataChangeListener(this);

	}
	public void callChangedListener(){
		if (_listener!=null)
		_listener.onChannelDataChanged(null);
	}

	public void setDataChangeListener(OnChannelDataChanged l){_listener=l;}
	public abstract View makeView(View v, ViewGroup parent,LayoutInflater inf);
/*	
	@Override
	public void onClick(View v) {
		if (_cl_l!=null)
			_cl_l.onClick(v);
	}*/
	
	public void uploadIcon(ChannelsConfig.GenItem gi)
	{
		if (gi.getIconBytes().length==0)
		{
			gi.uploadIcon();
		}	
		else
		if(getIcon()==null)
			onChannelDataChanged(gi);
	}
	
	@Override
	public void onChannelDataChanged(ChannelsConfig.GenItem gi) {
		new AsyncImageDecoder().execute(gi.getIconBytes());
		
	}

}