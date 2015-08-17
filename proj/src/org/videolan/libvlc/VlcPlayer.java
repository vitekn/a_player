package org.videolan.libvlc;

import java.lang.ref.WeakReference;
import java.util.Date;

import org.videolan.libvlc.EventHandler;
import org.videolan.libvlc.IVideoPlayer;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaList;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup.LayoutParams;


public class VlcPlayer implements SurfaceHolder.Callback,IVideoPlayer {
	
	public interface PlayerEventsListener{
		void onPositionChanged();
	}
	private SurfaceView _surface;
	private SurfaceHolder _holder;

	// media player
	private LibVLC _libvlc=null;
	private int _videoWidth;
	private int _videoHeight;
	private boolean _is_paused=false;
	private Activity _act;
	private final static int VideoSizeChanged = -1;
	private static PlayerEventsListener _pl_events=null;
	private Date _pause_time=null;
	private long _pause_duration=0;
	
	public void setPlayerEventsListener(PlayerEventsListener pl_events){_pl_events=pl_events;}
	public VlcPlayer(SurfaceView sv,Activity act)
	{
		_surface=sv;
		_holder = _surface.getHolder();
        _holder.addCallback(this);
        _act=act;
        try{ 
        _libvlc = LibVLC.getInstance();
        _libvlc.init(_surface.getContext());
        _libvlc.setHardwareAcceleration(LibVLC.HW_ACCELERATION_FULL);
        _libvlc.setSubtitlesEncoding("");
        _libvlc.setAout(LibVLC.AOUT_OPENSLES);
//        _libvlc.setTimeStretching(true);
        _libvlc.setChroma("RV32");
        _libvlc.setVerboseMode(false);
        EventHandler.getInstance().addHandler(mHandler);
        _holder.setFormat(PixelFormat.RGBX_8888);
        _holder.setKeepScreenOn(true);
        }
        catch(Exception e){}
	}
	public void setCasPortal(String url)
	{
        _libvlc.setArescryptAuthUri(url+"/ca/");
	}
	
	@Override
	public void surfaceCreated(SurfaceHolder holder) {}

	@Override
	public void surfaceChanged(SurfaceHolder surfaceholder, int format,int width, int height) {
     //   Log.d("VLCPLAYER","sch");
		if (_libvlc != null)
			_libvlc.attachSurface(_holder.getSurface(), this);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder surfaceholder) {}

    private void setSize(int width, int height) {
      //  Log.d("VLCPLAYER","ss");
        _videoWidth = width;
        _videoHeight = height;
        if (_videoWidth *_videoHeight <= 1)
            return;

        // get screen size
        int w =_act.getWindow().getDecorView().getWidth();
        int h =_act.getWindow().getDecorView().getHeight();

//        int w=_windowWidth;
  //      int h=_windowHeight;
        
        // getWindow().getDecorView() doesn't always take orientation into
        // account, we have to correct the values
        boolean isPortrait = _surface.getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        if (w > h && isPortrait || w < h && !isPortrait) {
            int i = w;
            w = h;
            h = i;
        }

        float videoAR = (float) _videoWidth / (float) _videoHeight;
        float screenAR = (float) w / (float) h;

        if (screenAR < videoAR)
            h = (int) (w / videoAR);
        else
            w = (int) (h * videoAR);

        // force surface buffer size
        _holder.setFixedSize(_videoWidth, _videoHeight);

        // set display size
        LayoutParams lp = _surface.getLayoutParams();
        lp.width = w;
        lp.height = h;
        _surface.setLayoutParams(lp);
        _surface.invalidate();
       // Log.d("VLCPLAYER","ss ok");

    }
    
    @Override
    public void setSurfaceSize(int width, int height, int visible_width,
            int visible_height, int sar_num, int sar_den) {
      //  Log.d("VLCPLAYER","sss");
        Message msg = Message.obtain(mHandler, VideoSizeChanged, width, height);
        msg.sendToTarget();
       // Log.d("VLCPLAYER","sss ok");
    }
    
    public void setVideoURI(Uri um) {
        //release();
        String media=um.toString();
      //  Log.d("VLCPLAYER","svu "+media);
        _is_paused=false;
		_pause_time=null;
		_pause_duration=0;
        
        try {
        		stop();
            	_libvlc.playMRL(media);
            	
        } catch (Exception e) {
            //Toast.makeText(this, "Error creating player!", Toast.LENGTH_LONG).show();
        }
       // Log.d("VLCPLAYER","svu ok");
    }
    public void setPosition(float pos)
    {
		_pause_time=null;
		_pause_duration=0;
    	_libvlc.setPosition(pos);
    }
    public float getPosition(){
    	if (_libvlc!=null)
    		return _libvlc.getPosition();
    	else
    		return -1;
    }
    public boolean isSeekable()
    {
    	if (_libvlc!=null)
    		return _libvlc.isSeekable();
    	else
    		return false;
    }
    public long getPauseTime(){
    	long add=0;
    	if (_pause_time!=null)
			add=((new Date()).getTime()-_pause_time.getTime());
    		
    	return _pause_duration+add;
    	}
    public void togglePause()
    {
    	_is_paused= !_is_paused;    	
    	if (_is_paused)
    	{
    		_pause_time=new Date();
    		_libvlc.pause();
    	}
    	else
    	{
    		if (_pause_time!=null)
    			_pause_duration+=((new Date()).getTime()-_pause_time.getTime());
    		_pause_time=null;
    		_libvlc.play();
    	}
    }
    public boolean isPaused() {return _is_paused;}
    
    
    public void stop()
    {
		_pause_time=null;
		_pause_duration=0;
        if (_libvlc.isPlaying())
        	_libvlc.stop();
    }
    
    public void start(){
 //       Log.d("VLCPLAYER","st");
        //if (_libvlc!=null)
        	//_libvlc.playIndex(0);
   //     Log.d("VLCPLAYER","st ok");
    }
    public void release() {
     //   Log.d("VLCPLAYER","r");
        if (_libvlc == null)
            return;
        EventHandler.getInstance().removeHandler(mHandler);
        _libvlc.stop();
        _libvlc.detachSurface();
      //  _holder = null;
        _libvlc.closeAout();
        _libvlc.destroy();
        _libvlc = null;

        _videoWidth = 0;
        _videoHeight = 0;
     //   Log.d("VLCPLAYER","rok");
    }
    
	private Handler mHandler = new MyHandler(this);
	
    private static class MyHandler extends Handler {
        private WeakReference<VlcPlayer> mOwner;

        public MyHandler(VlcPlayer owner) {
            mOwner = new WeakReference<VlcPlayer>(owner);
        }

        @Override
        public void handleMessage(Message msg) {
        	VlcPlayer player = mOwner.get();

            // SamplePlayer events
            if (msg.what == VideoSizeChanged) {
                player.setSize(msg.arg1, msg.arg2);
               // Log.d("VLCPLAYER","hm ok1");
                return;
            }

            // Libvlc events
            Bundle b = msg.getData();
            switch (b.getInt("event")) {
            case EventHandler.MediaPlayerPositionChanged:
            	if (_pl_events!=null)
            		_pl_events.onPositionChanged();
            	break;
            case EventHandler.MediaPlayerEndReached:
             //   player.release();
                break;
            case EventHandler.MediaPlayerPlaying:
            case EventHandler.MediaPlayerPaused:
            case EventHandler.MediaPlayerStopped:
            default:
                break;
            }
        }
    }
}
