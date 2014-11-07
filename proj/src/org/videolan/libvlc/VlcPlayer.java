package org.videolan.libvlc;

import java.lang.ref.WeakReference;

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
	
	private SurfaceView _surface;
	private SurfaceHolder _holder;

	// media player
	private LibVLC _libvlc=null;
	private int _videoWidth;
	private int _videoHeight;
	private Activity _act;
	private final static int VideoSizeChanged = -1;
	
	public VlcPlayer(SurfaceView sv,Activity act)
	{
		_surface=sv;
		_holder = _surface.getHolder();
        _holder.addCallback(this);
        _act=act;
        try{
        Log.d("VLCPLAYER","svu gi");
        _libvlc = LibVLC.getInstance();
        Log.d("VLCPLAYER","svu ha");
        _libvlc.setHardwareAcceleration(LibVLC.HW_ACCELERATION_FULL);
        Log.d("VLCPLAYER","svu st");
        _libvlc.setSubtitlesEncoding("");
        Log.d("VLCPLAYER","svu aout");
        _libvlc.setAout(LibVLC.AOUT_OPENSLES);
        Log.d("VLCPLAYER","svu ts");
//        _libvlc.setTimeStretching(true);
        Log.d("VLCPLAYER","svu hr");
        _libvlc.setChroma("RV32");
        Log.d("VLCPLAYER","svu vm");
        _libvlc.setVerboseMode(false);
        Log.d("VLCPLAYER","svu rest");
        //_libvlc.init(_surface.getContext());
        LibVLC.restart(_surface.getContext());
        Log.d("VLCPLAYER","svu addh");
        EventHandler.getInstance().addHandler(mHandler);
        Log.d("VLCPLAYER","svu setpf");
        _holder.setFormat(PixelFormat.RGBX_8888);
        Log.d("VLCPLAYER","svu kons");
        _holder.setKeepScreenOn(true);
        Log.d("VLCPLAYER","svu ml");
        }
        catch(Exception e){}
        Log.d("VLCPLAYER","constr");
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {}

	@Override
	public void surfaceChanged(SurfaceHolder surfaceholder, int format,int width, int height) {
        Log.d("VLCPLAYER","sch");
		if (_libvlc != null)
			_libvlc.attachSurface(_holder.getSurface(), this);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder surfaceholder) {}

    private void setSize(int width, int height) {
        Log.d("VLCPLAYER","ss");
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
        Log.d("VLCPLAYER","ss ok");

    }
    
    @Override
    public void setSurfaceSize(int width, int height, int visible_width,
            int visible_height, int sar_num, int sar_den) {
        Log.d("VLCPLAYER","sss");
        Message msg = Message.obtain(mHandler, VideoSizeChanged, width, height);
        msg.sendToTarget();
        Log.d("VLCPLAYER","sss ok");
    }
    
    public void setVideoURI(Uri um) {
        Log.d("VLCPLAYER","svu");
        //release();
        String media=um.toString();
        try {
/*            if (media.length() > 0) {
                Toast toast = Toast.makeText(this, media, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0,
                        0);
                toast.show();
            }*/

            // Create a new media player
            if (_libvlc.isPlaying())
            	_libvlc.stop();
//            MediaList list = _libvlc.getMediaList();
  //          Log.d("VLCPLAYER","svu mlc");
    //        list.clear();
      //      Log.d("VLCPLAYER","svu addm");
        //    list.add(new Media(_libvlc, media), false);
//            _libvlc.playIndex(0);
            	_libvlc.playMRL(media);
        } catch (Exception e) {
            //Toast.makeText(this, "Error creating player!", Toast.LENGTH_LONG).show();
        }
        Log.d("VLCPLAYER","svu ok");
    }
    public void start(){
        Log.d("VLCPLAYER","st");
        //if (_libvlc!=null)
        	//_libvlc.playIndex(0);
        Log.d("VLCPLAYER","st ok");
    }
    public void release() {
        Log.d("VLCPLAYER","r");
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
        Log.d("VLCPLAYER","rok");
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
                Log.d("VLCPLAYER","hm ok1");
                return;
            }

            // Libvlc events
            Bundle b = msg.getData();
            switch (b.getInt("event")) {
            case EventHandler.MediaPlayerEndReached:
                player.release();
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
